# Custom ESLint Rules

This directory contains custom ESLint rules specific to this project.

## Available Rules

### `create-effect-in-service`

Enforces that NgRx `createEffect` calls are only used in appropriate contexts.

**Rule Type:** `problem`

**Severity:** `error`

#### What it does

This rule ensures that `createEffect` from `@ngrx/effects` is only called:
- Inside a class decorated with `@Injectable()`
- As a class property initializer (not inside methods or lifecycle hooks)
- Not at the top level (enforces class-based effects over functional effects)

#### Why?

While NgRx supports both class-based and functional effects, this rule enforces the traditional pattern of defining effects in `@Injectable()` service classes. This provides several benefits:

1. **Consistency**: All effects follow the same pattern
2. **Testability**: Injectable services are easier to test
3. **Dependency Injection**: Leverages Angular's DI system
4. **Best Practices**: Follows Angular style guide recommendations

#### Examples

##### ✅ Valid Usage

```typescript
import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';

@Injectable()
export class MovieEffects {
  // ✅ Valid: createEffect as a class property in an @Injectable service
  loadMovies$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MovieActions.load),
      // ...
    )
  );

  constructor(private actions$: Actions) {}
}
```

##### ❌ Invalid Usage

**1. createEffect in a Component**

```typescript
@Component({
  selector: 'app-movie',
  template: '...'
})
export class MovieComponent {
  // ❌ Error: createEffectInNonInjectableClass
  loadMovies$ = createEffect(() => ...);
}
```

**2. createEffect inside a method**

```typescript
@Injectable()
export class MovieEffects {
  ngOnInit() {
    // ❌ Error: createEffectInMethod
    this.loadMovies$ = createEffect(() => ...);
  }
}
```

**3. createEffect outside a class (functional effect)**

```typescript
// ❌ Error: createEffectOutsideClass
export const loadMovies$ = createEffect(() => ..., { functional: true });
```

#### Error Messages

- `createEffectOutsideClass`: createEffect must be defined inside a class. Consider creating an @Injectable effects service.
- `createEffectInNonInjectableClass`: createEffect should only be used in @Injectable() classes. This class is not decorated with @Injectable.
- `createEffectInMethod`: createEffect should be a class property initializer, not called inside a method or function.

#### Configuration

This rule has no configuration options. It's enabled by default in the ESLint configuration:

```javascript
rules: {
  'local-rules/create-effect-in-service': 'error',
}
```

#### When to disable

If your project adopts functional effects (available in NgRx v14+), you may want to disable this rule:

```javascript
rules: {
  'local-rules/create-effect-in-service': 'off',
}
```

Or disable it for specific files using ESLint comments:

```typescript
/* eslint-disable local-rules/create-effect-in-service */
export const loadMovies$ = createEffect(() => ..., { functional: true });
```

## Testing

The custom rules are tested using ESLint's built-in `RuleTester`. Tests ensure the rules correctly identify violations and allow valid usage patterns.

### Running Tests

```bash
# Run all ESLint rule tests
npm run test:eslint-rules

# Or run directly
node eslint-local-rules/rules/create-effect-in-service.test.js
```

### Test Coverage

#### `create-effect-in-service` rule tests:

**Valid usage (5 tests):**
- ✅ createEffect as property in @Injectable class
- ✅ createEffect with @Injectable decorator (with/without parentheses)
- ✅ createEffect in class expressions with @Injectable
- ✅ Multiple effects in same class
- ✅ createEffect with arrow function properties

**Invalid usage (9 tests):**
- ❌ createEffect in @Component classes
- ❌ createEffect in classes without decorators
- ❌ createEffect outside any class (functional effects)
- ❌ createEffect called in methods (ngOnInit, constructor, custom methods)
- ❌ createEffect in class expressions without @Injectable
- ❌ createEffect with member expression syntax

### Writing Tests for New Rules

When adding a new rule, create a corresponding test file:

```javascript
const { RuleTester } = require('eslint');
const tsParser = require('@typescript-eslint/parser');
const rule = require('./your-rule-name');

const ruleTester = new RuleTester({
  languageOptions: {
    parser: tsParser,
    parserOptions: {
      ecmaVersion: 2020,
      sourceType: 'module',
    },
  },
});

ruleTester.run('your-rule-name', rule, {
  valid: [/* valid test cases */],
  invalid: [/* invalid test cases with expected errors */],
});
```

## Adding New Custom Rules

To add a new custom rule:

1. Create a new file in `rules/your-rule-name.js`
2. Export a rule module following ESLint's rule API
3. Add it to `index.js`:

```javascript
module.exports = {
  rules: {
    'create-effect-in-service': require('./rules/create-effect-in-service'),
    'your-rule-name': require('./rules/your-rule-name'),
  },
};
```

4. Enable it in `eslint.config.js`:

```javascript
rules: {
  'local-rules/your-rule-name': 'error',
}
```

## Detecting Unused Code

### Why Not an ESLint Rule?

We previously attempted to create an ESLint rule to detect unused Signal Store methods, but **ESLint cannot perform cross-file analysis**. ESLint processes one file at a time, meaning it cannot detect if a method defined in `store.ts` is used in `component.ts`. This results in false positives where actually-used methods are flagged as unused.

### Recommended Tool: Knip

For detecting unused exports, methods, files, and dependencies across your entire project, use [**Knip**](https://knip.dev/).

#### What is Knip?

Knip finds unused files, dependencies, and exports in JavaScript and TypeScript projects using project-wide analysis. Unlike ESLint, Knip can detect:

- ✅ Unused exports (functions, classes, types)
- ✅ Unused files
- ✅ Unused dependencies and devDependencies
- ✅ Unlisted dependencies
- ✅ Unused class members and enum members
- ✅ Duplicate exports

#### ⚠️ Knip Limitations with Signal Store Methods

**Important**: While Knip can detect unused class members in traditional Angular components and services, it **cannot detect unused methods within Signal Store `withMethods()` blocks**.

**Why?** Methods in `withMethods()` are defined as properties in an object literal returned by a function:

```typescript
withMethods((store) => ({
  usedMethod() { },      // Knip cannot tell if this is used
  unusedMethod() { },    // Knip cannot detect this is unused
}))
```

**Testing Results:**
- ✅ **Detects**: Unused class members in Components, Services, Directives
- ❌ **Cannot detect**: Unused methods in Signal Store `withMethods()` blocks
- ✅ **Detects**: Entire store file if unused
- ✅ **Detects**: Exported store if never imported

**Workaround**: Manually review Signal Store methods or use TypeScript's `noUnusedLocals` compiler option (though it also has limitations with object methods).

**Bottom Line**: For Signal Store methods specifically, there is currently **no automated tool** that can reliably detect unused methods. Manual code review and testing remain the best approach.

#### Installation

Knip works out of the box without installation:

```bash
npx knip
```

Or install it as a dev dependency:

```bash
npm install -D knip
```

#### Usage

```bash
# Analyze entire project
npx knip

# Only check exports (including unused Signal Store methods)
npx knip --exports

# Only check dependencies
npx knip --dependencies

# Only check unused files
npx knip --files

# Auto-fix issues (use with caution)
npx knip --fix

# Generate report in JSON format
npx knip --reporter json > knip-report.json
```

#### Example Output

```bash
$ npx knip --exports

Unused exported types (2)
Category    interface  src/app/services/api.service.ts:51:18
CacheStats  interface  src/app/services/api.service.ts:59:18
```

#### Configuration

Create a `knip.json` file to customize Knip's behavior:

```json
{
  "ignore": [
    "**/*.spec.ts",
    "**/*.test.ts"
  ],
  "ignoreDependencies": [
    "@angular/platform-browser-dynamic"
  ]
}
```

#### When to Run Knip

- **Before refactoring**: Identify dead code that can be safely removed
- **In CI/CD**: Prevent unused code from being merged
- **During code reviews**: Ensure all new exports are actually used
- **Periodic cleanup**: Monthly or quarterly cleanup sessions

#### Why Knip Over ts-prune?

- **ts-prune is in maintenance mode** (no longer actively developed)
- Knip has **broader feature set** (dependencies, files, types)
- Knip is **actively maintained** and receives regular updates
- Knip is **faster** and more accurate
- Knip works with **modern tooling** (Vite, Next.js, Angular, etc.)

### Comparison: ESLint Rule vs Knip vs Manual Review

| Feature | ESLint Custom Rule | Knip | Manual Review |
|---------|-------------------|------|---------------|
| Cross-file analysis | ❌ No | ✅ Yes | ✅ Yes |
| Detects unused exports | ⚠️ False positives | ✅ Accurate | ✅ Accurate |
| Detects unused files | ❌ No | ✅ Yes | ✅ Yes |
| Detects unused dependencies | ❌ No | ✅ Yes | ⚠️ Manual |
| **Signal Store methods** | ⚠️ False positives | ❌ Cannot detect | ✅ Can detect |
| Detects unused class members | ❌ No | ✅ Yes | ✅ Yes |
| Real-time feedback | ✅ Yes (on save) | ⚠️ On-demand | ❌ No |
| CI/CD integration | ✅ Easy | ✅ Easy | ❌ Difficult |
| Setup complexity | 🔴 High | 🟢 Low | 🟢 None |
| Accuracy | 🔴 Low (false +) | 🟢 High | 🟢 Perfect |

**Conclusion**:
- **For general unused code detection**: Use Knip (detects unused exports, files, dependencies, class members)
- **For Signal Store methods specifically**: Manual code review and comprehensive testing
- **For code style and patterns**: Use ESLint

There is currently **no automated solution** for detecting unused Signal Store methods. The combination of:
1. Knip for general dead code
2. Manual review for Signal Store methods
3. Comprehensive test coverage

...provides the best approach for maintaining clean code.

## References

- [ESLint Custom Rules Documentation](https://eslint.org/docs/latest/extend/custom-rules)
- [NgRx Effects Best Practices](https://ngrx.io/guide/effects/lifecycle)
- [@ngrx/eslint-plugin](https://ngrx.io/guide/eslint-plugin)
- [Knip Documentation](https://knip.dev/)
- [Why Knip Over ts-prune](https://levelup.gitconnected.com/dead-code-detection-in-typescript-projects-why-we-chose-knip-over-ts-prune-8feea827da35)
- [Knip Comparison & Migration Guide](https://knip.dev/explanations/comparison-and-migration)
