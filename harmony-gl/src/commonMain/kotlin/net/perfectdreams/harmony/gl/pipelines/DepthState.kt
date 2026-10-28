package net.perfectdreams.harmony.gl.pipelines

class DepthState(
    /**
     * Controls if the depth buffer should be written to
     */
    val write: Boolean,

    /**
     * Controls the comparison function used to compare depth values
     */
    val compare: CompareFunc = CompareFunc.LESS_THAN
)