package io.github.libgdx_dungeon_crawler

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import kotlin.math.max


class MainMenuScreen(private val game: Main) : KtxScreen {

    override fun show() {
        Gdx.input.inputProcessor = stage

        val root = Stack()
        root.setFillParent(true)
        stage.addActor(root)

        val base = Table()
        root.add(base)

        base.setBackground(game.skin.getTiledDrawable("tile-a"))

        val image = Image(game.skin, "label-title")
        base.add<Image?>(image)

        base.row()
        var table = Table(game.skin)
        table.setBackground("window-c")
        base.add<Table?>(table).height(100.0f).width(200.0f)

        var label = Label("Config:", game.skin)
        table.add<Label?>(label).expandX().center()

        base.row()
        ipTable.setBackground("window-c")
        base.add<Table?>(ipTable).width(300f).height(140.0f)

        label = Label("IP Address:", game.skin)
        ipTable.add<Label?>(label)

        ipTable.row()
        ipText = TextField("127.0.0.1", game.skin)
        ipText.setTextFieldListener(TextField.TextFieldListener(
            fun(event : TextField, c: Char){
                if(event.text.length > 15 || !(c.isDigit() || c.isISOControl() || c == '.')) {
                    event.text = event.text.substring(0, max(event.text.length - 1, 0))
                    event.cursorPosition = event.text.length
                }
            }
        ))
        ipTable.add<TextField?>(ipText).right()

        base.row()
        portTable.setBackground("window-c")
        base.add<Table?>(portTable).width(300f).height(140.0f)

        label = Label("Port:", game.skin)
        portTable.add<Label?>(label)

        portTable.row()
        portText = TextField("3000", game.skin)
        portText.setTextFieldListener(TextField.TextFieldListener(
            fun(event : TextField, c: Char){
                if(event.text.length > 5 || !(c.isDigit() || c.isISOControl())) {
                    event.text = event.text.substring(0, max(event.text.length - 1, 0))
                    event.cursorPosition = event.text.length
                }
            }
        ))
        portTable.add<TextField?>(portText).right()

        base.row()
        nameTable.isVisible = false
        nameTable.setBackground("window-c")
        base.add<Table?>(nameTable).width(300f).height(140.0f)

        label = Label("Name:", game.skin)
        nameTable.add<Label?>(label)

        nameTable.row()
        nameText = TextField(username, game.skin)
        nameText.setTextFieldListener(TextField.TextFieldListener(
            fun(event : TextField, c: Char){
                if(event.text.length > 10 || !(c.isDefined() || !c.isWhitespace() || c.isISOControl())) {
                    event.text = event.text.substring(0, max(event.text.length - 1, 0))
                    event.cursorPosition = event.text.length
                }

                username = event.text
            }
        ))
        nameTable.add<TextField?>(nameText).right()

        base.row()
        table = Table(game.skin)
        table.setBackground("window-c")
        base.add<Table?>(table).width(220f).height(80.0f)

        loginButton = TextButton("Login", game.skin)
        loginButton.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent?, x: Float, y: Float) {
                if(!isConnected) {
                    println("Attempting connection to the server.")
                    if (NetClient.tryConnect(ipText.text, portText.text.toInt())) {
                        println("Connected to the server.")
                        onConnectToServer(true)
                    } else {
                        println("Could not connect to the server.")
                        showError("Could not connect to the server.")
                    }
                }
                else{
                    NetClient.sendMessage(GameMessage.RegisterMessage(username))
                }
            }
        })

        table.add<Container<TextButton>?>(Container(loginButton))





        errorTable.setFillParent(true)
        errorTable.isVisible = false
        errorTable.setBackground(game.skin.getTiledDrawable("tile-a"))
        root.add(errorTable)

        table = Table(game.skin)
        table.setBackground("window-c")
        errorTable.add<Table?>(table).width(700f).height(400F)

        errorLabel = Label("ERROR: ", game.skin)
        table.add<Label?>(errorLabel)

        table.row()
        val button = TextButton("Dismiss", game.skin)
        button.addListener(object : ClickListener() {
            override fun clicked(e: InputEvent?, x: Float, y: Float) {
                errorTable.isVisible = false
            }
        })

        val container = Container(button)
        table.add<Container<TextButton>?>(container)
    }

    val stage = Stage(ScreenViewport())
    val errorTable = Table(game.skin)
    lateinit var errorLabel : Label
    lateinit var loginButton : TextButton
    lateinit var ipText : TextField
    lateinit var portText : TextField
    lateinit var nameText : TextField
    val portTable = Table(game.skin)
    val ipTable = Table(game.skin)
    val nameTable = Table(game.skin)
    var isConnected = false
    var username = "username"

    fun showError(message :String){
        errorTable.isVisible = true
        errorLabel.setText("ERROR: $message")
    }

    fun onConnectToServer(connected: Boolean){
        isConnected = connected
        ipTable.isVisible = !connected
        portTable.isVisible = !connected
        nameTable.isVisible = connected
        if(connected)
            loginButton.setText("Join Game")
        else
            loginButton.setText("Login")
    }

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val message = NetClient.getNewMessage()
        if( message != null){
            if(message is GameMessage.ErrorMessage) {
                showError(message.code.toString() + ": " + message.errorMessage)

                if(message.code == 1.toShort()){
                    NetClient.stopServer()
                    onConnectToServer(false)
                }
            }
            else if (message is GameMessage.SuccessMessage) {
                if (message.code == 0.toShort() && isConnected) {
                    game.startGame(username)
                }
            }
            else if (message is GameMessage.InfoMessage){
                if(message.details == "PlayerID")
                    game.id = message.payload
            }
        }

        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    override fun resume() {

    }

    override fun dispose() {
        game.skin.disposeSafely()
        stage.disposeSafely()
    }

}
