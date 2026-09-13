# 🤝 Contributing to OmniRepair

First off, thanks for taking the time to contribute! ❤️

All types of contributions are encouraged and valued. See the [Table of Contents](#table-of-contents) for different ways to help and details about how this project handles them.

---

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [I Have a Question](#i-have-a-question)
- [I Want To Contribute](#i-want-to-contribute)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Enhancements](#suggesting-enhancements)
- [Your First Code Contribution](#your-first-code-contribution)
- [Styleguides](#styleguides)
- [Commit Messages](#commit-messages)

---

## Code of Conduct

This project and everyone participating in it is governed by good practices. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainer.

---

## I Have a Question

> If you want to ask a question, we assume that you have read the available [Documentation](README.md).

Before you ask a question, it is best to search for existing [Issues](https://github.com/Syaaddd/OmniRepair/issues) that might help you. In case you have found a suitable issue and still need clarification, you can write your question in this issue.

### How to Ask a Question

1. Open an [Issue](https://github.com/Syaaddd/OmniRepair/issues/new)
2. Provide as much context as you can about what you're running into
3. Include your server version, plugin version, and any relevant configuration

---

## I Want To Contribute

> ### Legal Notice
> When contributing to this project, you must agree that you have authored 100% of the content, that you have the necessary rights to the content, and that the content you contribute may be provided under the project license.

### How to Contribute

1. **Fork the repository** on GitHub
2. **Clone your fork** locally
3. **Create a branch** for your feature (`git checkout -b feature/amazing-feature`)
4. **Make your changes** following our styleguides
5. **Test your changes** thoroughly
6. **Commit your changes** (`git commit -m 'Add amazing feature'`)
7. **Push to the branch** (`git push origin feature/amazing-feature`)
8. **Open a Pull Request** on GitHub

---

## Reporting Bugs

### Before Submitting a Bug Report

A good bug report shouldn't leave others needing to chase you up for more information. Therefore, we ask you to investigate carefully, collect information, and describe the issue in detail in your report.

Please complete the following steps in advance:

1. **Check if you're using the latest version** — Your bug may already be fixed
2. **Search for similar issues** — Your bug may have been reported already
3. **Collect information:**
   - Plugin version
   - Server version (Paper/Spigot/Purpur + Minecraft version)
   - Java version
   - Configuration files (config.yml, messages.yml)
   - Console logs (use [mclogs](https://mclo.gs/) for long logs)
   - Steps to reproduce

### How to Submit a Bug Report

1. Open an [Issue](https://github.com/Syaaddd/OmniRepair/issues/new)
2. Use the bug report template if available
3. Fill in all required information
4. Describe the expected behavior vs actual behavior
5. Include steps to reproduce the issue
6. Attach logs and screenshots if relevant

---

## Suggesting Enhancements

This section guides you through submitting an enhancement suggestion, including completely new features and minor improvements to existing functionality.

### Before Submitting an Enhancement

1. **Ensure you're using the latest version** — Your feature may already exist
2. **Read the documentation** — Your enhancement might already be possible with existing features
3. **Search existing issues** — Your idea may have been discussed already
4. **Consider the scope** — Does this fit the plugin's purpose?

### How to Submit an Enhancement

1. Open an [Issue](https://github.com/Syaaddd/OmniRepair/issues/new)
2. Use a clear and descriptive title
3. Describe the current behavior and what you'd like to see
4. Explain why this enhancement would be useful
5. Provide examples or mockups if applicable
6. List any alternatives you've considered

---

## Your First Code Contribution

### Prerequisites

- **Java 21+** installed
- **Maven 3.8+** or use the Maven wrapper
- **Git** for version control
- **IDE** (IntelliJ IDEA recommended, Eclipse supported)

### Setting Up Development Environment

#### IntelliJ IDEA (Recommended)
1. Clone the repository
2. Open the project folder in IntelliJ
3. Let IntelliJ import the Maven project automatically
4. Ensure JDK 21 is selected in Project Structure
5. Wait for dependencies to download

#### Eclipse
1. Clone the repository
2. File → Import → Maven → Existing Maven Projects
3. Select the project folder
4. Ensure JDK 21 is configured

### Building the Project

```bash
# Using Maven wrapper (recommended)
mvnw.cmd clean package

# Or with installed Maven
mvn clean package
```

The compiled JAR will be in `target/OmniRepair-1.0.1-SNAPSHOT.jar`

### Testing Your Changes

1. **Build the plugin** — `mvn clean package`
2. **Set up a test server** — Paper/Spigot 1.21+
3. **Install the plugin** — Copy JAR to `plugins/` folder
4. **Install dependencies** — MMOItems, Vault (optional)
5. **Test your feature** — Verify it works as expected
6. **Test edge cases** — Try to break it
7. **Check console** — No errors or warnings

### What You Can Contribute

- **Bug fixes** — Fix existing issues
- **New features** — Add functionality
- **Documentation** — Improve docs, add examples
- **Code optimization** — Performance improvements
- **Testing** — Add test cases
- **Translations** — Multi-language support

---

## Styleguides

### Java Code Style

We follow standard Java conventions with these specifics:

#### Naming Conventions

```java
// Classes: PascalCase
public class RepairHandler { }

// Methods: camelCase
public void repairItem() { }

// Variables: camelCase
private ItemStack itemToRepair;

// Constants: UPPER_SNAKE_CASE
public static final int MAX_BULK_REPAIR = 360;

// Packages: lowercase
package com.github.Syaaddd.omniRepair.utils;
```

#### Code Organization

```java
public class ExampleClass {
    // 1. Static fields
    private static final String CONSTANT = "value";
    
    // 2. Instance fields
    private final Plugin plugin;
    
    // 3. Constructors
    public ExampleClass(Plugin plugin) {
        this.plugin = plugin;
    }
    
    // 4. Public methods
    public void doSomething() { }
    
    // 5. Private methods
    private void helperMethod() { }
}
```

#### Comments

```java
// Good: Explain WHY, not WHAT
// Clone item before repair to enable rollback on failure
ItemStack clone = item.clone();

// Bad: State the obvious
// Create a new ItemStack
ItemStack clone = item.clone();

// Good: Document complex logic
/**
 * Calculates repair cost based on damage percentage and config multipliers.
 * 
 * @param item The item to calculate cost for
 * @param damagePercent Percentage of durability lost (0.0 - 1.0)
 * @return Final cost after applying multipliers and caps
 */
public double calculateCost(ItemStack item, double damagePercent) { }
```

#### Best Practices

- **Keep methods small** — Max ~30 lines per method
- **Single responsibility** — Each method does one thing
- **Avoid magic numbers** — Use named constants
- **Handle nulls** — Always check for null values
- **Use Optional** — When return value might be absent
- **Prefer composition** — Over inheritance
- **Follow DRY** — Don't repeat yourself

### YAML Configuration Style

```yaml
# Use clear section separators
# ═══════════════════════════════════════════════════════════════
# SECTION NAME
# ═══════════════════════════════════════════════════════════════

# Comment every setting
settings:
  # Enable debug mode (shows detailed console messages)
  debug: false

# Use consistent indentation (2 spaces)
section:
  key: value
  nested:
    deep-key: value

# Group related settings
gui:
  size: 27
  title: "&8&l🔨 RPG Mender"
  slots:
    repair-hand: 10
    repair-all: 12
    close: 4
```

### Message Strings Style

```yaml
# Use color codes consistently
success: "&a✓ Successfully repaired! Cost: &e${cost}"
error: "&c✗ Error occurred!"
warning: "&e⚠ Warning!"

# Use placeholders for dynamic content
player-message: "&aHello, &e{player}&a!"

# Keep action bar messages short
action-bar: "&a✓ Repaired!"
```

---

## Commit Messages

### Commit Message Format

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Code style changes (formatting, semicolons, etc)
- **refactor**: Code change that neither fixes a bug nor adds a feature
- **perf**: Performance improvement
- **test**: Adding or updating tests
- **chore**: Maintenance tasks (dependencies, build, etc)

### Scopes

- **gui**: GUI-related changes
- **economy**: Economy/payment changes
- **mmoitems**: MMOItems integration changes
- **config**: Configuration changes
- **api**: API changes
- **build**: Build system changes

### Examples

```bash
# Simple feature
feat(gui): add repair preview before confirmation

# Bug fix with issue reference
fix(economy): prevent negative balance exploit

Closes #42

# Breaking change
refactor(api)!: change RepairHandler interface signature

BREAKING CHANGE: repairItem() now returns RepairResult instead of boolean

# Documentation
docs(readme): add installation instructions

# Multiple changes
feat(mmoitems): add durability fallback system

- Added NBT reading for custom durability
- Implemented 4-layer fallback detection
- Updated config with fallback options

Closes #15, #23
```

### Commit Best Practices

- **Keep commits atomic** — One logical change per commit
- **Write clear subjects** — Imperative mood, max 50 chars
- **Explain the WHY** — In the body, not the subject
- **Reference issues** — Use `Closes #123` or `Fixes #456`
- **Don't mix changes** — Separate refactoring from features

---

## Pull Request Process

### Before Submitting

1. **Update documentation** — README, comments, etc.
2. **Test thoroughly** — All features work correctly
3. **Check code style** — Follow our styleguides
4. **Write clear commit messages** — Follow conventional commits
5. **Rebase if needed** — Stay up-to-date with main branch

### Submitting the PR

1. **Use a descriptive title** — Summarize the changes
2. **Fill the PR template** — Provide all requested information
3. **Describe your changes** — What and why
4. **Link related issues** — `Closes #123`
5. **Add screenshots** — For UI changes
6. **Test your branch** — One last check

### After Submitting

1. **Wait for review** — Maintainers will review your PR
2. **Respond to feedback** — Make requested changes
3. **Be patient** — Reviews take time
4. **Celebrate** — Your contribution matters! 🎉

---

## Development Tips

### Debug Mode

Enable debug mode to see detailed logs:
```
/repair debug
```

### Testing MMOItems Integration

1. Install MMOItems on test server
2. Create custom items with durability
3. Test repair with and without API
4. Verify NBT fallback works

### Testing Economy

1. Install Vault + economy plugin (EssentialsX, CMI, etc.)
2. Test all payment methods:
   - Money
   - XP levels
   - Item costs
   - Free repair

### Common Issues

**Build fails with dependency errors:**
- Check internet connection
- Verify Maven repositories are accessible
- Try `mvn clean` first

**Plugin doesn't load:**
- Check Java version (21+)
- Verify server version (1.21+)
- Check console for errors

**MMOItems repair fails:**
- Enable debug mode
- Check MMOItems version (6.9+)
- Verify soft dependency is loaded

---

## Recognition

Contributors will be recognized in:
- GitHub contributors list
- Release notes
- Documentation credits

Thank you for contributing to OmniRepair! 🙏

---

## Questions?

Feel free to open an issue or contact the maintainer.

**Happy Coding!** 🔨
