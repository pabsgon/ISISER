package furhatos.app.isiser.questions

import furhatos.app.isiser.App
import furhatos.app.isiser.setting.*
import javax.swing.plaf.nimbus.State

data class Question(
    val id: String,  // immutable ID, no need for setter
    private var correctAnswer: EnumAnswer?, // Set in construction
    private var robotMode: EnumRobotMode?, // Set when user is set
    private var robotAnswer: EnumAnswer, // Set in construction

    private var confirmed: Boolean = false,
    // Statements/Utterances
    private var disclosureHasQuestion: Boolean?, //Unused at the moment

/*    private var reflection: Statement?, // Set in data loading per statement
    private var markingRequest: Statement?, // Set in data loading per statement
    private var disclosure: Statement?, // Set in data loading per statement
    private var probe: Statement?, // Set in data loading per statement
    private var friendlyUltimatum: Statement?,
    private var unfriendlyUltimatum: Statement?,
    private var friendlyCheckpoint: Statement?,
    private var unfriendlyCheckpoint: Statement?,
    private var elaborationRequest: Statement?,
    private var confirmationRequest1st: Statement?, // Set in data loading per statement
    private var confirmationRequest2nd: Statement?, // Set in data loading per statement
    private var currentClaim: Claim?,
    private var currentState: State?,
    private var previousState: State?,
    private var lastRejoinder: EnumRejoinders?,
    private var lastUtterance: String*/
    private var unfriendlyClaims: MutableList<Claim> = mutableListOf(),
    private var currentUnfriendlyClaim: Claim? = null,
    private var friendlyClaims: MutableList<Statement> = mutableListOf(),
    private var friendlyClaimsWereUsed: Boolean = false,
) {
    private var userVerbalAnswer: EnumAnswer = EnumAnswer.UNSET
/*
    private var unfriendlyProbesCount:Int = 0
    private var friendlyProbesCount:Int = 0
*/
    private var checkpointReached: Boolean = false
    private var statements: StatementMap = StatementMap(mutableMapOf(), this)
    // Secondary constructor
    constructor(id: String, cAns: EnumAnswer, rAns: EnumAnswer ) : this(
        id = id,
        correctAnswer = cAns,
        robotMode = null,
        robotAnswer = rAns,
        confirmed = false,
        disclosureHasQuestion = null,
        unfriendlyClaims = mutableListOf(),
        friendlyClaims = mutableListOf()
    /*
        reflection = null,
        markingRequest = null,
        disclosure = null,
        probe = null,
        friendlyUltimatum = null,
        unfriendlyUltimatum = null,
        friendlyCheckpoint = null,
        unfriendlyCheckpoint = null,
        elaborationRequest = null,
        confirmationRequest1st = null,
        confirmationRequest2nd = null,
        currentClaim = null,
        currentState = null,
        previousState = null,
        lastRejoinder = null,
        lastUtterance = "",
*/

    )
    fun addClaim(claim: Claim) {
        if(currentUnfriendlyClaim ==null)currentUnfriendlyClaim = claim //Just adding the first one.
        unfriendlyClaims.add(claim)
    }
    fun confirm() {confirmed=true}

    private fun getFriendlyClaimStatement():Statement?{
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
            return friendlyClaims.removeFirst()
        }else{return null}
        //return if (friendlyClaims.isNotEmpty() == true) friendlyClaims.removeFirst().getText(this) else ""

    }

    fun getMarkedAnswer():EnumAnswer = App.getGUI().getMarkedAnswer()

/*
    fun getProbe(friendly: EnumFriendliness):String{
        if(friendly==EnumFriendliness.FRIENDLY) friendlyProbesCount++ else unfriendlyProbesCount++
        return getStatement(EnumWordingTypes.PROBE,friendly)
        //return probe!!.getText(this)
    }*/
    fun getRobotAnswer(): EnumAnswer{
        return robotAnswer
    }
    fun getRobotMode(): EnumRobotMode{
        return robotMode!!
    }
    fun getRobotModeForStatement(wordingId: EnumWordingTypes, friendliness: EnumFriendliness? = EnumFriendliness.ANY): EnumRobotMode{
        var statementIsTriMode =
        if(wordingId == EnumWordingTypes.CLAIM){
            true
        }else {
            statements.isTrimode(wordingId,friendliness)
        }

        return if(statementIsTriMode) robotMode!! else EnumRobotMode.NEUTRAL
    }
    fun getExtendedUtterance(wordingId: EnumWordingTypes, friendliness: EnumFriendliness? = EnumFriendliness.ANY, aside:String):ExtendedUtterance{
        val st: Statement? = getStatement(wordingId, friendliness)

        return ExtendedUtterance(
            st!!.getText(this),
            aside,
            getRobotModeForStatement(wordingId, friendliness),
            st.isAssentSensitive
        )
    }

    fun getStatement(wordingId: EnumWordingTypes, friendliness: EnumFriendliness? = EnumFriendliness.ANY): Statement?{

        return if(wordingId == EnumWordingTypes.CLAIM){
            getClaimStatement(friendliness)
        }else {
            statements.getSpecificStatement(wordingId, friendliness)
        }

    }

    fun getStatementText(wordingId: EnumWordingTypes, friendliness: EnumFriendliness? = EnumFriendliness.ANY): String{
        return if(wordingId == EnumWordingTypes.CLAIM){
            getClaimText(friendliness!!)
        }else {
            statements.getText(wordingId, friendliness)
        }
    }
    fun getClaimStatement(friendliness: EnumFriendliness?): Statement? {
        return if(friendliness == EnumFriendliness.UNFRIENDLY) getUnfriendlyClaimStatement() else getFriendlyClaimStatement()
    }

    fun getClaimText(friendliness: EnumFriendliness): String{
        return if(friendliness == EnumFriendliness.UNFRIENDLY) getUnfriendlyClaimStatement()?.getText(this)?:"" else getFriendlyClaimStatement()?.getText(this) ?: ""
    }
    private fun getUnfriendlyClaimStatement(): Statement? {
        friendlyClaimsWereUsed = false // This is in case friendly claims were used, and then
        //again, unfriendly claim is requested. In that case, the current list of friendly claims should be discarded.
        setNextUnfriendlyClaimAsCurrent()
        return currentUnfriendlyClaim?.getStatement()
    }
    fun maxNumOfFriendlyProbesReached():Boolean {
        return statements.timesUsed(EnumWordingTypes.PROBE,EnumFriendliness.FRIENDLY)>= MAX_NUM_PROBES_AT_REVIEW
        //friendlyProbesCount>= MAX_NUM_PROBES_AT_REVIEW
    }
    fun maxNumOfUnfriendlyProbesReached():Boolean {
        return statements.timesUsed(EnumWordingTypes.PROBE,EnumFriendliness.FRIENDLY)>= MAX_NUM_PROBES_AT_PERSUASION
        //unfriendlyProbesCount>= MAX_NUM_PROBES_AT_PERSUASION
    }

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

        statements.add(st)
/*
        when (st.type) {
            EnumWordingTypes.REFLECTION -> {
                reflection = st
            }
            EnumWordingTypes.MARKING_REQUEST -> {
                markingRequest = st
            }
            EnumWordingTypes.CLAIM -> {//Performed via add Claim
            }
            EnumWordingTypes.ASSERTION -> {
                unfriendlyClaims[index].setAssertion(st)
            }
            EnumWordingTypes.PROBE -> {
                probe = st
            }
            EnumWordingTypes.ASIDE -> {//Performed via add Aside
            }
            EnumWordingTypes.CHECKPOINT -> {
                if(st.isFriendly()) {
                    friendlyCheckpoint = st
                }else{
                    unfriendlyCheckpoint = st
                }
            }
            EnumWordingTypes.ELABORATION_REQUEST -> {
                elaborationRequest = st
            }
            EnumWordingTypes.DISCLOSURE -> {
                disclosure = st
            }
            EnumWordingTypes.ULTIMATUM -> {
                if(st.isFriendly()) {
                    friendlyUltimatum = st
                }else{
                    unfriendlyUltimatum = st
                }
            }
            EnumWordingTypes.CONFIRMATION_REQUEST ->  {
                if(st.isFriendly()) {
                    confirmationRequest1st = st
                }else{
                    confirmationRequest2nd = st
                }

            }
            EnumWordingTypes.REPEAT -> {*//*Impossible, since they are not statements. Handled programmatically.*//*}
            EnumWordingTypes.WELCOME,
            EnumWordingTypes.INSTRUCTIONS_INTRO ,
            EnumWordingTypes.INSTRUCTIONS_CHECKPOINT ,
            EnumWordingTypes.INSTRUCTIONS_GENERAL ,
            EnumWordingTypes.INSTRUCTIONS_DETAILED ,
            EnumWordingTypes.PRESS_READY_REQUEST,
            EnumWordingTypes.FAREWELL,
                    EnumWordingTypes.ANY -> {*//*Impossible, since they are not statements. Handled by wording.*//*}


        }*/
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
    fun setUserVerballyAgrees(){
        userVerbalAnswer = robotAnswer
    }
    fun setUserVerballyDisagrees(){
        userVerbalAnswer = robotAnswer.getOpposite()
    }
    fun isUserVerballyAgreeing(): Boolean = userVerbalAnswer.agreesWith(robotAnswer)
    fun isUserVerballyUndecided(): Boolean = userVerbalAnswer == EnumAnswer.UNSET
    fun isCheckpointReached(): Boolean{ return checkpointReached}
    fun checkpointReached(){checkpointReached = true}


    /*

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
    */


}
