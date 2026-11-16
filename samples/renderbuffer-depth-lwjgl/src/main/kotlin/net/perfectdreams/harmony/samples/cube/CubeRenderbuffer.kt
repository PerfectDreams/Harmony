package net.perfectdreams.harmony.samples.cube

import net.perfectdreams.harmony.gl.ByteArrayTextureSource
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLConstants.GL_ARRAY_BUFFER
import net.perfectdreams.harmony.gl.GLConstants.GL_CLAMP_TO_EDGE
import net.perfectdreams.harmony.gl.GLConstants.GL_COLOR_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_NEAREST
import net.perfectdreams.harmony.gl.GLConstants.GL_STATIC_DRAW
import net.perfectdreams.harmony.gl.opengl.OpenGLContext
import net.perfectdreams.harmony.gl.opengl.OpenGLDebugging
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.batcher.SpriteBatcher
import net.perfectdreams.harmony.render.batcher.withSpritePipeline
import net.perfectdreams.harmony.render.framebuffers.FramebufferAttachment
import net.perfectdreams.harmony.render.framebuffers.FramebufferAttachments
import net.perfectdreams.harmony.render.pipelines.DepthState
import net.perfectdreams.harmony.render.pipelines.Pipeline
import net.perfectdreams.harmony.render.rendertarget.ClearOptions
import net.perfectdreams.harmony.render.rendertarget.WindowRenderTarget
import net.perfectdreams.harmony.render.textures.TextureFormat
import net.perfectdreams.harmony.render.vertex.buildVertexAttributes
import org.joml.Matrix4f
import org.lwjgl.opengl.GL
import org.lwjgl.sdl.SDLError.SDL_GetError
import org.lwjgl.sdl.SDLEvents.SDL_EVENT_QUIT
import org.lwjgl.sdl.SDLEvents.SDL_PollEvent
import org.lwjgl.sdl.SDLInit.*
import org.lwjgl.sdl.SDLTimer.SDL_GetTicks
import org.lwjgl.sdl.SDLVideo.*
import org.lwjgl.sdl.SDL_Event
import org.lwjgl.stb.STBImage
import org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import kotlin.math.ceil
import kotlin.math.sin

fun main() {
    check(SDL_Init(SDL_INIT_VIDEO or SDL_INIT_AUDIO)) { "SDL_Init failed: ${SDL_GetError()}" }

    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 3)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE)

    val windowFlags = SDL_WINDOW_OPENGL or SDL_WINDOW_HIGH_PIXEL_DENSITY
    val window = SDL_CreateWindow("Cube Renderbuffer", 1280, 720, windowFlags)
    val scale = SDL_GetWindowDisplayScale(window)
    // Thanks Wayland!
    SDL_SetWindowSize(window, (ceil(1280 / scale)).toInt(), (ceil(720 / scale)).toInt())
    check(window != MemoryUtil.NULL) { "SDL_CreateWindow failed: ${SDL_GetError()}" }

    val context = SDL_GL_CreateContext(window)
    check(context != MemoryUtil.NULL) { "SDL_GL_CreateContext failed: ${SDL_GetError()}" }
    SDL_GL_MakeCurrent(window, context)
    GL.createCapabilities() // required before any GL call
    OpenGLDebugging.enableReportGLErrors()
    SDL_GL_SetSwapInterval(1) // vsync

    val gl = OpenGLContext()
    val gpu = RenderDevice(gl)
    val windowRenderTarget = WindowRenderTarget(1280, 720)

    val vao = gl.genVertexArrays()
    val vbo = gl.genBuffers()

    val vertexShader = """
            #version 330 core
            layout(location = 0) in vec3 aPosition;
            layout(location = 1) in vec3 aNormal;
            layout(location = 2) in vec2 aTexCoord;
            
            uniform mat4 uProjection;
            uniform mat4 uView;
            uniform mat4 uModel;
            
            out vec2 vTexCoord;
            out vec3 vNormal;
            out vec3 vWorldPosition;
            
            void main() {
                vec4 worldPosition = uModel * vec4(aPosition, 1.0);
                gl_Position = uProjection * uView * worldPosition;
                vTexCoord = aTexCoord;
                vNormal = (uModel * vec4(aNormal, 1.0)).xyz;
                vWorldPosition = worldPosition.xyz;
            }
        """.trimIndent()

    val fragmentShader = """
            #version 330 core
            uniform sampler2D uTexture;
            uniform vec3 uCameraPosition;

            in vec2 vTexCoord;
            in vec3 vNormal;
            in vec3 vWorldPosition;
            out vec4 FragColor;
            
            void main() {
                vec3 normal = normalize(vNormal);
                vec3 toCamera = normalize(uCameraPosition - vWorldPosition);
                float facing = dot(normal, toCamera);   // 1 = facing camera, 0 = edge-on, <0 = facing away
                        
                vec4 texColor = texture(uTexture, vTexCoord);
                FragColor = vec4(texColor.r * (facing * 0.75), texColor.g * (facing * 0.75), texColor.b * facing, texColor.a);
            }
        """.trimIndent()

    gl.bindVertexArray(vao)
    gl.bindBuffer(GL_ARRAY_BUFFER, vbo)
    val vertices = floatArrayOf(
        //   x      y      z      nx     ny     nz     u     v
        // Front (z = 0.5)
        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f,
        0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 0.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f,

        -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f,
        -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 1.0f,

        // Back (z = -0.5)
        0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
        -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
        -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,

        0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
        -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,

        // Right (x = 0.5)
        0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
        0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
        0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,

        0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
        0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
        0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,

        // Left (x = -0.5)
        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
        -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,

        -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
        -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
        -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,

        // Top (y = 0.5)
        -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
        0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,

        -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
        0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
        -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,

        // Bottom (y = -0.5)
        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
        0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
        0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,

        -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
        0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
        -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
    )
    gl.bufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    val attributes = buildVertexAttributes {
        this.vec3(0) // aPosition
        this.vec3(1) // aNormal
        this.vec2(2) // aUV
    }

    attributes.apply(gl)

    val shader = gpu.createShader(vertexShader, fragmentShader, ::BasicShader)
    val uiShader = gpu.createShader(
        """
            #version 330 core
            layout(location = 0) in vec2 aPosition;
            layout(location = 1) in vec2 aTexCoord;
            layout(location = 2) in vec4 aBlendColor;
            
            uniform mat4 uProjection;
            
            out vec2 vTexCoord;
            out vec4 vBlendColor;
            
            void main() {
                gl_Position = uProjection * vec4(aPosition, 0.0, 1.0);
                vTexCoord = aTexCoord;
                vBlendColor = aBlendColor;
            }
        """.trimIndent(),
        """
            #version 330 core
            uniform sampler2D uTexture;
            
            in vec2 vTexCoord;
            in vec4 vBlendColor;
            
            out vec4 fragColor;
            
            void main() {
                fragColor = texture(uTexture, vTexCoord) * vBlendColor;
            }
        """.trimIndent(),
        ::UIShader
    )

    val projection = Matrix4f().perspective(45f, 16f / 9f, 0.1f, 100f)

    stbi_set_flip_vertically_on_load(true)

    val data1 = object {}.javaClass.enclosingClass.getResourceAsStream("/loritta_lose.png")?.readAllBytes() ?: throw RuntimeException("Failed to load cube texture")
    val imageBuffer = MemoryUtil.memAlloc(data1.size)
    imageBuffer.put(data1)
    imageBuffer.flip()

    val textureSource = try {
        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            // Decode the image
            val decodedImage = STBImage.stbi_load_from_memory(
                imageBuffer,
                w,
                h,
                channels,
                4 // Force 4 channels (RGBA)
            ) ?: throw RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason())

            val pixels = ByteArray(decodedImage.remaining())
            // Absolute get, because stbi_image_free frees memAddress(buffer), which is offset by the buffer position
            decodedImage.get(0, pixels)
            STBImage.stbi_image_free(decodedImage)

            ByteArrayTextureSource(w.get(), h.get(), pixels)
        }
    } finally {
        MemoryUtil.memFree(imageBuffer)
    }

    val framebuffer = gpu.createFramebuffer(
        FramebufferAttachments(
            mapOf(0 to FramebufferAttachment.RenderbufferAttachment(gpu.createRenderbuffer(160, 90, TextureFormat.RGBA8, 4))),
            depthStencilAttachment = FramebufferAttachment.RenderbufferAttachment(gpu.createRenderbuffer(160, 90, TextureFormat.DEPTH24, 4))
        )
    )
    val targetFramebuffer = gpu.createFramebuffer(
        160,
        90,
        null,
        GLConstants.GL_CLAMP_TO_EDGE,
        GLConstants.GL_CLAMP_TO_EDGE,
        GLConstants.GL_NEAREST,
        GLConstants.GL_NEAREST
    )
    val framebufferBlitTarget = gpu.createFramebuffer(
        FramebufferAttachments(
            mapOf(),
            depthStencilAttachment = FramebufferAttachment.TextureAttachment(gpu.createTexture(160, 90, TextureFormat.DEPTH24, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST))
        )
    )
    val texture = gpu.createTexture(
        textureSource,
        GLConstants.GL_CLAMP_TO_EDGE,
        GLConstants.GL_CLAMP_TO_EDGE,
        GLConstants.GL_LINEAR,
        GLConstants.GL_LINEAR
    )

    gl.enable(GLConstants.GL_DEPTH_TEST)

    val batcher = SpriteBatcher(gl)
    val uiProjection = Matrix4f().ortho(0f, 1280f, 720f, 0f, -1f, 1f)

    val cameraX = 0f
    val cameraY = 0f
    val cameraZ = 2f

    MemoryStack.stackPush().use { stack ->
        val event = SDL_Event.malloc(stack)
        var running = true

        while (running) {
            val view = Matrix4f()
                .translate(-cameraX, -cameraY, -cameraZ)

            while (SDL_PollEvent(event)) {
                if (event.type() == SDL_EVENT_QUIT)
                    running = false
            }

            gl.enable(GLConstants.GL_DEPTH_TEST)

            gpu.onRenderTarget(framebuffer, clear = ClearOptions(color = ClearOptions.Color(0.0f, 0.0f, 1.0f, 1.0f), depth = 1f)) {
                withPipeline(Pipeline(shader, depth = DepthState(write = true))) {
                    val model = Matrix4f()
                        .translate(0f, sin(SDL_GetTicks() / 1000f) * 0.25f, 0f)
                        .rotateY(SDL_GetTicks() / 1000f)
                    this.shader.uProjection.set(projection.get(FloatArray(16)))
                    this.shader.uView.set(view.get(FloatArray(16)))
                    this.shader.uModel.set(model.get(FloatArray(16)))
                    this.shader.uTexture.set(0, texture)
                    this.shader.uCameraPosition.set(cameraX, cameraY, cameraZ)
                    gl.bindVertexArray(vao)
                    gl.drawArrays(GLConstants.GL_TRIANGLES, 0, 6 * 6)
                }
            }

            framebuffer.blitTo(
                framebufferBlitTarget,
                GL_NEAREST,
                copyColor = false,
                copyDepth = true,
                copyStencil = false
            )

            framebuffer.blitRegionTo(
                targetFramebuffer,
                GLConstants.GL_NEAREST,
                0,
                0,
                160,
                90,
                0,
                0,
                160,
                90,
                copyColor = true,
                copyDepth = false,
                copyStencil = false
            )

            gpu.onRenderTarget(windowRenderTarget, clear = ClearOptions(color = ClearOptions.Color(0.0f, 0.0f, 1.0f, 1.0f), depth = 1f)) {
                withSpritePipeline(batcher, uiShader) {
                    this.shader.uProjection.set(uiProjection.get(FloatArray(16)))

                    it.drawBatch(this) {
                        drawQuad(
                            0f,
                            0f,
                            1280f,
                            720f,
                            0f,
                            1f,
                            1f,
                            0f,
                            targetFramebuffer.attachments.color0Texture
                        )

                        drawQuad(
                            0f,
                            0f,
                            320f,
                            180f,
                            0f,
                            1f,
                            1f,
                            0f,
                            (framebufferBlitTarget.attachments.depthStencilAttachment as FramebufferAttachment.TextureAttachment).texture
                        )
                    }
                }
            }

            SDL_GL_SwapWindow(window)
        }
    }
}