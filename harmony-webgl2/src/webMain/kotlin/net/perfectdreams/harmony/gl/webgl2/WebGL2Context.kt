package net.perfectdreams.harmony.gl.webgl2

import js.buffer.ArrayBufferLike
import js.reflect.unsafeCast
import js.typedarrays.Uint8Array
import net.perfectdreams.harmony.gl.ByteArrayTextureSource
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.TextureSource
import net.perfectdreams.harmony.wasm.toArrayBufferFast
import net.perfectdreams.harmony.wasm.toByteArrayFast
import net.perfectdreams.harmony.wasm.toFloat32ArrayFast
import net.perfectdreams.harmony.wasm.toInt32ArrayFast
import web.gl.*

// Very sketchy!
fun Int.asGLenum() = unsafeCast<GLenum>(this)
fun Int.asGLbitfield() = unsafeCast<GLbitfield>(this)
fun Int.asGLuint() = unsafeCast<GLuint>(this)

// WebGL2 returns typed objects, but our interface expects raw values
// Because I'm too lazy to create a proper typed interface, we'll for now just convert objects into ints
private class ObjectStorage<T> {
    private val object2id = mutableMapOf<T, Int>()
    private val id2object = mutableMapOf<Int, T>()
    // We start at 1 because OpenGL uses 0 for "default"
    var currentId = 1

    fun allocate(value: T): Int {
        if (this.object2id.containsKey(value)) {
            return this.object2id[value]!!
        }

        val newId = this.currentId++
        this.object2id[value] = newId
        this.id2object[newId] = value
        return newId
    }

    fun getObjectById(id: Int) = this.id2object[id]

    fun deleteById(id: Int) {
        this.object2id.remove(this.id2object[id])
        this.id2object.remove(id)
    }
}

class WebGL2Context(val context: WebGL2RenderingContext) : GLContext() {
    private val textures = ObjectStorage<WebGLTexture>()
    private val vertexArrayObjects = ObjectStorage<WebGLVertexArrayObject>()
    private val buffers = ObjectStorage<WebGLBuffer>()
    private val shaders = ObjectStorage<WebGLShader>()
    private val programs = ObjectStorage<WebGLProgram>()
    private val framebuffers = ObjectStorage<WebGLFramebuffer>()
    private val uniformLocations = ObjectStorage<WebGLUniformLocation>()

    override fun enable(target: Int) = context.enable(target.asGLenum())
    override fun disable(target: Int) = context.disable(target.asGLenum())
    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) = context.clearColor(red, green, blue, alpha)
    override fun clear(mask: Int) = context.clear(mask.asGLbitfield())
    override fun viewport(x: Int, y: Int, width: Int, height: Int) = context.viewport(x, y, width, height)
    override fun scissor(x: Int, y: Int, width: Int, height: Int) = context.scissor(x, y, width, height)
    override fun bindBuffer(target: Int, buffer: Int) = context.bindBuffer(target.asGLenum(), this.buffers.getObjectById(buffer))
    override fun bufferData(target: Int, size: Long, usage: Int) = context.bufferData(target.asGLenum(), size.toInt(), usage.asGLenum())
    override fun bufferData(target: Int, buffer: FloatArray, usage: Int) = context.bufferData(target.asGLenum(), buffer.toFloat32ArrayFast(), usage.asGLenum())
    override fun bufferData(target: Int, buffer: IntArray, usage: Int) = context.bufferData(target.asGLenum(), buffer.toInt32ArrayFast(), usage.asGLenum())

    override fun bufferSubData(target: Int, offset: Long, buffer: FloatArray) = context.bufferSubData(target.asGLenum(), offset.toInt(), buffer.toFloat32ArrayFast())
    override fun bindVertexArray(array: Int) = context.bindVertexArray(this.vertexArrayObjects.getObjectById(array))
    override fun drawArrays(mode: Int, first: Int, count: Int) = context.drawArrays(mode.asGLenum(), first, count)
    override fun genTextures() = textures.allocate(context.createTexture())
    override fun bindTexture(target: Int, texture: Int) = context.bindTexture(target.asGLenum(), textures.getObjectById(texture))
    override fun activeTexture(texture: Int) = context.activeTexture(texture.asGLenum())
    override fun deleteTextures(values: List<Int>) {
        if (values.isNotEmpty()) {
            for (textureId in values) {
                context.deleteTexture(textures.getObjectById(textureId))
                textures.deleteById(textureId)
            }
        }
    }
    override fun genVertexArrays() = this.vertexArrayObjects.allocate(context.createVertexArray())
    override fun genBuffers() = this.buffers.allocate(context.createBuffer())
    override fun blendFunc(src: Int, dst: Int) = context.blendFunc(src.asGLenum(), dst.asGLenum())
    override fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long) = context.vertexAttribPointer(index.asGLuint(), size, type.asGLenum(), normalized, stride, offset.toInt())
    override fun vertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) = context.vertexAttribIPointer(index.asGLuint(), size, type.asGLenum(), stride, offset)

    override fun enableVertexAttribArray(index: Int) = context.enableVertexAttribArray(index.asGLuint())
    override fun disableVertexAttribArray(index: Int) = context.disableVertexAttribArray(index.asGLuint())
    override fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int): ByteArray {
        val dst = Uint8Array<ArrayBufferLike>(width * height * 4)
        context.readPixels(x, y, width, height, format.asGLenum(), type.asGLenum(), dst)
        return dst.buffer.toByteArrayFast()
    }

    override fun pixelStorei(pname: Int, param: Int) = context.pixelStorei(pname.asGLenum(), param)
    override fun texImage2D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int, border: Int, format: Int, type: Int) = context.texImage2D(target.asGLenum(), level, internalFormat.asGLenum(), width, height, border, format.asGLenum(), type.asGLenum(), null)
    override fun texImage2D(
        target: Int,
        level: Int,
        internalFormat: Int,
        border: Int,
        format: Int,
        type: Int,
        textureSource: TextureSource
    ) {
        when (textureSource) {
            is ByteArrayTextureSource -> {
                val pixels = Uint8Array<ArrayBufferLike>(textureSource.pixels.toArrayBufferFast())
                context.texImage2D(target.asGLenum(), level, internalFormat.asGLenum(), textureSource.width, textureSource.height, border, format.asGLenum(), type.asGLenum(), pixels)
            }
            is BrowserImageTextureSource -> {
                context.texImage2D(target.asGLenum(), level, internalFormat.asGLenum(), textureSource.width, textureSource.height, border, format.asGLenum(), type.asGLenum(), textureSource.image)
            }
            is BrowserImageBitmapTextureSource -> {
                context.texImage2D(target.asGLenum(), level, internalFormat.asGLenum(), textureSource.width, textureSource.height, border, format.asGLenum(), type.asGLenum(), textureSource.image)
            }
            else -> error("Unsupported image type!")
        }
    }

    override fun texSubImage2D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        width: Int,
        height: Int,
        format: Int,
        type: Int,
        textureSource: TextureSource
    ) {
        when (textureSource) {
            is ByteArrayTextureSource -> {
                val pixels = Uint8Array<ArrayBufferLike>(textureSource.pixels.toArrayBufferFast())
                context.texSubImage2D(target.asGLenum(), level, xoffset, yoffset, textureSource.width, textureSource.height, format.asGLenum(), type.asGLenum(), pixels)
            }
            is BrowserImageTextureSource -> {
                context.texSubImage2D(target.asGLenum(), level, xoffset, yoffset, textureSource.width, textureSource.height, format.asGLenum(), type.asGLenum(), textureSource.image)
            }
            is BrowserImageBitmapTextureSource -> {
                context.texSubImage2D(target.asGLenum(), level, xoffset, yoffset, textureSource.width, textureSource.height, format.asGLenum(), type.asGLenum(), textureSource.image)
            }
            else -> error("Unsupported image type!")
        }
    }

    override fun texParameteri(target: Int, pname: Int, param: Int) = context.texParameteri(target.asGLenum(), pname.asGLenum(), param.asGLenum())
    override fun generateMipmap(target: Int) = context.generateMipmap(target.asGLenum())
    override fun depthFunc(func: Int) = context.depthFunc(func.asGLenum())
    override fun depthMask(flag: Boolean) = context.depthMask(flag)

    override fun genFramebuffers() = this.framebuffers.allocate(context.createFramebuffer())
    override fun deleteFramebuffers(values: List<Int>) {
        if (values.isNotEmpty()) {
            for (textureId in values) {
                context.deleteFramebuffer(framebuffers.getObjectById(textureId))
                framebuffers.deleteById(textureId)
            }
        }
    }
    override fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) = context.framebufferTexture2D(target.asGLenum(), attachment.asGLenum(), textarget.asGLenum(), textures.getObjectById(texture), level)
    override fun checkFramebufferStatus(target: Int) = unsafeCast<JsNumber>(context.checkFramebufferStatus(target.asGLenum())).toInt()
    override fun bindFramebuffer(target: Int, framebuffer: Int) = context.bindFramebuffer(target.asGLenum(), this.framebuffers.getObjectById(framebuffer))

    override fun createShader(type: Int) = shaders.allocate(context.createShader(type.asGLenum())!!)
    override fun shaderSource(shader: Int, source: String) = context.shaderSource(this.shaders.getObjectById(shader)!!, source)
    override fun compileShader(shader: Int) = context.compileShader(this.shaders.getObjectById(shader)!!)
    override fun getShaderi(shader: Int, pname: Int): Int {
        val r = context.getShaderParameter(this.shaders.getObjectById(shader)!!, pname.asGLenum()) as JsBoolean
        return if (r.toBoolean())
            GLConstants.GL_TRUE
        else
            GLConstants.GL_FALSE
    }

    override fun getShaderInfoLog(shader: Int, maxLength: Int): String = context.getShaderInfoLog(this.shaders.getObjectById(shader)!!)?.take(maxLength) ?: ""
    override fun deleteShader(shader: Int) {
        context.deleteShader(this.shaders.getObjectById(shader)!!)
        this.shaders.deleteById(shader)
    }
    override fun createProgram() = this.programs.allocate(context.createProgram())
    override fun attachShader(program: Int, shader: Int) = context.attachShader(programs.getObjectById(program)!!, this.shaders.getObjectById(shader)!!)
    override fun detachShader(program: Int, shader: Int) = context.detachShader(programs.getObjectById(program)!!, this.shaders.getObjectById(shader)!!)
    override fun linkProgram(program: Int) = context.linkProgram(programs.getObjectById(program)!!)
    override fun getProgrami(program: Int, pname: Int): Int {
        val r = context.getProgramParameter(programs.getObjectById(program)!!, pname.asGLenum()) as JsBoolean
        return if (r.toBoolean())
            GLConstants.GL_TRUE
        else
            GLConstants.GL_FALSE
    }
    override fun getProgramInfoLog(program: Int, maxLength: Int): String = context.getProgramInfoLog(programs.getObjectById(program)!!)?.take(maxLength) ?: ""
    override fun useProgram(program: Int) {
        this.activeProgram = program
        context.useProgram(programs.getObjectById(program))
    }

    override fun getUniformLocation(program: Int, name: String) = this.uniformLocations.allocate(context.getUniformLocation(programs.getObjectById(program)!!, name)!!)
    override fun programUniform1i(program: Int, location: Int, x: Int) {
        checkActiveProgram(program)
        context.uniform1i(this.uniformLocations.getObjectById(location), x)
    }

    override fun programUniform1f(program: Int, location: Int, x: Float) {
        checkActiveProgram(program)
        context.uniform1f(this.uniformLocations.getObjectById(location), x)
    }

    override fun programUniform2f(program: Int, location: Int, x: Float, y: Float) {
        checkActiveProgram(program)
        context.uniform2f(this.uniformLocations.getObjectById(location), x, y)
    }

    override fun programUniform3f(program: Int, location: Int, x: Float, y: Float, z: Float) {
        checkActiveProgram(program)
        context.uniform3f(this.uniformLocations.getObjectById(location), x, y, z)
    }

    override fun programUniform4f(program: Int, location: Int, x: Float, y: Float, z: Float, w: Float) {
        checkActiveProgram(program)
        context.uniform4f(this.uniformLocations.getObjectById(location), x, y, z, w)
    }

    override fun programUniformMatrix4fv(program: Int, location: Int, transpose: Boolean, value: FloatArray) {
        checkActiveProgram(program)
        context.uniformMatrix4fv(this.uniformLocations.getObjectById(location), transpose, value.toFloat32ArrayFast())
    }
}