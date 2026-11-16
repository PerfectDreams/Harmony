package net.perfectdreams.harmony.render.pipelines

import net.perfectdreams.harmony.render.shaders.ShaderProgram

class PipelineScope<T : ShaderProgram>(val shader: T)