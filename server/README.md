# Quiz Server

A FastAPI server that uses the Claude API to generate themed multiple-choice quizzes.

## Setup

```bash
uv sync
cp .env.example .env
# Add your ANTHROPIC_API_KEY to .env
```

> **No API key?** If `ANTHROPIC_API_KEY` is missing or invalid, the server automatically falls back to the local `claude` CLI (Claude Code) to generate quizzes.

## Run

```bash
# Terminal 1 — start the server
uv run uvicorn main:app --host 0.0.0.0 --port 8000 --reload

# Terminal 2 — expose via a tunnel (pick one below)
```

### Tunnel options

**cloudflared** (no account required):
```bash
brew install cloudflared
cloudflared tunnel --url http://localhost:8000
# Prints a URL like: https://abc-123.trycloudflare.com
```

**ngrok** (free account required):
```bash
brew install ngrok/ngrok/ngrok
ngrok config add-authtoken <your-token>  # one-time setup
ngrok http 8000
# Prints a URL like: https://abc123.ngrok-free.app
```

## Usage

**Local:**
```bash
curl "http://localhost:8000/quiz?theme=history"
```

**Via cloudflared** (no special headers required):
```bash
curl "https://abc-123.trycloudflare.com/quiz?theme=history"
```

**Via ngrok** (include `ngrok-skip-browser-warning` to bypass the interstitial):
```bash
curl "https://abc123.ngrok-free.app/quiz?theme=history" \
  -H "ngrok-skip-browser-warning: true"
```

> **Mobile app note:** In your HTTP client, set the header `ngrok-skip-browser-warning: true` for ngrok URLs. No extra header is needed for cloudflared URLs.

## Response shape

```json
{
  "theme": "history",
  "questions": [
    {
      "question": "...",
      "answers": [
        { "text": "...", "correct": true, "explanation": "..." },
        { "text": "...", "correct": false },
        { "text": "...", "correct": false },
        { "text": "...", "correct": false }
      ]
    }
  ]
}
```

API docs available at `http://localhost:8000/docs` while the server is running.
