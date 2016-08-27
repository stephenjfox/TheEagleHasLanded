package hashing

import com.fox.io.log.ConsoleColor
import com.fox.io.log.ConsoleLogger
import message.MessageHub
import model.WordGenerator
import org.json.JSONArray
import org.json.JSONObject
import spark.Spark.get
import spark.Spark.post
import java.util.*

/**
 * Hashing Server portion, for distributing the work to (in our case) NodeJS worker instances
 * Created by stephen on 11/18/15.
 */
object HashingDistribution {

    fun serviceRequests(toInterrupt: Thread) {
        get("/hello", { req, res -> "Hello, world: length = ${req.contentLength()}" })
        get("/work", { req, res -> answerWorkRequest() })
        get("/work/:count", { req, res ->
            val count = req.params(":count").toInt()
            answerWorkRequest(count)
        })
        post("/work", "application/json", { req, res ->
            handleRemoteWork(resultJson = req.body(), threadToInterrupt = toInterrupt)
        })
    }

    fun answerWorkRequest(count: Int = 20): String {

        val mapForJson = HashMap<String, Any>()

        mapForJson.put("toMatch", HashProblemSolver.hashToFind)

        mapForJson.put("triples", WordGenerator.getNextPermutationBatch(count))

        mapForJson.put("fives", JSONArray(WordGenerator.listOfFives))

        return JSONObject(mapForJson).toString()
    }

    fun handleRemoteWork(resultJson: String, threadToInterrupt: Thread): String {
        ConsoleLogger.writeLine("handleRemoteWork - Top", ConsoleColor.YELLOW, 1)
        val reqBodyJson = JSONObject(resultJson)
        val isFound = reqBodyJson.getBoolean("found")
        if (isFound) {
            val targetSentence = reqBodyJson.getString("sentence")
            MessageHub.getLocalMessageHub().writeMessage(this, targetSentence)
            MessageHub.getFileMessageHub().writeMessage(this, targetSentence)
            threadToInterrupt.interrupt()
        }

        ConsoleLogger.writeLine("handleRemoteWork - End", ConsoleColor.YELLOW, 1)

        return "Thank you for your service"
    }
}