package io.github.libgdx_dungeon_crawler

import java.nio.ByteBuffer

// ---------------------------------------------------
// GameMessages must implement this interface
// so that objects can be serialised/deserialized
// ---------------------------------------------------
interface Serializable {
    fun serialize() : ByteArray
}

interface Deserializable {
    fun deserialize(bb: ByteBuffer) : GameMessage
}

// ---------------------------------------------------
// GameMessageType
//
// The game message types as a byte that can be
// encoded/decoded from a byte stream.
// ---------------------------------------------------
enum class GameMessageType(val type: Byte) {
    REGISTER(1),
    PLAYER_MOVE(2);

    companion object {
        fun fromByte(type: Byte) = entries.first { it.type == type }
    }
}

// ---------------------------------------------------
// Our various GameMessage implementations
// ---------------------------------------------------
sealed class GameMessage(val type: GameMessageType) : Serializable {

    data class RegisterMessage(val playerName: String) : GameMessage(GameMessageType.REGISTER) {
        override fun serialize() : ByteArray {
            // pretend it does what it is supposed to
            return ByteArray(10)
        }

        companion object : Deserializable {
            override fun deserialize(bb: ByteBuffer) : GameMessage {
                // pretend it does what it is supposed to
                // i.e. the name is taken from the ByteBuffer
                //      as seen previously
                val nameLength = bb.get().toInt()
                val rawName = ByteArray(nameLength)

                bb.get(rawName)

                return RegisterMessage(String(rawName, Charsets.UTF_8))
            }
        }
    }

    data class PlayerMoveMessage(val x: Float, val y: Float) : GameMessage(GameMessageType.REGISTER) {
        override fun serialize() : ByteArray {
            // pretend it does what it is supposed to
            // i.e. the x, y values are taken from the ByteArray
            //      as seen previously

            return ByteArray(10)
        }

        companion object : Deserializable {
            override fun deserialize(bb: ByteBuffer) : GameMessage {
                // pretend it does what it is supposed to
                // i.e. the data is taken from the ByteArray

                return PlayerMoveMessage(1.5f, 2.5f)
            }
        }
    }

}

// ---------------------------------------------------
// GameMessageFactory
// ---------------------------------------------------
class GameMessageFactory {
    fun createGameMessage(ba: ByteArray) : GameMessage {
        // ------------------------------------------------------------------------
        // Factory determines correct type and how to manage the vararg params
        //
        // Would need to handle incorrect types
        // ------------------------------------------------------------------------
        val bb = ByteBuffer.wrap(ba)
        val messageIdType = bb.get()

        return when(GameMessageType.fromByte(messageIdType)) {

            GameMessageType.REGISTER -> GameMessage.RegisterMessage.deserialize(bb)

            GameMessageType.PLAYER_MOVE -> GameMessage.PlayerMoveMessage.deserialize(bb)
        }
    }
}

/*
fun main() {
    val factory = GameMessageFactory()

    // .. Simulate an incoming message from the network ...
    val bb = ByteBuffer.allocate(6)
    val name = "BOB1"
    bb.put(GameMessageType.REGISTER.type)
    bb.put(name.length.toByte())
    bb.put(name.toByteArray(Charsets.UTF_8))

    // .. Simulate reconstructing the objects from data read from the network
    val gm1 = factory.createGameMessage(bb.array())
//     val gm2 = factory.createGameMessage(bb.array())

    println(gm1)
//     println(gm2)

    // ... Later, serialize the objects ...
    val ba = gm1.serialize()
}
*/
