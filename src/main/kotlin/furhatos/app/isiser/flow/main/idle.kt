package furhatos.app.isiser.flow.main

import furhatos.app.isiser.Session
import furhatos.app.isiser.nlu.Greetings
import furhatos.app.isiser.setting.GUIEvent
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.skills.UserManager




val Idle: State = state {
    data class Context(val furhat: Furhat, val users: UserManager)
    var context: Context? = null

    fun handleListen(){
        if (context != null) {
            if(context!!.users.count > 0){
                println(" >listening...")
                context!!.furhat.listen(endSil = 1000, timeout = 8000, maxSpeech = 30000)
            }
        }
    }
    init {
        /* when {
            users.count > 0 -> {
                furhat.attend(users.random)
                goto(Greeting)
            }
            users.count == 0 && furhat.isVirtual() -> furhat.say("I can't see anyone. Add a virtual user please. ")
            users.count == 0 && !furhat.isVirtual() -> furhat.say("I can't see anyone. Step closer please. ")
        }
        */
        context = Context(furhat, users)
    }
    onEvent<GUIEvent> {
        //val loggerEventListener = EventListener { event ->
            //if(event is GUIEvent && event.message== GUI_STARTED){
              //  raise()
           // }
        //}
        /*
        if(it.message == GUI_STARTED){
            furhat.say("Ok, well, let's start the party!")
        }*/
    }
    onEntry {
        println("Idle>onEntry.Users:" + users.count)
        //furhat.attendNobody()
        //furhat.gesture(CloseEyes, priority=10)
        handleListen()
    }
    onReentry {
        println("Idle>onReentry.Users:" + users.count)
        furhat.attendNobody()
        furhat.gesture(CloseEyes, priority=10)
        handleListen()
    }

    onUserEnter {
        Session.log("User enters")
        println("Idle>onUserEnter")
        //furhat.gesture(Gestures.Smile(duration=2.0))
        furhat.attend(it)
        furhat.glance(it)
        furhat.listen(endSil = 1000, timeout = 8000, maxSpeech = 30000)

// Change the default thresholds:
        /*furhat.param.endSilTimeout = 1000
        furhat.param.noSpeechTimeout = 8000
        furhat.param.maxSpeechTimeout = 30000

         */
        //goto(Greeting)
    }
    onResponse<Greetings> {
        if(Session.isGUILoaded() && users.count>0 ) {
            furhat.gesture(OpenEyes, priority = 10)
            furhat.gesture(Gestures.Smile(duration = 2.0))

            goto(Welcome)
        }else{
            println("Hearing, but not listening: The GUI must initialised and there must be a user present. ")
        }
    }

    onUserLeave(instant=true) {
        Session.log("User leaves")
        /*

        TODO("We need to control that a user for some reason leaves momentarily.
         If that happens, then when they get back in the user will not be the same,
         so everything that was done will be lost.
         ")
         */

        println("Idle>onUserLeave")
        if (users.count > 0) {
            if (it == users.current) {
                furhat.attend(users.other)
            } else {
                furhat.glance(it)
            }
        }
        furhat.stopListening()
    }
    onResponse<Greetings> {
        furhat.gesture(OpenEyes, priority=10)
        furhat.gesture(Gestures.Smile(duration=2.0))

        goto(Welcome)
    }
    onResponseFailed {
        handleListen()
    }
    onResponse{
        println("Idle>onResponseElse")
        handleListen()
    }
    onNoResponse{
        println("Idle>onNoResponse")
        handleListen()
    }

}


