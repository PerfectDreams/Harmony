package net.perfectdreams.harmony.gl.pipelines

import net.perfectdreams.harmony.gl.GLConstants

enum class CompareFunc(val glValue: Int) {
    NEVER(GLConstants.GL_NEVER),
    LESS_THAN(GLConstants.GL_LESS),
    EQUAL(GLConstants.GL_EQUAL),
    LESS_THAN_OR_EQUAL(GLConstants.GL_LEQUAL),
    GREATER_THAN(GLConstants.GL_GREATER),
    NOT_EQUAL(GLConstants.GL_NOTEQUAL),
    GREATER_THAN_OR_EQUAL(GLConstants.GL_GEQUAL),
    ALWAYS(GLConstants.GL_ALWAYS)
}