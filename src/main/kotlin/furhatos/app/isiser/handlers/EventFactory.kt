package furhatos.app.isiser.handlers

import furhatos.app.isiser.App
import furhatos.app.isiser.setting.EnumConditions
import furhatos.app.isiser.setting.EventType
import furhatos.app.isiser.setting.EnumStages
import furhatos.event.Event
import furhatos.event.EventSystem
import furhatos.flow.kotlin.FlowControlRunner
import furhatos.flow.kotlin.State
import io.ktor.http.*

class EventFactory {
    fun triggerGUIEvent( evType: EventType): ISISEREvent{ return sendEvent(GUIEvent(evType)) }
    fun triggerGUIEvent( initialData: Map<String, String>, isGUIloaded: Boolean): ISISEREvent{ return sendEvent(GUIEvent(initialData, isGUIloaded)) }
    fun triggerSessionEvent(msg: String, subject: String, type: EventType? = EventType.GENERIC): ISISEREvent{ return sendEvent(SessionEvent(msg, subject,type)) }
    fun triggerSessionEvent(type: EventType, subject: String): ISISEREvent{ return sendEvent(SessionEvent(type, subject)) }
    fun triggerSessionEvent(type:EventType, stage: EnumStages): ISISEREvent{ return sendEvent(SessionEvent(type, stage)) }
    fun triggerFlowEvent(newFcr: FlowControlRunner): ISISEREvent{ return sendEvent(FlowEvent(newFcr)) }
    fun triggerFlowEvent( state: State): ISISEREvent{ return sendEvent(FlowEvent(state)) }

    fun sendEvent(ev: ISISEREvent): ISISEREvent{
        EventSystem.send(ev)
        return ev
    }
}
open class ISISEREvent (
    open val type: EventType = EventType.GENERIC,
    open val message: String = type.getDefaultMessage(),
    open val stage: EnumStages = App.getStage(),
    open val subject: String = App.getSubject(),
    open val condition: EnumConditions = App.getCondition(),
    open val stateId: String =  App.getStateId()
) : Event() {
    // Initialization code or common methods here
}

class GUIEvent(
    //Differently as the other type of events, GUIEvent needs an answer NOW.
    // The GUI is waiting for an ok. So it is now when the new stage must
    // be approved, or the new subject, or the new answer. Hence the
    // function "isAcceptable".
    override val type: EventType,
    initialData: Map<String, String>,
    isGUIloaded: Boolean
) : ISISEREvent(type = EventType.fromString(initialData["type"] ?: "GENERIC"),
    message = initialData["message"] ?: "No message") {

    val isQuestionStage: Boolean = false
    var requestData: Map<String, String> = initialData.toMap() // Create a defensive copy
    val guiSubject: String = requestData["subject"] ?: ""
    val isAcceptable: Boolean = type.isAcceptable(isGUIloaded, stage, subject, guiSubject)
    override var message: String = super.message + (if (isAcceptable) " [ACCEPTED]" else " [REJECTED]")
    val responseData: Map<String, String> = mapOf(
        "status" to (if (isAcceptable) "0" else "1"),
        "received" to message,
        "answer" to (requestData["answer"] ?: ""),
        "subject" to (if (isAcceptable) guiSubject else subject),
        "stage" to (requestData["stage"] ?: stage.toString())
    )

    val responseStatusCode: HttpStatusCode = if (isGUIloaded) HttpStatusCode.OK else HttpStatusCode.Forbidden

    // Override or add additional methods as necessary
    fun getRequestValue(key: String, defaultValue: String = ""): String = requestData.getOrDefault(key, defaultValue)
    fun getResponseValue(key: String, defaultValue: String = ""): String = responseData.getOrDefault(key, defaultValue)

    constructor(evType: EventType) : this(
        type = evType,
        initialData = mapOf("message" to evType.toString(), "type" to evType.toString()),
        isGUIloaded = true // Defaulting accept to false or true as needed
    )
    constructor(    initialData: Map<String, String>,
                    isGUIloaded: Boolean) : this(
        type = EventType.fromString(initialData["type"] ?: "GENERIC"),
        initialData,  isGUIloaded
    )
}
class SessionEvent(
    message: String,
    subject: String,
    type: EventType? = EventType.GENERIC
) : ISISEREvent(
    message = message,
    subject = subject,
    type = type ?: EventType.GENERIC  // Ensures a non-null value is passed

) {
    var data: MutableMap<String, Any> = mutableMapOf()

    constructor(type: EventType,
                subject: String) : this(
        message = type.getDefaultMessage(),
        subject = subject,
        type = type
    )
    constructor(type: EventType, stage: EnumStages) : this(
        message = type.getDefaultMessage(),
        subject = "",
        type = type
    ){
        data["stage"] =  stage // Add flowRunner to the map
    }
}

class FlowEvent(
    message: String,
    subject: String,
    type: EventType? = EventType.GENERIC,
    val state: State? = null // New property to store state
) : ISISEREvent(
    message = message,
    subject = subject,
    type = type ?: EventType.GENERIC  // Ensures a non-null value is passed
) {
    override val stateId = if (type == EventType.NEW_FLOW_STATE) message else super.stateId
    var data: MutableMap<String, Any> = mutableMapOf()

    constructor(
        type: EventType,
        subject: String
    ) : this(
        message = type.getDefaultMessage(),
        subject = subject,
        type = type,
        state = null
    )

    constructor(state: State) : this(
        message = state.name,
        subject = "",
        type = EventType.NEW_FLOW_STATE,
        state = state // Directly set the state property
    )

    constructor(newFcr: FlowControlRunner) : this(
        message = "",
        subject = "",
        type = EventType.FLOW_START,
        state = null
    ) {
        data["flowRunner"] = newFcr  // Add flowRunner to the map
    }
}

/*
class GUIEvent2(initialData: Map<String, String>,
                val isGUIloaded: Boolean ) : Event() {
    val isQuestionStage: Boolean = false
    var requestData: Map<String, String> = initialData.toMap() // Create a defensive copy
    val type: EventType = EventType.fromString(requestData["type"] ?: "GENERIC")
    val guiSubject: String = requestData["subject"] ?: ""
    val isAcceptable: Boolean = type.isAcceptable(isGUIloaded, appStage, appSubject, guiSubject )
    var message: String = (requestData["message"] ?: "No message") + (if (isAcceptable) " [ACCEPTED]" else " [REJECTED]") // Set default if key "message" is not found
    val responseData: Map<String, String> = mapOf("status" to (if (isAcceptable) "0" else "1"),
        "received" to message,
        "answer" to  (requestData["answer"] ?: ""),
        "subject" to (if (isAcceptable) guiSubject else appSubject),
        "stage" to (requestData["stage"] ?: appStage.toString())) //
    val responseStatusCode: HttpStatusCode = if (isGUIloaded) HttpStatusCode.OK else HttpStatusCode.Forbidden


    fun getRequestValue(key: String, defaultValue: String = ""): String = requestData.getOrDefault(key, defaultValue)
    fun getResponseValue(key: String, defaultValue: String = ""): String = responseData.getOrDefault(key, defaultValue)

    constructor(message: String, appStage: StagesEnum, appSubject: String ) : this(
        initialData = mapOf("message" to message, "type" to message),
        appStage = appStage, // Defaulting appStage to a sensible default
        appSubject = appSubject, // Defaulting appSubject to a sensible default
        isGUIloaded = true // Defaulting accept to false or true as needed
    )
    constructor(evType: EventType, appStage: StagesEnum, appSubject: String ) : this(evType.toString(), appStage, appSubject)
    init {}
}


 */
