import PyInstaller.__main__
import os
import sys

def build_server():
    """Compile le serveur Python en executable"""
    script_path = os.path.join(os.getcwd(), 'src', 'main', 'python', 'server.py')
    output_path = os.path.join(os.getcwd(), 'src', 'main', 'resources', 'extra')
    os.makedirs(output_path, exist_ok=True)

    PyInstaller.__main__.run([
        script_path,
        '--onefile',
        '--name=server',
        f'--distpath={output_path}',
        '--noconsole',
        '--hidden-import=websockets',
        '--hidden-import=httpx',
        '--hidden-import=beautifulsoup4',
        '--hidden-import=fake_useragent',
        '--hidden-import=cloudscraper',
        '--hidden-import=undetected_chromedriver',
        '--hidden-import=selenium',
        '--hidden-import=asyncio',
        '--hidden-import=logging',
        '--hidden-import=json',
        '--hidden-import=random',
        '--hidden-import=time',
        '--hidden-import=bs4',
    ])

if __name__ == "__main__":
    build_server()