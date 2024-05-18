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
    private var confirmationRequest1st: Statement?, // Set in data loading per statement
    private var confirmationRequest2nd: Statement?, // Set in data loading per statement
    private var unfriendlyClaims: MutableList<Claim> = mutableListOf(),
    private var currentClaim: Claim?,
    private var friendlyClaims: MutableList<Statement> = mutableListOf(),
    private var friendlyClaimsWereUsed: Boolean = false,

    private var lastRejoinder: EnumRejoinders?,
    private var currentState: State?,
    private var previousState: State?,
    private var lastUtterance: String,
) {
    private var userVerbalAnswer: EnumAnswer = EnumAnswer.UNDEFINED
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
        confirmationRequest1st = null,
        confirmationRequest2nd = null,
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
    fun confirm() {confirmed=true}
    fun getCheckpoint(forReview: Boolean? = FRIENDLY): String{
        // Returns the friendly or unfriendly checkpoint depending on the param or,
        // if not provided, whether the user already agreed verbally in a
        // previous checkpoint.
        val s: Statement? = if(forReview!!) friendlyCheckpoint else unfriendlyCheckpoint
        return s!!.getText(this)
    }
    fun get1stConfirmationRequest(): String{
        return confirmationRequest1st!!.getText(this)
    }
    fun get2ndConfirmationRequest(): String{
        return confirmationRequest2nd!!.getText(this)
    }
    fun getDisclosure(): String{
        return disclosure!!.getText(this)
    }
    fun getElaborationRequest(): String{
        return elaborationRequest!!.getText(this)
    }
    fun getFriendlyClaim():String{
        /* Friendly claims (fClaim) are lists of claims linked to each unfriendly claim (uClaim). Each fClaim is
         a combination of the next 2 uclaims. So, for example, given a list of 4 uClaims, if the first fClaim is
        requested, the list of fClaims belonging to the first uClaim should be made the current list of fClaims.
        Then the first fClaim of the list should be served.
        And then the first uClaim of the list of Uclaims should be removed. At this point, the list of uClaims
        should have 2 claims. The list of fClaims should have 1 claim.
        If for any reason now an uClaim is requested, the right uClaim will be served.
        */
        if(!friendlyClaimsWereUsed){
            friendlyClaimsWereUsed = true
            if (unfriendlyClaims.isNotEmpty()) {
                friendlyClaims = unfriendlyClaims[0].friendlyStatementList
            }else{/*this should not be happening*/
                println("Fatal error I would say.")
            }
            /* Explanation of why currentUnfriendlyClaim can be null. This is because every time an fClaim is requested
            two positions of uClaims are moved forward (i.e. two uClaims are removed for each fClaim removed). This means
            that when the second to
            */
/*
            setNextUnfriendlyClaimAsCurrent()
            friendlyClaims = if(currentUnfriendlyClaim==null) friendlyClaims else currentUnfriendlyClaim?.friendlyStatementList!!
*/
        }
        // After the above, the friendlyClaims are set.
        if(friendlyClaims.isNotEmpty()){
            //Removing two unfriendly claims per friendly claim delivered.
            unfriendlyClaims.removeFirst()
            unfriendlyClaims.removeFirst()
            return friendlyClaims.removeFirst().getText(this)
        }else{return ""}
        //return if (friendlyClaims.isNotEmpty() == true) friendlyClaims.removeFirst().getText(this) else ""

    }
    fun getFriendlyProbe():String{return getProbe(FRIENDLY)}

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
    fun getRobotMode(): EnumRobotMode{
        return robotMode!!
    }
    fun getUltimatum(forReview: Boolean = UNFRIENDLY): String{
        val s: Statement? = if(forReview) friendlyUltimatum else unfriendlyUltimatum
        return s!!.getText(this)
    }
    fun getUnfriendlyClaim(): String {
        friendlyClaimsWereUsed = false // This is in case friendly claims were used, and then
        //again, unfriendly claim is requested. In that case, the current list of friendly claims should be discarded.
        setNextUnfriendlyClaimAsCurrent()
        return currentUnfriendlyClaim?.getText(this) ?: ""
    }
    fun getUnfriendlyProbe():String{
        return getProbe(UNFRIENDLY)
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

    fun setRobotAnswer(enumAnswer: EnumAnswer){
        robotAnswer = enumAnswer
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
            EnumStatementTypes.CONFIRMATION_REQUEST ->  {
                if(st.isFriendly()) {
                    confirmationRequest1st = st
                }else{
                    confirmationRequest2nd = st
                }

            }
        }
    }


    fun thereAreUnfriendlyClaims():Boolean{
        return unfriendlyClaims.size>0
    }
    fun thereAreFriendlyClaims():Boolean{
        if(friendlyClaimsWereUsed){
            return friendlyClaims.size>0
        }else{
            return thereAreUnfriendlyClaims()
        }
    }
    fun userVerballyAgrees(){
        userVerbalAnswer = robotAnswer
    }
    fun userVerballyDisagrees(){
        userVerbalAnswer = robotAnswer.getOpposite()
    }
    fun isUserVerballyAgreeing(): Boolean = userVerbalAnswer.agreesWith(robotAnswer)
    fun isUserVerballyUndecided(): Boolean = userVerbalAnswer == EnumAnswer.UNDEFINED





    override fun toString(): String {
        return """
        |Question Details:
        |ID: $id
        |Correct Answer: ${correctAnswer ?: "Not Set"}
        |Robot Mode: ${robotMode ?: "Not Set"}
        |Robot Answer: ${robotAnswer}
        |User Verbally agrees: ${userVerbalAnswer}
        |Reflection: ${reflection ?: "Not Set"}
        |Marking request: ${markingRequest ?: "Not Set"}
        |Disclosure: ${disclosure ?: "Not Set"}
        |Disclosure Has Question: ${disclosureHasQuestion ?: "Not Set"}
        |Friendly Ultimatum: ${friendlyUltimatum ?: "Not Set"}
        |Unfriendly Ultimatum: ${unfriendlyUltimatum ?: "Not Set"}
        |Friendly Checkpoint: ${friendlyCheckpoint ?: "Not Set"}
        |Unfriendly Checkpoint: ${unfriendlyCheckpoint ?: "Not Set"}
        |First confirmation req: ${confirmationRequest1st ?: "Not Set"}
        |Second confirmation req: ${confirmationRequest2nd ?: "Not Set"}
        |Current Claim: ${currentUnfriendlyClaim ?: "Not Set"}
        |Last Rejoinder: ${lastRejoinder ?: "Not Set"}
        |Current State: ${currentState ?: "Not Set"}
        |Previous State: ${previousState ?: "Not Set"}
        |Last Utterance: ${lastUtterance}
        |Unfriendly Claims Count: ${unfriendlyClaims.size}
        |Friendly Claims Count: ${friendlyClaims.size}
        """.trimMargin()
    }


}
