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

## License

This project is for learning purposes.

