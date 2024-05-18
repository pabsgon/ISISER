package furhatos.app.isiser.handlers

import furhatos.app.isiser.App
import furhatos.app.isiser.questions.Question
import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.Utterance
import furhatos.flow.kotlin.utterance

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
    fun buildUtterance2(text: String, aside: String = ""): Utterance{
        //THIS NEEDS TO BE COMPLETED
        return createUtterance(aside + " " + text)
    }
    fun confirmAnswer(){ currentQuestion!!.confirm() }

    fun getCheckpoint(rejoinder: EnumRejoinders?):String{
        return buildUtterance(currentQuestion!!.getCheckpoint(inAgreement()))
    }
    fun getCondition():  EnumConditions { return condition }

    fun get1stConfirmationRequest(rejoinder: EnumRejoinders? = null):String{
        return buildUtterance(currentQuestion!!.get1stConfirmationRequest())
    }
    fun get2ndConfirmationRequest(rejoinder: EnumRejoinders? = null):String{
        return buildUtterance(currentQuestion!!.get2ndConfirmationRequest())
    }

    fun getDisclosure(rejoinder: EnumRejoinders?):String{
        return buildUtterance(currentQuestion!!.getDisclosure())
    }
    fun getElaborationRequest(rejoinder: EnumRejoinders? = null):String{
        return buildUtterance(currentQuestion!!.getElaborationRequest())
    }
    fun getFriendlyProbe(rejoinder: EnumRejoinders?):String{
        return buildUtterance(currentQuestion!!.getFriendlyProbe())
    }
    fun getFriendlyClaim(rejoinder: EnumRejoinders?):String{
        return buildUtterance(currentQuestion!!.getFriendlyClaim())
    }
    fun getMarkingRequest(rejoinder: EnumRejoinders?):String{
        return buildUtterance(currentQuestion!!.getMarkingRequest())
    }

    fun getReflection(rejoinder: EnumRejoinders? = null):String{
        return buildUtterance(currentQuestion!!.getReflection())
    }
    fun getQuestions():  MutableList<Question> { return questions }

    fun getUltimatum(rejoinder: EnumRejoinders?):String{
        return buildUtterance(currentQuestion!!.getUltimatum(inAgreement()))
    }
    fun getUnfriendlyClaim(rejoinder: EnumRejoinders?):String{
        return buildUtterance(currentQuestion!!.getUnfriendlyClaim())
    }
    fun getUnfriendlyProbe(rejoinder: EnumRejoinders?):String{
        return buildUtterance(currentQuestion!!.getUnfriendlyProbe())
    }
    fun getUser():  String { return user }

    fun getWording(state: EnumStates, i: Int): Utterance{
        return buildUtterance2(dataHandler.getWording(state,i))
    }

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
        return if(neverAskedInCheckpoint()) inOfficialAgreement() else inVerbalAgreement()
    }
    fun inOfficialAgreement(): Boolean{
        //This is true if the marked answer and the robot answer coincide
        val robotAnswer = currentQuestion!!.getRobotAnswer()
        val userMarkedAnswer =   guiHandler.getMarkedAnswer()

        return robotAnswer != EnumAnswer.UNDEFINED && userMarkedAnswer == robotAnswer
    }
    fun neverAskedInCheckpoint(): Boolean = currentQuestion!!.isUserVerballyUndecided()
    fun inVerbalAgreement(): Boolean =  currentQuestion!!.isUserVerballyAgreeing()

    fun inCompleteAgreement(): Boolean =  (inVerbalAgreement() && inOfficialAgreement())

    fun isActive(): Boolean = active

    fun maxNumOfFriendlyProbesReached():Boolean = currentQuestion!!.maxNumOfFriendlyProbesReached()
    fun maxNumOfUnfriendlyProbesReached():Boolean = currentQuestion!!.maxNumOfUnfriendlyProbesReached()



    fun setQuestion(ind: Int?){
        currentQuestionId = ind
        currentQuestion = questions[currentQuestionId!!]
    }
    fun setQuestion(stage:EnumStages?){
        setQuestion(stage?.getQuestionIndex()) //This will return 0 if the stage is not a question stage.
    }
    fun setRobotFinalAnswer(enumAnswer: EnumAnswer){
        currentQuestion!!.setRobotAnswer(enumAnswer)
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

    fun createUtterance(s1: String): Utterance {
        val currentStateIsTrimode: Boolean = App.isCurrentStateTriMode()
        val robotMode = if (currentStateIsTrimode) currentQuestion!!.getRobotMode() else EnumRobotMode.NEUTRAL
        val speechRate = robotMode.speechRate

        val processedString = if (currentStateIsTrimode) {
            s1.replace(SOURCEDATA_CODE_QNUM, getQuestionNumber())
                .replace(SOURCEDATA_CODE_ROBOT_ANSWER, currentQuestion!!.getRobotAnswer().toString())
                .replace(SOURCEDATA_CODE_USER_ANSWER, guiHandler.getMarkedAnswer().toString())
        } else {
            s1.replace(SOURCEDATA_CODE_QNUM, "SAMPLE 4")
                .replace(SOURCEDATA_CODE_ROBOT_ANSWER, "SAMPLE TRUE")
                .replace(SOURCEDATA_CODE_USER_ANSWER, "SAMPLE FALSE")
        }

        return utterance {
            //+rate(speechRate)
            var currentText = StringBuilder()
            var i = 0
            while (i < processedString.length) {
                when {
                    processedString[i] == '.' -> {
                        var dotCount = 0
                        while (i < processedString.length && processedString[i] == '.') {
                            dotCount++
                            i++
                        }
                        if (dotCount > 1) {
                            if (currentText.isNotEmpty()) {
                                +currentText.toString().trim()
                                currentText = StringBuilder()
                            }
                            +delay(250 * dotCount)
                        }
                    }
                    else -> {
                        currentText.append(processedString[i])
                        i++
                    }
                }
            }
            if (currentText.isNotEmpty()) {
                +currentText.toString().trim()
            }
        }
    }

}