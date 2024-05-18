import furhatos.app.isiser.App
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.flow.main.OpenEyes
import furhatos.app.isiser.flow.main.ReviewInstructions
import furhatos.app.isiser.handlers.SessionHandler
import furhatos.app.isiser.setting.EnumStates
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures

val Farewell : State = state(Parent) {
    val session: SessionHandler = App.getSession()
    val state: EnumStates = EnumStates.FAREWELL
    fun getWording(i: Int): Utterance {
        return session.getWording(state, 0)
    }

    onEntry {
        App.printState(thisState)
        furhat.gesture(OpenEyes, priority = 10)
        furhat.gesture(Gestures.Smile(duration = 2.0))

        /*        random(
                    {furhat.say {
                        +"Hi there"
                        +delay(1000)}},
                    {furhat.say {
                        +"Oh, hello there"
                        +delay(1000)}}
                )*/
        furhat.ask(getWording(0))

        furhat.gesture(Gestures.BigSmile(0.6, 2.5))
        /*furhat.ask( {+"Nice to meet you."
            + delay(700)
            + "How you doing?"})*/
    }
    onReentry {
        furhat.ask {
            +"Oh, you there."
            +delay(1000)
            +"Shall we carry on?"
        }
    }

    onResponse{
        goto(ReviewInstructions)
    }
}