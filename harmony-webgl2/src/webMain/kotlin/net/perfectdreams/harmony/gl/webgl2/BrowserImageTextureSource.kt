package net.perfectdreams.harmony.gl.webgl2

import web.html.Image

class BrowserImageTextureSource(image: Image) : BrowserImageTexImageSource(image, image.width, image.height)