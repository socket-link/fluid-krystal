# SOUL.md

## Who We Are

Fluid Krystal exists to make liquid glass portable.

The goal is not to copy a screenshot from one Apple keynote frame and freeze it into a theme. The goal is to recreate the behavior of living material: blur that samples real content, tint that reacts to context, highlights that imply depth, and curved distortion that makes a surface feel suspended above what sits behind it.

Cross-platform UI kits usually flatten this into transparency plus shadow. That is not enough. Glass is not a color. It is an optical system.

## What We Are Actually Building

- A set of Kotlin Multiplatform primitives for translucent containers, controls, and layered surfaces.
- A rendering playground that proves the material across Android, iOS, desktop, and web.
- A fallback strategy that keeps the effect readable and intentional even when the target platform cannot match the ideal pipeline.

## How We Think About Glass

- **Glass is behavior, not decoration.** Blur alone does not create the effect. The material comes from backdrop capture, tint, edge contrast, noise, curvature, and motion working together.
- **Perceptual consistency beats literal parity.** Different targets have different graphics stacks and performance ceilings. Keep the sensation consistent, even when the implementation changes.
- **Performance is aesthetics.** A beautiful effect that stutters is a broken effect.
- **Shared semantics, native execution.** The API should describe one material family. The render path should respect the platform it runs on.
- **Graceful degradation matters.** If a platform cannot support a full distortion pass, the fallback should still feel deliberate rather than broken.

## Values When Contributing

- **Optical honesty.** Name the actual mechanism: blur, tint, refraction, curve, sampling, contrast.
- **Small primitives over giant magic.** Prefer composable building blocks to one opaque "do everything" surface.
- **Debuggability matters.** Demos and inspection tools are part of the product because rendering work is hard to reason about without visible probes.
- **Native empathy.** Build toward each platform's strengths instead of forcing one renderer to pretend every target is the same.
- **Document the sharp edges.** If a behavior is expensive, approximate, or platform-limited, say so.

## Working With The Maintainer

The maintainer is reviving this project to make it useful again, not to preserve a museum piece.

- Surface tradeoffs early: fidelity versus cost, shared API purity versus platform exceptions, polish versus reach.
- Bring evidence when possible: screenshots, recordings, device notes, frame timing, or exact reproduction steps.
- Say when a platform gap is fundamental instead of hiding it behind abstraction.
- Prefer incremental improvements that keep the library usable between experiments.
