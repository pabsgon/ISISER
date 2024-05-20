package furhatos.app.isiser.nlu

import com.sun.tracing.Probe
import furhat.libraries.standard.NluLib
import furhatos.app.isiser.setting.EnumRejoinders
import furhatos.nlu.Intent
import furhatos.nlu.common.*


//Greetings and beyond are defined in resources/furhatos.app.isiser.nlu
class Greetings: Intent()
/*              ALL INTENTS USED                            */
// NluLib.IAmDone is built-in
class AnswerFalse: Intent()
class AnswerTrue: Intent()
class AnswerMarked: Intent()
class RejoinderAgreed: Intent()
class RejoinderDisagreed: Intent()
// No() is built-in
class Disagree: Intent()
class Probe: Intent()
class ElaborationRequest: Intent()
// RequestRepeat() is built-in
// Wait() is built-in
// Maybe() is built-in
// DontKnow() is built-in
class Backchannel: Intent()
class Agree: Intent()
class MeReady: Intent()
// Yes() is built-in

// -------------------------------

class AllIntents : Intent {
    var rejoinder: EnumRejoinders

    // Primary constructor accepting an Intent and initializing rejoinder based on the intent
    constructor() {
        rejoinder = EnumRejoinders.OFF_TOPIC
    }
    constructor(intent: Intent) : this() {
        rejoinder = when (intent) {
            is NluLib.IAmDone -> EnumRejoinders.I_AM_DONE
            is MeReady -> EnumRejoinders.ME_READY
            is AnswerFalse -> EnumRejoinders.ANSWER_FALSE
            is AnswerTrue -> EnumRejoinders.ANSWER_TRUE

            is AnswerMarked -> EnumRejoinders.ANSWER_MARKED

            is RejoinderAgreed -> EnumRejoinders.REJOINDER_AGREED
            is RejoinderDisagreed -> EnumRejoinders.REJOINDER_DISAGREED

            is No -> EnumRejoinders.DENIAL
            is Disagree -> EnumRejoinders.DENIAL

            is Probe -> EnumRejoinders.PROBE
            is ElaborationRequest -> EnumRejoinders.ELABORATION_REQUEST

            is Maybe -> EnumRejoinders.NON_COMMITTAL
            is DontKnow -> EnumRejoinders.NON_COMMITTAL
            is Backchannel -> EnumRejoinders.BACKCHANNEL

            is Agree -> EnumRejoinders.ASSENT
            is Yes -> EnumRejoinders.ASSENT
            else -> EnumRejoinders.OFF_TOPIC
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