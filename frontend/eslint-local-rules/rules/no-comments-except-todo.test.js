const { RuleTester } = require('eslint');
const tsParser = require('@typescript-eslint/parser');
const rule = require('./no-comments-except-todo');

const ruleTester = new RuleTester({
  languageOptions: {
    parser: tsParser,
    parserOptions: {
      ecmaVersion: 2020,
      sourceType: 'module',
    },
  },
});

ruleTester.run('no-comments-except-todo', rule, {
  valid: [
    {
      name: 'Valid: Code without comments',
      code: `
        export class UserService {
          getUser(id: string) {
            return this.api.fetchUser(id);
          }
        }
      `,
    },
    {
      name: 'Valid: TODO comment with colon',
      code: `
        export class UserService {
          // TODO: Implement caching
          getUser(id: string) {
            return this.api.fetchUser(id);
          }
        }
      `,
    },
    {
      name: 'Valid: TODO comment with space',
      code: `
        export class UserService {
          // TODO implement caching
          getUser(id: string) {
            return this.api.fetchUser(id);
          }
        }
      `,
    },
    {
      name: 'Valid: FIXME comment',
      code: `
        export class UserService {
          // FIXME: Memory leak in subscription
          getUser(id: string) {
            return this.api.fetchUser(id);
          }
        }
      `,
    },
    {
      name: 'Valid: Block TODO comment',
      code: `
        export class UserService {
          /* TODO: Refactor this method */
          getUser(id: string) {
            return this.api.fetchUser(id);
          }
        }
      `,
    },
    {
      name: 'Valid: JSDoc comment',
      code: `
        /**
         * Fetches user data from the API
         * @param id User ID
         * @returns User object
         */
        export class UserService {
          getUser(id: string) {
            return this.api.fetchUser(id);
          }
        }
      `,
    },
    {
      name: 'Valid: Multiple JSDoc comments',
      code: `
        export class UserService {
          /**
           * Fetches a user by ID
           */
          getUser(id: string) {
            return this.api.fetchUser(id);
          }

          /**
           * Creates a new user
           */
          createUser(data: UserData) {
            return this.api.createUser(data);
          }
        }
      `,
    },
    {
      name: 'Valid: TODO in lowercase',
      code: `
        // todo: fix this
        const x = 1;
      `,
    },
  ],
  invalid: [
    {
      name: 'Invalid: Single-line comment',
      code: `
        // This is a regular comment
        const x = 1;
      `,
      errors: [
        {
          messageId: 'noComment',
        },
      ],
    },
    {
      name: 'Invalid: Block comment',
      code: `
        /* This is a block comment */
        const x = 1;
      `,
      errors: [
        {
          messageId: 'noComment',
        },
      ],
    },
    {
      name: 'Invalid: Inline comment',
      code: `
        const x = 1; // Set x to 1
      `,
      errors: [
        {
          messageId: 'noComment',
        },
      ],
    },
    {
      name: 'Invalid: Explanation comment',
      code: `
        export class UserService {
          // Fetch user from API
          getUser(id: string) {
            return this.api.fetchUser(id);
          }
        }
      `,
      errors: [
        {
          messageId: 'noComment',
        },
      ],
    },
    {
      name: 'Invalid: Multiple regular comments',
      code: `
        // First comment
        const x = 1;
        // Second comment
        const y = 2;
      `,
      errors: [
        {
          messageId: 'noComment',
        },
        {
          messageId: 'noComment',
        },
      ],
    },
    {
      name: 'Invalid: Commented out code',
      code: `
        const x = 1;
        // const y = 2;
        const z = 3;
      `,
      errors: [
        {
          messageId: 'noComment',
        },
      ],
    },
    {
      name: 'Invalid: Mixed valid and invalid comments',
      code: `
        // TODO: Implement feature
        const x = 1;
        // This is a regular comment
        const y = 2;
      `,
      errors: [
        {
          messageId: 'noComment',
        },
      ],
    },
    {
      name: 'Invalid: Comment that mentions TODO but is not a TODO',
      code: `
        // We should add a TODO for this
        const x = 1;
      `,
      errors: [
        {
          messageId: 'noComment',
        },
      ],
    },
  ],
});

console.log('✅ All tests passed!');
