package net.perfectdreams.harmony.gl.rendertarget

import net.perfectdreams.harmony.gl.GLContext

class WindowRenderTarget(gl: GLContext, initialWidth: Int, initialHeight: Int) : RenderTarget(gl) {
    override val width = initialWidth
    override val height = initialHeight
}