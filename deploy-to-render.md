# 🚀 Deploy I.D.T.F.E Backend to Render

## Step-by-Step Deployment Guide

### 1. **Prepare Your Repository**

```bash
# Navigate to your project root
cd /path/to/your/IDTFE/project

# Initialize git if not already done
git init

# Add all files
git add .

# Commit changes
git commit -m "Prepare I.D.T.F.E backend for Render deployment"

# Add remote repository (replace with your GitHub repo)
git remote add origin https://github.com/QRTQuick/IDTFE.git

# Push to GitHub
git push -u origin main
```

### 2. **Deploy on Render**

#### Option A: GitHub Integration (Recommended)

1. **Go to [render.com](https://render.com)**
2. **Sign up/Login** with your GitHub account
3. **Click "New +"** → **"Web Service"**
4. **Connect GitHub** and select your repository
5. **Configure the service:**
   ```
   Name: idtfe-backend
   Environment: Python 3
   Root Directory: backend
   Build Command: pip install -r requirements.txt
   Start Command: python main.py
   Plan: Free (or paid for better performance)
   ```
6. **Click "Create Web Service"**

#### Option B: Manual Git Deploy

1. **Go to [render.com](https://render.com)**
2. **Click "New +"** → **"Web Service"**
3. **Choose "Public Git repository"**
4. **Enter your repository URL**
5. **Configure as above**

### 3. **Get Your Deployment URL**

After deployment, Render will provide a URL like:
```
https://idtfe-backend.onrender.com
```

### 4. **Update Frontend Configuration**

To connect your JavaFX frontend to the Render backend:

```bash
# Run frontend with Render backend URL
java -Dbackend.url=https://your-service-name.onrender.com -jar your-app.jar

# Or set as environment variable
export BACKEND_URL=https://your-service-name.onrender.com
gradle run
```

### 5. **Test Your Deployment**

```bash
# Test health endpoint
curl https://your-service-name.onrender.com/api/v1/health

# Test metadata endpoint
curl https://your-service-name.onrender.com/api/v1/meta
```

## 🔧 Configuration Options

### Environment Variables (Optional)
- `PORT` - Automatically set by Render
- `FLASK_ENV` - Set to `production`
- `PYTHON_VERSION` - Set to `3.11.0`

### Custom Domain (Optional)
1. Go to your service settings in Render
2. Click "Custom Domains"
3. Add your domain
4. Update DNS records as instructed

## 📊 Monitoring

- **Logs:** Available in Render dashboard
- **Metrics:** CPU, Memory usage tracking
- **Health Checks:** Automatic monitoring
- **Auto-Deploy:** Automatic deployment on git push

## 💰 Pricing

- **Free Tier:** 750 hours/month, sleeps after 15 minutes of inactivity
- **Paid Plans:** Always-on, better performance, custom domains

## 🛠️ Troubleshooting

### Common Issues:

1. **Build Fails:**
   - Check `requirements.txt` format
   - Ensure Python version compatibility

2. **Service Won't Start:**
   - Check logs in Render dashboard
   - Verify `main.py` is in correct location

3. **CORS Issues:**
   - Frontend CORS is already configured
   - Check browser console for errors

### Support:
- **Render Docs:** [render.com/docs](https://render.com/docs)
- **GitHub Issues:** Create issue in your repository
- **Quick Red Tech:** Contact for support

## 🎉 Success!

Your I.D.T.F.E backend is now running on Render cloud platform!

**Next Steps:**
1. Update frontend to use your Render URL
2. Test all modules (API Tester, Web IDE, etc.)
3. Share your deployed application
4. Monitor usage and performance

---

**Developed by Chisom Life Eke | Quick Red Tech | Open Source**