package net.perfectdreams.harmony.render.pipelines

import net.perfectdreams.harmony.gl.GLConstants

enum class CullFace(val glValue: Int) {
    FRONT(GLConstants.GL_FRONT),
    BACK(GLConstants.GL_BACK)
}
