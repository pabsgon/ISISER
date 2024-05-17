package furhatos.app.isiser.flow.main

import furhat.libraries.standard.NluLib
import furhatos.app.isiser.App
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.handlers.GUIEvent
import furhatos.app.isiser.handlers.GUIHandler
import furhatos.app.isiser.handlers.SessionHandler
import furhatos.app.isiser.nlu.*
import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.*
import furhatos.nlu.Intent
import furhatos.nlu.common.*


val QuestionReflection = state(parent = Parent) {
    val session: SessionHandler = App.getSession()
    val gui: GUIHandler = App.getGUI()
    var userDidntSpeak = true
    var userSaidMarkedIt = false
    var robotAskedIfMarked = false //This will be true the second time the robot speaks (the first time is the very first utterance)

/*
    fun Furhat.doAsk(u: Utterance){
        if(!robotAskedIfMarked)robotAskedIfMarked=true
        this.say(u)
    }




    fun Furhat.doAsk(s: String) {
        this.doAsk( utterance{s })
    }*/
    fun Furhat.askMarkingRequest() {
        robotAskedIfMarked = true
        this.ask( session.getMarkingRequest() )
    }
    onEntry {
        //App.printState(thisState)
        println("XXXonEntry")
        furhat.ask(session.getReflection())
    }
    onReentry {
        //App.printState(thisState,"R")//
        println("XXXonReentry")
        furhat.ask(session.getReflection())
    }


    onResponse({listOf(
        NluLib.IAmDone(),
        AnswerFalse(),
        AnswerTrue(),
        AnswerMarked(),
        RejoinderAgreed(),
        RejoinderDisagreed(),
        No(),
        Disagree(),
        Probe(),
        ElaborationRequest(),
        Maybe(),
        DontKnow(),
        Backchannel(),
        Agree(),Yes()
    )}){
        //furhat.ask(it.intent.toString())
        raise(AllIntents(it.intent as Intent))
    }

    onResponse<AllIntents>{
        val rejoinder: EnumRejoinders = it.intent.rejoinder
        furhat.say(rejoinder.toString())
        userDidntSpeak = false
        if(!userSaidMarkedIt && (rejoinder.equals(EnumRejoinders.ANSWER_MARKED) ||
                    rejoinder.equals(EnumRejoinders.ASSENT)) )userSaidMarkedIt = true
        if(userSaidMarkedIt){//We are assuming they JUST SAID they marked it now.
            if(robotAskedIfMarked){// THis "means" that the robot did answer
                if(gui.isAnswerMarked()){
                    App.goto(QuestionDisclosure(rejoinder))
                }else{
                    furhat.askMarkingRequest()
                    //furhat.ask(session.getMarkingRequest())
                }
            }else{// This "means" that the the robot did NOT answer
                if(gui.isAnswerMarked()){
                    App.goto(QuestionDisclosure(rejoinder))
                }else{
                    furhat.askMarkingRequest()
                    //furhat.ask(session.getMarkingRequest())
                }
            }
        }else{ //The user never said they marked it
            furhat.askMarkingRequest()
            //furhat.ask(session.getMarkingRequest())
        }
    }
    onNoResponse{
        if(userDidntSpeak){
            this.reentry()
        }else{
            raise(AllIntents(EnumRejoinders.SILENCE))
        }
    }
    /*
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            propagate()
        }
    }

     */
}
fun QuestionDisclosure(lastRejoinderType: EnumRejoinders? = null) = state(parent = Parent) {
    val session: SessionHandler = App.getSession()


    onEntry {
        //App.printState(thisState)
        println("XXXDisclosure")
        furhat.ask(session.getDisclosure())
    }
    onReentry {
        //this should never happen, but just in case:
        furhat.ask(session.getDisclosure())
    }
    /*Intents discarded:
     I_AM_DONE*
     RejoinderDisagreed
     AnswerMarked

     */

    onResponse({listOf(
        AnswerFalse(),
        AnswerTrue(),
        RejoinderAgreed(),
        RejoinderDisagreed(),
        No(),
        Disagree(),
        Probe(),
        ElaborationRequest(),
        Maybe(),
        DontKnow(),
        Backchannel(),
        Agree(),Yes()
    )}){
        raise(AllIntents(it.intent as Intent))
    }

    onResponse<AllIntents> {
        var rejoinder: EnumRejoinders = it.intent.rejoinder
        rejoinder = session.impliedRejoinder(rejoinder)
        when(rejoinder){

            EnumRejoinders.REJOINDER_AGREED, EnumRejoinders.REJOINDER_DISAGREED,
            EnumRejoinders.PROBE, EnumRejoinders.ELABORATION_REQUEST, EnumRejoinders.NON_COMMITTAL,
            EnumRejoinders.BACKCHANNEL -> {
                if(session.inOfficialAgreement()){
                    // This means that the robot answer and the MARKED answer coincide
                    App.goto(QuestionReview())
                }else{
                    App.goto(QuestionPersuasion())
                }
            }

            EnumRejoinders.DENIAL -> {
                App.goto(QuestionPersuasion())
            }

            EnumRejoinders.ASSENT -> {
                if(session.inOfficialAgreement()){
                    // This means that the robot answer and the MARKED answer coincide
                    App.goto(QuestionReview())
                }else{
                    App.goto(QuestionCheckpoint())
                }
            }
            else -> {
                /* The rest of possible values should not occur for different reasons:
                EnumRejoinders.SILENCE -> {/*Handled in onNoResponse*/}
                EnumRejoinders.TIME_REQUEST, EnumRejoinders.OFF_TOPIC,
                EnumRejoinders.REPEAT_REQUEST -> {/*Handled in parent state*/}
                EnumRejoinders.I_AM_DONE, EnumRejoinders.ANSWER_MARKED ->  {/*Not captured in onResponse above*/}
                EnumRejoinders.ANSWER_TRUE,
                EnumRejoinders.ANSWER_FALSE They are converted to ASSENT or DENIAL by calling impliedRejoinder

                Therefore tHis should never happen, but just in case:
                 */

                furhat.ask("Uhm")
            }

        }


    }

    onNoResponse{
        if(session.inOfficialAgreement()){
            // This means that the robot answer and the MARKED answer coincide
            App.goto(QuestionCheckpoint(EnumRejoinders.SILENCE))
        }else{
            App.goto(QuestionPersuasion())
        }
    }
    /*
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            propagate()
        }
    }

     */
}
fun QuestionPersuasion(lastRejoinderType: EnumRejoinders? = null) = state(parent = Parent) {
    val session: SessionHandler = App.getSession()

    fun Furhat.sayUnfriendlyClaimOrGetOut(lastRejoinderType: EnumRejoinders? = null){
        if(session.thereAreUnfriendlyClaims()) {
            this.ask(session.getUnfriendlyClaim())
        }else{
            if(session.inVerbalAgreement()){
                //If they are in verbal agreement means that they went through CHECKPOINT already and agreed there
                App.goto(QuestionUltimatum(lastRejoinderType))
            }else{
                App.goto(QuestionCheckpoint(lastRejoinderType))
            }
        }
    }

    fun Furhat.gotoCheckPointOrReview(){
        if(session.inVerbalAgreement()){
            App.goto(QuestionCheckpoint())
        }else{
            App.goto(QuestionReview())
        }
    }
    onEntry {
        // When entering this state, the student and robot should be in disagreement. There's a function
        // that calculates this based on the user's verbal and marked answers and the robot's answer.
        // Based on this, the right claim (friendly or unfriendly) could be extracted based on this info.
        // However, for security, once in this state the type of claim will be asked explicitly.
        //App.printState(thisState)
        // ADD A WARNING LINE: if(session.inAgreement()) warn "Both in agreement in persuasion state is unexpected"
        furhat.say("Persuasion!")
        furhat.sayUnfriendlyClaimOrGetOut(lastRejoinderType)

    }
    onReentry {
        //App.printState(thisState,"R")//
        /*if(session.thereAreUnfriendlyClaims()) {
            furhat.ask(session.getUnfriendlyClaim())
        }else{
            App.goto(QuestionUltimatum())
        }*/
    }

    onResponse({listOf(
        NluLib.IAmDone(),
        AnswerFalse(),
        AnswerTrue(),
        AnswerMarked(),
        RejoinderAgreed(),
        RejoinderDisagreed(),
        No(),
        Disagree(),
        Probe(),
        ElaborationRequest(),
        Maybe(),
        DontKnow(),
        Backchannel(),
        Agree(),Yes()
    )}){
        raise(AllIntents(it.intent as Intent))
    }
    onResponse<AllIntents> {
        var rejoinder: EnumRejoinders = it.intent.rejoinder
        rejoinder = session.impliedRejoinder(rejoinder) //This will convert ANSWER_TRUE or ANSWER_FALSE into ASSENT or DENIAL, when proceeds.
        when(rejoinder){

            EnumRejoinders.REJOINDER_AGREED, EnumRejoinders.REJOINDER_DISAGREED,
            EnumRejoinders.PROBE, EnumRejoinders.ELABORATION_REQUEST,
            EnumRejoinders.NON_COMMITTAL, EnumRejoinders.BACKCHANNEL -> furhat.sayUnfriendlyClaimOrGetOut(rejoinder)

            EnumRejoinders.DENIAL -> {
                session.userVerballyDisagrees()
                if(USE_PROBES_AT_DISCUSSION && !session.maxNumOfUnfriendlyProbesReached()){
                    furhat.ask(session.getUnfriendlyProbe())
                }else{
                    furhat.sayUnfriendlyClaimOrGetOut(lastRejoinderType)
                }

            }

            EnumRejoinders.I_AM_DONE, EnumRejoinders.ASSENT -> furhat.gotoCheckPointOrReview()

            else -> { // This should not happen, but just in case:
                furhat.ask("Uhm")
            }
        }

    }
    onNoResponse{
        furhat.sayUnfriendlyClaimOrGetOut(EnumRejoinders.SILENCE)
    }
    /*
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            propagate()
        }
    }
     */
}
fun QuestionReview(lastRejoinderType: EnumRejoinders? = null) = state(parent = Parent) {
    val session: SessionHandler = App.getSession()
    val gui: GUIHandler = App.getGUI()
    // If this state has been reached, is because they agree.
    // We're assuming that there's no turning back to disagreement.

    fun Furhat.sayFriendlyClaimOrGetOut(lastRejoinder: EnumRejoinders? = null){
        if(session.thereAreFriendlyClaims()) {
            this.ask(session.getFriendlyClaim())
        }else{
            if(session.inVerbalAgreement()){
                //If they are in verbal agreement means that they went through CHECKPOINT already and agreed there
                App.goto(QuestionUltimatum(lastRejoinderType))
            }else{
                App.goto(QuestionCheckpoint(lastRejoinderType))
            }
        }
    }
    onEntry {
        //App.printState(thisState)
        furhat.say("Review!")
        furhat.sayFriendlyClaimOrGetOut(lastRejoinderType)
    }
    onReentry() {
        //App.printState(thisState,"R")//
    }
    onResponse({listOf(
        NluLib.IAmDone(),
        AnswerFalse(),
        AnswerTrue(),
        AnswerMarked(),
        RejoinderAgreed(),
        RejoinderDisagreed(),
        No(),
        Disagree(),
        Probe(),
        ElaborationRequest(),
        Maybe(),
        DontKnow(),
        Backchannel(),
        Agree(),Yes()
    )}){
        raise(AllIntents(it.intent as Intent))
    }

    onResponse<AllIntents> {
        var rejoinder: EnumRejoinders = it.intent.rejoinder
        rejoinder = session.impliedRejoinder(rejoinder) //This will convert ANSWER_TRUE or ANSWER_FALSE into ASSENT or DENIAL, when proceeds.
        when(rejoinder){



            EnumRejoinders.DENIAL -> {
                session.userVerballyDisagrees()
                if(USE_PROBES_AT_DISCUSSION && !session.maxNumOfUnfriendlyProbesReached()){
                    furhat.ask(session.getFriendlyProbe())
                }else{
                    furhat.sayFriendlyClaimOrGetOut(rejoinder)
                }

            }

            EnumRejoinders.I_AM_DONE, EnumRejoinders.ASSENT,
            EnumRejoinders.REJOINDER_AGREED, EnumRejoinders.REJOINDER_DISAGREED,
            EnumRejoinders.PROBE, EnumRejoinders.ELABORATION_REQUEST,
            EnumRejoinders.NON_COMMITTAL, EnumRejoinders.BACKCHANNEL -> furhat.sayFriendlyClaimOrGetOut(rejoinder)

            else -> { // This should not happen, but just in case:
                furhat.ask("Uhm")
            }
        }


    }

    onNoResponse{
        reentry()
    }
    /*
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            propagate()
        }
    }

     */
}
fun QuestionCheckpoint(lastRejoinderType: EnumRejoinders? = null, beFriendly: Boolean? = null) = state(parent = Parent) {
/*    STATE ENTRY: This state is called at several moments. The checkpoints statement should be added in order of clarity.
This is so because the state will call itself, and will repeat exactly the same logic. The only difference should be the
utterance. Since texts are designed to be extracted from a list and moved to the end, utterances should be different every
 time, if several texts were added. In all cases, the expected answer must be true/false, with TRUE meaning that the user
 aligns with the robot.
The main difference between Checkpoint and ultimatum is that 1) Ultimatum is never friendly (it was reached with
disagreement) and 2) there's no coming back.

This state determines if the user is in verbal agreement or not. userVerballyAgrees is set here to TRUE or FALSE.
There are some implications in REVIEW.


    if beFriendly or inOfficialAgreement (friendly)
    we agree with #ROBOT_ANSWER# as we discussed?
    shall we go with #ROBOT_ANSWER# as we suggest?

    if not befriendly Not inOfficialAgreement (unfriendly)
    you agree with #ROBOT_ANSWER# as I suggest?
    shall we go with #ROBOT_ANSWER# as I suggest?
*/
    val session: SessionHandler = App.getSession()
    val gui: GUIHandler = App.getGUI()
    val friendly = beFriendly ?: session.inOfficialAgreement()

    onEntry {
        //App.printState(thisState)
        println("XXXDisclosure")
        furhat.say("Checkpoint!")
        furhat.ask(session.getCheckpoint())
    }
    onReentry {
        //App.printState(thisState,"R")//
        //println("XXXonReentry")
        //furhat.ask(session.get())
    }

    onResponse({listOf(
        NluLib.IAmDone(),
        AnswerFalse(),
        AnswerTrue(),
        AnswerMarked(),
        RejoinderAgreed(),
        RejoinderDisagreed(),
        No(),
        Disagree(),
        Probe(),
        ElaborationRequest(),
        Maybe(),
        DontKnow(),
        Backchannel(),
        Agree(),Yes()
    )}){
        raise(AllIntents(it.intent as Intent))
    }

    onResponse<AllIntents> {
        println("XXXX>Case1")
        var rejoinder: EnumRejoinders = it.intent.rejoinder

        if (rejoinder.equals(EnumRejoinders.ANSWER_MARKED) ||
            rejoinder.equals(EnumRejoinders.ASSENT) ){
            //App.goto(QuestionReview())
        } else{
            //App.goto(QuestionPersuasion())
        }
    }

    onNoResponse{
        //App.goto(QuestionCheckpoint(EnumSubjectRejoinders.SILENCE))
    }
    /*
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            propagate()
        }
    }

     */
}
fun QuestionUltimatum(lastRejoinderType: EnumRejoinders? = null) = state(parent = Parent) {
    val session: SessionHandler = App.getSession()
    val gui: GUIHandler = App.getGUI()


    onEntry {
        //App.printState(thisState)
        println("XXXDisclosure")
        furhat.ask(session.getUltimatum())
    }
    onReentry {
        //App.printState(thisState,"R")//
        //println("XXXonReentry")
        //furhat.ask(session.get())
    }

    onResponse({listOf(
        NluLib.IAmDone(),
        AnswerFalse(),
        AnswerTrue(),
        AnswerMarked(),
        RejoinderAgreed(),
        RejoinderDisagreed(),
        No(),
        Disagree(),
        Probe(),
        ElaborationRequest(),
        Maybe(),
        DontKnow(),
        Backchannel(),
        Agree(),Yes()
    )}){
        raise(AllIntents(it.intent as Intent))
    }

    onResponse<AllIntents> {
        println("XXXX>Case1")
        var rejoinder: EnumRejoinders = it.intent.rejoinder

        if (rejoinder.equals(EnumRejoinders.ANSWER_MARKED) ||
            rejoinder.equals(EnumRejoinders.ASSENT) ){
            //App.goto(QuestionReview())
        } else{
            //App.goto(QuestionPersuasion())
        }
    }

    onNoResponse{
        //App.goto(QuestionCheckpoint(EnumSubjectRejoinders.SILENCE))
    }
    /*
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            propagate()
        }
    }

     */
}

val QuestionReview2 = state(parent = Parent) {
    onEntry {
        App.printState(thisState)
        furhat.ask() {
            +delay(1000)
            random {
                +"It's false"
                +"This is definitely false"
                +"I am not entirely sure but I'd say false "
                +"It's true"
                +"This is definitely true"
                +"I am not entirely sure but I'd say true "
            }
        }
    }


    onReentry {
        App.printState(thisState, "R")
        furhat.ask("So what do you think?")
    }
    onResponse<AnswerFalse> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<AnswerTrue> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<Agree> {
        furhat.ask("Of course you agree. Mark the answer and confirm")
    }
    onResponse<Disagree> {
        furhat.ask("I don't care. Mark the answer I said and confirm")
    }
    onResponseFailed {
        furhat.say("I think my connection broke. Did you say something?")
    }
    onResponse{
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onNoResponse{
        reentry()
    }
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_MARKED){
            reentry()
        }else{
            furhat.say("Good boy. You confirmed")
            propagate()
        }
    }
}

fun QuestionDisclosure2(aside: Utterance? = null) = state(parent = Parent) {
    var session: SessionHandler = App.getSession()
    onEntry {
        App.printState(thisState)
        println("XXXQuestionDisclosure ${session.getDisclosure()}")
        furhat.ask( {
            +delay(1000)
            +session.getDisclosure()
        })
        /*random {
            +"It's false"
            +"This is definitely false"
            +"I am not entirely sure but I'd say false "
            +"It's true"
            +"This is definitely true"
            +"I am not entirely sure but I'd say true "
        }

         */
    }


    onReentry {
        App.printState(thisState, "R")
        furhat.ask("So what do you think?")
    }
    onResponse<AnswerFalse> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<AnswerTrue> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<Agree> {
        furhat.ask("Of course you agree. Mark the answer and confirm")
    }
    onResponse<Disagree> {
        furhat.ask("I don't care. Mark the answer I said and confirm")
    }
    onResponseFailed {
        furhat.say("I think my connection broke. Did you say something?")
    }
    onResponse{
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onNoResponse{
        reentry()
    }
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_MARKED){
            reentry()
        }else{
            furhat.say("Good boy. You confirmed")
            propagate()
        }
    }
}
