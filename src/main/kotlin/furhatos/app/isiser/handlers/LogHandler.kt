package furhatos.app.isiser.handlers

import furhatos.app.isiser.App
import furhatos.app.isiser.setting.APP_EXECUTION_MODE
import furhatos.app.isiser.setting.EventType
import furhatos.app.isiser.setting.LOG_ALL_EVENTS
import furhatos.app.isiser.setting.PRO
import furhatos.event.Event
import furhatos.event.actions.ActionGesture
import furhatos.event.monitors.MonitorSpeechStart
import furhatos.event.senses.SenseSpeech
import furhatos.records.Record
import org.slf4j.LoggerFactory

class LogHandler {
    private val logger = LoggerFactory.getLogger(App::class.java)


    fun log(message: String) {
        logger.info(message)
    }
    fun log(event: Event, stage: String, subject: String){
        if(LOG_ALL_EVENTS) {
            println("Event received: ${event.javaClass.simpleName}\t${event.eventParams}")
            /*
            TODO("At the moment I am only logging to the console. We may want in the future to log everything." +
                    "In that case, a new logger needs to be created (e.g. fullLogger), to log into another file.")
             */
        }
        var logEntry = ""
        when (event) {
            is GUIEvent -> {
                if(APP_EXECUTION_MODE > PRO){
                    if(event.type!= EventType.SYNCH_REQUESTED) {
                        logEntry =
                            "\t${event.type}\t${event.message}\t${event.stage}\t${event.subject}\t${event.condition}\t${event.stateId}"
                    }
                }else{
                    logEntry = "\t${event.type}\t${event.message}\t${event.stage}\t${event.subject}\t${event.condition}\t${event.stateId}"
                }
            }
            is FlowEvent -> logEntry = "\t${event.type}\t${event.message}\t${event.stage}\t${event.subject}\t${event.condition}\t${event.stateId}"

            is SessionEvent -> logEntry = "\t${event.type}\t${event.message}\t${event.stage}\t${event.subject}\t${event.condition}\t${event.stateId}"

            is ISISEREvent -> logEntry = "\t${event.type}\t${event.message}\t${event.stage}\t${event.subject}\t${event.condition}\t${event.stateId}"

            is SenseSpeech -> {
                if(event.text != "") logEntry = "\t${event.text}\t${event.length}"
            }
            is ActionGesture -> {
                val gest: Record = event.eventParams.getRecord("gesture")
                if(gest.get("name") != "Blink") logEntry = "\t${gest.get("name")}"
            }
            is MonitorSpeechStart -> logEntry = "\t${event.text}\t${event.length}"
        }
        if(logEntry !=""){
            logEntry = "${event.javaClass.simpleName}${logEntry}"
            println(logEntry)
            log(logEntry)
        }
    }

}