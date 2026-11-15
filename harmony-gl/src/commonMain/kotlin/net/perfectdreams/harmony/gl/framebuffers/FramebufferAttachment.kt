package net.perfectdreams.harmony.gl.framebuffers

import net.perfectdreams.harmony.gl.renderbuffers.Renderbuffer
import net.perfectdreams.harmony.gl.textures.Texture
import net.perfectdreams.harmony.gl.textures.TextureFormat

sealed class FramebufferAttachment {
    abstract val width: Int
    abstract val height: Int
    abstract val format: TextureFormat

    class TextureAttachment(val texture: Texture) : FramebufferAttachment() {
        override val width = texture.width
        override val height = texture.height
        override val format = texture.format
    }

    class RenderbufferAttachment(val renderbuffer: Renderbuffer) : FramebufferAttachment() {
        override val width = renderbuffer.width
        override val height = renderbuffer.height
        override val format = renderbuffer.format
    }
}