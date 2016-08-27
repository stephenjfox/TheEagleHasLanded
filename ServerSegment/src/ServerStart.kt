import com.fox.io.log.ConsoleColor
import com.fox.io.log.ConsoleLogger
import hashing.HashProblemSolver
import model.WordGenerator
import org.json.JSONArray
import org.json.JSONObject
import spark.Request
import spark.Response
import spark.Spark.get
import spark.Spark.post
import java.util.*

/**
 * Created by stephen on 11/18/15.
 */
fun main(args : Array<String>) {
    sparkRun()
}

fun sparkRun() {

    HashProblemSolver().seedProblemSolving()

    get("/hello", { req, res -> "Hello, world: length = ${req.contentLength()}" })
    get("/work", { req, res -> answerWorkRequest() })
    get("/work/:count", { req, res ->
        val count = req.params(":count").toInt()
        answerWorkRequest(count)
    })
    post("/work", "application/json", { req, res -> dealWorkResult(req) })
}

fun answerWorkRequest(count: Int = 20) : String {

    val mapForJson = HashMap<String, Any>()

    mapForJson.put("toMatch", HashProblemSolver.hashToFind)

    mapForJson.put("triples", WordGenerator.getNextPermutationBatch(count))

    mapForJson.put("fives", JSONArray(WordGenerator.listOfFives))

    return JSONObject(mapForJson).toString()
}

fun dealWorkResult(request: Request?) : String {
    println("We received the POST")
    if (request != null) {
        if (request.contentLength() > 0) {
            val reqBodyJson = JSONObject(request.body())
            val isFound = reqBodyJson.getBoolean("found")
            if (isFound) {
                val targetSentence = reqBodyJson.getString("sentence")
                // TODO: alter the other guys
            }
            else {
                println("Target sentence was not found.")
            }
        }
    }

    ConsoleLogger.writeLine("Delimiter", ConsoleColor.YELLOW, 1)

    return "Testing string return"
}
