#!/usr/bin/env python3
"""
Simple test script for I.D.T.F.E backend API
"""

import requests
import json

BASE_URL = "http://localhost:8000"

def test_health():
    """Test health endpoint"""
    print("Testing health endpoint...")
    response = requests.get(f"{BASE_URL}/api/v1/health")
    print(f"Status: {response.status_code}")
    print(f"Response: {response.json()}")
    print()

def test_metadata():
    """Test metadata endpoint"""
    print("Testing metadata endpoint...")
    response = requests.get(f"{BASE_URL}/api/v1/meta")
    print(f"Status: {response.status_code}")
    data = response.json()
    print(f"App: {data['app_name']} v{data['version']}")
    print(f"Modules: {len(data['modules'])}")
    for module in data['modules']:
        print(f"  - {module['name']} ({module['id']}): {'✓' if module['enabled'] else '✗'}")
    print()

def test_api_tester():
    """Test API tester endpoint"""
    print("Testing API tester endpoint...")
    test_data = {
        "url": "https://httpbin.org/get",
        "method": "GET",
        "headers": {"User-Agent": "IDTFE-Test"},
        "body": ""
    }
    
    response = requests.post(f"{BASE_URL}/api/v1/tools/api-tester", json=test_data)
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        print(f"Target Status: {result['status_code']}")
        print(f"Duration: {result['duration']}s")
        print("✓ API Tester working")
    else:
        print(f"Error: {response.text}")
    print()

def test_markdown_renderer():
    """Test markdown renderer endpoint"""
    print("Testing markdown renderer endpoint...")
    test_data = {
        "content": "# Hello World\n\nThis is a **test** markdown document.\n\n- Item 1\n- Item 2"
    }
    
    response = requests.post(f"{BASE_URL}/api/v1/tools/markdown/render", json=test_data)
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        print("✓ Markdown renderer working")
        print(f"HTML length: {len(result['html'])} characters")
    else:
        print(f"Error: {response.text}")
    print()

if __name__ == "__main__":
    print("I.D.T.F.E Backend API Test")
    print("=" * 30)
    
    try:
        test_health()
        test_metadata()
        test_api_tester()
        test_markdown_renderer()
        print("✓ All tests completed successfully!")
        
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to backend. Make sure it's running on http://localhost:8000")
    except Exception as e:
        print(f"❌ Test failed: {e}")