# I.D.T.F.E Setup Instructions

## Prerequisites

- **Java 17+** with JavaFX 20.0.2 at `C:\javafx-sdk-20.0.2`
- **Python 3.8+**
- **Gradle** (or use included gradlew)

## Backend Setup (Flask)

1. Navigate to backend directory:
   ```cmd
   cd backend
   ```

2. Install Python dependencies:
   ```cmd
   pip install -r requirements.txt
   ```

3. Start the backend server:
   ```cmd
   python main.py
   ```
   
   Backend will run on `http://localhost:8000`

## Frontend Setup (JavaFX)

1. Navigate to frontend directory:
   ```cmd
   cd frontend
   ```

2. Compile the JavaFX application:
   ```cmd
   gradle compileJava
   ```

3. Run the JavaFX application:
   ```cmd
   gradle run
   ```

## Testing the Backend

You can test the backend API directly:

```powershell
# Health check
Invoke-RestMethod -Uri "http://localhost:8000/api/v1/health" -Method GET

# Get metadata
Invoke-RestMethod -Uri "http://localhost:8000/api/v1/meta" -Method GET

# Test API endpoint
$body = @{
    url = "https://httpbin.org/get"
    method = "GET"
    headers = @{}
    body = ""
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8000/api/v1/tools/api-tester" -Method POST -Body $body -ContentType "application/json"
```

## Usage

1. Start the backend server first
2. Launch the JavaFX frontend
3. The app will automatically check backend connectivity
4. Select modules from the sidebar:
   - **API Tester**: Test REST endpoints with various HTTP methods
   - **README Previewer**: Render Markdown files with live preview
   - **Web IDE**: Code editor with formatting, linting, and preview
   - **Browser**: Embedded web browser with JSON formatting

## Usage

1. Start the backend server first
2. Launch the JavaFX frontend  
3. The app automatically checks backend connectivity
4. Select modules from the sidebar or **Tools menu**
5. Each tool opens in a new tab for easy multitasking

### 📋 **Menu Bar Features**

#### **File Menu**
- Exit application

#### **Tools Menu**  
- Quick access to all modules:
  - API Tester
  - README Previewer
  - Web IDE
  - Browser

#### **View Menu**
- Refresh Modules (reload from backend)
- Toggle Status Bar visibility

#### **Help Menu**
- **Help Documentation** - Comprehensive usage guide
- **GitHub Repository** - Opens https://github.com/QRTQuick  
- **About I.D.T.F.E** - Application info and credits

## Module Features

### 🔧 **API Tester**
- Support for GET, POST, PUT, DELETE methods
- Custom headers and request body
- Response formatting and timing
- Error handling and status display

### 📖 **README Previewer**
- Load Markdown files from disk
- Live HTML preview with styling
- Support for code highlighting
- Clickable links integration

### 💻 **Web IDE**
- **Multi-language support** (HTML, CSS, JS, JSON, Markdown, XML)
- **Project folder support** - Open entire project directories
- **File tree navigation** - Browse and switch between files easily
- **Auto-resource detection** - Automatically detects and includes CSS/JS files when previewing HTML
- **Code formatting and linting** with file-type specific rules
- **Live preview** for supported formats with enhanced HTML rendering
- **File operations** (open, save, folder management)
- **Syntax error detection** and validation
- **Three-panel layout** - File tree, editor, and preview/lint results

### 🌐 **Web Browser**
- Full web browsing capabilities
- Navigation controls (back, forward, refresh)
- JSON formatting for API responses
- URL validation and protocol handling

## API Endpoints

- `GET /api/v1/health` - Health check
- `GET /api/v1/meta` - App metadata and modules
- `GET /api/v1/version` - Version information
- `POST /api/v1/tools/api-tester` - API testing
- `POST /api/v1/tools/markdown/render` - Markdown rendering

## Architecture

The application follows a modular, plugin-based architecture:
- **Frontend**: JavaFX with FXML for UI components
- **Backend**: Flask for REST API endpoints
- **Communication**: JSON over HTTP
- **Modules**: Loosely coupled, easily extendable

Each module has its own controller, FXML file, and backend endpoints following the established pattern.

## Troubleshooting

- If you get Pydantic errors, the Flask backend should work without issues
- Make sure JavaFX is properly configured in your system
- Backend must be running before starting the frontend
- Check that ports 8000 is available