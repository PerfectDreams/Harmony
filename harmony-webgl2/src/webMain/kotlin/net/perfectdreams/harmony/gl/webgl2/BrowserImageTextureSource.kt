package net.perfectdreams.harmony.gl.webgl2

import net.perfectdreams.harmony.gl.TextureSource
import web.html.Image

class BrowserImageTextureSource(val image: Image) : TextureSource() {
    override val width = image.width
    override val height = image.height
}