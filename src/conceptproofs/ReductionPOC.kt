package conceptproofs

import com.mashape.unirest.http.Unirest
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import printAsWords
import java.util.*

object ReductionPOC {

    fun run(wordsApiFormat: String) : Unit {

        println("This is main")
        val jsObject : JSONObject = JSONObject(Carrier("Stephen", 19))
        val hashMap = HashMap<String, Any>()
        hashMap.putIfAbsent("jsObject", jsObject)
        hashMap.putIfAbsent("number", 2)

        val readIn = JSONTokener(JSONObject(hashMap).toString(2))
        while (readIn.more()) {
            val payloadAsJSON = JSONObject(readIn.nextValue().toString())
            println("The payloadAsJSON = ${payloadAsJSON.toString(2)}")

            val intGet = payloadAsJSON.get("number")
            println("The intGet = $intGet")
            val innerObject = payloadAsJSON.getJSONObject("jsObject")
            val carrierBack = Carrier(innerObject.getString("name"), innerObject.getInt("age"))
            println("Got back a $carrierBack")
        }

        val urlForReq = createUrlForRequest(startInd = 0, endInd = 20, format = wordsApiFormat)

        val getRequest = Unirest.get(urlForReq)
        val requestBody = getRequest.asString()
        println("Get request (as JSON): ${requestBody.body}\n---------- Delimiter ----------\n")

        val jsonArray = JSONArray(requestBody.body)

        jsonArray.printAsWords()

    }

    fun createUrlForRequest(startInd : Int, endInd : Int, format : String): String {
        return java.lang.String.format(format, startInd, endInd)
    }

    data class Carrier(val name: String, var age: Int)

}