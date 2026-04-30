package io.github.libgdx_dungeon_crawler

class Map(val mapX: Int, val mapY: Int) {

    val tiles = Array(mapY) { ShortArray(mapX, init = {return@ShortArray -2}) }
}
