from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
import time
import markdown
import json
import os

app = Flask(__name__)
CORS(app)

# Get port from environment variable (Render sets this automatically)
PORT = int(os.environ.get('PORT', 8000))

# Health endpoint
@app.route("/api/v1/health", methods=["GET"])
def health_check():
    return jsonify({"status": "ok", "message": "I.D.T.F.E Backend is running on Render", "port": PORT})

# Metadata endpoint
@app.route("/api/v1/meta", methods=["GET"])
def get_metadata():
    return jsonify({
        "app_name": "IDTFE",
        "full_name": "Integrated Developer Tools for Efficiency",
        "version": "1.0.0",
        "developer": "Chisom Life Eke",
        "company": "Quick Red Tech",
        "github": "QRTQuick",
        "license": "Open Source",
        "copyright": "© 2026 Quick Red Tech",
        "deployment": "Render Cloud Platform",
        "modules": [
            {"name": "API Tester", "id": "api-tester", "enabled": True, "description": "Test REST APIs with various HTTP methods"},
            {"name": "README Previewer", "id": "readme-previewer", "enabled": True, "description": "Render Markdown files with live preview"},
            {"name": "Web IDE", "id": "web-ide", "enabled": True, "description": "Code editor with formatting and linting"},
            {"name": "Browser", "id": "browser", "enabled": True, "description": "Embedded web browser with JSON formatting"}
        ],
        "feature_flags": {
            "auto_format": True,
            "syntax_highlighting": True,
            "live_preview": True,
            "github_integration": True,
            "cloud_deployment": True
        }
    })

# Version endpoint
@app.route("/api/v1/version", methods=["GET"])
def get_version():
    return jsonify({
        "version": "1.0.0",
        "compatible_frontend": "1.0.0",
        "changelog": "Initial release with core modules - Deployed on Render",
        "build_info": {
            "developer": "Chisom Life Eke",
            "company": "Quick Red Tech",
            "github": "QRTQuick",
            "license": "Open Source",
            "build_date": "2026-01-13",
            "platform": "Render Cloud"
        }
    })

# API Tester endpoint
@app.route("/api/v1/tools/api-tester", methods=["POST"])
def test_api():
    try:
        data = request.get_json()
        url = data.get("url")
        method = data.get("method", "GET").upper()
        headers = data.get("headers", {})
        body = data.get("body", "")
        
        if not url:
            return jsonify({"error": "URL is required"}), 400
        
        start_time = time.time()
        
        # Make request
        if method == "GET":
            response = requests.get(url, headers=headers, timeout=30)
        elif method == "POST":
            response = requests.post(url, headers=headers, data=body, timeout=30)
        elif method == "PUT":
            response = requests.put(url, headers=headers, data=body, timeout=30)
        elif method == "DELETE":
            response = requests.delete(url, headers=headers, timeout=30)
        else:
            return jsonify({"error": "Unsupported HTTP method"}), 400
        
        duration = time.time() - start_time
        
        # Parse response body
        try:
            body_result = response.json()
        except:
            body_result = response.text
        
        return jsonify({
            "status_code": response.status_code,
            "headers": dict(response.headers),
            "body": body_result,
            "duration": round(duration, 3)
        })
    
    except requests.exceptions.RequestException as e:
        return jsonify({"error": f"Request failed: {str(e)}"}), 400

# Markdown renderer endpoint
@app.route("/api/v1/tools/markdown/render", methods=["POST"])
def render_markdown():
    try:
        data = request.get_json()
        content = data.get("content", "")
        
        html = markdown.markdown(content, extensions=['codehilite', 'fenced_code'])
        return jsonify({"html": html})
    except Exception as e:
        return jsonify({"error": f"Markdown rendering failed: {str(e)}"}), 400

# Web IDE action endpoint
@app.route("/api/v1/tools/ide/action", methods=["POST"])
def ide_action():
    try:
        data = request.get_json()
        action = data.get("action")
        content = data.get("content", "")
        file_type = data.get("file_type", "")
        
        if action == "format":
            # Enhanced formatting based on file type
            if file_type.lower() == "json":
                try:
                    import json
                    parsed = json.loads(content)
                    formatted_content = json.dumps(parsed, indent=2, ensure_ascii=False)
                except:
                    formatted_content = content.strip()
            elif file_type.lower() == "html":
                # Basic HTML formatting (add proper indentation)
                formatted_content = format_html(content)
            elif file_type.lower() == "css":
                # Basic CSS formatting
                formatted_content = format_css(content)
            else:
                # Basic formatting (remove extra whitespace)
                formatted_content = content.strip()
            
            return jsonify({"content": formatted_content, "action": "format", "success": True})
        
        elif action == "lint":
            # Enhanced linting based on file type
            issues = []
            
            if file_type.lower() == "json":
                try:
                    json.loads(content)
                except json.JSONDecodeError as e:
                    issues.append({"line": getattr(e, 'lineno', 1), "message": f"JSON Error: {e.msg}"})
                except Exception as e:
                    issues.append({"line": 1, "message": f"JSON Error: {str(e)}"})
            elif file_type.lower() == "html":
                issues.extend(lint_html(content))
            elif file_type.lower() == "css":
                issues.extend(lint_css(content))
            elif len(content.strip()) == 0:
                issues.append({"line": 1, "message": "File is empty"})
            
            return jsonify({"issues": issues, "action": "lint", "success": True})
        
        elif action == "preview":
            auto_detect = data.get("auto_detect_resources", False)
            directory_path = data.get("directory_path", "")
            current_file = data.get("current_file", "")
            
            if file_type.lower() == "markdown":
                html = markdown.markdown(content, extensions=['codehilite', 'fenced_code'])
                return jsonify({"preview": html, "type": "html", "action": "preview", "success": True})
            
            elif file_type.lower() == "html":
                if auto_detect and directory_path:
                    # Detect and inject CSS/JS resources
                    enhanced_html, detected_resources = enhance_html_with_resources(
                        content, directory_path, current_file
                    )
                    return jsonify({
                        "preview": enhanced_html, 
                        "type": "html", 
                        "action": "preview", 
                        "success": True,
                        "detected_resources": detected_resources
                    })
                else:
                    return jsonify({"preview": content, "type": "html", "action": "preview", "success": True})
            
            elif file_type.lower() == "json":
                try:
                    parsed = json.loads(content)
                    formatted = json.dumps(parsed, indent=2, ensure_ascii=False)
                    return jsonify({"preview": formatted, "type": "text", "action": "preview", "success": True})
                except:
                    return jsonify({"preview": content, "type": "text", "action": "preview", "success": True})
            else:
                return jsonify({"preview": content, "type": "text", "action": "preview", "success": True})
        
        else:
            return jsonify({"error": "Unsupported action"}), 400
    
    except Exception as e:
        return jsonify({"error": f"IDE action failed: {str(e)}"}), 400

def enhance_html_with_resources(html_content, directory_path, current_file):
    """
    Enhance HTML content by automatically detecting and injecting CSS and JS files
    from the same directory
    """
    import os
    import re
    
    detected_resources = []
    
    try:
        # Find CSS files in the directory
        css_files = []
        js_files = []
        
        for file in os.listdir(directory_path):
            if file.endswith('.css'):
                css_files.append(file)
            elif file.endswith('.js'):
                js_files.append(file)
        
        # Read CSS and JS content
        css_content = ""
        js_content = ""
        
        for css_file in css_files:
            css_path = os.path.join(directory_path, css_file)
            try:
                with open(css_path, 'r', encoding='utf-8') as f:
                    css_content += f"/* {css_file} */\n{f.read()}\n\n"
                    detected_resources.append(css_file)
            except Exception as e:
                print(f"Error reading CSS file {css_file}: {e}")
        
        for js_file in js_files:
            js_path = os.path.join(directory_path, js_file)
            try:
                with open(js_path, 'r', encoding='utf-8') as f:
                    js_content += f"/* {js_file} */\n{f.read()}\n\n"
                    detected_resources.append(js_file)
            except Exception as e:
                print(f"Error reading JS file {js_file}: {e}")
        
        # Inject CSS and JS into HTML
        enhanced_html = html_content
        
        # Inject CSS
        if css_content:
            css_injection = f"<style>\n{css_content}</style>"
            
            # Try to inject before closing </head> tag
            if "</head>" in enhanced_html:
                enhanced_html = enhanced_html.replace("</head>", f"{css_injection}\n</head>")
            else:
                # If no head tag, add at the beginning
                enhanced_html = f"<head>{css_injection}</head>\n{enhanced_html}"
        
        # Inject JavaScript
        if js_content:
            js_injection = f"<script>\n{js_content}</script>"
            
            # Try to inject before closing </body> tag
            if "</body>" in enhanced_html:
                enhanced_html = enhanced_html.replace("</body>", f"{js_injection}\n</body>")
            else:
                # If no body tag, add at the end
                enhanced_html = f"{enhanced_html}\n{js_injection}"
        
        return enhanced_html, detected_resources
        
    except Exception as e:
        print(f"Error enhancing HTML with resources: {e}")
        return html_content, []

def format_html(content):
    """Basic HTML formatting"""
    import re
    # Add basic indentation
    lines = content.split('\n')
    formatted_lines = []
    indent_level = 0
    
    for line in lines:
        stripped = line.strip()
        if not stripped:
            continue
            
        # Decrease indent for closing tags
        if stripped.startswith('</'):
            indent_level = max(0, indent_level - 1)
        
        # Add indentation
        formatted_lines.append('  ' * indent_level + stripped)
        
        # Increase indent for opening tags (but not self-closing)
        if stripped.startswith('<') and not stripped.startswith('</') and not stripped.endswith('/>'):
            if not any(tag in stripped for tag in ['<br', '<hr', '<img', '<input', '<meta']):
                indent_level += 1
    
    return '\n'.join(formatted_lines)

def format_css(content):
    """Basic CSS formatting"""
    import re
    # Add basic formatting
    content = re.sub(r'\s*{\s*', ' {\n  ', content)
    content = re.sub(r';\s*', ';\n  ', content)
    content = re.sub(r'\s*}\s*', '\n}\n\n', content)
    return content.strip()

def lint_html(content):
    """Basic HTML linting"""
    issues = []
    lines = content.split('\n')
    
    for i, line in enumerate(lines, 1):
        # Check for common issues
        if '<script>' in line.lower() and '</script>' not in line.lower():
            issues.append({"line": i, "message": "Unclosed script tag"})
        if '<style>' in line.lower() and '</style>' not in line.lower():
            issues.append({"line": i, "message": "Unclosed style tag"})
    
    return issues

def lint_css(content):
    """Basic CSS linting"""
    issues = []
    lines = content.split('\n')
    
    for i, line in enumerate(lines, 1):
        stripped = line.strip()
        if stripped and not stripped.endswith((';', '{', '}')):
            if ':' in stripped and not stripped.startswith('/*'):
                issues.append({"line": i, "message": "Missing semicolon"})
    
    return issues

if __name__ == "__main__":
    # Use Gunicorn in production, Flask dev server locally
    if os.environ.get('RENDER'):
        # Running on Render - use Gunicorn
        import subprocess
        subprocess.run([
            "gunicorn", 
            "--bind", f"0.0.0.0:{PORT}",
            "--workers", "2",
            "--timeout", "120",
            "main:app"
        ])
    else:
        # Running locally - use Flask dev server
        app.run(host="0.0.0.0", port=PORT, debug=True)