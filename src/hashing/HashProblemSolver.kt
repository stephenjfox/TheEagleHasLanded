package hashing

import com.fox.io.log.ConsoleColor
import com.fox.io.log.ConsoleLogger
import com.google.common.collect.Lists
import conceptproofs.HashingPOC
import config.WordsPreProcessor
import message.FlatFileTextWriter
import message.LocalBulletinBoard
import message.MessageHub
import model.Word
import model.WordGenerator
import model.WorkerFactory
import model.worker.Worker
import java.time.LocalDateTime

/**
 * Created by stephen on 11/13/15.
 */
class HashProblemSolver {

    val hashMatcher : (String) -> Boolean = { s ->
        val preppedMessageDigest = HashingUtilities.getPreppedMessageDigest(s)
        val toString = HashingPOC.getStringBuilderFromMessageDigest(preppedMessageDigest).toString()
        val equals = hashToFind.equals(toString)
        if (equals) {
            MessageHub.getFileBasedMessageHub().writeMessage("HashProblemSolverKt", toString)
            MessageHub.getFileBasedMessageHub().writeMessage("TimeStamp", LocalDateTime.now().toString())
        }
        equals
    }

    /*val dummyMessage = "that which with there have"
    val dummyHashAsString : String*/

    constructor() {
        /*val mdHashMan = HashingUtilities.getMessageDigest()
        val bytes = dummyMessage.toByteArray()
        mdHashMan.update(bytes)

        dummyHashAsString = HashingPOC.getStringBuilderFromMessageDigest(mdHashMan).toString()*/
    }

    val hashToFind: String = "FAB5B1537807FF40FE49B17DBB476136AC06BBA884C0706926563A10F43B80AE".toLowerCase()

    fun runIt() : Unit {
        println("Started at ${LocalDateTime.now()}")
        println("Hash to find: $hashToFind")
        val (foursAndFives, fourLettersList) = seedProblemSolving()

        val workers = WorkerFactory.findWorkers("thread")

        println("\n---------------------------------\n")
        workers.forEach{ println(it) }
        println("\n---------------------------------\n")

        val partitionsOfFour = Lists.partition(fourLettersList, fourLettersList.size / workers.size + 1)

        dispatchWorkers(hashMatcher, workers, foursAndFives, partitionsOfFour)

        awaitCompletion()
        println("Ended at ${LocalDateTime.now()}")
    }

    fun seedProblemSolving() : Pair<Pair<List<String>, List<String>>, List<String>> {
        val filteredFoursAndFives = WordsPreProcessor.getFilteredWordPartitionsBy(0, 300) { s -> s.length == 4 }

        val foursAndFives = wordsToStringsListsPair(filteredFoursAndFives) // and repeat

        val fourLettersList = foursAndFives.first

        WordGenerator.seedGenerator(fourLettersList)
        WordGenerator.seedGenerator(fourLettersList)
        WordGenerator.maintainReferenceTo(foursAndFives.second)

        return Pair(foursAndFives, fourLettersList)
    }

    private fun wordsToStringsListsPair(filteredFoursAndFives : Pair<List<Word>, List<Word>>) : Pair<List<String>, List<String>> {
        return Pair(filteredFoursAndFives.first // stream the words
                .map({ it.component2() }), // into Strings
                filteredFoursAndFives.second.map { it.component2() }) // rinse
    }

    private fun dispatchWorkers(hashPredicate : Function1<String, Boolean>,
                                workers : List<Worker>,
                                foursAndFives : Pair<List<String>, List<String>>, partitionsOfFour : List<List<String>>) {
        var i = 0
        val partitionCount = partitionsOfFour.size
        println("Partition size = $partitionCount")
        while (i < partitionCount) {
            val partition = partitionsOfFour[i]
            workers[i].receiveBatch(
                    hashPredicate, // predicate
                    i, // particular partition
                    foursAndFives.second) // all the five-lengths that one can handle
            i++
        }
    }

    private fun awaitCompletion() {
        try {
            System.out.println("Main going to sleep until the job is done")
            Thread.sleep(java.lang.Long.MAX_VALUE)
        } catch (e : InterruptedException) {
            ConsoleLogger.writeLine("Main thread was interrupted. Time to check mail", ConsoleColor.CYAN)

            val hub = MessageHub.getLocalMessageHub()
            if (hub is LocalBulletinBoard) {
                val nextMessage = hub.getNextMessage()
                System.err.println("The message is: $nextMessage")

                val fsHub = MessageHub.getFileBasedMessageHub()
                if (fsHub is FlatFileTextWriter) {
                    hub.writeMessage("HashProblemSolver", "The message is $nextMessage}")
                    fsHub.bufferedOutputStream.flush()
                    fsHub.bufferedOutputStream.close()
                    println("Wrote message to a file")
                }
            }
            System.exit(0)
        }
    }
}