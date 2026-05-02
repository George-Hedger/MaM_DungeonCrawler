package io.github.libgdx_dungeon_crawler

import java.nio.ByteBuffer
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue

const val DELAY : Long = 300
const val TIMEOUT : Long = 15000

object NetClient {

    private class SocketConnection(ip: String, port: Int) {
        val queue = ConcurrentLinkedQueue<String>()
        val outputQueue = ConcurrentLinkedQueue<ByteArray>()
        val socket = Socket(ip, port)

        val outstream: OutputStream = socket.getOutputStream()
        val instream: InputStream = socket.getInputStream()

        fun runServer() =
            CoroutineScope(Dispatchers.IO).launch {
                sendMessageLoop()

                var partial = false
                var expected = 0

                var message_buffer : ByteBuffer
                var ba = ByteArray(0)
                var buffer_size = 0

                while (!socket.isClosed) {
                    var length : Int
                    val read = ByteArray(1024)

                    try {
                        instream.read(read).also { length = it }
                    }
                    catch (error : Exception){
                        stopServer()
                        break
                    }

                    if(length > 0) {
                        buffer_size += length
                        message_buffer = ByteBuffer.wrap((ba + read), 0, buffer_size)

                        var loop = true;

                        while (loop) {
                            loop = false

                            if (!partial) {
                                expected = message_buffer.get().toShort().toInt()
                                buffer_size--
                            } else {
                                partial = false;
                            }

                            if (buffer_size >= expected) {
                                val msg = ByteArray(expected)
                                message_buffer.get(msg)
                                addMessage(String(msg))
                                buffer_size -= expected;

                                if (buffer_size > expected && expected != 0)
                                    loop = true;
                                else
                                    message_buffer.clear();
                            } else {
                                println("Received partial from server")

                                partial = true;
                            }
                        }

                        ba = ByteArray(buffer_size)
                        message_buffer.get(ba)
                    }
                }
            }

        fun addMessage(output: String){
            System.out.flush()
            println("New Message: $output")

            queue.add(output)
        }

        fun sendMessageLoop() =
            CoroutineScope(Dispatchers.IO).launch {
                while (!socket.isClosed) {
                    if(outputQueue.isNotEmpty()) {
                        val message = outputQueue.remove()
                        println("Sending message")
                        outstream.flush()
                        outstream.write(message)
                    }

                    delay(50)
                }
            }
    }

    private var connection : SocketConnection? = null

    fun tryConnect(ip: String, port: Int) : Boolean = runBlocking{
        stopServer()

        try {
            connection = SocketConnection(ip, port)
        }
        catch (e : Exception) {
            stopServer()
            println("Error connecting to server: " + e.message)
            return@runBlocking false
        }

        var connected = false
        var waitingTime : Long = 0

        while(!connected && waitingTime < TIMEOUT) {
            delay(DELAY)

            waitingTime += DELAY

            connection?.let {
                if (it.socket.isConnected) {
                    connected = true
                }
            }
        }

        if(connected){
            connection?.runServer()
            return@runBlocking true
        }

        stopServer()
        return@runBlocking false
    }

    fun sendMessage(message: GameMessage){
        connection?.let {
            println("Sending message of type:" + message.type)
            it.outputQueue.add(message.serialize())
        }
    }

    fun hasNewMessage(): Boolean {
        connection?.let {
            return it.queue.isNotEmpty()
        }

        return false
    }

    fun getNewMessage(): GameMessage? {
        connection?.let {
            if (hasNewMessage()) {
                return GameMessageFactory.createGameMessage(it.queue.remove().toByteArray())
            }
        }

        return null
    }

    fun stopServer() {
        connection?.let {
            it.socket.close()
            connection = null
        }
    }
}

