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

The `create-effect-in-service` rule includes tests for:

**Valid usage:**
- ✅ createEffect as property in @Injectable class
- ✅ createEffect with @Injectable decorator (with/without parentheses)
- ✅ createEffect in class expressions with @Injectable
- ✅ Multiple effects in same class
- ✅ createEffect with arrow function properties

**Invalid usage:**
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
