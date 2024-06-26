package furhatos.app.isiser.setting
val GUI = "GUI"
val GUI_PORT = 8888
val GUI_HOSTNAME = "http://localhost:" + GUI_PORT
val UNDEFINED = "UNDEFINED"
val UNFRIENDLY_SUFFIX = "_U"
val FRIENDLY_SUFFIX = "_F"
val ASSERTIONS_PER_CLAIM = 2
val MAX_QUESTIONS=8
val SOURCEDATA_SPREADSHEETID = "1--UZgR4W01c7Z5yml06KCWw4xm8hRBJLVv-bT63iyzg" // Your spreadsheet ID
val SOURCEDATA_RANGE = "RAWDATA_FOR_LOADING!A3:AS150"
val SOURCEDATA_CREDENTIAL_FILE_PATH= "C:\\Development\\ISISER\\Isiser\\src\\main\\resources\\json\\isiser00-4078fd895f22.json" // Adjust this path
val SOURCEDATA_SETTINGS_SIZE = 9 // the first N elements of each row are settings, the rest texts.
val SOURCEDATA_CLAIM_SIZE = 6 // the first N elements of each row are settings, the rest texts.
val SOURCEDATA_TRUE = "TRUE"
val SOURCEDATA_FALSE = "FALSE"
val SOURCEDATA_FRIENDLY = "FRIENDLY"
val SOURCEDATA_QUESTION = 0
val SOURCEDATA_TYPE = 4
val SOURCEDATA_STATEMENT_INDEX = 5
val SOURCEDATA_PERTRIPLET = 6
val SOURCEDATA_SUBTYPE = 7
val SOURCEDATA_ID = 8


enum class EnumQuestionPhase {
    REFLECTION, DISCLOSURE, PERSUASION, REVIEW, CHECKPOINT, ULTIMATUM, CONFIRMATION
}
enum class EnumConditions {
    CONDITION1, CONDITION2, CONDITION3;
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
enum class EnumStatePhase { ENTRY, BODY, ANY;

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
}
enum class StagesEnum(val num: Double, val isQuestion: Boolean) {
    STAGE_0(0.0,false), STAGE_0_1(0.1, false), STAGE_0_2(0.2, false),
    STAGE_1(1.0, true),STAGE_2(2.0, true),STAGE_3(3.0, true),
    STAGE_4(4.0,true),STAGE_5(5.0, true),STAGE_6(6.0, true),
    STAGE_7(7.0, true),STAGE_8(8.0, true), STAGE_0_3(0.3, false);
    companion object {
        fun fromString(input: String): StagesEnum {
            val normalizedInput = input.toDoubleOrNull() ?: -1.0  // Convert string to double, defaulting to -1 if conversion fails
            return values().firstOrNull { it.num == normalizedInput } ?: STAGE_0  // Match the double against num, default to STAGE_0 if no match
        }
    }
    fun isQuestionStage(): Boolean {
        return this.isQuestion
    }
    override fun toString(): String {
        return if (isQuestion) {
            num.toInt().toString()  // Converts num to Int, then to String
        } else {
            num.toString()  // Converts num directly to String
        }
    }

}

enum class EventType {
    GUI_STARTED, GUI_RELOADED, SYNCH_REQUESTED, NEW_STAGE_REQUESTED, ANSWER_SENT, GENERIC;
    companion object {
        fun fromString(typeStr: String): EventType {
            return when (typeStr.toUpperCase()) {
                "GUI_STARTED" -> GUI_STARTED
                "GUI_RELOADED" -> GUI_RELOADED
                "SYNCH_REQUESTED" -> SYNCH_REQUESTED //This happens the GUI is instantiated. This always happens once the system starts. The next thing is >
                // for the user to open up the GUI, which will call the server with this request. The same thing would >
                // happen after the eventuality of closing the GUI. The following attempt to re-open the GUI will generate >
                // this event. The server, at this point of the code, did already send the current stage, for the GUI to synch.
                "NEW_STAGE_REQUESTED" -> NEW_STAGE_REQUESTED //This happens when the GUI requests a stage different than the one that the servers holds. This is normal
                //when the user presses the button on the bottom to advance to the next stage, or when presses "Confirm" after answering. But it is also thought to be
                // used in case of emergency, after a fatal error occurred for some reason, and things need to be restarted at a particular stage. In that case, this
                //can be used to force the server to move to a particular stage.
                "ANSWER_SENT" -> ANSWER_SENT
                else -> GENERIC
            }
        }
    }
    fun isAcceptable(isGuiLoaded: Boolean, stage: StagesEnum, appSubject: String, guiSubject: String): Boolean {
        println("EventType.isacceptable: ${this.toString()} - isGuiloaded ${isGuiLoaded} - stage ${stage.toString()} - appSubj ${appSubject} - guiSubj ${guiSubject}" )
        val guiSubjectMatches: Boolean = (appSubject == guiSubject)
        return when (this) {
            ANSWER_SENT -> stage.isQuestionStage() && guiSubjectMatches // Return isQuestionStage itself
            NEW_STAGE_REQUESTED -> isGuiLoaded && (guiSubjectMatches || appSubject == UNDEFINED) // Return isQuestionStage itself
            else -> isGuiLoaded            // For all other types, return the value of isGuiLoaded
        }
    }
}

enum class EnumQuestionStageStates {
    PERSUASION,
    REVISION,
    CHECKPOINT,
    ULTIMATUM
}
enum class EnumSubjectRejoinders {
    REJOINDER_AGREED,
    REJOINDER_DISAGREED,
    REJOINDER_NONCOMMITED,
    PROBE,
    DENIAL,
    ELABORATION_REQUEST,
    NON_COMMITTAL,
    BACKCHANNEL,
    OFF_TOPIC,
    ASSENT,
    REPEAT_REQUEST,
    SILENCE
}

enum class EnumStatementTypes {
    CLAIM,
    ASSERTION,
    PROBE,
    ASIDE,
    CHECKPOINT,
    ELABORATION_REQUEST,
    DISCLOSURE,
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



