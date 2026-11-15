package net.perfectdreams.harmony.gl.framebuffers

import net.perfectdreams.harmony.gl.GLConstants.GL_FRAMEBUFFER
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLFramebuffer
import net.perfectdreams.harmony.gl.rendertarget.RenderTarget

class Framebuffer(
    gl: GLContext,
    val framebufferId: GLFramebuffer,
    val attachments: FramebufferAttachments,
    override val width: Int,
    override val height: Int
) : AutoCloseable, RenderTarget(gl) {
    override fun close() {
        gl.deleteFramebuffer(framebufferId)

        fun deleteAttachment(attachment: FramebufferAttachment) {
            when (attachment) {
                is FramebufferAttachment.TextureAttachment -> attachment.texture.close()
                is FramebufferAttachment.RenderbufferAttachment -> attachment.renderbuffer.close()
            }
        }

        for (attachment in attachments.colorAttachments) {
            deleteAttachment(attachment.value)
        }

        attachments.depthStencilAttachment?.let { deleteAttachment(it) }
    }
}

inline fun <T> Framebuffer.withFramebuffer(block: () -> T): T {
    gl.bindFramebuffer(GL_FRAMEBUFFER, framebufferId)
    gl.viewport(0, 0, this.width, this.height)
    try {
        return block()
    } finally {
        gl.bindFramebuffer(GL_FRAMEBUFFER, null)
        gl.viewport(0, 0, 1280, 720)
    }
}