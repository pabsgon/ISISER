package furhatos.app.isiser.flow.main

import furhatos.app.isiser.Session
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.nlu.Greetings
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val Welcome : State = state(Parent) {
    onEntry {
        Session.printState(thisState)
        furhat.gesture(OpenEyes, priority = 10)
        furhat.gesture(Gestures.Smile(duration = 2.0))

        random(
            {furhat.say {
                +"Hi there"
                +delay(1000)}},
            {furhat.say {
                +"Oh, hello there"
                +delay(1000)}}
        )

        furhat.gesture(Gestures.BigSmile(0.6, 2.5))
        furhat.ask( {+"Nice to meet you."
            + delay(700)
            + "How you doing?"})
    }
    onReentry {
        Session.printState(thisState,"R")
        furhat.say {
            +"Oh, you there."
            +delay(1000)
            +"Shall we carry on?"
        }
    }
    onResponse{
        goto(ReviewInstructions)
    }

}
val ReviewInstructions : State = state(Parent) {
    onEntry {
        furhat.ask({+"Ok"
                + delay(700)
                + "I suppose they told you what we are here for a little bit"
                +delay(400)
                + "Right?"})
    }
    onReentry {
        furhat.ask({+"So"
                + delay(700)
            + "I guess we have clear what we have to do now, don't we?"})
    }
    onResponse<Yes> {
        furhat.say {
            +"Right, as far as I know, once you press the button there on the tablet, "
            +"we will both get a question to resolve together.  "
            +delay(1000)
        }
        furhat.ask({ +"So"
            + delay(700)
            + "Whenever you want, press the button, go ahead"})
    }
    onResponse<No> {
        furhat.say("I would ask you now if you have any questions. But now press the button.")
    }
    onResponse {
        reentry()
    }
}