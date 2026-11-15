package net.perfectdreams.harmony.gl.textures

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLTexture
import net.perfectdreams.harmony.gl.TextureSource

class Texture(
    val gl: GLContext,
    val textureId: GLTexture,
    val format: TextureFormat,
    var width: Int,
    var height: Int
) : AutoCloseable {
    /**
     * Write [textureSource] to this texture
     *
     * If the [textureSource] is not the same size as this texture, a new storage will be allocated with [GLContext.texImage2D]
     */
    fun write(textureSource: TextureSource) {
        requireTextureSourceCompatibleFormat()

        withTexture {
            // Upload the texture data
            if (this.width != textureSource.width || this.height != textureSource.height) {
                gl.texImage2D(
                    GLConstants.GL_TEXTURE_2D,
                    0,
                    format.internalFormat,
                    0,
                    format.format,
                    format.type,
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
        requireTextureSourceCompatibleFormat()

        withTexture {
            gl.texSubImage2D(
                GLConstants.GL_TEXTURE_2D,
                0,
                xoffset,
                yoffset,
                textureSource.width,
                textureSource.height,
                format.format,
                format.type,
                textureSource
            )
        }
    }

    private fun requireTextureSourceCompatibleFormat() {
        // A TextureSource always holds RGBA bytes, uploading it to a texture with any other layout would misinterpret the pixels
        require(format.format == GLConstants.GL_RGBA && format.type == GLConstants.GL_UNSIGNED_BYTE) { "Cannot write a TextureSource to a texture with format $format, because TextureSources only hold RGBA bytes!" }
    }

    override fun close() {
        gl.deleteTexture(textureId)
    }
}

inline fun <T> Texture.withTexture(block: () -> T): T {
    gl.bindTexture(GLConstants.GL_TEXTURE_2D, this.textureId)
    return block()
}