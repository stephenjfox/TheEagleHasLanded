package model.worker

import java.net.Socket
import java.util.*

object HttpWorkDistributor {

    private val baton = Object() // dud object

    private val remoteWorkersAndCompletion: HashMap<Int, Pair<Boolean, Socket>> = HashMap()
        get() {
            // This might be inappropriate, but how else would I lock it down?
            return synchronized(baton, { field })
        }

    fun registerWorker(assignment: Int, socketConnection: Socket): Int {
        // this object has the key for the Triple Generator, should it be necessary
        remoteWorkersAndCompletion[assignment] = Pair(false, socketConnection)
        // for now, give the batch number back
        return assignment
    }

}