#!/bin/bash
# Watch SignalR logs from Android device
# Usage: ./watch_signalr_logs.sh

echo "==================================="
echo "Watching SignalR Logs"
echo "==================================="
echo "Looking for:"
echo "  - 🔔 RECEIVED events"
echo "  - ✅ Success messages"  
echo "  - ❌ Error messages"
echo "  - 📡 Connection status"
echo ""
echo "Press Ctrl+C to stop"
echo "==================================="
echo ""

# Clear logcat and watch for SignalR related logs
adb logcat -c 2>/dev/null
adb logcat | grep -E "(SignalRManager|SignalR|RECEIVED|deviceId|fingerNumber|════|🔔|✅|❌|📡|👆)"
