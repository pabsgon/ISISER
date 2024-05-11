package furhatos.app.isiser.flow

import furhatos.app.isiser.Session
import furhatos.app.isiser.flow.main.QuestionReflection
import furhatos.app.isiser.flow.main.Sleep
import furhatos.app.isiser.flow.main.Welcome
import furhatos.app.isiser.setting.*
import furhatos.flow.kotlin.*

val Parent: State = state {

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
    onResponse {
        println("Parent>onResponseElse")
        furhat.say("Oops, I didn't get that.")
        reentry()
    }
    onNoResponse {
        println("Parent>onNoResponse")
        furhat.say("Oops, I don't hear you.")
        reentry()
    }
    onResponseFailed {
        furhat.say("I think my connection broke. Did you say something?")
    }
    //onEvent<GUIEvent> {
        //val loggerEventListener = EventListener { event ->
        //if(event is GUIEvent && event.message== GUI_STARTED){
        //  raise()
        // }
        //}
        /*
        if(it.message == GUI_STARTED){
            furhat.say("Ok, well, let's start the party!")
        }*/
    //}
    onEvent<GUIEvent> {
        println("Parent:GUIEvent")
        if(it.isAcceptable) {
            println("Parent:GUIEvent [Acceptable]")
            if (Session.isQuestionStage()) {
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
                if (Session.getStage() == StagesEnum.STAGE_0_2) {
                    println("Parent:GUIEvent [Stage 0.2]")
                    goto(Welcome)
                }else
                    println("Parent:GUIEvent [Stage ${Session.getStageName()}]")
            }
        }
    }
}