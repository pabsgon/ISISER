package furhatos.app.isiser.flow.main

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
import furhatos.nlu.common.*

val Testing = state(parent = Parent) {
    var session: SessionHandler = App.getSession()
    var speechDuration = 0

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
            furhat.doAsk("You said you finished")
    }
    onResponse<AnswerFalse> {
        furhat.doAsk("You said it is false")
    }
    onResponse<AnswerTrue> {
        furhat.doAsk("You said it is true")
    }
    onResponse<Yes> {
        furhat.doAsk("You assented")
    }

    onResponse<No> {
        furhat.doAsk("You denied")
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
        furhat.doAsk("That's a rejoinder I agree with")
    }
    onResponse<RejoinderDisagreed> {
        furhat.doAsk("That's a rejoinder I don't agree with")
    }
    onResponse<Greeting> {
        furhat.doAsk("Hello to you too.")
    }
    onResponse<ElaborationRequest> {
        furhat.doAsk("You want me to elaborate.")
    }
    onResponse<Backchannel> {
        furhat.doAsk("You just backchanneled.")
    }
    onPartialResponse<DontKnow> { // Catches a Greeting together with another intent, such as Order
        // Greet the user and proceed with the order in the same turn
        raise(it, it.secondaryIntent)
    }
    onPartialResponse<Agree> { // Catches a Greeting together with another intent, such as Order
        // Greet the user and proceed with the order in the same turn
        raise(it, it.secondaryIntent)
    }
    onPartialResponse<No> { // Catches a Greeting together with another intent, such as Order
        // Greet the user and proceed with the order in the same turn
        raise(it, it.secondaryIntent)
    }
    onPartialResponse<Yes> { // Catches a Greeting together with another intent, such as Order
        // Greet the user and proceed with the order in the same turn
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


