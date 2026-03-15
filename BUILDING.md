# 🔨 OmniRepair - Build Instructions

## Prerequisites

- **Java JDK 21** or higher
- **Maven 3.8+** (or use the Maven wrapper)

## Building with Maven

### Option 1: Using installed Maven

```bash
mvn clean package
```

### Option 2: Using Maven Wrapper (recommended)

#### Windows
```bash
mvnw.cmd clean package
```

#### Linux/Mac
```bash
./mvnw clean package
```

## Output

The compiled JAR file will be located in:
```
target/OmniRepair-1.0.0-SNAPSHOT.jar
```

## Build Commands

| Command | Description |
|---------|-------------|
| `mvn clean` | Clean build directory |
| `mvn compile` | Compile source code |
| `mvn package` | Build JAR file |
| `mvn clean package` | Clean and build |
| `mvn install` | Install to local repository |

## Troubleshooting

### "Java 21 not found"
Ensure JAVA_HOME is set correctly:
```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

### "Maven not found"
Download Maven from: https://maven.apache.org/download.cgi

Or use the Maven wrapper (it will download automatically on first run).

### Build fails with dependency errors
Ensure you have internet connection. The build needs to download:
- PaperMC API
- MMOItems API
- Vault API
- WorldGuard API

All dependencies are marked as `provided` scope, meaning they should be present on the server but are not included in the JAR.

## Server Installation

1. Copy `target/OmniRepair-1.0.0-SNAPSHOT.jar` to your server's `plugins/` folder
2. Ensure required dependencies are installed:
   - **Required:** Paper/Spigot 1.21+
   - **Optional:** MMOItems, Vault, WorldGuard
3. Start the server
4. Configure in `plugins/OmniRepair/config.yml`

## Development

### IDE Setup

#### IntelliJ IDEA
1. Open project folder
2. Let IntelliJ import Maven project automatically
3. Ensure JDK 21 is selected

#### Eclipse
1. File → Import → Maven → Existing Maven Projects
2. Select project folder
3. Ensure JDK 21 is configured

### Code Style

- Use Java 21 features (records, pattern matching, switch expressions)
- Follow Java naming conventions
- Add comments for complex logic only
- Keep methods small and focused

## Testing

Currently, no automated tests are included. Manual testing is recommended:

1. Set up a test server with Paper/Spigot
2. Install OmniRepair and dependencies
3. Test each feature:
   - GUI opening
   - Vanilla item repair
   - MMOItems repair (if MMOItems installed)
   - Economy integration (if Vault installed)
   - Bulk repair
   - Blacklist functionality

## Release Checklist

- [ ] Update version in `pom.xml`
- [ ] Update version in `plugin.yml`
- [ ] Update README with new features
- [ ] Test all features manually
- [ ] Build with `mvn clean package`
- [ ] Create GitHub release
- [ ] Upload to Modrinth/SpigotMC

---

**Happy Building!** 🔨
