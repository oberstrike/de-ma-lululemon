# Frontend Style Guide

This document defines the visual design standards for the Media Server application. All UI components should follow these guidelines for consistency.

## Table of Contents

1. [Color Palette](#color-palette)
2. [Typography](#typography)
3. [Spacing](#spacing)
4. [Shadows](#shadows)
5. [Border Radius](#border-radius)
6. [Transitions](#transitions)
7. [Component Patterns](#component-patterns)
8. [Accessibility](#accessibility)
9. [Automatic Enforcement](#automatic-enforcement)

---

## Color Palette

All colors are defined as CSS custom properties in `styles.scss`. Use these variables instead of hardcoded values.

### Primary Colors

| Variable          | Value     | Usage                                    |
| ----------------- | --------- | ---------------------------------------- |
| `--primary`       | `#e50914` | Primary brand color, CTAs, active states |
| `--primary-hover` | `#f40612` | Hover state for primary elements         |
| `--primary-dark`  | `#b20710` | Active/pressed state, darker accents     |

### Background Colors

| Variable          | Value     | Usage                      |
| ----------------- | --------- | -------------------------- |
| `--bg-primary`    | `#0a0a0a` | Main app background        |
| `--bg-secondary`  | `#141414` | Page backgrounds, sections |
| `--bg-tertiary`   | `#1a1a1a` | Card backgrounds, inputs   |
| `--bg-elevated`   | `#232323` | Elevated surfaces, modals  |
| `--bg-card`       | `#181818` | Card backgrounds           |
| `--bg-card-hover` | `#252525` | Card hover state           |

### Text Colors

| Variable           | Value     | Usage                        |
| ------------------ | --------- | ---------------------------- |
| `--text-primary`   | `#fff`    | Primary text, headings       |
| `--text-secondary` | `#b3b3b3` | Secondary text, descriptions |
| `--text-tertiary`  | `#808080` | Tertiary text, placeholders  |
| `--text-muted`     | `#595959` | Muted/disabled text          |

### Border Colors

| Variable           | Value                    | Usage            |
| ------------------ | ------------------------ | ---------------- |
| `--border-subtle`  | `rgb(255 255 255 / 8%)`  | Subtle dividers  |
| `--border-default` | `rgb(255 255 255 / 15%)` | Standard borders |

### Usage Example

```scss
.card {
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  color: var(--text-primary);

  &:hover {
    background: var(--bg-card-hover);
    border-color: var(--border-default);
  }
}
```

---

## Typography

### Font Stack

```scss
font-family: 'Netflix Sans', 'Helvetica Neue', Helvetica, Arial, sans-serif;
```

### Font Sizes

| Size      | Usage            |
| --------- | ---------------- |
| `3.5rem`  | Hero titles      |
| `2.5rem`  | Page titles      |
| `1.5rem`  | Section headings |
| `1.4rem`  | Category titles  |
| `1.1rem`  | Large body text  |
| `1rem`    | Body text        |
| `0.95rem` | Secondary text   |
| `0.9rem`  | Small text       |
| `0.85rem` | Labels, captions |

### Font Weights

- `700` - Bold (headings)
- `600` - Semi-bold (subheadings, buttons)
- `500` - Medium (labels)
- `400` - Regular (body text)

### Line Heights

- `1.1` - Headings
- `1.2` - Subheadings
- `1.5` - Body text
- `1.6` - Long-form content
- `1.8` - Descriptions

---

## Spacing

Use the spacing scale for consistent margins and padding.

| Variable      | Value           | Usage            |
| ------------- | --------------- | ---------------- |
| `--space-xs`  | `0.25rem` (4px) | Tight spacing    |
| `--space-sm`  | `0.5rem` (8px)  | Small gaps       |
| `--space-md`  | `1rem` (16px)   | Standard spacing |
| `--space-lg`  | `1.5rem` (24px) | Large spacing    |
| `--space-xl`  | `2rem` (32px)   | Section spacing  |
| `--space-2xl` | `3rem` (48px)   | Page sections    |

### Usage Example

```scss
.section {
  padding: var(--space-xl) var(--space-2xl);
  margin-bottom: var(--space-lg);
}

.button-group {
  gap: var(--space-md);
}
```

---

## Shadows

| Variable        | Value                          | Usage               |
| --------------- | ------------------------------ | ------------------- |
| `--shadow-sm`   | `0 2px 8px rgb(0 0 0 / 40%)`   | Subtle elevation    |
| `--shadow-md`   | `0 4px 16px rgb(0 0 0 / 50%)`  | Cards, dropdowns    |
| `--shadow-lg`   | `0 8px 32px rgb(0 0 0 / 60%)`  | Modals, popovers    |
| `--shadow-glow` | `0 0 20px rgb(229 9 20 / 30%)` | Primary accent glow |

### Usage Example

```scss
.card {
  box-shadow: var(--shadow-md);

  &:hover {
    box-shadow: var(--shadow-lg);
  }
}

.featured {
  box-shadow: var(--shadow-glow);
}
```

---

## Border Radius

| Variable        | Value    | Usage                |
| --------------- | -------- | -------------------- |
| `--radius-sm`   | `4px`    | Small elements, tags |
| `--radius-md`   | `8px`    | Cards, buttons       |
| `--radius-lg`   | `12px`   | Large cards, posters |
| `--radius-xl`   | `16px`   | Modals               |
| `--radius-full` | `9999px` | Pills, circles       |

### Usage Example

```scss
.button {
  border-radius: var(--radius-md);
}

.avatar {
  border-radius: var(--radius-full);
}
```

---

## Transitions

| Variable               | Value        | Usage                |
| ---------------------- | ------------ | -------------------- |
| `--transition-fast`    | `150ms ease` | Micro-interactions   |
| `--transition-default` | `250ms ease` | Standard transitions |
| `--transition-slow`    | `400ms ease` | Page transitions     |

### Usage Example

```scss
.button {
  transition: all var(--transition-fast);

  &:hover {
    transform: scale(1.05);
  }
}

.card {
  transition: transform var(--transition-default);

  &:hover {
    transform: scale(1.35);
  }
}
```

---

## Component Patterns

### Cards

```scss
.card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition:
    transform var(--transition-default),
    box-shadow var(--transition-default);

  &:hover {
    transform: scale(1.05);
    box-shadow: var(--shadow-lg);
  }
}
```

### Buttons

```scss
.button-primary {
  background: var(--primary);
  color: var(--text-primary);
  border: none;
  border-radius: var(--radius-md);
  padding: 0.875rem var(--space-lg);
  font-weight: 500;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--primary-hover);
    transform: translateY(-2px);
  }
}
```

### Overlays

```scss
.overlay {
  background: linear-gradient(transparent, rgb(0 0 0 / 70%) 30%, rgb(0 0 0 / 95%));
  padding: var(--space-lg);
}
```

### Loading States

Use skeleton components instead of spinners for better UX:

```html
@if (loading()) {
<p-skeleton width="100%" height="200px" />
} @else {
<div class="content">...</div>
}
```

### Empty States

```scss
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8rem var(--space-xl);
  text-align: center;

  .icon {
    width: 120px;
    height: 120px;
    border-radius: var(--radius-full);
    background: var(--bg-tertiary);
  }
}
```

---

## Accessibility

### Focus States

All interactive elements must have visible focus states:

```scss
:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.button:focus-visible {
  box-shadow: 0 0 0 3px rgb(229 9 20 / 40%);
}
```

### ARIA Labels

Always provide ARIA labels for interactive elements:

```html
<button aria-label="Play video">
  <i class="pi pi-play"></i>
</button>

<div role="slider" aria-valuenow="50" aria-valuemin="0" aria-valuemax="100">...</div>
```

### Color Contrast

Ensure text has sufficient contrast:

- Primary text (`#fff`) on backgrounds should have 4.5:1 ratio
- Secondary text (`#b3b3b3`) is used only on dark backgrounds

---

## Automatic Enforcement

The style guide is enforced through linting:

### Stylelint

Configured in `.stylelintrc.json`:

- **Property ordering** - Properties are grouped logically
- **Color restrictions** - No named colors allowed
- **Duplicate properties** - Prevented
- **Shorthand** - Required where applicable

Run with:

```bash
npm run lint:styles
```

### ESLint

Custom rule `prefer-css-variables`:

- Warns when hardcoded colors are used in component styles
- Suggests appropriate CSS variable replacements

Run with:

```bash
npm run lint
```

### Pre-commit Hooks

Husky runs lint-staged on commit:

- Prettier formats files
- ESLint auto-fixes issues
- Stylelint fixes style issues

---

## Quick Reference

### Do's

- Use CSS variables for colors, spacing, shadows, radii
- Use skeleton loaders for loading states
- Add toast notifications for user actions
- Include ARIA labels on interactive elements
- Use transitions for state changes

### Don'ts

- Don't use hardcoded hex colors
- Don't use `px` for spacing (use `rem` via variables)
- Don't use named colors like `red`, `blue`
- Don't skip focus states
- Don't use spinners where skeletons work better
