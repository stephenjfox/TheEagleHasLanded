package config

import model.Word
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by stephen on 11/12/15.
 */
object WordsPreProcessor {
    /**
     * Returns the File
     */
    val currentDir: String = "src/config"
    val filtered: String = "filteredWords"

    fun getFilteredWords(lowerBound: Int = 0, count: Int): List<Word> {
        // there should only be a single line
        val allLines = Files.readAllLines(Paths.get("$currentDir/$filtered.json"))
        val jsStringArray = allLines.first()

        val skippedAhead = Sequence { JSONArray(jsStringArray).iterator() }
                .drop(lowerBound)
                .take(count)

        return skippedAhead
                .map { Word.fromJSONObject(JSONObject(it.toString())) }
                .toList()
    }

    /**
     * Returns the filteredWords, bisected into two List<T>s.
     * [Pair.first] will be elements that matched the predicate,
     * [Pair.second] will be elements that failed to match
     */
    fun getFilteredWordPartitionsBy(lowerBound: Int = 0, count: Int,
                                    partitionPredicate: (String) -> Boolean)
            : Pair<List<Word>, List<Word>> {

        val filteredWords = getFilteredWords(lowerBound, count)
                .filter { it.rank < 601 }
        println("Filter words.size = ${filteredWords.size}")

        return filteredWords.partition { partitionPredicate(it.word) }
    }
}
