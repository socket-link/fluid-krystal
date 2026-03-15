# STYLE.md

Style guide for Fluid Krystal documentation, demo copy, naming, comments, and public-facing descriptions.

## Voice

**A rendering engineer with product taste.** Precise, visual, skeptical of hype. Makes the reader see the effect, then explains the mechanism. Direct without being sterile. Opinionated without overselling.

## Tone By Context

| Context | Tone | Example |
|--------|------|---------|
| README / hero | Visual and concrete | *"Translucent surfaces that react to the content beneath them, not static frosted rectangles."* |
| Technical docs | Exact and pragmatic | *"Curve passes are optional at the API level and target-dependent at runtime."* |
| Code comments | Sparse and explanatory | *"Compensate for scroll offset so backdrop analysis stays aligned with window coordinates."* |
| Error messages | Direct and useful | *"Native blur unavailable on this target; falling back to software blur."* |
| Demo labels | Short and descriptive | *"Dark Music"*, *"Simple List"*, *"Surface Stress Test"* |

## Language Principles

- Describe both perception and mechanism when it matters.
- Prefer short declarative sentences over soft marketing copy.
- Use physical words for physical behavior: blur, tint, backdrop, depth, curve, edge light, distortion, noise, contrast.
- State tradeoffs directly. If something is approximate, expensive, or platform-limited, say so.
- Use `liquid glass` for the general material. Use `Apple's Liquid Glass` only when explicitly comparing against Apple's implementation.
- Never promise pixel-perfect parity across every KMP target.

## Vocabulary

### Prefer

| Use | Instead of |
|-----|------------|
| glass surface | frosted card |
| backdrop | background layer |
| blur pass | blur magic |
| distortion / curve | 3D effect |
| tint | color overlay |
| edge highlight | glossy border |
| depth | premium feel |
| perceptual consistency | exact match |
| degrade gracefully | best effort |
| rendering path | pipeline magic |

### Avoid

| Word | Why |
|------|-----|
| revolutionary / game-changing | Hype with no signal |
| seamless / frictionless | Overused and vague |
| delightful | Says nothing about behavior |
| glassmorphism | Too broad for what this project is doing |
| identical | Usually false across platforms |
| Apple clone | Undersells the real engineering problem |
| shader sauce | Cute, but meaningless |
| next-gen | Empty marketing filler |

## Naming New Concepts

When naming a new type, modifier, or renderer, ask in order:

1. What visible behavior does this control?
2. What mechanism produces that behavior?
3. Will a Compose developer understand where it belongs from the name alone?

Prefer these families:

- `Krystal*` for public-facing material primitives and top-level API.
- `Blur*` for blur strategies, engines, and helpers.
- `Curve*` for distortion, masks, and related render stages.
- `*Style` for parameter bundles that shape a surface.
- `*Debug*` and `*Demo*` for inspection and showcase code.

Avoid decorative names that do not explain function.

## Visual Language

- Show content behind the glass. Flat empty backgrounds hide whether the effect works.
- Keep light and dark demos in parity. The material should survive both.
- Corners can be generous, but borders and highlights should stay restrained.
- Motion should feel suspended and responsive, not springy or playful for its own sake.
- Use real content in demos: lists, album art, gradients, typography, controls. Empty placeholder blocks do not prove the material.

## Comment And Copy Rules

- Comments should explain thresholds, coordinate math, fallback decisions, and cross-platform caveats.
- Do not narrate obvious Compose code.
- Prefer screenshots, recordings, or exact reproduction notes over adjectives when documenting visual issues.
- When comparing with Apple behavior, describe the specific trait being matched or missed instead of invoking the brand as a shortcut.
