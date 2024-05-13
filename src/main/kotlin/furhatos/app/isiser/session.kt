// File: Session.kt
package furhatos.app.isiser
import furhatos.app.isiser.questions.Question
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

            is ISISEREvent -> logEntry = "\t${event.type}\t${event.message}\t${this.getStageName()}\t${this.getSubject()}"

            is InteractionEvent -> logEntry = "\t${event.type}\t${event.message}\t${this.getStageName()}\t${this.getSubject()}"

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
    var data: Data = Data()

    private var appStage: StagesEnum = StagesEnum.STAGE_0
    private var subject: String = UNDEFINED
    private var curAnswer = ""
    private var guiLoaded = false
    private lateinit var fileHandler: FileHandler
    private val statements: MutableList<Statement> = mutableListOf()
    private val questions: MutableList<Question> = mutableListOf()
    private val conditions: MutableMap<EnumConditions, List<EnumRobotMode>> = mutableMapOf()
    private val users: MutableMap<Int, EnumConditions> = mutableMapOf()


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
        //Subject should only be set when it's different of UNDEFINED (default) and
        // empty string (which maybe the value sent from the GUI)
        if(value != "" && value != subject ){
            subject = value
            if(value != UNDEFINED )activateCondition(subject)
        }

    }
    fun isSubjectUndefined(): Boolean = subject == UNDEFINED

    fun resetAnswer(){
        curAnswer = ""
    }
    fun createCondition(cond: EnumConditions, rmlist: List<EnumRobotMode>, unlist: List<Int>) {
        val enumList = rmlist.map { EnumRobotMode.valueOf(it.toString()) }
        conditions[cond] = enumList
        unlist.map {it }.forEach { key -> users[key] = cond }
    }
    fun activateCondition(s: String){
        try {
            // 1) Convert the parameter to an integer
            val userId = s.toInt()

            // 2) Use the map users to obtain the EnumCondition
            val userCondition = users[userId]
                ?: throw IllegalStateException("No condition found for user ID $userId")

            // 3) Get the list of EnumRobotMode from the map conditions
            val robotModes = conditions[userCondition]
                ?: throw IllegalStateException("No robot modes found for condition $userCondition")

            // 4) Iterate on the items of that list
            robotModes.forEachIndexed { index, enumRobotMode ->
                // Ensure the index is within the bounds of the questions list
                if (index < questions.size) {
                    // For each element e in position i, call questions[i].robotMode = e
                    questions[index].robotMode = enumRobotMode
                } else {
                    println("Warning: No question at index $index to update robotMode")
                }
            }
        } catch (e: NumberFormatException) {
            error("Error converting '$s' to an integer: ${e.message}")
        } catch (e: Exception) {
            error("Error during condition activation: ${e.message}")
        }
        printQuestions()
    }
    /* ------------------------- METHODS ------------------------------ */
    fun addQuestion(id: String, cAns: EnumAnswer, rAns: EnumAnswer ) {
        questions.add(Question(id,cAns,rAns))
    }
    fun registerQuestionAnswer(index: Int, ans: EnumAnswer) {
        questions[index].correctAnswer = ans
    }
    fun addStatement(qIndex: Int, id: String, type: EnumStatementTypes, subType: Boolean, stIndex: Int, perTriplet: Boolean,  vararg texts: String) {
        //qIndex is 0-based, must be
        val textTriplets = if (perTriplet) {
            mutableListOf<TextTriplet>()  // Initialize as an empty list if isIndexical is true
        } else {
            texts.map { TextTriplet(it) }.toMutableList()  // Map texts to TextTriplets if isIndexical is false
        }
        if(perTriplet) {
            for (i in texts.indices step 3) {
                val neutral = texts.getOrNull(i) ?: break
                val uncertain = texts.getOrNull(i + 1) ?: neutral
                val certain = texts.getOrNull(i + 2) ?: uncertain
                textTriplets.add(TextTriplet(neutral, uncertain, certain))
            }
        }
        if(type.equals(EnumStatementTypes.CLAIM)){
            addClaim(qIndex, id, subType, textTriplets)
        }else{
            val s = Statement(id, type, textTriplets, subType)
            statements.add(s)
            if (qIndex < 0) {

                // Call addStatement on all items in the list
                questions.forEach { it.setStatement(s, stIndex) }
            } else {
                // Call addStatement on the item at qIndex if it's within the bounds of the list
                questions.getOrNull(qIndex)?.setStatement(s, stIndex) ?: println("Error loading data: Question Index is out of bounds")
            }
        }
    }

    fun addClaim(qIndex: Int, id: String, subType: Boolean, textTriplets:  MutableList<TextTriplet> ) {

        if (qIndex < 0) {
            error("Error loading data: Question Index = [1.. $MAX_QUESTIONS]")
        } else {
            val s1 = Statement(id + UNFRIENDLY_SUFFIX, EnumStatementTypes.CLAIM, textTriplets.subList(0, 1), subType)
            val s2 = Statement(id + FRIENDLY_SUFFIX, EnumStatementTypes.CLAIM, textTriplets.subList(1, 2), subType)
            statements.add(s1)
            statements.add(s2)
            // Call addStatement on the item at qIndex if it's within the bounds of the list
            questions.getOrNull(qIndex)?.addClaim(Claim(id,s1,s2)) ?: println("Error loading data: Question Index is out of bounds")
        }
    }

    fun printStatements() {
        println("Printing all statements:")
        statements.forEach { println(it) }
    }
    fun printQuestions() {
        println("Printing all questions (${questions.size}):")
        questions.forEach { println(it) }
    }
    fun printConditions() {
        println("Printing all conditions (${conditions.size}):")
        conditions.forEach { println(it) }
    }
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

        println("----------------- LOADING DATA-------------------")

        loadSheetData()
        printStatements()
        printQuestions()
        printConditions()


        println("----------------- DATA LOADED -------------------")
    }

}
