package furhatos.app.isiser.setting

import furhatos.app.isiser.questions.Question
import furhatos.flow.kotlin.State

data class Wordings(
    val map: MutableMap<EnumStates, MutableList<MutableList<String>>> = mutableMapOf()
) {
    // Function to add a new item to the map
    fun add(s1: String, l2: List<Any>) {
        // Convert the string to EnumStates
        val state = EnumStates.valueOf(s1)

        // Initialize the list of lists of strings if not already present
        val listOfLists = map.getOrPut(state) { mutableListOf() }

        // Convert l2 to a list of strings
        val strings = l2.map { it.toString() }.toMutableList()

        // Add the list of strings to the list of lists
        listOfLists.add(strings)
    }

    // Function to get and rotate the first element in the list at the specified index
    fun get(e: EnumStates, index: Int): String {
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
open class Statement(
    val id: String,
    val type: EnumStatementTypes, // CLAIM, ASSERTION, PROBE, ASIDE, CHECKPOINT, CLARIFICATION_REQUEST, DISCLOSURE, ULTIMATUM
    val texts: MutableList<TextTriplet>,  // List to easily manipulate elements
    val subType: Boolean //If assertion, it means Indexical. If checkpoint or ultimatum, it means friendly.
) {
    var usedInQuestion: Question? = null

    fun getText(question: Question): String {
        // Ensure there is at least one text triplet
        if (texts.isEmpty()) throw IllegalStateException("No text triplets available.")

        // Get the first text triplet and move it to the end of the list
        val triplet = texts.removeAt(0)
        texts.add(triplet)

        // Mark the statement as used in this question
        usedInQuestion = question

        // Return the text for the given question's robot mode
        return triplet.getText(question.getRobotMode())
    }

    override fun toString(): String {
        val textDescriptions = texts.joinToString(separator = ", ") { it.toString() }
        return "ID: $id, Type: $type, Texts: [$textDescriptions]"
    }
    fun isSubtypeTRUE(): Boolean{ return subType}
    fun isFriendly(): Boolean{ return subType}
}
class Aside(
    id: String,
    type: EnumStatementTypes, // Inherited property, also ensures that Aside must be one of the specified types
    texts: MutableList<TextTriplet>,  // Inherited property
    isIndexical: Boolean, // Inherited property
    val state: State? = null, // Additional property for Asides, null means it works for any state
    val statePhase: EnumStatePhase, // Additional property indicating when the aside is applicable (ENTRY, BODY, ANY)
    val forRejoinder: EnumRejoinders? = null // Additional property, null means it works for any rejoinder situation
) : Statement(id, type, texts, isIndexical) {
    // All properties and methods from Statement are inherited and available here

    // Optionally, you can add additional methods or override existing ones here
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
