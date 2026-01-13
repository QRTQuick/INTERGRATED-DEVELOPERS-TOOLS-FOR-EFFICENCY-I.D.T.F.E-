#!/usr/bin/env python3
"""
Test script for I.D.T.F.E branding and metadata
"""

import requests
import json

BASE_URL = "http://localhost:8000"

def test_branding_metadata():
    """Test enhanced metadata with branding"""
    print("🏢 I.D.T.F.E Branding & Metadata Test")
    print("=" * 40)
    
    # Test enhanced metadata
    print("1️⃣ Testing Enhanced Metadata...")
    response = requests.get(f"{BASE_URL}/api/v1/meta")
    assert response.status_code == 200
    data = response.json()
    
    # Verify branding information
    assert data['app_name'] == 'IDTFE'
    assert data['full_name'] == 'Integrated Developer Tools for Efficiency'
    assert data['developer'] == 'Chisom Life Eke'
    assert data['company'] == 'Quick Red Tech'
    assert data['github'] == 'QRTQuick'
    assert data['license'] == 'Open Source'
    assert '2026 Quick Red Tech' in data['copyright']
    
    print(f"   ✅ App: {data['full_name']}")
    print(f"   ✅ Developer: {data['developer']}")
    print(f"   ✅ Company: {data['company']}")
    print(f"   ✅ GitHub: {data['github']}")
    print(f"   ✅ License: {data['license']}")
    print(f"   ✅ Copyright: {data['copyright']}")
    
    # Test enhanced version info
    print("\n2️⃣ Testing Enhanced Version Info...")
    response = requests.get(f"{BASE_URL}/api/v1/version")
    assert response.status_code == 200
    version_data = response.json()
    
    assert 'build_info' in version_data
    build_info = version_data['build_info']
    assert build_info['developer'] == 'Chisom Life Eke'
    assert build_info['company'] == 'Quick Red Tech'
    assert build_info['github'] == 'QRTQuick'
    assert build_info['license'] == 'Open Source'
    
    print(f"   ✅ Version: {version_data['version']}")
    print(f"   ✅ Build Date: {build_info['build_date']}")
    print(f"   ✅ Build Info Complete")
    
    # Test module descriptions
    print("\n3️⃣ Testing Module Descriptions...")
    modules = data['modules']
    for module in modules:
        assert 'description' in module
        print(f"   ✅ {module['name']}: {module['description']}")
    
    # Test feature flags
    print("\n4️⃣ Testing Feature Flags...")
    flags = data['feature_flags']
    expected_flags = ['auto_format', 'syntax_highlighting', 'live_preview', 'github_integration']
    for flag in expected_flags:
        assert flag in flags
        print(f"   ✅ {flag}: {flags[flag]}")
    
    print("\n🎉 ALL BRANDING TESTS PASSED!")
    print("\n📋 Application Summary:")
    print(f"   🏢 Company: {data['company']}")
    print(f"   👨‍💻 Developer: {data['developer']}")
    print(f"   📱 Application: {data['full_name']}")
    print(f"   🔗 GitHub: https://github.com/{data['github']}")
    print(f"   📄 License: {data['license']}")
    print(f"   📅 Copyright: {data['copyright']}")
    print(f"   🛠️ Modules: {len(modules)} available")
    print(f"   ⚙️ Features: {len(flags)} feature flags")
    
    print("\n🚀 Ready for professional deployment!")

if __name__ == "__main__":
    try:
        test_branding_metadata()
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to backend. Make sure it's running on http://localhost:8000")
    except AssertionError as e:
        print(f"❌ Test assertion failed: {e}")
    except Exception as e:
        print(f"❌ Test failed: {e}")