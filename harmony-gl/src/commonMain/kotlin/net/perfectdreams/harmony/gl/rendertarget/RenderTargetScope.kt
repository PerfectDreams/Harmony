package net.perfectdreams.harmony.gl.rendertarget

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.pipelines.Pipeline
import net.perfectdreams.harmony.gl.pipelines.PipelineScope
import net.perfectdreams.harmony.gl.shaders.ShaderProgram
import net.perfectdreams.harmony.gl.shaders.withProgram

class RenderTargetScope(val gpu: GPUResources, val renderTarget: RenderTarget) {
    fun <T : ShaderProgram> withPipeline(pipeline: Pipeline<T>, block: PipelineScope<T>.() -> (Unit)) {
        if (pipeline.enableDepthTest) {
            gpu.gl.enable(GLConstants.GL_DEPTH_TEST)
        } else {
            gpu.gl.disable(GLConstants.GL_DEPTH_TEST)
        }

        val scope = PipelineScope(pipeline.shader)
        pipeline.shader.withProgram {
            block(scope)
        }
    }
}