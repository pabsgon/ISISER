package furhatos.app.isiser.flow

import furhatos.app.isiser.App
import furhatos.app.isiser.flow.main.Sleep
import furhatos.app.isiser.flow.main.Testing
import furhatos.app.isiser.setting.ENGAGMENT_DISTANCE
import furhatos.app.isiser.setting.MAX_NUM_USERS
import furhatos.app.isiser.setting.TESTING_LEVEL
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.Voice
import furhatos.util.CommonUtils

val consoLog = CommonUtils.getLogger("ISISER_CONSOLE")
//val logger = Logger.getLogger("ISISER")

val Init : State = state() {
    //dialogLogger.startSession(directory = "ISISER\\", maxLength = 20)
    init {
        App.startFlow( this)

        try {
            // Define the file handler and formatter
            App.startLogger()
        } catch (e: Exception) {
            /*
            TODO:
            STOP EVERYTHING HERE SINCE LOGGER IS NOT WORKING.
            OR ALLOW SIMPLE LOGGING IN THE CONSOLE (BUT IT WILL HAVE TO BE
            COPIED MANUALLY)
             */
            consoLog.error("Failed to log to file: ${e.message}")

        }
        //dialogLogger.startSession()
        /** Set our default interaction parameters */
        users.setSimpleEngagementPolicy(ENGAGMENT_DISTANCE, MAX_NUM_USERS)
        furhat.voice = Voice("Matthew")
        /** start the interaction */
        when (TESTING_LEVEL) {
            0 -> App.goto(Sleep)
            1 -> App.goto(Testing)
        }
    }
}


