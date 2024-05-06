
package furhat.libraries.standard

import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.util.Language

/**
 * A collection of intents & entities.
 * They are only available in the english language.
 *
 * Examples :
 * ```
 * onResponse<NluLib.IAmDone> {...}
 * ```
 */
object NluLib {
    /**
     *  An entity with all the languages Furhat has the ability to speak.
     *  Updated 2021-07-27
     */
    class SpokenLanguages : EnumEntity() {
        override fun getEnum(lang: Language): List<String> {
            return listOf(
                "Arabic",
                "Catalan",
                "Chinese",
                "Czech",
                "Danish",
                "Dutch",
                "English",
                "Faroese",
                "Finnish",
                "French",
                "German",
                "Greek",
                "Hindi",
                "Icelandic",
                "Italian",
                "Japanese",
                "Korean",
                "Norwegian",
                "Polish",
                "Portuguese",
                "Romanian",
                "Russian",
                "Sami",
                "Spanish",
                "Swedish",
                "Turkish",
                "Welsh"
            )
        }
    }

    /**
     *  An entity with all the languages Furhat has the ability to understand.
     *  Updated 2021-07-27
     */
    class UnderStoodLanguages : EnumEntity() {
        override fun getEnum(lang: Language): List<String> {
            return listOf(
                "Afrikaans",
                "Amharic",
                "Arabic",
                "Armenian",
                "Bahasa Indonesian",
                "Bahasa Melayu",
                "Malaysian",
                "Basque",
                "Bengali",
                "Bulgarian",
                "Catalan",
                "Chinese",
                "Mandarin",
                "Cantonese",
                "Croatian",
                "Czech",
                "Danish",
                "Dutch",
                "English",
                "Faroese",
                "Filipino",
                "Finnish",
                "French",
                "Galician",
                "Georgian",
                "German",
                "Greek",
                "Gujarati",
                "Hebrew",
                "Hindi",
                "Hungarian",
                "Icelandic",
                "Indonesian",
                "Italian",
                "Japanese",
                "Javanese",
                "Kannada",
                "Korean",
                "Khmer",
                "Lao",
                "Latvian",
                "Lithuanian",
                "Malay",
                "Malayalam",
                "Marathi",
                "Nepali",
                "Norwegian",
                "Persian",
                "Polish",
                "Portuguese",
                "Romanian",
                "Russian",
                "Sami",
                "Serbian",
                "Sinhala",
                "Slovak",
                "Slovenian",
                "Spanish",
                "Sundanese",
                "Swahili",
                "Swedish",
                "Tamil",
                "Telegu",
                "Thai",
                "Turkish",
                "Ukranian",
                "Urdu",
                "Vietnamese",
                "Welsh",
                "Zulu"
            )
        }
    }

    /**
     *  An intent for catching when the user is done and wants to move on.
     */
    class IAmDone : Intent() {
        override fun getExamples(lang: Language): List<String> {
            return listOf(
                "I am done",
                "I think I'm done",
                "Done",
                "I am finished",
                "I'm ready",
                "alright I'm done",
                "ok, let's keep going",
                "We can move on",
                "Continue",
                "Did it",
                "It is done",
                "Finished",
                "can we move on",
                "let's move on",
                "let's move to the next",
                "I think they get the point",
                "we get the point"
            )
        }
    }

    /**
     *  An intent for catching when the user wants Furhat to wake up
     */
    class WakeUp : Intent() {
        override fun getExamples(lang: Language): List<String> {
            return listOf(
                "wake up",
                "wakeup",
                "can you wake up",
                "time to wake up",
                "wakey wakey",
                "are you there",
                "Hello are you there",
                "hello is anybody home",
                "I'm back",
                "are you here",
                "are you there",
                "do you hear me",
                "can you hear me",
                "can I talk to you",
                "I want to talk to you",
                "can you wake",
                "are you awake",
                "can you pick up",
                "hello furhat",
                "hello farhat",
                "pick up"
            )
        }
    }

    /**
     *  An entity with positive expressions.
     */
    class PositiveExpression : EnumEntity() {
        override fun getEnum(lang: Language): List<String> {
            return listOf(
                "great",
                "fine:fine,define",
                "happy",
                "nice",
                "fantastic",
                "awesome",
                "good:good,best",
                "amazing",
                "incredible",
                "excellent",
                "fabulous",
                "hilarious",
                "epic",
                "love",
                "excited",
                "exciting",
                "astonished",
                "cool",
                "terrific",
                "cheerful",
                "contented",
                "delighted",
                "ecstatic",
                "elated",
                "glad",
                "joyful",
                "joyous",
                "jubilant",
                "lively",
                "merry",
                "overjoyed",
                "peaceful",
                "pleasant",
                "pleased",
                "thrilled",
                "upbeat",
                "blessed: blessed, blest",
                "blissful",
                "blithe",
                "can't complain",
                "captivated",
                "chipper",
                "chirpy",
                "content",
                "convivial",
                "exultant",
                "gay",
                "gleeful",
                "gratified",
                "intoxicated",
                "jolly",
                "laughing",
                "light",
                "looking good",
                "mirthful",
                "on cloud nine",
                "peppy",
                "perky",
                "playful",
                "sparkling",
                "sunny",
                "tickled",
                "up",
                "alright:alright, all right",
                "splendid",
                "good-looking",
                "wonderful"
            )
        }
    }

    /**
     *  An entity with negative expressions.
     */
    class NegativeExpression : EnumEntity() {
        override fun getEnum(lang: Language): List<String> {
            return listOf(
                "terrible:terrible,parable",
                "bad:bad,dad,worst",
                "horrible",
                "awful",
                "dreadful",
                "atrocious",
                "appalling",
                "shit",
                "sucks",
                "crap",
                "annoying",
                "unacceptable",
                "unprofessional",
                "shabby"
            )
        }
    }

    /**
     *  An Intent for catching positive expressions, including 'not + negative expressions'
     */
    class PositiveReaction(
        val positiveExpression: PositiveExpression? = null,
        val negativeExpression: NegativeExpression? = null
    ) : Intent() {
        override fun getExamples(lang: Language): List<String> {
            return listOf(
                "@positiveExpression",
                "not @negativeExpression",
                "not too @negativeExpression"
            )
        }

        override fun getNegativeExamples(lang: Language): List<String> {
            return listOf(
                "not @positiveExpression",
                "not very @positiveExpression",
                "not too @positiveExpression",
                "not particularly @positiveExpression",
                "@negativeExpression"
            )
        }
    }

    /**
     *  An Intent for catching negative expressions, including 'not + positive expressions'
     */
    class NegativeReaction(
        val positiveExpression: PositiveExpression? = null,
        val negativeExpression: NegativeExpression? = null
    ) : Intent() {
        override fun getExamples(lang: Language): List<String> {
            return listOf(
                "not @positiveExpression",
                "not very @positiveExpression",
                "not too @positiveExpression",
                "not particularly @positiveExpression",
                "@negativeExpression"
            )
        }

        override fun getNegativeExamples(lang: Language): List<String> {
            return listOf(
                "@positiveExpression",
                "not @negativeExpression",
                "not too @negativeExpression"
            )
        }
    }
}