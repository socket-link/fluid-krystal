package link.socket.krystal.curve

sealed interface CurveInputScale {
    data object None : CurveInputScale
    data object Auto : CurveInputScale
    data class Fixed(val scale: Float) : CurveInputScale {
        init {
            require(scale > 0f && scale <= 1f) {
                "Scale must be between 0 and 1"
            }
        }
    }

    companion object {
        val Default: CurveInputScale = None
    }
}
