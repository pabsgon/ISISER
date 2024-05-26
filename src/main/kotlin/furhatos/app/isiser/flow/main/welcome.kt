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
import furhatos.records.User


val Welcome : State = state(Parent) {
    val session: SessionHandler = App.getSession()

    onEntry {
        furhat.gesture(OpenEyes, priority = 10)
        furhat.gesture(Gestures.Smile(duration = 2.0))

        furhat.attend(users.all.first())

        furhat.doAsk(session.getUtterance(EnumWordingTypes.WELCOME))

        furhat.gesture(Gestures.BigSmile(0.6, 2.5))
    }

    onResponse{
        App.goto(ReviewInstructions)
    }
    onNoResponse{
        App.goto(ReviewInstructions)
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

    onResponse<AllIntents> {
        val rejoinder: EnumRejoinders = it.intent.rejoinder
        //furhat.doSay(rejoinder.toString())
        userDidntEverTalk = if(rejoinder==EnumRejoinders.SILENCE) userDidntEverTalk else false
        if(userDidntEverTalk){
            reentry()
        }else{
            if(!checkPointPassed) {
                if (!checkPointReached) {
                    checkPointReached = true
                    furhat.doSay(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_DETAILED, rejoinder))
                    furhat.doAsk(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_CHECKPOINT, EnumRejoinders.NONE))
                } else {
                    when (rejoinder) {
                        EnumRejoinders.SILENCE ->
                            furhat.doAsk(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_CHECKPOINT,rejoinder)
                        )

                        EnumRejoinders.ME_READY, EnumRejoinders.ME_READY,
                        EnumRejoinders.ASSENT -> {
                            checkPointPassed = true
                            furhat.doAsk(session.getUtterance(EnumWordingTypes.PRESS_READY_REQUEST))
                        }
                        else -> {
                            furhat.doSay(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_DETAILED, rejoinder))
                            furhat.doAsk(session.getUtterance(EnumWordingTypes.INSTRUCTIONS_CHECKPOINT, EnumRejoinders.NONE))
                        }
                    }
                }
            }else{
                furhat.doAsk(session.getUtterance(EnumWordingTypes.PRESS_READY_REQUEST))
            }
        }
    }
}
