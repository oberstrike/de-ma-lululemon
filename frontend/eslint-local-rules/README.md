# Custom ESLint Rules

This directory contains custom ESLint rules specific to this project.

## Available Rules

### `signal-store-no-unused-methods`

Detects and reports unused methods in Signal Store `withMethods()` blocks.

**Rule Type:** `suggestion`

**Severity:** `warn`

#### What it does

This rule detects methods defined in `withMethods()` that are never called or used anywhere in the codebase. This helps identify dead code and ensures all store methods serve a purpose.

The rule checks for method usage through:
- Direct calls: `store.methodName()`
- Optional chaining: `store?.methodName()`
- Destructured usage: `const { methodName } = store`
- Template usage in components

#### Why?

Removing unused methods provides several benefits:

1. **Code Cleanliness**: Eliminates dead code from your Signal Stores
2. **Maintainability**: Reduces confusion about which methods are actually used
3. **Bundle Size**: Smaller production bundles by removing unused code
4. **Clarity**: Makes it clear which methods are part of the public API
5. **Refactoring**: Helps identify methods that can be safely removed during refactoring

#### Examples

##### ✅ Valid Usage (Methods are used)

```typescript
import { signalStore, withMethods } from '@ngrx/signals';

export const MovieStore = signalStore(
  withMethods((store) => ({
    loadMovies() {
      console.log('loading');
    },
  }))
);

// Method is used
const store = inject(MovieStore);
store.loadMovies();
```

```typescript
// Destructured usage
const { loadMovies } = inject(MovieStore);
loadMovies();
```

##### ❌ Invalid Usage (Methods are unused)

```typescript
import { signalStore, withMethods } from '@ngrx/signals';

export const MovieStore = signalStore(
  withMethods((store) => ({
    // ❌ Warning: This method is never called
    unusedMethod() {
      console.log('never used');
    },
  }))
);

const store = inject(MovieStore);
// unusedMethod is never called
```

#### Error Messages

- `unusedMethod`: Signal Store method "{{methodName}}" is defined but never used. Remove it or use it.

#### Configuration

This rule is enabled by default as a warning:

```javascript
rules: {
  'local-rules/signal-store-no-unused-methods': 'warn',
}
```

#### When to disable

You may want to disable this rule if:
- You're building a library and methods are intended for external use
- You're gradually migrating code and plan to use the method later

```typescript
/* eslint-disable local-rules/signal-store-no-unused-methods */
export const MyStore = signalStore(
  withMethods(() => ({
    futureMethod() { } // Will be used later
  }))
);
```

---

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

# Or run individual test files
node eslint-local-rules/rules/signal-store-no-unused-methods.test.js
node eslint-local-rules/rules/create-effect-in-service.test.js
```

### Test Coverage

#### `signal-store-no-unused-methods` rule tests:

**Valid usage (6 tests):**
- ✅ Method called directly on store instance
- ✅ Method called with optional chaining
- ✅ Method destructured from store
- ✅ Multiple methods all used
- ✅ Method used in component template pattern
- ✅ Empty withMethods

**Invalid usage (4 tests):**
- ❌ Method defined but never used
- ❌ Multiple unused methods
- ❌ Some methods used, some unused
- ❌ Method with similar name but not called

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

## References

- [ESLint Custom Rules Documentation](https://eslint.org/docs/latest/extend/custom-rules)
- [NgRx Effects Best Practices](https://ngrx.io/guide/effects/lifecycle)
- [@ngrx/eslint-plugin](https://ngrx.io/guide/eslint-plugin)
