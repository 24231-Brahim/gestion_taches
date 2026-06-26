# FRONTEND DESIGN SYSTEM SPECIFICATION

Build a production-ready frontend UI following this design system exactly.

---

## Framework & Technical Requirements

- Use semantic HTML5
- Use CSS variables for all theme values
- No inline styles
- Mobile-first responsive design
- Responsive down to 375px width
- Respect `prefers-reduced-motion`
- WCAG AA accessibility compliance
- All interactive elements must be keyboard accessible
- Visible focus states are mandatory

---

## Design Style: Brutalism

Raw, unapologetic, structural. The interface should feel engineered rather than decorated. Expose the grid. Make structure visible. Elements should appear bolted together, not floating.

### Forbidden

- NO border-radius
- NO gradients
- NO glassmorphism / neumorphism / material design patterns
- NO blurred shadows
- NO floating cards
- NO soft effects
- NO decorative curves

### Required

- Hard edges everywhere
- Thick borders (3–5px solid)
- Flat offset shadows only (no blur)
- Visible structural divisions
- Strong visual hierarchy
- Intentional asymmetrical spacing
- Exposed grid system

---

## Color Palette

Only use colors from this palette. Never introduce outside colors.

### Dark Theme (default — "somber")

```
Primary Blue : #25a7fd
Accent Cyan  : #51d5fc
Background   : #0a0a0f
Surface      : #111118
Border       : #25a7fd
Text Primary : #e8f4ff
Text Muted   : #6a8fac
```

### Light Theme ("claire")

```
Background   : #dff0ff
Surface      : #ffffff
Border       : #25a7fd
Text Primary : #0a0a0f
Text Muted   : #47657d
```

---

## Themes

```html
<html data-theme="dark">
  <!-- default -->
  <html data-theme="light"></html>
</html>
```

Theme switching updates all CSS variables. Dark is always default.

---

## Typography

```html
<link href="https://fonts.googleapis.com/css2?family=Audiowide&family=JetBrains+Mono:wght@300;400;500;700&display=swap" rel="stylesheet" />
```

### Display — Audiowide

- H1 / H2 only
- Uppercase preferred
- Letter-spacing: -0.02em
- Used with restraint

### Body / UI — JetBrains Mono

- Navigation, buttons, inputs, labels, body text, code, tables, captions

### Type Scale

```css
--text-xs: 0.75rem;
--text-sm: 0.875rem;
--text-base: 1rem;
--text-lg: 1.25rem;
--text-xl: 1.5rem;
--text-2xl: 2rem;
--text-4xl: 3.5rem;
```

---

## CSS Variables

```css
:root {
  --color-primary: #25a7fd;
  --color-accent: #51d5fc;
  --color-bg: #0a0a0f;
  --color-surface: #111118;
  --color-border: #25a7fd;
  --color-text: #e8f4ff;
  --color-muted: #6a8fac;

  --font-display: 'Audiowide', sans-serif;
  --font-mono: 'JetBrains Mono', monospace;

  --border-width: 3px;
  --shadow-brutal: 4px 4px 0px var(--color-primary);
  --radius: 0;
}

[data-theme='light'] {
  --color-bg: #dff0ff;
  --color-surface: #ffffff;
  --color-border: #25a7fd;
  --color-text: #0a0a0f;
  --color-muted: #47657d;
  --color-primary: #0077cc;
  --color-accent: #0099e6;
}
```

---

## Layout System

CSS Grid as primary layout mechanism.

- Desktop : 12-column grid
- Tablet : 6-column grid
- Mobile : 4-column grid

Grid structure must be visually expressed through borders and alignment.

---

## Components

### Navigation

- Full width
- `border-bottom: 3px solid var(--color-primary)`
- JetBrains Mono, uppercase
- Active item: `background: var(--color-primary)` / `color: var(--color-bg)`

### Buttons

```css
border: 3px solid var(--color-primary);
border-radius: 0;
font-family: var(--font-mono);
text-transform: uppercase;

/* hover */
transform: translate(-2px, -2px);
box-shadow: 4px 4px 0 var(--color-accent);

/* active */
transform: translate(0, 0);
box-shadow: none;

/* disabled */
opacity: 0.4;
pointer-events: none;
```

### Cards

```css
background: var(--color-surface);
border: 3px solid var(--color-border);
border-radius: 0;
box-shadow: var(--shadow-brutal);
```

### Inputs

```css
background: transparent;
border: 3px solid var(--color-border);
border-radius: 0;
font-family: var(--font-mono);
color: var(--color-text);
```

### Tables

- Thick borders, monospace typography
- No zebra striping, no rounded corners
- Strong exposed grid appearance

### Dividers

- 3px solid borders
- May use `skew()` transforms
- Must reinforce structural rhythm

---

## Icons

- Lucide Icons only
- Stroke-width: 2.5px
- Outline only — no filled icons

---

## Motion

- Max duration: 150ms
- Allowed: hover displacement, focus transitions
- Forbidden: bounce, float, soft fades, long page transitions

```css
@media (prefers-reduced-motion: reduce) {
  * {
    transition: none !important;
    animation: none !important;
  }
}
```

---

## Signature Brutalist Element

At least one hero section must contain:

- A massive Audiowide heading partially escaping its container
- Overlapping bordered boxes with intentional clipping
- Structural tension between typography and layout — text that breaks the grid

---

## Accessibility

```css
:focus-visible {
  outline: 3px solid var(--color-accent);
  outline-offset: 2px;
}
```

---

## Output Requirements

- Production-ready code only
- No placeholders, no omitted styles
- Every component follows the brutalist system
- Final result must feel industrial, structural, technical, and aggressively brutalist
