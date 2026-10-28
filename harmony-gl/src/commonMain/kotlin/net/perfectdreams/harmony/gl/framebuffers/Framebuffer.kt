package net.perfectdreams.harmony.gl.framebuffers

import net.perfectdreams.harmony.gl.GLConstants.GL_FRAMEBUFFER
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.rendertarget.RenderTarget
import net.perfectdreams.harmony.gl.textures.Texture

class Framebuffer(
    gl: GLContext,
    val framebufferId: Int,
    val texture: Texture,
    val depthTexture: Texture?
) : AutoCloseable, RenderTarget(gl) {
    override val width: Int
        get() = texture.width
    override val height: Int
        get() = texture.height

    override fun close() {
        gl.deleteFramebuffer(framebufferId)
        gl.deleteTexture(texture.textureId)
    }
}

inline fun <T> Framebuffer.withFramebuffer(block: () -> T): T {
    gl.bindFramebuffer(GL_FRAMEBUFFER, framebufferId)
    gl.viewport(0, 0, texture.width, texture.height)
    try {
        return block()
    } finally {
        gl.bindFramebuffer(GL_FRAMEBUFFER, 0)
        gl.viewport(0, 0, 1280, 720)
    }
}