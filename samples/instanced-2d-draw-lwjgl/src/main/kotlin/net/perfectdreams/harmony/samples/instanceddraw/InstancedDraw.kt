package net.perfectdreams.harmony.samples.instanceddraw

import net.perfectdreams.harmony.gl.ByteArrayTextureSource
import net.perfectdreams.harmony.gl.GLConstants.GL_ARRAY_BUFFER
import net.perfectdreams.harmony.gl.GLConstants.GL_CLAMP_TO_EDGE
import net.perfectdreams.harmony.gl.GLConstants.GL_COLOR_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_DYNAMIC_DRAW
import net.perfectdreams.harmony.gl.GLConstants.GL_ELEMENT_ARRAY_BUFFER
import net.perfectdreams.harmony.gl.GLConstants.GL_LINEAR
import net.perfectdreams.harmony.gl.GLConstants.GL_STATIC_DRAW
import net.perfectdreams.harmony.gl.GLConstants.GL_TRIANGLES
import net.perfectdreams.harmony.gl.GLConstants.GL_UNSIGNED_INT
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.opengl.OpenGLContext
import net.perfectdreams.harmony.gl.opengl.OpenGLDebugging
import net.perfectdreams.harmony.gl.pipelines.Pipeline
import net.perfectdreams.harmony.gl.vertex.buildVertexAttributes
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
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.random.Random

fun main() {
    check(SDL_Init(SDL_INIT_VIDEO or SDL_INIT_AUDIO)) { "SDL_Init failed: ${SDL_GetError()}" }

    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 3)
    SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE)

    val windowFlags = SDL_WINDOW_OPENGL or SDL_WINDOW_HIGH_PIXEL_DENSITY
    val window = SDL_CreateWindow("Instanced Draw", 1280, 720, windowFlags)
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
    val gpu = GPUResources(gl, 1280, 720)

    val vao = gl.genVertexArrays()
    val vbo = gl.genBuffers()

    gl.bindVertexArray(vao)
    gl.bindBuffer(GL_ARRAY_BUFFER, vbo)

    val indices = intArrayOf(
        0, 1, 2, // first triangle
        0, 2, 3 // second triangle
    )

    val vertices = floatArrayOf(
        // position, uv
        0.0f, 0.0f, 0.0f, 0.0f,
        128.0f, 0.0f, 1.0f, 0.0f,
        128.0f, 128.0f, 1.0f, 1.0f,
        0.0f, 128.0f, 0.0f, 1.0f
    )

    gl.bufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    buildVertexAttributes {
        vec2(0) // aPosition
        vec2(1) // aTexCoords
    }.apply(gl)

    val vbo2 = gl.genBuffers()
    gl.bindBuffer(GL_ARRAY_BUFFER, vbo2)

    // 2 (position) + 4 (color) * 16
    val positions = FloatArray((2 + 4) * 16)
    gl.bufferData(GL_ARRAY_BUFFER, positions, GL_DYNAMIC_DRAW)

    buildVertexAttributes {
        vec2(2, divisor = 1)
        vec4(3, divisor = 1)
    }.apply(gl)

    val ebo = gl.genBuffers()
    gl.bindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    gl.bufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

    val uiShader = gpu.createShader(
        """
            #version 330 core
            layout(location = 0) in vec2 aPosition;
            layout(location = 1) in vec2 aTexCoords;
            layout(location = 2) in vec2 aPositionRel; // This is the "world" position
            layout(location = 3) in vec4 aBlendColor;
            
            uniform mat4 uProjection;
            
            out vec2 vTexCoord;
            out vec4 vBlendColor;
            
            void main() {
                vec2 transformedPosition = aPosition + aPositionRel;
                // gl_Position = uProjection * uModel * vec4(aPosition + vec2(0.0, gl_InstanceID), 0.0, 1.0);
                gl_Position = uProjection * vec4(transformedPosition, 0.0, 1.0);
                vTexCoord = aTexCoords;
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

    val texture = gpu.createTexture(textureSource, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_LINEAR, GL_LINEAR)

    val uiProjection = Matrix4f().ortho(0f, 1280f, 720f, 0f, -1f, 1f)

    MemoryStack.stackPush().use { stack ->
        val event = SDL_Event.malloc(stack)
        var running = true

        while (running) {
            while (SDL_PollEvent(event)) {
                if (event.type() == SDL_EVENT_QUIT)
                    running = false
            }

            gpu.onRenderTarget(gpu.windowRenderTarget) {
                gl.clearColor(0.0f, 0.0f, 1.0f, 1.0f)
                gl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                withPipeline(Pipeline(uiShader)) {
                    gl.bindBuffer(GL_ARRAY_BUFFER, vbo2)
                    var idx = 0
                    val random = Random(0)
                    repeat(16) {
                        positions[idx++] = it.toFloat() * 128f
                        positions[idx++] = (sin((SDL_GetTicks().toFloat() + (it * 1_000)) * 0.001f) * 128f) + 256f
                        positions[idx++] = random.nextFloat()
                        positions[idx++] = random.nextFloat()
                        positions[idx++] = random.nextFloat()
                        positions[idx++] = 1f
                    }
                    gl.bufferSubData(GL_ARRAY_BUFFER, 0, positions)

                    this.shader.uProjection.set(uiProjection.get(FloatArray(16)))
                    this.shader.uTexture.set(0, texture)

                    gl.bindVertexArray(vao)
                    // gl.drawArraysInstanced(GL_TRIANGLES, 0, 6, 16)
                    gl.drawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0L, 16)
                }
            }

            SDL_GL_SwapWindow(window)
        }
    }
}