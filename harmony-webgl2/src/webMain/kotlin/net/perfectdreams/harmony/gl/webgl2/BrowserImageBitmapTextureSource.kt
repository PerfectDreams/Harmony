package net.perfectdreams.harmony.gl.webgl2

import net.perfectdreams.harmony.gl.TextureSource
import web.html.Image
import web.images.ImageBitmap

class BrowserImageBitmapTextureSource(val image: ImageBitmap) : TextureSource() {
    override val width = image.width
    override val height = image.height
}