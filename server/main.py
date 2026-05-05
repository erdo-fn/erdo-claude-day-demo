import asyncio
import json
import re
import subprocess
import uvicorn
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import anthropic

load_dotenv()

app = FastAPI(title="Quiz Server")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET"],
    allow_headers=["*"],
)

client = anthropic.Anthropic()

QUIZ_TOOL = {
    "name": "return_quiz",
    "description": "Return a 10-question multiple-choice quiz as structured data.",
    "input_schema": {
        "type": "object",
        "required": ["theme", "questions"],
        "properties": {
            "theme": {"type": "string"},
            "questions": {
                "type": "array",
                "minItems": 10,
                "maxItems": 10,
                "items": {
                    "type": "object",
                    "required": ["question", "answers"],
                    "properties": {
                        "question": {"type": "string"},
                        "answers": {
                            "type": "array",
                            "minItems": 4,
                            "maxItems": 4,
                            "items": {
                                "type": "object",
                                "required": ["text", "correct"],
                                "properties": {
                                    "text": {"type": "string"},
                                    "correct": {"type": "boolean"},
                                    "explanation": {"type": "string"},
                                },
                            },
                        },
                    },
                },
            },
        },
    },
}

CLI_PROMPT_TEMPLATE = """Generate a 10-question multiple-choice quiz on the theme: "{theme}".

Return ONLY valid JSON with this exact structure, no other text, no markdown:
{{
  "theme": "{theme}",
  "questions": [
    {{
      "question": "...",
      "answers": [
        {{"text": "...", "correct": true, "explanation": "..."}},
        {{"text": "...", "correct": false}},
        {{"text": "...", "correct": false}},
        {{"text": "...", "correct": false}}
      ]
    }}
  ]
}}

Rules:
- Exactly 10 questions
- Each question has exactly 4 answers, exactly 1 correct
- Only the correct answer includes an explanation field
- Return ONLY the JSON, no markdown fences, no extra text"""


async def get_quiz_via_cli(theme: str) -> dict:
    prompt = CLI_PROMPT_TEMPLATE.format(theme=theme)
    result = await asyncio.to_thread(
        subprocess.run,
        ["claude", "-p", prompt],
        capture_output=True,
        text=True,
        timeout=120,
    )
    if result.returncode != 0:
        raise HTTPException(status_code=502, detail=f"CLI fallback failed: {result.stderr.strip()}")

    output = result.stdout.strip()
    # Strip markdown code fences if Claude added them anyway
    output = re.sub(r"^```(?:json)?\s*", "", output)
    output = re.sub(r"\s*```$", "", output)

    try:
        return json.loads(output)
    except json.JSONDecodeError as e:
        raise HTTPException(status_code=502, detail=f"CLI fallback returned invalid JSON: {e}")


@app.get("/quiz")
async def get_quiz(theme: str):
    try:
        response = client.messages.create(
            model="claude-sonnet-4-6",
            max_tokens=4096,
            tools=[QUIZ_TOOL],
            tool_choice={"type": "any"},
            messages=[
                {
                    "role": "user",
                    "content": (
                        f'Generate a 10-question multiple-choice quiz on the theme: "{theme}". '
                        "Each question must have exactly 4 answers, exactly 1 correct. "
                        "Only the correct answer should include an explanation field."
                    ),
                }
            ],
        )
    except (anthropic.AuthenticationError, TypeError):
        return JSONResponse(content=await get_quiz_via_cli(theme))
    except anthropic.APIError as e:
        raise HTTPException(status_code=502, detail=str(e))

    if response.stop_reason != "tool_use":
        raise HTTPException(status_code=502, detail=f"Unexpected stop_reason: {response.stop_reason}")

    tool_block = next(b for b in response.content if b.type == "tool_use")
    return JSONResponse(content=tool_block.input)


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
