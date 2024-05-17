package furhatos.app.isiser.questions

import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.State

data class Question(
    val id: String,  // immutable ID, no need for setter
    private var correctAnswer: EnumAnswer?, // Set in construction
    private var robotMode: EnumRobotMode?, // Set when user is set
    private var robotAnswer: EnumAnswer, // Set in construction

    private var confirmed: Boolean = false,

    // Statements/Utterances
    private var reflection: Statement?, // Set in data loading per statement
    private var markingRequest: Statement?, // Set in data loading per statement
    private var disclosure: Statement?, // Set in data loading per statement
    private var disclosureHasQuestion: Boolean?, //Unused at the moment
    private var probe: Statement?, // Set in data loading per statement
    private var friendlyUltimatum: Statement?,
    private var unfriendlyUltimatum: Statement?,
    private var friendlyCheckpoint: Statement?,
    private var unfriendlyCheckpoint: Statement?,
    private var elaborationRequest: Statement?,
    private var unfriendlyClaims: MutableList<Claim> = mutableListOf(),
    private var currentClaim: Claim?,
    private var friendlyClaims: MutableList<Statement> = mutableListOf(),
    private var friendlyClaimsWereUsed: Boolean = false,

    private var lastRejoinder: EnumRejoinders?,
    private var currentState: State?,
    private var previousState: State?,
    private var lastUtterance: String,
) {
    private var userVerballyAgrees: Boolean = false
    private var currentUnfriendlyClaim: Claim? = null
    private var unfriendlyProbesCount:Int = 0
    private var friendlyProbesCount:Int = 0

    // Secondary constructor
    constructor(id: String, cAns: EnumAnswer, rAns: EnumAnswer ) : this(
        id = id,
        correctAnswer = cAns,
        robotMode = null,
        robotAnswer = rAns,
        confirmed = false,
        reflection = null,
        markingRequest = null,
        disclosure = null,
        disclosureHasQuestion = null,
        probe = null,
        friendlyUltimatum = null,
        unfriendlyUltimatum = null,
        friendlyCheckpoint = null,
        unfriendlyCheckpoint = null,
        elaborationRequest = null,
        unfriendlyClaims = mutableListOf(),
        friendlyClaims = mutableListOf(),
        currentClaim = null,
        lastRejoinder = null,
        currentState = null,
        previousState = null,
        lastUtterance = ""
    )
    fun addClaim(claim: Claim) {
        if(currentUnfriendlyClaim ==null)currentUnfriendlyClaim = claim //Just adding the first one.
        unfriendlyClaims.add(claim)
    }

    fun getCheckpoint(forReview: Boolean = FOR_PERSUASION): String{
        val s: Statement? = if(forReview) friendlyCheckpoint else unfriendlyCheckpoint
        return s!!.getText(this)
    }
    fun getDisclosure(): String{
        return disclosure!!.getText(this)
    }
    fun getElaborationRequest(): String{
        return elaborationRequest!!.getText(this)
    }
    fun getFriendlyClaim():String{
        if(!friendlyClaimsWereUsed){
            setNextUnfriendlyClaimAsCurrent()
            friendlyClaims = if(currentUnfriendlyClaim==null) friendlyClaims else currentUnfriendlyClaim?.friendlyStatementList!!
        }
        return if (friendlyClaims.isNotEmpty() == true) friendlyClaims.removeFirst().getText(this) else ""

    }
    fun getFriendlyProbe():String{return getProbe(FOR_REVIEW)}

    fun getReflection(): String{
        return reflection!!.getText(this)
    }
    fun getMarkingRequest(): String{
        return markingRequest!!.getText(this)
    }

    fun getProbe(forReview: Boolean):String{
        if(forReview)friendlyProbesCount++ else unfriendlyProbesCount++
        return probe!!.getText(this)
    }
    fun getRobotAnswer(): EnumAnswer{
        return robotAnswer
    }

    fun getUltimatum(forReview: Boolean = FOR_PERSUASION): String{
        val s: Statement? = if(forReview) friendlyUltimatum else unfriendlyUltimatum
        return s!!.getText(this)
    }
    fun getUnfriendlyClaim(): String {
        setNextUnfriendlyClaimAsCurrent()
        return currentUnfriendlyClaim?.getText(this) ?: ""
    }
    fun getUnfriendlyProbe():String{
        return getProbe(FOR_PERSUASION)
    }

    fun maxNumOfFriendlyProbesReached():Boolean = friendlyProbesCount>= MAX_NUM_PROBES_AT_REVIEW

    fun maxNumOfUnfriendlyProbesReached():Boolean = unfriendlyProbesCount>= MAX_NUM_PROBES_AT_PERSUASION

    fun repeat(): String = lastUtterance ?: ""
    private fun setNextUnfriendlyClaimAsCurrent() {
        currentUnfriendlyClaim?.let {
            if (unfriendlyClaims.isNotEmpty() == true) {
                currentUnfriendlyClaim = unfriendlyClaims.removeFirst()
            }
        }
    }
    fun setRobotMode(mode: EnumRobotMode){robotMode=mode}
    fun setStatement(st: Statement, index: Int = 0){
        when (st.type) {
            EnumStatementTypes.REFLECTION -> {
                reflection = st
            }
            EnumStatementTypes.MARKING_REQUEST -> {
                markingRequest = st
            }
            EnumStatementTypes.CLAIM -> {//Performed via add Claim
            }
            EnumStatementTypes.ASSERTION -> {
                unfriendlyClaims[index].setAssertion(st)
            }
            EnumStatementTypes.PROBE -> {
                probe = st
            }
            EnumStatementTypes.ASIDE -> {
                //TODO()
            }
            EnumStatementTypes.CHECKPOINT -> {
                if(st.isFriendly()) {
                    friendlyCheckpoint = st
                }else{
                    unfriendlyCheckpoint = st
                }
            }
            EnumStatementTypes.ELABORATION_REQUEST -> {
                elaborationRequest = st
            }
            EnumStatementTypes.DISCLOSURE -> {
                disclosure = st
            }
            EnumStatementTypes.ULTIMATUM -> {
                if(st.isFriendly()) {
                    friendlyUltimatum = st
                }else{
                    unfriendlyUltimatum = st
                }
            }

        }
    }


    fun thereAreUnfriendlyClaims():Boolean{
        return unfriendlyClaims.size>0
    }
    fun thereAreFriendlyClaims():Boolean{
        return thereAreUnfriendlyClaims() || (friendlyClaimsWereUsed && friendlyClaims.size>0)
    }
    fun userVerballyAgrees(){
        userVerballyAgrees = true
    }
    fun userVerballyDisagrees(){
        userVerballyAgrees = false
    }
    fun isUserVerballyAgreeing(): Boolean = userVerballyAgrees





    override fun toString(): String {
        return """
        |Question Details:
        |ID: $id
        |Correct Answer: ${correctAnswer ?: "Not Set"}
        |Robot Mode: ${robotMode ?: "Not Set"}
        |Robot Answer: ${robotAnswer ?: "Not Set"}
        |User Verbally agrees: ${userVerballyAgrees ?: "Not Set"}
        |Reflection: ${reflection ?: "Not Set"}
        |Reflection: ${markingRequest ?: "Not Set"}
        |Disclosure: ${disclosure ?: "Not Set"}
        |Disclosure Has Question: ${disclosureHasQuestion ?: "Not Set"}
        |Friendly Ultimatum: ${friendlyUltimatum ?: "Not Set"}
        |Unfriendly Ultimatum: ${unfriendlyUltimatum ?: "Not Set"}
        |Friendly Checkpoint: ${friendlyCheckpoint ?: "Not Set"}
        |Unfriendly Checkpoint: ${unfriendlyCheckpoint ?: "Not Set"}
        |Current Claim: ${currentUnfriendlyClaim ?: "Not Set"}
        |Last Rejoinder: ${lastRejoinder ?: "Not Set"}
        |Current State: ${currentState ?: "Not Set"}
        |Previous State: ${previousState ?: "Not Set"}
        |Last Utterance: ${lastUtterance ?: "Not Set"}
        |Claims Count: ${unfriendlyClaims.size ?: "No claims"}
        |Past Claims Count: ${friendlyClaims.size}
        """.trimMargin()
    }


}
