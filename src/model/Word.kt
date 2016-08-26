package model

import org.json.JSONObject

/**
 * An object to encapsulate the (rank, word) pair (i.e. "{"rank":0,"word":"the"}" ) returned from the API
 *
 * Created by stephen on 11/12/15.
 */
data class Word(val rank: Int, val word: String) {

    companion object {
        public fun fromJSONObject(jsonObject : JSONObject): Word {
            val errorFormat = "jsonObject was missing property: %s"
            if (!jsonObject.has("rank"))
                throw IllegalArgumentException(java.lang.String.format(errorFormat, "rank"))
            if (!jsonObject.has("word"))
                throw IllegalArgumentException(java.lang.String.format(errorFormat, "word"))

            return Word(jsonObject.getInt("rank"), jsonObject.getString("word"))
        }
    }

    fun toJsonObject() : JSONObject {
        return JSONObject(this)
    }

    fun toJsonString() : String = toJsonObject().toString()
}