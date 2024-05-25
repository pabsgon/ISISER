package furhatos.app.isiser.flow.main

import furhat.libraries.standard.GesturesLib
import furhat.libraries.standard.NluLib
import furhatos.app.isiser.App
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.handlers.SessionHandler
import furhatos.app.isiser.handlers.doAsk
import furhatos.app.isiser.handlers.doSay
import furhatos.app.isiser.nlu.*
import furhatos.app.isiser.setting.*
import furhatos.event.senses.SenseSpeech
import furhatos.flow.kotlin.*
import furhatos.gestures.Gesture
import furhatos.gestures.Gestures
import furhatos.nlu.common.*

val Testing = state(parent = Parent) {
    var session: SessionHandler = App.getSession()
    var speechDuration = 0
    val lst: MutableList<Gesture> = mutableListOf(
        GesturesLib.PerformDoubleNod,
        GesturesLib.ExpressConfusion1(),
        GesturesLib.PerformOhYeah1,
        GesturesLib.PerformIntenseShake,
        GesturesLib.PerformMouthSide1, // INTERESTINT
        GesturesLib.PerformReadWordInAir,
        GesturesLib.PerformHeadDown(), // INTERESTING
        GesturesLib.PerformSceptical1, // Mm
        GesturesLib.PerformThoughtful1, //
        GesturesLib.PerformThoughtful2, // interesting
        GesturesLib.PerformWhat1, // INTERESTING
        GesturesLib.ExpressConsidering1(), // INTERESTINT
        GesturesLib.ExpressDetermination(), // MAYBE
        GesturesLib.ExpressGazeAversion1(), // INTER
        GesturesLib.ExpressGazeAversion2(), //same
        GesturesLib.ExpressGuilt1(), // maybe
        GesturesLib.ExpressInterest1(), // maybe
        GesturesLib.ExpressRecall(), // maybe
        GesturesLib.ExpressRecallDown(), // maybe
        GesturesLib.ExpressSmileUnsure1(), // could
        GesturesLib.ExpressThinking(), // UNCERTAIN
        GesturesLib.ExpressUncertaintyOrDiscomfort(), //UNCERT
        GesturesLib.ExpressUnconvinced(), //UNC
        GesturesLib.ExpressWhatThe1(), //NO
        GesturesLib.PerformDoubleNod(), // COULD BE
        GesturesLib.PerformHeadeUp() // CERTAIN (MAYBE)
    )
    fun Furhat.performAndCycleGesture(back:Boolean = false) {

        if (lst.isNotEmpty()) {
            // Get the first gesture
            val gest = lst.removeAt(if(back)lst.size-1 else 0)

            // Perform the gesture
            this.gesture(gest)

            // Add the gesture back to the end of the list
            if(back){
                lst.add(0,gest)
            }else {
                lst.add(gest)
            }

            // Get the gesture name
            val gestureName = gest.name

            // Ask with the gesture name
            this.ask("$gestureName")
        } else {
            this.say("The list of gestures is empty.")
        }
    }
    fun getWording(i: Int):ExtendedUtterance{
        return session.getUtterance(EnumWordingTypes.WELCOME)
    }
    val w0 = getWording(0)

    onEntry {
        furhat.doAsk("Ok, let's test. Say whatever you want.")
        //furhat.listen(endSil = 1000, timeout = 8000, maxSpeech = 30000)
    }
    onReentry {
        furhat.doAsk("I'm listening.")
    }
    onEvent<SenseSpeech> {
        speechDuration = it.length
        println("You said something I couldn't understand in $speechDuration milliseconds.")
    }
    onResponse<NluLib.IAmDone> {
            furhat.doAsk("You said you finished", Gestures.Thoughtful)
    }
    onResponse<AnswerFalse> {
        furhat.doAsk("You said it is false", Gestures.GazeAway)
    }
    onResponse<AnswerTrue> {
        furhat.doAsk("You said it is true", GesturesLib.PerformDoubleNod)
    }
    onResponse<Yes> {
        furhat.performAndCycleGesture()
    }

    onResponse<No> {
        furhat.performAndCycleGesture(true)
    }
    onResponse<Agree> {
        furhat.doAsk("You agreed")
    }
    onResponse<Wait> {
        furhat.doAsk("More time?")
    }
    onResponse<Disagree> {
        if (it.intent.intentName != "Disagree")
            furhat.doSay("Oh...")
        println("XXXDisagree [$it.intent.intentName ]")
        furhat.doAsk("You disagreed")
    }
    onResponse<DontKnow> {
        furhat.doAsk("That was non-committal")
    }
    onResponse<RequestRepeat> {
        furhat.doAsk("You want me to repeat")
    }
    onResponse<RejoinderAgreed> {
        furhat.doAsk("That's a rejoinder I <emphasis>agree<emphasis> with")
    }
    onResponse<RejoinderDisagreed> {
        furhat.doAsk("That's a rejoinder I don't agree with")
    }
    onResponse<Greeting> {
        furhat.ask("Hello to you too. That's a rejoinder I  agree<emphasis> with")
    }
    onResponse<ElaborationRequest> {
        furhat.doAsk("You want me to elaborate.")
    }
    onResponse<Backchannel> {
        furhat.doAsk("You just backchanneled.")
    }
    onPartialResponse<DontKnow> {
        // Greet the user and proceed with the order in the same turn
        raise(it, it.secondaryIntent)
    }
    onPartialResponse<Agree> {
        raise(it, it.secondaryIntent)
    }
    onPartialResponse<No> {
        raise(it, it.secondaryIntent)
    }
    onPartialResponse<Yes> {
        raise(it, it.secondaryIntent)
    }
    onResponse {
        if(it.text.uppercase() == "CONDITIONS"){

            furhat.doSay(w0,EnumRobotMode.NEUTRAL.speechRate)
            furhat.doSay(w0,EnumRobotMode.UNCERTAIN.speechRate)
            furhat.doAsk(w0,EnumRobotMode.CERTAIN.speechRate)
        }
        if(seemsLikeBackchannel(it.text, it.speech.length)){
            raise(Backchannel())
        }else{
            furhat.doAsk("You said something in about ${it.speech.length} milliseconds. ")
        }
    }
    onNoResponse {
        reentry()
    }
    onResponseFailed {
        furhat.doAsk("The connection failed... Can you repeat that?")
    }
}


