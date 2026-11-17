package net.perfectdreams.harmony.render.framebuffers

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLConstants.GL_COLOR_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_FRAMEBUFFER
import net.perfectdreams.harmony.gl.GLConstants.GL_STENCIL_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLFramebuffer
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.rendertarget.RenderTarget
import net.perfectdreams.harmony.render.rendertarget.WindowRenderTarget
import kotlin.collections.iterator

class Framebuffer(
    val device: RenderDevice,
    val framebufferId: GLFramebuffer,
    val attachments: FramebufferAttachments,
    override val width: Int,
    override val height: Int,
    val initialReadBuffer: Int,
    val initialDrawBuffers: IntArray,
    val isMultisampled: Boolean
) : AutoCloseable, RenderTarget() {
    private val gl
        get() = device.gl

    fun blitTo(
        target: RenderTarget,
        filter: Int,
        copyColor: Boolean,
        copyDepth: Boolean,
        copyStencil: Boolean,
        overrideReadBuffer: Int?,
        overrideDrawBuffers: IntArray?
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
            copyStencil,
            overrideReadBuffer,
            overrideDrawBuffers
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
        overrideReadBuffer: Int?,
        overrideDrawBuffers: IntArray?
    ) {
        require(!device.isInsideARenderTarget) { "You cannot blit framebuffers while inside a render target!" }
        require(copyColor || copyDepth || copyStencil) { "A blit needs to copy at least one of color, depth or stencil!" }
        require(!isMultisampled || (sourceWidth == targetWidth && sourceHeight == targetHeight)) { "Cannot scale while copying from a multisampled framebuffer, the source region is ${sourceWidth}x${sourceHeight} but the target region is ${targetWidth}x${targetHeight}!" }
        require(overrideDrawBuffers == null || target is Framebuffer) { "You can only set overrideDrawBuffers if the target is a framebuffer!" }

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

        if (overrideReadBuffer != null)
            gl.readBuffer(overrideReadBuffer)
        if (overrideDrawBuffers != null)
            gl.drawBuffers(overrideDrawBuffers)

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

        gl.readBuffer(initialReadBuffer)
        if (target is Framebuffer)
            gl.drawBuffers(target.initialDrawBuffers)

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
            if (attachment != null)
                deleteAttachment(attachment)
        }

        attachments.depthStencilAttachment?.let { deleteAttachment(it) }
    }
}

inline fun <T> Framebuffer.withFramebuffer(block: () -> T): T {
    device.gl.bindFramebuffer(GL_FRAMEBUFFER, framebufferId)
    device.gl.viewport(0, 0, this.width, this.height)
    try {
        return block()
    } finally {
        device.gl.bindFramebuffer(GL_FRAMEBUFFER, null)
    }
}