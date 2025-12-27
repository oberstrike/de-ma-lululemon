/**
 * Tests for signal-store-method-return-type ESLint rule
 */
const { RuleTester } = require('eslint');
const tsParser = require('@typescript-eslint/parser');
const rule = require('./signal-store-method-return-type');

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

ruleTester.run('signal-store-method-return-type', rule, {
  valid: [
    {
      name: 'Valid: all methods have explicit return types',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            loadMovies(): void {
              console.log('loading');
            },
            async fetchData(): Promise<void> {
              await fetch('/api');
            },
            selectMovie(id: string): void {
              console.log(id);
            },
          }))
        );
      `,
    },
    {
      name: 'Valid: arrow functions with return types',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            loadMovies: (): void => {
              console.log('loading');
            },
            fetchData: async (): Promise<void> => {
              await fetch('/api');
            },
          }))
        );
      `,
    },
    {
      name: 'Valid: methods with complex return types',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            getMovie(id: string): Movie | null {
              return null;
            },
            getAllMovies(): Movie[] {
              return [];
            },
            getMovieMap(): Record<string, Movie> {
              return {};
            },
          }))
        );
      `,
    },
    {
      name: 'Valid: withMethods with return statement',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => {
            return {
              loadMovies(): void {
                console.log('loading');
              },
              selectMovie(id: string): void {
                console.log(id);
              },
            };
          })
        );
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
    {
      name: 'Valid: withMethods with non-function properties',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            constantValue: 42,
            loadMovies(): void {
              console.log('loading');
            },
          }))
        );
      `,
    },
  ],

  invalid: [
    {
      name: 'Invalid: method without return type',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            loadMovies() {
              console.log('loading');
            },
          }))
        );
      `,
      errors: [
        {
          messageId: 'missingReturnType',
          data: { methodName: 'loadMovies' },
        },
      ],
    },
    {
      name: 'Invalid: async method without return type',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            async loadMovies() {
              await fetch('/api');
            },
          }))
        );
      `,
      errors: [
        {
          messageId: 'missingReturnType',
          data: { methodName: 'loadMovies' },
        },
      ],
    },
    {
      name: 'Invalid: arrow function without return type',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            loadMovies: () => {
              console.log('loading');
            },
          }))
        );
      `,
      errors: [
        {
          messageId: 'missingReturnType',
          data: { methodName: 'loadMovies' },
        },
      ],
    },
    {
      name: 'Invalid: multiple methods missing return types',
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
      `,
      errors: [
        {
          messageId: 'missingReturnType',
          data: { methodName: 'loadMovies' },
        },
        {
          messageId: 'missingReturnType',
          data: { methodName: 'selectMovie' },
        },
        {
          messageId: 'missingReturnType',
          data: { methodName: 'deleteMovie' },
        },
      ],
    },
    {
      name: 'Invalid: mixed - some with, some without return types',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            loadMovies(): void {
              console.log('loading');
            },
            selectMovie(id: string) {
              console.log(id);
            },
            deleteMovie(id: string): void {
              console.log(id);
            },
          }))
        );
      `,
      errors: [
        {
          messageId: 'missingReturnType',
          data: { methodName: 'selectMovie' },
        },
      ],
    },
    {
      name: 'Invalid: withMethods with return statement',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => {
            return {
              loadMovies() {
                console.log('loading');
              },
            };
          })
        );
      `,
      errors: [
        {
          messageId: 'missingReturnType',
          data: { methodName: 'loadMovies' },
        },
      ],
    },
    {
      name: 'Invalid: arrow function with implicit return',
      code: `
        import { signalStore, withMethods } from '@ngrx/signals';

        export const MovieStore = signalStore(
          withMethods((store) => ({
            getCount: () => 42,
          }))
        );
      `,
      errors: [
        {
          messageId: 'missingReturnType',
          data: { methodName: 'getCount' },
        },
      ],
    },
  ],
});

console.log('✅ All tests passed!');
