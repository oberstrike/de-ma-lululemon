module.exports = {
  meta: {
    type: 'suggestion',
    docs: {
      description:
        'Prefer CSS variables over hardcoded color values in component styles for consistency',
      category: 'Style Guide',
      recommended: true,
    },
    schema: [],
    messages: {
      preferVariable:
        'Prefer CSS variable over hardcoded color "{{value}}". Use var(--{{suggestion}}) instead.',
    },
  },

  create(context) {
    const colorPatterns = [
      { pattern: /#(?:[0-9a-fA-F]{3}){1,2}\b/g, type: 'hex' },
      { pattern: /rgb\s*\([^)]+\)/gi, type: 'rgb' },
      { pattern: /rgba\s*\([^)]+\)/gi, type: 'rgba' },
      { pattern: /hsl\s*\([^)]+\)/gi, type: 'hsl' },
      { pattern: /hsla\s*\([^)]+\)/gi, type: 'hsla' },
    ];

    const allowedColors = [
      '#000',
      '#000000',
      '#fff',
      '#ffffff',
      '#e50914',
      '#f40612',
      '#b20710',
      '#0a0a0a',
      '#141414',
      '#1a1a1a',
      '#232323',
      '#181818',
      '#252525',
    ];

    const colorSuggestions = {
      '#e50914': 'primary',
      '#f40612': 'primary-hover',
      '#b20710': 'primary-dark',
      '#0a0a0a': 'bg-primary',
      '#141414': 'bg-secondary',
      '#1a1a1a': 'bg-tertiary',
      '#232323': 'bg-elevated',
      '#181818': 'bg-card',
      '#252525': 'bg-card-hover',
      '#fff': 'text-primary',
      '#ffffff': 'text-primary',
      '#b3b3b3': 'text-secondary',
      '#808080': 'text-tertiary',
      '#595959': 'text-muted',
    };

    return {
      Property(node) {
        if (
          node.key.name !== 'styles' ||
          !node.value.elements ||
          node.value.type !== 'ArrayExpression'
        ) {
          return;
        }

        node.value.elements.forEach((element) => {
          if (element.type !== 'TemplateLiteral' && element.type !== 'Literal') {
            return;
          }

          const styleContent =
            element.type === 'Literal'
              ? element.value
              : element.quasis.map((q) => q.value.raw).join('');

          if (typeof styleContent !== 'string') return;

          colorPatterns.forEach(({ pattern }) => {
            const matches = styleContent.match(pattern) || [];
            matches.forEach((match) => {
              const normalizedMatch = match.toLowerCase().replace(/\s/g, '');

              if (
                allowedColors.some(
                  (c) => c.toLowerCase() === normalizedMatch || normalizedMatch.includes('var(')
                )
              ) {
                return;
              }

              if (
                normalizedMatch.startsWith('rgb') &&
                (normalizedMatch.includes('0 0 0') || normalizedMatch.includes('255 255 255'))
              ) {
                return;
              }

              const suggestion = colorSuggestions[normalizedMatch] || 'appropriate-variable';

              context.report({
                node: element,
                messageId: 'preferVariable',
                data: {
                  value: match,
                  suggestion,
                },
              });
            });
          });
        });
      },
    };
  },
};
