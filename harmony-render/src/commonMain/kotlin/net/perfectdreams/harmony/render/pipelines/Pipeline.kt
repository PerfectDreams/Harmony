package net.perfectdreams.harmony.render.pipelines

import net.perfectdreams.harmony.render.shaders.ShaderProgram

class Pipeline<T : ShaderProgram>(
    val shader: T,
    val depth: DepthState? = null,
    val blend: BlendState? = null,
)