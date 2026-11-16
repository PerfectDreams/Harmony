package net.perfectdreams.harmony.render.framebuffers

import net.perfectdreams.harmony.render.renderbuffers.Renderbuffer
import net.perfectdreams.harmony.render.textures.Texture
import net.perfectdreams.harmony.render.textures.TextureFormat

sealed class FramebufferAttachment {
    abstract val width: Int
    abstract val height: Int
    abstract val format: TextureFormat
    abstract val samples: Int

    class TextureAttachment(val texture: Texture) : FramebufferAttachment() {
        override val width = texture.width
        override val height = texture.height
        override val format = texture.format
        override val samples = 1
    }

    class RenderbufferAttachment(val renderbuffer: Renderbuffer) : FramebufferAttachment() {
        override val width = renderbuffer.width
        override val height = renderbuffer.height
        override val format = renderbuffer.format
        override val samples = renderbuffer.samples
    }
}