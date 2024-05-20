package furhatos.app.isiser.flow.main

import furhatos.app.isiser.App
import furhatos.flow.kotlin.*
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
            users.count == 0 && furhat.isVirtual() -> furhat.doSay("I can't see anyone. Add a virtual user please. ")
            users.count == 0 && !furhat.isVirtual() -> furhat.doSay("I can't see anyone. Step closer please. ")
        }
        */
        context = Context(furhat, users)
    }

    onEntry {
        App.printState(thisState)
        //furhat.attendNobody()
        //furhat.gesture(CloseEyes, priority=10)
        handleListen()
    }
    onReentry {
        App.printState(thisState,"R")
        furhat.attendNobody()
        furhat.gesture(CloseEyes, priority=10)
        handleListen()
    }

    onUserEnter {
        println("User enters")
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

    onUserLeave(instant=true) {
        println("User leaves")
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


