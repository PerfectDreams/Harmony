package net.perfectdreams.harmony.gl.webgl2

import web.html.Image
import web.images.ImageData

class BrowserImageDataTextureSource(image: ImageData) : BrowserImageTexImageSource(image, image.width, image.height)