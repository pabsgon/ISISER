// File: Session.kt
package furhatos.app.isiser
import Farewell
import com.sun.org.apache.bcel.internal.generic.Instruction
import furhatos.app.isiser.flow.Init
import furhatos.app.isiser.flow.main.*
import furhatos.app.isiser.handlers.*
import furhatos.app.isiser.setting.*
import furhatos.event.Event
import furhatos.event.EventListener
import furhatos.event.EventSystem
import furhatos.event.actions.ActionGesture
import furhatos.event.monitors.MonitorSpeechStart
import furhatos.event.senses.SenseSpeech
import furhatos.flow.kotlin.FlowControlRunner
import furhatos.flow.kotlin.State
import io.ktor.websocket.*
import java.util.logging.*

object App {
    /* ------------------------- PRIVATE INMUTABLE VARS ------------------------------ */
    /* ------------------------- PRIVATE MUTABLE VARS ------------------------------ */
    private var logHandler: LogHandler = LogHandler()
    private var eventFactory: EventFactory = EventFactory()
    private var dataHandler: DataHandler = DataHandler(eventFactory)
    private var guiHandler: GUIHandler = GUIHandler(eventFactory)
    private var flowHandler: FlowHandler = FlowHandler(eventFactory)
    private var sessionHandler: SessionHandler = SessionHandler(dataHandler,flowHandler, guiHandler )
    private val loggerEventListener = EventListener { event -> handleEvent(event)}
    //private var fcr: FlowControlRunner? = null


    fun getSession(): SessionHandler = sessionHandler
    fun getGUI(): GUIHandler = guiHandler

    private fun handleEvent(event: Event?) {
        when (event) {
            is GUIEvent -> {
                guiHandler.handleEvent(event)

            }
            is FlowEvent -> {
                flowHandler.handleEvent(event)
            }

            is SessionEvent -> {
                sessionHandler.handleEvent(event)
            }

            is SenseSpeech -> {
            }
            is ActionGesture -> {
            }
            is MonitorSpeechStart -> {

            }
        }
        logHandler.log(event!!, this.getStageName(), this.getSubject())
    }

    private lateinit var fileHandler: FileHandler


    /* ------------------------- PUBLIC VARS ------------------------------ */
    var currentSession: WebSocketSession? = null
    suspend fun sendToCurrentSession(message: String) {
        currentSession?.send(message)
    }

    /* ------------------------- SETTERS AND GETTERS ------------------------------ */
    fun isGUILoaded(): Boolean {return guiHandler.isGUILoaded()}
    fun setGUILoaded() {guiHandler.setGUILoaded()}
    fun getStage(): EnumStages  {return guiHandler.getStage()}
    fun getStageName(): String  {return guiHandler.getStageName()}
    // Method to set the stage

    fun setStage(value: String) {
        println("XXX> setSubject ${value} ")
        guiHandler.setStage(value)}
/*
    fun isQuestionStage(): Boolean {return guiHandler.isQuestionStage()}

    fun isAnswerMarked(): Boolean  {return guiHandler.isAnswerMarked()}
*/

    fun getSubject(): String  {return sessionHandler.getUser()}

    fun getCondition(): EnumConditions = sessionHandler.getCondition()
    fun getStateId(): String = flowHandler.getStateId()


    /* ------------------------- METHODS ------------------------------ */

    fun startLogger(){
        logHandler.log("----------------- NEW EXECUTION-------------------")
    }
    fun startFlow(newFcr: FlowControlRunner){
        //eventFactory.triggerFlowEvent( newFcr )
        flowHandler.setFlowRunner(newFcr)
    }
    fun goto(to:State){
        eventFactory.triggerFlowEvent(to)
        flowHandler.goto(to)
    }

    fun printState(st: State, entry: String ){
        println("-------------------${st.name}[${entry}]--------------------")
    }
    fun printState(st: State){printState(st,"E")}

/*    fun isCurrentStateTriMode(): Boolean{
        return when(flowHandler.getStateId()){
            Init.name,
            Sleep.name,
            Welcome.name,
            QuestionReflection.name,-> false

            QuestionDisclosure().name,
            QuestionPersuasion().name,
            QuestionReview().name,
            QuestionCheckpoint().name -> true

            QuestionUltimatum().name,
            QuestionConfirmation().name,
            Farewell.name -> false

            else -> {false}
        }
        *//*return EnumStates.fromState()?.robotModeApplies?: false*//*
    }*/
    /* ------------------------- INIT ------------------------------ */
    init{
        EventSystem.addEventListener(App.loggerEventListener)

        println("----------------- LOADING DATA-------------------")

        dataHandler.loadSheetData(sessionHandler.getQuestions())
        dataHandler.printStatements()
        sessionHandler.printQuestions()
        dataHandler.printConditions()
        dataHandler.printWordings()
        dataHandler.printAsides()


        println("----------------- DATA LOADED -------------------")
    }

}
