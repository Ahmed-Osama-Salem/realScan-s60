# Debugging Server_RequestAction Not Received

## Quick Checklist

### 1. Verify Connection is Established
Look for these logs:
```
E/SignalRManager: === SignalR CONNECTED SUCCESSFULLY ===
E/SignalRManager: Connection State: CONNECTED
```

If you see `DISCONNECTED` or `CONNECTING`, the connection isn't ready.

### 2. Verify Event Name Matches
Check logs for:
```
D/SignalRManager: Event name: Server_RequestAction
D/SignalRManager: Full event name: 'Server_RequestAction'
```

**CRITICAL:** The frontend MUST send to exactly `"Server_RequestAction"` (case-sensitive, no spaces)

### 3. Check if Event is Received
Look for ANY of these logs when frontend sends:
```
E/SignalRManager: === RECEIVED Server_RequestAction (Object handler) ===
E/SignalRManager: === RECEIVED Server_RequestAction (String handler) ===
E/SignalRManager: === RECEIVED Server_RequestAction (Object[] handler) ===
E/SignalRManager: === RECEIVED Server_RequestAction (No parameters) ===
```

**If you DON'T see any of these**, the event is NOT being received. Possible causes:
- Event name mismatch
- Connection not actually connected
- Frontend not sending to correct hub/method

### 4. Verify Payload Format
Expected format from frontend:
```javascript
connection.invoke("Server_RequestAction", {
    deviceId: 2,
    fingerNumber: 1  // or any number 1-10, or 13-15
});
```

### 5. Check Connection Verification
After 3-5 seconds, you should see:
```
E/SignalRManager: === VERIFYING CONNECTION AND EVENTS ===
E/SignalRManager: ✅ Connection is CONNECTED
E/SignalRManager: Event name we're listening for: 'Server_RequestAction'
```

## Common Issues

### Issue 1: Event Name Mismatch
**Symptom:** No "RECEIVED" logs when frontend sends

**Solution:**
- Verify frontend is calling: `connection.invoke("Server_RequestAction", ...)`
- Check for typos: `Server_RequestAction` (not `ServerRequestAction`, `server_request_action`, etc.)
- Case-sensitive! Must match exactly

### Issue 2: Connection Not Actually Connected
**Symptom:** Connection logs show `DISCONNECTED` or `CONNECTING`

**Solution:**
- Check network connectivity
- Verify SSL certificate issues are resolved
- Check SignalR hub URL is correct
- Wait a few seconds after app starts

### Issue 3: Handlers Not Registered
**Symptom:** No "Registered handler" logs

**Solution:**
- Check for exceptions during handler registration
- Look for: `Failed to register ... handler`

### Issue 4: Payload Format Wrong
**Symptom:** Event received but parsing fails

**Solution:**
- Check logs show: `Payload type: ...`
- Verify payload is Map/JSON object, not string or array
- Frontend should send object, not JSON string

## Testing Steps

1. **Start the app and wait for connection:**
   ```bash
   adb logcat -c
   adb logcat -v time | grep SignalRManager
   ```

2. **Look for connection success:**
   ```
   E/SignalRManager: === SignalR CONNECTED SUCCESSFULLY ===
   ```

3. **Wait for verification (3-5 seconds):**
   ```
   E/SignalRManager: === VERIFYING CONNECTION AND EVENTS ===
   ```

4. **Have frontend send request:**
   ```javascript
   connection.invoke("Server_RequestAction", {
       deviceId: 2,
       fingerNumber: 1
   });
   ```

5. **Check logs for:**
   ```
   E/SignalRManager: === RECEIVED Server_RequestAction ===
   E/SignalRManager: === handleServerRequestAction CALLED ===
   E/SignalRManager: Parsed - deviceId: 2, fingerNumber: 1
   ```

## What to Share for Debugging

If still not working, share these logs:

1. Connection logs (when app starts)
2. Verification logs (after 3-5 seconds)
3. Any logs when frontend sends request
4. Full logcat output filtered by SignalRManager

## Frontend Code Check

Make sure frontend is using:
```javascript
// Correct way
connection.invoke("Server_RequestAction", {
    deviceId: 2,
    fingerNumber: 1
});

// NOT this (wrong event name)
connection.invoke("RequestFingerprint", ...);
connection.invoke("server_request_action", ...);

// NOT this (wrong format)
connection.invoke("Server_RequestAction", "deviceId:2,fingerNumber:1");
connection.invoke("Server_RequestAction", [2, 1]);
```

