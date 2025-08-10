package link.socket.krystal.util

internal object DirtyFields {
    const val CurveEnabled: Int = 0b1
    const val InputScale: Int = CurveEnabled shl 1
    const val ScreenPosition = InputScale shl 1
    const val AreaOffsets = ScreenPosition shl 1
    const val Size = AreaOffsets shl 1
    const val CurveIntensity = Size shl 1
    const val NoiseFactor = CurveIntensity shl 1
    const val Mask = NoiseFactor shl 1
    const val BackgroundColor = Mask shl 1
    const val Tints = BackgroundColor shl 1
    const val FallbackTint = Tints shl 1
    const val Alpha = FallbackTint shl 1
    const val Progressive = Alpha shl 1
    const val Areas = Progressive shl 1
    const val LayerSize = Areas shl 1
    const val LayerOffset = LayerSize shl 1
    const val DrawContentBehind = LayerOffset shl 1

    const val RenderEffectAffectingFlags =
        CurveEnabled or
            InputScale or
            Size or
            LayerSize or
            LayerOffset or
            CurveIntensity or
            NoiseFactor or
            Mask or
            Tints or
            FallbackTint or
            Progressive

    const val InvalidateFlags =
        RenderEffectAffectingFlags or
            CurveEnabled or
            InputScale or
            AreaOffsets or
            Size or
            LayerSize or
            LayerOffset or
            BackgroundColor or
            Progressive or
            Areas or
            Alpha or
            DrawContentBehind
}
