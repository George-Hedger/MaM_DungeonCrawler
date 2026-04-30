
package io.github.libgdx_dungeon_crawler

import java.nio.ByteBuffer
import kotlin.toUByte
import kotlin.toUShort

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
    REGISTER(0.toByte()),
    ERROR(1.toByte()),
    SUCCESS(2.toByte()),
    INFO(3.toByte()),
    TILE_UPDATE(4.toByte());

    companion object {
        fun fromByte(type: Byte) = entries.first { it.type.toByte() == type }
    }
}

// ---------------------------------------------------
// Our various GameMessage implementations
// ---------------------------------------------------
sealed class GameMessage(val type: GameMessageType) : Serializable {
    data class RegisterMessage(val playerName: String) : GameMessage(GameMessageType.REGISTER) {
        override fun serialize() : ByteArray {
            val ba = ByteArray(2)
            ba[0] = type.type
            ba[1] = playerName.length.toUShort().toByte()

            return ba + playerName.toByteArray()
        }

        companion object : Deserializable {
            override fun deserialize(bb: ByteBuffer) : GameMessage {
                val nameLength = bb.get().toUShort().toInt()
                val rawName = ByteArray(nameLength)
                bb.get(rawName)

                return RegisterMessage(String(rawName, Charsets.UTF_8))
            }
        }
    }

    data class ErrorMessage(val errorMessage: String, val code : Short) : GameMessage(GameMessageType.ERROR) {
        override fun serialize() : ByteArray {
            val ba = ByteArray(3)
            ba[0] = type.type
            ba[1] = code.toByte()
            ba[2] = errorMessage.length.toUShort().toByte()

            return ba + errorMessage.toByteArray()
        }

        companion object : Deserializable {
            override fun deserialize(bb: ByteBuffer) : GameMessage {
                val code = bb.get().toShort()
                val messageLength = bb.get().toUShort().toInt()
                val rawName = ByteArray(messageLength)
                bb.get(rawName)

                return ErrorMessage(String(rawName, Charsets.UTF_8), code)
            }
        }
    }

    data class SuccessMessage(val code : Short) : GameMessage(GameMessageType.SUCCESS) {
        override fun serialize() : ByteArray {
            val ba = ByteArray(2)
            ba[0] = type.type
            ba[1] = code.toByte()
            return ba
        }

        companion object : Deserializable {
            override fun deserialize(bb: ByteBuffer) : GameMessage {
                val code = bb.get().toShort()

                return SuccessMessage(code)
            }
        }
    }

    data class InfoMessage(val details: String, val payload : Short) : GameMessage(GameMessageType.INFO) {
        override fun serialize() : ByteArray {
            val ba = ByteArray(3)
            ba[0] = type.type
            ba[1] = payload.toByte()
            ba[2] = details.length.toUShort().toByte()

            return ba + details.toByteArray()
        }

        companion object : Deserializable {
            override fun deserialize(bb: ByteBuffer) : GameMessage {
                val payload = bb.get().toShort()
                val messageLength = bb.get().toUShort().toInt()
                val rawName = ByteArray(messageLength)
                bb.get(rawName)

                return InfoMessage(String(rawName, Charsets.UTF_8), payload)
            }
        }
    }

    data class TileUpdateMessage(val x : Short, val y : Short, val occupiedId : Short) : GameMessage(GameMessageType.TILE_UPDATE) {
        override fun serialize() : ByteArray {
            val ba = ByteArray(4)
            ba[0] = type.type
            ba[1] = x.toByte()
            ba[2] = y.toByte()
            ba[3] = occupiedId.toByte()
            return ba
        }

        companion object : Deserializable {
            override fun deserialize(bb: ByteBuffer) : GameMessage {
                val x = bb.get().toShort()
                val y = bb.get().toShort()
                val occupiedId = bb.get().toShort()

                return TileUpdateMessage(x,y,occupiedId)
            }
        }
    }
}

// ---------------------------------------------------
// GameMessageFactory
// ---------------------------------------------------
object GameMessageFactory {
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

            GameMessageType.ERROR -> GameMessage.ErrorMessage.deserialize(bb)

            GameMessageType.SUCCESS -> GameMessage.SuccessMessage.deserialize(bb)

            GameMessageType.INFO -> GameMessage.InfoMessage.deserialize(bb)

            GameMessageType.TILE_UPDATE -> GameMessage.TileUpdateMessage.deserialize(bb)
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

