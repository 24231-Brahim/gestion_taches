---
name: Cyber-Industrial Proto
colors:
  surface: '#0f1419'
  surface-dim: '#0f1419'
  surface-bright: '#353a3f'
  surface-container-lowest: '#0a0f14'
  surface-container-low: '#171c21'
  surface-container: '#1b2025'
  surface-container-high: '#262a30'
  surface-container-highest: '#30353b'
  on-surface: '#dfe3ea'
  on-surface-variant: '#bec7d3'
  inverse-surface: '#dfe3ea'
  inverse-on-surface: '#2c3136'
  outline: '#89929d'
  outline-variant: '#3f4852'
  surface-tint: '#97cbff'
  primary: '#97cbff'
  on-primary: '#003353'
  primary-container: '#25a7fd'
  on-primary-container: '#003a5d'
  inverse-primary: '#00639b'
  secondary: '#52d6fd'
  on-secondary: '#003543'
  secondary-container: '#01afd4'
  on-secondary-container: '#003d4c'
  tertiary: '#a5cbea'
  on-tertiary: '#05344d'
  tertiary-container: '#7fa4c2'
  on-tertiary-container: '#0f3a53'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#cee5ff'
  primary-fixed-dim: '#97cbff'
  on-primary-fixed: '#001d33'
  on-primary-fixed-variant: '#004a76'
  secondary-fixed: '#b5ebff'
  secondary-fixed-dim: '#52d6fd'
  on-secondary-fixed: '#001f28'
  on-secondary-fixed-variant: '#004e60'
  tertiary-fixed: '#cae6ff'
  tertiary-fixed-dim: '#a5cbea'
  on-tertiary-fixed: '#001e2f'
  on-tertiary-fixed-variant: '#234a64'
  background: '#0f1419'
  on-background: '#dfe3ea'
  surface-variant: '#30353b'
typography:
  display-lg:
    fontFamily: Audiowide
    fontSize: 48px
    fontWeight: '400'
    lineHeight: '1.1'
    letterSpacing: 0.05em
  headline-lg:
    fontFamily: Audiowide
    fontSize: 32px
    fontWeight: '400'
    lineHeight: '1.2'
    letterSpacing: 0.04em
  headline-md:
    fontFamily: Audiowide
    fontSize: 24px
    fontWeight: '400'
    lineHeight: '1.2'
    letterSpacing: 0.04em
  body-lg:
    fontFamily: JetBrains Mono
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
    letterSpacing: 0em
  body-md:
    fontFamily: JetBrains Mono
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
    letterSpacing: 0em
  body-sm:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
    letterSpacing: 0em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '700'
    lineHeight: '1'
    letterSpacing: 0.1em
  headline-lg-mobile:
    fontFamily: Audiowide
    fontSize: 24px
    fontWeight: '400'
    lineHeight: '1.2'
spacing:
  unit: 4px
  gutter: 24px
  margin-page: 32px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style
This design system utilizes a "Softened Brutalism" aesthetic tailored for high-performance project management. It balances raw, industrial structural elements with a refined, deep-space dark palette to create a technical, developer-centric atmosphere.

The style is defined by rigid geometry, deliberate lack of organic curves, and high-contrast linework. It rejects soft shadows and gradients in favor of hard-edged 4px offset "neo-brutalist" shadows and 3px solid borders, evoking a sense of structural integrity and digital precision. The emotional response is one of focus, reliability, and technical competence.

## Colors
The palette is rooted in a deep "Void" background to minimize eye strain during long sessions. The primary and secondary blues act as "active energy" indicators, used for borders and interactive states to guide the eye through the technical interface.

- **Background**: Use for the lowest level of the UI.
- **Surface**: Use for cards, containers, and sidebars to create subtle depth.
- **Primary**: Use for critical borders, primary buttons, and active status indicators.
- **Secondary**: Use for accents, progress bars, and highlighting secondary information.
- **Muted**: Use for metadata, inactive states, and secondary icons.
- **Text**: High-legibility cool white to ensure maximum contrast against dark surfaces.

## Typography
The system employs a dual-font strategy. **Audiowide** is reserved for branding, page headers, and major section titles, always set in uppercase to reinforce the industrial, futuristic tone. 

**JetBrains Mono** is the workhorse font for all UI elements, data tables, and body text. Its monospaced nature ensures that columns of data align perfectly, fitting the "technical tool" narrative. All labels and buttons should use the medium weight for increased legibility against the dark background.

## Layout & Spacing
The layout follows a strict 4px baseline grid. Content should be organized within a 12-column fluid grid for desktop, transitioning to a single-column stack for mobile.

- **Gutters**: Fixed at 24px to maintain structural separation.
- **Margins**: 32px page padding to allow the brutalist elements "room to breathe" despite their heavy borders.
- **Alignment**: Hard left-alignment for all text and UI blocks to emphasize the vertical axis of the grid.

## Elevation & Depth
In this design system, depth is communicated through **Hard Offsets** rather than light and shadow. 

1. **Flat Layer**: Items at `z-0` have no shadow and sit directly on the background.
2. **Elevated Layer**: Interactive elements (buttons, cards) use a 4px horizontal and 4px vertical offset shadow. The shadow color is always solid (no blur) and matches the `primary_color_hex` or a darker variant of the background depending on the element's priority.
3. **Active State**: On click/press, elements shift 2px down and 2px right, and the shadow size reduces to 2px, simulating a physical "push" into the surface.

## Shapes
Geometry is strictly rectilinear. All corners are 90 degrees (0px radius). This applies to buttons, input fields, cards, tooltips, and even decorative accents. The "softness" of the brutalism comes from the deliberate use of color and refined typography, not from rounded corners.

## Components

### Buttons
- **Primary**: 3px solid border (`Primary`), 4px flat offset shadow. Text in `JetBrains Mono` bold, uppercase.
- **Interaction**: On hover, the shadow color changes to `Secondary`. On click, the element translates +2px, +2px.

### Input Fields
- **Container**: 3px solid border (`Primary` or `Muted`), background is `Surface`.
- **Focus**: Border color switches to `Secondary` with a persistent 2px internal inset glow.

### Cards
- **Structure**: 3px solid border (`Primary`). 4px offset shadow in `Primary` (low opacity).
- **Header**: Separated from content by a 2px horizontal rule.

### Chips & Tags
- **Small Scale**: No shadow. 1px solid border. Text `label-md` for high information density.

### Lists & Tables
- **Rows**: Separated by 2px solid lines. No zebra-striping; use border-bottom only to maintain the grid look.
- **Active Row**: Uses a full `Primary` color background with `Background` color text.

### Progress Bars
- **Style**: Square ends. Segmented "blocks" rather than a continuous smooth fill to reinforce the technical, industrial mood.