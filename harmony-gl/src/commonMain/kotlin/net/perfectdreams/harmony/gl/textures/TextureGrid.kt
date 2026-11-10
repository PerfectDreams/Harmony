package net.perfectdreams.harmony.gl.textures

import net.perfectdreams.harmony.gl.TextureSource

/**
 * A texture grid, used for texture arrays
 */
class TextureGrid(
    val cells: Int,
    val cellWidth: Int,
    val cellHeight: Int,
    val source: TextureSource
)