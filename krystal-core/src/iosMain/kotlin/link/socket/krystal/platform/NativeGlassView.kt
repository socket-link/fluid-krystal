@file:OptIn(ExperimentalForeignApi::class)

package link.socket.krystal.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import link.socket.krystal.api.GlassStyle
import link.socket.krystal.api.GlassTint
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIVisualEffectView

// iOS 26 (2025) introduces Liquid Glass as a new UIBlurEffect style.
// On iOS < 26, we fall back to UIBlurEffectStyleSystemThinMaterial which
// provides a similar translucent blur. The Compose content is overlaid on
// top via the normal composition, while the UIVisualEffectView handles
// the backdrop blur natively.
//
// Architecture note: this is the key Apple pass-through — we intentionally
// do NOT use the KMP Skia renderer on Apple platforms. Native glass is
// always better than emulated glass.

@Composable
internal fun NativeGlassEffectView(
    style: GlassStyle,
    modifier: Modifier = Modifier,
) {
    UIKitView(
        factory = {
            createGlassEffectView(style)
        },
        modifier = modifier,
        update = { view ->
            updateGlassEffectView(view, style)
        },
    )
}

private fun createGlassEffectView(style: GlassStyle): UIView {
    val blurEffect = createBlurEffect()
    val effectView = UIVisualEffectView(effect = blurEffect).apply {
        setAutoresizingMask(UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight)
    }

    applyTint(effectView, style)
    applyCornerRadius(effectView, style)

    return effectView
}

private fun updateGlassEffectView(view: UIView, style: GlassStyle) {
    val effectView = view as? UIVisualEffectView ?: return
    applyTint(effectView, style)
    applyCornerRadius(effectView, style)
}

private fun createBlurEffect(): UIBlurEffect {
    // UIBlurEffectStyleSystemGlass is not yet available in the Kotlin/Native UIKit bindings
    // (iOS 26 SDK). Until the headers ship, use SystemUltraThinMaterial on all versions,
    // which is the closest stock blur to Liquid Glass.
    // TODO: switch the isIos26OrLater branch to UIBlurEffectStyleSystemGlass once
    //  Kotlin/Native ships iOS 26 SDK interop headers.
    return UIBlurEffect.effectWithStyle(UIBlurEffectStyle.UIBlurEffectStyleSystemUltraThinMaterial)
}

private fun applyTint(effectView: UIVisualEffectView, style: GlassStyle) {
    val tintColor = style.tint.toUIColor()

    // Apply tint as a colored overlay inside the contentView
    effectView.contentView.backgroundColor = tintColor

    effectView.backgroundColor = UIColor.clearColor
}

private fun applyCornerRadius(effectView: UIVisualEffectView, style: GlassStyle) {
    // Corner radius is handled by the Compose Box clip in GlassSurface.
    // Applying CALayer.cornerRadius here as well causes sub-pixel seams
    // because Skia and Core Animation rasterize the rounded rect independently.
}

internal fun GlassTint.toUIColor(): UIColor = when (this) {
    is GlassTint.Clear -> UIColor(
        red = 1.0,
        green = 1.0,
        blue = 1.0,
        alpha = 0.05,
    )
    is GlassTint.ElectricPurple -> UIColor(
        red = 0.482,
        green = 0.184,
        blue = 0.745,
        alpha = 0.15,
    )
    is GlassTint.CerebralBlue -> UIColor(
        red = 0.231,
        green = 0.510,
        blue = 0.965,
        alpha = 0.15,
    )
    is GlassTint.SignalAmber -> UIColor(
        red = 0.961,
        green = 0.620,
        blue = 0.043,
        alpha = 0.15,
    )
    is GlassTint.Custom -> UIColor(
        red = color.red.toDouble(),
        green = color.green.toDouble(),
        blue = color.blue.toDouble(),
        alpha = alpha.toDouble(),
    )
}
