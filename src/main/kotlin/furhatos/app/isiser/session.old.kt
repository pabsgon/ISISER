// File: Session.kt
package furhatos.app.isiser
import furhatos.app.isiser.handlers.GUIEvent
import furhatos.app.isiser.setting.LOG_ALL_EVENTS
import furhatos.event.EventListener
import furhatos.event.EventSystem
import furhatos.event.actions.ActionGesture
import furhatos.event.monitors.MonitorSpeechStart
import furhatos.event.senses.SenseSpeech
import furhatos.records.Record
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.*

object SessionOld {
    private val loggerEventListener = EventListener { event ->
        if(LOG_ALL_EVENTS) {
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
        EventSystem.addEventListener(loggerEventListener)
    }
    private var guiInSynch = false
    private lateinit var fileHandler: FileHandler

    private val logger: Logger by lazy {
        val logDirectoryPath = "./logs"
        val logPath = Paths.get(logDirectoryPath)
        if (Files.notExists(logPath)) {
            Files.createDirectories(logPath) // Create the directory if it does not exist
        }

        val logger = Logger.getLogger("ISISER")
        // Define the file name based on the current date
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val formattedDateTime = currentDateTime.format(formatter)
        val fileName = "$logDirectoryPath/ISISER_$formattedDateTime.log" // Include the directory in the path

        // Define the file handler and formatter
        fileHandler = FileHandler(fileName)
        //val logFormatter = SimpleFormatter()

        // Set formatter to the file handler
        fileHandler.formatter = CustomLogFormatter()

        // Add the file handler to the logger
        logger.addHandler(fileHandler)

        println("[XXX]:" + logger.handlers.size)
        // Set log level (optional)
        logger.level = Level.INFO

        logger
    }

    fun startLogger() {
        // Logger initialization is already handled by lazy initialization
    }

    fun log(message: String) {
        if (logger.handlers.isEmpty()) {
            println("[XXX]:ADDING")
            logger.level = Level.INFO
            //println("[XXX]:" +logger.level)

            //This is weird, but I needed to add this to because when changing states, the
            // logger forgets the filehandler added in creation.
            logger.addHandler(fileHandler)
        }
        println("[XXX]:" + logger.handlers.size)
        //println("[***]:" + message)
        logger.info(message)
    }

    fun isGuiInSynch(): Boolean{
        return guiInSynch
    }
    fun setGuiInSynch(){
        guiInSynch = true
    }
}



class CustomLogFormatter : Formatter() {
    override fun format(record: LogRecord): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val formattedDateTime = currentDateTime.format(formatter)

        return "$formattedDateTime\t${record.message}\n"
    }
}
//class DEBUGGING(val message: String) : Event()