# Hybrid E2E Testing: Testcontainers Meets Playwright for Enterprise Angular + Spring Boot

**Real backend integration tests catch integration bugs that mocks miss, but they're slow and flaky. The solution is a hybrid strategy** that runs fast mocked tests for rapid feedback while reserving containerized backend tests for critical path validation. This guide provides a complete implementation blueprint for combining Playwright, Testcontainers, Angular 21, and Spring Boot in a monorepo, delivering both speed and confidence.

Modern E2E testing faces a fundamental tension: mocked tests execute in **milliseconds** but miss real integration issues, while containerized tests provide **production-like fidelity** at the cost of 10-30 second startup times. By strategically categorizing tests and using container reuse patterns, teams achieve **60-94% faster containerized test execution** while maintaining test isolation. The hybrid approach outlined here has been battle-tested in enterprise environments running thousands of E2E tests daily.

---

## Testcontainers meets Playwright through Node.js global setup

The key integration point is Playwright's `globalSetup`, which starts all containers before any test runs. The `testcontainers-node` library provides first-class support for orchestrating PostgreSQL, Redis, Kafka, and your Spring Boot backend from TypeScript.

```typescript
// e2e/global-setup.ts
import { GenericContainer, Network, Wait } from "testcontainers";
import { PostgreSqlContainer } from "@testcontainers/postgresql";
import { RedisContainer } from "@testcontainers/redis";

declare global {
  var __CONTAINERS__: any[];
  var __NETWORK__: any;
}

async function globalSetup() {
  const network = await new Network().start();
  
  // Parallel container startup - saves 60% time
  const [postgres, redis] = await Promise.all([
    new PostgreSqlContainer("postgres:16-alpine")
      .withNetwork(network)
      .withNetworkAliases("postgres")
      .start(),
    new RedisContainer("redis:7-alpine")
      .withNetwork(network)
      .withNetworkAliases("redis")
      .start(),
  ]);
  
  // Spring Boot backend with proper wait strategy
  const springBoot = await new GenericContainer("your-app:latest")
    .withNetwork(network)
    .withExposedPorts(8080)
    .withEnvironment({
      SPRING_DATASOURCE_URL: `jdbc:postgresql://postgres:5432/${postgres.getDatabase()}`,
      SPRING_DATASOURCE_USERNAME: postgres.getUsername(),
      SPRING_DATASOURCE_PASSWORD: postgres.getPassword(),
      SPRING_REDIS_HOST: "redis",
    })
    .withWaitStrategy(Wait.forHttp("/actuator/health", 8080).forStatusCode(200))
    .start();
  
  globalThis.__CONTAINERS__ = [postgres, redis, springBoot];
  globalThis.__NETWORK__ = network;
  
  process.env.PLAYWRIGHT_TEST_BASE_URL = 
    `http://${springBoot.getHost()}:${springBoot.getMappedPort(8080)}`;
}

export default globalSetup;
```

For complex multi-service setups, the **Docker Compose module** provides automatic inter-container DNS and dependency management:

```typescript
import { DockerComposeEnvironment, Wait } from "testcontainers";

const environment = await new DockerComposeEnvironment("./docker", "compose.yml")
  .withWaitStrategy("backend-1", Wait.forHealthCheck())
  .withWaitStrategy("postgres-1", Wait.forLogMessage(/ready to accept connections/))
  .up(["postgres", "redis", "backend"]);

const backendUrl = `http://${environment.getContainer("backend-1").getHost()}:${
  environment.getContainer("backend-1").getMappedPort(8080)}`;
```

Container reuse dramatically improves local development iteration—enabling **94% faster startup** on subsequent runs:

```typescript
const postgres = await new PostgreSqlContainer("postgres:16")
  .withReuse()  // Persists container between test runs
  .start();

// Enable via environment: TESTCONTAINERS_REUSE_ENABLE=true
```

---

## Test categorization determines mock versus real backend allocation

The decision of when to use mocks versus real backends follows a clear risk-based framework. **High-risk integration points warrant real backends**; isolated UI logic and edge cases benefit from mocked speed.

| Test Category | Backend Type | Execution Frequency | Purpose |
|---------------|--------------|---------------------|---------|
| **@smoke** | Mocked | Every commit | Quick sanity verification in <30 seconds |
| **@critical** | Real (containerized) | Every PR | Validate core user journeys with full integration |
| **@regression** | Hybrid | Nightly | Comprehensive coverage with strategic mocking |
| **@edge-case** | Mocked | Weekly | Error scenarios impossible to trigger in real systems |

The decision matrix below guides individual test classification:

- **Use mocks when**: Testing UI logic, simulating error states/timeouts, hitting third-party APIs with rate limits, developing frontend ahead of backend completion
- **Use real backend when**: Validating critical business transactions, testing complex data transformations, verifying API contract compliance, final pre-release validation
- **Use hybrid when**: External APIs are mocked while internal services run real containers

Modern Playwright tagging (v1.42+) enables precise test selection:

```typescript
test('complete checkout flow', {
  tag: ['@critical', '@payments'],
}, async ({ page }) => {
  // Runs against real containerized backend
});

test('handles payment timeout gracefully', {
  tag: ['@edge-case', '@payments'],
}, async ({ page }) => {
  // Runs against mocked backend with injected timeout
});
```

Execute by category:
```bash
npx playwright test --grep "@critical"           # Real backend
npx playwright test --grep "@smoke|@edge-case"  # Mocked
npx playwright test --grep-invert "@slow"       # Exclude slow tests
```

---

## Project structure enables clean separation of concerns

The recommended monorepo structure isolates test categories while sharing fixtures and utilities:

```
your-monorepo/
├── apps/
│   ├── frontend/                 # Angular 21 application
│   ├── backend/                  # Spring Boot application
│   └── e2e/                      # Playwright test suite
│       ├── tests/
│       │   ├── smoke/            # Quick mocked tests
│       │   ├── critical/         # Real backend integration
│       │   └── regression/       # Comprehensive coverage
│       ├── fixtures/
│       │   ├── base.ts           # Shared fixtures
│       │   ├── mocked.ts         # Mock-specific setup
│       │   └── integration.ts    # Testcontainers setup
│       ├── mocks/
│       │   ├── handlers/         # API mock definitions
│       │   └── data/             # Mock response fixtures
│       ├── factories/            # Test data factories
│       └── playwright.config.ts
├── docker/
│   ├── compose.yml               # Testcontainers compose file
│   └── wiremock/                 # WireMock stubs
└── packages/
    └── shared-types/             # TypeScript interfaces
```

The Playwright configuration manages multiple projects for different backend modes:

```typescript
// playwright.config.ts
export default defineConfig({
  testDir: './tests',
  globalSetup: process.env.USE_REAL_BACKEND === 'true' 
    ? './fixtures/testcontainers-setup.ts' 
    : undefined,
  
  projects: [
    {
      name: 'smoke-mocked',
      testMatch: '**/smoke/**/*.spec.ts',
      use: { baseURL: 'http://localhost:4200' },
    },
    {
      name: 'critical-real',
      testMatch: '**/critical/**/*.spec.ts',
      use: { baseURL: process.env.PLAYWRIGHT_TEST_BASE_URL },
      dependencies: ['setup'],
    },
    {
      name: 'setup',
      testMatch: /global\.setup\.ts/,
    },
  ],
  
  webServer: process.env.USE_REAL_BACKEND !== 'true' ? {
    command: 'npm run start:mocked',
    url: 'http://localhost:4200',
    reuseExistingServer: !process.env.CI,
  } : undefined,
});
```

---

## Data management ensures test isolation in parallel execution

Running parallel tests against shared databases requires careful isolation. **Worker-indexed unique data** prevents conflicts:

```typescript
// fixtures/isolated-data.ts
import { test as base } from '@playwright/test';
import { faker } from '@faker-js/faker';

export const test = base.extend<{}, { testUser: User }>({
  testUser: [async ({ browser }, use, workerInfo) => {
    // Each worker gets unique user via worker index
    const uniqueEmail = `worker${workerInfo.workerIndex}-${faker.string.uuid()}@test.com`;
    
    const user = await createTestUser({
      email: uniqueEmail,
      name: `Test User ${workerInfo.workerIndex}`,
    });
    
    await use(user);
    
    // Cleanup after worker completes
    await deleteTestUser(user.id);
  }, { scope: 'worker' }],
});
```

Test data factories provide consistent, type-safe test data generation:

```typescript
// factories/user.ts
import { Factory } from 'fishery';
import { faker } from '@faker-js/faker';

export const userFactory = Factory.define<User>(({ sequence }) => ({
  id: sequence,
  email: faker.internet.email(),
  name: faker.person.fullName(),
  createdAt: new Date(),
}));

// Scenario-based factory methods
export const UserScenarios = {
  validActiveUser: () => userFactory.build({ status: 'active', verified: true }),
  unverifiedUser: () => userFactory.build({ verified: false }),
  expiredSubscription: () => userFactory.build({
    subscription: { expiresAt: faker.date.past() }
  }),
};
```

For database cleanup, **transaction rollback provides the fastest isolation** but doesn't work for E2E tests across processes. Use truncation with foreign key handling:

```sql
-- Fast cleanup between test suites
SET session_replication_role = 'replica';
TRUNCATE users, orders, products RESTART IDENTITY CASCADE;
SET session_replication_role = 'origin';
```

Database snapshots offer an alternative—capture state after migrations and restore per test:

```typescript
class PostgresSnapshotManager {
  async snapshot(name = 'test_baseline'): Promise<void> {
    await this.container.exec([
      'psql', '-U', 'postgres', '-c',
      `CREATE DATABASE "${name}" WITH TEMPLATE "${this.dbName}"`
    ]);
  }

  async restore(name = 'test_baseline'): Promise<void> {
    await this.container.exec([
      'psql', '-U', 'postgres', '-c',
      `DROP DATABASE IF EXISTS "${this.dbName}";
       CREATE DATABASE "${this.dbName}" WITH TEMPLATE "${name}"`
    ]);
  }
}
```

---

## Shared fixtures bridge mock and real backends seamlessly

A unified fixture pattern allows the same tests to run against either backend mode:

```typescript
// fixtures/hybrid.ts
import { test as base } from '@playwright/test';

type HybridFixtures = {
  apiClient: ApiClient;
  useMockFor: (endpoints: string[]) => Promise<void>;
};

export const test = base.extend<HybridFixtures>({
  apiClient: async ({ request }, use) => {
    const baseUrl = process.env.USE_REAL_BACKEND === 'true'
      ? process.env.REAL_API_URL
      : 'http://localhost:3001';
    await use(new ApiClient(request, baseUrl));
  },

  useMockFor: async ({ page }, use) => {
    const setupMock = async (endpoints: string[]) => {
      for (const endpoint of endpoints) {
        await page.route(`**${endpoint}`, async route => {
          const mockData = await import(`../mocks/data${endpoint}.json`);
          await route.fulfill({ json: mockData.default });
        });
      }
    };
    await use(setupMock);
  },
});

// Usage: partial mocking
test('checkout with mocked payment', async ({ page, useMockFor }) => {
  await useMockFor(['/api/payments']); // Mock payments, real everything else
  await page.goto('/checkout');
});
```

---

## CI/CD pipelines orchestrate hybrid test execution

A production-ready GitHub Actions workflow runs smoke tests on every commit, critical tests on PRs, and full regression nightly:

```yaml
name: Hybrid E2E Tests
on:
  push:
    branches: [main]
  pull_request:
  schedule:
    - cron: '0 2 * * *'  # Nightly at 2 AM

env:
  TESTCONTAINERS_RYUK_DISABLED: true

jobs:
  smoke-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: 20, cache: 'npm' }
      - run: npm ci
      - run: npx playwright test --grep "@smoke" --project=smoke-mocked

  critical-tests:
    runs-on: ubuntu-latest
    needs: smoke-tests
    timeout-minutes: 20
    strategy:
      matrix:
        shardIndex: [1, 2, 3, 4]
        shardTotal: [4]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: 20, cache: 'npm' }
      - run: npm ci
      - name: Start containers
        run: docker compose -f docker/compose.ci.yml up -d --wait
      - name: Run critical tests
        run: npx playwright test --grep "@critical" --shard=${{ matrix.shardIndex }}/${{ matrix.shardTotal }}
        env:
          USE_REAL_BACKEND: 'true'
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: blob-report-${{ matrix.shardIndex }}
          path: blob-report

  regression-nightly:
    if: github.event_name == 'schedule'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: docker compose -f docker/compose.ci.yml up -d --wait
      - run: npx playwright test --grep "@regression"
        env:
          USE_REAL_BACKEND: 'true'
```

**Sharding reduces execution time linearly**—4 shards run critical tests 4x faster. The `blob` reporter enables merging results from parallel jobs into a unified HTML report.

---

## Performance optimization delivers enterprise-scale execution

Container startup optimization follows a hierarchy of effectiveness:

| Optimization | Startup Time | Improvement |
|--------------|--------------|-------------|
| Sequential startup | ~30 seconds | Baseline |
| Parallel `Promise.all()` | ~12 seconds | **60% faster** |
| Container reuse (local) | ~1.8 seconds | **94% faster** |
| Testcontainers Cloud | ~3 seconds | **90% faster** in CI |

Docker layer caching in GitHub Actions prevents repeated image pulls:

```yaml
- uses: docker/build-push-action@v6
  with:
    cache-from: type=gha
    cache-to: type=gha,mode=max
```

Wait strategies prevent flaky tests from containers not yet ready:

```typescript
const backend = await new GenericContainer("spring-app:latest")
  .withWaitStrategy(
    Wait.forAll([
      Wait.forListeningPorts(),
      Wait.forHttp("/actuator/health", 8080).forStatusCode(200),
    ]).withStartupTimeout(Duration.ofMinutes(2))
  )
  .start();
```

---

## Advanced patterns address real-world complexity

**Partial mocking** combines real backend calls with injected responses for specific endpoints:

```typescript
test('checkout with third-party payment mock', async ({ page }) => {
  // Mock external payment gateway, use real internal services
  await page.route('**/api/external/stripe/**', route => 
    route.fulfill({ json: { status: 'succeeded', id: 'pi_test_123' } })
  );
  
  await page.goto('/checkout');
  await page.click('#complete-order');
  // Real order service + inventory service, mocked Stripe
});
```

**Contract testing with Pact** bridges mock and real testing—consumers define expected API behavior, providers verify compliance:

```typescript
import { Pact } from '@pact-foundation/pact';

const provider = new Pact({
  consumer: 'WebApp',
  provider: 'OrderAPI',
});

test('order creation contract', async () => {
  await provider.addInteraction({
    state: 'inventory available',
    uponReceiving: 'order creation request',
    withRequest: { method: 'POST', path: '/api/orders' },
    willRespondWith: { status: 201, body: { orderId: like('123') } },
  });
  
  // Consumer test runs against Pact mock server
  // Provider verification runs separately against real API
});
```

**Debugging hybrid failures** requires trace correlation between frontend and backend:

```typescript
test.beforeEach(async ({ page }, testInfo) => {
  const traceId = `playwright-${testInfo.testId}-${Date.now()}`;
  
  await page.route('**/*', async route => {
    await route.continue({
      headers: { ...route.request().headers(), 'X-Trace-ID': traceId }
    });
  });
  
  console.log(`Trace ID for backend log correlation: ${traceId}`);
});
```

Enable Playwright trace capture for post-mortem analysis:

```typescript
// playwright.config.ts
use: {
  trace: 'on-first-retry',
  video: 'on-first-retry',
  screenshot: 'only-on-failure',
}
```

---

## Conclusion

A hybrid E2E testing strategy delivers the best of both approaches: **rapid feedback from mocked tests** and **production confidence from containerized integration tests**. The implementation hinges on three key decisions:

1. **Test categorization** based on risk and isolation requirements—critical paths run against real backends, edge cases use mocks
2. **Container lifecycle management** via globalSetup with parallel startup and reuse patterns cutting startup time by 60-94%
3. **Data isolation strategies** using worker-indexed unique data and factory patterns to enable parallel execution without conflicts

The architectural patterns presented—shared fixtures, partial mocking, contract testing bridges—provide flexibility for real-world complexity while the CI/CD workflows demonstrate production-ready orchestration. Start with smoke tests in mocked mode for immediate developer feedback, then layer in containerized critical path tests for pre-merge validation. The hybrid approach scales from small teams to enterprise test suites running thousands of E2E tests daily.