package furhatos.app.isiser.flow.main

import Farewell
import furhat.libraries.standard.NluLib
import furhatos.app.isiser.App
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.handlers.SessionHandler
import furhatos.app.isiser.handlers.doAsk
import furhatos.app.isiser.handlers.doSay
import furhatos.app.isiser.nlu.*
import furhatos.app.isiser.setting.EnumRejoinders
import furhatos.app.isiser.setting.EnumRobotMode
import furhatos.app.isiser.setting.EnumWordingTypes
import furhatos.app.isiser.setting.ExtendedUtterance
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.Intent
import furhatos.nlu.common.DontKnow
import furhatos.nlu.common.Maybe
import furhatos.nlu.common.No
import furhatos.nlu.common.Yes

val Welcome : State = state(Parent) {
    val session: SessionHandler = App.getSession()

    onEntry {
        App.printState(thisState)
        furhat.gesture(OpenEyes, priority = 10)
        furhat.gesture(Gestures.Smile(duration = 2.0))

        furhat.doAsk(session.getUtterance(EnumWordingTypes.WELCOME))

        furhat.gesture(Gestures.BigSmile(0.6, 2.5))
        /*furhat.doAsk( {+"Nice to meet you."
            + delay(700)
            + "How you doing?"})*/
    }
    onReentry {

    }

    onResponse{
        App.goto(Farewell)
    }
}
val ReviewInstructions : State = state(Parent) {
    val session: SessionHandler = App.getSession()
    var userDidntEverTalk = true
    var checkPointReached = false
    var checkPointPassed = false
    onEntry {
        furhat.doAsk(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_GENERAL))
    }
    onReentry {
        furhat.doAsk(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_GENERAL))
    }

    onResponse({listOf(
        MeReady(),
        NluLib.IAmDone(),
        Disagree(), No(),
        Probe(),
        ElaborationRequest(),
        DontKnow(),
        Backchannel(),
        Agree(),Yes()
    )}){
        //furhat.doAsk(it.intent.toString())
        raise(AllIntents(it.intent as Intent))
    }
    onResponse<AllIntents> {
        val rejoinder: EnumRejoinders = it.intent.rejoinder
        furhat.doSay(rejoinder.toString())
        userDidntEverTalk = if(rejoinder==EnumRejoinders.SILENCE) userDidntEverTalk else false
        if(userDidntEverTalk){
            reentry()
        }else{
            if(!checkPointPassed) {
                if (!checkPointReached) {
                    checkPointReached = true
                    furhat.doSay(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_DETAILED, rejoinder))
                    furhat.doAsk(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_CHECKPOINT, rejoinder))
                } else {
                    when (rejoinder) {
                        EnumRejoinders.SILENCE -> furhat.doAsk(
                            session.getUtterance(EnumWordingTypes.INSTRUCTIONS_CHECKPOINT,rejoinder)
                        )

                        EnumRejoinders.I_AM_DONE, EnumRejoinders.ME_READY,
                        EnumRejoinders.ASSENT -> {
                            checkPointPassed = true
                            furhat.doAsk(session.getUtterance(EnumWordingTypes.PRESS_READY_REQUEST))
                        }
                        else -> {
                            furhat.doSay(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_DETAILED, rejoinder))
                            furhat.doAsk(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_CHECKPOINT, rejoinder))
                        }
                    }
                }
            }else{
                furhat.doAsk(session.getUtterance(EnumWordingTypes.PRESS_READY_REQUEST))
            }
        }
    }

/*    onResponse<Yes> {
        furhat.doSay {
            +"Right, as far as I know, once you press the button there on the tablet, "
            +"we will both get a question to resolve together.  "
            +delay(1000)
        }
        furhat.doAsk({ +"So"
            + delay(700)
            + "Whenever you want, press the button, go ahead"})
    }*/
/*    onResponse<No> {
        furhat.doAsk("I would ask you now if you have any questions. But now press the button.")
    }*/


}
