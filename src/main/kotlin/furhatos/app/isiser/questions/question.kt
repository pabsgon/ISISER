package furhatos.app.isiser.questions

import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.State

data class Question(
    val id: String,  // immutable ID, no need for setter
    private var _correctAnswer: EnumAnswer?, // Set in construction
    private var _robotMode: EnumRobotMode?, // Set when user is set
    private var _robotAnswer: EnumAnswer?, // Set in construction
    private var _userVerbalAnswer: EnumAnswer?, // Set in construction to Undefined
    private var _userMarkedAnswer: EnumAnswer?, // Set in construction to Undefined
    private var _confirmed: Boolean = false,

    // Statements/Utterances
    private var _reflection: Statement?, // Set in data loading per statement
    private var _markingRequest: Statement?, // Set in data loading per statement

    private var _disclosure: Statement?, // Set in data loading per statement
        private var _disclosureHasQuestion: Boolean?, //Unused at the moment
    private var _probe: Statement?, // Set in data loading per statement
    private var _friendlyUltimatum: Statement?,
    private var _unfriendlyUltimatum: Statement?,
    private var _friendlyCheckpoint: Statement?,
    private var _unfriendlyCheckpoint: Statement?,
    private var _elaborationRequest: Statement?,
    private var claims: MutableList<Claim> = mutableListOf(),
    private var pastClaims: MutableList<Claim> = mutableListOf(),
    private var _currentClaim: Claim?,

    private var _lastRejoinder: EnumSubjectRejoinders?,
    private var _currentState: State?,
    private var _previousState: State?,
    private var _lastUtterance: String?
) {
    // Secondary constructor
    constructor(id: String, cAns: EnumAnswer, rAns: EnumAnswer ) : this(
        id = id,
        _correctAnswer = cAns,
        _robotMode = null,
        _robotAnswer = rAns,
        _userVerbalAnswer = EnumAnswer.UNDEFINED,
        _userMarkedAnswer = EnumAnswer.UNDEFINED,
        _confirmed = false,
        _reflection = null,
        _markingRequest = null,
        _disclosure = null,
        _disclosureHasQuestion = null,
        _probe = null,
        _friendlyUltimatum = null,
        _unfriendlyUltimatum = null,
        _friendlyCheckpoint = null,
        _unfriendlyCheckpoint = null,
        _elaborationRequest = null,
        claims = mutableListOf(),
        pastClaims = mutableListOf(),
        _currentClaim = null,
        _lastRejoinder = null,
        _currentState = null,
        _previousState = null,
        _lastUtterance = null
    )
    fun reset(){

    }

    var robotMode: EnumRobotMode?
        get() = _robotMode
        set(value) {
            _robotMode = value
        }

    var correctAnswer: EnumAnswer?
        get() = _correctAnswer
        set(value) {
            _correctAnswer = value
        }
    var robotAnswer: EnumAnswer?
        get() = _robotAnswer
        set(value) {
            _robotAnswer = value
        }

    var userVerbalAnswer: EnumAnswer?
        get() = _userVerbalAnswer
        set(value) {
            _userVerbalAnswer = value
        }
    var userMarkedAnswer: EnumAnswer?
        get() = _userMarkedAnswer
        set(value) {
            _userMarkedAnswer = value
        }

    var isConfirmed: Boolean
        get() = _confirmed
        set(value) {
            _confirmed = value
        }

    var markingRequest: Statement?
        get() = _markingRequest
        set(value) {
            _markingRequest = value
        }
    var probe: Statement?
        get() = _probe
        set(value) {
            _probe = value
        }
    var reflection: Statement?
        get() = _reflection
        set(value) {
            _reflection = value
        }
    var disclosure: Statement?
        get() = _disclosure
        set(value) {
            _disclosure = value
        }
    var elaborationRequest: Statement?
        get() = _elaborationRequest
        set(value) {
            _elaborationRequest = value
        }

    var disclosureHasQuestion: Boolean?
        get() = _disclosureHasQuestion
        set(value) {
            _disclosureHasQuestion = value
        }

    var friendlyUltimatum: Statement?
        get() = _friendlyUltimatum
        set(value) {
            _friendlyUltimatum = value
        }

    var unfriendlyUltimatum: Statement?
        get() = _unfriendlyUltimatum
        set(value) {
            _unfriendlyUltimatum = value
        }

    var friendlyCheckpoint: Statement?
        get() = _friendlyCheckpoint
        set(value) {
            _friendlyCheckpoint = value
        }

    var unfriendlyCheckpoint: Statement?
        get() = _unfriendlyCheckpoint
        set(value) {
            _unfriendlyCheckpoint = value
        }
    var currentClaim: Claim?
        get() = _currentClaim
        set(value) {
            _currentClaim = value
        }

    var lastRejoinder: EnumSubjectRejoinders?
        get() = _lastRejoinder
        set(value) {
            _lastRejoinder = value
        }

    var currentState: State?
        get() = _currentState
        set(value) {
            _currentState = value
        }

    var previousState: State?
        get() = _previousState
        set(value) {
            _previousState = value
        }

    var lastUtterance: String?
        get() = _lastUtterance
        set(value) {
            _lastUtterance = value
        }
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
                claims[index].setAssertion(st)
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
    fun addClaim(claim: Claim) {
        claims.add(claim)
    }

    fun inOfficialAgreement(): Boolean = robotAnswer != EnumAnswer.UNDEFINED && userMarkedAnswer == robotAnswer
    fun inUnofficialAgreement(): Boolean = robotAnswer != EnumAnswer.UNDEFINED && userVerbalAnswer == robotAnswer
    fun inAgreement(): Boolean{
        /*
        * The robot will always have an answer defined, except at the onset of the program, during dataloading.
        *
        * The priority to check if they are in agreement is if the verbal answer. If this has been determined and
        * set, then that becomes the reference over the marked answer.
        *
        * */

        if(robotAnswer == EnumAnswer.UNDEFINED ){
            return false
        }else{
            if(userVerbalAnswer == EnumAnswer.UNDEFINED ){
                if(userMarkedAnswer == EnumAnswer.UNDEFINED ){
                    return false
                }else{
                   return userMarkedAnswer == robotAnswer
                }
            }else{
                return userVerbalAnswer == robotAnswer
            }
        }
    }

    fun getClaim(forReview: Boolean?): String {
        return currentClaim?.getText(this) ?: ""
    }

    fun getNextClaim(): String {
        val localClaims = claims  // Create a local copy of the claims
        currentClaim?.let {
            pastClaims.add(it)
            if (localClaims?.isNotEmpty() == true) {
                currentClaim = localClaims.removeFirst()
            }
        }
        return currentClaim?.getText(this) ?: ""
    }

    fun getCheckpoint(rechecking: Boolean): String {
        return if (!rechecking) {
            val checkpoint = if (inUnofficialAgreement()) friendlyCheckpoint else unfriendlyCheckpoint
            checkpoint?.getText(this) ?: ""
        } else {
            // Rechecking logic
            ""
        }
    }

    fun repeat(): String = lastUtterance ?: ""

    fun getClarification(): String = "What do you mean?"

    override fun toString(): String {
        return """
        |Question Details:
        |ID: $id
        |Correct Answer: ${correctAnswer ?: "Not Set"}
        |Robot Mode: ${robotMode ?: "Not Set"}
        |Robot Answer: ${robotAnswer ?: "Not Set"}
        |User Marked Answer: ${userMarkedAnswer ?: "Not Set"}
        |User Verbal Answer: ${userVerbalAnswer ?: "Not Set"}
        |Reflection: ${reflection ?: "Not Set"}
        |Reflection: ${markingRequest ?: "Not Set"}
        |Disclosure: ${disclosure ?: "Not Set"}
        |Disclosure Has Question: ${disclosureHasQuestion ?: "Not Set"}
        |Friendly Ultimatum: ${friendlyUltimatum ?: "Not Set"}
        |Unfriendly Ultimatum: ${unfriendlyUltimatum ?: "Not Set"}
        |Friendly Checkpoint: ${friendlyCheckpoint ?: "Not Set"}
        |Unfriendly Checkpoint: ${unfriendlyCheckpoint ?: "Not Set"}
        |Current Claim: ${currentClaim ?: "Not Set"}
        |Last Rejoinder: ${lastRejoinder ?: "Not Set"}
        |Current State: ${currentState ?: "Not Set"}
        |Previous State: ${previousState ?: "Not Set"}
        |Last Utterance: ${lastUtterance ?: "Not Set"}
        |Claims Count: ${claims.size ?: "No claims"}
        |Past Claims Count: ${pastClaims.size}
        """.trimMargin()
    }


}
