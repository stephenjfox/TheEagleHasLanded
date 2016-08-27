package model.worker

import com.fox.io.log.ConsoleColor
import com.fox.io.log.ConsoleLogger
import message.MessageHub
import model.WordGenerator

/**
 * Worker that spawns a thread to accomplish the task
 * Created by stephen on 11/12/15.
 */
class ThreadedWorker(private val toSignal: Thread, private val assignedID: Int) : Worker {

    var fiveLetterWords: Array<String> = emptyArray()

    private var testingPredicate: (String) -> Boolean = { false }

    private var innerThread = lazy {
        getWorkingThread()
    }

    // From Worker
    override fun hasCompleted(): Boolean = innerThread.value.state.let {
        it == Thread.State.NEW || it == Thread.State.TERMINATED
    }

    /**
     * To be called only after checking [hasCompleted]
     */
    override fun receiveBatch(wordTest: (String) -> Boolean,
                              batchAssignment: Int,
                              fiveLetterWordBank: List<String>) {
        this.testingPredicate = wordTest
        this.fiveLetterWords = fiveLetterWordBank.toTypedArray()

        if (innerThread.value.state == Thread.State.TERMINATED) {
            innerThread = lazy { getWorkingThread() }
        }

        innerThread.value.start()
        println("${this.toString()} here. I've started my worker")
    }

    override fun toString(): String {
        return "ThreadedWorker #$assignedID"
    }

    /**
     * Gets a thread (that will be lazily initialized) to check all permutations
     * of the four and five letter words.
     * Four letter words are retrieved from the (thread-safe) [WordGenerator]
     *
     * Lowers memory usage, by using Int reference pointers instead of String heap references
     * then index accesses them to combine them with Kotlin String interpolated formatting.
     */
    private fun getWorkingThread(): Thread {
        return object : Thread({

            while (WordGenerator.hasMore) {
                val triple = WordGenerator.getNextPermutation()

                for (i in fiveLetterWords.indices) {
                    for (j in fiveLetterWords.indices) {

                        val testVal = "${triple.first} ${fiveLetterWords[i]} ${triple.second} ${fiveLetterWords[j]} ${triple.third}"

                        if (testingPredicate(testVal)) {
                            notifyAndRecord(testVal)
                        }
                    }
                }
            }

            println("Thread #${this.assignedID} finished the job")
        }) {
            override fun start() {
                super.start()
                println("Thread #${this.id} starting the work")
            }
        }
    }

    private fun notifyAndRecord(successfulFind: String) {
        ConsoleLogger.writeLine("The matching phrase is: $successfulFind", ConsoleColor.BLUE)
        MessageHub.getLocalMessageHub().writeMessage(this, successfulFind)
        MessageHub.getFileMessageHub().writeMessage(this, successfulFind)
        toSignal.interrupt()
    }
}