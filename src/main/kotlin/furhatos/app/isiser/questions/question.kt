package furhatos.app.isiser.questions
import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.State
import sun.management.jmxremote.LocalRMIServerSocketFactory


data class Question(
    val id: Int,
    val robotMode: EnumRobotMode,
    val robotAnswer: EnumAnswer,
    var userVerbalAnswer: EnumAnswer,
    val disclosure: Statement,
    val disclosureHasQuestion: Boolean,
    val friendlyUltimatum: Statement,
    val unfriendlyUltimatum: Statement,
    val friendlyCheckpoint: Statement,
    val unfriendlyCheckpoint: Statement,
    val claims: MutableList<Claim>,
    val pastClaims: MutableList<Claim> = mutableListOf(),
    var currentClaim: Claim?,
    var lastRejoinder: EnumSubjectRejoinders?,
    var currentState: State?,
    var previousState: State?,
    var lastUtterance: String?
) {
    fun inAgreement(): Boolean = userVerbalAnswer != EnumAnswer.UNDEFINED && userVerbalAnswer == robotAnswer

    fun getClaim(forReview: Boolean?): String {
        return currentClaim?.getText(this) ?: ""
    }

    fun getNextClaim(): String {
        currentClaim?.let {
            pastClaims.add(it)
            if (claims.isNotEmpty()) {
                currentClaim = claims.removeFirst()
            }
        }
        return currentClaim?.getText(this) ?: ""
    }

    fun getCheckpoint(rechecking: Boolean): String {
        return if (!rechecking) {
            val checkpoint = if (inAgreement()) friendlyCheckpoint else unfriendlyCheckpoint
            checkpoint.getText(this)
        } else {
            // Rechecking logic
            ""
        }
    }

    fun repeat(): String = lastUtterance ?: ""

    fun getClarification(): String = "What do you mean?"

    // Additional functions based on interactions and internal state management
}

/*
val QuestionStage = state(Parent) {
    onResponse<NluLib.IAmDone> {
        random(
            {   furhat.say("Oh, give me a sec") },
            {   furhat.say("Mm...I see, hold on") }
        )
    }
}
fun Question1(greeting: String) = state(Parent) {
    onEntry {
        furhat.say(greeting)
    }
}
/*
val myState2 = state {
    onEntry {
        goto(myState("hello"))
    }
}*/