
import conceptproofs.HttpServerPOC
import hashing.HashProblemSolver
import model.Word
import org.json.JSONArray
import org.json.JSONObject

/**
 * Starting point for the multi-threaded SHA256 code matcher
 * Created by stephen on 11/12/15.
 */

fun main(args : Array<String>) {

    val mainThread = Thread.currentThread()
    var serverThread = Thread({
        HttpServerPOC.sparkRun(mainThread)
    })
    // Scratch: This works!!! In like 2.5h
    serverThread.start()
    HashProblemSolver().runIt()
}

/**
 * This is my glorified debugging. Just be glad I'm not using my pretty print
 * library functions that I built.
 */
fun JSONArray.printAsWords() : Unit {
    this.filter { it is JSONObject }.forEach { println(Word.fromJSONObject(it as JSONObject)) }
}

