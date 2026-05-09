# Nature-Themed Q&A App

This project is a full-stack Q&A app with:
- `backend/`: Spring Boot API that securely calls OpenAI Chat Completions.
- `frontend/`: Angular app with a nature-themed UI and a Google-like question box.

## Security First

- Do not put your API key in frontend code.
- Use backend environment variables only.
- Revoke the previously leaked key and generate a new one before running.

## Backend Setup (Spring Boot)

1. Go to backend:
   - `cd backend`
2. Set environment variable:
   - PowerShell: `$env:OPENAI_API_KEY="your_new_key_here"`
3. Optional settings:
   - `$env:OPENAI_MODEL="gpt-4.1-mini"`
   - `$env:APP_CORS_ALLOWED_ORIGIN="http://localhost:4200"`
4. Run backend:
   - `mvn spring-boot:run`

Backend runs on `http://localhost:8080`.

### Backend API

- `POST /api/ask`
- Request body:
  - `{ "question": "Explain Spring Boot simply" }`
- Success response:
  - `{ "answer": "..." }`
- Error response:
  - `{ "message": "..." }`

## Frontend Setup (Angular)

1. Go to frontend:
   - `cd frontend`
2. Install dependencies:
   - `npm install`
3. Run frontend:
   - `npm start`

Frontend runs on `http://localhost:4200`.

## How It Works

1. User enters a question in the Angular page.
2. Angular calls backend `POST /api/ask`.
3. Spring Boot calls OpenAI `v1/chat/completions`.
4. Backend returns normalized answer JSON.
5. Frontend renders answer, loading state, or friendly error.
