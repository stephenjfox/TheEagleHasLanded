package message

/**
 * Abstraction for nodes to report their particular goings on
 * Created by stephen on 11/13/15.
 */
interface MessageHub {

    fun writeMessage(sender : Any, text : String)

    companion object {

        private val localBoard = LocalBulletinBoard()
        private val fileHub = FlatFileTextWriter()

        fun getLocalMessageHub() : MessageHub {
            return localBoard
        }

        fun getFileBasedMessageHub() : MessageHub {
            return fileHub
        }

    }

    /**
     * Implementation is left to the developer, but it is suggested that
     * after this is called, some internal message container be emptied
     */
    fun clearMessages()

}

