import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.locks.ReentrantLock

class Lotto(val lottoNumbers: List<Int>? = (1..40).shuffled().take(7), val stats: MutableList<Int> = mutableListOf(),  val totalGuesses: Int) {
    val a_lock = ReentrantLock()
    fun check(guess: List<Int>?) : Int? {
        if(guess == null) return -1
        return lottoNumbers?.intersect(guess)?.count()
    }

    fun generateGuesses() {
        val guess: List<Int> = (1..40).shuffled().take(7)
        check(guess)?.let {
            stats.add(it)
        }
    }
    fun henerateGuesses() {
        while (true) {
            a_lock.lock()
            if (stats.size < totalGuesses) {
                val guess: List<Int> = (1..40).shuffled().take(7)
                check(guess)?.let {
                    stats.add(it)
                }
                a_lock.unlock()
            } else {
                a_lock.unlock()
                break
            }
        }
    }
}

fun main() {
    val lotto: Lotto? = Lotto(totalGuesses = 13500000)
    println("Lottonumerot: ${lotto?.lottoNumbers}")
    val randomThreadCount = ThreadLocalRandom.current().nextInt(1, Runtime.getRuntime().availableProcessors())
    val guessesPerThread = lotto?.totalGuesses?.div((randomThreadCount))
    val threads = (1..randomThreadCount).map { Thread {lotto?.henerateGuesses()} }
    if (lotto != null) {
        val time = System.currentTimeMillis()
        threads.forEach {
            println("${it.name} / ${randomThreadCount - 1} starting for ${guessesPerThread} guesses")
            it.start()
        }
        threads.forEach { it.join() }
        println("All joined")
        println("Running time ${System.currentTimeMillis() - time}ms")
        println("Checksum: ${lotto.stats.size}, should be ${lotto.totalGuesses}")
        println("0: ${lotto.stats.count { it == 0 }}")
        println("1: ${lotto.stats.count { it == 1 }}")
        println("2: ${lotto.stats.count { it == 2 }}")
        println("3: ${lotto.stats.count { it == 3 }}")
        println("4: ${lotto.stats.count { it == 4 }}")
        println("5: ${lotto.stats.count { it == 5 }}")
        println("6: ${lotto.stats.count { it == 6 }}")
        println("7: ${lotto.stats.count { it == 7 }}")

    }
}