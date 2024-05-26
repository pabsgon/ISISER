package furhatos.app.isiser.handlers
import furhatos.app.isiser.flow.Init
import furhatos.app.isiser.setting.EventType
import furhatos.app.isiser.setting.ExtendedUtterance
import furhatos.app.isiser.setting.WAITING_ANSWER_TIMEOUT
import furhatos.flow.kotlin.*
import furhatos.gestures.Gesture

class FlowHandler(evFactory: EventFactory ) {
    private val EvFactory: EventFactory = evFactory
    private var state: State = Init //Promised
    private var flowRunner: FlowControlRunner? = null
    private var ready: Boolean = false

    fun getStateId(): String = state.name
    fun getState():State = state
    fun isReady(): Boolean = (flowRunner != null && state != null)
    fun handleEvent(event: FlowEvent) {
        when (event.type) {
            EventType.NEW_FLOW_STATE -> {
                /*state = event.data["state"] as State*/
                state = event.state as State
            }
            EventType.FLOW_START -> {
                //flowRunner = event.data["flowRunner"] as? FlowControlRunner
            }
            else -> {} // Nothing to do
        }
    }
    fun goto(to: State){
        flowRunner!!.goto(to)
    }

    fun setFlowRunner(newFcr: FlowControlRunner) {
        flowRunner = newFcr
    }

}
fun Furhat.doAsk(s: String){
    //this.voice.rate = 1.0
    this.ask(s,  timeout = WAITING_ANSWER_TIMEOUT)
}
fun Furhat.doAsk(s: String, g: Gesture){
    //this.voice.rate = 1.0
    this.gesture(g, )
    this.ask(s,  timeout = WAITING_ANSWER_TIMEOUT)
}
fun Furhat.doAsk(u: Utterance){
    this.voice.rate = 1.0
    this.ask(u)
}
fun Furhat.doAsk(u: UtteranceDefinition){
    this.voice.rate = 1.0
    this.ask(u)
}
fun Furhat.doAsk(u: ExtendedUtterance, rate: Double? = null, timeout:Int? = null){
    this.voice.rate = rate ?: u.rate
    this.ask(u.utterance, timeout = timeout?: WAITING_ANSWER_TIMEOUT)
}

fun Furhat.doSay(s: String){
    this.voice.rate = 1.0
    this.say(s)
}
fun Furhat.doSay(u: Utterance){
    this.voice.rate = 1.0
    this.say(u)
}
fun Furhat.doSay(u: UtteranceDefinition){
    this.voice.rate = 1.0
    this.say(u)
}
fun Furhat.doSay(u: ExtendedUtterance, rate: Double? = null){
    this.voice.rate = rate ?: u.rate
    this.say(u.utterance)
}