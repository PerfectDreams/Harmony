package net.perfectdreams.harmony.gl.shaders

import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.textures.Texture
import net.perfectdreams.harmony.gl.textures.TextureArray

abstract class ShaderProgram(val gl: GLContext, val programId: Int) {
    private fun getUniformLocation(name: String): Int {
        val location = gl.getUniformLocation(programId, name)
        if (location == -1) {
            println("Program $programId does not have uniform \"$name\"!")
            return -1
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

    abstract class Uniform(val shader: ShaderProgram, val location: Int, val name: String) {
        fun isUniformLocationInvalidAndLog(): Boolean {
            if (this.location == -1) {
                println("Program ${shader.programId} does not have uniform \"$name\"! Ignoring set...")
                return true
            }

            return false
        }
    }

    class Uniform1f(shader: ShaderProgram, location: Int, name: String) : Uniform(shader, location, name) {
        fun set(x: Float) {
            if (isUniformLocationInvalidAndLog())
                return

            shader.gl.programUniform1f(shader.programId, location, x)
        }
    }

    class Uniform2f(shader: ShaderProgram, location: Int, name: String) : Uniform(shader, location, name) {
        fun set(x: Float, y: Float) {
            if (isUniformLocationInvalidAndLog())
                return

            shader.gl.programUniform2f(shader.programId, location, x, y)
        }
    }

    class Uniform3f(shader: ShaderProgram, location: Int, name: String) : Uniform(shader, location, name) {
        fun set(x: Float, y: Float, z: Float) {
            if (isUniformLocationInvalidAndLog())
                return

            shader.gl.programUniform3f(shader.programId, location, x, y, z)
        }
    }

    class Uniform4f(shader: ShaderProgram, location: Int, name: String) : Uniform(shader, location, name) {
        fun set(x: Float, y: Float, z: Float, w: Float) {
            if (isUniformLocationInvalidAndLog())
                return

            shader.gl.programUniform4f(shader.programId, location, x, y, z, w)
        }
    }

    class UniformMatrix4fv(shader: ShaderProgram, location: Int, name: String) : Uniform(shader, location, name) {
        fun set(value: FloatArray) {
            if (isUniformLocationInvalidAndLog())
                return

            shader.gl.programUniformMatrix4fv(shader.programId, location, false, value)
        }
    }

    class Uniform1i(shader: ShaderProgram, location: Int, name: String) : Uniform(shader, location, name) {
        fun set(x: Int) {
            if (isUniformLocationInvalidAndLog())
                return

            shader.gl.programUniform1i(shader.programId, location, x)
        }
    }

    class UniformSampler2D(shader: ShaderProgram, location: Int, name: String) : Uniform(shader, location, name) {
        fun set(slot: Int, texture: Texture) = set(slot, texture.textureId)

        fun set(slot: Int, textureId: Int) {
            if (isUniformLocationInvalidAndLog())
                return

            // Hacky!!!
            val pos = slot + GLConstants.GL_TEXTURE0

            shader.gl.activeTexture(pos)
            shader.gl.bindTexture(GLConstants.GL_TEXTURE_2D, textureId)

            shader.gl.programUniform1i(shader.programId, location, slot)
        }
    }

    class UniformSampler2DArray(shader: ShaderProgram, location: Int, name: String) : Uniform(shader, location, name) {
        fun set(slot: Int, texture: TextureArray) = set(slot, texture.textureId)

        fun set(slot: Int, textureId: Int) {
            if (isUniformLocationInvalidAndLog())
                return

            // Hacky!!!
            val pos = slot + GLConstants.GL_TEXTURE0

            shader.gl.activeTexture(pos)
            shader.gl.bindTexture(GLConstants.GL_TEXTURE_2D_ARRAY, textureId)

            shader.gl.programUniform1i(shader.programId, location, slot)
        }
    }

    class Uniform1Bool(shader: ShaderProgram, location: Int, name: String) : Uniform(shader, location, name) {
        fun set(x: Boolean) {
            if (isUniformLocationInvalidAndLog())
                return

            shader.gl.programUniform1i(shader.programId, location, if (x) 1 else 0)
        }
    }
}

inline fun <T : ShaderProgram> T.withProgram(block: T.() -> (Unit)) {
    val previous = gl.activeProgram
    gl.useProgram(this.programId)
    try {
        block.invoke(this)
    } finally {
        gl.useProgram(previous)
    }
}