package com.realscanseries.rssample;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;

import com.realscanseries.rsalib.BuildConfig;
import com.realscanseries.rsalib.RealScanAPI;
import com.realscanseries.rsalib.enums.DeviceDataHandler;
import com.realscanseries.rsalib.enums.DeviceType;
import com.realscanseries.rsalib.util.Logger;

import java.util.HashSet;
import java.util.Set;

public class DataStorage {

    protected static DataStorage _dataStorage;
    public static MainActivity    mActivity;

    public static final String PREFERENCES_NAME = "rstamp_data";
    private static final String DEFAULT_VALUE_STRING = "-1";
    private static final Set<String> DEFAULT_VALUE_STRINGSET = null;
    private static final boolean DEFAULT_VALUE_BOOLEAN = false;
    private static final int DEFAULT_VALUE_INT = 0;
    private static final long DEFAULT_VALUE_LONG = -1L;
    private static final float DEFAULT_VALUE_FLOAT = -1F;

    //data
    public static final String INITIALIZE_KEY = "initialized";
    public static final boolean INITIALIZED_DEFAULT_VALUE = false;

    public static final String SEGMENTATION_ACTIVATE_KEY = "segmentation_activate";
    public static final boolean SEGMENTATION_ACTIVATE_DEFAULT_VALUE = true;

    private static final String CAPTURE_TIMEOUT_KEY = "capture_timeout";
    private static int CAPTURE_TIME_VALUE;

    private static final String ROLL_DIRECTION_VALUE_KEY = "roll_capture_direction";
    private static int ROLL_DIRECTION_VALUE;

    private static final String SEQUENCE_CHECK_CAPTURE_MODE_KEY = "sequence_check_capture_mode";
    private static int SEQUENCE_CHECK_CAPTURE_MODE_VALUE;

    public static final String IMGFORMATLIST_KEY = "imgformatlist";

    public static int IMGFORMATLIST_DEFAULT_VALUE;

    public static final String FINGERACTIVATE_KEY = "missingfinger_activate";

    public static final String MINIMUM_NUMBER_OF_FINGER = "minimum_number_of_finger";
    public static int MINIMUM_NUMBER_OF_FINGER_VALUE;

    public static final String FINGERSELECT_KEY = "fingerselect";
    public static Set<String> FINGERSELECT_DEFAULT_SET = new HashSet<String>();

    public static final String CUSTOMSEGSIZE_KEY = "customsegmentsize_activate";
    public static final String PREVIEW_QUALITY_KEY = "pref_previewquality";
    public static final String CUSTOMSEGMENTSIZE_WIDTH_KEY = "custom_segmentsize_width";
    public static int CUSTOMSEGMENTSIZE_WIDTH_DEFAULT_VALUE;

    public static final String CUSTOMSEGMENTSIZE_HEIGHT_KEY = "custom_segmentsize_height";
    public static int CUSTOMSEGMENTSIZE_HEIGHT_DEFAULT_VALUE;

    public static final String CUSTOMER_ID_ACTIVATE_KEY = "customer_id_activate";
    public static final String CUSTOMER_ID_VALUE_KEY = "customer_id_value";

    public static final String DIRTY_CHECK_ACTIVATE_KEY = "dirty_check_activate";
    public static final String DIRTY_CHECK_LEVEL_KEY = "dirty_check_level";

    public static final String NFIQ_MODE_KEY = "nfiq_mode";
    public static final boolean NFIQ_MODE_DEFAULT_VALUE = false;

    public static final String LFD_MODE_KEY = "lfd_mode";
    public static final int LFD_MODE_DEFAULT_VALUE = 0;

    public static final String ENHANCED_PREVIEW_MODE_KEY = "pref_enhancedpreviewmode";
    public static final boolean ENHANCED_PREVIEW_MODE_VALUE = false;

    public static final String USB_LIB_USB_MODE_KEY = "pref_usbNativeUsbMode";
    public static boolean   DEFAULT_USB_LIB_USB_MODE_VALUE;

    public static final String LOW_CPU_DEVICE_MODE_KEY = "pref_supportLowCpuDevice";
    public static boolean   DEFAULT_LOW_CPU_DEVICE_MODE_VALUE;

    public static final String WRITE_LOG_KEY = "pref_writeLog";
    public static boolean   WRITE_LOG_KEY_VALUE;

    public static final String DEBUG_ENABLE_KEY = "debug_enable";

    public static final String CAPTURE_AREA_EXPAND_MODE_KEY = "pref_captureareaexpandmode";
    public static final boolean CAPTRUE_AREA_EXPAND_MODE_VALUE = false;

    public static final String HALO_REDUCTION_ENHANCED_MODE_KEY = "pref_haloreductionenhanced";
    public static final boolean HALO_REDUCTION_ENHANCED_MODE_VALUE = false;

    private static final String S60_EXTERNAL_BRIGHT_ENABLE_MODE_KEY = "pref_s60Capture_measurement_enable_mode";
    private static boolean S60_EXTERNAL_BRIGHT_ENABLE_MODE_VALUE = true;


    private static final String CUSTOM_LED_KEY = "pref_led_mode";
    private static boolean CUSTOM_LED_VALUE = false;

    private static final String S60_EXTERNAL_BRIGHT_MEASUREMODELIST_KEY = "pref_s60Capture_measurement_mode";
    private static int S60_EXTERNAL_BRIGHT_MEASUREMODELIST_VALUE = 0;

    public static final String SENSITIVE_MODE_KEY = "sensitive_mode";

    public static final int SENSITIVE_MODE_DEFAULT_VALUE = 0;

    private Context mContext;
    private static SharedPreferences   mPreference;

    public DataStorage(Context context) {
        mContext = context;
        mActivity = (MainActivity) context;
        mPreference =   mContext.getSharedPreferences(PREFERENCES_NAME,Context.MODE_PRIVATE);
    }
    public static DataStorage getInstance(Context context)
    {
        if (_dataStorage == null) {
            _dataStorage = new DataStorage(context);
        }

        return _dataStorage;
    }
    public static DataStorage getInstance()  {
        return _dataStorage;
    }

    public void setDefaultPrefValue(int _pid) {
        Logger.d("START! : " + _pid);
        setImgformatValue(1); //1. JPEG 2. BMP
        setMissingFingerActivateValue(false);
        setCustomsegSizeActivate(0);
        setCustomSegSizeWidth(0);
        setCustomSegSizeHeight(0);

        if(_pid == DeviceType.REALSCAN_G10.getProductId() || _pid == DeviceType.REALSCAN_SG10.getProductId())
        {
            setMinimumNumberOfFinger(4);
        }
        else if(_pid == DeviceType.REALSCAN_S60.getProductId())
        {
            setMinimumNumberOfFinger(4);
        }
        else if(_pid == DeviceType.REALSCAN_D.getProductId())
        {
            setMinimumNumberOfFinger(2);
        }
        setSelectedFinger(FINGERSELECT_DEFAULT_SET);
        setCustomerIdActivate(false);
//        setDirtyCheckActivate(true);
//        setDirtyCheckLevelValue(5);
        setNfiqMode(false);
        setRollCaptureDirectionValue(DeviceDataHandler.RollCaptureDirection.ROLL_DIR_AUTO.getDirection());
        setCaptureTimeoutValue(10);
        setSequenceCheckCaptureMode(0);
    }

    /**
     * String 값 저장
     * @param key
     * @param value
     */
    public void setString( String key, String value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }
    /**
     * StringSet 저장
     * @param key
     * @param dataSet
     */
    public void setStringSet( String key, Set<String> dataSet) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putStringSet(key, dataSet);
        editor.commit();
    }
    /**
     * boolean 값 저장
     * @param key
     * @param value
     */
    public void setBoolean( String key, boolean value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    /**
     * int 값 저장
     * @param key
     * @param value
     */
    public void setInt( String key, int value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    /**
     * long 값 저장
     * @param key
     * @param value
     */
    public void setLong( String key, long value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putLong(key, value);
        editor.commit();
    }
    /**
     * float 값 저장
     * @param key
     * @param value
     */
    public void setFloat( String key, float value) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putFloat(key, value);
        editor.commit();
    }
    /**
     * String 값 로드
     * @param key
     * @return
     */
    public String getString( String key) {
        String value = mPreference.getString(key, DEFAULT_VALUE_STRING);
        return value;
    }
    /**
     * String 값 로드
     * @param key
     * @return
     */
    public Set<String> getStringSet( String key) {
        Set<String> dataSet = mPreference.getStringSet(key, DEFAULT_VALUE_STRINGSET);
        return dataSet;
    }

    /**
     * boolean 값 로드
     * @param key
     * @return
     */
    public boolean getBoolean( String key) {
        boolean value = mPreference.getBoolean(key, DEFAULT_VALUE_BOOLEAN);
        return value;
    }
    /**
     * int 값 로드
     * @param key
     * @return
     */
    public int getInt( String key) {
        int value = mPreference.getInt(key, DEFAULT_VALUE_INT);
        return value;
    }
    /**
     * long 값 로드
     * @param key
     * @return
     */
    public long getLong( String key) {
        long value = mPreference.getLong(key, DEFAULT_VALUE_LONG);
        return value;
    }
    /**
     * float 값 로드
     * @param key
     * @return
     */
    public float getFloat( String key) {
        float value = mPreference.getFloat(key, DEFAULT_VALUE_FLOAT);
        return value;
    }
    /**
     * 키 값 삭제
     * @param key
     */
    public void removeKey( String key) {
        SharedPreferences.Editor edit = mPreference.edit();
        edit.remove(key);
        edit.commit();
    }
    /**
     * 모든 저장 데이터 삭제
     * @param context
     */
    public void clear(Context context) {
        SharedPreferences.Editor edit = mPreference.edit();
        edit.clear();
        edit.commit();
    }
    public void setSegmentationActivate(boolean _value) {setBoolean(SEGMENTATION_ACTIVATE_KEY,_value);}
    public boolean getSegmentationActivateValue()
    {
        return mPreference.getBoolean(SEGMENTATION_ACTIVATE_KEY,SEGMENTATION_ACTIVATE_DEFAULT_VALUE);
    }

    public void setImgformatValue(int _value)
    {
        setInt(IMGFORMATLIST_KEY,_value);
    }
    public int getImgformatValue()
    {
        return mPreference.getInt(IMGFORMATLIST_KEY,IMGFORMATLIST_DEFAULT_VALUE);
    }

    public void setMissingFingerActivateValue(Boolean _value)
    {
        setBoolean(FINGERACTIVATE_KEY,_value);
    }
    public boolean getMissingFingerActivateValue()
    {
        return mPreference.getBoolean(FINGERACTIVATE_KEY,false);
    }
    public void setSelectedFinger(Set<String> _dataSet)
    {
        setStringSet(FINGERSELECT_KEY,_dataSet);
    }
    public Set<String> getSelectedFinger()
    {
        return mPreference.getStringSet(FINGERSELECT_KEY,FINGERSELECT_DEFAULT_SET);
    }

    public void setCustomsegSizeActivate(Integer _value)
    {
        setInt(CUSTOMSEGSIZE_KEY,_value);
    }
    public int getCustomsegSizeActivate()
    {
        return mPreference.getInt(CUSTOMSEGSIZE_KEY,0);
    }

    public void setPreviewQuality(Integer _value)
    {
        setInt(PREVIEW_QUALITY_KEY,_value);
    }
    public int getPreviewQuality()
    {
        return mPreference.getInt(PREVIEW_QUALITY_KEY,1);
    }

    public void setCustomSegSizeWidth(Integer _value)
    {
        setInt(CUSTOMSEGMENTSIZE_WIDTH_KEY,_value);
    }
    public int getCustomSegSizeWidth()
    {
        return mPreference.getInt(CUSTOMSEGMENTSIZE_WIDTH_KEY,CUSTOMSEGMENTSIZE_WIDTH_DEFAULT_VALUE);
    }

    public void setCustomSegSizeHeight(Integer _value)
    {
        setInt(CUSTOMSEGMENTSIZE_HEIGHT_KEY,_value);
    }
    public int getCustomSegSizeHeight()
    {
        return mPreference.getInt(CUSTOMSEGMENTSIZE_HEIGHT_KEY,CUSTOMSEGMENTSIZE_HEIGHT_DEFAULT_VALUE);
    }

    public void setInitializeDevice(int _pid) {
        if(mPreference.contains(INITIALIZE_KEY) == false) {
            if(RealScanAPI.getInstance() == null) {
                UsbManager usbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
                try {
                    RealScanAPI.getInstance(mContext,usbManager);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(RealScanAPI.getInstance().getCurrentDevice() == null) {
                Logger.d("Device is not connected.");
                return;
            }
            if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != null){
                if(_pid == 4193
                        && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_S60) {
                    return;
                }
            }
            Logger.d("Initialize first time.");
            clear(mContext);
            setInt(INITIALIZE_KEY,_pid);
            setDefaultPrefValue(_pid);
        }else {
            Logger.d("Device is already launched.");
            Logger.d("_pid : " + _pid);
            Logger.d("getInitializeDevice : " + getInitializeDevice());
            if(RealScanAPI.getInstance().getCurrentDevice() != null
                    && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != null){
                if(_pid == 4193
                        && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_S60) {
                    return;
                }
            }

            if(_pid != getInitializeDevice())
            {
                Logger.d("Device is changed. do clear data storage.");
                clear(mContext);
                mPreference = mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
                setDefaultPrefValue(_pid);
            }
        }

    }
    public int getInitializeDevice()
    {
        return getInt(INITIALIZE_KEY);
    }

    public void setCustomerIdActivate(boolean _value)
    {
        setBoolean(CUSTOMER_ID_ACTIVATE_KEY,_value);
    }
    public boolean getCustomerIdActivate()
    {
        return mPreference.getBoolean(CUSTOMER_ID_ACTIVATE_KEY,false);
    }
    public void setCustomerIdValue(String _value)
    {
        setString(CUSTOMER_ID_VALUE_KEY,_value);
    }
    public String getCustomerIdValue()
    {
        return mPreference.getString(CUSTOMER_ID_VALUE_KEY,"");
    }

    public void setDirtyCheckActivate(boolean _value)
    {
        setBoolean(DIRTY_CHECK_ACTIVATE_KEY,_value);
    }
    public boolean getDirtyCheckActivate()
    {
        return mPreference.getBoolean(DIRTY_CHECK_ACTIVATE_KEY,true);
    }
    public void setDirtyCheckLevelValue(int _value)
    {
        setInt(DIRTY_CHECK_LEVEL_KEY,_value);
    }
    public int getDirtyCheckLevelValue()
    {
        return mPreference.getInt(DIRTY_CHECK_LEVEL_KEY,5);
    }

    public void setNfiqMode(boolean _value)
    {
        setBoolean(NFIQ_MODE_KEY,_value);
    }
    public boolean getNfiqMode()
    {
        return mPreference.getBoolean(NFIQ_MODE_KEY,NFIQ_MODE_DEFAULT_VALUE);
    }

    public void setCaptureTimeoutValue(Integer _value) {
        setInt(CAPTURE_TIMEOUT_KEY,_value*1000);
    }
    public int getCaptureTimeoutValue() {
        return mPreference.getInt(CAPTURE_TIMEOUT_KEY,10000);
    }

    public void setRollCaptureDirectionValue(Integer _value) {
        setInt(ROLL_DIRECTION_VALUE_KEY,_value);
    }
    public int getRollCaptureDirectionValue() {
        return mPreference.getInt(ROLL_DIRECTION_VALUE_KEY,2);
    }

    public void setMinimumNumberOfFinger(int _value) {
        setInt(MINIMUM_NUMBER_OF_FINGER,_value);
    }
    public int getMinimumNumberOfFinger()
    {
        return getInt(MINIMUM_NUMBER_OF_FINGER);
    }

    public void setLfdMode(int newValue) {
        setInt(LFD_MODE_KEY, newValue);
    }

    public int getLfdMode() {
        return mPreference.getInt(LFD_MODE_KEY,LFD_MODE_DEFAULT_VALUE);
    }

    public void setSequenceCheckCaptureMode(int value) {
        setInt(SEQUENCE_CHECK_CAPTURE_MODE_KEY,value);
    }
    public int getSequenceCheckCaptureMode() {
        return mPreference.getInt(SEQUENCE_CHECK_CAPTURE_MODE_KEY,DEFAULT_VALUE_INT);
    }

    public void setEnhancedPreviewMode(boolean newValue) {
        setBoolean(ENHANCED_PREVIEW_MODE_KEY,newValue);
    }
    public boolean getEnhancedPreviewMode() {
        return mPreference.getBoolean(ENHANCED_PREVIEW_MODE_KEY,false);
    }
    public void setUseNativeUsbModeParam(boolean _value) {
        setBoolean(USB_LIB_USB_MODE_KEY,_value);
    }
    public boolean getUseNativeUsbModeParam()
    {
        Logger.d("getUseNativeUsbModeParam : " + mPreference.getBoolean(USB_LIB_USB_MODE_KEY,true));
        return mPreference.getBoolean(USB_LIB_USB_MODE_KEY,true);
    }

    public void setLowCpuDeviceModeParam(boolean _value) {
        setBoolean(LOW_CPU_DEVICE_MODE_KEY,_value);
    }
    public boolean getLowCpuDeviceModeParam() {
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Logger.d("getLowCpuDeviceModeParam (Debug Mode): true");
            return true;
        }

        boolean value = mPreference.getBoolean(LOW_CPU_DEVICE_MODE_KEY,false);
        Logger.d("getLowCpuDeviceModeParam : " + value);
        return value;
    }
    public void setCaptureAreaExpandMode(boolean newValue) {
        setBoolean(CAPTURE_AREA_EXPAND_MODE_KEY,newValue);
    }
    public boolean getCaptureAreaExpandMode() {
        if(RealScanAPI.getInstance() != null && RealScanAPI.getInstance().getCurrentDevice() != null){
            if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_G10
                || RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_SG10
                || RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_G10i
                || RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_G10iBIOS
                || RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_F_C
                || RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_F_CiBIOS
                ){
                return mPreference.getBoolean(CAPTURE_AREA_EXPAND_MODE_KEY,false);
            }
        }
        return mPreference.getBoolean(CAPTURE_AREA_EXPAND_MODE_KEY,true);
    }

    public void setHaloReductionEnhandedMode(boolean newValue) {
        setBoolean(HALO_REDUCTION_ENHANCED_MODE_KEY, newValue);
    }
    public boolean getHaloReductionEnhandedExpandMode() {
        return mPreference.getBoolean(HALO_REDUCTION_ENHANCED_MODE_KEY,false);
    }

    public void setS60ExternalBrightEnableModeValue(boolean newValue) {
        setBoolean(S60_EXTERNAL_BRIGHT_ENABLE_MODE_KEY, newValue);
    }

    public boolean getS60ExternalBrightEnableModeValue() {
        return mPreference.getBoolean(S60_EXTERNAL_BRIGHT_ENABLE_MODE_KEY, S60_EXTERNAL_BRIGHT_ENABLE_MODE_VALUE);
    }


    public void setCustomLedMode(boolean newValue) {
        setBoolean(CUSTOM_LED_KEY, newValue);
    }

    public boolean getCustomLedMode() {
        return mPreference.getBoolean(CUSTOM_LED_KEY, CUSTOM_LED_VALUE);
    }


    public void setS60CaptureExternalBrightMeasureModeValue(Integer _value) {
        setInt(S60_EXTERNAL_BRIGHT_MEASUREMODELIST_KEY,_value);
    }
    public int getS60CaptureExternalBrightMeasureModeValue() {
        return mPreference.getInt(S60_EXTERNAL_BRIGHT_MEASUREMODELIST_KEY, S60_EXTERNAL_BRIGHT_MEASUREMODELIST_VALUE);
    }

    public void setSensitiveMode(int newValue) {
        setInt(SENSITIVE_MODE_KEY, newValue);
    }

    public int getSensitiveMode() {
        return mPreference.getInt(SENSITIVE_MODE_KEY,SENSITIVE_MODE_DEFAULT_VALUE);
    }
}
