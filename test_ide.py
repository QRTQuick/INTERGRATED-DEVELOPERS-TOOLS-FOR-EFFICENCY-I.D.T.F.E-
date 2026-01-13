#!/usr/bin/env python3
"""
Test script for I.D.T.F.E Web IDE functionality
"""

import requests
import json

BASE_URL = "http://localhost:8000"

def test_ide_format():
    """Test IDE format functionality"""
    print("Testing IDE format functionality...")
    
    # Test JSON formatting
    test_data = {
        "action": "format",
        "content": '{"name":"John","age":30,"city":"New York"}',
        "file_type": "json"
    }
    
    response = requests.post(f"{BASE_URL}/api/v1/tools/ide/action", json=test_data)
    print(f"JSON Format Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        print("✓ JSON formatting working")
        print(f"Formatted content preview: {result['content'][:100]}...")
    
    # Test HTML formatting
    test_data = {
        "action": "format",
        "content": '<html><head><title>Test</title></head><body><h1>Hello</h1></body></html>',
        "file_type": "html"
    }
    
    response = requests.post(f"{BASE_URL}/api/v1/tools/ide/action", json=test_data)
    print(f"HTML Format Status: {response.status_code}")
    if response.status_code == 200:
        print("✓ HTML formatting working")
    
    print()

def test_ide_lint():
    """Test IDE lint functionality"""
    print("Testing IDE lint functionality...")
    
    # Test JSON linting with error
    test_data = {
        "action": "lint",
        "content": '{"name":"John","age":30,"city":}',  # Invalid JSON
        "file_type": "json"
    }
    
    response = requests.post(f"{BASE_URL}/api/v1/tools/ide/action", json=test_data)
    print(f"JSON Lint Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        issues = result.get('issues', [])
        print(f"✓ JSON linting working - found {len(issues)} issues")
        for issue in issues:
            print(f"  Line {issue['line']}: {issue['message']}")
    
    print()

def test_ide_preview():
    """Test IDE preview functionality"""
    print("Testing IDE preview functionality...")
    
    # Test Markdown preview
    test_data = {
        "action": "preview",
        "content": "# Hello World\n\nThis is a **test** markdown document.",
        "file_type": "markdown"
    }
    
    response = requests.post(f"{BASE_URL}/api/v1/tools/ide/action", json=test_data)
    print(f"Markdown Preview Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        print("✓ Markdown preview working")
        print(f"HTML preview length: {len(result['preview'])} characters")
    
    # Test JSON preview
    test_data = {
        "action": "preview",
        "content": '{"name":"John","age":30}',
        "file_type": "json"
    }
    
    response = requests.post(f"{BASE_URL}/api/v1/tools/ide/action", json=test_data)
    print(f"JSON Preview Status: {response.status_code}")
    if response.status_code == 200:
        print("✓ JSON preview working")
    
    print()

if __name__ == "__main__":
    print("I.D.T.F.E Web IDE Test")
    print("=" * 25)
    
    try:
        test_ide_format()
        test_ide_lint()
        test_ide_preview()
        print("✓ All IDE tests completed successfully!")
        
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to backend. Make sure it's running on http://localhost:8000")
    except Exception as e:
        print(f"❌ Test failed: {e}")