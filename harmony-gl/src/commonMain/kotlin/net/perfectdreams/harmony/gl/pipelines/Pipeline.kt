package net.perfectdreams.harmony.gl.pipelines

import net.perfectdreams.harmony.gl.shaders.ShaderProgram

class Pipeline<T : ShaderProgram>(val shader: T, val depth: DepthState? = null)