package net.perfectdreams.harmony.gl.webgl2

import js.array.toJsArray
import js.buffer.ArrayBufferLike
import js.numbers.JsNumbers.toJsInt
import js.reflect.unsafeCast
import js.typedarrays.Uint8Array
import net.perfectdreams.harmony.gl.ByteArrayTextureSource
import net.perfectdreams.harmony.gl.GLBuffer
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GLFramebuffer
import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.gl.GLRenderbuffer
import net.perfectdreams.harmony.gl.GLShader
import net.perfectdreams.harmony.gl.GLTexture
import net.perfectdreams.harmony.gl.GLUniformLocation
import net.perfectdreams.harmony.gl.GLVertexArrayObject
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

class WebGL2Context(val context: WebGL2RenderingContext) : GLContext() {
    private fun GLTexture.unwrap(): web.gl.WebGLTexture = (this as WebGLTexture).handle
    private fun GLProgram.unwrap(): web.gl.WebGLProgram = (this as WebGLProgram).handle
    private fun GLShader.unwrap(): web.gl.WebGLShader = (this as WebGLShader).handle
    private fun GLUniformLocation.unwrap(): web.gl.WebGLUniformLocation = (this as WebGLUniformLocation).handle
    private fun GLBuffer.unwrap(): web.gl.WebGLBuffer = (this as WebGLBuffer).handle
    private fun GLVertexArrayObject.unwrap(): web.gl.WebGLVertexArrayObject = (this as WebGLVertexArrayObject).handle
    private fun GLFramebuffer.unwrap(): web.gl.WebGLFramebuffer = (this as WebGLFramebuffer).handle
    private fun GLRenderbuffer.unwrap(): web.gl.WebGLRenderbuffer = (this as WebGLRenderbuffer).handle

    override fun enable(target: Int) = context.enable(target.asGLenum())
    override fun disable(target: Int) = context.disable(target.asGLenum())
    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) = context.clearColor(red, green, blue, alpha)
    override fun clearDepth(depth: Float) = context.clearDepth(depth)
    override fun clearStencil(stencil: Int) = context.clearStencil(stencil)

    override fun clear(mask: Int) = context.clear(mask.asGLbitfield())
    override fun viewport(x: Int, y: Int, width: Int, height: Int) = context.viewport(x, y, width, height)
    override fun scissor(x: Int, y: Int, width: Int, height: Int) = context.scissor(x, y, width, height)
    override fun bindBuffer(target: Int, buffer: GLBuffer?) = context.bindBuffer(target.asGLenum(), buffer?.unwrap())
    override fun bufferData(target: Int, size: Long, usage: Int) = context.bufferData(target.asGLenum(), size.toInt(), usage.asGLenum())
    override fun bufferData(target: Int, buffer: FloatArray, usage: Int) = context.bufferData(target.asGLenum(), buffer.toFloat32ArrayFast(), usage.asGLenum())
    override fun bufferData(target: Int, buffer: IntArray, usage: Int) = context.bufferData(target.asGLenum(), buffer.toInt32ArrayFast(), usage.asGLenum())
    override fun bufferData(target: Int, buffer: ByteArray, usage: Int) = context.bufferData(target.asGLenum(), buffer.toArrayBufferFast(), usage.asGLenum())

    override fun bufferSubData(target: Int, offset: Long, buffer: FloatArray) = context.bufferSubData(target.asGLenum(), offset.toInt(), buffer.toFloat32ArrayFast())
    override fun deleteBuffers(values: List<GLBuffer>) {
        for (bufferId in values) {
            context.deleteBuffer(bufferId.unwrap())
        }
    }
    override fun bindVertexArray(array: GLVertexArrayObject?) = context.bindVertexArray(array?.unwrap())
    override fun deleteVertexArrays(values: List<GLVertexArrayObject>) {
        for (vertexArrayId in values) {
            context.deleteVertexArray(vertexArrayId.unwrap())
        }
    }
    override fun drawArrays(mode: Int, first: Int, count: Int) = context.drawArrays(mode.asGLenum(), first, count)
    override fun drawArraysInstanced(mode: Int, first: Int, count: Int, primcount: Int) = context.drawArraysInstanced(mode.asGLenum(), first, count, primcount)
    override fun drawElements(mode: Int, count: Int, type: Int, offset: Long) = context.drawElements(mode.asGLenum(), count, type.asGLenum(), offset.toInt())
    override fun drawElementsInstanced(mode: Int, count: Int, type: Int, indices: Long, primcount: Int) = context.drawElementsInstanced(mode.asGLenum(), count, type.asGLenum(), indices.toInt(), primcount.toInt())

    override fun genTextures() = WebGLTexture(context.createTexture())
    override fun bindTexture(target: Int, texture: GLTexture?) = context.bindTexture(target.asGLenum(), texture?.unwrap())
    override fun activeTexture(texture: Int) = context.activeTexture(texture.asGLenum())
    override fun deleteTextures(values: List<GLTexture>) {
        if (values.isNotEmpty()) {
            for (textureId in values) {
                context.deleteTexture((textureId as WebGLTexture).handle)
            }
        }
    }
    override fun genVertexArrays() = WebGLVertexArrayObject(context.createVertexArray())
    override fun genBuffers() = WebGLBuffer(context.createBuffer())
    override fun blendFunc(src: Int, dst: Int) = context.blendFunc(src.asGLenum(), dst.asGLenum())
    override fun blendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) = context.blendFuncSeparate(srcRGB.asGLenum(), dstRGB.asGLenum(), srcAlpha.asGLenum(), dstAlpha.asGLenum())
    override fun blendEquation(mode: Int) = context.blendEquation(mode.asGLenum())
    override fun cullFace(mode: Int) = context.cullFace(mode.asGLenum())
    override fun polygonOffset(factor: Float, units: Float) = context.polygonOffset(factor, units)
    override fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long) = context.vertexAttribPointer(index.asGLuint(), size, type.asGLenum(), normalized, stride, offset.toInt())
    override fun vertexAttribIPointer(index: Int, size: Int, type: Int, stride: Int, offset: Int) = context.vertexAttribIPointer(index.asGLuint(), size, type.asGLenum(), stride, offset)

    override fun enableVertexAttribArray(index: Int) = context.enableVertexAttribArray(index.asGLuint())
    override fun disableVertexAttribArray(index: Int) = context.disableVertexAttribArray(index.asGLuint())
    override fun vertexAttribDivisor(index: Int, divisor: Int) = context.vertexAttribDivisor(index.asGLuint(), divisor.asGLuint())

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
            is BrowserImageTexImageSource -> {
                context.texImage2D(target.asGLenum(), level, internalFormat.asGLenum(), textureSource.width, textureSource.height, border, format.asGLenum(), type.asGLenum(), textureSource.image)
            }
            else -> error("Unsupported image type!")
        }
    }

    override fun texImage3D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int, depth: Int, border: Int, format: Int, type: Int) = context.texImage3D(target.asGLenum(), level, internalFormat.asGLenum(), width, height, depth, border, format.asGLenum(), type.asGLenum(), null)

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
                val pixels = Uint8Array<ArrayBufferLike>(textureSource.pixels.toArrayBufferFast())
                context.texImage3D(target.asGLenum(), level, internalFormat.asGLenum(), textureSource.width, layerHeight, depth, border, format.asGLenum(), type.asGLenum(), pixels)
            }
            is BrowserImageTexImageSource -> {
                context.texImage3D(target.asGLenum(), level, internalFormat.asGLenum(), textureSource.width, layerHeight, depth, border, format.asGLenum(), type.asGLenum(), textureSource.image)
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
                context.texSubImage2D(target.asGLenum(), level, xoffset, yoffset, width, height, format.asGLenum(), type.asGLenum(), pixels)
            }
            is BrowserImageTexImageSource -> {
                context.texSubImage2D(target.asGLenum(), level, xoffset, yoffset, width, height, format.asGLenum(), type.asGLenum(), textureSource.image)
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
                val pixels = Uint8Array<ArrayBufferLike>(textureSource.pixels.toArrayBufferFast())
                context.texSubImage3D(target.asGLenum(), level, xoffset, yoffset, zoffset, width, height, depth, format.asGLenum(), type.asGLenum(), pixels)
            }
            is BrowserImageTexImageSource -> {
                context.texSubImage3D(target.asGLenum(), level, xoffset, yoffset, zoffset, width, height, depth, format.asGLenum(), type.asGLenum(), textureSource.image)
            }
            else -> error("Unsupported image type!")
        }
    }

    override fun texParameteri(target: Int, pname: Int, param: Int) = context.texParameteri(target.asGLenum(), pname.asGLenum(), param.asGLenum())
    override fun generateMipmap(target: Int) = context.generateMipmap(target.asGLenum())
    override fun depthFunc(func: Int) = context.depthFunc(func.asGLenum())
    override fun depthMask(flag: Boolean) = context.depthMask(flag)
    override fun stencilMask(mask: Int) = context.stencilMask(mask.asGLuint())

    override fun genFramebuffers() = WebGLFramebuffer(context.createFramebuffer())
    override fun deleteFramebuffers(values: List<GLFramebuffer>) {
        for (textureId in values) {
            context.deleteFramebuffer(textureId.unwrap())
        }
    }
    override fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: GLTexture?, level: Int) = context.framebufferTexture2D(target.asGLenum(), attachment.asGLenum(), textarget.asGLenum(), texture?.unwrap(), level)
    override fun framebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: GLRenderbuffer?) = context.framebufferRenderbuffer(target.asGLenum(), attachment.asGLenum(), renderbuffertarget.asGLenum(), renderbuffer?.unwrap())
    override fun checkFramebufferStatus(target: Int) = unsafeCast<JsNumber>(context.checkFramebufferStatus(target.asGLenum())).toInt()
    override fun bindFramebuffer(target: Int, framebuffer: GLFramebuffer?) = context.bindFramebuffer(target.asGLenum(), framebuffer?.unwrap())
    override fun blitFramebuffer(srcX0: Int, srcY0: Int, srcX1: Int, srcY1: Int, dstX0: Int, dstY0: Int, dstX1: Int, dstY1: Int, mask: Int, filter: Int) = context.blitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask.asGLbitfield(), filter.asGLenum())
    override fun genRenderbuffers() = WebGLRenderbuffer(context.createRenderbuffer())
    override fun bindRenderbuffer(target: Int, renderbuffer: GLRenderbuffer?) = context.bindRenderbuffer(target.asGLenum(), renderbuffer?.unwrap())
    override fun renderbufferStorage(target: Int, internalFormat: Int, width: Int, height: Int) = context.renderbufferStorage(target.asGLenum(), internalFormat.asGLenum(), width, height)
    override fun renderbufferStorageMultisample(target: Int, samples: Int, internalFormat: Int, width: Int, height: Int) = context.renderbufferStorageMultisample(target.asGLenum(), samples, internalFormat.asGLenum(), width, height)
    override fun deleteRenderbuffers(values: List<GLRenderbuffer>) {
        for (renderbuffer in values) {
            context.deleteRenderbuffer(renderbuffer.unwrap())
        }
    }
    override fun drawBuffers(buffers: IntArray) = context.drawBuffers(buffers.map { it.asGLenum() }.toJsArray())
    override fun readBuffer(src: Int) = context.readBuffer(src.asGLenum())

    override fun getInteger(pname: Int): Int {
        val value = requireNotNull(context.getParameter(pname.asGLenum())) { "getParameter returned null for pname 0x${pname.toString(16)}!" }
        return unsafeCast<JsNumber>(value).toInt()
    }

    override fun createShader(type: Int) = WebGLShader(context.createShader(type.asGLenum())!!)
    override fun shaderSource(shader: GLShader, source: String) = context.shaderSource(shader.unwrap(), source)
    override fun compileShader(shader: GLShader) = context.compileShader(shader.unwrap())
    override fun getShaderi(shader: GLShader, pname: Int): Int {
        val r = context.getShaderParameter(shader.unwrap(), pname.asGLenum()) as JsBoolean
        return if (r.toBoolean())
            GLConstants.GL_TRUE
        else
            GLConstants.GL_FALSE
    }

    override fun getShaderInfoLog(shader: GLShader, maxLength: Int): String = context.getShaderInfoLog(shader.unwrap())?.take(maxLength) ?: ""
    override fun deleteShader(shader: GLShader) = context.deleteShader(shader.unwrap())
    override fun createProgram() = WebGLProgram(context.createProgram())
    override fun deleteProgram(program: GLProgram) {
        context.deleteProgram(program.unwrap())
    }
    override fun attachShader(program: GLProgram, shader: GLShader) = context.attachShader(program.unwrap(), shader.unwrap())
    override fun detachShader(program: GLProgram, shader: GLShader) = context.detachShader(program.unwrap(), shader.unwrap())
    override fun linkProgram(program: GLProgram) = context.linkProgram(program.unwrap())
    override fun getProgrami(program: GLProgram, pname: Int): Int {
        val r = context.getProgramParameter(program.unwrap(), pname.asGLenum()) as JsBoolean
        return if (r.toBoolean())
            GLConstants.GL_TRUE
        else
            GLConstants.GL_FALSE
    }
    override fun getProgramInfoLog(program: GLProgram, maxLength: Int): String = context.getProgramInfoLog(program.unwrap())?.take(maxLength) ?: ""
    override fun useProgram(program: GLProgram?) = context.useProgram(program?.unwrap())

    override fun getUniformLocation(program: GLProgram, name: String): GLUniformLocation? {
        val program = program.unwrap()
        val uniformLocation = context.getUniformLocation(program, name) ?: return null
        return WebGLUniformLocation(uniformLocation)
    }

    override fun uniform1i(location: GLUniformLocation, x: Int) {
        context.uniform1i(location.unwrap(), x)
    }

    override fun uniform1f(location: GLUniformLocation, x: Float) {
        context.uniform1f(location.unwrap(), x)
    }

    override fun uniform2f(location: GLUniformLocation, x: Float, y: Float) {
        context.uniform2f(location.unwrap(), x, y)
    }

    override fun uniform3f(location: GLUniformLocation, x: Float, y: Float, z: Float) {
        context.uniform3f(location.unwrap(), x, y, z)
    }

    override fun uniform4f(location: GLUniformLocation, x: Float, y: Float, z: Float, w: Float) {
        context.uniform4f(location.unwrap(), x, y, z, w)
    }

    override fun uniformMatrix4fv(location: GLUniformLocation, transpose: Boolean, value: FloatArray) {
        context.uniformMatrix4fv(location.unwrap(), transpose, value.toFloat32ArrayFast())
    }
}