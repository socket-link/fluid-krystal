# AGENTS.md

Fluid Krystal is a Kotlin Multiplatform rendering playground and library for recreating liquid-glass style UI across Android, iOS, desktop, and web. Read [SOUL.md](SOUL.md) for project intent and [STYLE.md](STYLE.md) for naming, voice, and visual language.

## Development Commands

### Build and Run
```bash
./gradlew build                                      # Full repository build
./gradlew check                                      # Aggregated verification tasks
./gradlew :composeApp:run                            # Desktop demo
./gradlew :composeApp:packageDistributionForCurrentOS # Package desktop app for the current OS
./gradlew :composeApp:assembleDebug                  # Android debug build
./gradlew :composeApp:wasmJsBrowserDevelopmentRun    # Web demo dev server
./gradlew :shared:assemble                           # Shared KMP module only
```

The first Gradle invocation can take a while because the wrapper and daemon bootstrap from a cold checkout.

## Architecture At A Glance

| Area | Location | Purpose |
|------|----------|---------|
| Public composables and demos | `composeApp/src/commonMain/kotlin/link/socket/krystal/` | `KrystalContainer`, `KrystalButton`, style primitives, demo content |
| Blur pipeline | `composeApp/src/commonMain/.../blur/` plus platform `blur/` sources | Shared blur contract with target-specific implementations and fallbacks |
| Curve pipeline | `composeApp/src/commonMain/.../curve/` and `composeApp/src/skikoMain/.../curve/` | Distortion, masking, and curved-glass rendering |
| Content capture and analysis | `composeApp/src/commonMain/.../engine/` | Infer background content and interaction context for glass surfaces |
| Platform entry points | `composeApp/src/androidMain/`, `composeApp/src/desktopMain/`, `composeApp/src/wasmJsMain/` | App launchers and target-specific rendering hooks |
| Shared non-UI code | `shared/src/commonMain/` and platform source sets | Platform detection and common KMP support code |
| iOS shell | `iosApp/` | Xcode host app embedding the shared framework |
| Public docs site | `docs/` | Lightweight landing page and project documentation |

## Before You Change Anything

- Read the composable or renderer you are touching, then read its call sites.
- If a change starts in `commonMain`, trace the corresponding platform behavior in `androidMain`, `desktopMain`, `wasmJsMain`, and `skikoMain` before you commit to an API shape.
- Treat the demo surfaces as diagnostic tools, not throwaway samples. If a rendering change affects them, update them deliberately.
- Check whether the behavior is part of the public material contract or just a platform implementation detail. Keep those layers separate.

## Rendering Rules

- Shared APIs belong in `commonMain`; platform source sets provide the rendering truth.
- Do not leak Android, Skia, or browser-specific types into shared APIs unless there is no portable alternative.
- Preserve graceful degradation. If a target cannot do the ideal blur or curve pass, fall back cleanly instead of forcing fake parity.
- Performance is part of the effect. Avoid extra allocations or expensive analysis inside hot layout and draw paths unless you have measured the cost.
- Keep blur, tint, edge treatment, and curve behavior perceptually consistent even when the implementation differs by platform.

## Kotlin And Compose Conventions

- Prefer `expect`/`actual` or source set splits for platform-specific behavior.
- Keep public API names descriptive: `Krystal*`, `Blur*`, `Curve*`, `Backdrop*`, and `*Style` should describe real behavior, not branding.
- Add comments only where the math, coordinate transforms, or fallback logic would otherwise be hard to reconstruct.
- No formatter or lint plugin is wired into this checkout yet. Match the surrounding style and keep imports and whitespace clean.

## Verification

- Docs-only changes do not require a Gradle run.
- For shared rendering changes, run the smallest relevant Gradle task at minimum; `./gradlew build` is the broadest safe default.
- If you touch blur, curve, or content-capture logic, also exercise the desktop demo and one additional target when practical.
- Call out visual regressions with target, screen, and interaction details. "Looks off" is not enough.

## Safety Boundaries

- Do not hardcode Apple-only assumptions into shared APIs.
- Do not add dependencies to `commonMain` unless they are genuinely multiplatform.
- Do not commit generated Xcode or IDE noise unless the change requires it.
- Keep `docs/` aligned with any major product or API shift.
- Prefer concrete, measured tradeoffs over vague fidelity claims.
