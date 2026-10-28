package net.perfectdreams.harmony.gl

import net.perfectdreams.harmony.gl.GLConstants.GL_DEPTH_TEST
import net.perfectdreams.harmony.gl.framebuffers.Framebuffer
import net.perfectdreams.harmony.gl.framebuffers.withFramebuffer
import net.perfectdreams.harmony.gl.pipelines.PipelineScope
import net.perfectdreams.harmony.gl.rendertarget.RenderTarget
import net.perfectdreams.harmony.gl.rendertarget.RenderTargetScope
import net.perfectdreams.harmony.gl.rendertarget.WindowRenderTarget
import net.perfectdreams.harmony.gl.rendertarget.withRenderTarget
import net.perfectdreams.harmony.gl.shaders.ShaderProgram
import net.perfectdreams.harmony.gl.shaders.withProgram
import net.perfectdreams.harmony.gl.textures.Texture
import net.perfectdreams.harmony.gl.textures.withTexture

class GPUResources(val gl: GLContext, val initialWidth: Int, val initialHeight: Int) {
    val white1x1Texture = create1x1WhiteTexture()
    val windowRenderTarget = WindowRenderTarget(gl, initialWidth, initialHeight)

    private fun create1x1WhiteTexture(): Texture {
        return createTexture(
            ByteArrayTextureSource(1, 1, byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())),
            GLConstants.GL_REPEAT,
            GLConstants.GL_REPEAT,
            GLConstants.GL_NEAREST,
            GLConstants.GL_NEAREST
        )
    }

    /**
     * Creates a texture but does not upload any data to it
     */
    fun createTexture(
        source: TextureSource,
        // horizontal
        wrapS: Int,
        // vertical
        wrapT: Int,
        // minify
        minFilter: Int,
        // magnify
        magFilter: Int
    ): Texture {
        val texture = createTexture(source.width, source.height, wrapS, wrapT, minFilter, magFilter)
        texture.write(source)
        return texture
    }

    /**
     * Creates a texture but does not upload any data to it
     */
    fun createTexture(
        width: Int,
        height: Int,
        // horizontal
        wrapS: Int,
        // vertical
        wrapT: Int,
        // minify
        minFilter: Int,
        // magnify
        magFilter: Int
    ): Texture {
        // --- OpenGL Texture Creation ---

        // 1. Generate and bind a new texture ID
        val textureId = gl.genTextures()
        gl.bindTexture(GLConstants.GL_TEXTURE_2D, textureId)

        // 2. Tell OpenGL how to unpack the RGBA bytes.
        // Each component is 1 byte size
        gl.pixelStorei(GLConstants.GL_UNPACK_ALIGNMENT, 1)

        // 3. Set texture parameters
        // Repeat image in both directions
        gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_WRAP_S, wrapS)
        gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_WRAP_T, wrapT)
        // When stretching the image, pixelate
        gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_MIN_FILTER, minFilter)
        // When shrinking the image, pixelate
        gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_MAG_FILTER, magFilter)

        gl.texImage2D(
            GLConstants.GL_TEXTURE_2D,
            0,
            GLConstants.GL_RGBA,
            width,
            height,
            0,
            GLConstants.GL_RGBA,
            GLConstants.GL_UNSIGNED_BYTE
        )

        return Texture(gl, textureId, width, height)
    }

    fun createDepthTexture(width: Int, height: Int): Texture {
        // Depth textures can't be linearly filtered on WebGL2, and have no mipmaps
        val texture = createTexture(width, height, GLConstants.GL_CLAMP_TO_EDGE, GLConstants.GL_CLAMP_TO_EDGE, GLConstants.GL_NEAREST, GLConstants.GL_NEAREST)
        texture.withTexture {
            // Sized format, required by WebGL2
            gl.texImage2D(GLConstants.GL_TEXTURE_2D, 0, GLConstants.GL_DEPTH_COMPONENT24, width, height, 0, GLConstants.GL_DEPTH_COMPONENT, GLConstants.GL_UNSIGNED_INT)
        }
        return texture
    }

    fun createFramebuffer(
        width: Int,
        height: Int,
        hasDepthTexture: Boolean,
        // horizontal
        wrapS: Int,
        // vertical
        wrapT: Int,
        // minify
        minFilter: Int,
        // magnify
        magFilter: Int
    ): Framebuffer {
        val texture = createTexture(width, height, wrapS, wrapT, minFilter, magFilter)
        val depthTexture = if (hasDepthTexture) createDepthTexture(width, height) else null
        return createFramebuffer(texture, depthTexture)
    }

    fun createFramebuffer(texture: Texture, depthTexture: Texture? = null): Framebuffer {
        val framebufferId = gl.genFramebuffers()

        gl.bindFramebuffer(GLConstants.GL_FRAMEBUFFER, framebufferId)
        gl.framebufferTexture2D(GLConstants.GL_FRAMEBUFFER, GLConstants.GL_COLOR_ATTACHMENT0, GLConstants.GL_TEXTURE_2D, texture.textureId, 0)
        if (depthTexture != null)
            gl.framebufferTexture2D(GLConstants.GL_FRAMEBUFFER, GLConstants.GL_DEPTH_ATTACHMENT, GLConstants.GL_TEXTURE_2D, depthTexture.textureId, 0)
        val status = gl.checkFramebufferStatus(GLConstants.GL_FRAMEBUFFER)
        check(status == GLConstants.GL_FRAMEBUFFER_COMPLETE) { "Framebuffer is incomplete! Status: 0x${status.toString(16)}" }
        gl.bindFramebuffer(GLConstants.GL_FRAMEBUFFER, 0)

        return Framebuffer(gl, framebufferId, texture, depthTexture)
    }

    /**
     * Loads the vertex shader and fragment shader from the [vertexShaderCode] and [fragmentShaderCode]
     */
    fun <T : ShaderProgram> createShader(vertexShaderCode: String, fragmentShaderCode: String, createGameShader: (GLContext, Int) -> (T)): T {
        val programId = createShader(vertexShaderCode, fragmentShaderCode)
        return createGameShader.invoke(gl, programId)
    }

    /**
     * Loads the vertex shader and fragment shader
     */
    fun createShader(vertexShaderCode: String, fragmentShaderCode: String): Int {
        val vertexShaderId = gl.createShader(GLConstants.GL_VERTEX_SHADER)
        val fragmentShaderId = gl.createShader(GLConstants.GL_FRAGMENT_SHADER)

        // Compile Vertex Shader
        checkAndCompile(vertexShaderId, vertexShaderCode)

        // Compile Fragment Shader
        checkAndCompile(fragmentShaderId, fragmentShaderCode)

        val programId = gl.createProgram()
        gl.attachShader(programId, vertexShaderId)
        gl.attachShader(programId, fragmentShaderId)
        gl.linkProgram(programId)

        // Check the program
        val result = gl.getProgrami(programId, GLConstants.GL_LINK_STATUS)
        val infoLog = gl.getProgramInfoLog(programId, GLConstants.GL_INFO_LOG_LENGTH)

        // YES DON'T FORGET THAT WE NEED TO USE GLConstants.GL_TRUE!!
        // I was checking using == 0 and of course that doesn't work because that means FALSE (i think)
        if (result != GLConstants.GL_TRUE) {
            error("Something went wrong while linking shader! Status: $result; Info: $infoLog")
        }

        gl.detachShader(programId, vertexShaderId)
        gl.detachShader(programId, fragmentShaderId)

        gl.deleteShader(vertexShaderId)
        gl.deleteShader(fragmentShaderId)

        return programId
    }

    private fun checkAndCompile(shaderId: Int, code: String) {
        // Compile Shader
        gl.shaderSource(shaderId, code)
        gl.compileShader(shaderId)

        // Check Shader
        val result = gl.getShaderi(shaderId, GLConstants.GL_COMPILE_STATUS)
        val infoLog = gl.getShaderInfoLog(shaderId, GLConstants.GL_INFO_LOG_LENGTH)

        // YES DON'T FORGET THAT WE NEED TO USE GLConstants.GL_TRUE!!
        // I was checking using == 0 and of course that doesn't work because that means FALSE (i think)
        if (result != GLConstants.GL_TRUE) {
            error("Something went wrong while compiling shader $shaderId! Status: $result; Info: $infoLog")
        }
    }

    // RenderTarget related things are things that affect the ENTIRE RenderTarget (like clear)
    fun onRenderTarget(target: RenderTarget, block: RenderTargetScope.() -> (Unit)) {
        val scope = RenderTargetScope(this, target)
        target.withRenderTarget {
            block(scope)
        }
    }
}