package net.perfectdreams.harmony.render.textures

import net.perfectdreams.harmony.gl.GLConstants

/**
 * The storage format of a texture or a renderbuffer
 *
 * Only formats that are available on both OpenGL 3.3 and WebGL2 are listed here
 *
 * @param internalFormat the sized internal format, used by `texImage2D`, `texStorage2D` and `renderbufferStorage`
 * @param format the pixel data format, used by `texImage2D` and `texSubImage2D` when uploading/allocating texture data
 * @param type the pixel data type, used by `texImage2D` and `texSubImage2D` when uploading/allocating texture data
 * @param hasDepth if this format has a depth component
 * @param hasStencil if this format has a stencil component
 */
enum class TextureFormat(
    val internalFormat: Int,
    val format: Int,
    val type: Int,
    val hasDepth: Boolean,
    val hasStencil: Boolean
) {
    // ===[ Color formats ]===
    R8(GLConstants.GL_R8, GLConstants.GL_RED, GLConstants.GL_UNSIGNED_BYTE, false, false),
    RG8(GLConstants.GL_RG8, GLConstants.GL_RG, GLConstants.GL_UNSIGNED_BYTE, false, false),
    RGBA8(GLConstants.GL_RGBA8, GLConstants.GL_RGBA, GLConstants.GL_UNSIGNED_BYTE, false, false),
    SRGB8_ALPHA8(GLConstants.GL_SRGB8_ALPHA8, GLConstants.GL_RGBA, GLConstants.GL_UNSIGNED_BYTE, false, false),

    // Rendering to float formats on WebGL2 requires the EXT_color_buffer_float extension
    R16F(GLConstants.GL_R16F, GLConstants.GL_RED, GLConstants.GL_HALF_FLOAT, false, false),
    RG16F(GLConstants.GL_RG16F, GLConstants.GL_RG, GLConstants.GL_HALF_FLOAT, false, false),
    RGBA16F(GLConstants.GL_RGBA16F, GLConstants.GL_RGBA, GLConstants.GL_HALF_FLOAT, false, false),
    R32F(GLConstants.GL_R32F, GLConstants.GL_RED, GLConstants.GL_FLOAT, false, false),
    RG32F(GLConstants.GL_RG32F, GLConstants.GL_RG, GLConstants.GL_FLOAT, false, false),
    RGBA32F(GLConstants.GL_RGBA32F, GLConstants.GL_RGBA, GLConstants.GL_FLOAT, false, false),

    // ===[ Depth formats ]===
    DEPTH16(GLConstants.GL_DEPTH_COMPONENT16, GLConstants.GL_DEPTH_COMPONENT, GLConstants.GL_UNSIGNED_SHORT, true, false),
    // WebGL2 requires UNSIGNED_INT for DEPTH_COMPONENT24
    DEPTH24(GLConstants.GL_DEPTH_COMPONENT24, GLConstants.GL_DEPTH_COMPONENT, GLConstants.GL_UNSIGNED_INT, true, false),
    DEPTH32F(GLConstants.GL_DEPTH_COMPONENT32F, GLConstants.GL_DEPTH_COMPONENT, GLConstants.GL_FLOAT, true, false),

    // ===[ Depth + Stencil formats ]===
    DEPTH24_STENCIL8(GLConstants.GL_DEPTH24_STENCIL8, GLConstants.GL_DEPTH_STENCIL, GLConstants.GL_UNSIGNED_INT_24_8, true, true),
    DEPTH32F_STENCIL8(GLConstants.GL_DEPTH32F_STENCIL8, GLConstants.GL_DEPTH_STENCIL, GLConstants.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, true, true),

    // ===[ Stencil formats ]===
    // Only usable as a renderbuffer! Stencil-only textures require OpenGL 4.4 and are not supported on WebGL2
    STENCIL8(GLConstants.GL_STENCIL_INDEX8, GLConstants.GL_STENCIL_INDEX, GLConstants.GL_UNSIGNED_BYTE, false, true);

    /**
     * The framebuffer attachment point for this depth/stencil format
     *
     * @throws IllegalStateException if this format is a color format
     */
    val depthStencilAttachmentPoint: Int
        get() = when {
            hasDepth && hasStencil -> GLConstants.GL_DEPTH_STENCIL_ATTACHMENT
            hasDepth -> GLConstants.GL_DEPTH_ATTACHMENT
            hasStencil -> GLConstants.GL_STENCIL_ATTACHMENT
            else -> error("$this is a color format and does not have a depth/stencil attachment point!")
        }
}
