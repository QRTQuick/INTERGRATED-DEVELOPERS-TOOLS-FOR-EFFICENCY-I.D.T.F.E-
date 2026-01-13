#!/usr/bin/env python3
"""
Test script to verify I.D.T.F.E backend is ready for Render deployment
"""

import requests
import subprocess
import sys
import os

def test_local_backend():
    """Test the local backend before deployment"""
    print("🧪 Testing I.D.T.F.E Backend for Render Deployment")
    print("=" * 50)
    
    # Test if backend is running locally
    try:
        response = requests.get("http://localhost:8000/api/v1/health", timeout=5)
        if response.status_code == 200:
            print("✅ Local backend is running")
            data = response.json()
            print(f"   Status: {data.get('status')}")
            print(f"   Message: {data.get('message')}")
        else:
            print("❌ Local backend returned error:", response.status_code)
            return False
    except requests.exceptions.RequestException:
        print("❌ Local backend is not running")
        print("   Please start the backend first: cd backend && python main.py")
        return False
    
    # Test metadata endpoint
    try:
        response = requests.get("http://localhost:8000/api/v1/meta", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print("✅ Metadata endpoint working")
            print(f"   App: {data.get('full_name')}")
            print(f"   Developer: {data.get('developer')}")
            print(f"   Company: {data.get('company')}")
        else:
            print("❌ Metadata endpoint failed")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Metadata endpoint error: {e}")
        return False
    
    return True

def check_deployment_files():
    """Check if all required deployment files exist"""
    print("\n📁 Checking Deployment Files...")
    
    required_files = [
        "backend/main.py",
        "backend/requirements.txt",
        "backend/Procfile",
        "backend/runtime.txt",
        "backend/render.yaml"
    ]
    
    all_files_exist = True
    for file_path in required_files:
        if os.path.exists(file_path):
            print(f"   ✅ {file_path}")
        else:
            print(f"   ❌ {file_path} - MISSING")
            all_files_exist = False
    
    return all_files_exist

def test_render_deployment_url(url):
    """Test a deployed Render URL"""
    print(f"\n🌐 Testing Render Deployment: {url}")
    
    try:
        # Test health endpoint
        response = requests.get(f"{url}/api/v1/health", timeout=10)
        if response.status_code == 200:
            data = response.json()
            print("✅ Render deployment is working!")
            print(f"   Status: {data.get('status')}")
            print(f"   Message: {data.get('message')}")
            print(f"   Port: {data.get('port', 'Not specified')}")
            
            # Test metadata
            meta_response = requests.get(f"{url}/api/v1/meta", timeout=10)
            if meta_response.status_code == 200:
                meta_data = meta_response.json()
                print("✅ All endpoints working on Render")
                print(f"   Deployment: {meta_data.get('deployment', 'Unknown')}")
                return True
            else:
                print("❌ Metadata endpoint failed on Render")
                return False
        else:
            print(f"❌ Render deployment returned error: {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Cannot connect to Render deployment: {e}")
        return False

def main():
    print("🚀 I.D.T.F.E Render Deployment Checker")
    print("Developed by Chisom Life Eke | Quick Red Tech")
    print("=" * 50)
    
    # Check deployment files
    if not check_deployment_files():
        print("\n❌ Some deployment files are missing!")
        print("   Please ensure all files are created before deploying.")
        return
    
    print("\n✅ All deployment files are present")
    
    # Test local backend
    if test_local_backend():
        print("\n✅ Local backend is ready for deployment!")
        
        print("\n📋 Next Steps:")
        print("1. Push your code to GitHub:")
        print("   git add .")
        print("   git commit -m 'Deploy I.D.T.F.E backend to Render'")
        print("   git push origin main")
        print("\n2. Deploy on Render:")
        print("   - Go to https://render.com")
        print("   - Create new Web Service")
        print("   - Connect your GitHub repository")
        print("   - Use 'backend' as root directory")
        print("   - Build command: pip install -r requirements.txt")
        print("   - Start command: python main.py")
        print("\n3. Test your deployment:")
        print("   python test_render_deployment.py --url https://your-service.onrender.com")
    else:
        print("\n❌ Local backend has issues. Please fix before deploying.")
    
    # Test Render URL if provided
    if len(sys.argv) > 2 and sys.argv[1] == "--url":
        render_url = sys.argv[2]
        test_render_deployment_url(render_url)

if __name__ == "__main__":
    main()