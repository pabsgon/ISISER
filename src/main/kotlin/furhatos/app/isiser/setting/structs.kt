package furhatos.app.isiser.setting

import furhatos.app.isiser.questions.Question
import furhatos.flow.kotlin.Utterance
import furhatos.flow.kotlin.utterance
data class Wordings(
    val map: MutableMap<EnumWordingTypes, MutableList<String>> = mutableMapOf()
) {
    // Function to add a new item to the map
    fun add(s1: String, l2: List<Any>) {
        // Convert the string to EnumWordingTypes
        val state = EnumWordingTypes.valueOf(s1)

        // Convert l2 to a list of strings
        val strings = l2.map { it.toString() }.toMutableList()

        // Assign the list of strings to the label
        map[state] = strings
    }

    // Function to get and rotate the first element in the list for the specified state
    fun get(e: EnumWordingTypes): String {
        val list = map[e] ?: throw IllegalArgumentException("State $e not found in the map")
        if (list.isEmpty()) {
            throw NoSuchElementException("The list for state $e is empty")
        }
        val t1 = list.removeAt(0)
        list.add(t1)
        return t1
    }

    // Print function
    fun print() {
        for ((state, list) in map) {
            println("State: $state")
            println("  List: $list")
        }
    }
}
data class Asides(
    val map: MutableMap<
            Triple<EnumWordingTypes, EnumStatePhase, EnumRejoinders>,
            MutableMap<EnumFriendliness, MutableList<String>>
            > = mutableMapOf()
) {
    // Function to add a new item to the map
    fun add(
        type: Any, // To align with EnumWordingTypes (WELCOME, INSTRUCTIONS,  MARKING_REQUEST, CLAIM, etc.
        phase: Any, // To align with ENTRY, BODY, ANY;
        rejoinder: Any,// To align with  I_AM_DONE, ANSWER_MARKED, ANSWER_TRUE,PROBE, DENIAL, etc.
        friendliness: Any,// To align with FRIENDLY, UNFRIENDLY, ANY.
        list: List<Any>
    ) {
        // Convert parameters to strings
        val typeString = type.toString()
        val phaseString = phase.toString()
        val rejoinderString = rejoinder.toString()
        val friendlinessString = friendliness.toString()

        // Convert strings to enums, throwing an error if the conversion fails
        val enumType = EnumWordingTypes.values().find { it.name == typeString }
            ?: error("Error at Data loading. Value \"$typeString\" cannot be converted as of type \"EnumWordingTypes\"")
        val enumPhase = EnumStatePhase.values().find { it.name == phaseString }
            ?: error("Error at Data loading. Value \"$phaseString\" cannot be converted as of type \"EnumStatePhase\"")
        val enumRejoinder = EnumRejoinders.values().find { it.name == rejoinderString }
            ?: error("Error at Data loading. Value \"$rejoinderString\" cannot be converted as of type \"EnumRejoinders\"")
        val enumFriendliness = EnumFriendliness.values().find { it.name == friendlinessString }
            ?: EnumFriendliness.ANY // Default to ANY if not provided or conversion fails

        val key = Triple(enumType, enumPhase, enumRejoinder)
        val strings = list.map { it.toString() }.toMutableList()
        val innerMap = map.getOrPut(key) { mutableMapOf() }
        innerMap[enumFriendliness] = strings
    }

    // Function to get and rotate the first element in the list
    fun get(
        type: EnumWordingTypes? = EnumWordingTypes.ANY,
        rejoinder: EnumRejoinders? = EnumRejoinders.ANY,
        phase: EnumStatePhase? = EnumStatePhase.BODY,
        friendliness: EnumFriendliness? = EnumFriendliness.ANY
    ): String {
        val actualType = type ?: EnumWordingTypes.ANY
        val actualPhase = phase ?: EnumStatePhase.BODY
        val actualRejoinder = rejoinder ?: EnumRejoinders.ANY
        val actualFriendliness = friendliness ?: EnumFriendliness.ANY

        val keysToTry = listOf(
            Triple(actualType, actualPhase, actualRejoinder),
            Triple(actualType, EnumStatePhase.ANY, actualRejoinder),
            Triple(EnumWordingTypes.ANY, actualPhase, actualRejoinder),
            Triple(EnumWordingTypes.ANY, EnumStatePhase.ANY, actualRejoinder)
        )

        for (key in keysToTry) {
            val innerMap = map[key]
            if (innerMap != null) {
                val listsToTry = listOf(
                    innerMap[actualFriendliness],
                    innerMap[EnumFriendliness.ANY]
                )

                for (list in listsToTry) {
                    if (list != null && list.isNotEmpty()) {
                        val t1 = list.removeAt(0)
                        list.add(t1)
                        return t1
                    }
                }
            }
        }

        println("Warning: ASIDE for type=$actualType, phase=$actualPhase, rejoinder=$actualRejoinder, friendliness=$actualFriendliness")
        return ""
    }

    // Print function
    fun print() {
        for ((key, innerMap) in map) {
            println("Type: ${key.first}, Phase: ${key.second}, Rejoinder: ${key.third}")
            for ((friendliness, list) in innerMap) {
                println("  Friendliness: $friendliness, List: $list")
            }
        }
    }
}

/*
data class Wordings(
    val map: MutableMap<EnumWordingTypes, MutableList<MutableList<String>>> = mutableMapOf()
) {
    // Function to add a new item to the map
    fun add(s1: String, l2: List<Any>) {
        // Convert the string to EnumStates
        val state = EnumWordingTypes.valueOf(s1)

        // Initialize the list of lists of strings if not already present
        val listOfLists = map.getOrPut(state) { mutableListOf() }

        // Convert l2 to a list of strings
        val strings = l2.map { it.toString() }.toMutableList()

        // Add the list of strings to the list of lists
        listOfLists.add(strings)
    }

    // Function to get and rotate the first element in the list at the specified index
    fun get(e: EnumWordingTypes, index: Int): String {
        val listOfLists = map[e] ?: throw IllegalArgumentException("State $e not found in the map")
        if (index < 0 || index >= listOfLists.size) {
            throw IndexOutOfBoundsException("Index $index is out of bounds for the list of lists")
        }
        val list = listOfLists[index]
        if (list.isEmpty()) {
            throw NoSuchElementException("The list at index $index is empty")
        }
        val t1 = list.removeAt(0)
        list.add(t1)
        return t1
    }
    // Print function
    fun print() {
        for ((state, listOfLists) in map) {
            println("State: $state")
            for ((index, list) in listOfLists.withIndex()) {
                println("  List $index: $list")
            }
        }
    }

}*/

data class ExtendedUtterance(
    var mainText: String,
    var aside: String = "",
    var robotMode: EnumRobotMode = EnumRobotMode.NEUTRAL,
    var rate: Double = 1.0
) {
    val utterance: Utterance = createUtterance(mainText, aside)

    init {
        this.rate = robotMode.speechRate
    }
    private fun createUtterance(text: String, aside: String): Utterance {
        val processedString = (if(aside.isNotEmpty()) "$aside " else "") + text

        return utterance {
            var currentText = StringBuilder()
            var i = 0
            while (i < processedString.length) {
                when {
                    processedString[i] == '.' -> {
                        var dotCount = 0
                        while (i < processedString.length && processedString[i] == '.') {
                            dotCount++
                            i++
                        }
                        if (dotCount > 1) {
                            if (currentText.isNotEmpty()) {
                                +currentText.toString().trim()
                                currentText = StringBuilder()
                            }
                            +delay(SILENT_MILLISECS_PER_DOT * dotCount)
                        }
                    }
                    else -> {
                        currentText.append(processedString[i])
                        i++
                    }
                }
            }
            if (currentText.isNotEmpty()) {
                +currentText.toString().trim()
            }
            if (aside.isNotEmpty()) {
                +delay(1000)
                +aside
            }
        }
    }
}

data class TextTriplet(
    private val triplet: Map<EnumRobotMode, String>
) {
    constructor(neutralText: String, uncertainText: String, certainText: String) :
            this(
                mapOf(
                    EnumRobotMode.NEUTRAL to neutralText,
                    EnumRobotMode.UNCERTAIN to uncertainText,
                    EnumRobotMode.CERTAIN to certainText
                )
            )

    constructor(neutralText: String) :
            this(mapOf(EnumRobotMode.NEUTRAL to neutralText))

    fun getText(robotMode: EnumRobotMode): String {
        return triplet[robotMode] ?: triplet[EnumRobotMode.NEUTRAL]!!
    }
}
/*
val s = Statement(id + FRIENDLY_SUFFIX + i, EnumStatementTypes.CLAIM, mutableListOf(textTriplets[i]), subType)
                tempStatements.add(s)
val s1 = Statement(id + UNFRIENDLY_SUFFIX, EnumStatementTypes.CLAIM, textTriplets.subList(0, 1).toMutableList(), subType)
            tempStatements.add(s1)
* */
open class Statement(
    val id: String,
    val type: EnumWordingTypes, // CLAIM, ASSERTION, PROBE, ASIDE, CHECKPOINT, CLARIFICATION_REQUEST, DISCLOSURE, ULTIMATUM
    private val texts: MutableList<TextTriplet>,  // List to easily manipulate elements
    val friendliness: EnumFriendliness = EnumFriendliness.ANY, //If assertion, it means Indexical.
    val indexical: Boolean = false
) {
    var usedInQuestion: Question? = null
    // New constructor
    constructor(
        id: String,
        type: EnumWordingTypes,
        texts: MutableList<TextTriplet>,
        subType: String
    ) : this(
        id = id,
        type = type,
        texts = texts,
        indexical = if (type == EnumWordingTypes.ASSERTION) subType == SOURCEDATA_TRUE else false,
        friendliness = if (type != EnumWordingTypes.ASSERTION) EnumFriendliness.fromString(subType) else EnumFriendliness.ANY
    )
    fun getText(question: Question): String {
        // Ensure there is at least one text triplet
        if (texts.isEmpty()) throw IllegalStateException("No text triplets available.")

        // Get the first text triplet and move it to the end of the list
        val triplet = texts.removeAt(0)
        texts.add(triplet)

        // Mark the statement as used in this question
        usedInQuestion = question

        // Return the text for the given question's robot mode
        var rawText =  triplet.getText(question.getRobotMode())


        return rawText.replace(SOURCEDATA_CODE_QNUM, question.id)
            .replace(SOURCEDATA_CODE_ROBOT_ANSWER, question.getRobotAnswer().toString())
            .replace(SOURCEDATA_CODE_USER_ANSWER, question.getMarkedAnswer().toString())

    }

    override fun toString(): String {
        val textDescriptions = texts.joinToString(separator = ", ") { it.toString() }
        return "ID: $id, Type: $type, Texts: [$textDescriptions]"
    }
    fun isIndexical(): Boolean{ return indexical}
    fun isFriendly(): Boolean{ return indexical}
}

class Claim(
    val id: String,
    val unfriendlyStatement: Statement,
    val friendlyStatementList: MutableList<Statement> = mutableListOf(),
    val assertions: MutableList<Statement> = mutableListOf(),
    var pendingAssertions: Int = ASSERTIONS_PER_CLAIM
) {
    constructor(id: String, statements: List<Statement>) : this(
        id = id,
        unfriendlyStatement = statements.first(),
        friendlyStatementList = statements.drop(1).toMutableList()
    )

    fun setAssertion(st: Statement){
        assertions.add(st)
    }

    fun getText(question: Question): String {
        return unfriendlyStatement.getText(question)
    }
    fun getfriendlyStatements(question: Question): MutableList<Statement> {
        return friendlyStatementList
    }

    fun getAssertion(question: Question): String? {
        if (pendingAssertions == 0) return null

        // Get the first assertion and move it to the end of the list
        val assertion = assertions!!.removeAt(0)
        assertions.add(assertion)

        // Decrement the number of pending assertions
        pendingAssertions--

        return assertion.getText(question)
    }
}
data class StatementMap(
    val map: MutableMap<Pair<EnumWordingTypes, EnumFriendliness>, Statement> = mutableMapOf(),
    val question: Question
) {
    // Function to add a new item to the map
    fun add(wordingType: EnumWordingTypes, friendliness: EnumFriendliness, statement: Statement) {
        val key = Pair(wordingType, friendliness)
        map[key] = statement
    }
    // Function to add a new item to the map
    fun add(statement: Statement) {
        add(statement.type, statement.friendliness, statement)
    }

    // Function to get a statement by wordingType and friendliness, defaults to ANY
    fun get(wordingType: EnumWordingTypes, friendliness: EnumFriendliness? = EnumFriendliness.ANY): String {
        // Try to get the specific friendliness first
        val specificKey = Pair(wordingType, friendliness ?: EnumFriendliness.ANY)
        val specificStatement = map[specificKey] ?: map[Pair(wordingType, EnumFriendliness.ANY)]

        if(specificStatement == null){
            println("WARNING: a statement is null (wordingType = $wordingType, friendliness=$friendliness). 'Mmh!' was returned")
            return "Mmh!"
        }else{
            return specificStatement.getText(question)
        }

    }

    // Print function to see all the statements
    fun print() {
        for ((key, statement) in map) {
            println("WordingType: ${key.first}, Friendliness: ${key.second}, Statement: $statement")
        }
    }
}
