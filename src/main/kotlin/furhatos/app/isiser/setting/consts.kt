package furhatos.app.isiser.setting
const val GUI = "GUI"
const val UNDEFINED = "UNDEFINED"
const val NO_MESSAGE = "NO MESSAGE"
const val SAFE_STRING = "THIS IS A WARNING"
const val UNFRIENDLY = false
const val FRIENDLY = true
const val UNFRIENDLY_SUFFIX = "_U"
const val FRIENDLY_SUFFIX = "_F"
const val ASSERTIONS_PER_CLAIM = 2
const val MAX_QUESTIONS=8
const val SOURCEDATA_SETTINGS_SIZE = 9 // the first N elements of each row are settings, the rest texts.
const val SOURCEDATA_TRUE = "TRUE"
const val SOURCEDATA_FALSE = "FALSE"
const val SOURCEDATA_FRIENDLY = "FRIENDLY"
const val SOURCEDATA_QUESTION = 0
const val SOURCEDATA_TYPE = 4
const val SOURCEDATA_STATEMENT_INDEX = 5
const val SOURCEDATA_PERTRIPLET = 6
const val SOURCEDATA_SUBTYPE = 7
const val SOURCEDATA_ID = 8


enum class EnumQuestionPhase {
    REFLECTION, DISCLOSURE, PERSUASION, REVIEW, CHECKPOINT, ULTIMATUM, CONFIRMATION
}
enum class EnumConditions {
    UNDEFINED, CONDITION1, CONDITION2, CONDITION3;
    companion object {
        fun fromString(ans: String): EnumConditions {
            return try {
                enumValueOf<EnumConditions>(ans.toUpperCase())
            } catch (e: IllegalArgumentException) {
                error("Error loading data: CONDITION value '$ans' unknown")
            }
        }
    }
}

enum class EnumRobotMode {
    NEUTRAL, CERTAIN, UNCERTAIN
}
enum class EnumAnswer {
    TRUE, FALSE, UNDEFINED;
    companion object {
        fun fromString(ans: String): EnumAnswer {
            return when (ans.toUpperCase()) {
                "TRUE" -> EnumAnswer.TRUE
                "FALSE" -> EnumAnswer.FALSE
                else -> EnumAnswer.UNDEFINED
            }
        }
    }
    fun getOpposite(): EnumAnswer {
        return when (this) {
            EnumAnswer.TRUE -> EnumAnswer.FALSE
            EnumAnswer.FALSE -> EnumAnswer.TRUE
            else -> EnumAnswer.UNDEFINED
        }
    }
    fun agreesWith(ans: EnumAnswer): Boolean {
        return (this != EnumAnswer.UNDEFINED && this == ans)
    }
}
enum class EnumStages(val num: Double, val isQuestion: Boolean, val isSartingStage: Boolean) {
    STAGE_0(0.0,false, false), STAGE_0_1(0.1, false, false),
    STAGE_0_2(0.2, false, true), STAGE_1(1.0, true, false),
    STAGE_2(2.0, true, false),STAGE_3(3.0, true, false),
    STAGE_4(4.0,true, false),STAGE_5(5.0, true, false),
    STAGE_6(6.0, true, false), STAGE_7(7.0, true, false),
    STAGE_8(8.0, true, false), STAGE_0_3(0.3, false, false);
    companion object {
        fun fromString(input: String): EnumStages {
            val normalizedInput = input.toDoubleOrNull() ?: -1.0  // Convert string to double, defaulting to -1 if conversion fails
            return values().firstOrNull { it.num == normalizedInput } ?: STAGE_0  // Match the double against num, default to STAGE_0 if no match
        }
    }
    fun isQuestionStage(): Boolean {
        return this.isQuestion
    }
    fun isStartingStage(): Boolean {
        return this.isSartingStage
    }
    override fun toString(): String {
        return if (isQuestion) {
            num.toInt().toString()  // Converts num to Int, then to String
        } else {
            num.toString()  // Converts num directly to String
        }
    }
    fun getQuestionIndex(): Int {
        return if (isQuestion) {
            (num.toInt() - 1)
        } else {
            0
        }
    }
}
enum class EventCategory(){GUI, FLOW, SESS, ANY}
enum class EventType(val defMsg: String, val cat: EventCategory) {
    GUI_STARTED(NO_MESSAGE, EventCategory.GUI), GUI_RELOADED(NO_MESSAGE, EventCategory.GUI), SYNCH_REQUESTED(NO_MESSAGE, EventCategory.GUI),
    NEW_STAGE_REQUESTED(NO_MESSAGE, EventCategory.GUI), ANSWER_MARKED(NO_MESSAGE, EventCategory.GUI), USER_SET("USER TO ACTIVATE.", EventCategory.SESS),
    QUESTION_SET("NEW QUESTION TO ACTIVATE", EventCategory.SESS), ANSWER_CONFIRMED("QUESTION CONFIRMED IN GUI.", EventCategory.SESS),
    FLOW_START("SYS_START. INIT STATE", EventCategory.FLOW), NEW_FLOW_STATE("NEW FLOW STATE", EventCategory.FLOW),
    GENERIC("", EventCategory.ANY);
    companion object {
        fun fromString(typeStr: String): EventType {
            return when (typeStr.toUpperCase()) {
                "GUI_STARTED" -> GUI_STARTED
                "GUI_RELOADED" -> GUI_RELOADED
                "SYNCH_REQUESTED" -> SYNCH_REQUESTED //This happens when the GUI is instantiated. The system is built on the principle that it's the server
                // that serves, and the client that requests. Therefore, at the very beginning, the client requests what state it should go to.
                // This would not only work the first time, but any time: if the first thing the client asks is to know where it should go, it will
                // re-synch wherever the server it's at.
                // So SYNCH is a signal with no parameters.
                "NEW_STAGE_REQUESTED" -> NEW_STAGE_REQUESTED //This happens when the GUI requests a stage different from the one it's in, which in theory
                // should match the server's at this point.
                // This occurs in three occasions: 1) when the user selects their number at the very start, asking for state "0.2". The server knows that
                // "0.2" is the starting state, so apart from accepting the request, it activates the system. 2) Then in state 0.2, the GUI will show a
                // button to signal that they're ready, sending this request for state 1. The server will accept, but it knows that it's a question state
                // so it connect the flow  the button on the bottom to advance to the next stage, or when presses "Confirm" after answering. But it is also thought
                // to be used in case of emergency, after a fatal error occurred for some reason, and things need to be restarted at a particular stage. In that case, this
                // can be used to force the server to move to a particular stage.
                "ANSWER_MARKED" -> ANSWER_MARKED
                else -> GENERIC
            }
        }
    }
    fun isGUIEvent(): Boolean = (this.cat == EventCategory.GUI || this == GENERIC)
    fun isFLOWEvent(): Boolean = (this.cat == EventCategory.FLOW || this == GENERIC)
    fun isSYSEvent(): Boolean = (this.cat == EventCategory.SESS || this == GENERIC)
    fun getDefaultMessage(): String{return defMsg}
    fun isAcceptable(isGuiLoaded: Boolean, stage: EnumStages, appSubject: String, guiSubject: String): Boolean {
        println("EventType.isacceptable: ${this.toString()} - isGuiloaded ${isGuiLoaded} - stage ${stage.toString()} - appSubj ${appSubject} - guiSubj ${guiSubject}" )
        val guiSubjectMatches: Boolean = (appSubject == guiSubject)
        return when (this) {
            ANSWER_MARKED -> stage.isQuestionStage() && guiSubjectMatches // Return isQuestionStage itself
            NEW_STAGE_REQUESTED -> isGuiLoaded && (guiSubjectMatches || appSubject == UNDEFINED) // Return isQuestionStage itself
            else -> isGuiLoaded            // For all other types, return the value of isGuiLoaded
        }
    }
}

enum class EnumQuestionStageStates {// CONNECTED TO THE DATA SPREADSHEET
    REFLECTION,
    DISCLOSURE,
    PERSUASION,
    REVISION,
    CHECKPOINT,
    ULTIMATUM,
    CONFIRMATION
}
enum class EnumRejoinders {// CONNECTED TO THE DATA SPREADSHEET
    I_AM_DONE,
    ANSWER_MARKED,
    ANSWER_TRUE,
    ANSWER_FALSE,
    REJOINDER_AGREED,
    REJOINDER_DISAGREED,
    /* REJOINDER_NONCOMMITED, This was considered, but never used. */
    PROBE,
    DENIAL,
    ELABORATION_REQUEST,
    TIME_REQUEST,
    NON_COMMITTAL,
    BACKCHANNEL,
    OFF_TOPIC,
    ASSENT,
    REPEAT_REQUEST,
    SILENCE;
    fun getAnswer(): EnumAnswer {
        return when (this) {
            ANSWER_TRUE -> EnumAnswer.TRUE
            ANSWER_FALSE ->EnumAnswer.FALSE
            else -> EnumAnswer.UNDEFINED
        }
    }

}

enum class EnumStatementTypes { // CONNECTED TO THE DATA SPREADSHEET
    REFLECTION,
    MARKING_REQUEST,
    CLAIM,
    ASSERTION,
    PROBE,
    ASIDE,
    CHECKPOINT,
    ELABORATION_REQUEST,
    DISCLOSURE,
    CONFIRMATION_REQUEST,
    ULTIMATUM;
    companion object {
        fun fromString(ans: String): EnumStatementTypes {
            return try {
                enumValueOf<EnumStatementTypes>(ans.toUpperCase())
            } catch (e: IllegalArgumentException) {
                error("Source data error: EnumStatementTypes value '$ans' unknown")
            }
        }
    }
}


enum class EnumStatePhase {// CONNECTED TO THE DATA SPREADSHEET
ENTRY, BODY, ANY;

    companion object {
        fun fromString(ans: String): EnumStatePhase {
            return try {
                enumValueOf<EnumStatePhase>(ans.toUpperCase())
            } catch (e: IllegalArgumentException) {
                error("Error loading data: STATE_PHASE value '$ans' unknown")
            }
        }
    }
}
