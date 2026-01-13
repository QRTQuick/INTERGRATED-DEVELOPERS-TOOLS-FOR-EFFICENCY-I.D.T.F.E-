# I.D.T.F.E - Integrated Developer Tools for Efficiency

A modular desktop application with JavaFX frontend and Python Flask backend for developer productivity tools.

## 🏢 **Quick Red Tech**
**Developer:** Chisom Life Eke  
**GitHub:** [QRTQuick](https://github.com/QRTQuick)  
**License:** Open Source  
**Copyright:** © 2026 Quick Red Tech

## ✨ Features

- **API Tester**: Test REST API endpoints with various HTTP methods
- **README Previewer**: Render Markdown README files in a web view  
- **Web IDE**: Advanced code editor with project management, auto-resource detection, formatting, linting, and live preview
- **Browser**: Embedded web browser with JSON formatting capabilities

## 🏗️ Architecture

- **Frontend**: JavaFX with FXML for UI components
- **Backend**: Python Flask for REST API endpoints  
- **Communication**: REST API with JSON payloads
- **Design**: Modular, plugin-based architecture for easy extension

## 🚀 Quick Start

### Prerequisites
- Java 17+ with JavaFX 20.0.2
- Python 3.8+
- Gradle

### Backend Setup
```bash
cd backend
pip install -r requirements.txt
python main.py
```

### Frontend Setup  
```bash
cd frontend
gradle run
```

## 📖 Usage

1. Start the backend server first
2. Launch the JavaFX application
3. The app automatically checks backend connectivity
4. Select modules from the sidebar or Tools menu
5. Each tool opens in a new tab for easy multitasking

## 🛠️ Development

The application follows a modular, plugin-based architecture that allows easy extension with new tools. Each module consists of:

- Backend API endpoints (`/api/v1/tools/{module}`)
- Frontend JavaFX controller
- FXML layout file
- Service integration

## 🤝 Contributing

This is an open source project. Contributions are welcome!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

Open Source - see the repository for license details.

## 🙏 Acknowledgments

Built with passion by **Chisom Life Eke** at **Quick Red Tech**.

---

**Quick Red Tech** - Building innovative developer tools and solutions.