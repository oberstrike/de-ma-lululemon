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
      description: 'Disallow createEffect outside @Injectable services or in improper locations',
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
     * Check if createEffect is inside a method/function body or improperly wrapped
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
        if (ancestor.type === 'FunctionExpression' || ancestor.type === 'ArrowFunctionExpression') {
          const parentIndex = i - 1;
          const parent = parentIndex >= 0 ? ancestors[parentIndex] : null;

          // If parent is PropertyDefinition, check if the function is the DIRECT value
          if (parent && parent.type === 'PropertyDefinition') {
            // If the arrow function's body contains createEffect, it's wrapped (INVALID)
            // Valid:   load$ = createEffect(...)
            // Invalid: load$ = () => createEffect(...)
            //
            // We detect this by checking if there's a CallExpression between
            // the PropertyDefinition and createEffect, which means createEffect
            // is inside the arrow function body, not the direct property value
            const grandparentIndex = i - 2;
            const grandparent = grandparentIndex >= 0 ? ancestors[grandparentIndex] : null;

            // If createEffect's CallExpression parent is NOT the property definition,
            // then it's nested inside the arrow function (invalid)
            if (grandparent && grandparent.type !== 'PropertyDefinition') {
              return true; // Wrapped in arrow function - invalid
            }

            // The arrow function itself is the property initializer value,
            // and createEffect is called directly as an argument - this is valid
            // Example: load$ = createEffect(() => ...)
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
