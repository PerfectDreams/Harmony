package net.perfectdreams.harmony.render.framebuffers

class FramebufferAttachments(
    val colorAttachments: List<FramebufferAttachment?>,
    val depthStencilAttachment: FramebufferAttachment?
) {
    val color0 get() = colorAttachments[0]
    val color1 get() = colorAttachments[1]
    val color2 get() = colorAttachments[2]
    val color3 get() = colorAttachments[3]

    val color0Texture get() = (color0 as FramebufferAttachment.TextureAttachment).texture
    val color1Texture get() = (color1 as FramebufferAttachment.TextureAttachment).texture
    val color2Texture get() = (color2 as FramebufferAttachment.TextureAttachment).texture
    val color3Texture get() = (color3 as FramebufferAttachment.TextureAttachment).texture

    val depthStencil get() = depthStencilAttachment
    val depthStencilTexture get() = (depthStencil as FramebufferAttachment.TextureAttachment).texture
}