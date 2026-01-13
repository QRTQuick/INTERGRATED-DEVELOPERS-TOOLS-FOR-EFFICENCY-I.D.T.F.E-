import requests
from flask import Blueprint, redirect, request, jsonify
from pathlib import Path
from dotenv import load_dotenv
from config import (
    GITHUB_CLIENT_ID,
    GITHUB_CLIENT_SECRET,
    GITHUB_REDIRECT_URI,
)

github = Blueprint("github", __name__)

# locate .env in repository root (one level up)
ENV_PATH = Path(__file__).resolve().parents[1] / ".env"
if ENV_PATH.exists():
    load_dotenv(ENV_PATH)


def save_env_var(key: str, value: str):
    lines = []
    if ENV_PATH.exists():
        with open(ENV_PATH, "r", encoding="utf-8") as f:
            lines = f.read().splitlines()
    found = False
    for i, line in enumerate(lines):
        if line.startswith(key + "="):
            lines[i] = f"{key}={value}"
            found = True
            break
    if not found:
        lines.append(f"{key}={value}")
    with open(ENV_PATH, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))


@github.route("/auth/github/login")
def github_login():
    url = (
        "https://github.com/login/oauth/authorize"
        f"?client_id={GITHUB_CLIENT_ID}"
        f"&redirect_uri={GITHUB_REDIRECT_URI}"
        "&scope=read:user repo"
    )
    return redirect(url)


@github.route("/auth/github/callback")
def github_callback():
    code = request.args.get("code")
    if not code:
        return jsonify({"error": "Missing code"}), 400

    token_res = requests.post(
        "https://github.com/login/oauth/access_token",
        headers={"Accept": "application/json"},
        data={
            "client_id": GITHUB_CLIENT_ID,
            "client_secret": GITHUB_CLIENT_SECRET,
            "code": code,
            "redirect_uri": GITHUB_REDIRECT_URI,
        },
        timeout=10,
    ).json()

    access_token = token_res.get("access_token")
    if not access_token:
        return jsonify({"error": "Token exchange failed", "details": token_res}), 400

    # Save token to .env so backend can use it
    try:
        save_env_var("GITHUB_TOKEN", access_token)
    except Exception:
        pass

    user = requests.get(
        "https://api.github.com/user",
        headers={"Authorization": f"Bearer {access_token}"},
        timeout=10,
    ).json()

    return jsonify(
        {
            "authenticated": True,
            "username": user.get("login"),
            "avatar": user.get("avatar_url"),
            "profile": user.get("html_url"),
            "id": user.get("id"),
        }
    )
