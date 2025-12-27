// @ts-check
const eslint = require('@eslint/js');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');
const prettier = require('eslint-config-prettier');
const simpleImportSort = require('eslint-plugin-simple-import-sort');
const unusedImports = require('eslint-plugin-unused-imports');
const rxjsX = require('eslint-plugin-rxjs-x');
const ngrx = require('@ngrx/eslint-plugin');
const localRules = require('./eslint-local-rules');

module.exports = tseslint.config(
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.strictTypeChecked,
      ...tseslint.configs.stylisticTypeChecked,
      ...angular.configs.tsRecommended,
    ],
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: __dirname,
      },
    },
    plugins: {
      'simple-import-sort': simpleImportSort,
      'unused-imports': unusedImports,
      'rxjs-x': rxjsX,
      '@ngrx': ngrx,
      'local-rules': localRules,
    },
    processor: angular.processInlineTemplates,
    rules: {
      // Angular component/directive selectors
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'app',
          style: 'camelCase',
        },
      ],
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          prefix: 'app',
          style: 'kebab-case',
        },
      ],

      // Import sorting
      'simple-import-sort/imports': 'error',
      'simple-import-sort/exports': 'error',

      // Unused imports
      'unused-imports/no-unused-imports': 'error',
      'unused-imports/no-unused-vars': [
        'warn',
        {
          vars: 'all',
          varsIgnorePattern: '^_',
          args: 'after-used',
          argsIgnorePattern: '^_',
        },
      ],

      // RxJS best practices
      'rxjs-x/no-async-subscribe': 'error',
      'rxjs-x/no-nested-subscribe': 'error',
      'rxjs-x/no-unbound-methods': 'error',
      'rxjs-x/no-unsafe-takeuntil': 'error',
      'rxjs-x/no-ignored-notifier': 'error',
      'rxjs-x/throw-error': 'error',
      'rxjs-x/prefer-observer': 'warn',

      // NgRx Effects best practices
      '@ngrx/no-dispatch-in-effects': 'error',
      '@ngrx/no-effects-in-providers': 'error',
      '@ngrx/prefer-action-creator-in-of-type': 'error',
      '@ngrx/prefer-effect-callback-in-block-statement': 'error',
      '@ngrx/use-effects-lifecycle-interface': 'error',
      '@ngrx/prefer-concat-latest-from': 'error',

      // NgRx Signal Store best practices
      '@ngrx/signal-store-feature-should-use-generic-type': 'error',
      '@ngrx/avoid-combining-component-store-selectors': 'error',
      '@ngrx/avoid-mapping-component-store-selectors': 'error',

      // Custom NgRx rules
      'local-rules/create-effect-in-service': 'error',

      // Custom code quality rules
      'local-rules/no-comments-except-todo': 'error',

      // Style guide rules
      'local-rules/prefer-css-variables': 'warn',

      // Code complexity
      complexity: ['warn', { max: 15 }],
      'max-depth': ['warn', { max: 4 }],
      'max-nested-callbacks': ['warn', { max: 3 }],
      'max-lines-per-function': ['warn', { max: 100, skipBlankLines: true, skipComments: true }],

      // TypeScript strict rules
      '@typescript-eslint/explicit-function-return-type': [
        'warn',
        {
          allowExpressions: true,
          allowTypedFunctionExpressions: true,
          allowHigherOrderFunctions: true,
          allowDirectConstAssertionInArrowFunctions: true,
          allowConciseArrowFunctionExpressionsStartingWithVoid: true,
        },
      ],
      '@typescript-eslint/no-explicit-any': 'error',
      '@typescript-eslint/no-unused-vars': 'off', // Handled by unused-imports
      '@typescript-eslint/no-deprecated': 'warn',
      '@typescript-eslint/prefer-readonly': 'warn',
      '@typescript-eslint/strict-boolean-expressions': [
        'warn',
        {
          allowString: true,
          allowNumber: true,
          allowNullableObject: true,
          allowNullableBoolean: true,
          allowNullableString: true,
          allowNullableNumber: false,
          allowAny: false,
        },
      ],

      // Naming conventions
      '@typescript-eslint/naming-convention': [
        'warn',
        // Default: camelCase
        {
          selector: 'default',
          format: ['camelCase'],
          leadingUnderscore: 'allow',
        },
        // Variables: camelCase, UPPER_CASE, or PascalCase (for NgRx stores)
        {
          selector: 'variable',
          format: ['camelCase', 'UPPER_CASE', 'PascalCase'],
          leadingUnderscore: 'allow',
        },
        // Imports: allow PascalCase for classes/decorators
        {
          selector: 'import',
          format: ['camelCase', 'PascalCase'],
        },
        // Functions: camelCase
        {
          selector: 'function',
          format: ['camelCase'],
        },
        // Types, classes, interfaces, enums: PascalCase
        {
          selector: 'typeLike',
          format: ['PascalCase'],
        },
        // Enum members: UPPER_CASE or PascalCase
        {
          selector: 'enumMember',
          format: ['UPPER_CASE', 'PascalCase'],
        },
        // Properties: allow numeric keys and various formats
        {
          selector: 'property',
          format: ['camelCase', 'UPPER_CASE', 'PascalCase'],
          leadingUnderscore: 'allow',
        },
        // Object literal properties: allow any format (for theme configs, etc.)
        {
          selector: 'objectLiteralProperty',
          format: null,
        },
        // Parameters: camelCase
        {
          selector: 'parameter',
          format: ['camelCase'],
          leadingUnderscore: 'allow',
        },
      ],

      // General best practices
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      eqeqeq: ['error', 'always'],
      'no-duplicate-imports': 'error',
      'prefer-const': 'error',

      // Relax some strict rules that are too noisy
      '@typescript-eslint/no-floating-promises': 'off',
      '@typescript-eslint/no-misused-promises': 'off',
      '@typescript-eslint/unbound-method': ['error', { ignoreStatic: true }],
      '@typescript-eslint/no-extraneous-class': 'off', // Angular components can be empty
      '@typescript-eslint/no-confusing-void-expression': 'off', // Common in Angular
      '@typescript-eslint/restrict-template-expressions': ['warn', { allowNumber: true }],
      '@typescript-eslint/restrict-plus-operands': ['warn', { allowNumberAndString: true }],
      '@typescript-eslint/use-unknown-in-catch-callback-variable': 'off',
      '@typescript-eslint/no-non-null-assertion': 'warn', // Downgrade to warning
      '@typescript-eslint/no-unnecessary-condition': 'off', // Too noisy with Angular signals
      '@typescript-eslint/no-invalid-void-type': 'off', // Observable<void> is common in Angular
    },
  },
  {
    files: ['**/*.test.ts', '**/*.spec.ts'],
    rules: {
      // Relax type-safety rules for test mocking
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unsafe-assignment': 'off',
      '@typescript-eslint/no-unsafe-member-access': 'off',
      '@typescript-eslint/no-unsafe-call': 'off',
      '@typescript-eslint/no-unsafe-argument': 'off',
      '@typescript-eslint/no-unsafe-return': 'off',
      'max-lines-per-function': 'off',
      // provideNoopAnimations is deprecated in Angular 21 for v23 - no alternative available yet
      '@typescript-eslint/no-deprecated': 'off',
    },
  },
  {
    // NgRx stores have larger factory functions by design
    files: ['**/*.store.ts'],
    rules: {
      'max-lines-per-function': ['warn', { max: 150, skipBlankLines: true, skipComments: true }],
    },
  },
  {
    // Animation providers deprecated in Angular 21 for v23 - no alternative available yet
    files: ['**/main.ts'],
    rules: {
      '@typescript-eslint/no-deprecated': 'off',
    },
  },
  {
    files: ['**/*.html'],
    extends: [...angular.configs.templateRecommended, ...angular.configs.templateAccessibility],
    rules: {},
  },
  // Prettier must be last to override conflicting rules
  prettier
);
