package link.socket.krystal.curve

object CurveDefaults {
    const val CURVE_INTENSITY = 50f
    const val DRAW_CONTENT_BEHIND = false

    fun style(
        curveIntensity: Float = CURVE_INTENSITY,
    ): CurveStyle = CurveStyle(curveIntensity)

    fun curveEnabled(): Boolean = true
}
