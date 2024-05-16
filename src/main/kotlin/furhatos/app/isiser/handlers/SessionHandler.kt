package furhatos.app.isiser.handlers

import furhatos.app.isiser.App
import furhatos.app.isiser.questions.Question
import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.FlowControlRunner

class SessionHandler(ef: EventFactory, dh: DataHandler, fh:FlowHandler) {

    private val evFactory: EventFactory = ef
    private val dataHandler: DataHandler = dh
    private val flowHandler: FlowHandler = fh


    private val questions: MutableList<Question> = mutableListOf()
    private var condition: EnumConditions = EnumConditions.UNDEFINED
    private var user: String = UNDEFINED
    private var currentQuestionId: Int? = null
    private var currentQuestion: Question? = null
    private var active = false



    fun handleEvent(event: SessionEvent) {
        when (event.type) {
            EventType.USER_SET -> {
                start(event.subject)
            }

            EventType.ANSWER_CONFIRMED -> {
                confirmAnswer()
            }

            EventType.QUESTION_SET -> {
                setQuestion(event.data["stage"] as? EnumStages)
            }

            else -> {} // Nothing to do

        }
    }

    fun isActive(): Boolean = active
    fun getUser():  String { return user }
    fun getCondition():  EnumConditions { return condition }
    fun getQuestions():  MutableList<Question> { return questions }
    fun start(newUser: String){
        //Subject should only be set when it's different of UNDEFINED (default) and
        // empty string (which maybe the value sent from the GUI)
        if(newUser != "" && newUser != user){
            user = newUser
            if(newUser != UNDEFINED ){
                condition = dataHandler.getConditionForUser(user)
                val robotModes: List<EnumRobotMode> = dataHandler.getRobotModesForCondition(condition)
                setupQuestions(robotModes)
                active = true
            }
        }
    }
    fun setQuestion(ind: Int?){
        currentQuestionId = ind
        currentQuestion = questions[currentQuestionId!!]
    }
    fun setQuestion(stage:EnumStages?){
        setQuestion(stage?.getQuestionIndex()) //This will return 0 if the stage is not a question stage.
        //println("XXXsetQuestion [${stage.toString()}] [${questions[currentQuestionId!!].disclosure?.getText(questions[0])} currentQuestionId:[$currentQuestionId]")
        //currentQuestion = questions[currentQuestionId!!]
    }
    fun confirmAnswer(){ currentQuestion!!.isConfirmed = true}

    fun questionIsConfirmed():Boolean = currentQuestion!!.isConfirmed


    fun getDisclosure():String{
        return currentQuestion!!.disclosure!!.getText(currentQuestion!!)
    }
    fun getMarkingRequest():String{
        return currentQuestion!!.markingRequest!!.getText(currentQuestion!!)
    }
    fun getReflection():String{
        return currentQuestion!!.reflection!!.getText(currentQuestion!!)
    }
    fun getElaborationRequest():String{
        return currentQuestion!!.elaborationRequest!!.getText(currentQuestion!!)
    }
    fun getQuestionNumber():String{
        return currentQuestion!!.id
    }
    private fun setupQuestions(robotModes: List<EnumRobotMode>) {
        robotModes.forEachIndexed { index, enumRobotMode ->
            // Ensure the index is within the bounds of the questions list
            if (index < questions.size) {
                // For each element e in position i, call questions[i].robotMode = e
                questions[index].robotMode = enumRobotMode
            } else {
                println("Warning: No question at index $index to update robotMode")
            }
        }
        printQuestions()
    }
    fun getStatement(id: String){

    }
    fun printQuestions() {
        println("Printing all questions (${questions.size}):")
        questions.forEach { println(it) }
    }
}