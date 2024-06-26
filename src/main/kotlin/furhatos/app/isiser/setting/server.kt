package furhatos.app.isiser.setting


import furhatos.app.isiser.Session
import furhatos.event.Event
import furhatos.event.EventSystem
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration


fun Application.module() {
    println("Starting server...")
    install(CORS) {
        anyHost() // You might want to restrict this in production!
    }
    println("Installing Status pages...")
    install(StatusPages) {
        exception<Throwable> {call, cause ->
            println("Unhandled exception caught ($cause)")
            call.application.log.error("[ISISER.KTH] [CORS] Unhandled exception caught", cause)
            call.respond(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }
    install(ContentNegotiation) {
        jackson()
    }
    install(WebSockets) {
        pingPeriod = Duration.ofMinutes(1)  // Keep the connection alive with a ping
        timeout = Duration.ofMinutes(15)    // Close inactive connections
        maxFrameSize = Long.MAX_VALUE       // Frame size limits for messages
        masking = false
    }
    routing {
        println("Routing...")
        static("/") {
            defaultResource("index.html", "gui")
            resources("gui")
            intercept(ApplicationCallPipeline.Call) {
                if (call.request.uri == "/") {
                    if(Session.isGUILoaded()){
                        EventSystem.send(GUIEvent(EventType.GUI_STARTED, Session.getStage(), Session.getSubject()))
                    }else{
                        Session.setGUILoaded()
                        EventSystem.send(GUIEvent(EventType.GUI_RELOADED, Session.getStage(), Session.getSubject()))
                    }
                }
            }
        }
        post("/receive") {
            val data = call.receive<Map<String, String>>()
            /*val message = data["message"] ?: "No message"
            val stage = data["stage"] ?: Session.getStage()
            val answer = data["answer"] ?: ""
            */
            /*
                This is the handler of the messages coming from the GUI, which may contain two fields:
                    1. stage
                    2. message
                The logic here is:
                    stage:
                      If stage is not provided, the GUI is requesting the stage. The response will
                      contain the current stage (Session.appstage)
                      If stage is provided,
                        if stage is an INVALID stage it will return the appStage but this is not
                        performed (2024/05/05)
                            TODO("CREATE AN ARRAY OF VALID STAGES = 0, 0.1, 0.2, 0.3 and 1-8)" etc.)
                        if stage (valid) is NOT the same as the appStage,
                          the GUI is requesting a particular stage to be set in
                          the system. The response will be 0 (no error), which will make the GUI move
                          to that stage.
                          It is important to note that the server will also move to the requested stage
                          This is means that the GUI can force the server to move to a particular stage.
                          For example, if currently appStage=2 (question 2), if the GUI is instantiated
                          with stage 4 (this can be done by accessing index.html with the parameter
                          stage=4: e.g. htpp://localserver:[port]/index.html?stage=4) the GUI will send
                          the request stage=4, the server will accept and both will move to stage 4.
                       if stage (valid) IS the same as the appStage,
                          the GUI is informing of an event in the current stage. This can be:
                            - the user selected either TRUE or FALSE answer.
                            - the user selected the CONFIRM button of the question.
             */

            val guiLoaded = Session.isGUILoaded()

            /*
            val statusStr = if (guiLoaded) " [ACCEPTED]" else " [REJECTED]"
            val status = if (guiLoaded) 0 else 1
            val statusCode: HttpStatusCode = if (guiLoaded) HttpStatusCode.OK else HttpStatusCode.Forbidden
            if(guiLoaded){
                Session.setStage(stage)
                Session.setAnswer(answer)
            }
             */
            val event = GUIEvent(data, Session.getStage(), Session.getSubject(), guiLoaded)
            if(event.isAcceptable){
                Session.setStage(event.getResponseValue("stage"))
                Session.setSubject(event.getResponseValue("subject"))
                Session.setAnswer(event.getResponseValue("answer"))
            }

            println("XXXX isGuiloaded $event.responseStatusCode")
            call.respond(event.responseStatusCode, event.responseData)  // Ensure this is a proper JSON response

            /*call.respond(statusCode, mapOf("status" to status,
                "received" to message,
                "stage" to stage))  // Ensure this is a proper JSON response

            val event2 = GUIEvent2(message + statusStr)
            */

            // Create and send the event
            EventSystem.send(event)
        }
        var currentSession: WebSocketSession? = null

        webSocket("/updates") {
            Session.currentSession = this
            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            send("Received: $text")  // Echo or respond to the message
                        }
                        is Frame.Binary -> {
                            // Handle binary frames if necessary
                        }
                        is Frame.Ping -> {
                            // Handle pings if necessary
                        }
                        is Frame.Pong -> {
                            // Handle pongs if necessary
                        }
                        is Frame.Close -> {
                            // Handle close if necessary
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket error: ${e.localizedMessage}")
            } finally {
                if (Session.currentSession == this) {
                    Session.currentSession = null
                }
            }
        }
    }
}

class ISISEREvent(val msg: String, val typ: EventType? = EventType.GENERIC) : Event(){
    var message: String = msg ?: "No message"
    val type: EventType = typ!!
}
class InteractionEvent(val msg: String, val typ: EventType? = EventType.GENERIC) : Event(){
    var message: String = msg ?: "No message"
    val type: EventType = typ!!
}

class GUIEvent(initialData: Map<String, String>, val appStage: StagesEnum, val appSubject: String, val isGUIloaded: Boolean ) : Event() {
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