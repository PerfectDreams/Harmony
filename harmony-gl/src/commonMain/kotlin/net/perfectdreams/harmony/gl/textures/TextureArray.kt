package net.perfectdreams.harmony.gl.textures

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.TextureSource

class TextureArray(
    val gl: GLContext,
    val textureId: Int,
    val layerCount: Int,
    val layerWidth: Int,
    val layerHeight: Int
) : AutoCloseable {
    /**
     * Write [textureSource] to this texture array, replacing all layers
     */
    fun writeAll(textureSource: TextureSource) {
        withTextureArray {
            // Upload the texture data
            writeLayers(
                textureSource,
                0,
                0,
                0,
                this.layerCount
            )

            // Generate Mipmaps
            gl.generateMipmap(GLConstants.GL_TEXTURE_2D_ARRAY)
        }
    }

    /**
     * Writes to a layer of the texture with the given texture source
     */
    fun writeLayer(textureSource: TextureSource, xoffset: Int, yoffset: Int, startAtLayer: Int) = writeLayers(textureSource, xoffset, yoffset, startAtLayer, 1)

    /**
     * Writes to a layer of the texture with the given texture source
     */
    fun writeLayers(textureSource: TextureSource, xoffset: Int, yoffset: Int, startAtLayer: Int, layersToBeWritten: Int) {
        withTextureArray {
            gl.texSubImage3D(
                GLConstants.GL_TEXTURE_2D_ARRAY,
                0,
                xoffset,
                yoffset,
                startAtLayer,
                textureSource.width,
                // We want to use the TOTAL layerCount of this "container"
                textureSource.height / this.layerCount,
                layersToBeWritten,
                GLConstants.GL_RGBA,
                GLConstants.GL_UNSIGNED_BYTE,
                textureSource
            )
        }
    }

    override fun close() {
        gl.deleteTexture(textureId)
    }
}

inline fun <T> TextureArray.withTextureArray(block: () -> T): T {
    gl.bindTexture(GLConstants.GL_TEXTURE_2D_ARRAY, this.textureId)
    return block()
}