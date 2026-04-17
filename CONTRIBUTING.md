# Contributing to Mustang

Thank you for your interest in contributing to Mustang! We welcome contributions in all forms — bug reports, feature requests, documentation improvements, and code changes.

---

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

---

## How to Contribute

### Reporting Bugs

Before opening a bug report, please:
1. Search [existing issues](https://github.com/PhonePe/mustang/issues) to avoid duplicates.
2. Use the latest released version if possible.

When filing a bug, include:
- A clear description of the problem and expected behaviour.
- The version of Mustang you are using.
- A minimal reproducible example (code snippet or unit test).
- Stack traces or error logs, if relevant.

### Suggesting Features

Open a [GitHub issue](https://github.com/PhonePe/mustang/issues) with the label `enhancement`. Describe:
- The problem you are trying to solve.
- The proposed solution and any alternatives you considered.

---

## Development Setup

### Prerequisites

- **Java 17+** (OpenJDK or Temurin recommended)
- **Maven 3.9+**
- Git

### Build

```bash
git clone https://github.com/PhonePe/mustang.git
cd mustang
mvn clean verify
```

This compiles all modules (`mustang-models`, `mustang-core`, `mustang-dw-bundle`) and runs the full test suite.

### Running Tests

```bash
mvn test
```

To run tests for a specific module:

```bash
mvn test -pl mustang-core
```

### Code Style

- This project uses standard Java conventions with [Lombok](https://projectlombok.org/) for boilerplate reduction.
- Indentation: **tabs** (see `lombok.config` and `.editorconfig`).
- Line length: **120 characters** maximum.
- All public types and methods should have Javadoc.

---

## Pull Request Process

1. **Fork** the repository and create your branch from `main`.
2. **Write tests** for any new functionality or bug fixes.
3. **Ensure all tests pass**: `mvn clean verify`
4. **Update documentation** — update the relevant docs in `docs/` and `README.md` if you change behaviour.
5. **Open a Pull Request** against `main` with:
   - A clear title and description.
   - References to any related issues (`Fixes #123`).

### PR Requirements

- All CI checks must pass.
- At least one approval from a [CODEOWNER](CODEOWNERS).
- No unresolved review comments.

---

## Release Process

Releases are performed by maintainers:

1. Bump the version in all `pom.xml` files.
2. Tag the release on `main`.
3. The [`maven-central-publish.yml`](.github/workflows/maven-central-publish.yml) workflow deploys to Maven Central.

---

## Questions?

Open a [GitHub Discussion](https://github.com/PhonePe/mustang/discussions) or file an issue.
