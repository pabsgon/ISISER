import furhatos.app.isiser.App
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.flow.main.OpenEyes
import furhatos.app.isiser.flow.main.Sleep
import furhatos.app.isiser.handlers.SessionHandler
import furhatos.app.isiser.handlers.doAsk
import furhatos.app.isiser.handlers.doSay
import furhatos.app.isiser.setting.EnumWordingTypes
import furhatos.app.isiser.setting.ExtendedUtterance
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures

val Farewell : State = state(Parent) {
    val session: SessionHandler = App.getSession()
    var goToSleep = false
    fun getWording(i: Int): ExtendedUtterance {
        return session.getUtterance(EnumWordingTypes.FAREWELL_START)
    }

    onEntry {
        furhat.gesture(Gestures.Smile(duration = 4.0))

        /*furhat.doAsk(getWording(0))*/
        furhat.doAsk(session.getUtterance(EnumWordingTypes.FAREWELL_START))
        furhat.gesture(Gestures.BigSmile(0.6, 2.5))
    }

    onNoResponse{
        if(!goToSleep){
            goToSleep=true
            furhat.doAsk(session.getUtterance(EnumWordingTypes.FAREWELL_END), null, 2000)
        }else{
            App.goto(Sleep)
        }
    }
    onResponse{
        if(!goToSleep){
            goToSleep=true
            furhat.doAsk(session.getUtterance(EnumWordingTypes.FAREWELL_END), null, 2000)
        }
    }
}