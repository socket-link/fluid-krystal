package link.socket.krystal.curve

import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.Shader

internal val NOISE_SHADER: Shader by unsynchronizedLazy {
    Shader.makeFractalNoise(
        baseFrequencyX = 0.45f,
        baseFrequencyY = 0.45f,
        numOctaves = 4,
        seed = 1.0f,
    )
}

private const val CURVE_SHADER_SKSL = """
uniform shader content;
uniform float curveIntensity; 
uniform vec4 crop;
uniform shader mask;
uniform shader displacement;

vec4 main(vec2 coord) {
    // Offset the coord for the mask, but coerce it to be at least 0, 0
    vec2 maskCoord = max(coord - crop.xy, vec2(0.0, 0.0));
    float intensity = mask.eval(maskCoord).a;

    // Sample from the displacement shader to get displacement values
    vec4 displacementColor = displacement.eval(coord);
    
    // Convert displacement color to displacement vector
    // Use a much smaller scale for displacement
    vec2 displacementVector = (displacementColor.rg - 0.5) * 2.0 * curveIntensity * intensity;
    
    // Apply displacement to sampling coordinate
    vec2 displacedCoord = coord + displacementVector;
    
    // Clamp to crop bounds to prevent sampling outside valid area
    displacedCoord = clamp(displacedCoord, crop.xy, crop.zw);
    
    return content.eval(displacedCoord);
}
"""

internal val CURVE_SHADER_EFFECT: RuntimeEffect by unsynchronizedLazy {
    RuntimeEffect.makeForShader(CURVE_SHADER_SKSL)
}

internal val COMPOSITE_SHADER_EFFECT: RuntimeEffect by unsynchronizedLazy {
    RuntimeEffect.makeForShader(COMPOSITE_SHADER_SKSL)
}

private const val COMPOSITE_SHADER_SKSL = """
  uniform shader content;
  uniform shader curve;
  uniform shader noise;

  uniform vec4 glassTint;
  uniform vec4 overlayTint;
  uniform vec4 vibrancyTint;
  uniform float noiseFactor;
  uniform float saturation;

  half4 main(vec2 coord) {
    half4 c = curve.eval(coord);
    half4 n = noise.eval(coord);

    // Apply saturation adjustment
    float luma = dot(c.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 saturated = mix(vec3(luma), c.rgb, saturation);

    // Layer glass tint
    vec3 tinted = mix(saturated, glassTint.rgb, glassTint.a);

    // Layer overlay tint
    tinted = mix(tinted, overlayTint.rgb, overlayTint.a);

    // Apply vibrancy tint (additive blend)
    tinted += vibrancyTint.rgb * vibrancyTint.a;

    // Apply noise texture
    float noiseLuma = dot(n.rgb, vec3(0.2126, 0.7152, 0.0722));
    float noiseOverlay = saturate(noiseLuma * noiseFactor);
    tinted = mix(tinted, vec3(1.0), noiseOverlay);

    return half4(tinted, c.a);
  }
"""
