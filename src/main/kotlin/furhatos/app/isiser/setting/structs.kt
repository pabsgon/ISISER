package furhatos.app.isiser.setting

import furhatos.app.isiser.flow.main.QuestionPersuasion
import furhatos.app.isiser.flow.main.QuestionReview
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
    val isIndexical: Boolean
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
        return triplet.getText(question.robotMode)
    }
}
class Aside(
    id: String,
    type: EnumStatementTypes, // Inherited property, also ensures that Aside must be one of the specified types
    texts: MutableList<TextTriplet>,  // Inherited property
    isIndexical: Boolean, // Inherited property
    val state: State? = null, // Additional property for Asides, null means it works for any state
    val statePhase: EnumStatePhase, // Additional property indicating when the aside is applicable (ENTRY, BODY, ANY)
    val forRejoinder: EnumSubjectRejoinders? = null // Additional property, null means it works for any rejoinder situation
) : Statement(id, type, texts, isIndexical) {
    // All properties and methods from Statement are inherited and available here

    // Optionally, you can add additional methods or override existing ones here
}

class Statements : ArrayList<Statement>() {
    fun addStatement(id: String, type: EnumStatementTypes, isIndexical: Boolean, vararg texts: String) {
        val textTriplets = mutableListOf<TextTriplet>()

        // Process every three items as a triplet
        for (i in texts.indices step 3) {
            val neutral = texts.getOrNull(i) ?: break // If there's no neutral text, skip creating a triplet
            val uncertain = texts.getOrNull(i + 1) ?: neutral // Use neutral if uncertain text is missing
            val certain = texts.getOrNull(i + 2) ?: uncertain // Use uncertain (or neutral) if certain text is missing

            textTriplets.add(TextTriplet(neutral, uncertain, certain))
        }

        this.add(Statement(id, type, textTriplets, isIndexical))
    }
    fun addStatementNeutral(id: String, type: EnumStatementTypes, isIndexical: Boolean, vararg texts: String) {
        val textTriplets = texts.map { TextTriplet(it) }.toMutableList()
        this.add(Statement(id,  type, textTriplets, isIndexical))
    }
}

class Claim(
    val id: Int,
    val persuasionStatement: Statement,
    val reviewStatement: Statement,
    val assertions: MutableList<Statement>,
    var pendingAssertions: Int = ASSERTIONS_PER_CLAIM
) {
    fun getText(question: Question): String {
        // Will return the text of the claim depending on the state the question is in
        // If the interaction is in QuestionReview, the reviewStatement will be provided.
        // By default, persuasionStatement will be returned.
        if(question.currentState?.name == QuestionReview.name )
            return reviewStatement.getText(question)
        else
            return persuasionStatement.getText(question)
    }

    fun getAssertion(question: Question): String? {
        if (pendingAssertions == 0) return null

        // Get the first assertion and move it to the end of the list
        val assertion = assertions.removeAt(0)
        assertions.add(assertion)

        // Decrement the number of pending assertions
        pendingAssertions--

        return assertion.getText(question)
    }
}
