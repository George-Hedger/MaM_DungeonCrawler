package io.github.libgdx_dungeon_crawler

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.async.KtxAsync
import ktx.graphics.use

const val IP_ADDRESS = "127.0.0.1"
const val PORT = 3000

class Main : KtxGame<KtxScreen>() {

    override fun create() {
        KtxAsync.initiate()

        skin = Skin(Gdx.files.internal("terra-mother/skin/terra-mother-ui.json"))
        skin.getFont("font").data.scale(1f)

        addScreen(MainMenuScreen(this))
        addScreen(LoadingScreen(this))
        addScreen(GameScreen(this))
        setScreen<MainMenuScreen>()
    }

    override fun render() {
        super.render()
    }

    override fun dispose() {
        NetClient.stopServer()
    }


    fun startGame(name : String){
        playerName = name
        setScreen<LoadingScreen>()
    }

    lateinit var skin : Skin
    lateinit var playerName : String
    lateinit var map : Map
}
