package net.perfectdreams.harmony.gl.webgl2

import web.images.ImageBitmap

class BrowserImageBitmapTextureSource(image: ImageBitmap) : BrowserImageTexImageSource(image, image.width, image.height)