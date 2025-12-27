/**
 * Tests for signal-store-no-unused-methods ESLint rule
 */
const { RuleTester } = require('eslint');
const tsParser = require('@typescript-eslint/parser');
const rule = require('./signal-store-no-unused-methods');

// Configure RuleTester for ESLint 9 (flat config format)
const ruleTester = new RuleTester({
  languageOptions: {
    parser: tsParser,
    parserOptions: {
      ecmaVersion: 2020,
      sourceType: 'module',
    },
  },
});

ruleTester.run('signal-store-no-unused-methods', rule, {
  valid: [
    {
      name: 'Valid: method called directly on store instance',
      code: `
        import { signalStore, withMethods, withState } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withState({ count: 0 }),
          withMethods((store) => ({
            increment() {
              console.log('incrementing');
            },
          }))
        );

        const store = inject(MovieStore);
        store.increment();
      `,
    },
    {
      name: 'Valid: method called with optional chaining',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            loadMovies() {
              console.log('loading');
            },
          }))
        );

        const store = inject(MovieStore);
        store?.loadMovies();
      `,
    },
    {
      name: 'Valid: method destructured from store',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            selectMovie(id: string) {
              console.log(id);
            },
          }))
        );

        const { selectMovie } = inject(MovieStore);
        selectMovie('123');
      `,
    },
    {
      name: 'Valid: multiple methods all used',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            loadMovies() {
              console.log('loading');
            },
            selectMovie(id: string) {
              console.log(id);
            },
            deleteMovie(id: string) {
              console.log(id);
            },
          }))
        );

        const store = inject(MovieStore);
        store.loadMovies();
        store.selectMovie('1');
        store.deleteMovie('2');
      `,
    },
    {
      name: 'Valid: method used in component template pattern',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            handleClick() {
              console.log('clicked');
            },
          }))
        );

        @Component({
          template: '<button (click)="store.handleClick()">Click</button>'
        })
        export class MyComponent {
          readonly store = inject(MovieStore);
        }
      `,
    },
    {
      name: 'Valid: empty withMethods',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({}))
        );
      `,
    },
  ],

  invalid: [
    {
      name: 'Invalid: method defined but never used',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            unusedMethod() {
              console.log('never called');
            },
          }))
        );

        const store = inject(MovieStore);
        // unusedMethod is never called
      `,
      errors: [
        {
          messageId: 'unusedMethod',
          data: { methodName: 'unusedMethod' },
        },
      ],
    },
    {
      name: 'Invalid: multiple unused methods',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            unusedMethod1() {
              console.log('never called');
            },
            unusedMethod2() {
              console.log('also never called');
            },
            unusedMethod3() {
              console.log('still never called');
            },
          }))
        );
      `,
      errors: [
        {
          messageId: 'unusedMethod',
          data: { methodName: 'unusedMethod1' },
        },
        {
          messageId: 'unusedMethod',
          data: { methodName: 'unusedMethod2' },
        },
        {
          messageId: 'unusedMethod',
          data: { methodName: 'unusedMethod3' },
        },
      ],
    },
    {
      name: 'Invalid: some methods used, some unused',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            usedMethod() {
              console.log('used');
            },
            unusedMethod() {
              console.log('not used');
            },
          }))
        );

        const store = inject(MovieStore);
        store.usedMethod();
      `,
      errors: [
        {
          messageId: 'unusedMethod',
          data: { methodName: 'unusedMethod' },
        },
      ],
    },
    {
      name: 'Invalid: method with similar name but not called',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            loadMovies() {
              console.log('loading');
            },
          }))
        );

        const store = inject(MovieStore);
        // loadMovie (singular) vs loadMovies (plural)
        function loadMovie() {
          console.log('different function');
        }
      `,
      errors: [
        {
          messageId: 'unusedMethod',
          data: { methodName: 'loadMovies' },
        },
      ],
    },
  ],
});

console.log('✅ All tests passed!');
