package com.realscanseries.rssample;

import android.content.Context;
import android.util.Log;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.HttpHubConnectionBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.HashMap;

public class SignalRManager {
    private static final String TAG = "SignalRManager";
    private HubConnection hubConnection;
    private Context context;
    private SignalRListener listener;
    private String hubUrl;
    private String deviceId = "2";  // Default device ID
    private String currentRequestId = null;  // Store current request ID for upload
    
    public interface SignalRListener {
        void onFingerprintRequest(int fingerIndex, String requestId);
        void onEnrollmentRequest(String requestId);
        void onConnectionStateChanged(boolean connected);
        void onError(String error);
    }
    
    /**
     * FingerScanTypeEnum mapping from frontend
     * Frontend sends finger numbers directly (0-12)
     */
    public static class FingerScanType {
        // Individual fingers (0-9) - frontend sends these directly
        public static final int RIGHT_THUMB = 0;
        public static final int RIGHT_INDEX = 1;
        public static final int RIGHT_MIDDLE = 2;
        public static final int RIGHT_RING = 3;
        public static final int RIGHT_LITTLE = 4;
        public static final int LEFT_THUMB = 5;
        public static final int LEFT_INDEX = 6;
        public static final int LEFT_MIDDLE = 7;
        public static final int LEFT_RING = 8;
        public static final int LEFT_LITTLE = 9;
        
        // Group captures (10-12)
        public static final int PLAIN_LEFT_FOUR_FINGERS = 10;
        public static final int PLAIN_RIGHT_FOUR_FINGERS = 11;
        public static final int PLAIN_THUMBS = 12;
        
        /**
         * Validate and return the finger index
         * Frontend sends the index directly (0-12), no conversion needed
         * @param fingerNumber Finger number from frontend (0-12)
         * @return Same finger number if valid, -1 if invalid
         */
        public static int toInternalIndex(int fingerNumber) {
            // Frontend sends finger number directly (0-12)
            // 0-9 = individual fingers, 10-12 = group captures
            if (fingerNumber >= 0 && fingerNumber <= 12) {
                return fingerNumber;
            }
            return -1;
        }
        
        /**
         * Check if the finger number represents a group capture (enrollment)
         * Group captures are 10, 11, 12
         */
        public static boolean isGroupCapture(int fingerNumber) {
            return fingerNumber == PLAIN_LEFT_FOUR_FINGERS ||
                   fingerNumber == PLAIN_RIGHT_FOUR_FINGERS ||
                   fingerNumber == PLAIN_THUMBS;
        }
        
        /**
         * Check if the finger number is valid (0-12)
         */
        public static boolean isValid(int fingerNumber) {
            return fingerNumber >= 0 && fingerNumber <= 12;
        }
        
        /**
         * Get finger name for display
         */
        public static String getFingerName(int fingerNumber) {
            switch (fingerNumber) {
                case RIGHT_THUMB: return "Right Thumb";
                case RIGHT_INDEX: return "Right Index";
                case RIGHT_MIDDLE: return "Right Middle";
                case RIGHT_RING: return "Right Ring";
                case RIGHT_LITTLE: return "Right Little";
                case LEFT_THUMB: return "Left Thumb";
                case LEFT_INDEX: return "Left Index";
                case LEFT_MIDDLE: return "Left Middle";
                case LEFT_RING: return "Left Ring";
                case LEFT_LITTLE: return "Left Little";
                case PLAIN_LEFT_FOUR_FINGERS: return "Plain Left Four Fingers";
                case PLAIN_RIGHT_FOUR_FINGERS: return "Plain Right Four Fingers";
                case PLAIN_THUMBS: return "Plain Thumbs";
                default: return "Unknown";
            }
        }
    }
    
    public SignalRManager(Context context, String hubUrl) {
        this.context = context;
        this.hubUrl = hubUrl;
    }
    
    public void setListener(SignalRListener listener) {
        this.listener = listener;
    }
    
    public void connect() {
        Log.e(TAG, "════════════════════════════════════════════════════════");
        Log.e(TAG, "=== SignalR Connect Attempt ===");
        Log.e(TAG, "════════════════════════════════════════════════════════");
        Log.e(TAG, "Hub URL: " + hubUrl);
        
        if (hubConnection != null) {
            HubConnectionState currentState = hubConnection.getConnectionState();
            Log.e(TAG, "Existing connection state: " + currentState);
            
            if (currentState == HubConnectionState.CONNECTED) {
                Log.e(TAG, "Already connected to SignalR");
                return;
            } else if (currentState == HubConnectionState.CONNECTING) {
                Log.e(TAG, "Connection already in progress, waiting...");
                return;
            } else {
                Log.e(TAG, "Disconnecting existing connection before reconnecting...");
                try {
                    hubConnection.stop().blockingAwait();
                } catch (Exception e) {
                    Log.e(TAG, "Error stopping existing connection: " + e.getMessage());
                }
                hubConnection = null;
            }
        }
        
        try {
            Log.e(TAG, "Creating new SignalR connection...");
            Log.e(TAG, "WARNING: Using trust-all SSL configuration for development.");
            
            // Create the trust-all X509TrustManager
            final X509TrustManager trustAllManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    Log.d(TAG, "checkClientTrusted called - accepting");
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    Log.d(TAG, "checkServerTrusted called - accepting all certificates");
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            
            // Create the SSLContext with our trust-all manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustAllManager}, new SecureRandom());
            
            Log.e(TAG, "Created trust-all SSLContext");
            
            // Use HttpHubConnectionBuilder directly with setHttpClientBuilderCallback
            HttpHubConnectionBuilder builder = HubConnectionBuilder.create(hubUrl);
            
            // Configure the OkHttpClient builder with trust-all SSL
            builder.setHttpClientBuilderCallback(okHttpBuilder -> {
                Log.e(TAG, "Configuring OkHttpClient with trust-all SSL");
                try {
                    okHttpBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustAllManager);
                    okHttpBuilder.hostnameVerifier((hostname, session) -> {
                        Log.d(TAG, "Hostname verification for: " + hostname + " -> ACCEPTED");
                        return true;
                    });
                    Log.e(TAG, "OkHttpClient configured successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error configuring OkHttpClient: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            // Build the connection
            hubConnection = builder.build();
            Log.e(TAG, "HubConnection created successfully");
            
            // ═══════════════════════════════════════════════════════════════════════════
            // REGISTER EVENT HANDLERS - Using LinkedHashMap which is how SignalR deserializes JSON
            // ═══════════════════════════════════════════════════════════════════════════
            Log.e(TAG, "");
            Log.e(TAG, "═══════════════════════════════════════════════════════════");
            Log.e(TAG, "=== REGISTERING EVENT HANDLERS ===");
            Log.e(TAG, "═══════════════════════════════════════════════════════════");
            
            // ═══════════════════════════════════════════════════════════════════════════
            // EVENT HANDLERS - Based on working SDK implementation
            // Server sends TWO STRING parameters: requestId and fingerNumber
            // ═══════════════════════════════════════════════════════════════════════════
            
            // Server_RequestAction with TWO STRING parameters (like working SDK)
            try {
                hubConnection.on("Server_RequestAction", (String requestId, String fingerNumber) -> {
                    Log.e(TAG, "");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "🔔🔔🔔 RECEIVED Server_RequestAction 🔔🔔🔔");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "Request ID: " + requestId);
                    Log.e(TAG, "Finger Number: " + fingerNumber);
                    handleServerRequestAction(requestId, fingerNumber);
                }, String.class, String.class);
                Log.e(TAG, "✅ Registered handler for 'Server_RequestAction' (String, String)");
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to register Server_RequestAction: " + e.getMessage());
            }
            
            // Also try with single Object parameter as fallback
            try {
                hubConnection.on("Server_RequestAction_obj", (Object payload) -> {
                    Log.e(TAG, "");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "🔔 RECEIVED Server_RequestAction_obj (Object fallback)");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "Payload: " + payload);
                    Log.e(TAG, "Type: " + (payload != null ? payload.getClass().getName() : "null"));
                }, Object.class);
            } catch (Exception e) {
                // Silently ignore fallback
            }
            
            // server_info (lowercase - as in working SDK)
            try {
                hubConnection.on("server_info", (String message) -> {
                    Log.e(TAG, "");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "ℹ️ RECEIVED server_info (lowercase)");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "Message: " + message);
                }, String.class);
                Log.e(TAG, "✅ Registered handler for 'server_info' (lowercase)");
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to register server_info: " + e.getMessage());
            }
            
            // Server_Info (uppercase - keep for compatibility)
            try {
                hubConnection.on("Server_Info", (Object info) -> {
                    Log.e(TAG, "");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "ℹ️ RECEIVED Server_Info (uppercase)");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "Info: " + info);
                    Log.e(TAG, "Type: " + (info != null ? info.getClass().getName() : "null"));
                }, Object.class);
                Log.e(TAG, "✅ Registered handler for 'Server_Info' (uppercase)");
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to register Server_Info: " + e.getMessage());
            }
            
            // Server_Error handler
            try {
                hubConnection.on("Server_Error", (String errorMessage) -> {
                    Log.e(TAG, "");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "❌ RECEIVED Server_Error");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "Error Message: " + errorMessage);
                    if (listener != null) {
                        listener.onError(errorMessage);
                    }
                }, String.class);
                Log.e(TAG, "✅ Registered handler for 'Server_Error'");
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to register Server_Error: " + e.getMessage());
            }
            
            // Register a test handler to verify events work
            try {
                hubConnection.on("TestEvent", (Object payload) -> {
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "=== TEST EVENT RECEIVED! ===");
                    Log.e(TAG, "════════════════════════════════════════════════════════");
                    Log.e(TAG, "Payload: " + payload);
                    Log.e(TAG, "If you see this, event handlers ARE working!");
                }, Object.class);
                Log.e(TAG, "✅ Registered TestEvent handler");
            } catch (Exception e) {
                Log.e(TAG, "Failed to register TestEvent handler: " + e.getMessage());
            }
            
            // Connection closed handler
            hubConnection.onClosed((error) -> {
                if (error != null) {
                    Log.e(TAG, "Connection closed with error: " + error.getMessage());
                } else {
                    Log.e(TAG, "Connection closed normally");
                }
                if (listener != null) {
                    listener.onConnectionStateChanged(false);
                }
            });
            
            Log.e(TAG, "");
            Log.e(TAG, "═══════════════════════════════════════════════════════════");
            Log.e(TAG, "=== STARTING CONNECTION ===");
            Log.e(TAG, "═══════════════════════════════════════════════════════════");
            
            hubConnection.start().subscribe(() -> {
                Log.e(TAG, "");
                Log.e(TAG, "════════════════════════════════════════════════════════");
                Log.e(TAG, "✅✅✅ SignalR CONNECTED SUCCESSFULLY ✅✅✅");
                Log.e(TAG, "════════════════════════════════════════════════════════");
                Log.e(TAG, "Connection State: " + hubConnection.getConnectionState());
                Log.e(TAG, "Hub URL: " + hubUrl);
                Log.e(TAG, "");
                Log.e(TAG, "📡 LISTENING FOR EVENTS:");
                Log.e(TAG, "  - Server_RequestAction");
                Log.e(TAG, "  - server_RequestAction");
                Log.e(TAG, "  - ServerRequestAction");
                Log.e(TAG, "  - requestAction");
                Log.e(TAG, "  - RequestAction");
                Log.e(TAG, "  - Server_Info");
                Log.e(TAG, "  - TestEvent");
                Log.e(TAG, "");
                Log.e(TAG, "📋 Expected payload: { \"deviceId\": 2, \"fingerNumber\": 1 }");
                Log.e(TAG, "════════════════════════════════════════════════════════");
                
                if (listener != null) {
                    listener.onConnectionStateChanged(true);
                }
            }, (error) -> {
                Log.e(TAG, "════════════════════════════════════════════════════════");
                Log.e(TAG, "❌❌❌ SignalR CONNECTION FAILED ❌❌❌");
                Log.e(TAG, "════════════════════════════════════════════════════════");
                Log.e(TAG, "Error: " + (error != null ? error.getMessage() : "Unknown error"));
                if (error != null) {
                    Log.e(TAG, "Error class: " + error.getClass().getName());
                    if (error.getCause() != null) {
                        Log.e(TAG, "Cause: " + error.getCause().getMessage());
                    }
                    error.printStackTrace();
                }
                if (listener != null) {
                    listener.onError(error != null ? error.getMessage() : "Unknown connection error");
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "════════════════════════════════════════════════════════");
            Log.e(TAG, "❌ EXCEPTION creating SignalR connection");
            Log.e(TAG, "════════════════════════════════════════════════════════");
            Log.e(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
            if (listener != null) {
                listener.onError("Exception: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle Server_RequestAction with requestId and fingerNumber (String parameters)
     * Frontend sends fingerNumber directly (0-12):
     *   0=RightThumb, 1=RightIndex, 2=RightMiddle, 3=RightRing, 4=RightLittle,
     *   5=LeftThumb, 6=LeftIndex, 7=LeftMiddle, 8=LeftRing, 9=LeftLittle,
     *   10=PlainLeftFourFingers, 11=PlainRightFourFingers, 12=PlainThumbs
     */
    private void handleServerRequestAction(String requestId, String fingerNumber) {
        Log.e(TAG, "");
        Log.e(TAG, "=== PROCESSING Server_RequestAction ===");
        Log.e(TAG, "Request ID: " + requestId);
        Log.e(TAG, "Finger Number (string): " + fingerNumber);
        
        // Store the request ID for later use when uploading
        this.currentRequestId = requestId;
        
        if (listener == null) {
            Log.e(TAG, "❌ No listener set!");
            return;
        }
        
        try {
            // Parse fingerNumber string to int
            int fingerNum = Integer.parseInt(fingerNumber);
            Log.e(TAG, "Finger Number (int): " + fingerNum);
            Log.e(TAG, "Finger Name: " + FingerScanType.getFingerName(fingerNum));
            
            // Validate finger number (0-12)
            if (!FingerScanType.isValid(fingerNum)) {
                Log.e(TAG, "❌ Invalid fingerNumber: " + fingerNum + " (must be 0-12)");
                listener.onError("Invalid fingerNumber: " + fingerNum);
                return;
            }
            
            // All finger requests (0-12) go through onFingerprintRequest
            // The MainActivity will handle the correct capture mode based on index
            Log.e(TAG, "");
            Log.e(TAG, "👆 FINGERPRINT REQUEST:");
            Log.e(TAG, "  Request ID: " + requestId);
            Log.e(TAG, "  Finger Index: " + fingerNum);
            Log.e(TAG, "  Finger Name: " + FingerScanType.getFingerName(fingerNum));
            Log.e(TAG, "  Is Group Capture: " + FingerScanType.isGroupCapture(fingerNum));
            listener.onFingerprintRequest(fingerNum, requestId);
        } catch (NumberFormatException e) {
            Log.e(TAG, "❌ Cannot parse fingerNumber: " + fingerNumber);
            listener.onError("Invalid fingerNumber format: " + fingerNumber);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing request: " + e.getMessage());
            e.printStackTrace();
            listener.onError("Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Set the device ID to use when uploading fingerprints
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        Log.e(TAG, "Device ID set to: " + deviceId);
    }
    
    /**
     * Get current request ID
     */
    public String getCurrentRequestId() {
        return currentRequestId;
    }
    
    /**
     * Register a handler for Server_RequestAction with a specific event name
     * Using Object.class like Server_Info which works correctly
     */
    private void registerRequestActionHandler(String eventName) {
        try {
            // Use Object.class - same as Server_Info which is working
            hubConnection.on(eventName, (Object payload) -> {
                Log.e(TAG, "");
                Log.e(TAG, "════════════════════════════════════════════════════════");
                Log.e(TAG, "🔔🔔🔔 RECEIVED EVENT: '" + eventName + "' 🔔🔔🔔");
                Log.e(TAG, "════════════════════════════════════════════════════════");
                Log.e(TAG, "Payload: " + payload);
                Log.e(TAG, "Payload type: " + (payload != null ? payload.getClass().getName() : "null"));
                Log.e(TAG, "Payload toString: " + (payload != null ? payload.toString() : "null"));
                
                // Process the payload based on its type
                if (payload instanceof Map) {
                    Log.e(TAG, "Payload is Map - processing...");
                    processRequestAction((Map<?, ?>) payload);
                } else if (payload instanceof String) {
                    Log.e(TAG, "Payload is String - attempting to parse...");
                    processStringPayload((String) payload);
                } else if (payload != null) {
                    Log.e(TAG, "Payload is unknown type: " + payload.getClass().getName());
                    Log.e(TAG, "Attempting to process anyway...");
                    // Try to convert to string and parse
                    processStringPayload(payload.toString());
                } else {
                    Log.e(TAG, "Payload is NULL!");
                }
            }, Object.class);
            Log.e(TAG, "✅ Registered handler for '" + eventName + "' (Object.class)");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to register handler for '" + eventName + "': " + e.getMessage());
        }
    }
    
    /**
     * Process a string payload - try to parse JSON-like format
     */
    private void processStringPayload(String payload) {
        Log.e(TAG, "Processing string payload: " + payload);
        
        if (payload == null || payload.isEmpty()) {
            Log.e(TAG, "Empty string payload");
            return;
        }
        
        try {
            int deviceId = -1;
            int fingerNumber = -1;
            
            // Try to extract deviceId and fingerNumber from JSON-like string
            // Format: {"deviceId":2,"fingerNumber":1} or {"deviceId":"2","fingerNumber":"1"}
            
            // Extract deviceId
            if (payload.contains("deviceId")) {
                int start = payload.indexOf("deviceId");
                int colonPos = payload.indexOf(":", start);
                if (colonPos > 0) {
                    int endPos = payload.indexOf(",", colonPos);
                    if (endPos < 0) endPos = payload.indexOf("}", colonPos);
                    if (endPos > colonPos) {
                        String value = payload.substring(colonPos + 1, endPos).trim();
                        // Remove quotes if present
                        value = value.replace("\"", "").trim();
                        try {
                            deviceId = Integer.parseInt(value);
                            Log.e(TAG, "Extracted deviceId: " + deviceId);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Could not parse deviceId: " + value);
                        }
                    }
                }
            }
            
            // Extract fingerNumber
            if (payload.contains("fingerNumber")) {
                int start = payload.indexOf("fingerNumber");
                int colonPos = payload.indexOf(":", start);
                if (colonPos > 0) {
                    int endPos = payload.indexOf(",", colonPos);
                    if (endPos < 0) endPos = payload.indexOf("}", colonPos);
                    if (endPos > colonPos) {
                        String value = payload.substring(colonPos + 1, endPos).trim();
                        // Remove quotes if present
                        value = value.replace("\"", "").trim();
                        try {
                            fingerNumber = Integer.parseInt(value);
                            Log.e(TAG, "Extracted fingerNumber: " + fingerNumber);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Could not parse fingerNumber: " + value);
                        }
                    }
                }
            }
            
            if (fingerNumber >= 0) {
                Log.e(TAG, "Successfully parsed - deviceId: " + deviceId + ", fingerNumber: " + fingerNumber);
                
                // Create a map and process
                HashMap<String, Object> map = new HashMap<>();
                map.put("deviceId", deviceId);
                map.put("fingerNumber", fingerNumber);
                processRequestAction(map);
            } else {
                Log.e(TAG, "Could not extract fingerNumber from payload");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing string payload: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Register a handler for Server_Info with a specific event name
     */
    private void registerInfoHandler(String eventName) {
        try {
            hubConnection.on(eventName, (Object info) -> {
                Log.e(TAG, "");
                Log.e(TAG, "════════════════════════════════════════════════════════");
                Log.e(TAG, "ℹ️ RECEIVED Server_Info: '" + eventName + "'");
                Log.e(TAG, "════════════════════════════════════════════════════════");
                Log.e(TAG, "Info: " + info);
                Log.e(TAG, "Type: " + (info != null ? info.getClass().getName() : "null"));
            }, Object.class);
            Log.e(TAG, "✅ Registered handler for '" + eventName + "'");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to register handler for '" + eventName + "': " + e.getMessage());
        }
    }
    
    /**
     * Process the Server_RequestAction payload
     */
    private void processRequestAction(Map<?, ?> payload) {
        Log.e(TAG, "");
        Log.e(TAG, "=== PROCESSING REQUEST ACTION ===");
        
        if (payload == null) {
            Log.e(TAG, "❌ Payload is null!");
            if (listener != null) {
                listener.onError("Invalid request: null payload");
            }
            return;
        }
        
        if (listener == null) {
            Log.e(TAG, "❌ No listener set!");
            return;
        }
        
        try {
            // Log all keys and values
            Log.e(TAG, "Payload contents:");
            for (Object key : payload.keySet()) {
                Object value = payload.get(key);
                Log.e(TAG, "  " + key + " = " + value + " (type: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
            }
            
            int deviceId = -1;
            int fingerNumber = -1;
            
            // Get deviceId
            Object deviceIdObj = payload.get("deviceId");
            if (deviceIdObj != null) {
                deviceId = parseToInt(deviceIdObj, "deviceId");
            }
            
            // Get fingerNumber
            Object fingerNumberObj = payload.get("fingerNumber");
            if (fingerNumberObj != null) {
                fingerNumber = parseToInt(fingerNumberObj, "fingerNumber");
            }
            
            Log.e(TAG, "");
            Log.e(TAG, "📊 PARSED VALUES:");
            Log.e(TAG, "  deviceId: " + deviceId);
            Log.e(TAG, "  fingerNumber: " + fingerNumber);
            Log.e(TAG, "  fingerName: " + FingerScanType.getFingerName(fingerNumber));
            Log.e(TAG, "  isGroupCapture: " + FingerScanType.isGroupCapture(fingerNumber));
            
            // Validate finger number (0-12)
            if (!FingerScanType.isValid(fingerNumber)) {
                Log.e(TAG, "❌ Invalid fingerNumber: " + fingerNumber + " (must be 0-12)");
                listener.onError("Invalid fingerNumber: " + fingerNumber);
                return;
            }
            
            // All finger requests (0-12) go through onFingerprintRequest
            // MainActivity will set the correct capture mode based on fingerNumber
            Log.e(TAG, "");
            Log.e(TAG, "👆 FINGERPRINT REQUEST:");
            Log.e(TAG, "  Finger Index: " + fingerNumber);
            Log.e(TAG, "  Finger Name: " + FingerScanType.getFingerName(fingerNumber));
            Log.e(TAG, "  DeviceId: " + deviceId);
            Log.e(TAG, "  Is Group Capture: " + FingerScanType.isGroupCapture(fingerNumber));
            listener.onFingerprintRequest(fingerNumber, currentRequestId != null ? currentRequestId : "unknown");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing request: " + e.getMessage());
            e.printStackTrace();
            if (listener != null) {
                listener.onError("Error processing request: " + e.getMessage());
            }
        }
    }
    
    /**
     * Parse an object to int - handles Number, String, and other types
     */
    private int parseToInt(Object obj, String fieldName) {
        if (obj == null) {
            Log.e(TAG, "  " + fieldName + ": null");
            return -1;
        }
        
        Log.e(TAG, "  Parsing " + fieldName + ": " + obj + " (type: " + obj.getClass().getSimpleName() + ")");
        
        if (obj instanceof Number) {
            int value = ((Number) obj).intValue();
            Log.e(TAG, "  " + fieldName + " parsed as Number -> " + value);
            return value;
        } else if (obj instanceof String) {
            try {
                String str = ((String) obj).trim();
                int value = Integer.parseInt(str);
                Log.e(TAG, "  " + fieldName + " parsed as String \"" + str + "\" -> " + value);
                return value;
            } catch (NumberFormatException e) {
                Log.e(TAG, "  ❌ Cannot parse " + fieldName + " string: \"" + obj + "\"");
                return -1;
            }
        } else {
            try {
                int value = Integer.parseInt(obj.toString().trim());
                Log.e(TAG, "  " + fieldName + " parsed from toString() -> " + value);
                return value;
            } catch (NumberFormatException e) {
                Log.e(TAG, "  ❌ Cannot parse " + fieldName + ": " + obj + " (type: " + obj.getClass().getName() + ")");
                return -1;
            }
        }
    }
    
    public void disconnect() {
        if (hubConnection != null) {
            hubConnection.stop().subscribe(() -> {
                Log.e(TAG, "SignalR disconnected");
            }, (error) -> {
                Log.e(TAG, "Error disconnecting: " + (error != null ? error.getMessage() : "Unknown"));
            });
        }
    }
    
    /**
     * Upload fingerprint to server (matches working SDK pattern)
     * @param requestId The request ID from Server_RequestAction
     * @param base64Image The fingerprint image as base64 string
     */
    public void uploadFingerprint(String requestId, String base64Image) {
        Log.e(TAG, "");
        Log.e(TAG, "════════════════════════════════════════════════════════");
        Log.e(TAG, "=== UPLOADING FINGERPRINT ===");
        Log.e(TAG, "════════════════════════════════════════════════════════");
        Log.e(TAG, "Request ID: " + requestId);
        Log.e(TAG, "Device ID: " + deviceId);
        Log.e(TAG, "Base64 length: " + (base64Image != null ? base64Image.length() : "null"));
        
        if (hubConnection == null) {
            Log.e(TAG, "❌ Cannot send: hubConnection is NULL");
            if (listener != null) {
                listener.onError("SignalR hubConnection is null");
            }
            return;
        }
        
        HubConnectionState state = hubConnection.getConnectionState();
        if (state != HubConnectionState.CONNECTED) {
            Log.e(TAG, "❌ Cannot send: not connected. State: " + state);
            if (listener != null) {
                listener.onError("SignalR not connected. State: " + state);
            }
            return;
        }
        
        try {
            // Log first 100 characters of base64 to verify format
            if (base64Image != null) {
                String preview = base64Image.length() > 100 ? base64Image.substring(0, 100) + "..." : base64Image;
                Log.e(TAG, "Base64 preview: " + preview);
            }
            
            Log.e(TAG, "Sending to hub method: UploadFingerprint");
            Log.e(TAG, "Parameters: requestId=" + requestId + ", deviceId=" + deviceId + ", base64Image=<" + (base64Image != null ? base64Image.length() : 0) + " chars>");
            
            // Send fingerprint data (matches working SDK pattern)
            hubConnection.send("UploadFingerprint", requestId, deviceId, base64Image);
            
            Log.e(TAG, "✅ Fingerprint upload initiated");
            
            // Send acknowledgment to server after successful upload
            sendServerAck(requestId);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error uploading fingerprint: " + e.getMessage());
            Log.e(TAG, "Error type: " + e.getClass().getName());
            if (e.getCause() != null) {
                Log.e(TAG, "Error cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            if (listener != null) {
                listener.onError("Upload error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Send acknowledgment to server after successfully sending data
     * @param requestId The request ID to acknowledge
     */
    public void sendServerAck(String requestId) {
        if (hubConnection == null || hubConnection.getConnectionState() != HubConnectionState.CONNECTED) {
            Log.e(TAG, "Cannot send ack: not connected");
            return;
        }

        try {
            Log.e(TAG, "=== Sending Server_Ack ===");
            Log.e(TAG, "Request ID: " + requestId);
            
            // Send acknowledgment to server
            hubConnection.send("Server_Ack", requestId);
            
            Log.e(TAG, "✅ Server acknowledgment sent successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending acknowledgment: " + e.getMessage());
        }
    }
    
    /**
     * Legacy method - use uploadFingerprint instead
     * @deprecated Use uploadFingerprint(requestId, base64Image) instead
     */
    @Deprecated
    public void sendFingerprint(int fingerIndex, String base64Image, int width, int height) {
        Log.e(TAG, "=== sendFingerprint (legacy) ===");
        Log.e(TAG, "Finger Index: " + fingerIndex);
        Log.e(TAG, "Image dimensions: " + width + "x" + height);
        
        // Use current request ID if available
        if (currentRequestId != null) {
            uploadFingerprint(currentRequestId, base64Image);
        } else {
            Log.e(TAG, "❌ No request ID available. Use uploadFingerprint(requestId, base64Image) instead.");
            if (listener != null) {
                listener.onError("No request ID available");
            }
        }
    }
    
    public void sendError(int fingerIndex, String errorMessage) {
        if (hubConnection == null || hubConnection.getConnectionState() != HubConnectionState.CONNECTED) {
            return;
        }
        
        try {
            hubConnection.send(SignalREventNames.SERVER_ERROR, fingerIndex, errorMessage);
            Log.e(TAG, "Sent error for index " + fingerIndex + ": " + errorMessage);
        } catch (Exception e) {
            Log.e(TAG, "Error sending error message: " + e.getMessage());
        }
    }
    
    public void sendEnrollmentError(String errorMessage) {
        if (hubConnection == null || hubConnection.getConnectionState() != HubConnectionState.CONNECTED) {
            return;
        }
        
        try {
            hubConnection.send(SignalREventNames.SERVER_ERROR, errorMessage);
            Log.e(TAG, "Sent enrollment error: " + errorMessage);
        } catch (Exception e) {
            Log.e(TAG, "Error sending enrollment error: " + e.getMessage());
        }
    }
    
    public void sendEnrollmentComplete() {
        if (hubConnection == null || hubConnection.getConnectionState() != HubConnectionState.CONNECTED) {
            return;
        }
        
        try {
            hubConnection.send(SignalREventNames.SERVER_ACK, "enrollment_complete");
            Log.e(TAG, "Sent enrollment complete");
        } catch (Exception e) {
            Log.e(TAG, "Error sending enrollment complete: " + e.getMessage());
        }
    }
    
    public void sendDataReceived() {
        if (hubConnection == null || hubConnection.getConnectionState() != HubConnectionState.CONNECTED) {
            return;
        }
        
        try {
            hubConnection.send(SignalREventNames.SERVER_DATA_RECEIVED);
            Log.e(TAG, "Sent data received acknowledgment");
        } catch (Exception e) {
            Log.e(TAG, "Error sending data received: " + e.getMessage());
        }
    }
    
    public void sendError(String errorMessage) {
        if (hubConnection == null || hubConnection.getConnectionState() != HubConnectionState.CONNECTED) {
            return;
        }
        
        try {
            hubConnection.send(SignalREventNames.SERVER_ERROR, errorMessage);
            Log.e(TAG, "Sent error: " + errorMessage);
        } catch (Exception e) {
            Log.e(TAG, "Error sending error: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }
    
    public void logConnectionStatus() {
        Log.e(TAG, "════════════════════════════════════════════════════════");
        Log.e(TAG, "=== SignalR Connection Status ===");
        Log.e(TAG, "Hub URL: " + hubUrl);
        if (hubConnection != null) {
            HubConnectionState state = hubConnection.getConnectionState();
            Log.e(TAG, "Connection State: " + state);
            Log.e(TAG, "isConnected(): " + isConnected());
        } else {
            Log.e(TAG, "hubConnection: NULL");
        }
        Log.e(TAG, "Listener set: " + (listener != null));
        Log.e(TAG, "════════════════════════════════════════════════════════");
    }
    
    /**
     * Verify connection and events are set up correctly
     */
    public void verifyConnectionAndEvents() {
        Log.e(TAG, "");
        Log.e(TAG, "════════════════════════════════════════════════════════");
        Log.e(TAG, "=== VERIFYING CONNECTION AND EVENTS ===");
        Log.e(TAG, "════════════════════════════════════════════════════════");
        
        if (hubConnection == null) {
            Log.e(TAG, "❌ hubConnection is NULL");
            return;
        }
        
        HubConnectionState state = hubConnection.getConnectionState();
        Log.e(TAG, "Connection State: " + state);
        
        if (state == HubConnectionState.CONNECTED) {
            Log.e(TAG, "✅ Connection is CONNECTED");
        } else {
            Log.e(TAG, "❌ Connection is NOT connected!");
            return;
        }
        
        Log.e(TAG, "");
        Log.e(TAG, "📡 Listening for events:");
        Log.e(TAG, "  - Server_RequestAction (primary)");
        Log.e(TAG, "  - server_RequestAction");
        Log.e(TAG, "  - ServerRequestAction");
        Log.e(TAG, "  - requestAction");
        Log.e(TAG, "  - RequestAction");
        Log.e(TAG, "  - Server_Info");
        Log.e(TAG, "  - TestEvent");
        Log.e(TAG, "");
        Log.e(TAG, "📋 Expected payload format:");
        Log.e(TAG, "  { \"deviceId\": 2, \"fingerNumber\": 1 }");
        Log.e(TAG, "");
        
        if (listener == null) {
            Log.e(TAG, "❌ WARNING: Listener is NULL!");
        } else {
            Log.e(TAG, "✅ Listener is set");
        }
        
        Log.e(TAG, "════════════════════════════════════════════════════════");
    }
    
    /**
     * Log event names being used
     */
    public void logEventNames() {
        Log.e(TAG, "=== SignalR Event Names ===");
        Log.e(TAG, "SERVER_REQUEST_ACTION: " + SignalREventNames.SERVER_REQUEST_ACTION);
        Log.e(TAG, "SERVER_INFO: " + SignalREventNames.SERVER_INFO);
        Log.e(TAG, "UPLOAD_FINGERPRINT: " + SignalREventNames.UPLOAD_FINGERPRINT);
        Log.e(TAG, "SERVER_ERROR: " + SignalREventNames.SERVER_ERROR);
        Log.e(TAG, "SERVER_ACK: " + SignalREventNames.SERVER_ACK);
        Log.e(TAG, "SERVER_DATA_RECEIVED: " + SignalREventNames.SERVER_DATA_RECEIVED);
    }
    
    /**
     * Test finger number mapping
     */
    public void testFingerMapping() {
        Log.e(TAG, "=== Finger Number Mapping (Direct from Frontend) ===");
        Log.e(TAG, "Frontend sends finger number directly (0-12), no conversion needed:");
        Log.e(TAG, "");
        Log.e(TAG, "Individual fingers (0-9):");
        for (int i = 0; i <= 9; i++) {
            Log.e(TAG, "  " + i + " = " + FingerScanType.getFingerName(i));
        }
        Log.e(TAG, "");
        Log.e(TAG, "Group captures (10-12):");
        for (int i = 10; i <= 12; i++) {
            Log.e(TAG, "  " + i + " = " + FingerScanType.getFingerName(i));
        }
    }
}
