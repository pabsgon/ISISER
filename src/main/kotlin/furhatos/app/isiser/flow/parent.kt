package furhatos.app.isiser.flow

import furhatos.app.isiser.flow.main.Idle
import furhatos.flow.kotlin.*

val Parent: State = state {

    onUserLeave(instant = true) {
        println("Parent>onUserLeave")
        when {
            users.count == 0 -> goto(Idle)
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


}