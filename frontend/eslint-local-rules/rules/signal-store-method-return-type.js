/**
 * Custom ESLint rule to enforce explicit return types on Signal Store methods.
 *
 * This rule ensures all methods defined in withMethods() have explicit return types,
 * following the project's NgRx Signal Store best practices.
 *
 * @type {import('eslint').Rule.RuleModule}
 */
module.exports = {
  meta: {
    type: 'problem',
    docs: {
      description:
        'Enforce explicit return types on all Signal Store methods defined in withMethods()',
      category: 'Best Practices',
      recommended: true,
    },
    messages: {
      missingReturnType:
        'Signal Store method "{{methodName}}" must have an explicit return type. Add : ReturnType after the parameter list.',
    },
    schema: [],
  },
  create(context) {
    /**
     * Check if a call expression is withMethods()
     * @param {import('eslint').Rule.Node} node
     * @returns {boolean}
     */
    function isWithMethodsCall(node) {
      if (node.type !== 'CallExpression') return false;

      const { callee } = node;

      // Direct call: withMethods(...)
      if (callee.type === 'Identifier' && callee.name === 'withMethods') {
        return true;
      }

      return false;
    }

    /**
     * Check if a function/method has an explicit return type annotation
     * @param {import('eslint').Rule.Node} node
     * @returns {boolean}
     */
    function hasReturnType(node) {
      // Check for TypeScript return type annotation
      return node.returnType != null;
    }

    /**
     * Get method name from property node
     * @param {import('eslint').Rule.Node} property
     * @returns {string}
     */
    function getMethodName(property) {
      if (property.key.type === 'Identifier') {
        return property.key.name;
      }
      if (property.key.type === 'Literal') {
        return String(property.key.value);
      }
      return 'unknown';
    }

    /**
     * Check object properties for methods without return types
     * @param {import('eslint').Rule.Node} objectExpression
     */
    function checkObjectMethods(objectExpression) {
      if (objectExpression.type !== 'ObjectExpression') return;

      for (const property of objectExpression.properties) {
        // Skip spread properties
        if (property.type === 'SpreadElement') continue;

        // Check if property is a method (function/arrow function)
        const isMethod =
          property.value &&
          (property.value.type === 'FunctionExpression' ||
            property.value.type === 'ArrowFunctionExpression');

        if (isMethod && !hasReturnType(property.value)) {
          const methodName = getMethodName(property);
          context.report({
            node: property,
            messageId: 'missingReturnType',
            data: { methodName },
          });
        }
      }
    }

    return {
      CallExpression(node) {
        // Only check withMethods() calls
        if (!isWithMethodsCall(node)) return;

        // withMethods() should have exactly one argument
        if (node.arguments.length === 0) return;

        const firstArg = node.arguments[0];

        // The argument should be an arrow function or function expression
        if (
          firstArg.type !== 'ArrowFunctionExpression' &&
          firstArg.type !== 'FunctionExpression'
        ) {
          return;
        }

        // The function should return an object expression
        const functionBody = firstArg.body;

        // Handle both block statements and direct object returns
        if (functionBody.type === 'BlockStatement') {
          // Find return statements
          for (const statement of functionBody.body) {
            if (statement.type === 'ReturnStatement' && statement.argument) {
              if (statement.argument.type === 'ObjectExpression') {
                checkObjectMethods(statement.argument);
              }
            }
          }
        } else if (functionBody.type === 'ObjectExpression') {
          // Direct object return: withMethods(() => ({ ... }))
          checkObjectMethods(functionBody);
        }
      },
    };
  },
};
