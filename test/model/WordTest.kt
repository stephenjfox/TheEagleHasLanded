package model

import org.junit.Assert
import org.junit.Test as test
/**
 * Created by stephen on 11/13/15.
 */
class WordTest {
    @test fun basicInit() {
        val testWord = "test value"
        val test = Word(rank = 10, word = testWord)
        Assert.assertNotNull(test)
        Assert.assertArrayEquals(testWord.toCharArray(), test.word.toCharArray())
        Assert.assertEquals("Everything should be equal", testWord, test.word)
    }
}