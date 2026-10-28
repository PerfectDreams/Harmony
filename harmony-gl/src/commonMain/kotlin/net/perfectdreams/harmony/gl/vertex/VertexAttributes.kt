package net.perfectdreams.harmony.gl.vertex

import net.perfectdreams.harmony.gl.GLConstants.GL_FLOAT
import net.perfectdreams.harmony.gl.GLConstants.GL_INT
import net.perfectdreams.harmony.gl.GLContext

fun buildVertexAttributes(block: VertexAttributesBuilder.() -> (Unit)): VertexAttributes {
    val builder = VertexAttributesBuilder()
    block(builder)

    return VertexAttributes(
        builder.currentStride,
        builder.attributes.map {
            VertexAttributes.Attribute(it.index, it.size, it.type, it.normalized, it.offset)
        }
    )
}

class VertexAttributesBuilder {
    data class Attribute(
        val index: Int,
        val size: Int,
        val type: Int,
        val normalized: Boolean,
        val offset: Int
    )

    var currentOffset = 0
    var currentStride = 0
    val attributes = mutableListOf<Attribute>()

    fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean) {
        require(attributes.none { it.index == index }) { "Attribute with index $index already exists!" }
        this.attributes.add(Attribute(index, size, type, normalized, this.currentOffset))
        val multiply = when (type) {
            GL_FLOAT -> Float.SIZE_BYTES
            GL_INT -> Int.SIZE_BYTES
            else -> error("I don't know how to handle attribute type $type!")
        }
        reserve(size * multiply)
    }

    fun vec2(index: Int) = vertexAttribPointer(index, 2, GL_FLOAT, false)
    fun vec3(index: Int) = vertexAttribPointer(index, 3, GL_FLOAT, false)
    fun vec4(index: Int) = vertexAttribPointer(index, 4, GL_FLOAT, false)

    fun int(index: Int) = vertexAttribPointer(index, 1, GL_INT, false)

    fun reserve(size: Int) {
        this.currentStride += size
        this.currentOffset += size
    }
}

class VertexAttributes(
    /**
     * The sum of the size of each attribute, including the per-type size
     */
    val totalSizeWithTypeSize: Int,
    val attributes: List<Attribute>
) {
    data class Attribute(
        val index: Int,
        val size: Int,
        val type: Int,
        val normalized: Boolean,
        val offset: Int,
    )

    /**
     * The sum of the size of each attribute
     *
     * Does NOT include the per-type size!
     */
    val totalSize = attributes.sumOf { it.size }

    fun apply(gl: GLContext) {
        for (attribute in this.attributes) {
            // stride is from the START of the current "section"!
            when (attribute.type) {
                GL_FLOAT -> gl.vertexAttribPointer(attribute.index, attribute.size, attribute.type, attribute.normalized, this.totalSizeWithTypeSize, attribute.offset.toLong())
                // YES WE NEED TO USE glVertexAttribIPointer FOR GL_INT!!!
                GL_INT -> gl.vertexAttribIPointer(attribute.index, attribute.size, attribute.type, this.totalSizeWithTypeSize, attribute.offset)
                // Technically should never happen because vertexAttribPointer already blocks it
                else -> error("I don't know how to handle attribute type ${attribute.type}!")
            }

            gl.enableVertexAttribArray(attribute.index)
        }
    }
}