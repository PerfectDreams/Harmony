package net.perfectdreams.harmony.gl

import net.perfectdreams.harmony.gl.framebuffers.Framebuffer
import net.perfectdreams.harmony.gl.pipelines.CompareFunc
import net.perfectdreams.harmony.gl.rendertarget.RenderTarget
import net.perfectdreams.harmony.gl.rendertarget.RenderTargetScope
import net.perfectdreams.harmony.gl.rendertarget.WindowRenderTarget
import net.perfectdreams.harmony.gl.rendertarget.withRenderTarget
import net.perfectdreams.harmony.gl.shaders.ShaderProgram
import net.perfectdreams.harmony.gl.textures.Texture
import net.perfectdreams.harmony.gl.textures.TextureArray
import net.perfectdreams.harmony.gl.textures.TextureGrid

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

    fun createTextureArray(
        grid: TextureGrid,
        // horizontal
        wrapS: Int,
        // vertical
        wrapT: Int,
        // minify
        minFilter: Int,
        // magnify
        magFilter: Int
    ): TextureArray {
        val texture = createTextureArray(grid.cellWidth, grid.cellHeight, grid.cells, wrapS, wrapT, minFilter, magFilter)
        texture.writeAll(grid.source)
        return texture
    }

    /**
     * Creates a texture array but does not upload any data to it
     */
    fun createTextureArray(
        layerWidth: Int,
        layerHeight: Int,
        layerCount: Int,
        // horizontal
        wrapS: Int,
        // vertical
        wrapT: Int,
        // minify
        minFilter: Int,
        // magnify
        magFilter: Int
    ): TextureArray {
        val textureArray = gl.genTextures()
        gl.bindTexture(GLConstants.GL_TEXTURE_2D_ARRAY, textureArray)
        gl.texImage3D(GLConstants.GL_TEXTURE_2D_ARRAY, 0, GLConstants.GL_RGBA8, layerWidth, layerHeight, layerCount, 0, GLConstants.GL_RGBA, GLConstants.GL_UNSIGNED_BYTE)
        gl.texParameteri(GLConstants.GL_TEXTURE_2D_ARRAY, GLConstants.GL_TEXTURE_MAG_FILTER, magFilter)
        gl.texParameteri(GLConstants.GL_TEXTURE_2D_ARRAY, GLConstants.GL_TEXTURE_MIN_FILTER, minFilter)
        gl.texParameteri(GLConstants.GL_TEXTURE_2D_ARRAY, GLConstants.GL_TEXTURE_WRAP_S, wrapS)
        gl.texParameteri(GLConstants.GL_TEXTURE_2D_ARRAY, GLConstants.GL_TEXTURE_WRAP_T, wrapT)
        return TextureArray(gl, textureArray, layerCount, layerWidth, layerHeight)
    }

    /**
     * Creates a depth texture, for framebuffer depth attachments
     *
     * With a [compareFunc] the texture is a shadow map, it must be sampled with a `sampler2DShadow`, which returns how lit the fragment is (0..1) instead of the depth
     *
     * Without a [compareFunc] the texture returns the raw depth when sampled with a `sampler2D`
     */
    fun createDepthTexture(width: Int, height: Int, compareFunc: CompareFunc?): Texture {
        val textureId = gl.genTextures()
        gl.bindTexture(GLConstants.GL_TEXTURE_2D, textureId)

        // Depth outside of the texture is meaningless, so it's clamped instead of repeated
        gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_WRAP_S, GLConstants.GL_CLAMP_TO_EDGE)
        gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_WRAP_T, GLConstants.GL_CLAMP_TO_EDGE)

        if (compareFunc != null) {
            gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_COMPARE_MODE, GLConstants.GL_COMPARE_REF_TO_TEXTURE)
            gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_COMPARE_FUNC, compareFunc.glValue)
            // With compare mode, LINEAR blends the result of 4 comparisons, so shadow edges are smooth
            gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_MIN_FILTER, GLConstants.GL_LINEAR)
            gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_MAG_FILTER, GLConstants.GL_LINEAR)
        } else {
            // WebGL2 can't linearly filter depth textures without compare mode
            gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_MIN_FILTER, GLConstants.GL_NEAREST)
            gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_MAG_FILTER, GLConstants.GL_NEAREST)
        }

        // WebGL2 requires UNSIGNED_INT for DEPTH_COMPONENT24
        gl.texImage2D(GLConstants.GL_TEXTURE_2D, 0, GLConstants.GL_DEPTH_COMPONENT24, width, height, 0, GLConstants.GL_DEPTH_COMPONENT, GLConstants.GL_UNSIGNED_INT)

        return Texture(gl, textureId, width, height)
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
        val depthTexture = if (hasDepthTexture) createDepthTexture(width, height, compareFunc = null) else null
        return createFramebuffer(texture, depthTexture)
    }

    fun createFramebuffer(colorTexture: Texture?, depthTexture: Texture?): Framebuffer {
        val framebufferId = gl.genFramebuffers()

        gl.bindFramebuffer(GLConstants.GL_FRAMEBUFFER, framebufferId)
        if (colorTexture != null) {
            gl.framebufferTexture2D(GLConstants.GL_FRAMEBUFFER, GLConstants.GL_COLOR_ATTACHMENT0, GLConstants.GL_TEXTURE_2D, colorTexture.textureId, 0)
        } else {
            // Framebuffers draw to and read from COLOR_ATTACHMENT0 by default, without a color attachment desktop OpenGL (pre-4.1) considers the framebuffer incomplete
            // OpenGL (Medium): [Other] FBO incomplete: missing drawbuffer [0]
            // Test it by using "MESA_GL_VERSION_OVERRIDE=3.3 MESA_EXTENSION_OVERRIDE="-GL_ARB_ES2_compatibility""
            gl.drawBuffers(intArrayOf(GLConstants.GL_NONE))
            gl.readBuffer(GLConstants.GL_NONE)
        }
        if (depthTexture != null)
            gl.framebufferTexture2D(GLConstants.GL_FRAMEBUFFER, GLConstants.GL_DEPTH_ATTACHMENT, GLConstants.GL_TEXTURE_2D, depthTexture.textureId, 0)
        val status = gl.checkFramebufferStatus(GLConstants.GL_FRAMEBUFFER)
        check(status == GLConstants.GL_FRAMEBUFFER_COMPLETE) { "Framebuffer is incomplete! Status: 0x${status.toString(16)}" }
        gl.bindFramebuffer(GLConstants.GL_FRAMEBUFFER, 0)

        return Framebuffer(gl, framebufferId, colorTexture, depthTexture)
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

        if (infoLog.isNotBlank())
            println("Shader program link log: $infoLog")

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

        if (infoLog.isNotBlank())
            println("Shader program link log: $infoLog")

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