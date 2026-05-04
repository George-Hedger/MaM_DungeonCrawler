package io.github.libgdx_dungeon_crawler

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxScreen


class GameScreen(val game: Main): KtxScreen {

    override fun show() {
        stage = Stage(FitViewport(game.map.mapX.toFloat(), game.map.mapY.toFloat()))

        Gdx.input.inputProcessor = stage

        val base = Group()
        tileGroup = Group()
        entityGroup = Group()

        game.map.createAllEntities(entityGroup)
        game.map.createTiles(tileGroup)

        base.addActor(tileGroup)
        base.addActor(entityGroup)

        entityGroup.toFront()

        stage.addActor(base)

        playerActor = game.map.getEntity(game.id)!!
    }

    lateinit var tileGroup : Group
    lateinit var entityGroup : Group
    lateinit var stage : Stage
    var isTurn = false
    lateinit var playerActor : EntityActor

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        val delta = Gdx.graphics.deltaTime

        val message = NetClient.getNewMessage()
        if( message != null) {
            if (message is GameMessage.TileUpdateMessage){
                game.map.moveEntity(message.x.toInt(), message.y.toInt(), message.occupiedId)
            }
            if (message is GameMessage.InfoMessage) {
                if(message.details == "BeginTurn"){
                    if(message.payload == game.id){
                        isTurn = true
                    }
                }
            }
        }

        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        stage.act(delta)
        stage.draw()
    }

    override fun resume() {

    }

    override fun dispose() {
        stage.dispose()
    }

}
