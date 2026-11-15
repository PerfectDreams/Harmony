package net.perfectdreams.harmony.gl.framebuffers

class FramebufferAttachments(
    val colorAttachments: Map<Int, FramebufferAttachment>,
    val depthStencilAttachment: FramebufferAttachment?
) {
    init {
        require(colorAttachments.isEmpty() || colorAttachments.keys.any { it >= 0 }) { "You can't have negative color attachment indices!" }
    }

    val color0 get() = colorAttachments[0]
    val color1 get() = colorAttachments[1]
    val color2 get() = colorAttachments[2]
    val color3 get() = colorAttachments[3]

    val color0Texture get() = (color0 as FramebufferAttachment.TextureAttachment).texture
    val color1Texture get() = (color1 as FramebufferAttachment.TextureAttachment).texture
    val color2Texture get() = (color2 as FramebufferAttachment.TextureAttachment).texture
    val color3Texture get() = (color3 as FramebufferAttachment.TextureAttachment).texture
}