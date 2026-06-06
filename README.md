# Senux – Remote Command Runner for Arch Linux

Senux is an Android app that lets you send **any shell command** from your phone to your Arch Linux PC over your local Wi‑Fi. No cloud, no third‑party services – fully self‑hosted.

## Features
- Execute any terminal command on your Arch PC from your phone.
- Save frequently used commands in a dropdown.
- Scrollable output area with command history.
- Simple token‑based authentication.
- Works entirely on your home network.

## How It Works
1. You run a **Python Flask server** on your Arch Linux machine.
2. Your Android app sends HTTP POST requests to that server.
3. The server executes the command and returns the output.

## Requirements
- Arch Linux (or any Linux with Python)
- Android phone (Android 7.0 / API 24 or later)
- Both devices on the same Wi‑Fi network

---

##  Setting Up the Arch Linux Server

### 1. Install Python and create a virtual environment

```bash
sudo pacman -S python python-pip
python -m venv ~/senux-venv
source ~/senux-venv/bin/activate
pip install flask

```
### 2. Creating a server script
- theres prolly a better method but as a personal project its fine
- this run on your arch

[START]
from flask import Flask, request, jsonify
import subprocess

app = Flask(__name__)

# CHANGE THIS TO YOUR SECRET TOKEN (must match Android app)
SECRET_TOKEN = "your-secret-token"

@app.route('/exec', methods=['POST'])
def execute():
    token = request.headers.get('X-Auth-Token')
    if token != SECRET_TOKEN:
        return jsonify({"error": "Unauthorized"}), 401

    data = request.get_json()
    if not data or 'cmd' not in data:
        return jsonify({"error": "Missing cmd"}), 400

    cmd = data['cmd']
    try:
        result = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=30)
        output = result.stdout + result.stderr
        return jsonify({"output": output, "returncode": result.returncode})
    except subprocess.TimeoutExpired:
        return jsonify({"error": "Command timed out"}), 408
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)
[END]

### 3. Running the server

```bash
source ~/senux-venv/bin/activate
python ~/remote_shell_server.py

```

### 3. Final steps (from android studio)

3.1 Edit RetrofitClient.java on line(you need your arch ip here)

private static final String BASE_URL = "http://YOUR_ARCH_IP:5000/";

3.2 Edit ShellApi.java(you can put anything here as your token just keep in mind to remember it since you will need it for STEP 2)

@Headers("X-Auth-Token: your-secret-token")

### 3. Final notes

-will i update it?(nah i wont)
-why did you make it (too lazy to open my archisteam XD so i just run the script from my phone very nice)
-theres no delete on the saved commands(yah too lazy XD)
-will you even put effort for splash screen and so(theres like a unused ico but meh does it really matter XD)





