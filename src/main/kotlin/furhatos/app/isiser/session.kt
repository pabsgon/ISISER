// File: Session.kt
package furhatos.app.isiser
import furhatos.app.isiser.setting.*
import furhatos.event.EventListener
import furhatos.event.EventSystem
import furhatos.event.actions.ActionGesture
import furhatos.event.monitors.MonitorSpeechStart
import furhatos.event.senses.SenseSpeech
import furhatos.flow.kotlin.State
import furhatos.records.Record
import io.ktor.websocket.*
import org.slf4j.LoggerFactory
import java.util.logging.*

object Session {
    /* ------------------------- PRIVATE INMUTABLE VARS ------------------------------ */
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
            is GUIEvent -> logEntry = "\t${event.type}\t${event.message}\t${this.getStageName()}\t${this.getSubject()}"
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
    private val logger = LoggerFactory.getLogger(Session::class.java)

    /* ------------------------- PRIVATE MUTABLE VARS ------------------------------ */
    private var appStage: StagesEnum = StagesEnum.STAGE_0
    private var subject: String = UNDEFINED
    private var curAnswer = ""
    private var guiLoaded = false
    private lateinit var fileHandler: FileHandler

    /* ------------------------- PUBLIC VARS ------------------------------ */
    var currentSession: WebSocketSession? = null
    suspend fun sendToCurrentSession(message: String) {
        currentSession?.send(message)
    }

    /* ------------------------- SETTERS AND GETTERS ------------------------------ */
    fun isGUILoaded(): Boolean{
        return guiLoaded
    }
    fun setGUILoaded(){
        guiLoaded = true
    }
    fun getStage(): StagesEnum = appStage
    fun getStageName(): String = appStage.toString()

    // Method to set the stage
    fun setStage(value: String) {
        println("Setting stage from ${appStage} to ${value}")
        appStage = StagesEnum.fromString(value)
    }
    fun isQuestionStage(): Boolean = appStage.isQuestionStage()


    fun getAnswer(): String = curAnswer

    // Method to set the stage
    fun setAnswer(value: String) {
        if(value != "" || value != curAnswer )curAnswer = value
    }
    fun getSubject(): String = subject

    // Method to set the stage
    fun setSubject(value: String) {
        if(value != "" && value != subject )subject = value
    }
    fun isSubjectUndefined(): Boolean = subject == UNDEFINED

    fun resetAnswer(){
        curAnswer = ""
    }
    /* ------------------------- METHODS ------------------------------ */
    fun startLogger(){
        log("----------------- NEW EXECUTION-------------------")
    }
    fun log(message: String) {
        //println("[***]:" + message)
        logger.info(message)
    }
    fun userAnswered(): Boolean = curAnswer != ""

    fun printState(st: State, entry: String ){
        println("-------------------${st.name}[${entry}]--------------------")
    }
    fun printState(st: State){printState(st,"E")}
    /* ------------------------- INIT ------------------------------ */
    init{
        EventSystem.addEventListener(Session.loggerEventListener)
        val spreadsheetId = "1--UZgR4W01c7Z5yml06KCWw4xm8hRBJLVv-bT63iyzg" // Your spreadsheet ID
        val range = "QuestionDefinition!A1:W24"

        val data = loadSheetData(spreadsheetId, range)
        println("----------------- DATA-------------------")

        println(data)
        println("----------------- END DATA-------------------")

    }

}
