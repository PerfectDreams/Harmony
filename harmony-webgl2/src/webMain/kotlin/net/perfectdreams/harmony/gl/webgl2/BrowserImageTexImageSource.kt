package net.perfectdreams.harmony.gl.webgl2

import net.perfectdreams.harmony.gl.TextureSource
import web.gl.TexImageSource
import web.html.Image

open class BrowserImageTexImageSource(val image: TexImageSource, override val width: Int, override val height: Int) : TextureSource()