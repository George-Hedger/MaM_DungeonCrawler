package io.github.libgdx_dungeon_crawler

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener



class EntityActor(val region: TextureRegion, val type: EntityType) : Actor() {
    init{
        setBounds(region.regionX.toFloat(), region.regionY.toFloat(),
            region.getRegionWidth().toFloat(), region.getRegionHeight().toFloat())
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        color = getColor()

        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
            getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation())
    }

    var coordY = -1

    var coordX = -1

    fun setCoords(_x: Int, _y: Int, mapSize: Int){
        coordX = _x
        coordY = _y
        setPosition((coordX*mapSize).toFloat(), (coordY*mapSize).toFloat())
    }
}
