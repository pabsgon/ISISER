package furhatos.app.isiser.handlers

import furhatos.app.isiser.flow.Init
import furhatos.app.isiser.setting.EventType
import furhatos.flow.kotlin.FlowControlRunner
import furhatos.flow.kotlin.State

class FlowHandler(evFactory: EventFactory ) {

    private val EvFactory: EventFactory = evFactory
    private var state: State = Init //Promised
    private var flowRunner: FlowControlRunner? = null
    private var ready: Boolean = false

    fun getStateId(): String = state.name
    fun isReady(): Boolean = (flowRunner != null && state != null)
    fun handleEvent(event: FlowEvent) {
        when (event.type) {
            EventType.NEW_FLOW_STATE -> {
                state = event.data["state"] as State
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