/**
 * Local ESLint rules plugin for project-specific linting rules
 */
module.exports = {
  rules: {
    'create-effect-in-service': require('./rules/create-effect-in-service'),
    'no-comments-except-todo': require('./rules/no-comments-except-todo'),
  },
};
