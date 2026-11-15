package net.perfectdreams.harmony.gl.framebuffers

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLConstants.GL_COLOR_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_FRAMEBUFFER
import net.perfectdreams.harmony.gl.GLConstants.GL_STENCIL_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLFramebuffer
import net.perfectdreams.harmony.gl.rendertarget.RenderTarget
import net.perfectdreams.harmony.gl.rendertarget.WindowRenderTarget

class Framebuffer(
    gl: GLContext,
    val framebufferId: GLFramebuffer,
    val attachments: FramebufferAttachments,
    override val width: Int,
    override val height: Int,
    val isMultisampled: Boolean
) : AutoCloseable, RenderTarget(gl) {
    fun blitTo(
        target: RenderTarget,
        filter: Int,
        copyColor: Boolean,
        copyDepth: Boolean,
        copyStencil: Boolean
    ) {
        blitRegionTo(
            target,
            filter,
            0,
            0,
            this.width,
            this.height,
            0,
            0,
            target.width,
            target.height,
            copyColor,
            copyDepth,
            copyStencil
        )
    }

    fun blitRegionTo(
        target: RenderTarget,
        filter: Int,
        sourceX: Int,
        sourceY: Int,
        sourceWidth: Int,
        sourceHeight: Int,
        targetX: Int,
        targetY: Int,
        targetWidth: Int,
        targetHeight: Int,
        copyColor: Boolean,
        copyDepth: Boolean,
        copyStencil: Boolean,
    ) {
        require(copyColor || copyDepth || copyStencil) { "A blit needs to copy at least one of color, depth or stencil!" }
        require(!isMultisampled || (sourceWidth == targetWidth && sourceHeight == targetHeight)) { "Cannot scale while copying from a multisampled framebuffer, the source region is ${sourceWidth}x${sourceHeight} but the target region is ${targetWidth}x${targetHeight}!" }
        if (copyDepth || copyStencil) {
            val sourceDepthStencilFormat = requireNotNull(this.attachments.depthStencilAttachment) { "Trying to copy depth and/or stencil, but the source framebuffer does not have a depth buffer!" }
            if (target is Framebuffer) {
                val targetDepthStencilAttachment = requireNotNull(target.attachments.depthStencilAttachment) { "Trying to copy depth and/or stencil, but the target framebuffer does not have a depth buffer!" }
                require(sourceDepthStencilFormat.format == targetDepthStencilAttachment.format) { "Incompatible depth stencil formats! Source is ${sourceDepthStencilFormat.format} and the target is ${targetDepthStencilAttachment.format}, both must use the same format" }
            }
        }

        gl.bindFramebuffer(GLConstants.GL_READ_FRAMEBUFFER, framebufferId)
        val targetFB = when (target) {
            is Framebuffer -> target.framebufferId
            is WindowRenderTarget -> null
            else -> error("Unsupported target $target!")
        }
        gl.bindFramebuffer(GLConstants.GL_DRAW_FRAMEBUFFER, targetFB)

        var mask = 0
        if (copyColor) mask = mask or GL_COLOR_BUFFER_BIT
        if (copyDepth) mask = mask or GL_DEPTH_BUFFER_BIT
        if (copyStencil) mask = mask or GL_STENCIL_BUFFER_BIT

        gl.blitFramebuffer(
            sourceX,
            sourceY,
            sourceX + sourceWidth,
            sourceY + sourceHeight,
            targetX,
            targetY,
            targetX + targetWidth,
            targetY + targetHeight,
            mask,
            filter
        )

        gl.bindFramebuffer(GLConstants.GL_READ_FRAMEBUFFER, null)
        gl.bindFramebuffer(GLConstants.GL_DRAW_FRAMEBUFFER, null)
    }

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