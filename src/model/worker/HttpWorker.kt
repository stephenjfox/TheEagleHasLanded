package model.worker

import java.net.Socket

class HttpWorker(val port : Int, val ipAddress: String) : Worker {

    private val socketConnection = Socket(ipAddress, port)
    private var assignedBatch : Int = 0

    override fun hasCompleted() : Boolean {
        return socketConnection.isConnected // SCRATCH: probably won't stay like this
    }

    override fun receiveBatch(wordTest : (String) -> Boolean, batchAssignment: Int, fiveLetterWordBank : List<String>) {
        assignedBatch = HttpWorkDistributor.registerWorker(batchAssignment, this.socketConnection)
    }

}
