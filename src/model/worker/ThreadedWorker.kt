package model.worker

import com.fox.io.log.ConsoleColor
import com.fox.io.log.ConsoleLogger
import message.MessageHub
import model.WordGenerator

/**
 * Worker that spawns a thread to accomplish the task
 * Created by stephen on 11/12/15.
 */
class ThreadedWorker(val toSignal : Thread, val assignedID: Int) : Worker {

    // scratch: I think this could be a ThreadLocal, or something. Doesn't feel write
    var fourLetterWords : Array<String> = emptyArray()
    var fiveLetterWords : Array<String> = emptyArray()

    private var testingPredicate : (String) -> Boolean = { true }

    private var innerThread = lazy {
        getWorkingThread()
    }

    // From Worker
    override fun hasCompleted() : Boolean =
            innerThread.value.state == Thread.State.NEW ||
            innerThread.value.state == Thread.State.TERMINATED

    /**
     * To be called only after checking ThreadedWorker::hasCompleted
     */
    override fun receiveBatch(wordTest : (String) -> Boolean, batchAssignment : Int, fiveLetterWordBank : List<String>) {
        this.testingPredicate = wordTest
//        this.fourLetterWords = fourLetterWordBank.toTypedArray()
        this.fiveLetterWords = fiveLetterWordBank.toTypedArray()

        if (innerThread.value.state == Thread.State.TERMINATED) {
            innerThread = lazy { getWorkingThread() }
        }

        innerThread.value.start()
        println("${this.toString()} here. I've started my worker")
    }

    override fun toString() : String {
        return "ThreadedWorker #$assignedID"
    }

    /**
     * Gets a thread (that will be lazily initialized) to check all permutations
     * of the four and five letter words. Lowers memory usage, by using Int
     * reference pointers instead of String heap references then index accesses
     * them to combine them with java.lang.String.format()
     */
    private fun getWorkingThread() : Thread {
        return object : Thread({
            /*for (first in fourLetterWords.indices) {
                for (sec in fiveLetterWords.indices) {
                    for (third in fourLetterWords.indices) {
                        for (fourth in fiveLetterWords.indices) {
                            for (fifth in fourLetterWords.indices) {

                                val testVal = String.format("%s %s %s %s %s",
                                        fourLetterWords[first],
                                        fiveLetterWords[sec],
                                        fourLetterWords[third],
                                        fiveLetterWords[fourth],
                                        fourLetterWords[fifth])

                                if (testingPredicate(testVal)) {
                                    notifyAndRecord(testVal)
                                }
                            }
                        }
                    }
                }
            }*/

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

    private fun notifyAndRecord(successfulFind : String) {
        ConsoleLogger.writeLine("The matching phrase is: $successfulFind", ConsoleColor.BLUE)
        MessageHub.getLocalMessageHub().writeMessage(this, successfulFind)
        MessageHub.getFileBasedMessageHub().writeMessage(this, successfulFind)
        toSignal.interrupt()
    }
}