package furhatos.app.isiser.flow.main

import furhat.libraries.standard.NluLib
import furhatos.app.isiser.App
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.handlers.SessionHandler
import furhatos.app.isiser.nlu.*
import furhatos.app.isiser.setting.seemsLikeBackchannel
import furhatos.app.isiser.setting.wordCount
import furhatos.event.senses.SenseSpeech
import furhatos.flow.kotlin.*
import furhatos.nlu.Intent
import furhatos.nlu.common.*

val Testing = state(parent = Parent) {
    var session: SessionHandler = App.getSession()
    var speechDuration = 0
    onEntry {
        furhat.ask("Ok, let's test. Say whatever you want.")
        //furhat.listen(endSil = 1000, timeout = 8000, maxSpeech = 30000)
    }
    onReentry {
        furhat.ask("I'm listening.")
    }
    onEvent<SenseSpeech> {
        speechDuration = it.length
        println("You said something I couldn't understand in $speechDuration milliseconds.")
    }
    onResponse<NluLib.IAmDone> {
            furhat.ask("You said you finished")
    }
    onResponse<AnswerFalse> {
        furhat.ask("You said it is false")
    }
    onResponse<AnswerTrue> {
        furhat.ask("You said it is true")
    }
    onResponse<Yes> {
        furhat.ask("You assented")
    }

    onResponse<No> {
        furhat.ask("You denied")
    }
        onResponse<Agree> {
        furhat.ask("You agreed")
    }
    onResponse<Disagree> {
        if (it.intent.intentName != "Disagree")
            furhat.say("Oh...")
        println("XXXDisagree [$it.intent.intentName ]")
        furhat.ask("You disagreed")
    }
    onResponse<DontKnow> {
        furhat.ask("That was non-committal")
    }
    onResponse<RequestRepeat> {
        furhat.ask("You want me to repeat")
    }
    onResponse<RejoinderAgreed> {
        furhat.ask("That's a rejoinder I agree with")
    }
    onResponse<RejoinderDisagreed> {
        furhat.ask("That's a rejoinder I don't agree with")
    }
    onResponse<Greeting> {
        furhat.ask("Hello to you too.")
    }
    onResponse<ElaborationRequest> {
        furhat.ask("You want me to elaborate.")
    }
    onResponse<Backchannel> {
        furhat.ask("You just backchanneled.")
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
        if(seemsLikeBackchannel(it.text, it.speech.length)){
            raise(Backchannel())
        }else{
            furhat.ask("You said something in about ${it.speech.length} milliseconds. ")
        }
    }
    onNoResponse {
        reentry()
    }
    onResponseFailed {
        furhat.ask("The connection failed... Can you repeat that?")
    }
}


