# Client

## Code breakdown

Run from the root of the repo (`demo-example/`):

```
cloc --include-lang=Kotlin shared/src && cloc --include-lang=Kotlin androidApp/src && cloc --include-lang=Swift iosApp/iosApp
```

The platform-specific modules (`androidApp`, `iosApp`) include surplus diagnostic/boilerplate code. The production UI on each platform is closer to ~250 lines for Android and ~250 lines for iOS.

| Module | Language | Files | Code lines | % of total (production) |
|--------|----------|------:|----------:|----------:|
| `shared/src` | Kotlin | 20 | 653 | 57% |
| `androidApp/src` | Kotlin | 8 | 719 | 22% |
| `iosApp/iosApp` | Swift | 4 | 264 | 22% |
| **Total** | | **32** | **1,636** | **100%** |
