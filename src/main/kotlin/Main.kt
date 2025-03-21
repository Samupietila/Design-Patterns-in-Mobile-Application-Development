fun guessedNumbers(s: String?): List<Int>? {
    try {
        val fixedList = s?.split(",")?.map { it.trim().toInt() }
        return fixedList
    } catch (e: NumberFormatException) {
        return null
    }
    """
        Voisi korvata näin:
        return try {
        s?.split(",")?.map { it.trim().toInt() }
    } catch (e: NumberFormatException) {
        null
    }
    """
}
fun isLegalGuess(guess: List<Int>?) : Boolean {
    if (guess == null) return false
    if (guess.size != 7) return false
    if (guess.any { it < 1 || it > 40 }) return false
    return true
}

fun equalsCount(secret: List<Int>?, guess: List<Int>?): Int {
    if (secret == null || guess == null) return -1
    return secret.intersect(guess).count()
}
fun main() {
    val secret: List<Int>? = (1..40).shuffled().take(7)
    // ei voi muuttaa koska tarvitaan useamman kerran.
    println(secret)
    println("Tervetuloa komentorivilottoon!!!!")
    do {
        println("Arvaa 7 numeroa väliltä 1-40 pilkulla erotettuna: ")
        val input = readlnOrNull() ?: ""
        val guess = guessedNumbers(input)
        if (!isLegalGuess(guess)) {
            println("Virheellinen syöte, yritä uudelleen")
            continue
            """
                Voisi korvata näin:
        if (!isLegalGuess(guessedNumbers(readlnOrNull() ?: ""))) {
            println("Virheellinen syöte, yritä uudelleen")
            continue
        }
            """
        }
        println("Oikeita numeroita oli ${equalsCount(secret, guess)}")
    } while (input != "quit" && equalsCount(secret, guessedNumbers(input)) != 7)


}