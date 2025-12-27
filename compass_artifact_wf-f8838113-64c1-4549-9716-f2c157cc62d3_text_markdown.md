# Production-Ready Playwright E2E Testing in Angular 21 Monorepos

**Nx with its Gradle plugin provides the optimal monorepo foundation** for Angular + Java projects, offering native polyglot support introduced in 2024. Combined with Playwright 1.57's latest features—including Chrome for Testing, AI-assisted test generation, and enhanced parallel execution—this guide delivers a battle-tested setup for enterprise E2E testing with mocked backends and GitHub Actions CI/CD.

## Monorepo architecture: why Nx dominates for Angular + Java

For projects combining Angular frontend with Java backend, **Nx with @nx/gradle is the only viable choice** among modern monorepo tools. Turborepo, despite its popularity, exclusively supports JavaScript/TypeScript and cannot natively handle Gradle or Maven projects. The manual approach requires excessive custom configuration without intelligent caching or dependency detection.

Nx's 2024 Gradle plugin provides genuine polyglot capabilities: automatic task detection, cross-language caching, and unified `nx build` commands across both ecosystems. The plugin integrates Java projects into Nx's dependency graph, enabling `nx affected` to work across language boundaries.

| Feature | Nx | Turborepo | Manual |
|---------|-----|-----------|--------|
| Java native support | ✅ @nx/gradle | ❌ npm scripts only | ⚠️ Custom config |
| Cross-language caching | ✅ Full | ❌ None | ❌ None |
| Dependency graph | ✅ Automatic | ⚠️ package.json only | ❌ Manual |
| Code generation | ✅ Extensive | ❌ None | ❌ None |

### Optimal folder structure

```
my-monorepo/
├── apps/
│   ├── angular-app/                 # Angular 21 frontend
│   │   ├── src/
│   │   ├── project.json
│   │   └── tsconfig.json
│   ├── java-backend/                # Spring Boot API
│   │   ├── src/main/java/
│   │   ├── build.gradle
│   │   └── project.json             # Nx project config
│   └── e2e/                         # Playwright tests
│       ├── tests/
│       │   ├── auth/
│       │   ├── dashboard/
│       │   └── features/
│       ├── pages/                   # Page Object Models
│       ├── fixtures/                # Test fixtures & mocks
│       ├── playwright.config.ts
│       └── project.json
├── libs/
│   ├── shared-ui/                   # Angular components
│   ├── shared-util/                 # TypeScript utilities
│   └── java-common/                 # Shared Java code
├── gradle/
│   └── libs.versions.toml
├── nx.json
├── package.json
├── settings.gradle
└── tsconfig.base.json
```

### Nx configuration for polyglot setup

**nx.json** - Core configuration:
```json
{
  "$schema": "./node_modules/nx/schemas/nx-schema.json",
  "plugins": [
    {
      "plugin": "@nx/gradle",
      "options": {
        "ciTestTargetName": "test-ci",
        "ciBuildTargetName": "build-ci"
      }
    }
  ],
  "tasksRunnerOptions": {
    "default": {
      "runner": "nx/tasks-runners/default",
      "options": {
        "cacheableOperations": ["build", "test", "lint", "e2e"]
      }
    }
  },
  "targetDefaults": {
    "e2e": {
      "dependsOn": ["^build"],
      "cache": true
    }
  }
}
```

**apps/java-backend/project.json** - Java project config:
```json
{
  "name": "java-backend",
  "targets": {
    "build": {
      "executor": "@nx/gradle:gradle",
      "options": {
        "task": "build"
      }
    },
    "serve": {
      "executor": "@nx/gradle:gradle",
      "options": {
        "task": "bootRun"
      },
      "continuous": true
    }
  }
}
```

**settings.gradle** - Gradle integration:
```groovy
rootProject.name = 'my-monorepo'
include 'apps:java-backend'
include 'libs:java-common'

// Nx Gradle plugin for project graph
plugins {
    id "dev.nx.gradle.project-graph" version "0.1.0"
}
```

---

## Angular 21 and Playwright 1.57 integration

### What's new in Playwright 1.57

Playwright's latest release introduces several production-critical features. **Chrome for Testing** replaces plain Chromium, providing more accurate browser behavior matching. The **Speedboard** tab in HTML reports surfaces slow tests for optimization. The new `webServer.wait` field enables regex-based server readiness detection with dynamic port extraction.

Most significantly, **Playwright Test Agents** offer AI-powered test creation: the planner explores apps and produces test plans, the generator transforms plans into test files, and the healer automatically repairs failing tests.

Browser versions: Chromium 143.0.7499.4, Firefox 142.0.1, WebKit 26.0.

### Setup methods

**Option A: Angular CLI schematics (recommended)**
```bash
ng add playwright-ng-schematics
npm run e2e
```

**Option B: Manual installation**
```bash
npm install -D @playwright/test
npx playwright install --with-deps
```

### Production playwright.config.ts

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  testMatch: '**/*.spec.ts',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  
  reporter: [
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['list'],
    ...(process.env.CI ? [['blob', { outputDir: 'blob-report' }] as const] : []),
  ],

  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:4200',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 10000,
    navigationTimeout: 30000,
    testIdAttribute: 'data-testid',
  },

  timeout: 30000,
  
  expect: {
    timeout: 5000,
    toHaveScreenshot: { maxDiffPixelRatio: 0.05 },
  },

  projects: [
    { name: 'setup', testMatch: /.*\.setup\.ts/ },
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/user.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
        storageState: 'playwright/.auth/user.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'webkit',
      use: { 
        ...devices['Desktop Safari'],
        storageState: 'playwright/.auth/user.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
      dependencies: ['setup'],
    },
  ],

  webServer: {
    command: 'npm run start',
    url: 'http://localhost:4200',
    timeout: 120 * 1000,
    reuseExistingServer: !process.env.CI,
    stdout: 'pipe',
    stderr: 'pipe',
  },
});
```

---

## Mocked backend strategies: route interception wins for Java APIs

When testing Angular applications against a Java REST backend, two primary mocking approaches exist: **Playwright's native `page.route()`** and **Mock Service Worker (MSW)**. For E2E testing against Java REST APIs specifically, Playwright's built-in interception is the recommended choice.

### Comparison matrix

| Aspect | Playwright `page.route()` | MSW |
|--------|---------------------------|-----|
| Setup complexity | Zero config, built-in | Requires packages + fixtures |
| GraphQL support | Manual | First-class |
| Mock reusability | E2E tests only | Unit, E2E, Storybook, dev |
| HAR file support | Native `routeFromHAR()` | Via ecosystem package |
| Learning curve | Low | Medium |
| Dependencies | None | msw, @msw/playwright |

### Playwright route interception implementation

**Centralized mock configuration:**
```typescript
// fixtures/java-api-mocks.ts
import { Page } from '@playwright/test';

export const mockEndpoints = {
  users: {
    GET: { status: 200, body: [{ id: 1, name: 'John Doe', email: 'john@example.com' }] },
    POST: { status: 201, body: { id: 2, message: 'User created' } },
  },
  orders: {
    GET: { status: 200, body: [{ id: 'ORD-001', total: 99.99, status: 'pending' }] },
    POST: { status: 201, body: { orderId: 'ORD-002' } },
  },
  products: {
    GET: { status: 200, body: [{ id: 1, name: 'Widget', price: 29.99 }] },
  },
};

export async function mockJavaBackend(page: Page, overrides: Partial<typeof mockEndpoints> = {}) {
  const mocks = { ...mockEndpoints, ...overrides };
  
  for (const [endpoint, methods] of Object.entries(mocks)) {
    await page.route(`**/api/v1/${endpoint}**`, async route => {
      const method = route.request().method() as keyof typeof methods;
      const mock = methods[method];
      
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
```

**Scenario-based mocking for edge cases:**
```typescript
// fixtures/mock-scenarios.ts
import { Page } from '@playwright/test';

export const mockScenarios = {
  success: async (page: Page) => {
    await page.route('**/api/v1/users', route => 
      route.fulfill({ status: 200, json: [{ id: 1, name: 'John' }] }));
  },
  
  serverError: async (page: Page) => {
    await page.route('**/api/v1/users', route => 
      route.fulfill({ status: 500, json: { error: 'Internal Server Error' } }));
  },
  
  unauthorized: async (page: Page) => {
    await page.route('**/api/v1/**', route => 
      route.fulfill({ status: 401, json: { error: 'Token expired' } }));
  },
  
  slowResponse: async (page: Page, delayMs = 3000) => {
    await page.route('**/api/v1/users', async route => {
      await new Promise(r => setTimeout(r, delayMs));
      await route.fulfill({ status: 200, json: [{ id: 1, name: 'John' }] });
    });
  },
  
  networkFailure: async (page: Page) => {
    await page.route('**/api/v1/**', route => route.abort('failed'));
  },
};
```

**HAR file recording from real backend:**
```bash
# Record actual Java backend responses
npx playwright open --save-har=backend.har --save-har-glob="**/api/**" http://localhost:4200
```

```typescript
// Replay in tests for realistic data
test('uses recorded backend responses', async ({ page }) => {
  await page.routeFromHAR('./fixtures/backend.har', {
    url: '**/api/**',
    update: false,
  });
  await page.goto('/dashboard');
});
```

### When to choose MSW instead

Consider MSW if you need to **share mocks between unit tests, integration tests, Storybook, and E2E tests**. MSW also provides first-class GraphQL support. The `@msw/playwright` official package enables integration:

```typescript
// Only if you need shared mocks across test types
import { test as testBase } from '@playwright/test';
import { createNetworkFixture } from '@msw/playwright';
import { handlers } from '../mocks/handlers';

export const test = testBase.extend({
  network: createNetworkFixture({ initialHandlers: handlers }),
});
```

---

## Parallel execution and sharding configuration

Playwright distributes tests across **worker processes**—independent OS processes with their own browser instances. Test files run in parallel across workers, while tests within a file run sequentially by default.

### Worker and parallelization settings

```typescript
// playwright.config.ts
export default defineConfig({
  // Enable full parallelism (tests within files also parallel)
  fullyParallel: true,
  
  // Workers configuration
  workers: process.env.CI ? 1 : undefined,  // 1 for CI stability
  // Alternative: workers: '50%' for half of CPU cores
  
  // Fail fast to save CI resources
  maxFailures: process.env.CI ? 10 : undefined,
});
```

**Parallelizing specific test blocks:**
```typescript
test.describe('Independent tests', () => {
  test.describe.configure({ mode: 'parallel' });
  
  test('runs in parallel 1', async ({ page }) => { /* ... */ });
  test('runs in parallel 2', async ({ page }) => { /* ... */ });
});
```

### Test isolation for parallel safety

```typescript
// fixtures/isolated-data.ts
import { test as base } from '@playwright/test';

export const test = base.extend<{}, { uniqueUserId: string }>({
  uniqueUserId: [async ({}, use, workerInfo) => {
    // Generate unique data per worker to prevent conflicts
    const userId = `user-${workerInfo.workerIndex}-${Date.now()}`;
    await use(userId);
  }, { scope: 'worker' }],
});
```

**Critical isolation rules:**
- Never share state between parallel tests
- Use `workerIndex` for unique test data per worker
- Avoid hardcoded ports—allocate dynamically
- Each test receives a fresh browser context automatically

---

## GitHub Actions CI/CD with sharding

### Complete sharded workflow

```yaml
# .github/workflows/playwright.yml
name: Playwright Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  playwright-tests:
    timeout-minutes: 60
    runs-on: ubuntu-latest
    container:
      image: mcr.microsoft.com/playwright:v1.57.0-noble
      options: --user 1001
    strategy:
      fail-fast: false
      matrix:
        shardIndex: [1, 2, 3, 4]
        shardTotal: [4]
    
    steps:
      - uses: actions/checkout@v5
      
      - uses: actions/setup-node@v6
        with:
          node-version: lts/*
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Run Playwright tests
        run: npx playwright test --shard=${{ matrix.shardIndex }}/${{ matrix.shardTotal }}
      
      - name: Upload blob report
        if: ${{ !cancelled() }}
        uses: actions/upload-artifact@v4
        with:
          name: blob-report-${{ matrix.shardIndex }}
          path: blob-report
          retention-days: 1

  merge-reports:
    if: ${{ !cancelled() }}
    needs: [playwright-tests]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v5
      
      - uses: actions/setup-node@v6
        with:
          node-version: lts/*
      
      - name: Install dependencies
        run: npm ci
      
      - name: Download blob reports
        uses: actions/download-artifact@v4
        with:
          path: all-blob-reports
          pattern: blob-report-*
          merge-multiple: true
      
      - name: Merge into HTML Report
        run: npx playwright merge-reports --reporter html ./all-blob-reports
      
      - name: Upload HTML report
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report-${{ github.run_attempt }}
          path: playwright-report
          retention-days: 14
```

### Caching strategy

Playwright officially recommends **not caching browser binaries**—restore time equals download time, and OS dependencies still require installation. Using the official Docker image eliminates browser installation entirely.

If caching is required for non-containerized runs:
```yaml
- name: Get Playwright version
  id: pw-version
  run: echo "version=$(npm pkg get devDependencies.@playwright/test | tr -d '\"')" >> $GITHUB_OUTPUT

- name: Cache Playwright browsers
  uses: actions/cache@v4
  id: playwright-cache
  with:
    path: ~/.cache/ms-playwright
    key: playwright-${{ runner.os }}-${{ steps.pw-version.outputs.version }}

- name: Install Playwright browsers
  if: steps.playwright-cache.outputs.cache-hit != 'true'
  run: npx playwright install --with-deps

- name: Install OS dependencies only
  if: steps.playwright-cache.outputs.cache-hit == 'true'
  run: npx playwright install-deps
```

---

## Page Object Model with fixtures

The modern Playwright pattern combines Page Objects with **custom fixtures**, eliminating constructor boilerplate and providing automatic lifecycle management.

### Page Object implementation

```typescript
// pages/login.page.ts
import { Page, Locator, expect } from '@playwright/test';

export class LoginPage {
  readonly usernameInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;
  readonly errorAlert: Locator;

  constructor(private readonly page: Page) {
    this.usernameInput = page.getByLabel('Username');
    this.passwordInput = page.getByLabel('Password');
    this.submitButton = page.getByRole('button', { name: 'Sign in' });
    this.errorAlert = page.getByRole('alert');
  }

  async goto() {
    await this.page.goto('/login');
  }

  async login(username: string, password: string) {
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }

  async expectError(message: string) {
    await expect(this.errorAlert).toContainText(message);
  }
}
```

```typescript
// pages/dashboard.page.ts
import { Page, Locator } from '@playwright/test';

export class DashboardPage {
  readonly welcomeHeading: Locator;
  readonly userMenu: Locator;
  readonly settingsLink: Locator;

  constructor(private readonly page: Page) {
    this.welcomeHeading = page.getByRole('heading', { name: /welcome/i });
    this.userMenu = page.getByTestId('user-menu');
    this.settingsLink = page.getByRole('link', { name: 'Settings' });
  }

  async navigateToSettings() {
    await this.settingsLink.click();
  }
}
```

### Fixture configuration

```typescript
// fixtures/test-base.ts
import { test as base, expect } from '@playwright/test';
import { LoginPage } from '../pages/login.page';
import { DashboardPage } from '../pages/dashboard.page';
import { mockJavaBackend } from './java-api-mocks';

type TestFixtures = {
  loginPage: LoginPage;
  dashboardPage: DashboardPage;
  mockBackend: void;
};

export const test = base.extend<TestFixtures>({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page));
  },

  dashboardPage: async ({ page }, use) => {
    await use(new DashboardPage(page));
  },

  mockBackend: [async ({ page }, use) => {
    await mockJavaBackend(page);
    await use();
  }, { auto: true }],  // Automatically applied to all tests
});

export { expect };
```

### Test implementation

```typescript
// tests/auth/login.spec.ts
import { test, expect } from '../../fixtures/test-base';
import { mockScenarios } from '../../fixtures/mock-scenarios';

test.describe('Login functionality', () => {
  test('successful login redirects to dashboard', async ({ loginPage, dashboardPage, page }) => {
    await loginPage.goto();
    await loginPage.login('testuser', 'password123');
    
    await expect(page).toHaveURL('/dashboard');
    await expect(dashboardPage.welcomeHeading).toBeVisible();
  });

  test('invalid credentials show error message', async ({ loginPage, page }) => {
    await mockScenarios.unauthorized(page);
    
    await loginPage.goto();
    await loginPage.login('invalid', 'wrong');
    
    await loginPage.expectError('Invalid credentials');
  });

  test('shows loading state during slow response', async ({ loginPage, page }) => {
    await mockScenarios.slowResponse(page, 2000);
    
    await loginPage.goto();
    await loginPage.login('testuser', 'password123');
    
    await expect(page.getByTestId('loading-spinner')).toBeVisible();
  });
});
```

---

## Authentication handling with mocked backends

### Setup project pattern

```typescript
// auth.setup.ts
import { test as setup, expect } from '@playwright/test';
import path from 'path';

const authFile = path.join(__dirname, 'playwright/.auth/user.json');

setup('authenticate', async ({ page }) => {
  // Mock the auth endpoint for setup
  await page.route('**/api/v1/auth/login', route => 
    route.fulfill({
      status: 200,
      json: { 
        token: 'mock-jwt-token-abc123',
        user: { id: 1, name: 'Test User', role: 'admin' }
      },
    })
  );
  
  await page.goto('/login');
  await page.getByLabel('Username').fill('testuser');
  await page.getByLabel('Password').fill('password123');
  await page.getByRole('button', { name: 'Sign in' }).click();
  
  await page.waitForURL('/dashboard');
  await expect(page.getByTestId('user-menu')).toBeVisible();
  
  // Save session state (includes cookies, localStorage, IndexedDB)
  await page.context().storageState({ 
    path: authFile,
    indexedDB: true,  // For Firebase/modern auth tokens
  });
});
```

### Programmatic session state

For fully mocked scenarios without UI login:
```typescript
// fixtures/mock-auth-state.ts
import { chromium } from '@playwright/test';

export async function createMockAuthState(outputPath: string) {
  const browser = await chromium.launch();
  const context = await browser.newContext();
  
  // Set authentication cookies
  await context.addCookies([{
    name: 'session-token',
    value: 'mock-session-token-xyz789',
    domain: 'localhost',
    path: '/',
    httpOnly: true,
    secure: false,
    sameSite: 'Lax',
    expires: Math.floor(Date.now() / 1000) + 86400 * 30,
  }]);
  
  // Set localStorage tokens
  await context.addInitScript(() => {
    localStorage.setItem('auth_token', 'mock-jwt-token');
    localStorage.setItem('user', JSON.stringify({ id: 1, name: 'Test User' }));
  });
  
  await context.storageState({ path: outputPath });
  await browser.close();
}
```

---

## Debugging and trace viewing

### Development debugging tools

**UI Mode** provides time-travel debugging with DOM snapshots:
```bash
npx playwright test --ui
```

**Inspector** enables step-through debugging:
```bash
npx playwright test --debug
```

**Programmatic pause:**
```typescript
test('debug specific point', async ({ page }) => {
  await page.goto('/dashboard');
  await page.pause();  // Opens Inspector at this point
  await page.getByRole('button', { name: 'Submit' }).click();
});
```

### Trace configuration

```typescript
// playwright.config.ts
export default defineConfig({
  use: {
    trace: 'on-first-retry',      // Capture on failures
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  outputDir: 'test-results',
});
```

**Viewing traces:**
```bash
npx playwright show-trace test-results/trace.zip
# Or upload to https://trace.playwright.dev
```

### VS Code integration

Install the **Playwright Test for VS Code** extension for:
- Run/debug tests directly from editor gutter
- Breakpoint support in test files
- Live browser preview with "Show Browser"
- Integrated trace viewer

---

## Locator strategies for Angular components

### Priority hierarchy

Use **role-based locators** first for accessibility and resilience:

```typescript
// ✅ Recommended: User-facing locators
await page.getByRole('button', { name: 'Submit' }).click();
await page.getByLabel('Email').fill('user@example.com');
await page.getByRole('heading', { name: 'Dashboard' }).isVisible();

// ✅ For Angular Material/complex components
await page.getByTestId('country-select').click();

// ❌ Avoid: Implementation-dependent selectors
await page.locator('.btn-primary').click();
await page.locator('#email-input').fill('user@example.com');
```

### Chaining for scoped selection

```typescript
// Find button within specific card
const productCard = page.getByRole('article', { name: 'Premium Widget' });
await productCard.getByRole('button', { name: 'Add to Cart' }).click();

// Filter rows by content
await page
  .getByRole('row')
  .filter({ has: page.getByText('Active') })
  .getByRole('button', { name: 'Edit' })
  .click();
```

---

## Conclusion

This guide provides a complete, production-ready foundation for Playwright E2E testing in Angular 21 monorepos. The key architectural decisions—**Nx for polyglot monorepo management**, **Playwright's native route interception for Java API mocking**, and **sharded GitHub Actions workflows**—optimize for both developer experience and CI performance.

Critical implementation takeaways:
- Use `fullyParallel: true` with test-level sharding for balanced CI distribution
- Employ the fixture-based Page Object pattern for maintainable test code  
- Configure traces as `on-first-retry` to capture debugging data only when needed
- Deploy via `mcr.microsoft.com/playwright:v1.57.0-noble` containers to eliminate browser installation overhead
- Leverage blob reporters with merge-reports for unified HTML reports across shards

The mocked backend approach using Playwright's `page.route()` provides zero-dependency API simulation suitable for Java REST APIs, while the scenario-based mock configuration enables comprehensive coverage of success paths, error handling, and edge cases.