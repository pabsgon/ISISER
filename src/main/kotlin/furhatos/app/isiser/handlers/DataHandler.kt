package furhatos.app.isiser.handlers

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import furhatos.app.isiser.questions.Question
import furhatos.app.isiser.setting.*
import java.io.FileInputStream

data class DataHandler(val evFactory: EventFactory,
    val statements: MutableList<Statement> = mutableListOf(),
    val conditions: MutableMap<EnumConditions, List<EnumRobotMode>> = mutableMapOf(),
    val users: MutableMap<Int, EnumConditions> = mutableMapOf()
){

    fun getSheetsService(): Sheets {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val credentialsFilePath = SOURCEDATA_CREDENTIAL_FILE_PATH


        val credential = GoogleCredential.fromStream(FileInputStream(credentialsFilePath))
            .createScoped(listOf(SheetsScopes.SPREADSHEETS_READONLY))

        return Sheets.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("Your Application Name")
            .build()
    }

    fun loadSheetData(questions: MutableList<Question>) {
        val spreadsheetId = SOURCEDATA_SPREADSHEETID // Your spreadsheet ID
        val range = SOURCEDATA_RANGE

        val service = getSheetsService()
        val response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute()
        processData( response.getValues(),questions)
    }



    fun processData(data: List<List<Any>>,questions: MutableList<Question>) {
        // Iterate over each row in the data
        var stCount = 0
        var qnlist: List<Any>? = null
        var qalist: List<Any>? = null
        data.forEach { row ->
            val it = if (row.isNotEmpty()) row[0].toString() else ""
            if(it.isNotEmpty()) {
                println("Loading: $it")
                when (it) {
                    "STATEMENT" -> {
                        stCount++
                        // Check if the row has enough elements to split into two parts
                        if (row.size >= SOURCEDATA_SETTINGS_SIZE +1) {
                            // The first 8 elements go into list1
                            val list1 = row.subList(1, SOURCEDATA_SETTINGS_SIZE +1)

                            // The rest from item 9 to the end of non-empty items go into list2
                            val list2 = mutableListOf<Any>()
                            if (row.size > SOURCEDATA_SETTINGS_SIZE +1) {
                                for (i in SOURCEDATA_SETTINGS_SIZE +1 until row.size) {
                                    if (row[i].toString().isNotEmpty()) { // Ensure the item is not empty
                                        list2.add(row[i])
                                    }
                                }
                            }

                            // Call the processLine function with the two lists
                            processLine(questions, stCount, list1, list2)
                        }
                    }
                    "QUESTION" -> {qnlist = row.subList(1, MAX_QUESTIONS +1)}
                    "CORRECT_ANSWER" ->{qalist = row.subList(1, MAX_QUESTIONS +1)}
                    "ROBOT_ANSWER" ->
                    {
                        val list1 = row.subList(1, MAX_QUESTIONS +1)
                        for (i in 0 until list1.size) {
                            val id = qnlist!![i].toString()
                            val cAns = EnumAnswer.fromString(qalist!![i].toString())
                            if(cAns.equals(EnumAnswer.UNDEFINED)) {
                                error("Unexpected value in row \"CORRECT_ANSWER\" col[{$i}]. Expected TRUE/FALSE.")
                            }
                            val rAns = EnumAnswer.fromString(list1[i].toString())
                            if(rAns.equals(EnumAnswer.UNDEFINED)) {
                                error("Unexpected value in row \"ROBOT_ANSWER\" col[{$i}]. Expected TRUE/FALSE.")
                            }
                            questions.add(Question(id,cAns,rAns))
                        }
                    }
                    "CONDITION1", "CONDITION2", "CONDITION3"  ->
                    {
                        if (row.size >= MAX_QUESTIONS +1) {
                            // The first 8 elements go into list1
                            val rbList = row.subList(1, MAX_QUESTIONS + 1).mapNotNull {
                                try {
                                    EnumRobotMode.valueOf(it.toString())
                                } catch (e: IllegalArgumentException) {
                                    error("Error loading data in $it: expected NEUTRAL, CERTAIN or UNCERTAIN")
                                    null  // Return null for invalid items, mapNotNull will filter them out
                                }
                            }

                            val unList = mutableListOf<Int>()
                            if (row.size > MAX_QUESTIONS + 1) {
                                for (i in MAX_QUESTIONS + 1 until row.size) {
                                    val itemString = row[i].toString().trim()  // Trim any whitespace
                                    if (itemString.isNotEmpty()) { // Ensure the item is not empty
                                        try {
                                            val itemInt = itemString.toInt()  // Try to convert the string to an integer
                                            unList.add(itemInt)
                                        } catch (e: NumberFormatException) {
                                            // Handle the case where the string cannot be converted to an integer
                                            println("Error loading data in $it: value '$itemString' should be integer.")
                                        }
                                    }
                                }
                            }
                            createCondition(
                                EnumConditions.fromString(it.toString()),
                                rbList,
                                unList
                            )
                        }
                    }
                }
            }else{
                println("Skipping row...")
            }
        }
    }
    private fun createCondition(cond: EnumConditions, rmlist: List<EnumRobotMode>, unlist: List<Int>) {
        val enumList = rmlist.map { EnumRobotMode.valueOf(it.toString()) }
        conditions[cond] = enumList
        unlist.map {it }.forEach { key -> users[key] = cond }
    }
    private fun addStatement(questions: MutableList<Question>, qIndex: Int, id: String, type: EnumStatementTypes,
                             subType: Boolean, stIndex: Int, perTriplet: Boolean,
                             vararg texts: String) {
        //qIndex is 0-based, must be
        val textTriplets = if (perTriplet) {
            mutableListOf<TextTriplet>()  // Initialize as an empty list
        } else {
            texts.map { TextTriplet(it) }.toMutableList()  // Map texts to TextTriplets. Each triplet repeated thrice.
        }
        if(perTriplet) {
            for (i in texts.indices step 3) {
                val neutral = texts.getOrNull(i) ?: break
                val uncertain = texts.getOrNull(i + 1) ?: neutral
                val certain = texts.getOrNull(i + 2) ?: uncertain
                textTriplets.add(TextTriplet(neutral, uncertain, certain))
            }
        }
        if(type.equals(EnumStatementTypes.CLAIM)){
            addClaim(questions, qIndex, id, subType, textTriplets)
        }else{
            val s = Statement(id, type, textTriplets, subType) // subType=true means "Indexical" if Assertion, and Friendly if Ultimatum or Checkpoint.
            statements.add(s)
            if (qIndex < 0) { //This means that the source said "ANY" question.
                // Call addStatement on all items in the list
                questions.forEach { it.setStatement(s, stIndex) }
            } else {
                // Call addStatement on the item at qIndex if it's within the bounds of the list
                questions.getOrNull(qIndex)?.setStatement(s, stIndex) ?: println("Error loading data: Question Index is out of bounds")
            }
        }
    }


    private fun addClaim(questions: MutableList<Question>, qIndex: Int, id: String, subType: Boolean, textTriplets:  MutableList<TextTriplet> ) {
    //Here the list of triplets will contain the 1) the UNFRIENDLY claim, and the rest of triplets must be used to create a statement, which
        //will be added to the list of friendly statements of the claim.

        if (qIndex < 0) {
            error("Error loading data: Question Index = [1.. $MAX_QUESTIONS]")
        } else {
            val tempStatements: MutableList<Statement> = mutableListOf()

            // Add the unfriendly statement (first triplet)
            val s1 = Statement(id + UNFRIENDLY_SUFFIX, EnumStatementTypes.CLAIM, textTriplets.subList(0, 1).toMutableList(), subType)
            tempStatements.add(s1)

            // Add the remaining triplets as friendly statements
            for (i in 1 until textTriplets.size) {
                val s = Statement(id + FRIENDLY_SUFFIX + i, EnumStatementTypes.CLAIM, mutableListOf(textTriplets[i]), subType)
                tempStatements.add(s)
            }

            // Call addClaim on the item at qIndex if it's within the bounds of the list
            questions.getOrNull(qIndex)?.addClaim(Claim(id, tempStatements)) ?: println("Error loading data: Question Index is out of bounds")
        }
    }


    fun processLine(questions: MutableList<Question>, c: Int, settings: List<Any>, texts: List<Any>) {
        val textsAsStringArray = texts.map { it.toString() }.toTypedArray()
        val id = settings[SOURCEDATA_ID].toString()
        val qIndex = questionIndexToInt(settings[SOURCEDATA_QUESTION].toString(),c)
        val type: EnumStatementTypes = EnumStatementTypes.fromString(settings[SOURCEDATA_TYPE].toString())
        val subType = subTypeToBool(type, settings[SOURCEDATA_SUBTYPE].toString())
        val stIndex = questionIndexToInt(settings[SOURCEDATA_STATEMENT_INDEX].toString(),c)
        val pertriplet = settings[SOURCEDATA_PERTRIPLET].toString() == SOURCEDATA_TRUE


        println("DATA LOADING: ----[$id]-----")
        println("Calling addStatement with values - qIndex: $qIndex, id: $id, type: $type, subType: $subType, stIndex: $stIndex, pertriplet: $pertriplet, texts: ${textsAsStringArray.joinToString(", ")}")

        addStatement(questions, qIndex,id, type, subType, stIndex, pertriplet, *textsAsStringArray)

    }
    fun questionIndexToInt(str: String, stNum: Int): Int {
        //This returns a 0-based index. In the DATASOURCE is 1-based. Here is the conversion done.
        return when {
            str.toUpperCase() == "ANY" || str.isEmpty() -> -1
            else -> {
                try {
                    str.toInt() -1
                } catch (e: NumberFormatException) {
                    error("Error loading data: The data file contains a invalid value in statement $stNum col 2 (expected an INT or 'ANY'")
                }

            }
        }
    }
    fun subTypeToBool(type: EnumStatementTypes, subType: String): Boolean {
        return when (type) {
            EnumStatementTypes.CHECKPOINT, EnumStatementTypes.ULTIMATUM -> subType== SOURCEDATA_FRIENDLY
            EnumStatementTypes.ASSERTION -> subType== SOURCEDATA_TRUE
            else -> false
        }
    }

    fun printStatements() {
        println("Printing all statements:")
        statements.forEach { println(it) }
    }
    fun printConditions() {
        println("Printing all conditions (${conditions.size}):")
        conditions.forEach { println(it) }
    }

    fun getRobotModesForCondition(userCondition: EnumConditions): List<EnumRobotMode> {
        val robotModes: List<EnumRobotMode>? = conditions[userCondition]
        return robotModes!!
    }
    fun getConditionForUser(s: String): EnumConditions {
        val userCondition: EnumConditions
        try {
            // 1) Convert the parameter to an integer
            val userId = s.toInt()

            // 2) Use the map users to obtain the EnumCondition
            userCondition = users[userId]
                ?: throw IllegalStateException("No condition found for user ID $userId")

        } catch (e: NumberFormatException) {
            error("Error converting '$s' to an integer: ${e.message}")
        } catch (e: Exception) {
            error("Error getting condition for user: ${e.message}")
        }
        return userCondition
    }

}


