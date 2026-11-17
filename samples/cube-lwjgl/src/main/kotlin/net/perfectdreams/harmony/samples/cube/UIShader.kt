package net.perfectdreams.harmony.samples.cube

import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.batcher.SpriteShader

class UIShader(device: RenderDevice, programId: GLProgram) : SpriteShader(device, programId) {
    val uProjection = this.uniformMatrix4fv("uProjection")
    override val uTexture = this.uniformSampler2D("uTexture")
}