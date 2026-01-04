package com.realscanseries.rssample;

import android.util.Log;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

/**
 * Helper class to configure SSL to accept all certificates (for development only)
 * WARNING: This bypasses SSL certificate validation. Use only for development!
 */
public class SSLHelper {
    private static final String TAG = "SSLHelper";
    private static boolean sslConfigured = false;

    /**
     * Configure SSL to accept all certificates
     * WARNING: This is insecure and should only be used for development!
     */
    public static void trustAllCertificates() {
        if (sslConfigured) {
            Log.d(TAG, "SSL already configured to trust all certificates");
            return;
        }

        try {
            Log.w(TAG, "=== WARNING: Configuring SSL to trust ALL certificates ===");
            Log.w(TAG, "This is INSECURE and should only be used for DEVELOPMENT!");

            // Create a trust manager that accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        Log.d(TAG, "checkClientTrusted: " + authType);
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        Log.d(TAG, "checkServerTrusted: " + authType);
                        // Accept all certificates - no validation
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // Set as default for HttpsURLConnection
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> {
                Log.d(TAG, "Hostname verification: " + hostname);
                return true; // Accept all hostnames
            });
            
            // Also set as default SSL context (for libraries that use SSLContext.getDefault())
            try {
                SSLContext.setDefault(sslContext);
                Log.d(TAG, "Set as default SSL context");
            } catch (Exception e) {
                Log.w(TAG, "Could not set default SSL context: " + e.getMessage());
            }
            
            // Set system properties for SSL (some libraries check these)
            try {
                System.setProperty("javax.net.ssl.trustStore", "NONE");
                System.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");
                Log.d(TAG, "Set SSL system properties");
            } catch (Exception e) {
                Log.w(TAG, "Could not set SSL system properties: " + e.getMessage());
            }

            sslConfigured = true;
            Log.d(TAG, "SSL configured to trust all certificates");
            Log.w(TAG, "NOTE: Some HTTP clients (like OkHttp) may not respect this configuration.");
            Log.w(TAG, "If SignalR still fails, the server certificate may need to be added to the app.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to configure SSL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create an OkHttpClient that trusts all certificates
     * This can be used by libraries that use OkHttp directly
     */
    public static OkHttpClient createTrustAllOkHttpClient() {
        try {
            Log.d(TAG, "Creating OkHttpClient that trusts all certificates");
            
            // Create a trust manager that accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // Accept all certificates
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Create OkHttpClient with trust-all configuration
            // IMPORTANT: Disable hostname verification to work with IP addresses or custom DNS
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> {
                        // Accept all hostnames (for development with IP addresses or custom DNS)
                        Log.d(TAG, "Hostname verification: " + hostname + " -> ACCEPTED");
                        return true;
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);
            
            Log.d(TAG, "OkHttpClient created with trust-all SSL configuration and disabled hostname verification");
            return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create trust-all OkHttpClient: " + e.getMessage());
            e.printStackTrace();
            // Return default OkHttpClient if creation fails
            return new OkHttpClient.Builder().build();
        }
    }
}

