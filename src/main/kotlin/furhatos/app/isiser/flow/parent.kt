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

val Parent: State = state {
    val session = App.getSession()

    onUserLeave(instant = true) {
        println("Parent>onUserLeave")
        when {
            users.count == 0 -> goto(Sleep)
            it == users.current -> furhat.attend(users.other)
        }
    }
    onUserEnter(instant = true) {
        println("Parent>onUserEnter")
        furhat.glance(it)
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
        //furhat.doAsk(it.intent.toString())
        raise(AllIntents(it.intent as Intent))
    }

    onResponseFailed {
        furhat.doAsk("I think my connection broke. Did you say something?")
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
            if(session.isActive()){
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
            EventType.USER_SET -> App.goto(Welcome)
            EventType.QUESTION_SET -> App.goto(QuestionReflection())
            else -> {}
        }
    }

    onNoResponse{
        furhat.say("Parent")
        raise(AllIntents(EnumRejoinders.SILENCE))
    }
    /*
    onEvent<GUIEvent> {
        println("Parent:GUIEvent")
        if(it.isAcceptable) {
            println("Parent:GUIEvent [Acceptable]")
            if (App.isQuestionStage()) {
                println("Parent:GUIEvent [IsQuestionStage]:[${it.type.toString()}]")
                when (it.type) {
                    EventType.ANSWER_SENT -> {
                        reentry()
                    }
                    // This should only happen when the stage is a question.
                    EventType.SYNCH_REQUESTED -> {
                        goto(QuestionReflection)
                    }
                    EventType.NEW_STAGE_REQUESTED -> {
                        goto(QuestionReflection)
                    }
                    else -> {
                        // This is equivalent to the 'default' case in a traditional switch statement.
                        // Handle the case where none of the above conditions are met.
                        println("No matching type found")
                    }
                }
            } else {
                if (App.getStage() == EnumStages.STAGE_0_2) {
                    println("Parent:GUIEvent [Stage 0.2]")
                    goto(Welcome)
                }else
                    println("Parent:GUIEvent [Stage ${App.getStageName()}]")
            }
        }
    }

     */
}