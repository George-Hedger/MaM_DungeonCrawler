package io.github.libgdx_dungeon_crawler

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Array
import ktx.actors.setPosition


class Map(val mapX: Int, val mapY: Int) {
    val tiles = Array(mapY) { ShortArray(mapX) { -2 } }
    private var tileActors = Array(mapY) { Array<TileActor>(mapX) }

    val texture: Texture = Texture(Gdx.files.internal("Dungeon/Dungeon_Tileset.png"))
    val wall = TextureRegion(texture, 128, 112, 16, 16)
    val floor = TextureRegion(texture, 144, 112, 16, 16)

    val mapSize = 100
    val tileSpacing = 10

    fun createTiles(stage: Stage){
        for (y in 0..<mapY){
            for (x in 0..<mapX){
                val state = tiles[y][x].toInt()
                
                if(state == -2)
                    tileActors[y].add(TileActor(wall))
                else
                    tileActors[y].add(TileActor(floor))

                tileActors[y][x].setPosition(x*mapSize,y*mapSize)
                tileActors[y][x].setSize(
                    (mapSize - tileSpacing).toFloat(),
                    (mapSize - tileSpacing).toFloat())

                if(state >= 0){
                    //TODO entities
                }

                tileActors[y][x].height = (-1).toFloat()

                stage.addActor(tileActors[y][x])
            }
        }
    }

    fun isTileFloor(x: Int, y: Int) : Boolean{
        return tiles[y][x].toInt() != -2
    }
}
