package furhatos.app.isiser.flow

import furhat.libraries.standard.NluLib
import furhatos.app.isiser.App
import furhatos.app.isiser.flow.main.QuestionReflection
import furhatos.app.isiser.flow.main.Sleep
import furhatos.app.isiser.flow.main.Welcome
import furhatos.app.isiser.handlers.SessionEvent
import furhatos.app.isiser.handlers.doAsk
import furhatos.app.isiser.handlers.doSay
import furhatos.app.isiser.nlu.*
import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.*
import furhatos.nlu.Intent
import furhatos.nlu.common.*
import furhatos.records.Location

val Parent: State = state {
    val session = App.getSession()
    onUserLeave(instant = true) {
        if(users.count == 0){
            furhat.doSay("Bye bye!. Sorry, I cannot work alone. I am leaving too.")
            System.exit(0) // Terminate the JVM
        }
    }
    onUserEnter(instant = true) {
        furhat.attend(it)
        if(session.isSessionActive() && users.count>0){
            //This is an assumption: there
            App.goto(Welcome)
        }
    }
    onResponse<RequestRepeat>{raise(SayItAgain())}
    onResponse<SayItAgain>{
        furhat.doAsk(session.getUtterance(EnumWordingTypes.REPEAT))
    }
    onResponse<Wait>{raise(TimeRequest())}
    onResponse<TimeRequest>{
        furhat.doAsk(session.getUtterance(EnumWordingTypes.GIVE_TIME))
    }
    onResponse<OffTopic>{
        furhat.doAsk(session.getUtterance(EnumWordingTypes.ELABORATION_REQUEST))
    }
    onResponse({listOf(
        // RequestRepeat: (handled by Parent)
        // SayItAgain: (handled by Parent)
        // TimeRequest: (handled by Parent)
        // Wait: (handled by Parent)
        NluLib.IAmDone(), //-> EnumRejoinders.ME_READY
        MeReady(), //-> EnumRejoinders.ME_READY
        ILikeMyAnswer(), // -> EnumRejoinders.I_LIKE_MY_ANSWER
        ILikeYourAnswer(), //-> EnumRejoinders.I_LIKE_YOUR_ANSWER
        AnswerFalse(), //-> EnumRejoinders.ANSWER_FALSE
        AnswerTrue(), //-> EnumRejoinders.ANSWER_TRUE
        AnswerMarked(), //-> EnumRejoinders.ANSWER_MARKED
        No(), Disagree(), //  -> EnumRejoinders.DENIAL
        RejoinderAgreed(), //-> EnumRejoinders.REJOINDER_AGREED
        RejoinderDisagreed(), // -> EnumRejoinders.REJOINDER_DISAGREED
        Probe(), //   -> EnumRejoinders.PROBE
        DontUnderstand(), ElaborationRequest(), // -> EnumRejoinders.ELABORATION_REQUEST
        Backchannel(), //-> EnumRejoinders.BACKCHANNEL
        DontKnow(), Maybe(), //  -> EnumRejoinders.NON_COMMITTAL
        Understand(), Agree(), Yes() //-> EnumRejoinders.ASSENT
    )}){

        raise(AllIntents(it.intent as Intent))
    }
    onResponseFailed {
        furhat.doAsk("I think my connection broke. Did you say something?")
        TODO()
    }
    onPartialResponse<DontKnow> {
        // Greet the user and proceed with the order in the same turn
        raise(it, it.secondaryIntent)
    }
    onPartialResponse<Agree> {
        raise(it, it.secondaryIntent)
    }
    onPartialResponse<No> {
        raise(it, it.secondaryIntent)
    }
    onPartialResponse<Yes> {
        raise(it, it.secondaryIntent)
    }
    onResponse {
        if(seemsLikeBackchannel(it.text, it.speech.length)){
            raise(AllIntents(EnumRejoinders.BACKCHANNEL))
        }else{
            if(session.isSessionActive()){
                if(session.inAgreement()){
                    raise(AllIntents(EnumRejoinders.REJOINDER_AGREED))
                }else {
                    raise(AllIntents(EnumRejoinders.REJOINDER_DISAGREED))
                }
            }else{
                raise(AllIntents(EnumRejoinders.OFF_TOPIC))
            }
        }
    }
    onEvent<SessionEvent> {
        when(it.type){
            EventType.USER_SET ->
                if(users.count>0){
                    App.goto(Welcome)
                }
            EventType.QUESTION_SET -> App.goto(QuestionReflection())
            else -> {}
        }
    }
    onNoResponse{
        raise(AllIntents(EnumRejoinders.SILENCE))
    }
}