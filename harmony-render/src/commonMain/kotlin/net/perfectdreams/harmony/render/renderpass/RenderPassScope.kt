package net.perfectdreams.harmony.render.renderpass

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.framebuffers.Framebuffer
import net.perfectdreams.harmony.render.pipelines.Pipeline
import net.perfectdreams.harmony.render.pipelines.PipelineScope
import net.perfectdreams.harmony.render.rendertarget.RenderTarget
import net.perfectdreams.harmony.render.scissors.ScissorRect
import net.perfectdreams.harmony.render.shaders.ShaderProgram
import net.perfectdreams.harmony.render.viewports.Viewport

class RenderPassScope(val device: RenderDevice, val renderTarget: RenderTarget, private val initialViewport: Viewport) {
    var activeShaderProgram: ShaderProgram? = null
        private set
    var activeScissor: ScissorRect? = null
        private set
    var isInsideAPipelineScope = false
        private set
    var stateVersion = 0

    fun setViewport(viewport: Viewport?) {
        this.stateVersion++
        val targetViewport = viewport ?: this.initialViewport
        device.gl.viewport(targetViewport.x, targetViewport.y, targetViewport.width, targetViewport.height)

    }

    fun setScissor(scissor: ScissorRect?) {
        this.stateVersion++
        this.activeScissor = scissor

        if (scissor == null) {
            device.gl.disable(GLConstants.GL_SCISSOR_TEST)
            return
        }

        device.gl.enable(GLConstants.GL_SCISSOR_TEST)
        device.gl.scissor(scissor.x, scissor.y, scissor.width, scissor.height)
    }

    fun <T : ShaderProgram> setPipeline(pipeline: Pipeline<T>) {
        check(!isInsideAPipelineScope) { "You cannot set a pipeline while you are inside a pipeline scope!" }
        val depthState = pipeline.depth
        val blendState = pipeline.blend
        val cullState = pipeline.cull

        if (depthState != null) {
            if (renderTarget is Framebuffer) {
                val hasDepth = renderTarget.attachments.depthStencilAttachment?.format?.hasDepth ?: false
                if (!hasDepth)
                    error("You cannot enable a depth state on a framebuffer that does not have a depth stencil attachment!")
            }

            device.gl.enable(GLConstants.GL_DEPTH_TEST)
            device.gl.depthFunc(depthState.compare.glValue)
            device.gl.depthMask(depthState.write)
        } else {
            // Yes this is weird, but we actually need to set this to revert to the "defaults"
            // Example: If a pipeline uses depthMask=false and another pipeline sets a depth state to null, it WILL cause issues!
            device.gl.depthFunc(GLConstants.GL_LESS)
            device.gl.depthMask(true)
            device.gl.disable(GLConstants.GL_DEPTH_TEST)
        }

        if (blendState != null) {
            device.gl.enable(GLConstants.GL_BLEND)
            device.gl.blendFuncSeparate(
                blendState.srcColor,
                blendState.dstColor,
                blendState.srcAlpha,
                blendState.dstAlpha
            )
            device.gl.blendEquationSeparate(
                blendState.colorEquation,
                blendState.alphaEquation
            )
        } else {
            device.gl.disable(GLConstants.GL_BLEND)
        }

        if (cullState != null) {
            device.gl.enable(GLConstants.GL_CULL_FACE)
            device.gl.cullFace(cullState.face.glValue)
        } else {
            device.gl.disable(GLConstants.GL_CULL_FACE)
        }

        setActiveShaderProgram(pipeline.shader)
    }

    fun <T : ShaderProgram> withPipeline(pipeline: Pipeline<T>, block: PipelineScope<T>.() -> (Unit)) {
        try {
            setPipeline(pipeline)
            this.isInsideAPipelineScope = true
            val pipelineScope = PipelineScope(pipeline.shader)
            block(pipelineScope)
        } finally {
            setActiveShaderProgram(null)
            this.isInsideAPipelineScope = false
        }
    }

    private fun setActiveShaderProgram(shaderProgram: ShaderProgram?) {
        this.stateVersion++
        this.activeShaderProgram = shaderProgram
        this.device.gl.useProgram(shaderProgram?.programId)
    }
}