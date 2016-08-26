package message

import org.json.JSONObject
import java.util.*
import java.util.concurrent.atomic.AtomicReference

val String.Companion.empty : String
    get () {
        return ""
    }

class LocalBulletinBoard : MessageHub {

    private val messageQueue = AtomicReference<ArrayDeque<Pair<Any, String>>>(ArrayDeque())
    private val INDENT_FACTOR = 2

    override fun writeMessage(sender : Any, text : String) {
        synchronized(messageQueue, {
            messageQueue.get().add(Pair(sender, text))
            println("Wrote to memory")
        })
    }

    override fun clearMessages() {
        messageQueue.get().clear()
    }

    fun peekNextMessage() : String {
        val deque = messageQueue.get()
        return safeQueuePeek(deque)
    }

    fun getNextMessage() : String {
        val deque = messageQueue.get()
        return safeQueuePop(deque)
    }

    /**
     * Returns the "head of queue" sender-message pair as a JSON string.
     */
    fun peekNextMessageJSON() : String {
        val deque = messageQueue.get()

        return try {
            val pair = deque.peek()
            JSONObject(pair).toString(INDENT_FACTOR)
        } catch(e : Exception) {
            JSONObject(Pair<Any, String>(object : Any() {}, "default message"))
                    .toString(INDENT_FACTOR)
        }
    }

    /**
     * Returns the "head of queue" sender-message pair as a JSON string.
     */
    fun getNextMessageJSON() : String {
        val deque = messageQueue.get()

        return try {
            val pair = deque.pop()
            JSONObject(pair).toString(INDENT_FACTOR)
        } catch (nse : NoSuchElementException) {
            JSONObject(Pair<Any, String>(object : Any() {}, "default message"))
                    .toString(INDENT_FACTOR)
        }
    }

    private fun safeQueuePeek(queue : Deque<Pair<Any, String>>) : String = try {
        val pair = queue.peekFirst()
        pair.second
    } catch (nse: NoSuchElementException) {
        println("Defaulting PEEK, because the message board is empty")
        String.empty
    }

    private fun safeQueuePop(queue : Deque<Pair<Any, String>>) : String = try {
        val pair = queue.pop()
        pair.second
    } catch (nse : NoSuchElementException) {
        println("Defaulting POP, because the message board is empty")
        String.empty
    }

}