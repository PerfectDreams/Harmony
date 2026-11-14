package net.perfectdreams.harmony.graphics.batcher

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.gl.shaders.ShaderProgram

abstract class SpriteShader(gl: GLContext, programId: GLProgram) : ShaderProgram(gl, programId) {
    abstract val uTexture: UniformSampler2D
}