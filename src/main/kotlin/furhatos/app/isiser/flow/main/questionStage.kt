package furhatos.app.isiser.flow.main

import com.sun.tracing.Probe
import furhat.libraries.standard.NluLib
import furhatos.app.isiser.App
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.handlers.GUIEvent
import furhatos.app.isiser.handlers.GUIHandler
import furhatos.app.isiser.handlers.SessionHandler
import furhatos.app.isiser.nlu.*
import furhatos.app.isiser.setting.EnumSubjectRejoinders
import furhatos.app.isiser.setting.EventType
import furhatos.flow.kotlin.*
import furhatos.nlu.Intent
import furhatos.nlu.common.*


val QuestionReflection = state(parent = Parent) {
    val session: SessionHandler = App.getSession()
    val gui: GUIHandler = App.getGUI()
    var userDidntSpeak = true
    var userSaidMarkedIt = false
    var robotAskedIfMarked = false //This will be true the second time the robot speaks (the first time is the very first utterance)


    fun Furhat.doAsk(u: Utterance){
        if(!robotAskedIfMarked)robotAskedIfMarked=true
        this.say(u)
    }

    fun Furhat.askMarkingRequest(s: String) {
        val u = utterance {
            + s
            + delay(1000)
            + session.getMarkingRequest()
        }
        this.doAsk( u )
    }


    fun Furhat.doAsk(s: String) {
        this.doAsk( utterance{s })
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
    /*
    onResponse<NluLib.IAmDone> {raise(Case1(EnumSubjectRejoinders.I_AM_DONE))}
    onResponse<AnswerFalse> {raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE))  }
    onResponse<AnswerTrue> {raise(Case1(EnumSubjectRejoinders.ANSWER_TRUE)) }
    onResponse<AnswerMarked> {raise(Case1(EnumSubjectRejoinders.ANSWER_MARKED))    }

    onResponse<No> {raise(Case1(it.intent))   }
    onResponse<Disagree> { raise(Case1(it.intent))}
    onResponse<DontKnow> {  raise(Case1(EnumSubjectRejoinders.NON_COMMITTAL)  )}
    onResponse<RequestRepeat> { raise(Case1(it.intent)) }
    onResponse<RejoinderAgreed> {  raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)  ) }
    onResponse<RejoinderDisagreed> {   raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE))  }
    onResponse<ElaborationRequest> {  raise(Case1(EnumSubjectRejoinders.ELABORATION_REQUEST))  }
    onResponse<Backchannel> {  raise(Case1(EnumSubjectRejoinders.BACKCHANNEL) )  }
    onResponse<Yes> {raise(Case1(EnumSubjectRejoinders.ASSENT)) }
    onResponse<Agree> { raise(Case1(EnumSubjectRejoinders.ASSENT))}

    onResponse({
        it.intent is NluLib.IAmDone ||
                it.intent is AnswerFalse ||
                it.intent is AnswerTrue ||
                it.intent is AnswerMarked ||
                it.intent is RejoinderAgreed ||
                it.intent is RejoinderDisagreed ||
                it.intent is No ||
                it.intent is Disagree ||
                it.intent is Probe ||
                it.intent is ElaborationRequest ||
                it.intent is Maybe ||
                it.intent is DontKnow ||
                it.intent is Backchannel ||
                it.intent is Agree ||
                it.intent is Yes
    }) {
        raise(Case1(it.intent as Intent))
    }*/
    onResponse({listOf(NluLib.IAmDone(), AnswerFalse(), AnswerTrue(),
            AnswerMarked(),
        RejoinderAgreed(),RejoinderDisagreed(),
        No(), Disagree(),
        Probe(), ElaborationRequest(),
        Maybe(), DontKnow(), Backchannel(),
        Agree(),Yes()
    )}){
        raise(Case1(it.intent as Intent))
    }

    onResponse<Case1>{
        println("XXXX>Case1")
        var rejoinder: EnumSubjectRejoinders = it.intent.rejoinder
        furhat.say(rejoinder.toString())
        userDidntSpeak = false
        if(!userSaidMarkedIt && (rejoinder.equals(EnumSubjectRejoinders.ANSWER_MARKED) ||
                    rejoinder.equals(EnumSubjectRejoinders.ASSENT)) )userSaidMarkedIt = true
        if(userSaidMarkedIt){//We are assuming they JUST SAID they marked it now.
            if(robotAskedIfMarked){// THis "means" that the robot did answer
                if(gui.isAnswerMarked()){
                    App.goto(QuestionDisclosure(rejoinder))
                }else{
                    furhat.ask(session.getMarkingRequest())
                }
            }else{// This "means" that the the robot did NOT answer
                if(gui.isAnswerMarked()){
                    App.goto(QuestionDisclosure(rejoinder))
                }else{
                    furhat.ask(session.getMarkingRequest())
                }
            }
        }else{ //The user never said they marked it
            furhat.ask( session.getMarkingRequest())
        }
    }
    onNoResponse{
        if(userDidntSpeak){
            this.reentry()
        }else{
            raise(Case1(EnumSubjectRejoinders.SILENCE))
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
fun QuestionDisclosure(rejoinderType: EnumSubjectRejoinders? = null) = state(parent = Parent) {
    val session: SessionHandler = App.getSession()
    val gui: GUIHandler = App.getGUI()


    onEntry {
        //App.printState(thisState)
        println("XXXDisclosure")
        furhat.ask(session.getDisclosure())
    }
    onReentry {
        //App.printState(thisState,"R")//
        //println("XXXonReentry")
        //furhat.ask(session.get())
    }

    onResponse<NluLib.IAmDone> { raise(Case1(EnumSubjectRejoinders.I_AM_DONE)) }
    onResponse<AnswerFalse> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<AnswerTrue> { raise(Case1(EnumSubjectRejoinders.ANSWER_TRUE)) }

    onResponse<No> { raise(Case1(EnumSubjectRejoinders.DENIAL)) }
    onResponse<Agree> { raise(Case1(EnumSubjectRejoinders.ASSENT)) }
    onResponse<Disagree> { raise(Case1(EnumSubjectRejoinders.DENIAL)) }
    onResponse<DontKnow> { raise(Case1(EnumSubjectRejoinders.NON_COMMITTAL)) }
    onResponse<RequestRepeat> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<RejoinderAgreed> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<RejoinderDisagreed> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<ElaborationRequest> { raise(Case1(EnumSubjectRejoinders.ELABORATION_REQUEST)) }
    onResponse<Backchannel> { raise(Case1(EnumSubjectRejoinders.BACKCHANNEL)) }
    onResponse<AnswerMarked> { raise(Case1(EnumSubjectRejoinders.ANSWER_MARKED)) }
    onResponse<Yes> { raise(Case1(EnumSubjectRejoinders.ASSENT)) }

    onResponse<Case1> {
        println("XXXX>Case1")
        var rejoinder: EnumSubjectRejoinders = it.intent.rejoinder

        if (rejoinder.equals(EnumSubjectRejoinders.ANSWER_MARKED) ||
            rejoinder.equals(EnumSubjectRejoinders.ASSENT) ){
            App.goto(QuestionReview())
        } else{
            App.goto(QuestionPersuasion())
        }
    }

    onNoResponse{
        App.goto(QuestionCheckpoint(EnumSubjectRejoinders.SILENCE))
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


fun QuestionPersuasion(rejoinderType: EnumSubjectRejoinders? = null) = state(parent = Parent) {
    val session: SessionHandler = App.getSession()
    val gui: GUIHandler = App.getGUI()


    onEntry {
        //App.printState(thisState)
        println("XXXDisclosure")
        furhat.ask(session.getDisclosure())
    }
    onReentry {
        //App.printState(thisState,"R")//
        //println("XXXonReentry")
        //furhat.ask(session.get())
    }

    onResponse<NluLib.IAmDone> { raise(Case1(EnumSubjectRejoinders.I_AM_DONE)) }
    onResponse<AnswerFalse> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<AnswerTrue> { raise(Case1(EnumSubjectRejoinders.ANSWER_TRUE)) }

    onResponse<No> { raise(Case1(EnumSubjectRejoinders.DENIAL)) }
    onResponse<Agree> { raise(Case1(EnumSubjectRejoinders.ASSENT)) }
    onResponse<Disagree> { raise(Case1(EnumSubjectRejoinders.DENIAL)) }
    onResponse<DontKnow> { raise(Case1(EnumSubjectRejoinders.NON_COMMITTAL)) }
    onResponse<RequestRepeat> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<RejoinderAgreed> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<RejoinderDisagreed> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<ElaborationRequest> { raise(Case1(EnumSubjectRejoinders.ELABORATION_REQUEST)) }
    onResponse<Backchannel> { raise(Case1(EnumSubjectRejoinders.BACKCHANNEL)) }
    onResponse<AnswerMarked> { raise(Case1(EnumSubjectRejoinders.ANSWER_MARKED)) }
    onResponse<Yes> { raise(Case1(EnumSubjectRejoinders.ASSENT)) }

    onResponse<Case1> {
        println("XXXX>Case1")
        var rejoinderType: EnumSubjectRejoinders = it.intent.rejoinder

        if (rejoinderType.equals(EnumSubjectRejoinders.ANSWER_MARKED) ||
            rejoinderType.equals(EnumSubjectRejoinders.ASSENT) ){
            App.goto(QuestionReview(rejoinderType))
        } else{
            //App.goto(QuestionPersuasion(rejoinderType))
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
fun QuestionReview(rejoinderType: EnumSubjectRejoinders? = null) = state(parent = Parent) {
    val session: SessionHandler = App.getSession()
    val gui: GUIHandler = App.getGUI()


    onEntry {
        //App.printState(thisState)
        println("XXXDisclosure")

        //furhat.ask(session.getClaim())
    }
    onReentry {
        //App.printState(thisState,"R")//
        //println("XXXonReentry")
        //furhat.ask(session.get())
    }

    onResponse<NluLib.IAmDone> { raise(Case1(EnumSubjectRejoinders.I_AM_DONE)) }
    onResponse<AnswerFalse> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<AnswerTrue> { raise(Case1(EnumSubjectRejoinders.ANSWER_TRUE)) }

    onResponse<No> { raise(Case1(EnumSubjectRejoinders.DENIAL)) }
    onResponse<Agree> { raise(Case1(EnumSubjectRejoinders.ASSENT)) }
    onResponse<Disagree> { raise(Case1(EnumSubjectRejoinders.DENIAL)) }
    onResponse<DontKnow> { raise(Case1(EnumSubjectRejoinders.NON_COMMITTAL)) }
    onResponse<RequestRepeat> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<RejoinderAgreed> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<RejoinderDisagreed> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<ElaborationRequest> { raise(Case1(EnumSubjectRejoinders.ELABORATION_REQUEST)) }
    onResponse<Backchannel> { raise(Case1(EnumSubjectRejoinders.BACKCHANNEL)) }
    onResponse<AnswerMarked> { raise(Case1(EnumSubjectRejoinders.ANSWER_MARKED)) }
    onResponse<Yes> { raise(Case1(EnumSubjectRejoinders.ASSENT)) }

    onResponse<Case1> {
        println("XXXX>Case1")
        var rejoinder: EnumSubjectRejoinders = it.intent.rejoinder

        if (rejoinder.equals(EnumSubjectRejoinders.ANSWER_MARKED) ||
            rejoinder.equals(EnumSubjectRejoinders.ASSENT) ){
            //App.goto(QuestionReview)
        } else{
            //App.goto(QuestionPersuasion)
        }
    }

    onNoResponse{
        App.goto(QuestionCheckpoint(EnumSubjectRejoinders.SILENCE))
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
fun QuestionCheckpoint(rejoinderType: EnumSubjectRejoinders? = null) = state(parent = Parent) {
    val session: SessionHandler = App.getSession()
    val gui: GUIHandler = App.getGUI()


    onEntry {
        //App.printState(thisState)
        println("XXXDisclosure")
        furhat.ask(session.getDisclosure())
    }
    onReentry {
        //App.printState(thisState,"R")//
        //println("XXXonReentry")
        //furhat.ask(session.get())
    }

    onResponse<NluLib.IAmDone> { raise(Case1(EnumSubjectRejoinders.I_AM_DONE)) }
    onResponse<AnswerFalse> { raise(Case1(EnumSubjectRejoinders.ANSWER_FALSE)) }
    onResponse<AnswerTrue> { raise(Case1(EnumSubjectRejoinders.ANSWER_TRUE)) }

    onResponse<No> { raise(Case1(EnumSubjectRejoinders.DENIAL)) }
    onResponse<Agree> { raise(Case1(EnumSubjectRejoinders.ASSENT)) }
    onResponse<Disagree> { raise(Case1(EnumSubjectRejoinders.DENIAL)) }
    onResponse<DontKnow> { raise(Case1(EnumSubjectRejoinders.NON_COMMITTAL)) }
    onResponse<RequestRepeat> { raise(Case1(EnumSubjectRejoinders.REPEAT_REQUEST)) }
    onResponse<RejoinderAgreed> { raise(Case1(EnumSubjectRejoinders.REJOINDER_AGREED)) }
    onResponse<RejoinderDisagreed> { raise(Case1(EnumSubjectRejoinders.REJOINDER_DISAGREED)) }
    onResponse<ElaborationRequest> { raise(Case1(EnumSubjectRejoinders.ELABORATION_REQUEST)) }
    onResponse<Backchannel> { raise(Case1(EnumSubjectRejoinders.BACKCHANNEL)) }
    onResponse<AnswerMarked> { raise(Case1(EnumSubjectRejoinders.ANSWER_MARKED)) }
    onResponse<Yes> { raise(Case1(EnumSubjectRejoinders.ASSENT)) }

    onResponse<Case1> {
        println("XXXX>Case1")
        var rejoinder: EnumSubjectRejoinders = it.intent.rejoinder

        if (rejoinder.equals(EnumSubjectRejoinders.ANSWER_MARKED) ||
            rejoinder.equals(EnumSubjectRejoinders.ASSENT) ){
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
