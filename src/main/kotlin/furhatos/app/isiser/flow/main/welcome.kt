package furhatos.app.isiser.flow.main

import furhatos.app.isiser.flow.Parent
import furhatos.flow.kotlin.*

val Welcome : State = state(Parent) {
    onEntry {
        random(
            {   furhat.say("Hi there"+ delay(1000)) },
            {   furhat.say("Oh, hello there"+ delay(1000)) }
        )


        goto(Question1)
    }
}