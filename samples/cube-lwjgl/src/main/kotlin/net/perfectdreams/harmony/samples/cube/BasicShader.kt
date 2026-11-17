package net.perfectdreams.harmony.samples.cube

import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.shaders.ShaderProgram

class BasicShader(device: RenderDevice, programId: GLProgram) : ShaderProgram(device, programId) {
    val uProjection = this.uniformMatrix4fv("uProjection")
    val uView = this.uniformMatrix4fv("uView")
    val uModel = this.uniformMatrix4fv("uModel")
    val uBlendColor = this.uniform4f("uBlendColor")
    val uCameraPosition = this.uniform3f("uCameraPosition")
    val uTexture = this.uniformSampler2D("uTexture")
}