# I.D.T.F.E Backend - Render Deployment

Backend API for Integrated Developer Tools for Efficiency (I.D.T.F.E) by Quick Red Tech.

## 🚀 Deploy to Render

### Method 1: GitHub Integration (Recommended)

1. **Push to GitHub:**
   ```bash
   git add .
   git commit -m "Prepare backend for Render deployment"
   git push origin main
   ```

2. **Deploy on Render:**
   - Go to [render.com](https://render.com)
   - Sign up/Login with GitHub
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Select the repository containing this backend
   - Configure:
     - **Name:** `idtfe-backend`
     - **Environment:** `Python 3`
     - **Build Command:** `pip install -r requirements.txt`
     - **Start Command:** `python main.py`
     - **Plan:** Free (or paid for better performance)

### Method 2: Direct Git Deploy

1. **Create Render Service:**
   - Go to [render.com](https://render.com)
   - Click "New +" → "Web Service"
   - Choose "Deploy from Git repository"
   - Enter your repository URL

2. **Configure Service:**
   ```
   Name: idtfe-backend
   Environment: Python 3
   Build Command: pip install -r requirements.txt
   Start Command: python main.py
   ```

### Method 3: Manual Upload

1. **Zip the backend folder**
2. **Upload to Render:**
   - Go to Render dashboard
   - Click "New +" → "Web Service"
   - Choose "Deploy from uploaded files"
   - Upload your zip file

## 🔧 Configuration

### Environment Variables (Optional)
- `PORT` - Automatically set by Render
- `FLASK_ENV` - Set to `production`
- `PYTHON_VERSION` - Set to `3.11.0`

### Custom Domain (Optional)
- Go to your service settings
- Add custom domain
- Update DNS records

## 📡 API Endpoints

Once deployed, your API will be available at:
```
https://your-service-name.onrender.com/api/v1/health
https://your-service-name.onrender.com/api/v1/meta
https://your-service-name.onrender.com/api/v1/version
```

## 🏢 About

**Developer:** Chisom Life Eke  
**Company:** Quick Red Tech  
**GitHub:** QRTQuick  
**License:** Open Source