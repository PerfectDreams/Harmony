package net.perfectdreams.harmony.gl

/**
 * Abstractions for OpenGL-like renderers
 */
abstract class GLContext {
    abstract fun enable(target: Int)
    abstract fun disable(target: Int)
    abstract fun clearColor(red: Float, green: Float, blue: Float, alpha: Float)
    abstract fun clear(mask: Int)
    abstract fun viewport(x: Int, y: Int, width: Int, height: Int)
    abstract fun scissor(x: Int, y: Int, width: Int, height: Int)
    abstract fun bindBuffer(target: Int, buffer: GLBuffer?)
    abstract fun bufferData(target: Int, size: Long, usage: Int)
    abstract fun bufferData(target: Int, buffer: FloatArray, usage: Int)
    abstract fun bufferData(target: Int, buffer: IntArray, usage: Int)
    abstract fun bufferData(target: Int, buffer: ByteArray, usage: Int)
    abstract fun bufferSubData(target: Int, offset: Long, buffer: FloatArray)
    fun deleteBuffer(buffer: GLBuffer) = deleteBuffers(listOf(buffer))
    abstract fun deleteBuffers(values: List<GLBuffer>)
    abstract fun bindVertexArray(array: GLVertexArrayObject?)
    fun deleteVertexArray(array: GLVertexArrayObject) = deleteVertexArrays(listOf(array))
    abstract fun deleteVertexArrays(values: List<GLVertexArrayObject>)
    abstract fun drawArrays(mode: Int, first: Int, count: Int)
    abstract fun drawArraysInstanced(mode: Int, first: Int, count: Int, primcount: Int)
    abstract fun drawElements(mode: Int, count: Int, type: Int, offset: Long)
    abstract fun drawElementsInstanced(mode: Int, count: Int, type: Int, indices: Long, primcount: Int)
    abstract fun genTextures(): GLTexture
    abstract fun bindTexture(target: Int, texture: GLTexture?)
    abstract fun activeTexture(texture: Int)
    fun deleteTexture(texture: GLTexture) = deleteTextures(listOf(texture))
    abstract fun deleteTextures(values: List<GLTexture>)
    abstract fun genVertexArrays(): GLVertexArrayObject
    abstract fun genBuffers(): GLBuffer
    abstract fun blendFunc(src: Int, dst: Int)
    abstract fun cullFace(mode: Int)
    abstract fun polygonOffset(factor: Float, units: Float)
    abstract fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long)
    abstract fun vertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int)
    abstract fun enableVertexAttribArray(index: Int)
    abstract fun disableVertexAttribArray(index: Int)
    abstract fun vertexAttribDivisor(index: Int, divisor: Int)
    abstract fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int): ByteArray
    abstract fun pixelStorei(pname: Int, param: Int)
    abstract fun texImage2D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int, border: Int, format: Int, type: Int)
    abstract fun texImage2D(target: Int, level: Int, internalFormat: Int, border: Int, format: Int, type: Int, textureSource: TextureSource)
    abstract fun texImage3D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int, type: Int)
    abstract fun texImage3D(target: Int, level: Int, internalFormat: Int, depth: Int, border: Int, format: Int, type: Int, textureSource: TextureSource)
    abstract fun texSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, textureSource: TextureSource)
    abstract fun texSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int, width: Int, height: Int, depth: Int, format: Int, type: Int, textureSource: TextureSource)
    abstract fun texParameteri(target: Int, pname: Int, param: Int)
    abstract fun generateMipmap(target: Int)
    abstract fun depthFunc(func: Int)
    abstract fun depthMask(flag: Boolean)

    abstract fun genFramebuffers(): GLFramebuffer
    fun deleteFramebuffer(framebuffer: GLFramebuffer) = deleteFramebuffers(listOf(framebuffer))
    abstract fun deleteFramebuffers(values: List<GLFramebuffer>)
    abstract fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: GLTexture?, level: Int)
    abstract fun framebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: GLRenderbuffer?)
    abstract fun checkFramebufferStatus(target: Int): Int
    abstract fun bindFramebuffer(target: Int, framebuffer: GLFramebuffer?)
    abstract fun genRenderbuffers(): GLRenderbuffer
    abstract fun bindRenderbuffer(target: Int, renderbuffer: GLRenderbuffer?)
    abstract fun renderbufferStorage(target: Int, internalFormat: Int, width: Int, height: Int)
    fun deleteRenderbuffer(renderbuffer: GLRenderbuffer) = deleteRenderbuffers(listOf(renderbuffer))
    abstract fun deleteRenderbuffers(values: List<GLRenderbuffer>)
    abstract fun drawBuffers(buffers: IntArray)
    abstract fun readBuffer(src: Int)

    abstract fun getInteger(pname: Int): Int

    abstract fun createShader(type: Int): GLShader
    abstract fun shaderSource(shader: GLShader, source: String)
    abstract fun compileShader(shader: GLShader)
    abstract fun getShaderi(shader: GLShader, pname: Int): Int
    abstract fun getShaderInfoLog(shader: GLShader, maxLength: Int): String
    abstract fun deleteShader(shader: GLShader)
    abstract fun createProgram(): GLProgram
    abstract fun attachShader(program: GLProgram, shader: GLShader)
    abstract fun detachShader(program: GLProgram, shader: GLShader)
    abstract fun linkProgram(program: GLProgram)
    abstract fun getProgrami(program: GLProgram, pname: Int): Int
    abstract fun getProgramInfoLog(program: GLProgram, maxLength: Int): String
    abstract fun useProgram(program: GLProgram?)
    abstract fun deleteProgram(program: GLProgram)

    abstract fun getUniformLocation(program: GLProgram, name: String): GLUniformLocation?
    abstract fun uniform1i(location: GLUniformLocation, x: Int)
    abstract fun uniform1f(location: GLUniformLocation, x: Float)
    abstract fun uniform2f(location: GLUniformLocation, x: Float, y: Float)
    abstract fun uniform3f(location: GLUniformLocation, x: Float, y: Float, z: Float)
    abstract fun uniform4f(location: GLUniformLocation, x: Float, y: Float, z: Float, w: Float)
    abstract fun uniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray)
}