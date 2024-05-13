package furhatos.app.isiser.setting

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import furhatos.app.isiser.Session
import java.io.FileInputStream

data class Data(
    val statements: MutableList<Statement> = mutableListOf(),
    val conditions: MutableMap<EnumConditions, List<EnumRobotMode>> = mutableMapOf(),
    val users: MutableMap<Int, EnumConditions> = mutableMapOf()
)
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

fun loadSheetData() {
    val spreadsheetId = SOURCEDATA_SPREADSHEETID // Your spreadsheet ID
    val range = SOURCEDATA_RANGE

    val service = getSheetsService()
    val response = service.spreadsheets().values()
        .get(spreadsheetId, range)
        .execute()
    processData( response.getValues())
}



fun processData(data: List<List<Any>>) {
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
                    if (row.size >= SOURCEDATA_SETTINGS_SIZE+1) {
                        // The first 8 elements go into list1
                        val list1 = row.subList(1, SOURCEDATA_SETTINGS_SIZE+1)

                        // The rest from item 9 to the end of non-empty items go into list2
                        val list2 = mutableListOf<Any>()
                        if (row.size > SOURCEDATA_SETTINGS_SIZE+1) {
                            for (i in SOURCEDATA_SETTINGS_SIZE+1 until row.size) {
                                if (row[i].toString().isNotEmpty()) { // Ensure the item is not empty
                                    list2.add(row[i])
                                }
                            }
                        }

                        // Call the processLine function with the two lists
                        processLine(stCount, list1, list2)
                    }
                }
                "QUESTION" -> {qnlist = row.subList(1, MAX_QUESTIONS+1)}
                "CORRECT_ANSWER" ->{qalist = row.subList(1, MAX_QUESTIONS+1)}
                "ROBOT_ANSWER" ->
                {
                    val list1 = row.subList(1, MAX_QUESTIONS+1)
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
                        Session.addQuestion(id, cAns, rAns)
                    }
                }
                "CONDITION1", "CONDITION2", "CONDITION3"  ->
                {
                    if (row.size >= MAX_QUESTIONS+1) {
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
                        Session.createCondition(EnumConditions.fromString(it.toString()),
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

fun processLine(c: Int, settings: List<Any>, texts: List<Any>) {
    val textsAsStringArray = texts.map { it.toString() }.toTypedArray()
    val id = settings[SOURCEDATA_ID].toString()
    val qIndex = questionIndexToInt(settings[SOURCEDATA_QUESTION].toString(),c)
    val type: EnumStatementTypes = EnumStatementTypes.fromString(settings[SOURCEDATA_TYPE].toString())
    val subType = subTypeToBool(type, settings[SOURCEDATA_SUBTYPE].toString())
    val stIndex = questionIndexToInt(settings[SOURCEDATA_STATEMENT_INDEX].toString(),c)
    val pertriplet = settings[SOURCEDATA_PERTRIPLET].toString() == SOURCEDATA_TRUE



    if(type == EnumStatementTypes.CLAIM && texts.size!=SOURCEDATA_CLAIM_SIZE){
        error("Error loading data. Claim in row $c must have exactly $SOURCEDATA_CLAIM_SIZE items.")
    }
    println("DATA LOADING: ----[$id]-----")
    println("Calling addStatement with values - qIndex: $qIndex, id: $id, type: $type, subType: $subType, stIndex: $stIndex, pertriplet: $pertriplet, texts: ${textsAsStringArray.joinToString(", ")}")

    Session.addStatement(qIndex,id, type, subType, stIndex, pertriplet, *textsAsStringArray)

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
        EnumStatementTypes.CHECKPOINT, EnumStatementTypes.ULTIMATUM -> subType==SOURCEDATA_FRIENDLY
        EnumStatementTypes.ASSERTION -> subType==SOURCEDATA_TRUE
        else -> false
    }
}


