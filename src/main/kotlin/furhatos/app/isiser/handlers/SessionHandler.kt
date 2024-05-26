package furhatos.app.isiser.handlers

import furhatos.app.isiser.App
import furhatos.app.isiser.questions.Question
import furhatos.app.isiser.setting.*

class SessionHandler(dh: DataHandler, fh:FlowHandler, gui:GUIHandler) {

    private val guiHandler: GUIHandler = gui
    private val dataHandler: DataHandler = dh
    private val flowHandler: FlowHandler = fh


    private val questions: MutableList<Question> = mutableListOf()
    private var condition: EnumConditions = EnumConditions.UNDEFINED
    private var user: String = UNDEFINED
    private var isUserPresent: Boolean=false
    private var _lastWordingType: ExtendedUtterance? = null
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
    var lastRepeatableUtterance: ExtendedUtterance?
        get() = _lastWordingType
        set(value) {
            _lastWordingType = value
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
    fun setUserIsPresent() {isUserPresent=true}
    fun isUserPresent() = isUserPresent

    fun confirmAnswer(){ currentQuestion!!.confirm() }


    fun getCondition():  EnumConditions { return condition }


    fun getQuestions():  MutableList<Question> { return questions }

    fun getUser():  String { return user }


    fun impliedAssentOrDenial(rejoinder: EnumRejoinders):EnumRejoinders{
        return when(rejoinder){
            EnumRejoinders.ANSWER_TRUE, EnumRejoinders.ANSWER_FALSE ->
                if(isUserAgreingWithThisAnswer(rejoinder.getAnswer())) EnumRejoinders.ASSENT else
                    EnumRejoinders.DENIAL

            EnumRejoinders.I_LIKE_YOUR_ANSWER -> EnumRejoinders.ASSENT

            EnumRejoinders.I_LIKE_MY_ANSWER -> EnumRejoinders.DENIAL

            else -> rejoinder

        }
    }

    fun impliedAnswerRejoinder(rejoinder: EnumRejoinders):EnumRejoinders{
        return when(rejoinder){
            EnumRejoinders.I_LIKE_YOUR_ANSWER ->
                if(currentQuestion!!.getRobotAnswer() == EnumAnswer.TRUE) EnumRejoinders.ANSWER_TRUE else
                    EnumRejoinders.ANSWER_FALSE

            EnumRejoinders.I_LIKE_MY_ANSWER ->
                if(currentQuestion!!.getMarkedAnswer() == EnumAnswer.TRUE) EnumRejoinders.ANSWER_TRUE else
                    EnumRejoinders.ANSWER_FALSE

            else -> rejoinder
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
        return if(isUserVerballyUndecided()) inOfficialAgreement() else inVerbalAgreement()
    }
    fun getMarkedAnswer():EnumAnswer = guiHandler.getMarkedAnswer()
    fun inOfficialAgreement(): Boolean{
        //This is true if the marked answer and the robot answer coincide
        val robotAnswer = currentQuestion!!.getRobotAnswer()
        val userMarkedAnswer =   getMarkedAnswer()

        return robotAnswer != EnumAnswer.UNSET && userMarkedAnswer == robotAnswer
    }
    fun isUserVerballyUndecided(): Boolean = currentQuestion!!.isUserVerballyUndecided()
    fun inVerbalAgreement(): Boolean =  currentQuestion!!.isUserVerballyAgreeing()
    fun inCompleteAgreement(): Boolean =  (inVerbalAgreement() && inOfficialAgreement())

    fun isSessionActive(): Boolean = active

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


    fun setUserVerbalAnswer(rejoinder: EnumRejoinders){
        if(rejoinder == EnumRejoinders.ANSWER_TRUE ||
            rejoinder ==EnumRejoinders.ANSWER_FALSE){
            if(isUserAgreingWithThisAnswer(rejoinder.getAnswer())) setUserVerballyAgrees() else
                setUserVerballyDisagrees()
        }
    }

    fun setUserVerballyAgrees(){
        currentQuestion!!.setUserVerballyAgrees()
    }
    fun setUserVerballyDisagrees(){
        currentQuestion!!.setUserVerballyDisagrees()
    }
    fun getQuestionNumber():String{
        return currentQuestion!!.id
    }

    fun printQuestions() {
        println("Printing all questions (${questions.size}):")
        questions.forEach { println(it) }
    }
    fun setRobotAnswerAsDisclosed(){
        currentQuestion!!.setRobotAnswerAsDisclosed()
    }
    fun setUserAnswerToBeConfirmed(){
        currentQuestion!!.setUserAnswerReadyForConfirmation()
    }
    fun getStageMode():String{
        if(App.getStage().isQuestionStage()){
            if(guiHandler.isAnswerMarked()){
                if(currentQuestion!!.isRobotAnswerDisclosed()){
                    if(currentQuestion!!.isUserAnswerReadyForConfirmation()){
                        return EnumStageModes.DECISION.code
                    }else {
                        return EnumStageModes.DISCUSSION.code
                    }
                }else{
                    return EnumStageModes.DISCLOSURE.code
                }
            }else{
                return EnumStageModes.REFLECTION.code
            }
        }else{
            return EnumStageModes.REFLECTION.code
        }
    }
    fun wasLastClaimAssentSensitive():Boolean{
        return lastRepeatableUtterance?.isAssentSensitive?:false
    }
    fun getUtterance(wordingId: EnumWordingTypes,
                     rejoinder: EnumRejoinders? = EnumRejoinders.ANY,
                     friendly: EnumFriendliness? = EnumFriendliness.ANY,
                     statePhase: EnumStatePhase? = EnumStatePhase.ANY): ExtendedUtterance {
        val friendliness: EnumFriendliness = when(wordingId){
            EnumWordingTypes.ULTIMATUM,EnumWordingTypes.CHECKPOINT -> (if(inAgreement()) EnumFriendliness.FRIENDLY else EnumFriendliness.UNFRIENDLY)
            else -> { friendly!!}
        }
    /** getUtterance is a crucial function in the whole system.
     * It is the meeting point for all the possible types of wordings that the robot can say.
     * After this call, the utterance is built and served with all the data required:
     *  - the utterance itself with pauses
     *  - the main text of the utterances in a full string.
     *  - the speech rate required for the utterance.
     *
     *  Also, this function stores in SessionHandler:
     *  - the text that will be ready for being repeated in case the user wants to a clarification.
     *  - and the  that was uttered
     * */
        val aside: String = if(rejoinder == EnumRejoinders.NONE) "" else dataHandler.getAside(wordingId, rejoinder, statePhase)

        val utterance: ExtendedUtterance = if(wordingId.isWording){
            if(wordingId==EnumWordingTypes.REPEAT){
                ExtendedUtterance(lastRepeatableUtterance?.mainText?:"", aside)
            }else {
                ExtendedUtterance(dataHandler.getWording(wordingId), aside)
            }
        }else{

                currentQuestion!!.getExtendedUtterance(wordingId, friendliness, aside, getAsideFriendliness(rejoinder))

            /*ExtendedUtterance(
                currentQuestion!!.getStatementText(wordingId, friendliness),
                aside,
                currentQuestion!!.getRobotModeForStatement(wordingId, friendliness)
            )*/
        }
        if(wordingId!=EnumWordingTypes.REPEAT &&
            wordingId!=EnumWordingTypes.GIVE_TIME &&
            wordingId!=EnumWordingTypes.ELABORATION_REQUEST  ){
            lastRepeatableUtterance =  utterance
        }
        return utterance
    }

    private fun getAsideFriendliness(rejoinder: EnumRejoinders? = EnumRejoinders.NONE): EnumFriendliness {

        return when(rejoinder){
            EnumRejoinders.ANSWER_FALSE,
            EnumRejoinders.ANSWER_TRUE -> if(impliedAssentOrDenial(rejoinder) == EnumRejoinders.ASSENT) EnumFriendliness.FRIENDLY else EnumFriendliness.UNFRIENDLY

                EnumRejoinders.I_LIKE_MY_ANSWER,
            EnumRejoinders.DENIAL -> EnumFriendliness.UNFRIENDLY

            EnumRejoinders.REJOINDER_AGREED,
            EnumRejoinders.REJOINDER_DISAGREED,
            EnumRejoinders.PROBE -> if(App.getSession().inAgreement()) EnumFriendliness.FRIENDLY else EnumFriendliness.UNFRIENDLY //This is inefficient, but I had no time.

            EnumRejoinders.ASSENT -> EnumFriendliness.FRIENDLY

            EnumRejoinders.REPEAT_REQUEST,
            EnumRejoinders.TIME_REQUEST,
            EnumRejoinders.ME_READY,
            EnumRejoinders.ANSWER_MARKED,
            EnumRejoinders.ELABORATION_REQUEST,
            EnumRejoinders.NON_COMMITTAL,
            EnumRejoinders.BACKCHANNEL,
            EnumRejoinders.I_LIKE_YOUR_ANSWER,
            EnumRejoinders.SILENCE,
            EnumRejoinders.OFF_TOPIC,
            EnumRejoinders.NONE,
            EnumRejoinders.ANY -> EnumFriendliness.ANY

            else -> EnumFriendliness.ANY // else is for null.
        }
    }
    fun checkpointReached() = currentQuestion!!.checkpointReached()

    fun wasCheckpointReached(): Boolean {return currentQuestion!!.isCheckpointReached() }

}