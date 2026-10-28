package net.perfectdreams.harmony.gl.textures

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.TextureSource

class Texture(val gl: GLContext, val textureId: Int, var width: Int, var height: Int) : AutoCloseable {
    /**
     * Write [textureSource] to this texture
     *
     * If the [textureSource] is not the same size as this texture, a new storage will be allocated with [GLContext.texImage2D]
     */
    fun write(textureSource: TextureSource) {
        withTexture {
            // Upload the texture data
            if (this.width != textureSource.width || this.height != textureSource.height) {
                gl.texImage2D(
                    GLConstants.GL_TEXTURE_2D,
                    0,
                    GLConstants.GL_RGBA,
                    0,
                    GLConstants.GL_RGBA,
                    GLConstants.GL_UNSIGNED_BYTE,
                    textureSource
                )
            } else {
                writeSection(textureSource, 0, 0)
            }

            this.width = textureSource.width
            this.height = textureSource.height

            // Generate Mipmaps
            gl.generateMipmap(GLConstants.GL_TEXTURE_2D)
        }
    }

    /**
     * Writes a section of the texture with the given texture source
     */
    fun writeSection(textureSource: TextureSource, xoffset: Int, yoffset: Int) {
        withTexture {
            gl.texSubImage2D(
                GLConstants.GL_TEXTURE_2D,
                0,
                xoffset,
                yoffset,
                textureSource.width,
                textureSource.height,
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

inline fun <T> Texture.withTexture(block: () -> T): T {
    gl.bindTexture(GLConstants.GL_TEXTURE_2D, this.textureId)
    return block()
}