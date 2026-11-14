package net.perfectdreams.harmony.gl.opengl

import net.perfectdreams.harmony.gl.ByteArrayTextureSource
import net.perfectdreams.harmony.gl.GLBuffer
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLFramebuffer
import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.gl.GLShader
import net.perfectdreams.harmony.gl.GLTexture
import net.perfectdreams.harmony.gl.GLUniformLocation
import net.perfectdreams.harmony.gl.GLVertexArrayObject
import net.perfectdreams.harmony.gl.TextureSource
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.opengl.GL11.glClearColor
import org.lwjgl.opengl.GL11.glCullFace
import org.lwjgl.opengl.GL11.glDeleteTextures
import org.lwjgl.opengl.GL11.glDepthFunc
import org.lwjgl.opengl.GL11.glDepthMask
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL11.glDrawElements
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL11.glGenTextures
import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL11.glPixelStorei
import org.lwjgl.opengl.GL11.glPolygonOffset
import org.lwjgl.opengl.GL11.glReadBuffer
import org.lwjgl.opengl.GL11.glReadPixels
import org.lwjgl.opengl.GL11.glScissor
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL11.glTexParameteri
import org.lwjgl.opengl.GL11.glTexSubImage2D
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.glBindBuffer
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glBufferSubData
import org.lwjgl.opengl.GL15.glDeleteBuffers
import org.lwjgl.opengl.GL15.glGenBuffers
import org.lwjgl.opengl.GL20.glAttachShader
import org.lwjgl.opengl.GL20.glCompileShader
import org.lwjgl.opengl.GL20.glCreateProgram
import org.lwjgl.opengl.GL20.glCreateShader
import org.lwjgl.opengl.GL20.glDeleteProgram
import org.lwjgl.opengl.GL20.glDeleteShader
import org.lwjgl.opengl.GL20.glDetachShader
import org.lwjgl.opengl.GL20.glDisableVertexAttribArray
import org.lwjgl.opengl.GL20.glDrawBuffers
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glGetProgramInfoLog
import org.lwjgl.opengl.GL20.glGetProgrami
import org.lwjgl.opengl.GL20.glGetShaderInfoLog
import org.lwjgl.opengl.GL20.glGetShaderi
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glLinkProgram
import org.lwjgl.opengl.GL20.glShaderSource
import org.lwjgl.opengl.GL20.glUniform1f
import org.lwjgl.opengl.GL20.glUniform1i
import org.lwjgl.opengl.GL20.glUniform2f
import org.lwjgl.opengl.GL20.glUniform3f
import org.lwjgl.opengl.GL20.glUniform4f
import org.lwjgl.opengl.GL20.glUniformMatrix4fv
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.glBindFramebuffer
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glCheckFramebufferStatus
import org.lwjgl.opengl.GL30.glDeleteFramebuffers
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL30.glFramebufferTexture2D
import org.lwjgl.opengl.GL30.glGenFramebuffers
import org.lwjgl.opengl.GL30.glGenVertexArrays
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.opengl.GL30.glVertexAttribIPointer
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer

class OpenGLContext : GLContext() {
    private fun GLTexture.unwrap(): Int = (this as OpenGLTexture).id
    private fun GLProgram.unwrap(): Int = (this as OpenGLProgram).id
    private fun GLBuffer.unwrap(): Int = (this as OpenGLBuffer).id
    private fun GLFramebuffer.unwrap(): Int = (this as OpenGLFramebuffer).id
    private fun GLShader.unwrap(): Int = (this as OpenGLShader).id
    private fun GLUniformLocation.unwrap(): Int = (this as OpenGLUniformLocation).id
    private fun GLVertexArrayObject.unwrap(): Int = (this as OpenGLVertexArrayObject).id

    override fun enable(target: Int) = glEnable(target)
    override fun disable(target: Int) = glDisable(target)
    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) = glClearColor(red, green, blue, alpha)
    override fun clear(mask: Int) = glClear(mask)
    override fun viewport(x: Int, y: Int, width: Int, height: Int) = glViewport(x, y, width, height)
    override fun scissor(x: Int, y: Int, width: Int, height: Int) = glScissor(x, y, width, height)
    override fun bindBuffer(target: Int, buffer: GLBuffer) = glBindBuffer(target, buffer.unwrap())
    override fun bufferData(target: Int, size: Long, usage: Int) = glBufferData(target, size, usage)
    override fun bufferData(target: Int, buffer: FloatArray, usage: Int) = glBufferData(target, buffer, usage)
    override fun bufferData(target: Int, buffer: IntArray, usage: Int) = glBufferData(target, buffer, usage)
    override fun bufferData(target: Int, buffer: ByteArray, usage: Int) {
        // LWJGL doesn't have a ByteArray overload
        val byteBuffer = ByteBuffer.allocateDirect(buffer.size)
        byteBuffer.put(buffer)
        byteBuffer.position(0)
        glBufferData(target, byteBuffer, usage)
    }
    override fun bufferSubData(target: Int, offset: Long, buffer: FloatArray) = glBufferSubData(target, offset, buffer)
    override fun deleteBuffers(values: List<GLBuffer>) = stackAllocateIntsAndCall(values.map { it.unwrap() }) { glDeleteBuffers(it) }
    override fun bindVertexArray(array: GLVertexArrayObject?) = glBindVertexArray(array?.unwrap() ?: 0)
    override fun deleteVertexArrays(values: List<GLVertexArrayObject>) = stackAllocateIntsAndCall(values.map { it.unwrap() }) { glDeleteVertexArrays(it) }
    override fun drawArrays(mode: Int, first: Int, count: Int) = glDrawArrays(mode, first, count)
    override fun drawElements(mode: Int, count: Int, type: Int, offset: Long) = glDrawElements(mode, count, type, offset)
    override fun genTextures() = OpenGLTexture(glGenTextures())
    override fun bindTexture(target: Int, texture: GLTexture?) = glBindTexture(target, texture?.unwrap() ?: 0)
    override fun activeTexture(texture: Int) = glActiveTexture(texture)
    override fun deleteTextures(values: List<GLTexture>) = stackAllocateIntsAndCall(values.map { it.unwrap() }) { glDeleteTextures(it) }
    override fun genVertexArrays() = OpenGLVertexArrayObject(glGenVertexArrays())
    override fun genBuffers() = OpenGLBuffer(glGenBuffers())
    override fun blendFunc(src: Int, dst: Int) = glBlendFunc(src, dst)
    override fun cullFace(mode: Int) = glCullFace(mode)
    override fun polygonOffset(factor: Float, units: Float) = glPolygonOffset(factor, units)
    override fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long) = glVertexAttribPointer(index, size, type, normalized, stride, offset)
    override fun vertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) = glVertexAttribIPointer(index, size, type, stride, offset.toLong())
    override fun enableVertexAttribArray(index: Int) = glEnableVertexAttribArray(index)
    override fun disableVertexAttribArray(index: Int) = glDisableVertexAttribArray(index)
    override fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int): ByteArray {
        val byteBuffer = ByteBuffer.allocateDirect(width * height * 4)
        glReadPixels(x, y, width, height, format, type, byteBuffer)

        // We can't use ".array()" directly because it is a ooo allocateDirect
        val byteArray = ByteArray(width * height * 4)
        byteBuffer.get(byteArray)

        return byteArray
    }

    override fun pixelStorei(pname: Int, param: Int) = glPixelStorei(pname, param)
    override fun texImage2D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int, border: Int, format: Int, type: Int) = glTexImage2D(target, level, internalFormat, width, height, border, format, type, null as ByteBuffer?)
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
                val byteBuffer = ByteBuffer.allocateDirect(textureSource.pixels.size)
                byteBuffer.put(textureSource.pixels)
                byteBuffer.position(0)
                glTexImage2D(target, level, internalFormat, textureSource.width, textureSource.height, border, format, type, byteBuffer)
            }
            else -> error("Unsupported image type!")
        }
    }

    override fun texImage3D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int, type: Int) = glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, null as ByteBuffer?)

    override fun texImage3D(
        target: Int,
        level: Int,
        internalFormat: Int,
        depth: Int,
        border: Int,
        format: Int,
        type: Int,
        textureSource: TextureSource
    ) {
        require(depth > 0 && textureSource.height % depth == 0) { "The texture source height (${textureSource.height}) must be a multiple of the depth ($depth)!" }
        val layerHeight = textureSource.height / depth

        when (textureSource) {
            is ByteArrayTextureSource -> {
                val byteBuffer = ByteBuffer.allocateDirect(textureSource.pixels.size)
                byteBuffer.put(textureSource.pixels)
                byteBuffer.position(0)
                glTexImage3D(target, level, internalFormat, textureSource.width, layerHeight, depth, border, format, type, byteBuffer)
            }
            else -> error("Unsupported image type!")
        }
    }

    override fun texSubImage3D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        zoffset: Int,
        width: Int,
        height: Int,
        depth: Int,
        format: Int,
        type: Int,
        textureSource: TextureSource
    ) {
        when (textureSource) {
            is ByteArrayTextureSource -> {
                val byteBuffer = ByteBuffer.allocateDirect(textureSource.pixels.size)
                byteBuffer.put(textureSource.pixels)
                byteBuffer.position(0)
                glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, byteBuffer)
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
                val byteBuffer = ByteBuffer.allocateDirect(textureSource.pixels.size)
                byteBuffer.put(textureSource.pixels)
                byteBuffer.position(0)
                glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, byteBuffer)
            }
            else -> error("Unsupported image type!")
        }
    }

    override fun texParameteri(target: Int, pname: Int, param: Int) = glTexParameteri(target, pname, param)
    override fun generateMipmap(target: Int) = glGenerateMipmap(target)
    override fun depthFunc(func: Int) = glDepthFunc(func)
    override fun depthMask(flag: Boolean) = glDepthMask(flag)

    override fun genFramebuffers() = OpenGLFramebuffer(glGenFramebuffers())
    override fun deleteFramebuffers(values: List<GLFramebuffer>) = stackAllocateIntsAndCall(values.map { it.unwrap() }) { glDeleteFramebuffers(it) }
    override fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: GLTexture, level: Int) = glFramebufferTexture2D(target, attachment, textarget, texture.unwrap(), level)
    override fun checkFramebufferStatus(target: Int) = glCheckFramebufferStatus(target)
    override fun bindFramebuffer(target: Int, framebuffer: GLFramebuffer?) = glBindFramebuffer(target, framebuffer?.unwrap() ?: 0)
    override fun drawBuffers(buffers: IntArray) = glDrawBuffers(buffers)
    override fun readBuffer(src: Int) = glReadBuffer(src)

    override fun getInteger(pname: Int) = glGetInteger(pname)

    override fun createShader(type: Int) = OpenGLShader(glCreateShader(type))
    override fun shaderSource(shader: GLShader, source: String) = glShaderSource(shader.unwrap(), source)
    override fun compileShader(shader: GLShader) = glCompileShader(shader.unwrap())
    override fun getShaderi(shader: GLShader, pname: Int) = glGetShaderi(shader.unwrap(), pname)
    override fun getShaderInfoLog(shader: GLShader, maxLength: Int): String = glGetShaderInfoLog(shader.unwrap(), maxLength)
    override fun deleteShader(shader: GLShader) = glDeleteShader(shader.unwrap())
    override fun createProgram() = OpenGLProgram(glCreateProgram())
    override fun attachShader(program: GLProgram, shader: GLShader) = glAttachShader(program.unwrap(), shader.unwrap())
    override fun detachShader(program: GLProgram, shader: GLShader) = glDetachShader(program.unwrap(), shader.unwrap())
    override fun linkProgram(program: GLProgram) = glLinkProgram(program.unwrap())
    override fun getProgrami(program: GLProgram, pname: Int) = glGetProgrami(program.unwrap(), pname)
    override fun getProgramInfoLog(program: GLProgram, maxLength: Int): String = glGetProgramInfoLog(program.unwrap(), maxLength)
    override fun useProgram(program: GLProgram?) = glUseProgram(program?.unwrap() ?: 0)

    override fun deleteProgram(program: GLProgram) = glDeleteProgram(program.unwrap())

    override fun getUniformLocation(program: GLProgram, name: String): GLUniformLocation? {
        val location = glGetUniformLocation(program.unwrap(), name)
        if (location == -1)
            return null
        return OpenGLUniformLocation(location)
    }

    override fun uniform1i(location: GLUniformLocation, x: Int) = glUniform1i(location.unwrap(), x)
    override fun uniform1f(location: GLUniformLocation, x: Float) = glUniform1f(location.unwrap(), x)
    override fun uniform2f(location: GLUniformLocation, x: Float, y: Float) = glUniform2f(location.unwrap(), x, y)
    override fun uniform3f(location: GLUniformLocation, x: Float, y: Float, z: Float) = glUniform3f(location.unwrap(), x, y, z)
    override fun uniform4f(location: GLUniformLocation, x: Float, y: Float, z: Float, w: Float) = glUniform4f(location.unwrap(), x, y, z, w)
    override fun uniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) = glUniformMatrix4fv(location.unwrap(), transpose, value)

    private fun stackAllocateIntsAndCall(values: List<Int>, block: (IntBuffer) -> (Unit)) {
        if (values.isNotEmpty()) {
            MemoryStack.stackPush().use {
                val elements = it.ints(*values.toIntArray())
                block(elements)
            }
        }
    }
}