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
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
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
            println("Routing indeed...")
            resources("gui")
            intercept(ApplicationCallPipeline.Call) {
                if (call.request.uri == "/") {
                    if(Session.isGUILoaded()){
                        EventSystem.send(GUIEvent(GUI_RELOADED))
                    }else{
                        Session.setGUILoaded()
                        EventSystem.send(GUIEvent(GUI_STARTED))
                    }
                }
            }
        }
        post("/receive") {
            val data = call.receive<Map<String, String>>()
            val message = data["message"] ?: "No message"
            val stage = data["stage"] ?: Session.getStage()
            val answer = data["answer"] ?: ""
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

            val statusStr = if (guiLoaded) " [ACCEPTED]" else " [REJECTED]"
            val status = if (guiLoaded) 0 else 1
            val statusCode: HttpStatusCode = if (guiLoaded) HttpStatusCode.OK else HttpStatusCode.Forbidden
            if(guiLoaded){
                Session.setStage(stage)
                Session.setAnswer(answer)
            }

            call.respond(statusCode, mapOf("status" to status,
                "received" to message,
                "stage" to stage))  // Ensure this is a proper JSON response

            val event = GUIEvent(message + statusStr)
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

class GUIEvent(val message: String) : Event()