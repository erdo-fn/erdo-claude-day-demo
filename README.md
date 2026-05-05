# erdo-claude-day-demo

A basic client/server quiz app. The server generates themed multiple-choice quizzes via the Claude API, and the client is a Kotlin Multiplatform app targeting Android and iOS.

- [`server/`](server/README.md) — FastAPI server (Python) that uses the Claude API to generate quizzes
- [`client/`](client/README.md) — KMP mobile app (Kotlin + Swift) that consumes the server



### Requirements

#### Server
- Python >= 3.12
- [uv](https://docs.astral.sh/uv/) (`brew install uv`)
- `ANTHROPIC_API_KEY` (optional — falls back to the local `claude` CLI)

