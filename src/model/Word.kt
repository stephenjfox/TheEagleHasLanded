package model

import org.json.JSONObject

/**
 * An object to encapsulate the (rank, word) pair (i.e. "{"rank":0,"word":"the"}" )
 * returned from the API
 *
 * Created by stephen on 11/12/15.
 */
data class Word(val rank: Int, val word: String) {

    companion object {
        fun fromJSONObject(jsonObject: JSONObject): Word {
            val errorPrelude = "jsonObject was missing property:"
            if (!jsonObject.has("rank"))
                throw IllegalArgumentException("$errorPrelude 'rank'")
            if (!jsonObject.has("word"))
                throw IllegalArgumentException("$errorPrelude 'word'")

            return Word(jsonObject.getInt("rank"), jsonObject.getString("word"))
        }
    }
}