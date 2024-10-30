import subprocess
import os
import sys

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

# Start recording service using adb
def start_recording_service():
    print("Starting recording service...")
    run_adb_command("shell am startservice -a com.sean8.jeminai.ACTION_START_RECORDING")

def go_home():
    print("Going home...")
    run_adb_command("shell input keyevent KEYCODE_HOME")

# CLI menu interface
def cli_menu():
    while True:
        print("\nAndroid Service Manager - Choose an action:")
        print("1. Grant RECORD_AUDIO permission")
        print("2. Enable Accessibility Service")
        print("3. Run Full Setup (grant permissions and enable services)")
        print("4. Start Recording Service")
        print("5. Exit")

        try:
            choice = int(input("Enter your choice (1-5): "))
        except ValueError:
            print("Invalid input. Please enter a number between 1 and 5.")
            continue

        if choice == 1:
            grant_permissions()
        elif choice == 2:
            enable_accessibility_service()
        elif choice == 3:
            print("Running full setup...")
            grant_permissions()
            enable_accessibility_service()
            go_home()
        elif choice == 4:
            start_recording_service()
        elif choice == 5:
            print("Exiting...")
            break
        else:
            print("Invalid choice. Please select a valid option.")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        # Command-line argument(s) provided
        try:
            choice = int(sys.argv[1])
        except ValueError:
            print("Invalid input. Please enter a number between 1 and 5.")
            sys.exit(1)

        if choice == 1:
            grant_permissions()
        elif choice == 2:
            enable_accessibility_service()
        elif choice == 3:
            print("Running full setup...")
            grant_permissions()
            enable_accessibility_service()
            go_home()
        elif choice == 4:
            start_recording_service()
        elif choice == 5:
            print("Exiting...")
        else:
            print("Invalid choice. Please select a valid option.")
    else:
        cli_menu()
