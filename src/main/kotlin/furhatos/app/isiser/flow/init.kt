package furhatos.app.isiser.flow

import com.sun.org.apache.xpath.internal.operations.Bool
import furhatos.app.isiser.Session
import furhatos.app.isiser.flow.main.Idle
import furhatos.app.isiser.flow.main.Sleep
import furhatos.app.isiser.setting.EventType
import furhatos.app.isiser.setting.distanceToEngage
import furhatos.app.isiser.setting.maxNumberOfUsers
import furhatos.event.Event
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.Voice
import furhatos.util.CommonUtils
import io.ktor.http.*


val consoLog = CommonUtils.getLogger("ISISER_CONSOLE")
//val logger = Logger.getLogger("ISISER")

val Init : State = state() {
    //dialogLogger.startSession(directory = "ISISER\\", maxLength = 20)
    init {
        try {
            // Define the file handler and formatter
            Session.startLogger()
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
        users.setSimpleEngagementPolicy(distanceToEngage, maxNumberOfUsers)
        furhat.voice = Voice("Matthew")
        /** start the interaction */
        goto(Sleep)
    }
}


