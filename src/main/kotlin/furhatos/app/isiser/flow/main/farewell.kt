import furhatos.app.isiser.App
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.flow.main.OpenEyes
import furhatos.app.isiser.handlers.SessionHandler
import furhatos.app.isiser.handlers.doAsk
import furhatos.app.isiser.setting.EnumWordingTypes
import furhatos.app.isiser.setting.ExtendedUtterance
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures

val Farewell : State = state(Parent) {
    val session: SessionHandler = App.getSession()

    fun getWording(i: Int): ExtendedUtterance {
        return session.getUtterance(EnumWordingTypes.FAREWELL)
    }

    onEntry {
        App.printState(thisState)
        furhat.gesture(OpenEyes, priority = 10)
        furhat.gesture(Gestures.Smile(duration = 2.0))

        /*furhat.doAsk(getWording(0))*/
        furhat.doAsk("OK That was it. It was good to meet you.")
        furhat.gesture(Gestures.BigSmile(0.6, 2.5))
        /*furhat.doAsk( {+"Nice to meet you."
            + delay(700)
            + "How you doing?"})*/
    }
    onReentry {
        furhat.doAsk("You have a nice day.")
    }

    onResponse{
        reentry()
    }
}