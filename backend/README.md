# I.D.T.F.E Backend - Render Deployment

Backend API for Integrated Developer Tools for Efficiency (I.D.T.F.E) by Quick Red Tech.


## 🚀 Run (Flask)

This backend now runs with Flask. If you previously launched a Flask server, stop that process before starting this service.

### Run locally (recommended)

1. Create a virtual environment and install dependencies:
```bash
python -m venv .venv
# Windows PowerShell: .venv\Scripts\Activate.ps1
# macOS / Linux: source .venv/bin/activate
pip install -r requirements.txt
```

2. Start the Flask app:
```bash
cd backend
python main.py
```

3. Health check URL:
```text
http://127.0.0.1:8000/api/v1/health
```

### Run in production (example)

Use a WSGI server like `gunicorn` or containerize the app. Example with `gunicorn`:
```bash
gunicorn -w 4 -b 0.0.0.0:8000 main:app
```

### If you had a previous backend process running

- Stop the process (how you started it). Common commands:
   - If started in terminal, press `Ctrl+C` in that terminal.
   - If started as a background process on Linux/macOS, find and kill it:
      ```bash
      ps aux | grep python
      kill <PID>
      ```
   - On Windows, stop the terminal or find the Python process in Task Manager and end it.

- Confirm port is free (example for port 8000):
   - Windows (PowerShell):
      ```powershell
      netstat -ano | findstr :8000
      taskkill /PID <pid> /F
      ```
   - macOS / Linux:
      ```bash
      lsof -i :8000
      kill <PID>
      ```

## 🔧 Configuration & Environment

- `backend/.env` — stores `GITHUB_TOKEN`, `AI_API_KEY`, and other local secrets. The app will load this file automatically if present.
- `PORT` — can be provided by hosting providers; the Flask app reads `PORT` if set when running `python main.py`.

## 📡 API Endpoints (Flask)

Primary endpoints:

- `/api/v1/health` — health check
- `/api/v1/meta` — app metadata and modules
- `/api/v1/version` — backend version info

Access them at `http://127.0.0.1:8000/api/v1/health` when running locally.

## 🏢 About

**Developer:** Chisom Life Eke  
**Company:** Quick Red Tech  
**GitHub:** QRTQuick  
**License:** Open Source