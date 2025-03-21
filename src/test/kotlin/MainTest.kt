import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
class MainTest {

    @Test
    fun `guessedNumbers toimii kun syöte on validi`() {
        assertEquals(listOf(1,2,3,4,5,6,7), guessedNumbers("1,2,3,4,5,6,7"))
        assertEquals(listOf(1,2,3,4), guessedNumbers("    1   ,   2   ,  3  , 4        "))
    }

    @Test
    fun `guessedNumbers palauttaa null virheellisestä syötteestä`() {
        assertNull(guessedNumbers(null))
        assertNull(guessedNumbers("1,null,2"))
        assertNull(guessedNumbers(""))
    }

    @Test
    fun `isLegalGuess palauttaa true jos arvaus on validi`() {
        assertTrue(isLegalGuess(listOf(1,2,3,4,5,6,7)))
        assertTrue(isLegalGuess(listOf(40,39,38,37,36,35,34)))
    }

    @Test
    fun `isLegalGuess palautta false virheellisistä syötteistä`(){
        assertFalse(isLegalGuess(listOf(1,2,3,4,5,6,7,8)))
        assertFalse(isLegalGuess(listOf(1,2,3,4,5,6)))
        assertFalse(isLegalGuess(listOf(1,2,3,4,5,6,41)))
        assertFalse(isLegalGuess(null))
    }

    @Test
    fun `equalsCount palauttaa oikean määrän yhteisiä numeroita tai -1 jos jompikumpi on null`() {
        val secret = listOf(1,2,3,4,5,6,7)
        assertEquals(1, equalsCount(secret, listOf(1,31,8,9,10,11,12)))
        assertEquals(2, equalsCount(secret, listOf(1,2,8,9,10,11,12)))
        assertEquals(1, equalsCount(secret, listOf(1,1,1,1,1,1,1)))
        assertEquals(-1, equalsCount(secret, null))
        assertEquals(-1, equalsCount(null, listOf(1,2,3,4,5,6,7) ))
    }

}