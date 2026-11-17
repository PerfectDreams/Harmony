package net.perfectdreams.harmony.render.batcher

import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.shaders.ShaderProgram

abstract class SpriteShader(device: RenderDevice, programId: GLProgram) : ShaderProgram(device, programId) {
    abstract val uTexture: UniformSampler2D
}