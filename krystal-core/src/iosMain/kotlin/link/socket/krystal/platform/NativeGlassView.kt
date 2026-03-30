@file:OptIn(ExperimentalForeignApi::class)

package link.socket.krystal.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import link.socket.krystal.api.GlassStyle
import link.socket.krystal.api.GlassTint
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
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

internal val isIos26OrLater: Boolean by lazy {
    val version = cValue<NSOperatingSystemVersion> {
        majorVersion = 26
        minorVersion = 0
        patchVersion = 0
    }
    NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(version)
}

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
        clipsToBounds = true
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
    // On iOS 26+, UIBlurEffectStyleSystemGlass would be the Liquid Glass
    // material. Since that enum value may not be available in the current
    // Kotlin/Native UIKit bindings, we use systemThinMaterial as the
    // base and rely on tint color to achieve the glass appearance.
    // When Kotlin/Native bindings are updated for iOS 26, swap to the
    // glass-specific style here.
    return UIBlurEffect.effectWithStyle(UIBlurEffectStyle.UIBlurEffectStyleSystemThinMaterial)
}

private fun applyTint(effectView: UIVisualEffectView, style: GlassStyle) {
    val tintColor = style.tint.toUIColor()

    // Apply tint as a colored overlay inside the contentView
    effectView.contentView.backgroundColor = tintColor

    effectView.backgroundColor = UIColor.clearColor
}

private fun applyCornerRadius(effectView: UIVisualEffectView, style: GlassStyle) {
    val radius = style.cornerRadius.value.toDouble()
    effectView.layer.cornerRadius = radius
    effectView.clipsToBounds = true
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
