package net.perfectdreams.harmony.render.rendertarget

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.framebuffers.Framebuffer
import net.perfectdreams.harmony.render.pipelines.Pipeline
import net.perfectdreams.harmony.render.pipelines.PipelineScope
import net.perfectdreams.harmony.render.scissors.ScissorRect
import net.perfectdreams.harmony.render.shaders.ShaderProgram
import net.perfectdreams.harmony.render.shaders.withProgram
import net.perfectdreams.harmony.render.viewports.Viewport

class RenderTargetScope(val gpu: RenderDevice, val renderTarget: RenderTarget, private val initialViewport: Viewport) {
    private var isInsideAPipeline = false

    fun setViewport(viewport: Viewport?) {
        require(!this.isInsideAPipeline) { "You cannot call setViewport inside withPipeline!" }
        val targetViewport = viewport ?: this.initialViewport
        gpu.gl.viewport(targetViewport.x, targetViewport.y, targetViewport.width, targetViewport.height)
    }

    fun setScissor(scissor: ScissorRect?) {
        require(!this.isInsideAPipeline) { "You cannot call setScissor inside withPipeline!" }
        if (scissor == null) {
            gpu.gl.disable(GLConstants.GL_SCISSOR_TEST)
            return
        }

        gpu.gl.enable(GLConstants.GL_SCISSOR_TEST)
        gpu.gl.scissor(scissor.x, scissor.y, scissor.width, scissor.height)
    }

    fun <T : ShaderProgram> withPipeline(pipeline: Pipeline<T>, block: PipelineScope<T>.() -> (Unit)) {
        require(!this.isInsideAPipeline) { "You cannot nest withPipeline calls!" }
        try {
            this.isInsideAPipeline = true
            val depthState = pipeline.depth
            val blendState = pipeline.blend
            val cullState = pipeline.cull

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

            if (cullState != null) {
                gpu.gl.enable(GLConstants.GL_CULL_FACE)
                gpu.gl.cullFace(cullState.face.glValue)
            } else {
                gpu.gl.disable(GLConstants.GL_CULL_FACE)
            }

            val scope = PipelineScope(pipeline.shader)
            pipeline.shader.withProgram {
                block(scope)
            }
        } finally {
            this.isInsideAPipeline = false
        }
    }
}