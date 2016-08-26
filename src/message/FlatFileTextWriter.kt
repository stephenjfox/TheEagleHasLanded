package message

import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.PrintWriter

/**
 * Created by stephen on 11/14/15.
 */
class FlatFileTextWriter : MessageHub {

    val filePathString = "src/solution.txt"
    val bufferedOutputStream = BufferedOutputStream(FileOutputStream(filePathString))

    override fun writeMessage(sender : Any, text : String) {
        bufferedOutputStream.write("Sender: $sender  message: $text\n".toByteArray())
        bufferedOutputStream.flush()
        println("Wrote the solution to the file")
    }

    override fun clearMessages() {
        // not exactly sure how to blank a file in Java
        // THis: http://stackoverflow.com/questions/6994518/how-to-delete-the-content-of-text-file-without-deleting-itself
        // Says that
        PrintWriter(filePathString).close() // will do
    }
}