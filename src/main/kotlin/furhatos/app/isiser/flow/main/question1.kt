package furhatos.app.isiser.flow.main

import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onNoResponse
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.gestures.Gestures
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes


val Question1 = state(parent = Question) {
    onEntry {
/*        random(
            { furhat.ask("Ok, question 1" + delay(1000)) },
            { furhat.ask("Quessstion 1. Oh boy. " + delay(1000)) }
        )
*/
        furhat.say(async = true) {
            random {
                +"Ok, question 1"
                +"Quessstion 1"
            }
            + delay(1000)
            random {
                +"Oh boy"
                +"Right"
            }
            +Gestures.GazeAway
            + delay(700)
            random {
                +"Let's see"
                +"Let's think"
                +"Let's just think about it"
            }
        }
    }
    onResponse<Yes> {
        random(
            { furhat.ask("What kind of fruit do you want?") },
            { furhat.ask("What type of fruit?") }
        )
    }

    onResponse<No> {
        furhat.say("Okay, that's a shame. Have a splendid day!")
        goto(Idle)
    }

    onNoResponse{
        furhat.say("Okay, now I would push you here. Tomorrow.")
        goto(Idle)
    }
}