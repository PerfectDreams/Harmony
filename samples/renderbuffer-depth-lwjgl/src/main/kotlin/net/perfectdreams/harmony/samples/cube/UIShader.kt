package net.perfectdreams.harmony.samples.cube

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.render.batcher.SpriteShader

class UIShader(gl: GLContext, programId: GLProgram) : SpriteShader(gl, programId) {
    val uProjection = this.uniformMatrix4fv("uProjection")
    override val uTexture = this.uniformSampler2D("uTexture")
}