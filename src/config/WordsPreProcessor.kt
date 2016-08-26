package config

import com.mashape.unirest.http.Unirest
import model.Word
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Stream

/**
 * Created by stephen on 11/12/15.
 */
object WordsPreProcessor {
    /**
     * Returns the File
     */
    val currentDir : String = "src/config"
    val filtered : String = "filteredWords"

    fun fillFilesFromAPI(apiFormat : String, lowerBound : Int, upperBound : Int) : File? {
        val path = Paths.get("$currentDir/wordObjects.json")
        println("Absolute path ${path.toAbsolutePath().toString()}")

        println("request format = $apiFormat")

        if (Files.exists(path)) {
            println("Already got the file. Printing all strings in the file")
        } else {

            println("Creating the file")
            Files.createFile(path)

            println("Running Unirest.get request")
            val formattedRequest = String.format(apiFormat, lowerBound, upperBound)
            println("Request, formatted = $formattedRequest")

            val getRequest = Unirest.get(formattedRequest)
            val listOfItems = ArrayList<String>()
            listOfItems.add(getRequest.asString().body)

            println("Writing the request body to File @ ${path.toAbsolutePath().fileName}")
            Files.write(path, listOfItems)
        }

        val linesStream : Stream<String> = Files.lines(path)
        // println("Stream count = ${linesStream.count()}")

        val streamAsArrayOfJSON = JSONArray(linesStream.toArray()[0] as String).filter { it is JSONObject }
        // println("Array component type = ${streamAsArrayOfJSON[0].javaClass}")

        println("Total request return size = ${streamAsArrayOfJSON.size}")


        val filterJsonElements = streamAsArrayOfJSON.map { ((it as JSONObject).get("word") as String) }
                .filter { it.length == 4 || it.length == 5 }
        println("Filtered size = ${filterJsonElements.size}")
        println("Count of 4's = ${filterJsonElements.filter { it.length == 4 }.size}")
        println("Count of 5's = ${filterJsonElements.filter { it.length == 5 }.size}")

        val pathForFiltered = Paths.get("$currentDir/$filtered.json")

        if (Files.exists(pathForFiltered)) {
            println("Removing potentially duplicate or corrupted data")
            Files.delete(pathForFiltered)
        }

        println("Creating $filterJsonElements.json")
        Files.createFile(pathForFiltered)

        val filterJsonAsJSONObjects = streamAsArrayOfJSON.map { it as JSONObject }.filter {
            val len = (it.get("word") as String).length
            len == 4 || len == 5
        }

        val jsonTypedArray = JSONArray(filterJsonAsJSONObjects.toTypedArray())
        // println(jsonTypedArray.toString())

        return Files.write(pathForFiltered, arrayListOf(jsonTypedArray.toString())).toFile()
    }

    fun getFilteredWords(lowerBound : Int = 0, count : Int) : List<Word> {
        // there should only be a single line
        val allLines = Files.readAllLines(Paths.get("$currentDir/$filtered.json"))
        val jsStringArray = allLines[0]
        // println("stringArray.length = ${jsStringArray.length}")
        // println("string's first part = ${jsStringArray.subSequence(0, 20)}")

        val skippedAhead = JSONArray(jsStringArray)
                .drop(lowerBound)
                .take(count)

        return skippedAhead
                .map { Word.fromJSONObject(JSONObject(it.toString())) }
    }

    /**
     * Returns the filteredWords, bisected into two List<T>s.
     * Pair::getFirst() will be elements that matched the predicate,
     * Pair::getSecond() will be elements that failed to match
     */
    fun getFilteredWordPartitionsBy(lowerBound : Int = 0, count : Int,
                                    partitionFunction: (String) -> Boolean) : Pair<List<Word>, List<Word>> {

        val filteredWords = getFilteredWords(lowerBound, count).filter { it.rank < 601 }
        println("Filterwords.size = ${filteredWords.size}")

        return filteredWords.partition { partitionFunction(it.word) }
    }
}

fun String.Companion.format(format : String, vararg objects : Any) : String {
    return java.lang.String.format(format, *objects)
}
