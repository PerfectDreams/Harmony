package net.perfectdreams.harmony.gl

abstract class TextureSource {
    abstract val width: Int
    abstract val height: Int
}

class ByteArrayTextureSource(override val width: Int, override val height: Int, val pixels: ByteArray) : TextureSource()