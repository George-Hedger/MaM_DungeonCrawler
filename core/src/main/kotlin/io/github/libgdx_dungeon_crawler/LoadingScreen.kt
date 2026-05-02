package io.github.libgdx_dungeon_crawler

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxScreen
import ktx.assets.disposeSafely

class LoadingScreen(private val game: Main) : KtxScreen {

    override fun show() {
        Gdx.input.inputProcessor = stage

        val root = Stack()
        root.setFillParent(true)
        stage.addActor(root)

        val base = Table()
        root.add(base)

        base.setBackground(game.skin.getTiledDrawable("tile-a"))

        val table = Table(game.skin)
        table.setBackground("window-c")
        base.add<Table?>(table).height(300f).width(600.0f)

        label = Label("Waiting for players...", game.skin)
        table.add<Label?>(label).expandX().center()

        table.row()
        playersLabel = Label("Players:", game.skin)
        table.add<Label?>(playersLabel).expandX().center()

        NetClient.sendMessage(GameMessage.InfoMessage("CurrentPlayers", 0.toShort()))
        NetClient.sendMessage(GameMessage.InfoMessage("MaxPlayerCount", 0.toShort()))
    }

    val stage = Stage(ScreenViewport())

    var maxPlayers = 0
    var currentPlayers = 0
    var mapX = 0
    var mapY = 0
    var updates = 0

    lateinit var label : Label
    lateinit var playersLabel : Label

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
            if(message is GameMessage.InfoMessage){
                if(message.details == "MaxPlayerCount"){
                    maxPlayers = message.payload.toInt()

                    playersLabel.setText("Players: $currentPlayers/$maxPlayers")
                }
                else if(message.details == "CurrentPlayers"){
                    currentPlayers = message.payload.toInt()

                    playersLabel.setText("Players: $currentPlayers/$maxPlayers")
                }
                else if(message.details == "MapX"){
                    mapX = message.payload.toInt()
                }
                else if(message.details == "MapY"){
                    mapY = message.payload.toInt()
                }

                if(mapX != 0 && mapY != 0){
                    game.map = Map(mapX, mapY)
                    NetClient.sendMessage(GameMessage.SuccessMessage(1.toShort()))
                }
            }
            else if(message is GameMessage.RegisterMessage) {
                label.setText(message.playerName)
            }
            else if(message is GameMessage.LoadMapMessage){
                updates++

                for (x in 0..<(mapX-1)) {
                    game.map.tiles[message.y.toInt()][x] = message.tiles[x]
                }

                if(updates == mapY)
                    NetClient.sendMessage(GameMessage.SuccessMessage(2.toShort()))
            }
            else if(message is GameMessage.SuccessMessage){
                if(message.code.toInt() == 3){
                    game.setScreen<GameScreen>()
                }
            }
            else{
                println("Unneeded message")
            }
        }

        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    override fun resume() {

    }

    override fun dispose() {
        stage.disposeSafely()
    }

}
