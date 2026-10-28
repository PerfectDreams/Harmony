package net.perfectdreams.harmony.gl.pipelines

import net.perfectdreams.harmony.gl.shaders.ShaderProgram

class PipelineScope<T : ShaderProgram>(val shader: T)