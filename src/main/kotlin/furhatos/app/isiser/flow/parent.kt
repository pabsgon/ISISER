package furhatos.app.isiser.flow

import furhatos.app.isiser.App
import furhatos.app.isiser.flow.main.QuestionReflection
import furhatos.app.isiser.flow.main.Sleep
import furhatos.app.isiser.flow.main.Welcome
import furhatos.app.isiser.handlers.SessionEvent
import furhatos.app.isiser.nlu.Backchannel
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
    onNoResponse {
        println("Parent>onNoResponse")
        furhat.say("Oops, I don't hear you.")
        reentry()
    }
    onResponseFailed {
        furhat.say("I think my connection broke. Did you say something?")
    }
    onEvent<SessionEvent> {
        when(it.type){
            EventType.USER_SET -> App.goto(Welcome)
            EventType.QUESTION_SET -> App.goto(QuestionReflection)
            else -> {}
        }
    }
    onResponse<RequestRepeat>{
        furhat.ask("Yes I would repeat it but I have not been programmed for this yet")
    }
    onResponse<Wait>{
        furhat.ask("Yes I would give you more time but I have not been programmed for this yet")
    }
    onResponse<Goodbye>{
        goto(QuestionReflection)
    }
    onResponse {
        if(seemsLikeBackchannel(it.text, it.speech.length)){
            raise(Backchannel())
        }else{
            furhat.ask(session.getElaborationRequest())
        }
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