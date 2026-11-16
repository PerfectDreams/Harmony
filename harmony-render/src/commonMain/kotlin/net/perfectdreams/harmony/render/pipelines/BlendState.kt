package net.perfectdreams.harmony.render.pipelines

import net.perfectdreams.harmony.gl.GLConstants

class BlendState(
    val srcColor: Int,
    val dstColor: Int,
    val srcAlpha: Int = srcColor,
    val dstAlpha: Int = dstColor,
    val colorEquation: Int = GLConstants.GL_FUNC_ADD,
    val alphaEquation: Int = colorEquation,
) {
    companion object {
        val AlphaBlending = BlendState(
            GLConstants.GL_SRC_ALPHA,
            GLConstants.GL_ONE_MINUS_SRC_ALPHA,
            GLConstants.GL_ONE,
            GLConstants.GL_ONE_MINUS_SRC_ALPHA
        )
    }
}