package net.perfectdreams.harmony.gl.rendertarget

import net.perfectdreams.harmony.gl.GLConstants.GL_FRAMEBUFFER
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.framebuffers.Framebuffer
import net.perfectdreams.harmony.gl.framebuffers.withFramebuffer

abstract class RenderTarget(val gl: GLContext) {
    abstract val width: Int
    abstract val height: Int
}

fun RenderTarget.withRenderTarget(block: () -> Unit) {
    when (this) {
        is Framebuffer -> return this.withFramebuffer(block)
        is WindowRenderTarget -> {
            gl.bindFramebuffer(GL_FRAMEBUFFER, 0)
            gl.viewport(0, 0, this.width, this.height)
            block()
        }
        else -> error("I don't know how to render to $this!")
    }
}