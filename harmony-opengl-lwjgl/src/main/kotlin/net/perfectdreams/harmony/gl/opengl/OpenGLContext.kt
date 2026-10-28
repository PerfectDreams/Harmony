package net.perfectdreams.harmony.gl.opengl

import net.perfectdreams.harmony.gl.ByteArrayTextureSource
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.TextureSource
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.opengl.GL11.glClearColor
import org.lwjgl.opengl.GL11.glDeleteTextures
import org.lwjgl.opengl.GL11.glDepthFunc
import org.lwjgl.opengl.GL11.glDepthMask
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL11.glGenTextures
import org.lwjgl.opengl.GL11.glPixelStorei
import org.lwjgl.opengl.GL11.glReadPixels
import org.lwjgl.opengl.GL11.glScissor
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL11.glTexParameteri
import org.lwjgl.opengl.GL11.glTexSubImage2D
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.glBindBuffer
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glBufferSubData
import org.lwjgl.opengl.GL15.glGenBuffers
import org.lwjgl.opengl.GL20.glAttachShader
import org.lwjgl.opengl.GL20.glCompileShader
import org.lwjgl.opengl.GL20.glCreateProgram
import org.lwjgl.opengl.GL20.glCreateShader
import org.lwjgl.opengl.GL20.glDeleteShader
import org.lwjgl.opengl.GL20.glDetachShader
import org.lwjgl.opengl.GL20.glDisableVertexAttribArray
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
import org.lwjgl.opengl.GL30.glFramebufferTexture2D
import org.lwjgl.opengl.GL30.glGenFramebuffers
import org.lwjgl.opengl.GL30.glGenVertexArrays
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.opengl.GL30.glVertexAttribIPointer
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer

class OpenGLContext : GLContext() {
    override fun enable(target: Int) = glEnable(target)
    override fun disable(target: Int) = glDisable(target)
    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) = glClearColor(red, green, blue, alpha)
    override fun clear(mask: Int) = glClear(mask)
    override fun viewport(x: Int, y: Int, width: Int, height: Int) = glViewport(x, y, width, height)
    override fun scissor(x: Int, y: Int, width: Int, height: Int) = glScissor(x, y, width, height)
    override fun bindBuffer(target: Int, buffer: Int) = glBindBuffer(target, buffer)
    override fun bufferData(target: Int, size: Long, usage: Int) = glBufferData(target, size, usage)
    override fun bufferData(target: Int, buffer: FloatArray, usage: Int) = glBufferData(target, buffer, usage)
    override fun bufferData(target: Int, buffer: IntArray, usage: Int) = glBufferData(target, buffer, usage)
    override fun bufferSubData(target: Int, offset: Long, buffer: FloatArray) = glBufferSubData(target, offset, buffer)
    override fun bindVertexArray(array: Int) = glBindVertexArray(array)
    override fun drawArrays(mode: Int, first: Int, count: Int) = glDrawArrays(mode, first, count)
    override fun genTextures() = glGenTextures()
    override fun bindTexture(target: Int, texture: Int) = glBindTexture(target, texture)
    override fun activeTexture(texture: Int) = glActiveTexture(texture)
    override fun deleteTextures(values: List<Int>) = stackAllocateIntsAndCall(values) { glDeleteTextures(it) }
    override fun genVertexArrays() = glGenVertexArrays()
    override fun genBuffers() = glGenBuffers()
    override fun blendFunc(src: Int, dst: Int) = glBlendFunc(src, dst)
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

    override fun genFramebuffers() = glGenFramebuffers()
    override fun deleteFramebuffers(values: List<Int>) = stackAllocateIntsAndCall(values) { glDeleteFramebuffers(it) }
    override fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) = glFramebufferTexture2D(target, attachment, textarget, texture, level)
    override fun checkFramebufferStatus(target: Int) = glCheckFramebufferStatus(target)
    override fun bindFramebuffer(target: Int, framebuffer: Int) = glBindFramebuffer(target, framebuffer)

    override fun createShader(type: Int) = glCreateShader(type)
    override fun shaderSource(shader: Int, source: String) = glShaderSource(shader, source)
    override fun compileShader(shader: Int) = glCompileShader(shader)
    override fun getShaderi(shader: Int, pname: Int) = glGetShaderi(shader, pname)
    override fun getShaderInfoLog(shader: Int, maxLength: Int): String = glGetShaderInfoLog(shader, maxLength)
    override fun deleteShader(shader: Int) = glDeleteShader(shader)
    override fun createProgram() = glCreateProgram()
    override fun attachShader(program: Int, shader: Int) = glAttachShader(program, shader)
    override fun detachShader(program: Int, shader: Int) = glDetachShader(program, shader)
    override fun linkProgram(program: Int) = glLinkProgram(program)
    override fun getProgrami(program: Int, pname: Int) = glGetProgrami(program, pname)
    override fun getProgramInfoLog(program: Int, maxLength: Int): String = glGetProgramInfoLog(program, maxLength)
    override fun useProgram(program: Int) {
        this.activeProgram = program
        glUseProgram(program)
    }

    override fun getUniformLocation(program: Int, name: String) = glGetUniformLocation(program, name)
    override fun programUniform1i(program: Int, location: Int, x: Int) {
        checkActiveProgram(program)
        glUniform1i(location, x)
    }

    override fun programUniform1f(program: Int, location: Int, x: Float) {
        checkActiveProgram(program)
        glUniform1f( location, x)
    }

    override fun programUniform2f(program: Int, location: Int, x: Float, y: Float) {
        checkActiveProgram(program)
        glUniform2f( location, x, y)
    }

    override fun programUniform3f(program: Int, location: Int, x: Float, y: Float, z: Float) {
        checkActiveProgram(program)
        glUniform3f(location, x, y, z)
    }

    override fun programUniform4f(program: Int, location: Int, x: Float, y: Float, z: Float, w: Float) {
        checkActiveProgram(program)
        glUniform4f(location, x, y, z, w)
    }

    override fun programUniformMatrix4fv(program: Int, location: Int, transpose: Boolean, value: FloatArray) {
        checkActiveProgram(program)
        glUniformMatrix4fv(location, transpose, value)
    }

    private fun stackAllocateIntsAndCall(values: List<Int>, block: (IntBuffer) -> (Unit)) {
        if (values.isNotEmpty()) {
            MemoryStack.stackPush().use {
                val elements = it.ints(*values.toIntArray())
                block(elements)
            }
        }
    }
}