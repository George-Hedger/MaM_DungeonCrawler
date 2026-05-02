
package io.github.libgdx_dungeon_crawler

import java.nio.ByteBuffer
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
    TILE_UPDATE(4.toByte()),
    LOAD_MAP(5.toByte());

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
            val ba = ByteArray(3)
            ba[1] = type.type
            ba[2] = playerName.length.toUShort().toByte()

            val ret = ba + playerName.toByteArray()
            ret[0] = (ret.size - 1).toShort().toByte()

            return ret
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
            val ba = ByteArray(4)
            ba[1] = type.type
            ba[2] = code.toByte()
            ba[3] = errorMessage.length.toUShort().toByte()

            val ret = ba + errorMessage.toByteArray()
            ret[0] = (ret.size - 1).toShort().toByte()

            return ba
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
            val ba = ByteArray(3)
            ba[1] = type.type
            ba[2] = code.toByte()

            ba[0] = (ba.size.toShort() - 1).toByte()

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
            val ba = ByteArray(4)
            ba[1] = type.type
            ba[2] = payload.toByte()
            ba[3] = details.length.toUShort().toByte()

            val ret = ba + details.toByteArray()
            ret[0] = (ret.size - 1).toShort().toByte()

            return ret
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
            val ba = ByteArray(5)
            ba[1] = type.type
            ba[2] = x.toByte()
            ba[3] = y.toByte()
            ba[4] = occupiedId.toByte()

            ba[0] = (ba.size.toShort() - 1).toByte()

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

    data class LoadMapMessage(val y : Short, val length : Short, val tiles : Array<Short>) : GameMessage(GameMessageType.LOAD_MAP) {
        override fun serialize() : ByteArray {
            val ba = ByteArray(4 + length)
            ba[1] = type.type
            ba[2] = y.toByte()
            ba[3] = length.toByte()

            for (i in 0..<length) {
                ba[i + 4] = tiles[i].toByte()
            }

            ba[0] = (ba.size.toShort() - 1).toByte()

            return ba
        }

        companion object : Deserializable {
            override fun deserialize(bb: ByteBuffer) : GameMessage {
                val y = bb.get().toShort()
                val length = bb.get().toShort()
                val tiles = arrayOfNulls<Short>(length.toInt())

                for (i in 0..<length) {
                    tiles[i] = bb.get().toShort()
                }

                return LoadMapMessage(y, length,  tiles.filterNotNull().toTypedArray())
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LoadMapMessage

            if (length != other.length) return false
            if (!tiles.contentEquals(other.tiles)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = length.toInt()
            result = 31 * result + tiles.contentHashCode()
            return result
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

            GameMessageType.LOAD_MAP -> GameMessage.LoadMapMessage.deserialize(bb)
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

