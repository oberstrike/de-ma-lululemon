/**
 * Custom ESLint rule to detect unused Signal Store methods.
 *
 * This rule ensures methods defined in withMethods() are actually used
 * somewhere in the codebase, preventing dead code in Signal Stores.
 *
 * @type {import('eslint').Rule.RuleModule}
 */
module.exports = {
  meta: {
    type: 'suggestion',
    docs: {
      description:
        'Disallow unused methods in Signal Store withMethods() blocks',
      category: 'Best Practices',
      recommended: true,
    },
    messages: {
      unusedMethod:
        'Signal Store method "{{methodName}}" is defined but never used. Remove it or use it.',
    },
    schema: [],
  },
  create(context) {
    const sourceCode = context.sourceCode;
    const definedMethods = new Map(); // Map<methodName, node>
    let storeExportName = null;

    /**
     * Check if a call expression is withMethods()
     * @param {import('eslint').Rule.Node} node
     * @returns {boolean}
     */
    function isWithMethodsCall(node) {
      if (node.type !== 'CallExpression') return false;
      const { callee } = node;
      return callee.type === 'Identifier' && callee.name === 'withMethods';
    }

    /**
     * Get method name from property node
     * @param {import('eslint').Rule.Node} property
     * @returns {string | null}
     */
    function getMethodName(property) {
      if (property.key.type === 'Identifier') {
        return property.key.name;
      }
      if (property.key.type === 'Literal') {
        return String(property.key.value);
      }
      return null;
    }

    /**
     * Extract method definitions from withMethods
     * @param {import('eslint').Rule.Node} objectExpression
     */
    function extractMethodDefinitions(objectExpression) {
      if (objectExpression.type !== 'ObjectExpression') return;

      for (const property of objectExpression.properties) {
        // Skip spread properties
        if (property.type === 'SpreadElement') continue;

        // Check if property is a method (function/arrow function)
        const isMethod =
          property.value &&
          (property.value.type === 'FunctionExpression' ||
            property.value.type === 'ArrowFunctionExpression');

        if (isMethod) {
          const methodName = getMethodName(property);
          if (methodName) {
            definedMethods.set(methodName, property);
          }
        }
      }
    }

    /**
     * Find the store export name from signalStore call
     * @param {import('eslint').Rule.Node} node
     */
    function findStoreExportName(node) {
      // Check if this is an export: export const StoreName = signalStore(...)
      if (
        node.type === 'VariableDeclaration' &&
        node.declarations.length > 0
      ) {
        const declarator = node.declarations[0];
        if (
          declarator.init &&
          declarator.init.type === 'CallExpression' &&
          declarator.init.callee.type === 'Identifier' &&
          declarator.init.callee.name === 'signalStore'
        ) {
          if (declarator.id.type === 'Identifier') {
            return declarator.id.name;
          }
        }
      }
      return null;
    }

    return {
      // First pass: collect method definitions
      CallExpression(node) {
        if (!isWithMethodsCall(node)) return;

        if (node.arguments.length === 0) return;
        const firstArg = node.arguments[0];

        if (
          firstArg.type !== 'ArrowFunctionExpression' &&
          firstArg.type !== 'FunctionExpression'
        ) {
          return;
        }

        const functionBody = firstArg.body;

        // Handle both block statements and direct object returns
        if (functionBody.type === 'BlockStatement') {
          for (const statement of functionBody.body) {
            if (statement.type === 'ReturnStatement' && statement.argument) {
              if (statement.argument.type === 'ObjectExpression') {
                extractMethodDefinitions(statement.argument);
              }
            }
          }
        } else if (functionBody.type === 'ObjectExpression') {
          extractMethodDefinitions(functionBody);
        }
      },

      // Find the store export name
      ExportNamedDeclaration(node) {
        if (node.declaration) {
          const exportName = findStoreExportName(node.declaration);
          if (exportName) {
            storeExportName = exportName;
          }
        }
      },

      VariableDeclaration(node) {
        const exportName = findStoreExportName(node);
        if (exportName) {
          storeExportName = exportName;
        }
      },

      // Second pass: check for method usage
      'Program:exit': function () {
        if (definedMethods.size === 0) return;

        // Get the entire source code text
        const text = sourceCode.getText();

        // Check each defined method for usage
        for (const [methodName, node] of definedMethods) {
          // Create regex patterns to find method usage
          // Patterns like: store.methodName( or store.methodName?.( or destructured usage
          const patterns = [
            // Direct call: store.methodName( or storeInstance.methodName(
            new RegExp(`\\.${methodName}\\s*\\(`,'g'),
            // Optional chaining: store.methodName?.(
            new RegExp(`\\.${methodName}\\s*\\?\\s*\\(`,'g'),
            // Destructured usage in components/services
            new RegExp(`\\{[^}]*\\b${methodName}\\b[^}]*\\}\\s*=`, 'g'),
            // Template usage (if store is used in components)
            new RegExp(`\\(\\s*${methodName}\\s*\\(`, 'g'),
          ];

          let isUsed = false;

          // Check if any pattern matches
          for (const pattern of patterns) {
            const matches = text.match(pattern);
            if (matches && matches.length > 0) {
              // Make sure we're not just matching the definition itself
              // by checking if there are multiple occurrences
              const definitionText = sourceCode.getText(node);
              const nonDefinitionMatches = matches.filter(
                (match) => !definitionText.includes(match)
              );

              if (nonDefinitionMatches.length > 0) {
                isUsed = true;
                break;
              }
            }
          }

          // Report unused methods
          if (!isUsed) {
            context.report({
              node,
              messageId: 'unusedMethod',
              data: { methodName },
            });
          }
        }
      },
    };
  },
};
