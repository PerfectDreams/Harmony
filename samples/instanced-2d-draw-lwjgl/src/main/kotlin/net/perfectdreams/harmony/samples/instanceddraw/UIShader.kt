package net.perfectdreams.harmony.samples.instanceddraw

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.shaders.ShaderProgram

class UIShader(gl: RenderDevice, programId: GLProgram) : ShaderProgram(gl, programId) {
    val uProjection = this.uniformMatrix4fv("uProjection")
    val uTexture = this.uniformSampler2D("uTexture")
}