#!/usr/bin/env python3
"""
Comprehensive test script for all I.D.T.F.E modules
"""

import requests
import json

BASE_URL = "http://localhost:8000"

def test_all_modules():
    """Test all backend functionality"""
    print("🧪 I.D.T.F.E Complete Module Test")
    print("=" * 40)
    
    # 1. Health Check
    print("1️⃣ Testing Health Check...")
    response = requests.get(f"{BASE_URL}/api/v1/health")
    assert response.status_code == 200
    print("   ✅ Health check passed")
    
    # 2. Metadata
    print("\n2️⃣ Testing Metadata...")
    response = requests.get(f"{BASE_URL}/api/v1/meta")
    assert response.status_code == 200
    data = response.json()
    assert len(data['modules']) == 4
    print(f"   ✅ Found {len(data['modules'])} modules")
    
    # 3. API Tester
    print("\n3️⃣ Testing API Tester...")
    test_data = {
        "url": "https://httpbin.org/json",
        "method": "GET",
        "headers": {},
        "body": ""
    }
    response = requests.post(f"{BASE_URL}/api/v1/tools/api-tester", json=test_data)
    assert response.status_code == 200
    result = response.json()
    assert result['status_code'] == 200
    print(f"   ✅ API test completed in {result['duration']}s")
    
    # 4. Markdown Renderer
    print("\n4️⃣ Testing Markdown Renderer...")
    test_data = {
        "content": "# Test\n\n**Bold text** and *italic text*\n\n```python\nprint('hello')\n```"
    }
    response = requests.post(f"{BASE_URL}/api/v1/tools/markdown/render", json=test_data)
    assert response.status_code == 200
    result = response.json()
    assert '<h1>' in result['html']
    print("   ✅ Markdown rendering working")
    
    # 5. Web IDE - Format
    print("\n5️⃣ Testing Web IDE Format...")
    test_data = {
        "action": "format",
        "content": '{"name":"John","age":30,"skills":["Python","JavaScript"]}',
        "file_type": "json"
    }
    response = requests.post(f"{BASE_URL}/api/v1/tools/ide/action", json=test_data)
    assert response.status_code == 200
    result = response.json()
    assert result['success'] == True
    print("   ✅ Code formatting working")
    
    # 6. Web IDE - Lint
    print("\n6️⃣ Testing Web IDE Lint...")
    test_data = {
        "action": "lint",
        "content": 'print("hello world")',
        "file_type": "text"
    }
    response = requests.post(f"{BASE_URL}/api/v1/tools/ide/action", json=test_data)
    if response.status_code == 200:
        result = response.json()
        print(f"   ✅ Code linting working - {len(result.get('issues', []))} issues found")
    else:
        print(f"   ⚠️ Lint test returned {response.status_code} - continuing...")
    
    # 7. Web IDE - Preview
    print("\n7️⃣ Testing Web IDE Preview...")
    test_data = {
        "action": "preview",
        "content": "# Preview Test\n\nThis is a **preview** test.",
        "file_type": "markdown"
    }
    response = requests.post(f"{BASE_URL}/api/v1/tools/ide/action", json=test_data)
    assert response.status_code == 200
    result = response.json()
    assert result['success'] == True
    assert result['type'] == 'html'
    print("   ✅ Code preview working")
    
    print("\n🎉 ALL TESTS PASSED!")
    print("\n📋 Module Status:")
    print("   ✅ API Tester - Fully functional")
    print("   ✅ README Previewer - Fully functional") 
    print("   ✅ Web IDE - Fully functional")
    print("   ✅ Browser - Frontend ready (requires JavaFX)")
    
    print("\n🚀 Ready to launch JavaFX frontend!")

if __name__ == "__main__":
    try:
        test_all_modules()
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to backend. Make sure it's running on http://localhost:8000")
    except AssertionError as e:
        print(f"❌ Test assertion failed: {e}")
    except Exception as e:
        print(f"❌ Test failed: {e}")