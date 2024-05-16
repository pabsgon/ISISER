package furhatos.app.isiser.setting

fun String.wordCount(): Int {
    // Split the string by whitespace or directly followed punctuation
    return this.trim().split(Regex("[\\s,.:;?!]+")).filter { it.isNotEmpty() }.size
}

fun seemsLikeBackchannel(text: String, len: Int, maxWords: Int, maxLen: Int):Boolean{
    val wordCount: Int =  text.wordCount()
    return (wordCount <= maxWords && len<= maxLen)
}
fun seemsLikeBackchannel(text: String, len: Int):Boolean{
    return seemsLikeBackchannel(text, len, MAX_NUM_BACKCHANNEL_WORDS, MAX_BACKCHANNEL_LENGTH)
}