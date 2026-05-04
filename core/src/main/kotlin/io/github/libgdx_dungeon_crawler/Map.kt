package io.github.libgdx_dungeon_crawler

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Array
import ktx.actors.setPosition


class Map(val mapX: Int, val mapY: Int) {
    val tiles = Array(mapY) { ShortArray(mapX) { -2 } }
    private var tileActors = Array(mapY) { Array<TileActor>(mapX) }

    private var entities = HashMap<Short, EntityActor>()
    private val charTexture: Texture = Texture(Gdx.files.internal("Dungeon/Dungeon_Character.png"))
    private val player = TextureRegion(charTexture, 48, 48, 16, 16)
    private val tileTexture: Texture = Texture(Gdx.files.internal("Dungeon/Dungeon_Tileset.png"))
    private val wall = TextureRegion(tileTexture, 128, 112, 16, 16)
    private val floor = TextureRegion(tileTexture, 144, 112, 16, 16)

    private val mapSize = 1
    private val tileSpacing = mapSize * 0.1f

    fun addEntity(msg: GameMessage.NewEntityMessage) : EntityActor {
        val e = EntityActor(player, msg.entityType)
        e.setSize(
            (mapSize.toFloat() - tileSpacing),
            (mapSize.toFloat() - tileSpacing))

        entities.put(msg.id, e)
        return e
    }

    fun createAddEntity(group: Group, msg: GameMessage.NewEntityMessage){
        group.addActor(addEntity(msg))
    }

    fun createAllEntities(group: Group){
        for(e in entities){
            group.addActor(e.component2())
        }
    }

    fun getEntity(id: Short) : EntityActor?{
        return entities.get(id)
    }

    fun moveEntity(x:Int, y:Int, id: Short){
        if(isTileFloor(x, y)) {
            tiles[y][x] = id
            val e = entities.get(id)

            e?.let {
                val prevX = e.coordX
                val prevY = e.coordY

                if(prevX != -1 && prevY != -1)
                    tiles[prevY][prevX] = -1

                e.setCoords(x, y, mapSize)
            }
        }
    }

    fun createTiles(group: Group){
        for (y in 0..<mapY){
            for (x in 0..<mapX){

                val state = tiles[y][x]
                var t: TileActor

                if(state == (-2).toShort())
                    t = TileActor(wall, x, y)
                else
                    t = TileActor(floor, x, y)

                t.setPosition(x*mapSize,y*mapSize)
                t.setSize(
                    (mapSize.toFloat() - tileSpacing),
                    (mapSize.toFloat() - tileSpacing))

                if(state >= 0){
                    moveEntity(x, y, state)
                }

                tileActors[y].add(t)
                group.addActor(t)
            }
        }
    }

    fun isTileFloor(x: Int, y: Int) : Boolean{
        return tiles[y][x].toInt() != -2
    }
}
