/**
 * Custom ESLint rule to enforce that `createEffect` is only called
 * inside @Injectable service classes, not in components or other contexts.
 *
 * This rule ensures NgRx effects follow the recommended pattern:
 * - Effects must be defined in @Injectable() classes (effect services)
 * - createEffect should be used as a class property initializer, not in methods
 * - Prevents createEffect usage in components, lifecycle hooks, or outside classes
 *
 * @type {import('eslint').Rule.RuleModule}
 */
module.exports = {
  meta: {
    type: 'problem',
    docs: {
      description:
        'Disallow createEffect outside @Injectable services or in improper locations',
      category: 'Best Practices',
      recommended: true,
    },
    messages: {
      createEffectOutsideClass:
        '`createEffect` must be defined inside a class. Consider creating an @Injectable effects service.',
      createEffectInNonInjectableClass:
        '`createEffect` should only be used in @Injectable() classes. This class is not decorated with @Injectable.',
      createEffectInMethod:
        '`createEffect` should be a class property initializer, not called inside a method or function.',
    },
    schema: [],
  },
  create(context) {
    /**
     * Check if a node is a createEffect call expression
     * @param {import('eslint').Rule.Node} node
     * @returns {boolean}
     */
    function isCreateEffectCall(node) {
      if (node.type !== 'CallExpression') return false;

      const { callee } = node;

      // Direct call: createEffect(...)
      if (callee.type === 'Identifier' && callee.name === 'createEffect') {
        return true;
      }

      // Aliased call (rare, but possible): effects.createEffect(...)
      if (
        callee.type === 'MemberExpression' &&
        callee.property.type === 'Identifier' &&
        callee.property.name === 'createEffect'
      ) {
        return true;
      }

      return false;
    }

    /**
     * Check if a class has @Injectable decorator
     * @param {import('eslint').Rule.Node} classNode
     * @returns {boolean}
     */
    function hasInjectableDecorator(classNode) {
      if (!classNode.decorators) return false;

      return classNode.decorators.some((decorator) => {
        const expr = decorator.expression;

        // @Injectable or @Injectable()
        if (expr.type === 'Identifier' && expr.name === 'Injectable') {
          return true;
        }

        if (
          expr.type === 'CallExpression' &&
          expr.callee.type === 'Identifier' &&
          expr.callee.name === 'Injectable'
        ) {
          return true;
        }

        return false;
      });
    }

    /**
     * Find the closest ancestor class (declaration or expression)
     * @param {import('eslint').Rule.Node[]} ancestors
     * @returns {import('eslint').Rule.Node | null}
     */
    function findEnclosingClass(ancestors) {
      for (let i = ancestors.length - 1; i >= 0; i--) {
        const node = ancestors[i];
        if (node.type === 'ClassDeclaration' || node.type === 'ClassExpression') {
          return node;
        }
      }
      return null;
    }

    /**
     * Check if createEffect is inside a method/function body
     * @param {import('eslint').Rule.Node[]} ancestors
     * @param {import('eslint').Rule.Node} classNode
     * @returns {boolean}
     */
    function isInsideMethod(ancestors, classNode) {
      for (let i = ancestors.length - 1; i >= 0; i--) {
        const ancestor = ancestors[i];

        // If we hit the class, we're not inside a method
        if (ancestor === classNode) {
          return false;
        }

        // Check if we're inside a method definition
        if (ancestor.type === 'MethodDefinition') {
          return true;
        }

        // Check if we're inside a function expression/arrow function
        // that's not the direct property initializer
        if (
          ancestor.type === 'FunctionExpression' ||
          ancestor.type === 'ArrowFunctionExpression'
        ) {
          // If the parent is a PropertyDefinition, it's a property initializer (allowed)
          const parentIndex = i - 1;
          if (
            parentIndex >= 0 &&
            ancestors[parentIndex].type === 'PropertyDefinition'
          ) {
            continue;
          }
          // Otherwise, it's inside a function (not allowed)
          return true;
        }
      }
      return false;
    }

    return {
      CallExpression(node) {
        // Check if this is a createEffect call
        if (!isCreateEffectCall(node)) {
          return;
        }

        const ancestors = context.sourceCode.getAncestors(node);
        const enclosingClass = findEnclosingClass(ancestors);

        // Rule 1: createEffect must be inside a class
        if (!enclosingClass) {
          context.report({
            node,
            messageId: 'createEffectOutsideClass',
          });
          return;
        }

        // Rule 2: The class must have @Injectable decorator
        if (!hasInjectableDecorator(enclosingClass)) {
          context.report({
            node,
            messageId: 'createEffectInNonInjectableClass',
          });
          return;
        }

        // Rule 3: createEffect must be a property initializer, not inside a method
        if (isInsideMethod(ancestors, enclosingClass)) {
          context.report({
            node,
            messageId: 'createEffectInMethod',
          });
          return;
        }

        // All checks passed - this is valid usage
      },
    };
  },
};
