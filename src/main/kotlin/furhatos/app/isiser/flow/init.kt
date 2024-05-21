package furhatos.app.isiser.flow

import furhatos.app.isiser.App
import furhatos.app.isiser.flow.main.Sleep
import furhatos.app.isiser.flow.main.Testing
import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.PollyVoice
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
        when (APP_EXECUTION_MODE) {
            PRO,DEV -> App.goto(Sleep)
            TEST_INTENTS -> App.goto(Testing)
            TEST_UTTERANCES -> {
                TODO()
            }
        }
    }
}

class MatthewNeutral: PollyVoice.Matthew() {
    override fun transform(text: String): String {
        val myTransformedText = prosody(text, rate = 1.0)
        // Don't forget to call super.transform() !!
        return super.transform(myTransformedText)
    }
}
class MatthewCertain: PollyVoice.Matthew() {
    override fun transform(text: String): String {
        val myTransformedText = prosody(text, rate = 1.25)
        // Don't forget to call super.transform() !!
        return super.transform(myTransformedText)
    }
}
class MatthewUncertain: PollyVoice.Matthew() {
    override fun transform(text: String): String {
        val myTransformedText = prosody(text, rate = 0.75)
        // Don't forget to call super.transform() !!
        return super.transform(myTransformedText)
    }
}
