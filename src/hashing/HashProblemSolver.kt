package hashing

import com.fox.StringExtension
import com.fox.io.log.ConsoleColor
import com.fox.io.log.ConsoleLogger
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

    fun crackHash() {
        println("Started at ${LocalDateTime.now()}")
        println("Hash to find: ${Companion.hashToFind}")
        val foursAndFives = seedProblemSolving()

        val workers = WorkerFactory.findWorkers("thread")

        ConsoleLogger.writeLine(StringExtension.wrappedIn("Workers", '-', 10))
        workers.forEach { println(it) }
        println("\n---------------------------------\n")

        dispatchWorkers(Companion.hashMatcher, workers, foursAndFives)

        awaitCompletion()
        println("Ended at ${LocalDateTime.now()}")
    }

    fun seedProblemSolving(): Pair<List<String>, List<String>> {
        val filteredFoursAndFives = WordsPreProcessor.getFilteredWordPartitionsBy(0, 300) { s -> s.length == 4 }

        val (fours, fives) = wordsToStringsListsPair(filteredFoursAndFives) // and repeat

        WordGenerator.seedGenerator(fours)
        WordGenerator.maintainReferenceTo(fives)

        return Pair(fours, fives)
    }

    private fun wordsToStringsListsPair(filteredFoursAndFives: Pair<List<Word>, List<Word>>): Pair<List<String>, List<String>> {
        return Pair(filteredFoursAndFives.first // stream the words
                .map({ it.component2() }), // into Strings
                filteredFoursAndFives.second.map { it.component2() }) // rinse
    }

    private fun dispatchWorkers(hashPredicate: (String) -> Boolean,
                                workers: List<Worker>,
                                foursAndFives: Pair<List<String>, List<String>>) {

        workers.forEachIndexed { i, worker ->
            worker.receiveBatch(hashPredicate, i, foursAndFives.second)
        }
    }

    private fun awaitCompletion() {
        try {
            println("Main going to sleep until the job is done")
            Thread.sleep(java.lang.Long.MAX_VALUE)
        } catch (e: InterruptedException) {
            ConsoleLogger.writeLine("Main thread was interrupted. Time to check mail", ConsoleColor.CYAN)

            val hub = MessageHub.getLocalMessageHub()
            if (hub is LocalBulletinBoard) {
                val nextMessage = hub.getNextMessage()
                System.err.println("The message is: $nextMessage")

                val fsHub = MessageHub.getFileMessageHub()
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

    companion object {
        val hashToFind: String = "FAB5B1537807FF40FE49B17DBB476136AC06BBA884C0706926563A10F43B80AE".toLowerCase()
        val hashMatcher: (String) -> Boolean = { s ->
            val preppedMessageDigest = HashingUtilities.getPreppedMessageDigest(s)
            val toString = HashingUtilities.stringBuilderFromMessageDigest(preppedMessageDigest).toString()
            val equals = Companion.hashToFind.equals(toString)
            if (equals) {
                val messageHub = MessageHub.getFileMessageHub()
                messageHub.writeMessage("HashProblemSolverKt", toString)
                messageHub.writeMessage("TimeStamp", LocalDateTime.now().toString())
            }
            equals
        }
    }
}