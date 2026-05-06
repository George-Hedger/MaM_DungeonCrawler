package io.github.libgdx_dungeon_crawler

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

class TileActor(val region: TextureRegion, val coordX: Int, val coordY: Int, val screen: GameScreen) : Actor() {
    init{
        setBounds(region.regionX.toFloat(), region.regionY.toFloat(),
            region.getRegionWidth().toFloat(), region.getRegionHeight().toFloat())

        addListener(object : InputListener() {
            override fun touchUp(
                event: InputEvent?, x: Float, y: Float,
                pointer: Int, button: Int
            ) {
                screen.selectedTile = this@TileActor
            }

            override fun touchDown(
                event: InputEvent?, x: Float, y: Float,
                pointer: Int, button: Int
            ): Boolean {

                return true
            }
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        color = getColor()

        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
            getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation())
    }
}
