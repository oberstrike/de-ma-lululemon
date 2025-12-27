import { Page } from '@playwright/test';

export const mockEndpoints = {
  movies: {
    GET: {
      status: 200,
      body: [
        {
          id: '1',
          title: 'Test Movie 1',
          description: 'A test movie description',
          year: 2024,
          duration: 120,
          posterPath: '/assets/poster1.jpg',
          status: 'CACHED',
          categoryId: '1',
        },
        {
          id: '2',
          title: 'Test Movie 2',
          description: 'Another test movie',
          year: 2023,
          duration: 90,
          posterPath: '/assets/poster2.jpg',
          status: 'AVAILABLE',
          categoryId: '1',
        },
      ],
    },
  },
  categories: {
    GET: {
      status: 200,
      body: [
        { id: '1', name: 'Action', megaPath: '/mega/action' },
        { id: '2', name: 'Comedy', megaPath: '/mega/comedy' },
      ],
    },
  },
  'movies/1': {
    GET: {
      status: 200,
      body: {
        id: '1',
        title: 'Test Movie 1',
        description: 'A test movie description',
        year: 2024,
        duration: 120,
        posterPath: '/assets/poster1.jpg',
        status: 'CACHED',
        categoryId: '1',
      },
    },
  },
  favorites: {
    GET: { status: 200, body: ['1'] },
    POST: { status: 200, body: { success: true } },
    DELETE: { status: 200, body: { success: true } },
  },
};

export async function mockBackend(page: Page): Promise<void> {
  for (const [endpoint, methods] of Object.entries(mockEndpoints)) {
    await page.route(`**/api/v1/${endpoint}**`, async (route) => {
      const method = route.request().method() as keyof typeof methods;
      const mock = methods[method as keyof typeof methods];

      if (mock) {
        await route.fulfill({
          status: mock.status,
          contentType: 'application/json',
          body: JSON.stringify(mock.body),
        });
      } else {
        await route.continue();
      }
    });
  }
}

export const mockScenarios = {
  success: async (page: Page): Promise<void> => {
    await mockBackend(page);
  },

  serverError: async (page: Page): Promise<void> => {
    await page.route('**/api/v1/**', (route) =>
      route.fulfill({
        status: 500,
        json: { error: 'Internal Server Error' },
      })
    );
  },

  networkFailure: async (page: Page): Promise<void> => {
    await page.route('**/api/v1/**', (route) => route.abort('failed'));
  },

  slowResponse: async (page: Page, delayMs = 3000): Promise<void> => {
    await page.route('**/api/v1/**', async (route) => {
      await new Promise((r) => setTimeout(r, delayMs));
      await route.fulfill({
        status: 200,
        json: mockEndpoints.movies.GET.body,
      });
    });
  },

  emptyMovies: async (page: Page): Promise<void> => {
    await page.route('**/api/v1/movies**', (route) =>
      route.fulfill({
        status: 200,
        json: [],
      })
    );
  },
};
