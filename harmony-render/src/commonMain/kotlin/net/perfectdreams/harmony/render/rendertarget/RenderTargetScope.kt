package net.perfectdreams.harmony.render.rendertarget

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.framebuffers.Framebuffer
import net.perfectdreams.harmony.render.pipelines.Pipeline
import net.perfectdreams.harmony.render.pipelines.PipelineScope
import net.perfectdreams.harmony.render.shaders.ShaderProgram
import net.perfectdreams.harmony.render.shaders.withProgram

class RenderTargetScope(val gpu: RenderDevice, val renderTarget: RenderTarget) {
    fun <T : ShaderProgram> withPipeline(pipeline: Pipeline<T>, block: PipelineScope<T>.() -> (Unit)) {
        val depthState = pipeline.depth
        val blendState = pipeline.blend

        if (depthState != null) {
            if (renderTarget is Framebuffer) {
                val hasDepth = renderTarget.attachments.depthStencilAttachment?.format?.hasDepth ?: false
                if (!hasDepth)
                    error("You cannot enable a depth state on a framebuffer that does not have a depth stencil attachment!")
            }

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

        if (blendState != null) {
            gpu.gl.enable(GLConstants.GL_BLEND)
            gpu.gl.blendFuncSeparate(
                blendState.srcColor,
                blendState.dstColor,
                blendState.srcAlpha,
                blendState.dstAlpha
            )
            gpu.gl.blendEquationSeparate(
                blendState.colorEquation,
                blendState.alphaEquation
            )
        } else {
            gpu.gl.disable(GLConstants.GL_BLEND)
        }

        val scope = PipelineScope(pipeline.shader)
        pipeline.shader.withProgram {
            block(scope)
        }
    }
}