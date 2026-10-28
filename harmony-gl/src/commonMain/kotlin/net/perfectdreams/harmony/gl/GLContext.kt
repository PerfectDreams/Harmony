package net.perfectdreams.harmony.gl

/**
 * Abstractions for OpenGL-like renderers
 */
abstract class GLContext {
    var activeProgram = 0

    abstract fun enable(target: Int)
    abstract fun disable(target: Int)
    abstract fun clearColor(red: Float, green: Float, blue: Float, alpha: Float)
    abstract fun clear(mask: Int)
    abstract fun viewport(x: Int, y: Int, width: Int, height: Int)
    abstract fun scissor(x: Int, y: Int, width: Int, height: Int)
    abstract fun bindBuffer(target: Int, buffer: Int)
    abstract fun bufferData(target: Int, size: Long, usage: Int)
    abstract fun bufferData(target: Int, buffer: FloatArray, usage: Int)
    abstract fun bufferData(target: Int, buffer: IntArray, usage: Int)
    abstract fun bufferSubData(target: Int, offset: Long, buffer: FloatArray)
    abstract fun bindVertexArray(array: Int)
    abstract fun drawArrays(mode: Int, first: Int, count: Int)
    abstract fun genTextures(): Int
    abstract fun bindTexture(target: Int, texture: Int)
    abstract fun activeTexture(texture: Int)
    fun deleteTexture(texture: Int) = deleteTextures(listOf(texture))
    abstract fun deleteTextures(values: List<Int>)
    abstract fun genVertexArrays(): Int
    abstract fun genBuffers(): Int
    abstract fun blendFunc(src: Int, dst: Int)
    abstract fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long)
    abstract fun vertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int)
    abstract fun enableVertexAttribArray(index: Int)
    abstract fun disableVertexAttribArray(index: Int)
    abstract fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int): ByteArray
    abstract fun pixelStorei(pname: Int, param: Int)
    abstract fun texImage2D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int, border: Int, format: Int, type: Int)
    abstract fun texImage2D(target: Int, level: Int, internalFormat: Int, border: Int, format: Int, type: Int, textureSource: TextureSource)
    abstract fun texSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, textureSource: TextureSource)
    abstract fun texParameteri(target: Int, pname: Int, param: Int)
    abstract fun generateMipmap(target: Int)
    abstract fun depthFunc(func: Int)
    abstract fun depthMask(flag: Boolean)

    abstract fun genFramebuffers(): Int
    fun deleteFramebuffer(framebuffer: Int) = deleteFramebuffers(listOf(framebuffer))
    abstract fun deleteFramebuffers(values: List<Int>)
    abstract fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int)
    abstract fun checkFramebufferStatus(target: Int): Int
    abstract fun bindFramebuffer(target: Int, framebuffer: Int)

    abstract fun createShader(type: Int): Int
    abstract fun shaderSource(shader: Int, source: String)
    abstract fun compileShader(shader: Int)
    abstract fun getShaderi(shader: Int, pname: Int): Int
    abstract fun getShaderInfoLog(shader: Int, maxLength: Int): String
    abstract fun deleteShader(shader: Int)
    abstract fun createProgram(): Int
    abstract fun attachShader(program: Int, shader: Int)
    abstract fun detachShader(program: Int, shader: Int)
    abstract fun linkProgram(program: Int)
    abstract fun getProgrami(program: Int, pname: Int): Int
    abstract fun getProgramInfoLog(program: Int, maxLength: Int): String
    abstract fun useProgram(program: Int)
    fun checkActiveProgram(program: Int) = check(this.activeProgram == program) { "We want $program to be bound, but the current active program is ${this.activeProgram}!" }

    abstract fun getUniformLocation(program: Int, name: String): Int
    abstract fun programUniform1i(program: Int, location: Int, x: Int)
    abstract fun programUniform1f(program: Int, location: Int, x: Float)
    abstract fun programUniform2f(program: Int, location: Int, x: Float, y: Float)
    abstract fun programUniform3f(program: Int, location: Int, x: Float, y: Float, z: Float)
    abstract fun programUniform4f(program: Int, location: Int, x: Float, y: Float, z: Float, w: Float)
    abstract fun programUniformMatrix4fv(program: Int, location: Int, transpose: Boolean, value: FloatArray)
}