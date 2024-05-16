package furhatos.app.isiser.nlu

import com.sun.tracing.Probe
import furhat.libraries.standard.NluLib
import furhatos.app.isiser.setting.EnumSubjectRejoinders
import furhatos.flow.kotlin.raise
import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.ListEntity
import furhatos.nlu.common.*
import furhatos.nlu.common.Number
import furhatos.util.Language



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
// Yes() is built-in

// -------------------------------

class Case1 : Intent {
    var rejoinder: EnumSubjectRejoinders

    // Primary constructor accepting an Intent and initializing rejoinder based on the intent
    constructor() {
        rejoinder = EnumSubjectRejoinders.OFF_TOPIC
    }
    constructor(intent: Intent) : this() {
        rejoinder = when (intent) {
            is NluLib.IAmDone -> EnumSubjectRejoinders.I_AM_DONE
            is AnswerFalse -> EnumSubjectRejoinders.ANSWER_FALSE
            is AnswerTrue -> EnumSubjectRejoinders.ANSWER_TRUE

            is AnswerMarked -> EnumSubjectRejoinders.ANSWER_MARKED

            is RejoinderAgreed -> EnumSubjectRejoinders.REJOINDER_AGREED
            is RejoinderDisagreed -> EnumSubjectRejoinders.REJOINDER_DISAGREED

            is No -> EnumSubjectRejoinders.DENIAL
            is Disagree -> EnumSubjectRejoinders.DENIAL

            is Probe -> EnumSubjectRejoinders.PROBE
            is ElaborationRequest -> EnumSubjectRejoinders.ELABORATION_REQUEST
            is RequestRepeat -> EnumSubjectRejoinders.REPEAT_REQUEST
            is Wait -> EnumSubjectRejoinders.TIME_REQUEST

            is Maybe -> EnumSubjectRejoinders.NON_COMMITTAL
            is DontKnow -> EnumSubjectRejoinders.NON_COMMITTAL
            is Backchannel -> EnumSubjectRejoinders.BACKCHANNEL

            is Agree -> EnumSubjectRejoinders.ASSENT
            is Yes -> EnumSubjectRejoinders.ASSENT
            else -> EnumSubjectRejoinders.OFF_TOPIC
        }
    }
    // Secondary constructor accepting an EnumSubjectRejoinders and initializing rejoinder to it
    constructor(e: EnumSubjectRejoinders) : this() {
        rejoinder = e
    }
}
class Case2  (val  e: EnumSubjectRejoinders = EnumSubjectRejoinders.OFF_TOPIC) : Intent(){
    var rejoinder: EnumSubjectRejoinders = e
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