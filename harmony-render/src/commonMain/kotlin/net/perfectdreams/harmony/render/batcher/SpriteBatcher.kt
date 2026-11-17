package net.perfectdreams.harmony.render.batcher

import net.perfectdreams.harmony.gl.GLConstants.GL_ARRAY_BUFFER
import net.perfectdreams.harmony.gl.GLConstants.GL_DYNAMIC_DRAW
import net.perfectdreams.harmony.gl.GLConstants.GL_TEXTURE0
import net.perfectdreams.harmony.gl.GLConstants.GL_TEXTURE_2D
import net.perfectdreams.harmony.gl.GLConstants.GL_TRIANGLES
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.pipelines.BlendState
import net.perfectdreams.harmony.render.pipelines.Pipeline
import net.perfectdreams.harmony.render.renderpass.RenderPassScope
import net.perfectdreams.harmony.render.scissors.ScissorRect
import net.perfectdreams.harmony.render.shaders.ShaderProgram
import net.perfectdreams.harmony.render.textures.Texture
import net.perfectdreams.harmony.render.vertex.buildVertexAttributes

/**
 * A OpenGL sprite batcher, by combining all drawn sprites into a single draw call, we can draw everything at once, reducing draw calls
 */
class SpriteBatcher(val device: RenderDevice, spritesPerBatch: Int = 1024) {
    companion object {
        private const val VERTICES_PER_SPRITE = 6

        private val vertexAttributes = buildVertexAttributes {
            this.vec2(0) // position
            this.vec2(1) // uv
            this.vec4(2) // blend
        }
    }

    private val vao = device.gl.genVertexArrays()
    private val vbo = device.gl.genBuffers()
    private val batch = FloatArray(spritesPerBatch * VERTICES_PER_SPRITE * vertexAttributes.totalSize)
    private var currentIndex = 0
    private var activeBatchTexture: Texture? = null
    private var renderPassScope: RenderPassScope? = null
    private var stateVersion = -1

    init {
        device.gl.bindVertexArray(vao)
        device.gl.bindBuffer(GL_ARRAY_BUFFER, vbo)
        device.gl.bufferData(GL_ARRAY_BUFFER, batch, GL_DYNAMIC_DRAW)

        vertexAttributes.apply(device.gl)
    }

    fun <T : SpriteShader> createSpritePipeline(shader: T) = Pipeline(shader, null, BlendState.AlphaBlending)

    fun drawBatch(block: SpriteBatcherScope.() -> Unit) {
        val scope = createDrawingScope()
        block(scope)
        flush()
        scope.disposed = true
    }

    /**
     * Creates a drawing scope for the given shader
     *
     * Useful when drawing calls can't happen inside a [drawBatch] in a natural manner
     */
    fun createDrawingScope() = SpriteBatcherScope(this)

    private fun setActiveTexture(texture: Texture) {
        if (this.activeBatchTexture != texture)
            flush()
        this.activeBatchTexture = texture
    }

    private fun flushIfNeeded(required: Int) {
        if ((this.currentIndex + required) > this.batch.size)
            flush()
    }

    fun flush() {
        val shader = checkNotNull(device.activeRenderPassScope?.activeShaderProgram as? SpriteShader) { "There isn't a SpriteShader bound!" }

        val activeTexture = this.activeBatchTexture ?: return
        if (this.currentIndex == 0)
            return

        check(device.activeRenderPassScope == renderPassScope)
        check(device.activeRenderPassScope?.stateVersion == stateVersion) { "Invalid State Version! Stored version is $stateVersion but the active render pass scope version is ${device.activeRenderPassScope?.stateVersion}! Did you forget to flush?" }

        device.gl.activeTexture(GL_TEXTURE0)
        device.gl.bindTexture(GL_TEXTURE_2D, activeTexture.textureId)

        device.gl.bindBuffer(GL_ARRAY_BUFFER, this.vbo)
        device.gl.bufferSubData(GL_ARRAY_BUFFER, 0, this.batch)

        shader.uTexture.set(0, activeTexture)

        device.gl.bindVertexArray(vao)
        device.gl.drawArrays(GL_TRIANGLES, 0, currentIndex / vertexAttributes.totalSize)

        this.currentIndex = 0
        // We don't need to clear the current batch even though it has TRASH in it
    }

    class SpriteBatcherScope(val batcher: SpriteBatcher) {
        val renderPassScope = checkNotNull(batcher.device.activeRenderPassScope) { "You can only create a SpriteBatcherScope inside a RenderPass!" }
        var disposed = false

        fun drawTriangle(
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            x3: Float,
            y3: Float,
            u1: Float,
            v1: Float,
            u2: Float,
            v2: Float,
            u3: Float,
            v3: Float,
            texture: Texture,
            blend1R: Float,
            blend1G: Float,
            blend1B: Float,
            blend1A: Float,
            blend2R: Float,
            blend2G: Float,
            blend2B: Float,
            blend2A: Float,
            blend3R: Float,
            blend3G: Float,
            blend3B: Float,
            blend3A: Float,
        ) {
            drawVertexGroup(3, texture) {
                // top-left
                drawVertex(x1, y1, u1, v1, blend1R, blend1G, blend1B, blend1A)

                // top-right
                drawVertex(x2, y2, u2, v2, blend2R, blend2G, blend2B, blend2A)

                // bottom-left
                drawVertex(x3, y3, u3, v3, blend3R, blend3G, blend3B, blend3A)
            }
        }

        // Clockwise pattern for the coordinates
        fun drawQuad(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            u1: Float,
            v1: Float,
            u2: Float,
            v2: Float,
            texture: Texture
        ) = drawQuad(
            x,
            y,
            width,
            height,
            u1,
            v1,
            u2,
            v2,
            texture,
            1f,
            1f,
            1f,
            1f,
        )

        // Clockwise pattern for the coordinates
        fun drawQuad(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            u1: Float,
            v1: Float,
            u2: Float,
            v2: Float,
            texture: Texture,
            blendR: Float,
            blendG: Float,
            blendB: Float,
            blendA: Float,
        ) = drawQuad(
            x,
            y,
            x + width,
            y,
            x + width,
            y + height,
            x,
            y + height,
            u1,
            v1,
            u2,
            v2,
            texture,
            blendR,
            blendG,
            blendB,
            blendA,
            blendR,
            blendG,
            blendB,
            blendA,
            blendR,
            blendG,
            blendB,
            blendA,
            blendR,
            blendG,
            blendB,
            blendA,
        )

        // Clockwise pattern for the coordinates
        fun drawQuad(
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            x3: Float,
            y3: Float,
            x4: Float,
            y4: Float,
            u1: Float,
            v1: Float,
            u2: Float,
            v2: Float,
            texture: Texture,
            blend1R: Float,
            blend1G: Float,
            blend1B: Float,
            blend1A: Float,
            blend2R: Float,
            blend2G: Float,
            blend2B: Float,
            blend2A: Float,
            blend3R: Float,
            blend3G: Float,
            blend3B: Float,
            blend3A: Float,
            blend4R: Float,
            blend4G: Float,
            blend4B: Float,
            blend4A: Float,
        ) {
            drawVertexGroup(6, texture) {
                // top-left
                drawVertex(x1, y1, u1, v1, blend1R, blend1G, blend1B, blend1A)

                // bottom-right
                drawVertex(x3, y3, u2, v2, blend3R, blend3G, blend3B, blend3A)

                // bottom-left
                drawVertex(x4, y4, u1, v2, blend4R, blend4G, blend4B, blend4A)

                // top-left
                drawVertex(x1, y1, u1, v1, blend1R, blend1G, blend1B, blend1A)

                // top-right
                drawVertex(x2, y2, u2, v1, blend2R, blend2G, blend2B, blend2A)

                // bottom-right
                drawVertex(x3, y3, u2, v2, blend3R, blend3G, blend3B, blend3A)
            }
        }

        fun drawVertexGroup(vertexCount: Int, texture: Texture, block: SpriteBatcherScope.() -> Unit) {
            batcher.flushIfNeeded(vertexCount * vertexAttributes.totalSize)
            batcher.setActiveTexture(texture)

            block(this)
        }

        fun drawVertex(x: Float, y: Float, u: Float, v: Float, blendR: Float, blendG: Float, blendB: Float, blendA: Float) {
            check(!disposed) { "SpriteBatcherScope is already disposed!" }

            if (batcher.currentIndex == 0) {
                batcher.renderPassScope = renderPassScope
                batcher.stateVersion = renderPassScope.stateVersion
            }

            batcher.batch[batcher.currentIndex++] = x; batcher.batch[batcher.currentIndex++] = y;
            batcher.batch[batcher.currentIndex++] = u; batcher.batch[batcher.currentIndex++] = v;

            batcher.batch[batcher.currentIndex++] = blendR
            batcher.batch[batcher.currentIndex++] = blendG
            batcher.batch[batcher.currentIndex++] = blendB
            batcher.batch[batcher.currentIndex++] = blendA
        }

        fun flush() = batcher.flush()
    }
}