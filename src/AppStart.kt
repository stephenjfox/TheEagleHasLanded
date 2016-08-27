import hashing.HashProblemSolver
import hashing.HashingDistribution
import model.Word
import org.json.JSONArray
import org.json.JSONObject

/**
 * Starting point for the multi-threaded SHA256 code matcher
 * Created by stephen on 11/12/15.
 */

fun main(args: Array<String>) {

    val mainThread = Thread.currentThread()
    val serverThread = Thread({
        HashingDistribution.serviceRequests(mainThread)
    })

    serverThread.start()
    HashProblemSolver().crackHash()

}

