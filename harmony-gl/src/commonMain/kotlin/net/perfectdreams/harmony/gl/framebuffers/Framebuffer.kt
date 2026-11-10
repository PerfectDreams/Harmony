package net.perfectdreams.harmony.gl.framebuffers

import net.perfectdreams.harmony.gl.GLConstants.GL_FRAMEBUFFER
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.rendertarget.RenderTarget
import net.perfectdreams.harmony.gl.textures.Texture

class Framebuffer(
    gl: GLContext,
    val framebufferId: Int,
    val colorTexture: Texture?,
    val depthTexture: Texture?
) : AutoCloseable, RenderTarget(gl) {
    private val sizeTexture = requireNotNull(colorTexture ?: depthTexture) { "A framebuffer needs at least a color texture or a depth texture!" }

    override val width: Int
        get() = sizeTexture.width
    override val height: Int
        get() = sizeTexture.height

    override fun close() {
        gl.deleteFramebuffer(framebufferId)

        if (this.colorTexture != null)
            gl.deleteTexture(colorTexture.textureId)
        if (this.depthTexture != null)
            gl.deleteTexture(depthTexture.textureId)
    }
}

inline fun <T> Framebuffer.withFramebuffer(block: () -> T): T {
    gl.bindFramebuffer(GL_FRAMEBUFFER, framebufferId)
    gl.viewport(0, 0, this.width, this.height)
    try {
        return block()
    } finally {
        gl.bindFramebuffer(GL_FRAMEBUFFER, 0)
        gl.viewport(0, 0, 1280, 720)
    }
}