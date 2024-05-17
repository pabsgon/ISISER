package furhatos.app.isiser.handlers

import furhatos.app.isiser.questions.Question
import furhatos.app.isiser.setting.*

class SessionHandler(dh: DataHandler, fh:FlowHandler, gui:GUIHandler) {

    private val guiHandler: GUIHandler = gui
    private val dataHandler: DataHandler = dh
    private val flowHandler: FlowHandler = fh


    private val questions: MutableList<Question> = mutableListOf()
    private var condition: EnumConditions = EnumConditions.UNDEFINED
    private var user: String = UNDEFINED
    private var _lastRobotText: String = ""
    private var currentQuestionId: Int? = null
    private var currentQuestion: Question? = null
    private var active = false

    /* PRIVATE */
    private fun setupQuestions(robotModes: List<EnumRobotMode>) {
        robotModes.forEachIndexed { index, enumRobotMode ->
            // Ensure the index is within the bounds of the questions list
            if (index < questions.size) {
                // For each element e in position i, call questions[i].robotMode = e
                questions[index].setRobotMode(enumRobotMode)
            } else {
                println("Warning: No question at index $index to update robotMode")
            }
        }
        printQuestions()
    }
    /* PUBLIC */
    var lastRobotText: String
        get() = _lastRobotText
        set(value) {
            _lastRobotText = value
        }
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


    fun buildUtterance(text: String, aside: String? = ""): String{
        //THIS NEEDS TO BE COMPLETED
        lastRobotText = text
        return text
    }
    fun confirmAnswer(){ TODO() }


    fun getCheckpoint():String{
        return buildUtterance(currentQuestion!!.getCheckpoint())
    }
    fun getCondition():  EnumConditions { return condition }
    fun getDisclosure():String{
        return buildUtterance(currentQuestion!!.getDisclosure())
    }
    fun getElaborationRequest():String{
        return buildUtterance(currentQuestion!!.getElaborationRequest())
    }
    fun getFriendlyProbe():String{
        return buildUtterance(currentQuestion!!.getFriendlyProbe())
    }
    fun getFriendlyClaim():String{
        return buildUtterance(currentQuestion!!.getFriendlyClaim())
    }
    fun getMarkingRequest():String{
        return buildUtterance(currentQuestion!!.getMarkingRequest())
    }

    fun getReflection():String{
        return buildUtterance(currentQuestion!!.getReflection())
    }
    fun getQuestions():  MutableList<Question> { return questions }

    fun getUltimatum():String{
        return buildUtterance(currentQuestion!!.getUltimatum())
    }
    fun getUnfriendlyClaim():String{
        return buildUtterance(currentQuestion!!.getUnfriendlyClaim())
    }
    fun getUnfriendlyProbe():String{
        return buildUtterance(currentQuestion!!.getUnfriendlyProbe())
    }
    fun getUser():  String { return user }


    fun impliedRejoinder(rejoinder: EnumRejoinders):EnumRejoinders{
        if(rejoinder == EnumRejoinders.ANSWER_TRUE ||
            rejoinder ==EnumRejoinders.ANSWER_FALSE){
            return if(isUserAgreingWithThisAnswer(rejoinder.getAnswer())) EnumRejoinders.ASSENT else
                EnumRejoinders.DENIAL
        }else{
            return rejoinder
        }
    }

    fun inAgreement(): Boolean{
        /* Some behaviours of the robot and the flow depend on the user answer. There are two pieces of information that
        inform this:
        1) The answer marked on the GUI
        2) The verbal answer inferred by the system at some situations.
        Whereas the first one is unequivocal, the second is less reliable, because depends on the intent having been captured
         correctly so it means what it seems to mean.

        This in turn affects the function inAgreement. There's a related 100% reliable function called inOfficialAgreement,
        which is true when the marked answer and the robot answer coincides. inAgreement is true when the verbal answer and
         the robot answer coincides. If they do not coincide, then it inherits the value of inOfficialAgreement.
        */
        /*
        * The robot will always have an answer defined, except at the onset of the program, during dataloading.
        *
        * The priority to check if they are in agreement is if the verbal answer. If this has been determined and
        * set, then that becomes the reference over the marked answer.
        *
        * */
        return if(inVerbalAgreement()) true else inOfficialAgreement()
    }
    fun inOfficialAgreement(): Boolean{
        //This is true if the marked answer and the robot answer coincide
        val robotAnswer = currentQuestion!!.getRobotAnswer()
        val userMarkedAnswer =   guiHandler.getMarkedAnswer()

        return robotAnswer != EnumAnswer.UNDEFINED && userMarkedAnswer == robotAnswer
    }

    fun inVerbalAgreement(): Boolean =  currentQuestion!!.isUserVerballyAgreeing()


    fun isActive(): Boolean = active

    fun maxNumOfFriendlyProbesReached():Boolean = currentQuestion!!.maxNumOfFriendlyProbesReached()
    fun maxNumOfUnfriendlyProbesReached():Boolean = currentQuestion!!.maxNumOfUnfriendlyProbesReached()



    fun setQuestion(ind: Int?){
        currentQuestionId = ind
        currentQuestion = questions[currentQuestionId!!]
    }
    fun setQuestion(stage:EnumStages?){
        setQuestion(stage?.getQuestionIndex()) //This will return 0 if the stage is not a question stage.
        //println("XXXsetQuestion [${stage.toString()}] [${questions[currentQuestionId!!].disclosure?.getText(questions[0])} currentQuestionId:[$currentQuestionId]")
        //currentQuestion = questions[currentQuestionId!!]
    }
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
    fun thereAreFriendlyClaims():Boolean{
        return currentQuestion!!.thereAreFriendlyClaims()
    }
    fun thereAreUnfriendlyClaims():Boolean{
        return currentQuestion!!.thereAreUnfriendlyClaims()
    }
    fun isUserAgreingWithThisAnswer(answer: EnumAnswer): Boolean{
        //This will set return true if the answer given coincides with the robot's
        return currentQuestion!!.getRobotAnswer() == answer
    }
    fun userVerballyAgrees(){
        currentQuestion!!.userVerballyAgrees()
    }
    fun userVerballyDisagrees(){
        currentQuestion!!.userVerballyDisagrees()
    }
    fun getQuestionNumber():String{
        return currentQuestion!!.id
    }

    fun printQuestions() {
        println("Printing all questions (${questions.size}):")
        questions.forEach { println(it) }
    }
}