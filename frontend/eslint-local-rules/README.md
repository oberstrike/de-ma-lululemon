# Custom ESLint Rules

This directory contains custom ESLint rules specific to this project.

## Available Rules

### `signal-store-method-return-type`

Enforces explicit return type annotations on all Signal Store methods.

**Rule Type:** `problem`

**Severity:** `error`

#### What it does

This rule ensures that all methods defined in `withMethods()` have explicit return type annotations. This is required for:
- Type safety and better IDE support
- Clear API contracts
- Preventing accidental return type changes
- Following the project's coding standards

#### Why?

Explicit return types on store methods provide several benefits:

1. **Type Safety**: Prevents accidental breaking changes to method signatures
2. **Documentation**: Return types serve as inline documentation
3. **IDE Support**: Better autocomplete and type checking
4. **Consistency**: All store methods follow the same pattern
5. **Refactoring**: Safer refactoring with compile-time guarantees

#### Examples

##### ✅ Valid Usage

```typescript
import { signalStore, withMethods } from '@ngrx/signals';

export const MovieStore = signalStore(
  withMethods((store) => ({
    // ✅ Valid: void return type
    loadMovies(): void {
      console.log('loading');
    },

    // ✅ Valid: async with Promise return type
    async fetchData(): Promise<void> {
      await fetch('/api');
    },

    // ✅ Valid: method with parameter and return type
    selectMovie(id: string): void {
      patchState(store, { selectedId: id });
    },

    // ✅ Valid: arrow function with return type
    getCount: (): number => {
      return 42;
    },
  }))
);
```

##### ❌ Invalid Usage

```typescript
import { signalStore, withMethods } from '@ngrx/signals';

export const MovieStore = signalStore(
  withMethods((store) => ({
    // ❌ Error: missing return type
    loadMovies() {
      console.log('loading');
    },

    // ❌ Error: async method without Promise return type
    async fetchData() {
      await fetch('/api');
    },

    // ❌ Error: arrow function without return type
    getCount: () => {
      return 42;
    },
  }))
);
```

#### Error Messages

- `missingReturnType`: Signal Store method "{{methodName}}" must have an explicit return type. Add : ReturnType after the parameter list.

#### Configuration

This rule has no configuration options. It's enabled by default:

```javascript
rules: {
  'local-rules/signal-store-method-return-type': 'error',
}
```

#### When to disable

This rule should generally not be disabled. However, if you need to disable it for a specific file:

```typescript
/* eslint-disable local-rules/signal-store-method-return-type */
export const MyStore = signalStore(
  withMethods(() => ({ method() { } }))
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
node eslint-local-rules/rules/signal-store-method-return-type.test.js
node eslint-local-rules/rules/create-effect-in-service.test.js
```

### Test Coverage

#### `signal-store-method-return-type` rule tests:

**Valid usage (6 tests):**
- ✅ All methods have explicit return types
- ✅ Arrow functions with return types
- ✅ Methods with complex return types (union, array, record)
- ✅ withMethods with return statement
- ✅ Empty withMethods
- ✅ withMethods with non-function properties

**Invalid usage (7 tests):**
- ❌ Method without return type
- ❌ Async method without return type
- ❌ Arrow function without return type
- ❌ Multiple methods missing return types
- ❌ Mixed (some with, some without return types)
- ❌ withMethods with return statement missing types
- ❌ Arrow function with implicit return

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
