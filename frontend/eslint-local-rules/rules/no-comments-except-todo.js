/**
 * Custom ESLint rule to prohibit code comments except TODO comments.
 *
 * This rule enforces that code should be self-documenting and discourages
 * the use of comments that can become outdated or misleading. However, it
 * allows TODO comments to track pending work.
 *
 * Allowed patterns:
 * - // TODO: description
 * - // FIXME: description
 * - /* TODO: description *\/
 * - /** JSDoc comments *\/ (always allowed for documentation)
 *
 * Prohibited patterns:
 * - // regular comment
 * - /* regular comment *\/
 * - // Explanation of code
 *
 * @type {import('eslint').Rule.RuleModule}
 */
module.exports = {
  meta: {
    type: 'suggestion',
    docs: {
      description: 'Disallow code comments except TODO/FIXME comments',
      category: 'Best Practices',
      recommended: false,
    },
    messages: {
      noComment:
        'Code comments are not allowed. Code should be self-documenting. Use TODO/FIXME for pending work, or remove the comment.',
    },
    schema: [],
  },
  create(context) {
    const sourceCode = context.sourceCode;

    return {
      Program() {
        const comments = sourceCode.getAllComments();

        for (const comment of comments) {
          // Always allow JSDoc comments (block comments starting with **)
          if (comment.type === 'Block' && comment.value.startsWith('*')) {
            continue;
          }

          // Get the comment text (trimmed)
          const text = comment.value.trim();

          // Allow TODO and FIXME comments
          if (
            text.toUpperCase().startsWith('TODO:') ||
            text.toUpperCase().startsWith('TODO ') ||
            text.toUpperCase().startsWith('FIXME:') ||
            text.toUpperCase().startsWith('FIXME ')
          ) {
            continue;
          }

          // Report all other comments
          context.report({
            node: comment,
            messageId: 'noComment',
          });
        }
      },
    };
  },
};
