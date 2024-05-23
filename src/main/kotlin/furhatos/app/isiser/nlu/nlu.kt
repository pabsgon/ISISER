package furhatos.app.isiser.nlu

import furhat.libraries.standard.NluLib
import furhatos.app.isiser.setting.EnumRejoinders
import furhatos.nlu.Intent
import furhatos.nlu.common.*


//Greetings and beyond are defined in resources/furhatos.app.isiser.nlu
class Greetings: Intent()
/*  ALL INTENTS USED */

//    RequestRepeat() is built-in //handled in Parent
class SayItAgain: Intent() // handled in Parent
class TimeRequest: Intent() // handled in Parent
//    Wait() is built-in // handled in Parent
//    NluLib.IAmDone is built-in
class MeReady: Intent()
class ILikeMyAnswer: Intent()
class ILikeYourAnswer: Intent()
class AnswerFalse: Intent()
class AnswerTrue: Intent()
class AnswerMarked: Intent()
//    No() is built-in
class Disagree: Intent()
class RejoinderAgreed: Intent()
class RejoinderDisagreed: Intent()
class Probe: Intent()
class DontUnderstand: Intent()
class ElaborationRequest: Intent()
class Backchannel: Intent()
//    DontKnow() is built-in
//    Maybe() is built-in
class Understand: Intent()
class Agree: Intent()
class OffTopic: Intent()
//    Yes() is built-in

// -------------------------------

class AllIntents : Intent {
    var rejoinder: EnumRejoinders

    // Primary constructor accepting an Intent and initializing rejoinder based on the intent
    constructor() {
        rejoinder = EnumRejoinders.OFF_TOPIC
    }

    constructor(intent: Intent) : this() {
        rejoinder = when (intent) {
            // RequestRepeat ->  (Parent) -> REPEAT_REQUEST
            // SayItAgain -> (Parent) -> REPEAT_REQUEST
            // TimeRequest -> (Parent) -> (handled there)
            // Wait ->  (Parent) -> (handled there)
            // NluLib.IAmDone  ->  (Parent) -> ME_READY
            is MeReady -> EnumRejoinders.ME_READY
            is ILikeMyAnswer -> EnumRejoinders.I_LIKE_MY_ANSWER
            is ILikeYourAnswer -> EnumRejoinders.I_LIKE_YOUR_ANSWER
            is AnswerFalse -> EnumRejoinders.ANSWER_FALSE
            is AnswerTrue -> EnumRejoinders.ANSWER_TRUE
            is AnswerMarked -> EnumRejoinders.ANSWER_MARKED
            is No -> EnumRejoinders.DENIAL
            is Disagree -> EnumRejoinders.DENIAL
            is RejoinderAgreed -> EnumRejoinders.REJOINDER_AGREED //raised by Parent
            is RejoinderDisagreed -> EnumRejoinders.REJOINDER_DISAGREED //raised by Parent
            is Probe -> EnumRejoinders.PROBE
            is DontUnderstand,
            is ElaborationRequest -> EnumRejoinders.ELABORATION_REQUEST
            is Backchannel -> EnumRejoinders.BACKCHANNEL //raised by Parent
            is DontKnow,
            is Maybe  -> EnumRejoinders.NON_COMMITTAL
            is Understand,
            is Agree,
            is Yes -> EnumRejoinders.ASSENT
            is OffTopic -> EnumRejoinders.OFF_TOPIC
            else -> EnumRejoinders.OFF_TOPIC //raised by Parent
        }
    }

    // Secondary constructor accepting an EnumSubjectRejoinders and initializing rejoinder to it
    constructor(e: EnumRejoinders) : this() {
        rejoinder = e
    }
}
class Case2  (val  e: EnumRejoinders = EnumRejoinders.OFF_TOPIC) : Intent(){
    var rejoinder: EnumRejoinders = e
}

/*
class BuyFruit(var fruits : FruitList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@fruits", "I want @fruits", "I would like @fruits", "I want to buy @fruits")
    }
}

class RequestOptions: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("What options do you have?",
            "What fruits do you have?",
            "What are the alternatives?",
            "What do you have?")
    }
}

class FruitList : ListEntity<QuantifiedFruit>()

class QuantifiedFruit(
    var count : Number? = Number(1),
    var fruit : Fruit? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @fruit", "@fruit")
    }

    override fun toText(): String {
        return generate("$count " + if (count?.value == 1) fruit?.value else "${fruit?.value}" + "s")
    }
}

 */