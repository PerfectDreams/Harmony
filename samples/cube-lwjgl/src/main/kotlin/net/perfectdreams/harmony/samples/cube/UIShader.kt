package net.perfectdreams.harmony.samples.cube

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.shaders.ShaderProgram
import net.perfectdreams.harmony.graphics.batcher.SpriteShader

class UIShader(gl: GLContext, programId: Int) : SpriteShader(gl, programId) {
    val uProjection = this.uniformMatrix4fv("uProjection")
    override val uTexture = this.uniform1Sampler2D("uTexture")
}