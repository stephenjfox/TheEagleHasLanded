package model

import com.fox.io.log.ConsoleColor
import com.fox.io.log.ConsoleLogger
import java.util.*
import com.fox.io.log.ConsoleLogger.writeLine as printColored

/**
 * Created by stephen on 11/14/15.
 */
object WordGenerator {
    private val list : MutableList<String> = ArrayList()
    private val listOfFailurePoints: MutableList<Triple<Int, Int, Int>> = ArrayList()
    private var first : Int = 0
    private var second : Int = 0
    private var third : Int = 0
    private var failCursor = 0

    var hasMore : Boolean = true
        get() {
           return field
        }
        set (new) {
            field = new
        }

    var listOfFives : List<String> = emptyList()
        get() {
            return field
        }
        set (new) {
            field = new
        }

    fun seedGenerator(wordList : List<String>) : Unit {
        list.clear()
        list.addAll(wordList)
    }

    fun getNextPermutation() : Triple<String, String, String> {
        synchronized(this, {
            checkAndIncrementCursors()

            val retTriple = Triple(list[first++], list[second], list[third])

            return retTriple
        })
    }

    fun getNextPermutationBatch(count : Int) : List<Triple<String, String, String>> {

        synchronized(this, {
            val retList = ArrayList<Triple<String, String, String>>()

            listOfFailurePoints.add(Triple(first, second, third))

            ConsoleLogger.warning("Batching is scary, marking potential failure point ${listOfFailurePoints.last()}")

            for(i in 0 until count) {
                checkAndIncrementCursors()
                retList.add(Triple(list[first++], list[second], list[third]))
            }

            return retList
        })
    }

    private fun checkAndIncrementCursors() {
        if (first >= list.size) {
            first = 0
            second += 1
        }
        if (second >= list.size) {
            second = 0
            third += 1
        }
        if (third >= list.size) {
            third = 0
            hasMore = false
            printColored("The hashing job should be done", ConsoleColor.CYAN, 2)
        }
        if (!hasMore) {
            restartFromFailure()
        }
    }

    private fun restartFromFailure() {
        if (listOfFailurePoints.isEmpty()) {
            hasMore = true
            ConsoleLogger.warning("Restarting the process. Sentence not found")
        }
        else {
            val triple = listOfFailurePoints[failCursor]
            ConsoleLogger.warning("Restarting from failurePoint $triple")
            first = triple.first
            second = triple.second
            third = triple.third
            hasMore = true
        }
    }

    fun maintainReferenceTo(listOfFiveLetterWords : List<String>) {
        listOfFives = listOfFiveLetterWords
    }
}