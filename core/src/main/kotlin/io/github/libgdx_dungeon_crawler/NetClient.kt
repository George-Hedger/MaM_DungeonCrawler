package io.github.libgdx_dungeon_crawler

import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
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

                while (!socket.isClosed) {
                    val ba = ByteArray(1024)

                    var read: Int
                    instream.read(ba).also { read = it }

                    val output = String(ba, 0, read)

                    System.out.flush()
                    println("New Message: $output")

                    queue.add(output)
                }
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

