package model.worker

/**
 * Created by stephen on 11/12/15.
 */
interface Worker {
    fun hasCompleted(): Boolean
    fun receiveBatch(wordTest: (String) -> Boolean, batchAssignment: Int, fiveLetterWordBank: List<String>)
}