/**
 * Local ESLint rules plugin for project-specific linting rules
 */
module.exports = {
  rules: {
    'create-effect-in-service': require('./rules/create-effect-in-service'),
    'signal-store-method-return-type': require('./rules/signal-store-method-return-type'),
  },
};
