package net.perfectdreams.harmony.gl

import net.perfectdreams.harmony.gl.GLConstants.GL_FRAMEBUFFER
import net.perfectdreams.harmony.gl.GLConstants.GL_RENDERBUFFER
import net.perfectdreams.harmony.gl.framebuffers.Framebuffer
import net.perfectdreams.harmony.gl.framebuffers.FramebufferAttachment
import net.perfectdreams.harmony.gl.framebuffers.FramebufferAttachments
import net.perfectdreams.harmony.gl.framebuffers.withFramebuffer
import net.perfectdreams.harmony.gl.pipelines.CompareFunc
import net.perfectdreams.harmony.gl.renderbuffers.Renderbuffer
import net.perfectdreams.harmony.gl.rendertarget.RenderTarget
import net.perfectdreams.harmony.gl.rendertarget.RenderTargetScope
import net.perfectdreams.harmony.gl.rendertarget.WindowRenderTarget
import net.perfectdreams.harmony.gl.shaders.ShaderProgram
import net.perfectdreams.harmony.gl.textures.Texture
import net.perfectdreams.harmony.gl.textures.TextureArray
import net.perfectdreams.harmony.gl.textures.TextureFormat
import net.perfectdreams.harmony.gl.textures.TextureGrid
import net.perfectdreams.harmony.gl.textures.withTexture

class GPUResources(val gl: GLContext) {
    val white1x1Texture = create1x1Texture(0xFF, 0xFF, 0xFF, 0xFF)

    fun create1x1Texture(red: Int, green: Int, blue: Int, alpha: Int) = createTexture(
        ByteArrayTextureSource(1, 1, byteArrayOf(red.toByte(), green.toByte(), blue.toByte(), alpha.toByte())),
        GLConstants.GL_REPEAT,
        GLConstants.GL_REPEAT,
        GLConstants.GL_NEAREST,
        GLConstants.GL_NEAREST
    )

    /**
     * Creates a texture from the [source]
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
        val texture = createTexture(source.width, source.height, TextureFormat.RGBA8, wrapS, wrapT, minFilter, magFilter)
        texture.write(source)
        return texture
    }

    /**
     * Creates a texture but does not upload any data to it
     */
    fun createTexture(
        width: Int,
        height: Int,
        format: TextureFormat,
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
            format.internalFormat,
            width,
            height,
            0,
            format.format,
            format.type
        )

        return Texture(gl, textureId, format, width, height)
    }

    fun createRenderbuffer(
        width: Int,
        height: Int,
        format: TextureFormat,
        samples: Int
    ): Renderbuffer {
        require(samples >= 1) { "Renderbuffer samples must be 1 or higher, but it was $samples!" }
        val renderbufferId = gl.genRenderbuffers()
        gl.bindRenderbuffer(GL_RENDERBUFFER, renderbufferId)
        if (samples == 1) {
            gl.renderbufferStorage(GL_RENDERBUFFER, format.internalFormat, width, height)
        } else {
            gl.renderbufferStorageMultisample(GL_RENDERBUFFER, samples, format.internalFormat, width, height)
        }
        return Renderbuffer(gl, renderbufferId, format, width, height, samples)
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
    fun createDepthTexture(width: Int, height: Int, format: TextureFormat, compareFunc: CompareFunc?): Texture {
        require(format.hasDepth) { "Depth textures need a depth format, but $format was used!" }

        val filter = if (compareFunc != null) {
            // With compare mode, LINEAR blends the result of 4 comparisons, so shadow edges are smooth
            GLConstants.GL_LINEAR
        } else {
            // WebGL2 can't linearly filter depth textures without compare mode
            GLConstants.GL_NEAREST
        }

        val texture = createTexture(
            width,
            height,
            format,
            // Depth outside of the texture is meaningless, so it's clamped instead of repeated
            GLConstants.GL_CLAMP_TO_EDGE,
            GLConstants.GL_CLAMP_TO_EDGE,
            filter,
            filter
        )

        if (compareFunc != null) {
            texture.withTexture {
                gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_COMPARE_MODE, GLConstants.GL_COMPARE_REF_TO_TEXTURE)
                gl.texParameteri(GLConstants.GL_TEXTURE_2D, GLConstants.GL_TEXTURE_COMPARE_FUNC, compareFunc.glValue)
            }
        }

        return texture
    }

    fun createFramebuffer(
        width: Int,
        height: Int,
        depthTextureFormat: TextureFormat?,
        // horizontal
        wrapS: Int,
        // vertical
        wrapT: Int,
        // minify
        minFilter: Int,
        // magnify
        magFilter: Int
    ): Framebuffer {
        val texture = createTexture(width, height, TextureFormat.RGBA8, wrapS, wrapT, minFilter, magFilter)
        val depthTexture = if (depthTextureFormat != null) createDepthTexture(width, height, depthTextureFormat, compareFunc = null) else null
        return createFramebuffer(texture, depthTexture)
    }

    fun createFramebuffer(colorTexture: Texture?, depthTexture: Texture?): Framebuffer {
        val colorAttachments = mutableMapOf<Int, FramebufferAttachment>()
        if (colorTexture != null) {
            colorAttachments[0] = FramebufferAttachment.TextureAttachment(colorTexture)
        }
        return createFramebuffer(FramebufferAttachments(colorAttachments, depthTexture?.let { FramebufferAttachment.TextureAttachment(it) }))
    }

    fun createFramebuffer(attachments: FramebufferAttachments): Framebuffer {
        val framebufferId = gl.genFramebuffers()

        gl.bindFramebuffer(GLConstants.GL_FRAMEBUFFER, framebufferId)

        var targetWidth = -1
        var targetHeight = -1
        var targetSamples = -1

        fun updateTargetDimensions(width: Int, height: Int, samples: Int) {
            if (targetWidth == -1)
                targetWidth = width
            if (targetHeight == -1)
                targetHeight = height
            if (targetSamples == -1)
                targetSamples = samples
            require(targetWidth == width && targetHeight == height) { "Found a ${width}x${height} attachment on a ${targetWidth}x${targetHeight} framebuffer!" }
            require(targetSamples == samples) { "Found a $samples samples attachment on a $targetSamples samples framebuffer!" }
        }

        for ((attachmentIndex, attachment) in attachments.colorAttachments) {
            updateTargetDimensions(attachment.width, attachment.height, attachment.samples)
            when (attachment) {
                is FramebufferAttachment.TextureAttachment -> gl.framebufferTexture2D(GLConstants.GL_FRAMEBUFFER, GLConstants.GL_COLOR_ATTACHMENT0 + attachmentIndex, GLConstants.GL_TEXTURE_2D, attachment.texture.textureId, 0)
                is FramebufferAttachment.RenderbufferAttachment -> gl.framebufferRenderbuffer(GLConstants.GL_FRAMEBUFFER, GLConstants.GL_COLOR_ATTACHMENT0 + attachmentIndex, GL_RENDERBUFFER, attachment.renderbuffer.renderbufferId)
            }
        }

        if (attachments.colorAttachments.isEmpty()) {
            // Framebuffers draw to and read from COLOR_ATTACHMENT0 by default, without a color attachment desktop OpenGL (pre-4.1) considers the framebuffer incomplete
            // OpenGL (Medium): [Other] FBO incomplete: missing drawbuffer [0]
            // Test it by using "MESA_GL_VERSION_OVERRIDE=3.3 MESA_EXTENSION_OVERRIDE="-GL_ARB_ES2_compatibility""
            gl.drawBuffers(intArrayOf(GLConstants.GL_NONE))
            gl.readBuffer(GLConstants.GL_NONE)
        } else {
            val max = attachments.colorAttachments.keys.max()

            val array = IntArray(max + 1)

            for (i in 0..max) {
                val contains = attachments.colorAttachments.containsKey(i)
                array[i] = if (contains)
                    GLConstants.GL_COLOR_ATTACHMENT0 + i
                else
                    GLConstants.GL_NONE
            }

            gl.drawBuffers(array)
            gl.readBuffer(GLConstants.GL_COLOR_ATTACHMENT0 + attachments.colorAttachments.keys.min())
        }

        if (attachments.depthStencilAttachment != null) {
            updateTargetDimensions(attachments.depthStencilAttachment.width, attachments.depthStencilAttachment.height, attachments.depthStencilAttachment.samples)
            when (attachments.depthStencilAttachment) {
                is FramebufferAttachment.TextureAttachment -> gl.framebufferTexture2D(GLConstants.GL_FRAMEBUFFER, attachments.depthStencilAttachment.format.depthStencilAttachmentPoint, GLConstants.GL_TEXTURE_2D, attachments.depthStencilAttachment.texture.textureId, 0)
                is FramebufferAttachment.RenderbufferAttachment -> gl.framebufferRenderbuffer(GLConstants.GL_FRAMEBUFFER, attachments.depthStencilAttachment.format.depthStencilAttachmentPoint, GL_RENDERBUFFER, attachments.depthStencilAttachment.renderbuffer.renderbufferId)
            }
        }

        val status = gl.checkFramebufferStatus(GLConstants.GL_FRAMEBUFFER)
        check(status == GLConstants.GL_FRAMEBUFFER_COMPLETE) { "Framebuffer is incomplete! Status: 0x${status.toString(16)}" }
        gl.bindFramebuffer(GLConstants.GL_FRAMEBUFFER, null)

        return Framebuffer(gl, framebufferId, attachments, targetWidth, targetHeight, targetSamples != 1)
    }

    /**
     * Loads the vertex shader and fragment shader from the [vertexShaderCode] and [fragmentShaderCode]
     */
    fun <T : ShaderProgram> createShader(vertexShaderCode: String, fragmentShaderCode: String, createGameShader: (GLContext, GLProgram) -> (T)): T {
        val programId = createShader(vertexShaderCode, fragmentShaderCode)
        return createGameShader.invoke(gl, programId)
    }

    /**
     * Loads the vertex shader and fragment shader
     */
    fun createShader(vertexShaderCode: String, fragmentShaderCode: String): GLProgram {
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

    private fun checkAndCompile(shaderId: GLShader, code: String) {
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

        when (target) {
            is Framebuffer -> return target.withFramebuffer { block(scope) }
            is WindowRenderTarget -> {
                gl.bindFramebuffer(GL_FRAMEBUFFER, null)
                gl.viewport(0, 0, target.width, target.height)
                block(scope)
            }
            else -> error("I don't know how to render to $target!")
        }
    }
}