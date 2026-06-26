You are a Senior Frontend Engineer and UI/UX Architect specializing in Angular applications.

Your mission is to completely redesign the visual identity of an existing Angular project management application (Jira-like) for IT/Dev teams, without changing the business logic or Angular architecture.

====================================================
DESIGN IDENTITY — BRUTALIST ADOUCI
====================================================

The interface must feel:

- Industrial, structured, technical
- Brutalist but readable — raw edges softened for productivity
- Enterprise-grade, not decorative

Design rules:

- Hard edges (border-radius: 0 to 2px max)
- Thick borders: 2–3px solid
- Flat offset shadows only (no blur, no glow)
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

## Design Style: Softened Brutalism

A "Softened Brutalism" aesthetic tailored for high-performance project management. It balances raw, industrial structural elements with a refined, deep-space dark palette to create a technical, developer-centric atmosphere.

The style is defined by rigid geometry, deliberate lack of organic curves, and high-contrast linework. It rejects soft shadows and gradients in favor of hard-edged 4px offset "neo-brutalist" shadows and 3px solid borders, evoking a sense of structural integrity and digital precision.

### Forbidden

- NO border-radius (except avatars: use `rounded-full`)
- NO gradients
- NO glassmorphism
- NO soft floating cards
- Visible structural grid at all times
- Strong typographic hierarchy

====================================================
DESIGN SYSTEM
====================================================

COLORS — Dark theme (default):

--color-primary : #25a7fd
--color-accent : #51d5fc
--color-bg : #0a0a0f
--color-surface : #111118
--color-border : #25a7fd
--color-text : #e8f4ff
--color-muted : #6a8fac
--color-success : #22c55e
--color-warning : #f59e0b
--color-danger : #ef4444

COLORS — Light theme:

--color-bg : #dff0ff
--color-surface : #ffffff
--color-border : #25a7fd
--color-text : #0a0a0f
--color-muted : #47657d
--color-primary : #0077cc

TYPOGRAPHY:

Google Fonts:

- Audiowide → H1, H2, app name, section titles (uppercase)
- JetBrains Mono → All UI text, buttons, labels, inputs, tables, badges

### Dark Theme (default — "Void")

```
surface                 : #0f1419
surface-dim             : #0f1419
surface-bright          : #353a3f
surface-container-lowest: #0a0f14
surface-container-low   : #171c21
surface-container       : #1b2025
surface-container-high  : #262a30
surface-container-highest: #30353b
on-surface              : #dfe3ea
on-surface-variant      : #bec7d3
background              : #0f1419
on-background           : #dfe3ea
surface-variant         : #30353b
outline                 : #89929d
outline-variant         : #3f4852
primary                 : #97cbff
on-primary              : #003353
primary-container       : #25a7fd
on-primary-container    : #003a5d
secondary               : #52d6fd
on-secondary            : #003543
secondary-container     : #01afd4
on-secondary-container  : #003d4c
tertiary                : #a5cbea
on-tertiary             : #05344d
tertiary-container      : #7fa4c2
on-tertiary-container   : #0f3a53
error                   : #ffb4ab
on-error                : #690005
error-container         : #93000a
on-error-container      : #ffdad6
inverse-surface         : #dfe3ea
inverse-on-surface      : #2c3136
inverse-primary         : #00639b
surface-tint            : #97cbff
primary-fixed           : #cee5ff
primary-fixed-dim       : #97cbff
on-primary-fixed        : #001d33
on-primary-fixed-variant: #004a76
secondary-fixed         : #b5ebff
secondary-fixed-dim     : #52d6fd
on-secondary-fixed      : #001f28
on-secondary-fixed-variant: #004e60
tertiary-fixed          : #cae6ff
tertiary-fixed-dim      : #a5cbea
on-tertiary-fixed       : #001e2f
on-tertiary-fixed-variant: #234a64
```

Type scale:
--text-xs : 0.75rem
--text-sm : 0.875rem
--text-base : 1rem
--text-lg : 1.25rem
--text-xl : 1.5rem
--text-2xl : 2rem
--text-4xl : 3.5rem

SPACING: 8px base unit system

GRID: 12 columns desktop / 6 tablet / 4 mobile

====================================================
ANGULAR-SPECIFIC REQUIREMENTS
====================================================

- Use Angular component architecture (no migration)
- Apply design via SCSS with CSS custom properties
- Create a global design-system.scss file with all variables
- Use Angular Material components where already present,
  override their styles to match the brutalist system
- Each component gets its own .scss file
- Theme switching via [data-theme] attribute on <html>
- i18n-ready: use Angular i18n or ngx-translate
  for all UI text (FR / EN minimum)
- All icons: Material Symbols Outlined (Google Fonts)
  with stroke-only style
- `font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24`
- Outline only — no filled icons (exceptions: `check_circle`, `fiber_manual_record` for status indicators may use `'FILL' 1`)

====================================================
APPLICATION LAYOUT
====================================================

```html
<html class="dark">
  <!-- default — dark only for now -->
</html>
```

Dark is always default. No light theme until specified.

GLOBAL TOP BAR (64px, sticky):

- Logo (Audiowide font, uppercase)
- Workspace switcher
- Global search (keyboard shortcut: /)
- [+ CREATE] button (primary style)
- Notifications bell
- AI Assistant button
- Theme toggle (dark/light)
- Language switcher (FR/EN)
- User avatar + dropdown

COLLAPSIBLE LEFT SIDEBAR:

- Collapses to icon-only mode
- border-right: 3px solid var(--color-border)
- Sections: Home, Projects, My Work, Favorites,
  Dashboards, Teams, Calendar, Reports, Settings
- Active item: background var(--color-primary),
  color var(--color-bg)
- Bottom: Storage indicator, Upgrade, Profile

```html
<link
  href="https://fonts.googleapis.com/css2?family=Audiowide&family=JetBrains+Mono:wght@400;700&family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
  rel="stylesheet"
/>
```

PROJECT HEADER:

- Project icon + name (Audiowide)
- Breadcrumb navigation
- Visibility badge
- Members avatars
- Sprint selector dropdown
- Filter bar
- Search
- [+ CREATE ISSUE] button

PROJECT TABS (single active):
Overview | Board | Backlog | Timeline |
Calendar | Epics | Releases | Reports |
Issues | Components | Files | Automation | Settings

Typography usage:

- H1 / H2 only
- Uppercase always
- Used with restraint — branding, page headers, major section titles
- Navigation, buttons, inputs, labels, body text, code, tables, captions, all UI

### Type Scale (exact px values)

```css
--display-lg: 48px / 1.1 / 400 / 0.05em /* Audiowide — hero headings */ --headline-lg: 32px / 1.2 / 400 / 0.04em
  /* Audiowide — section headers */ --headline-md: 24px / 1.2 / 400 / 0.04em /* Audiowide — card titles */ --headline-lg-mobile: 24px /
  1.2 / 400 /* Audiowide — mobile fallback */ --body-lg: 18px / 1.6 / 400 / 0em /* JetBrains Mono */ --body-md: 16px / 1.5 / 400 / 0em
  /* JetBrains Mono — default */ --body-sm: 14px / 1.5 / 400 / 0em /* JetBrains Mono */ --label-md: 12px / 1 / 700 / 0.1em
  /* JetBrains Mono — labels, buttons, badges */;
```

### Spacing

```css
--unit: 4px /* baseline grid unit */ --gutter: 24px /* column gutters */ --margin-page: 32px /* page padding */ --stack-sm: 8px
  --stack-md: 16px --stack-lg: 32px;
```

====================================================
PAGES TO REDESIGN
====================================================

1. BOARD (Kanban)
   Columns: Backlog / Selected / In Progress /
   Review / Testing / Done

Each card shows:

- Issue key (monospace badge)
- Title
- Priority icon (colored)
- Labels (flat badges, no radius)
- Story points
- Assignee avatar
- Due date
- Comments + attachments count
- Subtask progress bar

Column header: thick top border colored by status
Drag & drop preserved (CDK Drag Drop)
Card hover: translate(-2px, -2px) + offset shadow

2. BACKLOG
   Left: Epics panel (collapsible)
   Center: Sprint sections + issue list
   Bottom: inline create issue

3. ISSUE DETAIL (Right Drawer)
   Full fields: Title, Description, Status, Priority,
   Type, Assignee, Reporter, Sprint, Epic,
   Story Points, Attachments, Activity,
   Comments, History, Checklist,
   Linked Issues, Time Tracking

4. DASHBOARD
   Draggable widget grid:
   - Assigned to me
   - Sprint progress
   - Velocity chart
   - Burndown chart
   - Recent activity
   - Upcoming deadlines
   - Workload
   - Pie/Bar charts

```css
:root {
  --color-primary: #97cbff;
  --color-primary-container: #25a7fd;
  --color-secondary: #52d6fd;
  --color-secondary-container: #01afd4;
  --color-tertiary: #a5cbea;
  --color-bg: #0f1419;
  --color-surface: #0f1419;
  --color-surface-container: #1b2025;
  --color-surface-container-low: #171c21;
  --color-surface-container-high: #262a30;
  --color-surface-container-highest: #30353b;
  --color-surface-container-lowest: #0a0f14;
  --color-surface-variant: #30353b;
  --color-outline: #89929d;
  --color-outline-variant: #3f4852;
  --color-on-surface: #dfe3ea;
  --color-on-surface-variant: #bec7d3;
  --color-error: #ffb4ab;
  --color-error-container: #93000a;
  --color-on-error: #690005;
  --color-text: #dfe3ea;
  --color-muted: #6a8fac;
  --border-width: 3px;
  --shadow-brutal: 4px 4px 0px var(--color-primary);
  --radius: 0;
}
```

5. TIMELINE
   Horizontal Gantt-style roadmap
   Zoom: Week / Month / Quarter / Year
   Dependencies, Milestones, Drag bars

6. REPORTS
   Charts: Velocity, Burndown, Burnup,
   Cycle Time, Lead Time, Cumulative Flow,
   Issue Distribution

7. SETTINGS
   Sections: General, Members, Roles,
   Permissions, Workflows, Issue Types,
   Labels, Priorities, Automation,
   Notifications, Integrations, API

====================================================
COMPONENT LIBRARY (SCSS overrides)
====================================================

Every component must follow brutalist rules:

### Buttons

```css
border: 3px solid var(--color-primary-container);
border-radius: 0;
font-family: var(--font-mono);
font-weight: 700;
text-transform: uppercase;
padding: 8px 24px;
box-shadow: 4px 4px 0 var(--color-primary);

/* hover */
transform: translate(-2px, -2px);
box-shadow: 6px 6px 0 var(--color-primary);

/* active */
transform: translate(2px, 2px);
box-shadow: 2px 2px 0 var(--color-primary);

/* disabled */
opacity: 0.4;
pointer-events: none;
```

### Input / Select

```css
background: var(--color-surface);
border: 3px solid var(--color-primary);
border-radius: 0;
font-family: var(--font-mono);
color: var(--color-on-surface);

/* focus */
border-color: var(--color-secondary);
outline: none;
```

### Cards

```css
background: var(--color-surface-container);
border: 3px solid var(--color-primary);
border-radius: 0;
box-shadow: var(--shadow-brutal);
```

### Badge / Label

```css
border: 2px solid currentColor;
border-radius: 0;
font-family: var(--font-mono);
font-size: var(--text-xs);
text-transform: uppercase;
```

### Table

- All borders 2px solid var(--color-border)
- No zebra stripe
- Monospace font
- Exposed grid
- Rows separated by 2px solid lines (border-bottom)
- Active row: full primary background with on-primary text

### Modal / Drawer

```css
border: 3px solid var(--color-border);
border-radius: 0;
box-shadow: 8px 8px 0 var(--color-primary);
```

### Dividers

- 3px solid borders
- May use skew() transforms
- Must reinforce structural rhythm

### Navigation (Top Bar)

- Full width, sticky top
- border-bottom: 3px solid var(--color-primary-container)
- JetBrains Mono, uppercase
- Active item: background var(--color-primary) / color var(--color-bg)

### Sidebar

- Fixed left, full height below top bar
- 256px width (collapsible to 80px icon-only)
- border-right: 3px solid var(--color-primary)
- Navigation items: icon + label, font-label-md, uppercase
- Active item: background var(--color-primary) / color var(--color-bg)
- Collapse button: positioned at right edge, right: -16px

### Grid Layout

- Desktop : 12-column fluid grid
- Tablet : 6-column grid
- Mobile : single-column stack

Hard left-alignment for all text and UI blocks to emphasize the vertical axis of the grid.

### Elevation & Depth

Depth is communicated through **Hard Offsets** rather than light and shadow.

1. **Flat Layer** (z-0): No shadow, sits directly on background.
2. **Elevated Layer**: Interactive elements use 4px horizontal + 4px vertical offset shadow. Shadow color is always solid (no blur), matches primary (#97cbff).
3. **Active State**: On click/press, elements shift 2px down + 2px right, shadow reduces to 2px offset — simulates physical "push" into surface.

### Kanban Board Columns

- Column header: colored top border (6px thick), per status:
  - Backlog: #25a7fd (primary-container)
  - Selected: #fbbf24 (amber)
  - In Progress: #f97316 (orange)
  - Review: #a855f7 (purple)
  - Testing: #06b6d4 (cyan)
  - Done: #22c55e (green)
- Column badge: border: 2px solid, count number
- Cards: draggable, brutalist-card styling
- Scrollable column body, hidden scrollbar

### Kanban Card

```css
background: var(--color-surface);
border: 3px solid var(--color-primary-container);
box-shadow: 4px 4px 0 var(--color-primary);
padding: 16px;

/* hover card */
transform: translate(-2px, -2px);
box-shadow: 6px 6px 0 var(--color-primary);
```

- Contains: ID badge, title, progress bar, avatars, metadata (comments, date, story points)
- Issue type indicator: colored dot (error=bug, secondary=task, etc.)

### Progress Bars

- Square ends (no border-radius)
- Segmented "blocks" style (not continuous fill)
- Height: 4px
- Track: background var(--color-surface-container)
- Fill: colored per context (primary/column color)
- Label: n/m or percentage

### Chips & Tags

- Small scale, no shadow
- border: 1px solid or 2px solid
- Text: --label-md (12px, bold, uppercase)
- Used for: status badges, priority labels, SP (story points)

### Avatars

- width: 32px, height: 32px
- border-radius: 9999px (EXCEPTION to the no-radius rule — avatars only)
- border: 2px solid (color matches context)
- object-fit: cover
- Avatar stacks: use negative margin for overlap

### Bottom Navigation (Mobile)

- Fixed bottom, full width
- border-top: 3px solid var(--color-primary-container)
- 4-5 items with icon + label
- Center "CREATE" button elevated above bar

### Focus state (all interactive)

```css
:focus-visible {
  outline: 3px solid var(--color-secondary);
  outline-offset: 2px;
}
```

====================================================
MOTION
====================================================

Max duration: 150ms
Allowed: hover displacement, focus transitions,
drawer slide-in, tab switch
Forbidden: bounce, float, blur fade, long transitions

```css
@media (prefers-reduced-motion: reduce) {
  * {
    transition: none !important;
    animation: none !important;
  }
}
```

### Optional Atmospheric Effect: Scanline

A subtle horizontal scanline overlay for the home/dashboard screen:

```css
@keyframes scanline {
  0% {
    transform: translateY(-100%);
  }
  100% {
    transform: translateY(100%);
  }
}
.scanline {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: rgba(151, 203, 255, 0.03);
  z-index: 100;
  pointer-events: none;
  animation: scanline 8s linear infinite;
}
```

====================================================
ACCESSIBILITY
====================================================

- WCAG AA minimum
- All interactive elements keyboard accessible
- Visible focus states mandatory
- ARIA labels on icons and icon-only buttons
- Sufficient color contrast in both themes
- Screen reader friendly structure

====================================================
RESPONSIVE
====================================================

Desktop (1280px+) : Full sidebar + content
Laptop (1024px) : Sidebar collapsible by default
Tablet (768px) : Sidebar overlay
Mobile (375px+) : Sidebar hidden, bottom nav

====================================================
DELIVERABLES
====================================================

1. global design-system.scss (all variables + base)
2. Redesigned SCSS for each existing component
3. Updated HTML templates (Angular syntax preserved)
4. Theme toggle service (dark/light)
5. i18n translation files (fr.json + en.json)
6. Component library documentation (inline comments)

====================================================
CONSTRAINTS
====================================================

- Do NOT touch TypeScript logic or services
- Do NOT change routing or module structure
- Do NOT remove Angular Material — override it
- Keep all existing [routerLink], (click), *ngFor, *ngIf
- Production-ready code only
- No placeholders, no omitted styles
- Every component follows the softened brutalist system
- Final result must feel industrial, structural, technical, and aggressively brutalist
