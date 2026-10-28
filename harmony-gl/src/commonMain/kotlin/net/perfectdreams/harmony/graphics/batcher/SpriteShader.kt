package net.perfectdreams.harmony.graphics.batcher

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.shaders.ShaderProgram

abstract class SpriteShader(gl: GLContext, programId: Int) : ShaderProgram(gl, programId) {
    abstract val uTexture: Uniform1Sampler2D
}