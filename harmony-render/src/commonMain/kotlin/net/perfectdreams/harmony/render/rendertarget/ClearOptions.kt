package net.perfectdreams.harmony.render.rendertarget

class ClearOptions(
    val color: Color? = null,
    val depth: Float? = null,
    val stencil: Int? = null
) {
    companion object {
        val Nothing = ClearOptions()
    }

    data class Color(
        val red: Float,
        val green: Float,
        val blue: Float,
        val alpha: Float
    )
}