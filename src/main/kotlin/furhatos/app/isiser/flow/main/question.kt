package furhatos.app.isiser.flow.main
import furhat.libraries.standard.NluLib
import furhatos.app.isiser.flow.Parent
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state

val Question = state(Parent) {
    onResponse<NluLib.IAmDone> {
        random(
            {   furhat.say("Oh, give me a sec") },
            {   furhat.say("Mm...I see, hold on") }
        )
    }
}