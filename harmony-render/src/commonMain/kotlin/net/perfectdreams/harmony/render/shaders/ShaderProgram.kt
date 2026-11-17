package net.perfectdreams.harmony.render.shaders

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.gl.GLTexture
import net.perfectdreams.harmony.gl.GLUniformLocation
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.textures.Texture
import net.perfectdreams.harmony.render.textures.TextureArray

abstract class ShaderProgram(val device: RenderDevice, val programId: GLProgram) {
    private fun getUniformLocation(name: String): GLUniformLocation? {
        val location = device.gl.getUniformLocation(programId, name)
        if (location == null) {
            println("Program $programId does not have uniform \"$name\"!")
            return null
        }

        return location
    }

    fun uniform1f(name: String) = Uniform1f(this, getUniformLocation(name), name)
    fun uniform1Bool(name: String) = Uniform1Bool(this, getUniformLocation(name), name)
    fun uniform2f(name: String) = Uniform2f(this, getUniformLocation(name), name)
    fun uniform3f(name: String) = Uniform3f(this, getUniformLocation(name), name)
    fun uniform4f(name: String) = Uniform4f(this, getUniformLocation(name), name)
    fun uniformMatrix4fv(name: String) = UniformMatrix4fv(this, getUniformLocation(name), name)
    fun uniform1i(name: String) = Uniform1i(this, getUniformLocation(name), name)
    fun uniformSampler2D(name: String) = UniformSampler2D(this, getUniformLocation(name), name)
    fun uniformSampler2DShadow(name: String) = uniformSampler2D(name)
    fun uniformSampler2DArray(name: String) = UniformSampler2DArray(this, getUniformLocation(name), name)

    abstract class Uniform(val shader: ShaderProgram, val location: GLUniformLocation?, val name: String) {
        fun isUniformLocationInvalidAndLog(): GLUniformLocation? {
            if (this.location == null) {
                println("Program ${shader.programId} does not have uniform \"$name\"! Ignoring set...")
                return null
            }

            return location
        }

        fun checkIfShaderIsActive() {
            check(shader == shader.device.activeRenderPassScope?.activeShaderProgram) { "Trying to change uniform $name of shader ${shader.programId}, but the current active shader is ${shader.device.activeRenderPassScope?.activeShaderProgram}!" }
        }

        fun increaseRenderPassState() = shader.device.activeRenderPassScope?.stateVersion++
    }

    class Uniform1f(shader: ShaderProgram, location: GLUniformLocation?, name: String) : Uniform(shader, location, name) {
        fun set(x: Float) {
            checkIfShaderIsActive()
            increaseRenderPassState()
            val location = isUniformLocationInvalidAndLog() ?: return
            shader.device.gl.uniform1f(location, x)
        }
    }

    class Uniform2f(shader: ShaderProgram, location: GLUniformLocation?, name: String) : Uniform(shader, location, name) {
        fun set(x: Float, y: Float) {
            checkIfShaderIsActive()
            increaseRenderPassState()
            val location = isUniformLocationInvalidAndLog() ?: return
            shader.device.gl.uniform2f(location, x, y)
        }
    }

    class Uniform3f(shader: ShaderProgram, location: GLUniformLocation?, name: String) : Uniform(shader, location, name) {
        fun set(x: Float, y: Float, z: Float) {
            checkIfShaderIsActive()
            increaseRenderPassState()
            val location = isUniformLocationInvalidAndLog() ?: return
            shader.device.gl.uniform3f(location, x, y, z)
        }
    }

    class Uniform4f(shader: ShaderProgram, location: GLUniformLocation?, name: String) : Uniform(shader, location, name) {
        fun set(x: Float, y: Float, z: Float, w: Float) {
            checkIfShaderIsActive()
            increaseRenderPassState()
            val location = isUniformLocationInvalidAndLog() ?: return
            shader.device.gl.uniform4f(location, x, y, z, w)
        }
    }

    class UniformMatrix4fv(shader: ShaderProgram, location: GLUniformLocation?, name: String) : Uniform(shader, location, name) {
        fun set(value: FloatArray) {
            checkIfShaderIsActive()
            increaseRenderPassState()
            val location = isUniformLocationInvalidAndLog() ?: return
            shader.device.gl.uniformMatrix4fv(location, false, value)
        }
    }

    class Uniform1i(shader: ShaderProgram, location: GLUniformLocation?, name: String) : Uniform(shader, location, name) {
        fun set(x: Int) {
            checkIfShaderIsActive()
            increaseRenderPassState()
            val location = isUniformLocationInvalidAndLog() ?: return
            shader.device.gl.uniform1i(location, x)
        }
    }

    class UniformSampler2D(shader: ShaderProgram, location: GLUniformLocation?, name: String) : Uniform(shader, location, name) {
        fun set(slot: Int, texture: Texture) = set(slot, texture.textureId)

        fun set(slot: Int, textureId: GLTexture) {
            checkIfShaderIsActive()
            increaseRenderPassState()
            val location = isUniformLocationInvalidAndLog() ?: return

            // Hacky!!!
            val pos = slot + GLConstants.GL_TEXTURE0

            shader.device.gl.activeTexture(pos)
            shader.device.gl.bindTexture(GLConstants.GL_TEXTURE_2D, textureId)

            shader.device.gl.uniform1i(location, slot)
        }
    }

    class UniformSampler2DArray(shader: ShaderProgram, location: GLUniformLocation?, name: String) : Uniform(shader, location, name) {
        fun set(slot: Int, texture: TextureArray) = set(slot, texture.textureId)

        fun set(slot: Int, textureId: GLTexture) {
            checkIfShaderIsActive()
            increaseRenderPassState()
            val location = isUniformLocationInvalidAndLog() ?: return

            // Hacky!!!
            val pos = slot + GLConstants.GL_TEXTURE0

            shader.device.gl.activeTexture(pos)
            shader.device.gl.bindTexture(GLConstants.GL_TEXTURE_2D_ARRAY, textureId)

            shader.device.gl.uniform1i(location, slot)
        }
    }

    class Uniform1Bool(shader: ShaderProgram, location: GLUniformLocation?, name: String) : Uniform(shader, location, name) {
        fun set(x: Boolean) {
            checkIfShaderIsActive()
            increaseRenderPassState()
            val location = isUniformLocationInvalidAndLog() ?: return
            shader.device.gl.uniform1i(location, if (x) 1 else 0)
        }
    }
}