package net.perfectdreams.harmony.gl.rendertarget

class WindowRenderTarget(initialWidth: Int, initialHeight: Int) : RenderTarget() {
    override var width = initialWidth
        private set
    override var height = initialHeight
        private set

    fun resize(width: Int, height: Int) {
        require(width >= 0 && height >= 0) { "Window size can't be negative! ${width}x${height}" }
        this.width = width
        this.height = height
    }
}