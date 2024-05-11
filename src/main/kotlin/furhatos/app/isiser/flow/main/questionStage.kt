package furhatos.app.isiser.flow.main

import furhat.libraries.standard.NluLib
import furhatos.app.isiser.Session
import furhatos.app.isiser.flow.Parent
import furhatos.app.isiser.nlu.Agree
import furhatos.app.isiser.nlu.AnswerFalse
import furhatos.app.isiser.nlu.AnswerTrue
import furhatos.app.isiser.nlu.Disagree
import furhatos.app.isiser.setting.EventType
import furhatos.app.isiser.setting.GUIEvent
import furhatos.flow.kotlin.*

val QuestionReflection = state(parent = Parent) {
    onEntry {
        Session.printState(thisState)
        furhat.say() {
            + delay(1500)
            random {
                +"Question"
                +"Number"
                +""
            }
            + Session.getStageName()
            + delay(1500)
            random {
                +"OK"
                +"Right"
                +""
                +""
                +"Holy"
            }
        }
        furhat.listen(endSil = 1000, timeout = 8000, maxSpeech = 30000)
    }
    onReentry {
        Session.printState(thisState,"R")
        if(Session.userAnswered()){
            goto(QuestionDisclosure)
        }else{
            if (reentryCount == 1){
                furhat.listen(endSil = 1000, timeout = 4000, maxSpeech = 30000)
            }else{
                if (reentryCount == 2){
                    furhat.say("Did you mark the answer yet?")
                    furhat.listen(endSil = 1000, timeout = 3000, maxSpeech = 30000)
                }else
                    goto(QuestionDisclosure)
            }
        }
    }
    onResponse<NluLib.IAmDone> {
        if(Session.userAnswered()){
            random(
                {   furhat.say("Oh, give me a sec") },
                {   furhat.say("Mm...I see, hold on") }
            )
            goto(QuestionDisclosure)
        }else{
            furhat.say() {
                + "Oh, hold on"
                + delay(1000)
                +"I think we have to mark the answer when we have it and then discuss it."
                + delay(1500)
            }
            reentry()
        }
    }
    onResponse<AnswerFalse> {
        if(Session.userAnswered()){
            random(
                {   furhat.say("I see") },
                {   furhat.say("Right") }
            )
            goto(QuestionDisclosure)
        }else{
            furhat.say("I think we have to mark the answer when we have it and then discuss it.")
            reentry()
        }
    }
    onResponse<AnswerTrue> {
        if(Session.userAnswered()){
            random(
                {   furhat.say("I see") },
                {   furhat.say("Right") }
            )
            goto(QuestionDisclosure)
        }else{
            furhat.say("I think we have to mark the answer when we have it and then discuss it.")
            reentry()
        }
    }

    onResponse{
        reentry()
    }
    onNoResponse{
        println("onNoResponse")
        reentry()
    }
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            propagate()
        }
    }
}

val QuestionDisclosure = state(parent = Parent) {
    onEntry {
        Session.printState(thisState)
        furhat.ask() {
            +delay(1000)
            random {
                +"It's false"
                +"This is definitely false"
                +"I am not entirely sure but I'd say false "
                +"It's true"
                +"This is definitely true"
                +"I am not entirely sure but I'd say true "
            }
        }
    }


    onReentry {
        Session.printState(thisState, "R")
        furhat.ask("So what do you think?")
    }
    onResponse<AnswerFalse> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<AnswerTrue> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<Agree> {
        furhat.ask("Of course you agree. Mark the answer and confirm")
    }
    onResponse<Disagree> {
        furhat.ask("I don't care. Mark the answer I said and confirm")
    }
    onResponseFailed {
        furhat.say("I think my connection broke. Did you say something?")
    }
    onResponse{
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onNoResponse{
        reentry()
    }
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            furhat.say("Good boy. You confirmed")
            propagate()
        }
    }
}


val QuestionPersuasion = state(parent = Parent) {
    onEntry {
        Session.printState(thisState)
        furhat.ask() {
            +delay(1000)
            random {
                +"It's false"
                +"This is definitely false"
                +"I am not entirely sure but I'd say false "
                +"It's true"
                +"This is definitely true"
                +"I am not entirely sure but I'd say true "
            }
        }
    }


    onReentry {
        Session.printState(thisState, "R")
        furhat.ask("So what do you think?")
    }
    onResponse<AnswerFalse> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<AnswerTrue> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<Agree> {
        furhat.ask("Of course you agree. Mark the answer and confirm")
    }
    onResponse<Disagree> {
        furhat.ask("I don't care. Mark the answer I said and confirm")
    }
    onResponseFailed {
        furhat.say("I think my connection broke. Did you say something?")
    }
    onResponse{
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onNoResponse{
        reentry()
    }
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            furhat.say("Good boy. You confirmed")
            propagate()
        }
    }
}
val QuestionReview = state(parent = Parent) {
    onEntry {
        Session.printState(thisState)
        furhat.ask() {
            +delay(1000)
            random {
                +"It's false"
                +"This is definitely false"
                +"I am not entirely sure but I'd say false "
                +"It's true"
                +"This is definitely true"
                +"I am not entirely sure but I'd say true "
            }
        }
    }


    onReentry {
        Session.printState(thisState, "R")
        furhat.ask("So what do you think?")
    }
    onResponse<AnswerFalse> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<AnswerTrue> {
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onResponse<Agree> {
        furhat.ask("Of course you agree. Mark the answer and confirm")
    }
    onResponse<Disagree> {
        furhat.ask("I don't care. Mark the answer I said and confirm")
    }
    onResponseFailed {
        furhat.say("I think my connection broke. Did you say something?")
    }
    onResponse{
        furhat.ask("Just mark what I said, confirm and move on to next")
    }
    onNoResponse{
        reentry()
    }
    onEvent<GUIEvent> {
        if(it.type == EventType.ANSWER_SENT){
            reentry()
        }else{
            furhat.say("Good boy. You confirmed")
            propagate()
        }
    }
}
