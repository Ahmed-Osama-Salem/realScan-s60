#!/bin/bash

# SignalR Testing Helper Script
# This script helps test SignalR connection and provides useful debugging commands

echo "=========================================="
echo "SignalR Testing Helper"
echo "=========================================="
echo ""

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ ERROR: No Android device connected"
    echo "Please connect your device via USB and enable USB debugging"
    exit 1
fi

echo "✅ Device connected"
echo ""

# Menu
echo "Select an option:"
echo "1. Monitor SignalR logs (real-time)"
echo "2. Clear logs and monitor"
echo "3. Check app status"
echo "4. Restart app"
echo "5. View all SignalR logs (saved to file)"
echo "6. Test connection status"
echo "7. Exit"
echo ""
read -p "Enter choice [1-7]: " choice

case $choice in
    1)
        echo ""
        echo "Starting log monitor..."
        echo "Press Ctrl+C to stop"
        echo ""
        adb logcat -v time | grep --line-buffered -E "(SignalRManager|SignalR|Server_RequestAction|UploadFingerprint|Server_Error|Server_Ack)"
        ;;
    2)
        echo ""
        echo "Clearing logs..."
        adb logcat -c
        echo "Starting log monitor..."
        echo "Press Ctrl+C to stop"
        echo ""
        adb logcat -v time | grep --line-buffered -E "(SignalRManager|SignalR|Server_RequestAction|UploadFingerprint|Server_Error|Server_Ack)"
        ;;
    3)
        echo ""
        echo "Checking app status..."
        if adb shell ps | grep -q "com.realscanseries.rssample"; then
            echo "✅ App is running"
            adb shell ps | grep "com.realscanseries.rssample"
        else
            echo "❌ App is not running"
        fi
        ;;
    4)
        echo ""
        echo "Restarting app..."
        adb shell am force-stop com.realscanseries.rssample
        sleep 1
        adb shell am start -n com.realscanseries.rssample/.SplashActivity
        echo "✅ App restarted"
        ;;
    5)
        echo ""
        read -p "Enter log filename (default: signalr_test.log): " logfile
        logfile=${logfile:-signalr_test.log}
        echo "Saving logs to $logfile..."
        echo "Press Ctrl+C to stop"
        adb logcat -v time | grep -E "(SignalRManager|SignalR|Server_RequestAction|UploadFingerprint|Server_Error|Server_Ack)" > "$logfile"
        echo "Logs saved to $logfile"
        ;;
    6)
        echo ""
        echo "Checking SignalR connection status..."
        echo "Look for these log messages:"
        echo "  ✅ 'SignalR CONNECTED SUCCESSFULLY'"
        echo "  ❌ 'SignalR CONNECTION FAILED'"
        echo ""
        echo "Recent connection logs:"
        adb logcat -d -v time | grep -E "(SignalR.*CONNECT|SignalR.*Connection)" | tail -20
        ;;
    7)
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

