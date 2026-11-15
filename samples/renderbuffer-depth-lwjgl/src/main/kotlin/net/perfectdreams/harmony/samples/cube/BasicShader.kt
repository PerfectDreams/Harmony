package net.perfectdreams.harmony.samples.cube

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.gl.shaders.ShaderProgram

class BasicShader(gl: GLContext, programId: GLProgram) : ShaderProgram(gl, programId) {
    val uProjection = this.uniformMatrix4fv("uProjection")
    val uView = this.uniformMatrix4fv("uView")
    val uModel = this.uniformMatrix4fv("uModel")
    val uCameraPosition = this.uniform3f("uCameraPosition")
    val uTexture = this.uniformSampler2D("uTexture")
}