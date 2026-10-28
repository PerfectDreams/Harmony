package net.perfectdreams.harmony.gl.rendertarget

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.framebuffers.Framebuffer
import net.perfectdreams.harmony.gl.pipelines.Pipeline
import net.perfectdreams.harmony.gl.pipelines.PipelineScope
import net.perfectdreams.harmony.gl.shaders.ShaderProgram
import net.perfectdreams.harmony.gl.shaders.withProgram

class RenderTargetScope(val gpu: GPUResources, val renderTarget: RenderTarget) {
    fun <T : ShaderProgram> withPipeline(pipeline: Pipeline<T>, block: PipelineScope<T>.() -> (Unit)) {
        val depthState = pipeline.depth

        if (depthState != null) {
            if (renderTarget is Framebuffer && renderTarget.depthTexture == null)
                error("You cannot enable a depth state on a framebuffer that does not have a depth texture!")

            gpu.gl.enable(GLConstants.GL_DEPTH_TEST)
            gpu.gl.depthFunc(depthState.compare.glValue)
            gpu.gl.depthMask(depthState.write)
        } else {
            // Yes this is weird, but we actually need to set this to revert to the "defaults"
            // Example: If a pipeline uses depthMask=false and another pipeline sets a depth state to null, it WILL cause issues!
            gpu.gl.depthFunc(GLConstants.GL_LESS)
            gpu.gl.depthMask(true)
            gpu.gl.disable(GLConstants.GL_DEPTH_TEST)
        }

        val scope = PipelineScope(pipeline.shader)
        pipeline.shader.withProgram {
            block(scope)
        }
    }
}