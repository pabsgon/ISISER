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
        furhat.gesture(CloseEyes, priority=10)
        furhat.attendNobody()
    }
    onTime(delay = 60000) { // Wait for 60 seconds before terminating
        furhat.doSay("No start signal received, terminating the program.")
        System.exit(0) // Terminate the JVM
    }
}


