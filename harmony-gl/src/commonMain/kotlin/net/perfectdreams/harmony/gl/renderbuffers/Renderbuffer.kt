package net.perfectdreams.harmony.gl.renderbuffers

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLRenderbuffer
import net.perfectdreams.harmony.gl.textures.TextureFormat

class Renderbuffer(
    val gl: GLContext,
    val renderbufferId: GLRenderbuffer,
    val format: TextureFormat,
    var width: Int,
    var height: Int,
    val samples: Int
) : AutoCloseable {
    override fun close() {
        gl.deleteRenderbuffer(this.renderbufferId)
    }
}