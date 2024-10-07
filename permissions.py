import subprocess
import os

# Define full path to adb
adb_path = f"/Users/{os.getenv('USER')}/Library/Android/sdk/platform-tools/adb"  # Path with user environment variable

# Define package name and service paths
package_name = "com.sean8.jeminai"
voice_interaction_service = f"{package_name}/com.sean8.jeminai.core.voice.MyVoiceInteractionService"
accessibility_service = f"{package_name}/com.sean8.jeminai.core.accessibility.AppAccessibilityService"

# Helper function to run adb commands with the full adb path
def run_adb_command(command):
    full_command = f"{adb_path} {command}"
    try:
        result = subprocess.run(full_command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        print(f"Success: {result.stdout.decode('utf-8')}")
    except subprocess.CalledProcessError as e:
        print(f"Error: {e.stderr.decode('utf-8')}")

# Grant permissions
def grant_permissions():
    print("Granting permission RECORD_AUDIO...")
    run_adb_command(f"shell pm grant {package_name} android.permission.RECORD_AUDIO")

# Enable accessibility service
def enable_accessibility_service():
    print("Enabling accessibility service...")
    run_adb_command(f"shell settings put secure enabled_accessibility_services {accessibility_service}")
    run_adb_command("shell settings put secure accessibility_enabled 1")

# Run all setup steps
def setup_services():
    grant_permissions()
    enable_accessibility_service()

if __name__ == "__main__":
    setup_services()
