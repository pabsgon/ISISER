package furhatos.app.isiser.setting

import furhatos.app.isiser.questions.Question
import furhatos.flow.kotlin.State

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
        return triplet.getText(question.robotMode!!)
    }

    override fun toString(): String {
        val textDescriptions = texts.joinToString(separator = ", ") { it.toString() }
        return "ID: $id, Type: $type, Texts: [$textDescriptions]"
    }
    fun isIndexical(): Boolean{ return subType}
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
