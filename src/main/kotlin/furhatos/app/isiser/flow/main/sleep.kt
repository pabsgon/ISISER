package furhatos.app.isiser.flow.main

import furhatos.app.isiser.Session
import furhatos.app.isiser.flow.consoLog
import furhatos.app.isiser.nlu.Greetings
import furhatos.app.isiser.setting.GUIEvent
import furhatos.app.isiser.setting.StagesEnum
import furhatos.event.EventListener
import furhatos.flow.kotlin.*
import furhatos.gestures.BasicParams
import furhatos.gestures.Gestures
import furhatos.gestures.defineGesture
import furhatos.skills.Skill
import furhatos.skills.UserManager



val Sleep: State = state() {
    init {
        furhat.gesture(CloseEyes, priority=10)
    }
    onEntry {
        Session.printState(thisState)
        furhat.attendNobody()
    }
    onReentry {
        Session.printState(thisState,"R")
        furhat.attendNobody()
    }
    onTime(delay = 60000) { // Wait for 60 seconds before terminating
        furhat.say("No start signal received, terminating the program.")
        System.exit(0) // Terminate the JVM
    }
    onEvent<GUIEvent> {
        println("Sleep GUIEvent [${Session.getStage()}]")
        if(Session.getStage() == StagesEnum.STAGE_0_2){
            println("Sleep GUIEvent 0.2")
            goto(Welcome)
        }else
            println("Sleep NOT GUIEvent 0.2")
    }
}


