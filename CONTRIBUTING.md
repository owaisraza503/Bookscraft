# Contributing to Bookscraft

Thank you for your interest in contributing to Bookscraft! We welcome contributions from developers, designers, and documentation writers.

## Code Standards
- **Language:** Kotlin (100%)
- **UI:** Jetpack Compose with Material 3 design guidelines.
- **Architecture:** Clean MVVM with reactive StateFlow streams.
- **PDF Operations:** PDF modifications must preserve ISO 32000-1 compliance and not introduce proprietary non-standard structures.

## Development Workflow
1. Fork the repository and create your feature branch from `main`.
2. Follow Kotlin style conventions and Android Kotlin code style.
3. Ensure all unit and Robolectric tests pass:
   ```bash
   gradle :app:testDebugUnitTest
   ```
4. If adding new PDF manipulation features, include a corresponding round-trip unit test verifying that saved PDF objects are structurally valid.
5. Open a Pull Request with a clear description of the problem solved.
