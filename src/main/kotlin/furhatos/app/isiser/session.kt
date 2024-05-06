// File: Session.kt
package furhatos.app.isiser
import furhatos.app.isiser.setting.GUIEvent
import furhatos.app.isiser.setting.logAllEvents
import furhatos.event.EventListener
import furhatos.event.EventSystem
import furhatos.event.actions.ActionGesture
import furhatos.event.monitors.MonitorSpeechStart
import furhatos.event.senses.SenseSpeech
import furhatos.records.Record
import io.ktor.websocket.*
import org.slf4j.LoggerFactory
import java.util.logging.*

object Session {
    var currentSession: WebSocketSession? = null

    suspend fun sendToCurrentSession(message: String) {
        currentSession?.send(message)
    }
    private val loggerEventListener = EventListener { event ->
        if(logAllEvents) {
            println("Event received: ${event.javaClass.simpleName}\t${event.eventParams}")
            /*
            TODO("At the moment I am only logging to the console. We may want in the future to log everything." +
                    "In that case, a new logger needs to be created (e.g. fullLogger), to log into another file.")
             */
        }
        var logEntry = ""
        when (event) {
            is GUIEvent -> logEntry = "\t${event.message}"
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
    init{
        EventSystem.addEventListener(Session.loggerEventListener)
    }
    private var appStage = "0"
    private var curAnswer = ""
    private var guiLoaded = false
        get() = field
        set(value) {
            field = value
        }
    private lateinit var fileHandler: FileHandler
    private val logger = LoggerFactory.getLogger(Session::class.java)

    fun startLogger(){
        log("----------------- NEW EXECUTION-------------------")
    }
    fun log(message: String) {
        //println("[***]:" + message)
        logger.info(message)
    }
    fun isGUILoaded(): Boolean{
        return guiLoaded
    }
    fun setGUILoaded(){
        guiLoaded = true
    }
    fun getStage(): String = appStage

    // Method to set the stage
    fun setStage(value: String) {
        appStage = value
    }
    fun getAnswer(): String = curAnswer

    // Method to set the stage
    fun setAnswer(value: String) {
        if(value != "" || value != curAnswer )curAnswer = value
    }
}



//class DEBUGGING(val message: String) : Event()