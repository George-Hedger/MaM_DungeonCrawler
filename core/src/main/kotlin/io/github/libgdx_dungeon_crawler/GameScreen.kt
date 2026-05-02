package io.github.libgdx_dungeon_crawler

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxScreen

class GameScreen(val game: Main): KtxScreen {

    override fun show() {
        Gdx.input.inputProcessor = stage

        game.map.createTiles(stage)

    }

    val stage = Stage(ScreenViewport())

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        val delta = Gdx.graphics.deltaTime

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
