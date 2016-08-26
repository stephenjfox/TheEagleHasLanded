package conceptproofs

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by stephen on 11/12/15.
 */
object ThreadMessagePOC {

    val messageBox = AtomicReference<String>()
    private var canCheckBox : AtomicBoolean = AtomicBoolean()


    fun run() : Unit {
        val waiter: Waiter = Waiter(Thread.currentThread())
        val interrupter = Thread({
            for(i in 1 .. 3) {
                println("${Thread.currentThread().id}: Waiting ${3 - i}")
                Thread.sleep(1000)
            }
            messageBox.set("The new message")
            waiter.interrupt()
//            Thread.currentThread().join()
        })

        waiter.start()
        interrupter.start()

        try {
            Thread.currentThread().join()
        } catch(e : InterruptedException) {
            println("${Thread.currentThread().id}: Can check the message: ${canCheckBox.get()}")
            println("${Thread.currentThread().id}: Message reads, \"${messageBox.get()}\"")

            println("${Thread.currentThread().id}: Active threads = ${Thread.activeCount()}")
        }
    }

    class Waiter(toNotify: Thread) : Thread({
        try {
            Thread.sleep(Long.MAX_VALUE)
        } catch(e : InterruptedException) {
            // exceptional things
            canCheckBox.set(true)
        } finally {
            toNotify.interrupt()
            println("${Thread.currentThread().id}: The waiter thread was interrupted")
//            Thread.currentThread().join()
        }
    })
}