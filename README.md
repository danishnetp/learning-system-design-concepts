# Learning System Design Concepts

A Java project for learning system design patterns and concepts.

## Project Setup

This project uses **Gradle** as the build system.

### Building the Project

#### Using Gradle Wrapper (Recommended)

**On Linux/macOS:**
```bash
./gradlew build
```

**On Windows:**
```bash
gradlew.bat build
```

#### Using Gradle (if installed locally)
```bash
gradle build
```

### Common Gradle Tasks

- **Build the project:**
  ```bash
  ./gradlew build
  ```

- **Run tests:**
  ```bash
  ./gradlew test
  ```

- **Clean build directory:**
  ```bash
  ./gradlew clean
  ```

- **Run specific test:**
  ```bash
  ./gradlew test --tests "com.learningsystemdesign.YourTestClass"
  ```

- **View dependencies:**
  ```bash
  ./gradlew dependencies
  ```

### Project Structure

```
learning-system-design-concepts/
├── src/
│   ├── main/
│   │   └── java/              # Application source code
│   └── test/
│       └── java/              # Test source code
├── build.gradle               # Gradle build configuration
├── settings.gradle            # Gradle settings
├── gradle/
│   └── wrapper/               # Gradle wrapper files
├── gradlew                     # Gradle wrapper script (Linux/macOS)
└── gradlew.bat                # Gradle wrapper script (Windows)
```

### Dependencies

- **Java Version:** 11+
- **Testing:** JUnit 4.13.2 & JUnit 5.9.2

### IDE Integration

#### IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Go to **File** → **Project Structure** → **Modules**
3. Right-click on the project and select **Reimport Gradle Project**
4. IntelliJ will automatically configure the project based on the Gradle files

#### Eclipse

1. Install the **Eclipse Gradle IDE** plugin if not already installed
2. Import the project: **File** → **Import** → **Gradle** → **Existing Gradle Project**
3. Browse to the project directory and click **Finish**

#### VS Code

1. Install the **Extension Pack for Java** extension
2. Open the project in VS Code
3. It will automatically detect the Gradle configuration

## Building and Testing

### Build the Project
```bash
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

## High-Level Design (HLD) Topics

Design diagrams live under `src/main/java/com/system/design/hld/`.
Each topic contains a **PlantUML** (`.puml`) and/or **draw.io** (`.drawio`) diagram.

| Folder | Topic | Key Concepts |
|---|---|---|
| `hld/` | **HLD-Overview** | Master map of all topics |
| `hld/idempodent/` | **Idempotency** | Idempotency keys, duplicate detection, TTL |
| `hld/load-balancing/` | **Load Balancing** | Round Robin, Least Conn, IP Hash, health checks |
| `hld/caching/` | **Caching** | Cache-Aside, Write-Through, Write-Back, LRU/LFU |
| `hld/rate-limiting/` | **Rate Limiting** | Token Bucket, Leaky Bucket, Sliding Window |
| `hld/message-queue/` | **Message Queue** | Pub/Sub, Kafka, RabbitMQ, DLQ, At-least-once |
| `hld/database-sharding/` | **DB Sharding** | Hash/Range/Directory sharding, replication |
| `hld/api-gateway/` | **API Gateway** | Auth, routing, SSL termination, aggregation |
| `hld/circuit-breaker/` | **Circuit Breaker** | CLOSED/OPEN/HALF-OPEN states, Resilience4j |
| `hld/consistent-hashing/` | **Consistent Hashing** | Hash ring, virtual nodes, minimal remapping |

### Rendering Diagrams

- **PlantUML** (`.puml`) — install the *PlantUML Integration* plugin in IntelliJ for live preview
- **draw.io** (`.drawio`) — install the *Diagrams.net Integration* plugin in IntelliJ to open visually

Plugin recommendations are saved in `.idea/externalDependencies.xml` — IntelliJ will prompt you to install them automatically.

## License

This project is for learning purposes.

