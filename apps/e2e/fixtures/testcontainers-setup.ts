import { GenericContainer, Network, Wait } from 'testcontainers';
import { PostgreSqlContainer } from '@testcontainers/postgresql';

declare global {
  var __CONTAINERS__: unknown[];
  var __NETWORK__: Network;
}

async function globalSetup(): Promise<void> {
  console.log('Starting Testcontainers...');

  const network = await new Network().start();

  const [postgres] = await Promise.all([
    new PostgreSqlContainer('postgres:16-alpine')
      .withNetwork(network)
      .withNetworkAliases('postgres')
      .start(),
  ]);

  console.log(`PostgreSQL started at ${postgres.getConnectionUri()}`);

  const backend = await new GenericContainer('media-server:latest')
    .withNetwork(network)
    .withExposedPorts(8080)
    .withEnvironment({
      SPRING_DATASOURCE_URL: `jdbc:postgresql://postgres:5432/${postgres.getDatabase()}`,
      SPRING_DATASOURCE_USERNAME: postgres.getUsername(),
      SPRING_DATASOURCE_PASSWORD: postgres.getPassword(),
    })
    .withWaitStrategy(
      Wait.forHttp('/actuator/health', 8080).forStatusCode(200)
    )
    .start();

  const backendUrl = `http://${backend.getHost()}:${backend.getMappedPort(8080)}`;
  console.log(`Backend started at ${backendUrl}`);

  globalThis.__CONTAINERS__ = [postgres, backend];
  globalThis.__NETWORK__ = network;

  process.env.PLAYWRIGHT_BASE_URL = backendUrl;
}

export default globalSetup;
