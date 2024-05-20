package furhatos.app.isiser.flow.main

import furhatos.app.isiser.App
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.handlers.GUIEvent
import furhatos.app.isiser.handlers.doSay
import furhatos.app.isiser.setting.EnumStages
import furhatos.flow.kotlin.*


val Sleep: State = state(Parent) {
    init {
        furhat.gesture(CloseEyes, priority=10)
    }
    onEntry {
        App.printState(thisState)
        furhat.attendNobody()
    }
    onReentry {
        App.printState(thisState,"R")
        furhat.attendNobody()
    }
    onTime(delay = 60000) { // Wait for 60 seconds before terminating
        furhat.doSay("No start signal received, terminating the program.")
        System.exit(0) // Terminate the JVM
    }
    /*
    onEvent<GUIEvent> {
        println("Sleep GUIEvent [${App.getStage()}]")
        if(App.getStage() == EnumStages.STAGE_0_2){
            println("Sleep GUIEvent 0.2")
            App.goto(Welcome)
        }else
            println("Sleep NOT GUIEvent 0.2")
    }
     */
}


