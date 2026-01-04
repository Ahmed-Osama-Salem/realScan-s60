package com.realscanseries.rssample;

/**
 * SignalR Event Names Configuration
 * 
 * This class contains all the SignalR event/channel names used for communication
 * between the Android app and the frontend server.
 * 
 * Update these constants when the actual event names are provided by the backend.
 */
public class SignalREventNames {
    
    /**
     * Event name for receiving requests from the frontend.
     * This is a unified event that handles both single fingerprint requests
     * and full enrollment requests. The event payload will contain the action type.
     */
    public static final String SERVER_REQUEST_ACTION = "Server_RequestAction";
    
    /**
     * Event name for uploading fingerprint base64 image data to the frontend.
     * Parameters: fingerIndex (int), base64Image (String), width (int), height (int)
     */
    public static final String UPLOAD_FINGERPRINT = "UploadFingerprint";
    
    /**
     * Event name for sending error messages to the frontend.
     * Used for any server-side errors, fingerprint capture errors, or enrollment errors.
     * Parameters: errorMessage (String) or fingerIndex (int), errorMessage (String)
     */
    public static final String SERVER_ERROR = "Server_Error";
    
    /**
     * Event name for sending acknowledgment to the frontend.
     * Used to confirm successful completion of operations (e.g., enrollment complete).
     */
    public static final String SERVER_ACK = "Server_Ack";
    
    /**
     * Event name for sending data received confirmation to the frontend.
     * Used to confirm that data has been successfully received and processed.
     */
    public static final String SERVER_DATA_RECEIVED = "Server_DataReceived";
    
    /**
     * Event name for receiving informational messages from the frontend/server.
     * Used for status updates, notifications, and other informational messages.
     */
    public static final String SERVER_INFO = "Server_Info";
}

