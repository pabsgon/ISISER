package furhatos.app.isiser.flow

import furhatos.app.isiser.App
import furhatos.app.isiser.flow.main.QuestionReflection
import furhatos.app.isiser.flow.main.Sleep
import furhatos.app.isiser.flow.main.Welcome
import furhatos.app.isiser.handlers.SessionEvent
import furhatos.app.isiser.handlers.doAsk
import furhatos.app.isiser.handlers.doSay
import furhatos.app.isiser.nlu.AllIntents
import furhatos.app.isiser.nlu.Backchannel
import furhatos.app.isiser.nlu.RejoinderDisagreed
import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.*
import furhatos.nlu.common.Goodbye
import furhatos.nlu.common.RequestRepeat
import furhatos.nlu.common.Wait

val Parent: State = state {
    var session = App.getSession()

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

    onResponse<RequestRepeat>{
        furhat.doAsk(session.getRepeat())
    }
    onResponse<Wait>{
        furhat.doAsk("Yes I would give you more time but I have not been programmed for this yet")
    }
/*    onNoResponse {
        println("Parent>onNoResponse")
        furhat.doAsk("Oops, I don't hear you.")
    }*/
    onResponseFailed {
        furhat.doAsk("I think my connection broke. Did you say something?")
    }

    onResponse {
        if(seemsLikeBackchannel(it.text, it.speech.length)){
            raise(AllIntents(EnumRejoinders.BACKCHANNEL))
        }else{
            if(session.isActive()){
                if(session.inAgreement()){
                    furhat.doAsk(session.getUtterance(EnumWordingTypes.ELABORATION_REQUEST, EnumRejoinders.REJOINDER_AGREED))
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
            EventType.QUESTION_SET -> App.goto(QuestionReflection)
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