package message

import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test as test
/**
 * Created by stephen on 11/13/15.
 */
class LocalBulletinBoardTest {

    val localBulletin = LocalBulletinBoard()

    @Before fun setup() : Unit {
        localBulletin.writeMessage("self", "first")
        localBulletin.writeMessage("sender", "second")
        localBulletin.writeMessage("self", "third")
    }

    @After fun cleanUp() : Unit {
        localBulletin.clearMessages()
    }

    @test fun testWriteMessage() : Unit {
        println("Pre-clearing the filled queue")
        localBulletin.clearMessages()
        // write a message
        localBulletin.writeMessage(this, "Testing messaging")
        // check the inner queue
        println(localBulletin.peekNextMessage())
        Assert.assertEquals("Testing messaging", localBulletin.getNextMessage())
    }

    @test fun testNextMessage() {

        val peek = localBulletin.peekNextMessage()
        Assert.assertEquals("Peek and pop should be equal",
                peek, localBulletin.getNextMessage())
        Assert.assertNotSame("Old peek and pop should be non-equal",
                peek, localBulletin.getNextMessage())
        Assert.assertEquals("Should say 'third'",
                "third", localBulletin.getNextMessage())
    }

    @test fun testNextMessageInvalid() {
        println("Empty board's nextMessage: ${localBulletin.getNextMessage()}")
    }

    @test fun testNextMessageJSON() {
        val jsonPeek = JSONObject(localBulletin.peekNextMessageJSON())
        Assert.assertEquals(jsonPeek.toString(2), localBulletin.getNextMessageJSON())

        val next = JSONObject(localBulletin.getNextMessageJSON())
        val nextFirstProp = next.getString("first")
        println("(from JSON String) next.first = $nextFirstProp")
        Assert.assertEquals("Should be the second sender", "sender", nextFirstProp)
    }
}