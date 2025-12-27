/**
 * Tests for create-effect-in-service ESLint rule
 */
const { RuleTester } = require('eslint');
const tsParser = require('@typescript-eslint/parser');
const rule = require('./create-effect-in-service');

// Configure RuleTester for ESLint 9 (flat config format)
const ruleTester = new RuleTester({
  languageOptions: {
    parser: tsParser,
    parserOptions: {
      ecmaVersion: 2020,
      sourceType: 'module',
      ecmaFeatures: {
        decorators: true,
      },
    },
  },
});

ruleTester.run('create-effect-in-service', rule, {
  valid: [
    {
      name: 'Valid: createEffect as property in @Injectable class',
      code: `
        import { Injectable } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        @Injectable()
        export class MovieEffects {
          loadMovies$ = createEffect(() => this.actions$.pipe());
          constructor(private actions$: Actions) {}
        }
      `,
    },
    {
      name: 'Valid: createEffect with @Injectable decorator without parentheses',
      code: `
        import { Injectable } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        @Injectable
        export class MovieEffects {
          loadMovies$ = createEffect(() => this.actions$.pipe());
        }
      `,
    },
    {
      name: 'Valid: createEffect in class expression with @Injectable',
      code: `
        import { Injectable } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        const MovieEffects = @Injectable() class {
          loadMovies$ = createEffect(() => this.actions$.pipe());
        };
      `,
    },
    {
      name: 'Valid: multiple effects in same class',
      code: `
        import { Injectable } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        @Injectable()
        export class MovieEffects {
          loadMovies$ = createEffect(() => this.actions$.pipe());
          saveMovie$ = createEffect(() => this.actions$.pipe());
          deleteMovie$ = createEffect(() => this.actions$.pipe());
        }
      `,
    },
    {
      name: 'Valid: createEffect with arrow function property',
      code: `
        import { Injectable } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        @Injectable()
        export class MovieEffects {
          loadMovies$ = createEffect(() => {
            return this.actions$.pipe();
          });
        }
      `,
    },
  ],

  invalid: [
    {
      name: 'Invalid: createEffect in @Component class',
      code: `
        import { Component } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        @Component({
          selector: 'app-movie'
        })
        export class MovieComponent {
          loadMovies$ = createEffect(() => this.actions$.pipe());
        }
      `,
      errors: [
        {
          messageId: 'createEffectInNonInjectableClass',
          line: 9,
        },
      ],
    },
    {
      name: 'Invalid: createEffect in class without decorator',
      code: `
        import { createEffect } from '@ngrx/effects';

        export class MovieEffects {
          loadMovies$ = createEffect(() => this.actions$.pipe());
        }
      `,
      errors: [
        {
          messageId: 'createEffectInNonInjectableClass',
          line: 5,
        },
      ],
    },
    {
      name: 'Invalid: createEffect outside any class',
      code: `
        import { createEffect } from '@ngrx/effects';
        import { inject } from '@angular/core';

        export const loadMovies$ = createEffect(() => {
          const actions$ = inject(Actions);
          return actions$.pipe();
        }, { functional: true });
      `,
      errors: [
        {
          messageId: 'createEffectOutsideClass',
          line: 5,
        },
      ],
    },
    {
      name: 'Invalid: createEffect called in ngOnInit method',
      code: `
        import { Injectable } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        @Injectable()
        export class MovieEffects {
          loadMovies$: any;

          ngOnInit() {
            this.loadMovies$ = createEffect(() => this.actions$.pipe());
          }
        }
      `,
      errors: [
        {
          messageId: 'createEffectInMethod',
          line: 10,
        },
      ],
    },
    {
      name: 'Invalid: createEffect called in constructor',
      code: `
        import { Injectable } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        @Injectable()
        export class MovieEffects {
          loadMovies$: any;

          constructor() {
            this.loadMovies$ = createEffect(() => this.actions$.pipe());
          }
        }
      `,
      errors: [
        {
          messageId: 'createEffectInMethod',
          line: 10,
        },
      ],
    },
    {
      name: 'Invalid: createEffect called in a custom method',
      code: `
        import { Injectable } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        @Injectable()
        export class MovieEffects {
          setupEffects() {
            const effect = createEffect(() => this.actions$.pipe());
          }
        }
      `,
      errors: [
        {
          messageId: 'createEffectInMethod',
          line: 8,
        },
      ],
    },
    {
      name: 'Invalid: createEffect in class expression without @Injectable',
      code: `
        import { createEffect } from '@ngrx/effects';

        const MovieEffects = class {
          loadMovies$ = createEffect(() => this.actions$.pipe());
        };
      `,
      errors: [
        {
          messageId: 'createEffectInNonInjectableClass',
          line: 5,
        },
      ],
    },
    {
      name: 'Invalid: createEffect with member expression syntax',
      code: `
        import { Component } from '@angular/core';
        import * as effects from '@ngrx/effects';

        @Component({
          selector: 'app-movie'
        })
        export class MovieComponent {
          loadMovies$ = effects.createEffect(() => this.actions$.pipe());
        }
      `,
      errors: [
        {
          messageId: 'createEffectInNonInjectableClass',
          line: 9,
        },
      ],
    },
    {
      name: 'Invalid: multiple violations in same file',
      code: `
        import { Component } from '@angular/core';
        import { Injectable } from '@angular/core';
        import { createEffect } from '@ngrx/effects';

        @Component({
          selector: 'app-movie'
        })
        export class MovieComponent {
          loadMovies$ = createEffect(() => this.actions$.pipe());
        }

        @Injectable()
        export class MovieEffects {
          setupEffects() {
            const effect = createEffect(() => this.actions$.pipe());
          }
        }

        export const functionalEffect = createEffect(() => {}, { functional: true });
      `,
      errors: [
        {
          messageId: 'createEffectInNonInjectableClass',
          line: 10,
        },
        {
          messageId: 'createEffectInMethod',
          line: 16,
        },
        {
          messageId: 'createEffectOutsideClass',
          line: 20,
        },
      ],
    },
  ],
});

console.log('✅ All tests passed!');
