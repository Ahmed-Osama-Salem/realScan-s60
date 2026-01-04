# SignalR Testing and Debugging Guide

## Quick Start - Monitor Logs

### Option 1: Use the Existing Script
```bash
# Make sure the script is executable
chmod +x watch_signalr_logs.sh

# Run the log monitor
./watch_signalr_logs.sh
```

### Option 2: Manual ADB Logcat
```bash
# Clear old logs
adb logcat -c

# Monitor SignalR logs only
adb logcat -v time | grep -E "(SignalRManager|SignalR|MainActivity.*SignalR)"

# Monitor all app logs
adb logcat -v time | grep "com.realscanseries.rssample"

# Monitor with specific tag
adb logcat SignalRManager:* MainActivity:* *:S
```

## Testing Checklist

### 1. Test SignalR Connection

**Steps:**
1. Launch the app on your device/emulator
2. Watch the logs for connection status
3. Look for these log messages:

```
✅ SUCCESS:
D/SignalRManager: === SignalR CONNECTED SUCCESSFULLY ===
D/SignalRManager: Connection State: CONNECTED

❌ FAILURE:
E/SignalRManager: === SignalR CONNECTION FAILED ===
E/SignalRManager: Error message: ...
```

**Expected Logs:**
```
D/SignalRManager: === SignalR Connect Attempt ===
D/SignalRManager: Hub URL: https://dev-operatorportal-notificationhubservice.cme.com/hubs/integration
D/SignalRManager: Creating new SignalR connection...
D/SignalRManager: Created trust-all SSLContext
D/SignalRManager: setHttpClientBuilderCallback configured successfully
D/SignalRManager: HubConnection created successfully
D/SignalRManager: Starting SignalR connection...
D/SignalRManager: === SignalR CONNECTED SUCCESSFULLY ===
```

### 2. Test Single Fingerprint Request

**From Frontend (JavaScript):**
```javascript
// Test Right Thumb (fingerNumber: 1)
signalRConnection.invoke("Server_RequestAction", {
    deviceId: 2,
    fingerNumber: 1
});

// Test Left Index (fingerNumber: 7)
signalRConnection.invoke("Server_RequestAction", {
    deviceId: 2,
    fingerNumber: 7
});
```

**Expected Logs:**
```
D/SignalRManager: Received Server_RequestAction with payload: {deviceId=2, fingerNumber=1}
D/SignalRManager: Parsing Map payload with keys: [deviceId, fingerNumber]
D/SignalRManager: Parsed - deviceId: 2, fingerNumber: 1
D/SignalRManager: Processing fingerprint request - frontend fingerNumber: 1, internal index: 5, deviceId: 2
D/MainActivity: SignalR: Received fingerprint request for index: 5
```

**What to Check:**
- ✅ Payload is received and parsed correctly
- ✅ Finger number is converted to correct internal index
- ✅ App shows "Please place your [Finger Name] on the scanner"
- ✅ Fingerprint is captured and sent via `UploadFingerprint`

### 3. Test Enrollment Request (Group Capture)

**From Frontend:**
```javascript
// Test Left 4 Fingers (fingerNumber: 13)
signalRConnection.invoke("Server_RequestAction", {
    deviceId: 2,
    fingerNumber: 13  // PlainLeftFourFingers
});

// Test Right 4 Fingers (fingerNumber: 14)
signalRConnection.invoke("Server_RequestAction", {
    deviceId: 2,
    fingerNumber: 14  // PlainRightFourFingers
});

// Test Thumbs (fingerNumber: 15)
signalRConnection.invoke("Server_RequestAction", {
    deviceId: 2,
    fingerNumber: 15  // PlainThumbs
});
```

**Expected Logs:**
```
D/SignalRManager: Received Server_RequestAction with payload: {deviceId=2, fingerNumber=13}
D/SignalRManager: Processing enrollment request for group: 13
D/MainActivity: SignalR: Received enrollment request
```

### 4. Test Fingerprint Upload

**Expected Logs When Fingerprint is Sent:**
```
D/SignalRManager: === Attempting to send fingerprint ===
D/SignalRManager: Finger Index: 5
D/SignalRManager: Image Width: 500, Height: 500
D/SignalRManager: Sending fingerprint via SignalR using event: UploadFingerprint
D/SignalRManager: === Fingerprint sent successfully for index: 5 ===
```

### 5. Test Error Handling

**Test Cases:**
1. **Device Not Connected:**
   - Disconnect fingerprint scanner
   - Send fingerprint request
   - Should see: `Server_Error` event sent

2. **Invalid Finger Number:**
   - Send request with `fingerNumber: 99`
   - Should see error in logs

3. **Null Payload:**
   - Frontend sends null/empty payload
   - Should handle gracefully

## Advanced Debugging

### View All SignalR Logs
```bash
# Comprehensive log filter
adb logcat -v time | grep -E "(SignalR|Fingerprint|Enrollment|Server_RequestAction|UploadFingerprint|Server_Error|Server_Ack)"
```

### Filter by Log Level
```bash
# Only errors
adb logcat *:E | grep SignalR

# Debug and above
adb logcat *:D | grep SignalR
```

### Save Logs to File
```bash
# Save all logs
adb logcat > signalr_debug.log

# Save filtered logs
adb logcat -v time | grep SignalR > signalr_filtered.log
```

### Clear Logs Before Testing
```bash
adb logcat -c
```

## Testing with Frontend

### 1. Setup Frontend Connection
```javascript
// Connect to SignalR hub
const connection = new signalR.HubConnectionBuilder()
    .withUrl("https://dev-operatorportal-notificationhubservice.cme.com/hubs/integration")
    .build();

// Start connection
await connection.start();
console.log("Connected to SignalR hub");

// Listen for responses
connection.on("UploadFingerprint", (fingerIndex, base64Image, width, height) => {
    console.log(`Received fingerprint ${fingerIndex}`);
    console.log(`Image size: ${width}x${height}`);
    console.log(`Base64 length: ${base64Image.length}`);
});

connection.on("Server_Error", (errorMessage) => {
    console.error("Server error:", errorMessage);
});

connection.on("Server_Ack", () => {
    console.log("Acknowledgment received");
});

connection.on("Server_DataReceived", () => {
    console.log("Data received confirmation");
});
```

### 2. Test Single Fingerprint
```javascript
// Request Right Thumb
connection.invoke("Server_RequestAction", {
    deviceId: 2,
    fingerNumber: 1
}).then(() => {
    console.log("Request sent successfully");
}).catch(err => {
    console.error("Error sending request:", err);
});
```

### 3. Test Enrollment
```javascript
// Request full enrollment (Left 4 fingers first)
connection.invoke("Server_RequestAction", {
    deviceId: 2,
    fingerNumber: 13  // PlainLeftFourFingers
});
```

## Common Issues and Solutions

### Issue 1: Connection Fails with SSL Error
**Symptoms:**
```
E/SignalRManager: Trust anchor for certification path not found
```

**Solution:**
- ✅ Already fixed with trust-all SSL configuration
- If still failing, check network security config

### Issue 2: Payload Not Parsed Correctly
**Symptoms:**
```
W/SignalRManager: Server_RequestAction received with no arguments
E/SignalRManager: Unsupported payload type
```

**Solution:**
- Check the actual payload format from frontend
- Add more logging to see what type of object is received
- May need to adjust parsing logic based on actual SignalR library behavior

### Issue 3: Finger Number Mapping Incorrect
**Symptoms:**
- Wrong finger is captured
- Finger index mismatch

**Solution:**
- Verify the mapping in `FingerScanType.toInternalIndex()`
- Check logs to see the conversion: `frontend fingerNumber: X, internal index: Y`

### Issue 4: Event Names Don't Match
**Symptoms:**
- Events not received
- Frontend can't send/receive

**Solution:**
- Verify event names in `SignalREventNames.java` match backend
- Check SignalR hub method names on server side

## Debugging Tips

### 1. Add More Logging
If something isn't working, add temporary logs:
```java
Log.d(TAG, "DEBUG: Variable value = " + variable);
```

### 2. Check Connection State
```java
// In SignalRManager, you can call:
signalRManager.logConnectionStatus();
```

### 3. Test Payload Format
Add logging to see exact payload structure:
```java
Log.d(TAG, "Payload class: " + payload.getClass().getName());
Log.d(TAG, "Payload toString: " + payload.toString());
```

### 4. Verify Finger Mapping
Test each finger number individually:
- fingerNumber: 1 (Right Thumb) → should map to internal index 5
- fingerNumber: 7 (Left Index) → should map to internal index 3
- etc.

## Testing Workflow

1. **Start Log Monitor:**
   ```bash
   ./watch_signalr_logs.sh
   ```

2. **Launch App:**
   - Install and launch on device
   - Wait for SignalR connection

3. **Connect Fingerprint Scanner:**
   - Ensure USB device is connected
   - Verify device is detected in app

4. **Test from Frontend:**
   - Send `Server_RequestAction` with test finger number
   - Watch logs for parsing and processing
   - Verify fingerprint capture
   - Check response events

5. **Verify End-to-End:**
   - Request → Parse → Capture → Send → Receive

## Quick Test Commands

```bash
# Monitor logs in real-time
adb logcat -v time | grep SignalR

# Check if app is running
adb shell ps | grep realscansample

# Clear app data (if needed)
adb shell pm clear com.realscanseries.rssample

# Restart app
adb shell am force-stop com.realscanseries.rssample
adb shell am start -n com.realscanseries.rssample/.SplashActivity
```

## Expected Behavior

### Successful Flow:
1. App launches → SignalR connects
2. Frontend sends `Server_RequestAction` → App receives and parses
3. App shows instruction → User places finger
4. Fingerprint captured → App sends `UploadFingerprint`
5. Frontend receives → Process complete

### Error Flow:
1. Error occurs → App sends `Server_Error`
2. Frontend receives error → Shows to user
3. Logs show detailed error information

## Next Steps

1. ✅ Test connection
2. ✅ Test single fingerprint (each finger 1-10)
3. ✅ Test enrollment (13, 14, 15)
4. ✅ Verify all events are sent correctly
5. ✅ Test error scenarios
6. ✅ Verify with actual frontend integration

