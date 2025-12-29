package com.realscanseries.rssample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.Toast;

import com.realscanseries.rsalib.BuildConfig;
import com.realscanseries.rsalib.RealScanAPI;
import com.realscanseries.rsalib.enums.CaptureMode;
import com.realscanseries.rsalib.enums.DeviceDataHandler;
import com.realscanseries.rsalib.enums.DeviceType;
import com.realscanseries.rsalib.enums.ErrorCode;
import com.realscanseries.rsalib.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.realscanseries.rssample.MainActivity.UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int EVENT_SCREEN_UPDATE = 100;
    public static Context mContext = null;
    public static MainActivity    mActivity;
    public static DataStorage mDataStorage = null;
    public static SwitchPreference mSegmentationPref;
    public static ListPreference mRollDirectionPref;
    public static EditTextPreference mCaptureTimeoutPref;
    public static ListPreference mImgFormatList;
    public static ListPreference mMinimumNumberOfFingerPref;
    public static MultiSelectListPreference mFingerSelect;
    public static SwitchPreference mFingerActivate;
    public static ListPreference mCustomSegSizeActivatePref;
    public static ListPreference mPreviewQualityPref;
    public static EditTextPreference mCustomSegSizeWidthPref;
    public static EditTextPreference mCustomSegSizeHeightPref;
    public static PreferenceCategory mCustomerIdPrefCategory;
    public static PreferenceCategory mCustomLedModePrefCategory;
    public static SwitchPreference mCheckCustomerIdPref;
    public static EditTextPreference mCheckCustomerIdValuePref;
    public static PreferenceCategory mDirtyCheckCategory;
    public static SwitchPreference mDirtyCheckPref;
    public static EditTextPreference mDirtyCheckLevelPref;
    public static SwitchPreference    mNfiqModePref;
    public static Preference    mNfiqScorePref;
    public static PreferenceCategory mLfdModePrefCategory;
    public static EditTextPreference  mLfdModePref;
    public static Preference    mLfdScorePref;
    public static Preference    mLicensePref;
    public static ListPreference    mSequenceCheckCaptureModePref;
    public static SwitchPreference mEnhancedPreviewModePref;
    public static SwitchPreference    mUseNativeUsbModePref;
    public static SwitchPreference mCaptureAreaExpandModePref;

    public static SwitchPreference mHaloReductionEnhandedModePref;

    public static PreferenceCategory mS60CaptureSettingCategory;

    public static SwitchPreference    mSupportLowCpuDeviceModePref;
    public static SwitchPreference mS60ExternalBrightEnableModePref;
    public static ListPreference mS60CaptureBrightMeasureListPref;

    public static ListPreference mSensitiveModeListPref;

    public static SwitchPreference mLedSettingCustomeEnableModePref;
    public static ModeLEDPreference modeLEDPreference;
    public static StatusLEDPreference statusLEDPreference;
    public boolean  mIsRunning = false;
    public boolean  mSegmentationActivate = false;
    public int mCaptureTimeoutValue = 0;
    public int mRollDirectionValue = 0;
    public boolean mIsMissingFingerActivate = false;
    public int mIsCustomSegSizeActivate = 0;
    public int mIsPreviewQualitryMode = 0;
    public int mMissingFingerLeftCount = 0;
    public int mMissingFingerRightCount = 0;
    public int mMinimumNumberOfFingerValue;
    public Set<String> mMissingFingerRightSet;
    public Set<String> mMissingFingerLeftSet;
    public int mCustomSegmentSizeWidth= 0;
    public int mCustomSegmentSizeHeight= 0;
    public boolean mIsPrefRefreshed = false;
    public boolean mIsParamInitiated = false;
    public boolean mIsCustimerIdActivate = false;
    public String mCustomerIdValue = "";
    public boolean mIsDirtyCheckActivate = false;
    public boolean mIsCustomLedMode = false;
    public int  mDirtyCheckLevel = 0;
    public DeviceDataHandler mDeviceDataHandler;
    public boolean  mNfiqMode = false;
    public int mLfdMode = 0;
    public boolean mIsUseLibUsbMode = false;
    public int mSequenceCheckCaptureMode = 0;
    public boolean mEnhancedPreviewMode = false;

    /**
     * Capture Area
     *  Flat Four Fingers 1600 * 1500
     *  Two Thumbs 900 * 900
     *  Roll/Flat Single 800 * 750
     *
     *  In the case of Roll/Flat Single, it was found that the fingerprint capture was cut off when leaving the area
     *  Mode to convert all Capture Areas to 1600 * 1500
     * */
    public boolean  mCaptrueAreaExpandMode = false; // Increased capture size to maximum. 1600*1500

    public boolean mHaloReductionEnhandedMode = false;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    public AlertDialog.Builder mBuilder = null;

    private int mCurrentImgValueIndex;


    public SettingFragment() {
        // Required empty public constructor
    }
    public SettingFragment(Context _context) {
        // Required empty public constructor
        Logger.d("SettingFragment constructor!");
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Logger.d("START!");
        //addPreferencesFromResource(R.xml.setting_pref);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Logger.d("START!");
        setPreferencesFromResource(R.xml.setting_pref,rootKey);
        loadPreference();

        modeLEDPreference = findPreference("mode_led_preference");
        if (modeLEDPreference != null) {
            modeLEDPreference.setOnModeLEDApplyButtonClickListener((ModeLEDPreference.OnModeLedApplyButtonClickListener) getActivity());
        }
        statusLEDPreference = findPreference("status_led_preference");
        if (statusLEDPreference != null) {
            statusLEDPreference.setOnStatusLedApplyButtonClickListener((StatusLEDPreference.OnStatusLedApplyButtonClickListener) getActivity());
        }
        resetPreference(false);
    }



    //    @SuppressLint("ResourceType")
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = null;
//        if(getView() == null)
//        {
//            Logger.d("onCreateView : " + getView());
//
//            view = inflater.inflate(R.layout.fragment_setting, container, false);
//        }
//
//        return view;
//    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.d("onAttach : " + context);
        mDeviceDataHandler = DeviceDataHandler.getInstance();
        mContext = context;
        mActivity = (MainActivity) context;
        mDataStorage = DataStorage.getInstance(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logger.d("onDetach!");
        mListener = null;
//        mContext = null; onDestory 이동
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("start!");
        mActivity.addDeviceToUsbDeviceList(null);
    }
    private void loadPreference() {
        Logger.d("START!");
        mSegmentationPref = findPreference("segmentation_activate");
        mSegmentationPref.setOnPreferenceChangeListener(this);

        mCaptureTimeoutPref = findPreference("pref_Capture_Timeout");
        mCaptureTimeoutPref.setOnPreferenceChangeListener(this);
        mCaptureTimeoutPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                editText.setText(String.valueOf(mDataStorage.getCaptureTimeoutValue()/1000));
                editText.setSelection(editText.getText().length());
            }
        });
        mRollDirectionPref = findPreference("roll_capture_direction");
        mRollDirectionPref.setOnPreferenceChangeListener(this);

        mSequenceCheckCaptureModePref = findPreference("sequence_check_capture_mode");
        mSequenceCheckCaptureModePref.setOnPreferenceChangeListener(this);

        mImgFormatList = (ListPreference) findPreference("imageformat");
        mImgFormatList.setOnPreferenceChangeListener(this);
        Logger.d("mImgFormatList : " + mImgFormatList);

        mFingerActivate = (SwitchPreference) findPreference("missingfinger_activate");
        mFingerActivate.setOnPreferenceChangeListener(this);

        mMinimumNumberOfFingerPref = (ListPreference) findPreference("minimum_number_of_finger");
        mMinimumNumberOfFingerPref.setOnPreferenceChangeListener(this);

        mFingerSelect = (MultiSelectListPreference) findPreference("fingerselect");
        mFingerSelect.setOnPreferenceClickListener(this);
        mFingerSelect.setOnPreferenceChangeListener(this);

        mCustomerIdPrefCategory = (PreferenceCategory) findPreference("customeridprefcategory");
        mCustomLedModePrefCategory = (PreferenceCategory) findPreference("LEDsettingcategory");
        mCustomSegSizeActivatePref = (ListPreference) findPreference("customsegmentsize_activate");
        mCustomSegSizeActivatePref.setOnPreferenceChangeListener(this);

        mPreviewQualityPref = (ListPreference) findPreference("pref_previewquality");
        mPreviewQualityPref.setOnPreferenceChangeListener(this);



        mCustomSegSizeWidthPref = (EditTextPreference) findPreference("custom_segmentsize_width");
        mCustomSegSizeWidthPref.setOnPreferenceChangeListener(this);
        mCustomSegSizeWidthPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                int captureMode = mCustomSegSizeActivatePref.getValue() != null
                        ? Integer.parseInt(mCustomSegSizeActivatePref.getValue())
                        : 0;

                int maxWidth = getWidthLimitByCaptureMode(captureMode);

                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(String.valueOf(mDataStorage.getCustomSegSizeWidth()));

                editText.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(4),
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                try {
                                    String input = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
                                    int value = Integer.parseInt(input);
                                    if (value <= 0 || value > maxWidth) {
                                        Toast.makeText(editText.getContext(),
                                                "Value must be greater than 0 and less than or equal to " + maxWidth,
                                                Toast.LENGTH_SHORT).show();
                                        return ""; // 초과 값 입력 차단
                                    }
                                } catch (NumberFormatException e) {
                                    Toast.makeText(editText.getContext(),
                                            "Invalid number format.",
                                            Toast.LENGTH_SHORT).show();
                                    // 잘못된 숫자 포맷인 경우
                                    return "";
                                }
                                return null;
                            }
                        }
                });
                editText.setSelection(editText.getText().length());
            }
        });

        mCustomSegSizeHeightPref = (EditTextPreference) findPreference("custom_segmentsize_height");
        mCustomSegSizeHeightPref.setOnPreferenceChangeListener(this);
        mCustomSegSizeHeightPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                int captureMode = mCustomSegSizeActivatePref.getValue() != null
                        ? Integer.parseInt(mCustomSegSizeActivatePref.getValue())
                        : 0;

                int maxHeight = getHeightLimitByCaptureMode(captureMode);

                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(String.valueOf(mDataStorage.getCustomSegSizeHeight()));
                editText.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(4),
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                try {
                                    String input = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
                                    int value = Integer.parseInt(input);
                                    if (value <= 0 || value > maxHeight) {
                                        Toast.makeText(editText.getContext(),
                                                "Value must be greater than 0 and less than or equal to " + maxHeight,
                                                Toast.LENGTH_SHORT).show();
                                        return ""; // 초과 값 입력 차단
                                    }
                                } catch (NumberFormatException e) {
                                    Toast.makeText(editText.getContext(),
                                            "Invalid number format.",
                                            Toast.LENGTH_SHORT).show();
                                    // 잘못된 숫자 포맷인 경우
                                    return "";
                                }
                                return null;
                            }
                        }
                });
                editText.setSelection(editText.getText().length());
            }
        });

        mCheckCustomerIdPref = (SwitchPreference) findPreference("checkCustomerId");
        mCheckCustomerIdPref.setOnPreferenceChangeListener(this);

        mCheckCustomerIdValuePref = (EditTextPreference) findPreference("checkCustomerId_value");
        mCheckCustomerIdValuePref.setOnPreferenceChangeListener(this);
        mCheckCustomerIdValuePref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setText("");
                editText.setSelection(editText.getText().length());
            }
        });

        mDirtyCheckCategory = (PreferenceCategory) findPreference("dirtycheckcategory");
        mDirtyCheckPref = (SwitchPreference) findPreference("pref_dirtycheck");
        mDirtyCheckPref.setOnPreferenceChangeListener(this);

        mDirtyCheckLevelPref = (EditTextPreference) findPreference("pref_dirtycheck_level");
        mDirtyCheckLevelPref.setOnPreferenceChangeListener(this);
        mDirtyCheckLevelPref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                editText.setText("");
                editText.setSelection(editText.getText().length());
            }
        });

        mNfiqModePref = (SwitchPreference) findPreference("pref_nfiqmode");
        mNfiqModePref.setOnPreferenceChangeListener(this);

        mNfiqScorePref = (Preference) findPreference("pref_nfiqscore");
        mNfiqScorePref.setOnPreferenceClickListener(this);

        mLfdModePrefCategory = (PreferenceCategory) findPreference("lfdmodecategory");

        mLfdModePref = findPreference("pref_lfdmode");
        mLfdModePref.setOnPreferenceChangeListener(this);

        mLfdModePref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                editText.setText("");
                editText.setSelection(editText.getText().length());
            }
        });

        mLfdScorePref = (Preference) findPreference("pref_lfdscore");
        mLfdScorePref.setOnPreferenceChangeListener(this);

        mLicensePref = (Preference) findPreference("pref_license");
        mLicensePref.setOnPreferenceClickListener(this);

        mEnhancedPreviewModePref = (SwitchPreference) findPreference("pref_enhancedpreviewmode");
        mEnhancedPreviewModePref.setOnPreferenceChangeListener(this);

        mUseNativeUsbModePref = (SwitchPreference) findPreference("pref_usbNativeUsbMode");
        mUseNativeUsbModePref.setOnPreferenceChangeListener(this);

        mCaptureAreaExpandModePref = (SwitchPreference) findPreference("pref_captureareaexpandmode");
        mCaptureAreaExpandModePref.setOnPreferenceChangeListener(this);

        mHaloReductionEnhandedModePref = (SwitchPreference) findPreference("pref_haloreductionenhanced");
        mHaloReductionEnhandedModePref.setOnPreferenceChangeListener(this);

        mS60CaptureSettingCategory = (PreferenceCategory) findPreference("s60settingcategory");

        mSupportLowCpuDeviceModePref = (SwitchPreference) findPreference("pref_supportLowCpuDevice");
        mSupportLowCpuDeviceModePref.setOnPreferenceChangeListener(this);

        mS60ExternalBrightEnableModePref = findPreference("pref_s60Capture_measurement_enable_mode");
        mS60ExternalBrightEnableModePref.setOnPreferenceChangeListener(this);

        mS60CaptureBrightMeasureListPref = findPreference("pref_s60Capture_measurement_mode");
        mS60CaptureBrightMeasureListPref.setOnPreferenceChangeListener(this);

        mSensitiveModeListPref = findPreference("pref_sensitive_mode");
        mSensitiveModeListPref.setOnPreferenceChangeListener(this);

        mLedSettingCustomeEnableModePref = findPreference("pref_setLed_mode");
        mLedSettingCustomeEnableModePref.setOnPreferenceChangeListener(this);
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Logger.d("START : " + preference.getKey() + " value : " + newValue);
//        if(mDataStorage == null)
//        {
//            Logger.d("mDataStorage is null. The preference value cannot be updated.");
//            return false;
//        }
        if(newValue.equals(""))
        {
            Toast.makeText(mContext,"No parameters have been entered.",Toast.LENGTH_LONG).show();
            return false;
        }
        switch (preference.getKey())
        {
            case "segmentation_activate":
                Logger.d("segmentation_activate is changed");
                mDataStorage.setSegmentationActivate((boolean)newValue);
                mSegmentationActivate = mDataStorage.getSegmentationActivateValue();
                mDeviceDataHandler.setSegmentationActivate(mSegmentationActivate);
                Logger.d("preference data check : " + mSegmentationActivate);
                break;
            case "pref_Capture_Timeout":
                Logger.d("pref_Capture_Timeout is changed.");
                int _timeout = Integer.parseInt((String)newValue);
                Logger.d("_timeout: " + _timeout);
                if(_timeout > 30 || _timeout < 0)
                {
                    Toast.makeText(mContext,"Please enter collect value. (0~30)",Toast.LENGTH_LONG).show();
                    return false;
                }
                mDataStorage.setCaptureTimeoutValue(_timeout);
                mCaptureTimeoutValue = mDataStorage.getCaptureTimeoutValue();
                mDeviceDataHandler.setCaptureTimeout(mCaptureTimeoutValue);
                Logger.d("preference data check : " + mCaptureTimeoutValue);
                break;
            case "roll_capture_direction":
                Logger.d("roll_capture_direction is changed.");
                mDataStorage.setRollCaptureDirectionValue(Integer.valueOf((String)newValue));
                mRollDirectionValue = mDataStorage.getRollCaptureDirectionValue();
                mDeviceDataHandler.setRollDirection(mRollDirectionValue);
                Logger.d("preference data check : " + mRollDirectionValue);
                break;
            case "imageformat":
                Logger.d("imageformat list is changed!");
                int _newImgFormatValue = 0;
                if(newValue.equals("1"))
                {
                    _newImgFormatValue = 1;
                }
                else if(newValue.equals("2"))
                {
                    _newImgFormatValue = 2;
                }
                else if(newValue.equals("3"))
                {
                    _newImgFormatValue = 3;
                }
                else if(newValue.equals("4"))
                {
                    _newImgFormatValue = 4;
                }
                else if(newValue.equals("5"))
                {
                    _newImgFormatValue = 5;
                }
                mDataStorage.setImgformatValue(_newImgFormatValue);
                Logger.d("preference data check : " + mDataStorage.getImgformatValue());
                mCurrentImgValueIndex = mImgFormatList.findIndexOfValue((String)newValue);
                break;
            case "missingfinger_activate":
                Logger.d("missingfinger is changed");
                if(mDataStorage.getSegmentationActivateValue()==false)
                {
                    Toast.makeText(mContext,"Please enable Segmentation first.",Toast.LENGTH_SHORT).show();
                    return false;
                }
                mDataStorage.setMissingFingerActivateValue((boolean)newValue);
                Logger.d("preference data check : " + mDataStorage.getMissingFingerActivateValue());
                mIsMissingFingerActivate = mDataStorage.getMissingFingerActivateValue();
                mDeviceDataHandler.setMissingFingerActivate(mIsMissingFingerActivate);
                sendMessageToAcitivityHandler(UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE, mDeviceDataHandler.getCaptureMode());
                break;
            case "minimum_number_of_finger":
                Logger.d("minimum_number_of_finger is changed. value: " + (String)newValue);
                if(mDeviceDataHandler.getFingerCountFromCaptureMode() - Integer.parseInt((String)newValue) < 0 &&
                mDeviceDataHandler.getCaptureMode() != CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED)
                {
                    Toast.makeText(mContext,"Minimum number should be same or smaller than capture mode finger count.",Toast.LENGTH_LONG).show();
                    return false;
                }
                mDataStorage.setMinimumNumberOfFinger(Integer.parseInt((String)newValue));
                mMinimumNumberOfFingerValue = mDataStorage.getMinimumNumberOfFinger();
                Logger.d("data check: " + mMinimumNumberOfFingerValue);
                break;
            case "fingerselect":
                Logger.d("fingerselect is changed.");
                HashSet<String> _dataset = (HashSet)newValue;
                Logger.d("_dataset size: " + _dataset.size());
                if(_dataset.size() == 0)
                {
                    Toast.makeText(mContext,"Finger is not selected.",Toast.LENGTH_LONG).show();
                    return false;
                }
                int _fingerCount = mDeviceDataHandler.getFingerCountFromCaptureMode();
                if(mDeviceDataHandler.isMissingFingerActivate() == false)
                {
                    Toast.makeText(mContext,"Please enable missing finger.",Toast.LENGTH_LONG).show();
                    return false;
                }
                if(_dataset.size() <1 || _dataset.size() > 10)
                {
                    Toast.makeText(mContext,mActivity.getResources().getString(R.string.pref_fingerselect_error),Toast.LENGTH_SHORT).show();
                    return false;
                }
                List _tempfingerList = new ArrayList((HashSet)newValue);
                Collections.sort(_tempfingerList);

                mDataStorage.setSelectedFinger(new HashSet<String>(_tempfingerList));
                mDeviceDataHandler.setFingerSelectSet((HashSet<String>) mDataStorage.getSelectedFinger());
//                if(mDataStorage.getHandselectValue() == DeviceDataHandler.HandType.RIGHT)
//                {
//                    List _fingerList = new ArrayList((HashSet)newValue);
//                    Collections.sort(_fingerList);
//                    mDataStorage.setRightSelectedFinger(new HashSet<String>(_fingerList));
//
//                    mMissingFingerRightSet = mDataStorage.getRightSelectedFinger();
//                    Iterator<String> it = mMissingFingerRightSet.iterator();
//                    while (it.hasNext())
//                    {
//                        Logger.d("mMissingFingerSet Right: " + it.next());
//                    }
//                }
//                if(mDataStorage.getHandselectValue() == DeviceDataHandler.HandType.LEFT)
//                {
//                    List _fingerList = new ArrayList((HashSet)newValue);
//                    Logger.d("do reverse order!");
//                    Collections.sort(_fingerList,Collections.reverseOrder());
//                    mDataStorage.setLeftSelectedFinger(new HashSet<String>(_fingerList));
//
//                    mMissingFingerLeftSet = mDataStorage.getLeftSelectedFinger();
//                    Iterator<String> it = mMissingFingerLeftSet.iterator();
//                    while (it.hasNext())
//                    {
//                        Logger.d("mMissingFingerSet Left: " + it.next());
//                    }
//                }
                break;
            case "customsegmentsize_activate":
                Logger.d("customsegmentsize_activate is changed");
                mDataStorage.setCustomsegSizeActivate(Integer.valueOf((String)newValue));

                mIsCustomSegSizeActivate = mDataStorage.getCustomsegSizeActivate();
                mDeviceDataHandler.setCustomSegSizeActivate(mDataStorage.getCustomsegSizeActivate());
                updateCustomSegmentAutoSetCustomWidthHeight(mIsCustomSegSizeActivate);
                mCustomSegSizeActivatePref.setSummary(getCaptureModeLabel(mIsCustomSegSizeActivate));
                Logger.d("preference data check : " + mIsCustomSegSizeActivate);
                break;
            case "pref_previewquality":
                Logger.d("pref_previewQuality is changed");
                mDataStorage.setPreviewQuality(Integer.valueOf((String)newValue));
                mIsPreviewQualitryMode = mDataStorage.getPreviewQuality();
                mDeviceDataHandler.setPreviewQualityMode(mDataStorage.getCustomsegSizeActivate());
                mPreviewQualityPref.setSummary(getPreviewQualityModeLabel(mIsPreviewQualitryMode));
                Logger.d("preference data check : " + mIsPreviewQualitryMode);
                break;
            case "custom_segmentsize_width":
                Logger.d("custom_segmentsize_width is changed");
                int _intWidthVal = Integer.parseInt((String)newValue);
                if(_intWidthVal < 0 || _intWidthVal > mActivity._rsApi.getDeviceWidth())
                {
                    Toast.makeText(mContext,mActivity.getResources().getString(R.string.pref_error_size_value_range_width),Toast.LENGTH_SHORT).show();
                    return false;
                }
                mDataStorage.setCustomSegSizeWidth(_intWidthVal);
                mCustomSegmentSizeWidth = mDataStorage.getCustomSegSizeWidth();
                mDeviceDataHandler.setCustomSegmentSizeWidth(mDataStorage.getCustomSegSizeWidth());
                mCustomSegSizeWidthPref.setSummary((String)newValue);
                break;
            case "custom_segmentsize_height":
                Logger.d("custom_segmentsize_height is changed");
                int _intHeightVal = Integer.parseInt((String)newValue);
                if(_intHeightVal < 0 || _intHeightVal > mActivity._rsApi.getDeviceHeight())
                {
                    Toast.makeText(mContext,mActivity.getResources().getString(R.string.pref_error_size_value_range_height),Toast.LENGTH_SHORT).show();
                    return false;
                }
                mDataStorage.setCustomSegSizeHeight(_intHeightVal);
                mCustomSegmentSizeHeight = mDataStorage.getCustomSegSizeHeight();
                mDeviceDataHandler.setCustomSegmentSizeHeight(mDataStorage.getCustomSegSizeHeight());
                mCustomSegSizeHeightPref.setSummary((String)newValue);
                break;
            case "checkCustomerId":
                Logger.d("checkCustomerId");
                if(RealScanAPI.getInstance().getCurrentDevice() != null)
                {
                    if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10
                        && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_SG10
                        && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10i
                        && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10iBIOS
                        && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_F_C)
                    {
                        Toast.makeText(mContext,"checkCustomerId func is supported only RealScan G10.",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                mDataStorage.setCustomerIdActivate((boolean)newValue);
                mIsCustimerIdActivate = mDataStorage.getCustomerIdActivate();
                mDeviceDataHandler.setUseCustomerId(mIsCustimerIdActivate);
                Logger.d("preference data check : " + mIsCustimerIdActivate);
                break;
            case "checkCustomerId_value":
                Logger.d("checkCustomerId_value");
                if(RealScanAPI.getInstance().getCurrentDevice() != null)
                {
                    if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10
                            && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_SG10
                            && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10i
                            && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10iBIOS
                            && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_F_C
                        ) {
                        Toast.makeText(mContext,"checkCustomerId func is supported only RealScan G10.",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                mDataStorage.setCustomerIdValue((String)newValue);
                mCustomerIdValue = mDataStorage.getCustomerIdValue();
                mDeviceDataHandler.setCustomerIdValue(mCustomerIdValue);
                mActivity.checkCustomerId(mCustomerIdValue);
                Logger.d("preference data check : " + mCustomerIdValue);
                break;
            case "pref_dirtycheck":
                Logger.d("pref_dirtycheck is changed! value = " + newValue);
                if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_S60)
                {
                    Toast.makeText(mContext,"REALSCAN_S60 isn't support dirtcheck.",Toast.LENGTH_LONG).show();
                    return false;
                }
                mDataStorage.setDirtyCheckActivate((boolean)newValue);
                mIsDirtyCheckActivate = mDataStorage.getDirtyCheckActivate();
                mDeviceDataHandler.setUseDirtyCheck(mIsDirtyCheckActivate);
                Logger.d("data check : " + mDataStorage.getDirtyCheckActivate());
                break;
            case "pref_dirtycheck_level":
                Logger.d("pref_dirtycheck_level is changed!. value = " + newValue);
                if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_S60)
                {
                    Toast.makeText(mContext,"REALSCAN_S60 isn't support pref_dirtycheck level.",Toast.LENGTH_LONG).show();
                    return false;
                }
                int _level = Integer.valueOf((String)newValue);
                if(_level < 1 || _level > 5)
                {
                    Toast.makeText(mContext,mActivity.getResources().getString(R.string.pref_dirtycheck_level_wrong_value),Toast.LENGTH_SHORT).show();
                    return false;
                }
                mDataStorage.setDirtyCheckLevelValue(_level);
                mDirtyCheckLevel = mDataStorage.getDirtyCheckLevelValue();

                mDeviceDataHandler.setDirtyCheckLevel(mDirtyCheckLevel);
                Logger.d("data check : " + mDataStorage.getDirtyCheckLevelValue());
                break;
            case "pref_nfiqmode":
                Logger.d("pref_nfiqmode is changed value = " + newValue);
                if(mDataStorage.getSegmentationActivateValue()==false)
                {
                    Toast.makeText(mContext,"Please enable Segmentation first.",Toast.LENGTH_SHORT).show();
                    return false;
                }
                mDataStorage.setNfiqMode((boolean) newValue);
                mNfiqMode = mDataStorage.getNfiqMode();
                mDeviceDataHandler.setNfiqmode(mNfiqMode);
                Logger.d("data check : " + mNfiqMode);
                break;
            case "pref_lfdmode":
                Logger.d("pref_lfdmode is changed value = " + newValue);
                if(mDataStorage.getSegmentationActivateValue()==false)
                {
                    Toast.makeText(mContext,"Please enable Segmentation first.",Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_D)
                {
                    Toast.makeText(mContext,"RealScan D is not support LFD.",Toast.LENGTH_SHORT).show();
                    return false;
                }
                int _lfd = Integer.parseInt((String)newValue);
                if(_lfd < 0 || _lfd > 5){
                    Toast.makeText(mContext,"The value is not available. Please enter a value in the correct range",Toast.LENGTH_SHORT).show();
                    return false;
                }
                mDataStorage.setLfdMode(_lfd);
                mLfdMode = mDataStorage.getLfdMode();
                mDeviceDataHandler.setLfdActivated(mLfdMode);
                Logger.d("data check : " + mLfdMode);
                break;
            case "sequence_check_capture_mode":
                Logger.d("sequence_check_capture_mode is changed value = " + newValue);
                if(mDataStorage.getSegmentationActivateValue()==false)
                {
                    Toast.makeText(mContext,"Please enable Segmentation first.",Toast.LENGTH_SHORT).show();
                    return false;
                }
                int value = Integer.parseInt((String)newValue);
                mDataStorage.setSequenceCheckCaptureMode(value);
                mSequenceCheckCaptureMode = mDataStorage.getSequenceCheckCaptureMode();
                break;
            case "pref_enhancedpreviewmode":
                Logger.d("pref_enhancedpreviewmode is changed value = " + newValue);
                mDataStorage.setEnhancedPreviewMode((boolean)newValue);
                mEnhancedPreviewMode = mDataStorage.getEnhancedPreviewMode();
                break;
            case "pref_usbNativeUsbMode":
                Logger.d("pref_usbNativeUsbMode is changed!");
                mDataStorage.setUseNativeUsbModeParam((boolean)newValue);
                Logger.d("data check : " + mDataStorage.getUseNativeUsbModeParam());
                Logger.d("data check : " + mDataStorage.getLowCpuDeviceModeParam());
                mDeviceDataHandler.setNativelibUsb(mDataStorage.getUseNativeUsbModeParam());
                mActivity.setTransferMode(mDataStorage.getUseNativeUsbModeParam(),true);
                break;
            case "pref_captureareaexpandmode":
                Logger.d("pref_captureareaexpandmode is changed value = " + newValue);
                mDataStorage.setCaptureAreaExpandMode((boolean)newValue);
                mEnhancedPreviewMode = mDataStorage.getCaptureAreaExpandMode();
                break;
            case "pref_haloreductionenhanced":
                Logger.d("pref_haloreductionenhanced is changed value = " + newValue);
                mDataStorage.setHaloReductionEnhandedMode((boolean)newValue);
                mDeviceDataHandler.setHaloReductionEnhanced(mDataStorage.getHaloReductionEnhandedExpandMode());
                mHaloReductionEnhandedMode = mDataStorage.getHaloReductionEnhandedExpandMode();
                break;
            case "pref_supportLowCpuDevice":
                Logger.d("pref_supportLowCpuDevice is changed!");
                mDataStorage.setLowCpuDeviceModeParam(((boolean)newValue));
                Logger.d("data check : " + mDataStorage.getLowCpuDeviceModeParam());
                mDeviceDataHandler.setLowSpeedUsbMode(mDataStorage.getLowCpuDeviceModeParam());
                break;
            case "pref_s60Capture_measurement_enable_mode":
                Logger.d("pref_s60Capture_measurement_enable_mode is changed.");
                mDataStorage.setS60ExternalBrightEnableModeValue((boolean)newValue);
                mDeviceDataHandler.setS60ExternalBrightEnableMode(mDataStorage.getS60ExternalBrightEnableModeValue());
                updateS60CaptureExternalBrightMeasurementListPref((boolean)newValue);
                break;
            case "pref_s60Capture_measurement_mode":
                Logger.d("pref_s60Capture_measurement_mode list is changed.");
                int _external_bright_measuremode = Integer.parseInt((String)newValue);
                Logger.d("_illumination_threshold : " + _external_bright_measuremode);
                mDataStorage.setS60CaptureExternalBrightMeasureModeValue(_external_bright_measuremode);
                mS60CaptureBrightMeasureListPref.findIndexOfValue((String)newValue);
                mDeviceDataHandler.setS60CaptureExternalBrightMeasureMode(_external_bright_measuremode);
                break;
            case "pref_sensitive_mode":
                Logger.d("pref_sensitive_mode is changed value list is changed");
                int _sensitive = Integer.parseInt((String)newValue);
                Logger.d("data check : " + _sensitive);
                mDataStorage.setSensitiveMode(_sensitive);
                mSensitiveModeListPref.findIndexOfValue((String)newValue);
                mDeviceDataHandler.setSensitiveLevel(_sensitive);
                RealScanAPI.getInstance().setSensitiveLevelValue(_sensitive);
                break;
            case "pref_setLed_mode":
                Logger.d("pref_s60Capture_measurement_enable_mode is changed.");
                mDataStorage.setCustomLedMode((boolean)newValue);
                mIsCustomLedMode = mDataStorage.getCustomLedMode();
                mDeviceDataHandler.setCustomLedMode(mDataStorage.getCustomLedMode());
                break;
        }
        mHandler.sendEmptyMessage(EVENT_SCREEN_UPDATE);
        return true;
    }

    @SuppressLint("NewApi")
    public int pref()
    {
        Logger.d("START!");
        if(RealScanAPI.getInstance() == null)
            return ErrorCode.Err.getErrorCode();
        if(RealScanAPI.getInstance().hasSelectedDevice() == false)
        {
            Logger.d("Device is not connected.");
            return ErrorCode.ERROR_NOT_FOUND_DEVICE.getErrorCode();
        }
        if(mDataStorage == null )
        {
            Logger.d("mDataStorage is null");
            return ErrorCode.Err.getErrorCode();
        }
        if(mDeviceDataHandler == null)
        {
            mDeviceDataHandler = DeviceDataHandler.getInstance();
        }
        updateImageFormatPref();

        updateSegmentationActivate();

        updateCaptureTimeoutPref();

        updateRollDirectionPref();

        updateSequenceCheckCaptureModePref();

        mIsMissingFingerActivate = mDataStorage.getMissingFingerActivateValue();
        mDeviceDataHandler.setMissingFingerActivate(mIsMissingFingerActivate);
        mFingerActivate.setChecked(mIsMissingFingerActivate);

        updateMimimumFingerPref();
        mFingerActivate.setChecked(mIsMissingFingerActivate);
        if(mIsMissingFingerActivate == true )
        {
            Logger.d("mIsMissingFingerActivate is True");

            mFingerSelect.setEnabled(true);

            Set<String> _fingerSet = mDataStorage.getSelectedFinger();
            mDeviceDataHandler.setFingerSelectSet((HashSet<String>) _fingerSet);
            Logger.d("_fingerSet: " + _fingerSet.size());
            String _fingerSelectValue = "";
            List _fingerList = new ArrayList(_fingerSet);
            if(_fingerList.size() > 0)
            {
                for(int i =0; i < _fingerList.size(); i++)
                {
                    int fingerSelectValueIndex = mFingerSelect.findIndexOfValue((String) _fingerList.get(i));
                    Logger.d("entry : " + mFingerSelect.getEntries()[fingerSelectValueIndex] + " value: " + _fingerList.get(i));
                    _fingerSelectValue = _fingerSelectValue + " " + mFingerSelect.getEntries()[fingerSelectValueIndex];
                }
                mFingerSelect.setValues(new HashSet<String>(_fingerSet));
                mFingerSelect.setSummary(_fingerSelectValue);
            }
            else {
                mFingerSelect.setValues(new HashSet<String>(_fingerSet));
                mFingerSelect.setSummary(mActivity.getResources().getString(R.string.pref_fingerselect_summery));
            }

        }
        else
        {
            mFingerSelect.setEnabled(false);
            mFingerSelect.setSummary(mActivity.getResources().getString(R.string.pref_fingerselect_summery));
        }
        mIsCustomSegSizeActivate = mDataStorage.getCustomsegSizeActivate();
        mDeviceDataHandler.setCustomSegSizeActivate(mIsCustomSegSizeActivate);
        Logger.d("mIsCustomSegSizeActivate : " + mIsCustomSegSizeActivate);
        if(mIsCustomSegSizeActivate > 0) {
            mCustomSegSizeActivatePref.setValueIndex(getIndexCaptureMode(mIsCustomSegSizeActivate));
            mCustomSegSizeActivatePref.setSummary(getCaptureModeLabel(mIsCustomSegSizeActivate));
            mCustomSegSizeWidthPref.setEnabled(true);
            mCustomSegSizeHeightPref.setEnabled(true);

            mCustomSegSizeWidthPref.setSummary(String.valueOf(mDataStorage.getCustomSegSizeWidth()));
            mDeviceDataHandler.setCustomSegmentSizeWidth(mDataStorage.getCustomSegSizeWidth());
            mCustomSegSizeHeightPref.setSummary(String.valueOf(mDataStorage.getCustomSegSizeHeight()));
            mDeviceDataHandler.setCustomSegmentSizeHeight(mDataStorage.getCustomSegSizeHeight());
        }else {
            mCustomSegSizeActivatePref.setValueIndex(0);
            mCustomSegSizeActivatePref.setSummary(getCaptureModeLabel(0));
            mCustomSegSizeWidthPref.setEnabled(false);
            mCustomSegSizeHeightPref.setEnabled(false);

            mCustomSegSizeWidthPref.setSummary(mActivity.getResources().getString(R.string.pref_custom_segmentsize_width_summery));
            mCustomSegSizeHeightPref.setSummary(mActivity.getResources().getString(R.string.pref_custom_segmentsize_height_summery));
        }

        updateCustomerIdPref();
        updateCustomerIdValuePref();
        updateDirtyCheckPref();
        updateDirtyCheckLevelValuePref();
        updateNfiqModePref();
        updateLfdModePref();
        updateEnhancedPreviewModePref();
        updateCaptureAreaExpandModePref();
        updateHaloReductionEnhandedModePref();
        updateUseNativeUsbModePref();
        updateCustomLedMode();
        updateCustomLedModeValue();
        updatePreviewQualityPref();


        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_S60) {
            updateS60ExternalEnableBrightModePref(true);
            updateS60CaptureExternalBrightMeasurementListPref(true);
            updateSensitiveModePref(true);
        }else{
            updateS60ExternalEnableBrightModePref(false);
            updateS60CaptureExternalBrightMeasurementListPref(false);
            updateSensitiveModePref(false);
        }

        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_S60){
            updateSupportLowCpuDeviceModePref(true);
        } else {
            updateSupportLowCpuDeviceModePref(false);
        }



        mIsPrefRefreshed = true;
        return 0;
    }
    private void updateUseNativeUsbModePref() {
        boolean _usbNativeUsbMode = mDataStorage.getUseNativeUsbModeParam();
        Logger.d("_usbNativeUsbMode : " + _usbNativeUsbMode);
        mUseNativeUsbModePref.setChecked(_usbNativeUsbMode);
        mDeviceDataHandler.setNativelibUsb(_usbNativeUsbMode);
        if(_usbNativeUsbMode) {
            mUseNativeUsbModePref.setSummary("");
        }else{
            if(mContext == null){
                mUseNativeUsbModePref.setSummary("Set USB mode to Native USB Mode. Default Mode is android USB Mode");
            }else{
                mUseNativeUsbModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_useNativeUsbMode));
            }
        }
    }

    private void updateEnhancedPreviewModePref() {
        boolean _mode = mDataStorage.getEnhancedPreviewMode();

        mDeviceDataHandler.setEnhancedPreviewMode(_mode);
        mEnhancedPreviewModePref.setChecked(_mode);
        if(_mode) {
            mEnhancedPreviewModePref.setSummary("");
        }else {
            if(mContext == null){
                mEnhancedPreviewModePref.setSummary("Preview images are displayed larger in independent dialogs.");
            }else{
                mEnhancedPreviewModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_enhancedpreviewmode));
            }
        }
    }

    private void updateCaptureAreaExpandModePref() {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_D) {
            mCaptureAreaExpandModePref.setVisible(false);
            return;
        }else{
            mCaptureAreaExpandModePref.setVisible(true);
        }
        boolean _mode = mDataStorage.getCaptureAreaExpandMode();

        mDeviceDataHandler.setCaptureAreaExpandMode(_mode);
        mCaptureAreaExpandModePref.setChecked(_mode);
        if(_mode) {
            mCaptureAreaExpandModePref.setSummary("");
        }else {
            if(mContext == null){
                mCaptureAreaExpandModePref.setSummary("Expand the capture area to the maximum.");
            }else{
                mCaptureAreaExpandModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_captureareaexpandmode));
            }
        }
    }

    private void updateSequenceCheckCaptureModePref() {
        int _mode = mDataStorage.getSequenceCheckCaptureMode();

        if(_mode == 0) {
            mDeviceDataHandler.setSequenceCheckCaptureMode(CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER);
        }else if(_mode == 1) {
            mDeviceDataHandler.setSequenceCheckCaptureMode(CaptureMode.RS_CAPTURE_ROLL_FINGER);
        }
        mSequenceCheckCaptureModePref.setSummary(mSequenceCheckCaptureModePref.getEntries()[_mode]);
        mSequenceCheckCaptureModePref.setValueIndex(_mode);
    }

    private void updateLfdModePref() {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_D) {
            mLfdModePrefCategory.setVisible(false);
            mLfdModePref.setVisible(false);
            mLfdScorePref.setVisible(false);
            return;
        }else {
            mLfdModePrefCategory.setVisible(true);
            mLfdModePref.setVisible(true);
            mLfdScorePref.setVisible(true);
        }
        int _lfdlevel = mDataStorage.getLfdMode();
        Logger.d("_lfdlevel: " + _lfdlevel);
            if(mContext == null){
                mLfdModePref.setSummary("LFD 1~5 range value." + "Current : " + String.valueOf(_lfdlevel));
            }else{
                mLfdModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_lfdmode) + "Current : " + String.valueOf(_lfdlevel));
            }

        if(_lfdlevel > 0) {
            mLfdModePref.setEnabled(true);
            mLfdScorePref.setEnabled(true);
            mLfdScorePref.setSummary("");
        } else {
            //mLfdModePref.setChecked(false);
            mLfdScorePref.setEnabled(false);
            if(mContext == null){
                mLfdScorePref.setSummary("Number of fingers to capture");
            }else{
                mLfdScorePref.setSummary(mActivity.getResources().getString(R.string.pref_summery_lfd_score));
            }
        }
        mDeviceDataHandler.setLfdActivated(_lfdlevel);
    }

    private void updateMimimumFingerPref() {
        int _value = mDataStorage.getMinimumNumberOfFinger();
        mDeviceDataHandler.setMinimumNumberOfFinger(_value);
        Logger.d("_value: " + _value);
        if(_value == 0) {
            return;
        }
        mMinimumNumberOfFingerPref.setSummary(mMinimumNumberOfFingerPref.getEntries()[_value-1]);
        mMinimumNumberOfFingerPref.setValueIndex(_value-1);
    }

    private void updateRollDirectionPref() {
        int _rollDirectionValue = mDataStorage.getRollCaptureDirectionValue();
        mDeviceDataHandler.setRollDirection(_rollDirectionValue);
        mRollDirectionPref.setSummary(mRollDirectionPref.getEntries()[_rollDirectionValue]);
        mRollDirectionPref.setValueIndex(_rollDirectionValue);
    }

    private void updateCaptureTimeoutPref() {
        int _timeoutValue = mDataStorage.getCaptureTimeoutValue();
        mDeviceDataHandler.setCaptureTimeout(_timeoutValue);
        if(_timeoutValue == 0) {
            mCaptureTimeoutPref.setSummary("No timeout.");
        }else {
            mCaptureTimeoutPref.setSummary(_timeoutValue / 1000 + " sec");
        }
    }

    private void updateNfiqModePref() {
        boolean _mode = mDataStorage.getNfiqMode();
        Logger.d("_mode: " + _mode);
        if(_mode) {
            mNfiqModePref.setEnabled(true);
            mNfiqScorePref.setEnabled(true);
            mNfiqModePref.setSummary("");
            mNfiqScorePref.setSummary("");
        } else {
            mNfiqModePref.setChecked(false);
            mNfiqScorePref.setEnabled(false);
            if(mContext == null){
                mNfiqModePref.setSummary("Select enable/disable NFIQ mode.");
                mNfiqScorePref.setSummary("Score Range is 1~5 with 1 step.");
            }else{
                mNfiqModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_nfiqmode));
                mNfiqScorePref.setSummary(mContext.getResources().getString(R.string.pref_summery_nfiq_score));
            }
        }
        mDeviceDataHandler.setNfiqmode(_mode);
    }

    private void updateSegmentationActivate() {
        boolean _segmentationActivate = mDataStorage.getSegmentationActivateValue();
        Logger.d("_segmentationActivate : " + _segmentationActivate);
        mSegmentationPref.setChecked(_segmentationActivate);
        mSegmentationActivate = _segmentationActivate;
        mDeviceDataHandler.setSegmentationActivate(mSegmentationActivate);
        if(mSegmentationActivate) {
            mSegmentationPref.setSummary("");
        } else {
            if(mContext == null){
                mSegmentationPref.setSummary("Enable/Disable check segmentation function.");
            }else{
                mSegmentationPref.setSummary(mContext.getResources().getString(R.string.pref_summery_segmentation));
            }
            mDataStorage.setNfiqMode(_segmentationActivate);
            if(mDeviceDataHandler.isMissingFingerActivate()) {
                mDataStorage.setMissingFingerActivateValue(_segmentationActivate);
            }
        }
    }

    private void updateImageFormatPref() {
        int _currentImgValueIndex = 0;
        int _format = mDataStorage.getImgformatValue();
        if(_format == 1) {
            _currentImgValueIndex = 0;
            mDeviceDataHandler.setImageFormat(DeviceDataHandler.ImageFormat.JPEG);
        }else if(_format == 2) {
            _currentImgValueIndex = 1;
            mDeviceDataHandler.setImageFormat(DeviceDataHandler.ImageFormat.BITMAP);
        }else if(_format == 3) {
            _currentImgValueIndex = 2;
            mDeviceDataHandler.setImageFormat(DeviceDataHandler.ImageFormat.WSQ);
        }else if(_format == 4) {
            _currentImgValueIndex = 3;
            mDeviceDataHandler.setImageFormat(DeviceDataHandler.ImageFormat.PGM);
        }else if(_format == 5) {
            _currentImgValueIndex = 4;
            mDeviceDataHandler.setImageFormat(DeviceDataHandler.ImageFormat.ISO_IEC_19794_4);
        }
        mImgFormatList.setSummary(mImgFormatList.getEntries()[_currentImgValueIndex]);
        mImgFormatList.setValueIndex(_currentImgValueIndex);
    }

    private void updateCustomerIdPref() {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10
                && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_SG10
                && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10i
                && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10iBIOS
            ) {
            mCustomerIdPrefCategory.setVisible(false);
            mCheckCustomerIdPref.setVisible(false);
            return;
        }else {
            mCustomerIdPrefCategory.setVisible(true);
            mCheckCustomerIdPref.setVisible(true);
        }
        boolean _customerIdActivate = mDataStorage.getCustomerIdActivate();
        Logger.d("_customerIdActivate : " + _customerIdActivate);
        mCheckCustomerIdPref.setChecked(_customerIdActivate);
        mIsCustimerIdActivate = _customerIdActivate;
        mDeviceDataHandler.setUseCustomerId(mIsCustimerIdActivate);
        if(mIsCustimerIdActivate) {
            mCheckCustomerIdPref.setSummary("");
        }else {
            if(mContext == null){
                mCheckCustomerIdPref.setSummary("Enable/Disable check Customer Id function.");
            }else{
                mCheckCustomerIdPref.setSummary(mContext.getResources().getString(R.string.checkCustomerId_function_summery));
            }
        }
    }

    private void updateCustomerIdValuePref() {
        if (RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10
                && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_SG10
                && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10i
                && RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_G10iBIOS
        ) {
            mCheckCustomerIdValuePref.setVisible(false);
            return;
        } else {
            mCheckCustomerIdValuePref.setVisible(true);
        }
        String _customerIdValue = mDataStorage.getCustomerIdValue();
        Logger.d("_customerIdActivate : " + mIsCustimerIdActivate);
        mDeviceDataHandler.setCustomerIdValue(_customerIdValue);
        if(mIsCustimerIdActivate) {
            if(_customerIdValue.equals("")){
                mCheckCustomerIdValuePref.setSummary("Not set");
            }else{
                mCheckCustomerIdValuePref.setSummary(_customerIdValue);
            }
            mCheckCustomerIdValuePref.setEnabled(true);
        }else{
            if(mContext == null){
                mCheckCustomerIdValuePref.setSummary("Enable/Disable check Customer Id function.");
            }else{
                mCheckCustomerIdValuePref.setSummary(mContext.getResources().getString(R.string.checkCustomerId_function_summery));
            }
            mCheckCustomerIdValuePref.setEnabled(false);
        }
    }
    private void updateDirtyCheckPref() {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_S60) {
            mDirtyCheckCategory.setVisible(false);
            mDirtyCheckPref.setVisible(false);
            mDirtyCheckLevelPref.setVisible(false);
            return;
        }else{
            mDirtyCheckCategory.setVisible(true);
            mDirtyCheckPref.setVisible(true);
            mDirtyCheckLevelPref.setVisible(true);
        }
        boolean _dirtycheckActivate = mDataStorage.getDirtyCheckActivate();
        Logger.d("_dirtycheckActivate : " + _dirtycheckActivate);
        mDirtyCheckPref.setChecked(_dirtycheckActivate);
        mIsDirtyCheckActivate = _dirtycheckActivate;
        mDeviceDataHandler.setUseDirtyCheck(mIsDirtyCheckActivate);
        if(mIsDirtyCheckActivate) {
            mDirtyCheckPref.setSummary("");
        }else{
            if(mContext == null){
                mDirtyCheckPref.setSummary("Activates the function to check the dirt on the sensor surface.");
            }else{
                mDirtyCheckPref.setSummary(mContext.getResources().getString(R.string.pref_summery_dirtycheck));
            }
        }
    }

    private void updateCustomLedMode() {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_SG10) {
            mCustomLedModePrefCategory.setVisible(true);
            mLedSettingCustomeEnableModePref.setVisible(true);
            modeLEDPreference.setVisible(true);
            statusLEDPreference.setVisible(true);
        }else{
            mCustomLedModePrefCategory.setVisible(false);
            mLedSettingCustomeEnableModePref.setVisible(false);
            modeLEDPreference.setVisible(false);
            statusLEDPreference.setVisible(false);
        }

        boolean customeLedMode = mDataStorage.getCustomLedMode();
        Logger.d("customeLedMode : " + customeLedMode);
        mLedSettingCustomeEnableModePref.setChecked(customeLedMode);
        mIsCustomLedMode = customeLedMode;
        mDeviceDataHandler.setCustomLedMode(mIsCustomLedMode);
        if(mIsDirtyCheckActivate) {
            mLedSettingCustomeEnableModePref.setSummary("");
        }else{
            if(mContext == null){
                mLedSettingCustomeEnableModePref.setSummary("The LED color can be customized.");
            }else{
                mLedSettingCustomeEnableModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_customLedMode));
            }
        }
    }

    private void updateCustomLedModeValue() {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_SG10) {
            mCustomLedModePrefCategory.setVisible(false);
            mLedSettingCustomeEnableModePref.setVisible(false);
            modeLEDPreference.setVisible(false);
            statusLEDPreference.setVisible(false);
            return;
        }

        if(mIsCustomLedMode) {
            modeLEDPreference.setSummary("modeLEDPreference");
            modeLEDPreference.setEnabled(true);

            statusLEDPreference.setSummary("statusLEDPreference");
            statusLEDPreference.setEnabled(true);
        }else{
            if(mContext == null){
                modeLEDPreference.setSummary(mContext.getResources().getString(R.string.pref_summary_mode_led_preference_mode));
                statusLEDPreference.setSummary(mContext.getResources().getString(R.string.pref_summary_status_led_preference_mode));
            }else{
                modeLEDPreference.setSummary(mContext.getResources().getString(R.string.pref_summary_mode_led_preference_mode));
                statusLEDPreference.setSummary(mContext.getResources().getString(R.string.pref_summary_status_led_preference_mode));
            }
            modeLEDPreference.setEnabled(false);
            statusLEDPreference.setEnabled(false);
        }
    }

    private void updateDirtyCheckLevelValuePref() {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_S60) {
            mDirtyCheckLevelPref.setVisible(false);
            return;
        }
        int _dirtyCheckLevelValue = mDataStorage.getDirtyCheckLevelValue();
        Logger.d("_dirtyCheckLevelValue : " + _dirtyCheckLevelValue);
        mDeviceDataHandler.setDirtyCheckLevel(_dirtyCheckLevelValue);
        if(mIsDirtyCheckActivate) {
            mDirtyCheckLevelPref.setSummary(String.valueOf(_dirtyCheckLevelValue));
            mDirtyCheckLevelPref.setEnabled(true);
        }else{
            if(mContext == null){
                mDirtyCheckLevelPref.setSummary("Set dirty check level.");
            }else{
                mDirtyCheckLevelPref.setSummary(mContext.getResources().getString(R.string.pref_summery_dirtycheck_level));
            }
            mDirtyCheckLevelPref.setEnabled(false);
        }
    }

    private void updateHaloReductionEnhandedModePref() {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_D
                || RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_G10
                || RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_SG10
                || RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_G10i
                || RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_G10iBIOS
        ) {
            mHaloReductionEnhandedModePref.setVisible(true);
        }else{
            mHaloReductionEnhandedModePref.setVisible(false);
        }
        boolean _mode = mDataStorage.getHaloReductionEnhandedExpandMode();

        mDeviceDataHandler.setHaloReductionEnhanced(_mode);
        mHaloReductionEnhandedModePref.setChecked(_mode);
        if(_mode) {
            mHaloReductionEnhandedModePref.setSummary("");
        }else {
            if(mContext == null){
                mHaloReductionEnhandedModePref.setSummary("Set Halo Reduction Enhanced.");
            }else{
                mHaloReductionEnhandedModePref.setSummary(mContext.getResources().getString(R.string.pref_summury_haloreductionenhanced));
            }
        }
    }


    private void updateSupportLowCpuDeviceModePref(boolean _enable) {
        mSupportLowCpuDeviceModePref.setVisible(_enable);
        mSupportLowCpuDeviceModePref.setEnabled(_enable);
        boolean _supportLowCpuDeviceMode = mDataStorage.getLowCpuDeviceModeParam();
        Logger.d("_supportLowCpuDeviceMode : " + _supportLowCpuDeviceMode);
        mSupportLowCpuDeviceModePref.setChecked(_supportLowCpuDeviceMode);
        mDeviceDataHandler.setLowSpeedUsbMode(_supportLowCpuDeviceMode);
        if(_supportLowCpuDeviceMode) {
            mSupportLowCpuDeviceModePref.setSummary("");
        }else{
            if(mContext == null){
                mSupportLowCpuDeviceModePref.setSummary("Set mode to support low cpu device.");
            }else{
                mSupportLowCpuDeviceModePref.setSummary(mContext.getResources().getString(R.string.pref_summery_supportLowCpuDevice));
            }
        }
    }

    private void updatePreviewQualityPref(){
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_SG10) {
            mPreviewQualityPref.setVisible(true);
        } else {
            mPreviewQualityPref.setVisible(false);
        }

        mIsPreviewQualitryMode = mDataStorage.getPreviewQuality();
        mDeviceDataHandler.setPreviewQualityMode(mIsPreviewQualitryMode);
        mPreviewQualityPref.setSummary(getPreviewQualityModeLabel(mIsPreviewQualitryMode));
    }

    private void updateS60ExternalEnableBrightModePref(boolean _enable) {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_S60) {
            mS60CaptureSettingCategory.setVisible(false);
            mS60ExternalBrightEnableModePref.setVisible(false);
            mS60ExternalBrightEnableModePref.setEnabled(false);
            return;
        }else{
            mS60CaptureSettingCategory.setVisible(_enable);
            mS60ExternalBrightEnableModePref.setVisible(_enable);
            mS60ExternalBrightEnableModePref.setEnabled(_enable);
        }
        boolean _mode = mDataStorage.getS60ExternalBrightEnableModeValue();

        mDeviceDataHandler.setS60ExternalBrightEnableMode(_mode);
        mS60ExternalBrightEnableModePref.setChecked(_mode);
        if(_mode) {
            mS60ExternalBrightEnableModePref.setSummary("");
        }else {
            if(mContext == null){
                mS60ExternalBrightEnableModePref.setSummary("Set External Bright measurement (default is Enable).");
            }else{
                mS60ExternalBrightEnableModePref.setSummary(mContext.getResources().getString(R.string.pref_summary_s60Capture_measurement_enable_mode));
            }
        }
        updateS60CaptureExternalBrightMeasurementListPref(_enable);
    }

    private void updateCustomSegmentAutoSetCustomWidthHeight(int captureMode){

        int _widthVal = -1 ;
        int _heightVal = -1 ;

        switch (captureMode) {
            case 2: //RS_CAPTURE_FLAT_SINGLE_FINGER:
            case 6: //RS_CAPTURE_ROLL_FINGER:
                _widthVal = 800;
                _heightVal = 750;
                break;
            case 4: //RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS:
            case 5: //RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS:
            case 18: //RS_CAPTURE_FLAT_SINGLE_FINGER_EX:
            case 19: //RS_CAPTURE_FLAT_TWO_FINGERS_EX:
                _widthVal = 1600;
                _heightVal = 1500;
                break;
            case 3: //RS_CAPTURE_FLAT_TWO_FINGERS:
            case 7: //RS_CAPTURE_FLAT_TWO_THUMB:
                _widthVal = 900;
                _heightVal = 900;
                break;
            default:
                _widthVal = 0;
                _heightVal = 0;
                break;
        }

        mDataStorage.setCustomSegSizeWidth(Integer.valueOf(_widthVal));
        mCustomSegmentSizeWidth = mDataStorage.getCustomSegSizeWidth();
        mDeviceDataHandler.setCustomSegmentSizeWidth(mDataStorage.getCustomSegSizeWidth());
        mCustomSegSizeWidthPref.setSummary(String.valueOf(_widthVal));

        mDataStorage.setCustomSegSizeHeight(Integer.valueOf(_heightVal));
        mCustomSegmentSizeHeight = mDataStorage.getCustomSegSizeHeight();
        mDeviceDataHandler.setCustomSegmentSizeHeight(mDataStorage.getCustomSegSizeHeight());
        mCustomSegSizeHeightPref.setSummary(String.valueOf(_heightVal));
    }

    private int getWidthLimitByCaptureMode(int captureMode) {
        switch (captureMode) {
            case 2: // RS_CAPTURE_FLAT_SINGLE_FINGER
            case 6: // RS_CAPTURE_ROLL_FINGER
                return 800;
            case 4: // RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS
            case 5: // RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS
            case 18: // RS_CAPTURE_FLAT_SINGLE_FINGER_EX
            case 19: // RS_CAPTURE_FLAT_TWO_FINGERS_EX
                return 1600;
            case 3: // RS_CAPTURE_FLAT_TWO_FINGERS
            case 7: // RS_CAPTURE_FLAT_TWO_THUMB
                return 900;
            default:
                return 1600;
        }
    }

    private int getHeightLimitByCaptureMode(int captureMode) {
        switch (captureMode) {
            case 2: // RS_CAPTURE_FLAT_SINGLE_FINGER
            case 6: // RS_CAPTURE_ROLL_FINGER
                return 750;
            case 4: // RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS
            case 5: // RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS
            case 18: // RS_CAPTURE_FLAT_SINGLE_FINGER_EX
            case 19: // RS_CAPTURE_FLAT_TWO_FINGERS_EX
                return 1500;
            case 3: // RS_CAPTURE_FLAT_TWO_FINGERS
            case 7: // RS_CAPTURE_FLAT_TWO_THUMB
                return 900;
            default:
                return 1500;
        }
    }

    private void updateS60CaptureExternalBrightMeasurementListPref(boolean _enable) {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_S60) {
            mS60CaptureBrightMeasureListPref.setVisible(false);
            mS60CaptureBrightMeasureListPref.setEnabled(false);
            return;
        }else{
            mS60CaptureBrightMeasureListPref.setVisible(_enable);
            mS60CaptureBrightMeasureListPref.setEnabled(_enable);
        }
        if(_enable){
            int s60CaptureExternalBrightMeasureValue = mDataStorage.getS60CaptureExternalBrightMeasureModeValue();
            mDeviceDataHandler.setS60CaptureExternalBrightMeasureMode(s60CaptureExternalBrightMeasureValue);
            mS60CaptureBrightMeasureListPref.setSummary(mS60CaptureBrightMeasureListPref.getEntries()[s60CaptureExternalBrightMeasureValue]);
            mS60CaptureBrightMeasureListPref.setValueIndex(s60CaptureExternalBrightMeasureValue);
        }
    }

    public void updateSensitiveModePref(boolean _enable) {
        if(RealScanAPI.getInstance().getCurrentDevice().getDeviceType() != DeviceType.REALSCAN_S60) {
            mSensitiveModeListPref.setVisible(false);
            mSensitiveModeListPref.setEnabled(false);
        }else {
            mSensitiveModeListPref.setVisible(_enable);
            mSensitiveModeListPref.setEnabled(_enable);
        }
        if(_enable){
            int _sensitivelevel = mDataStorage.getSensitiveMode();
            Logger.d("_sensitivelevel: " + _sensitivelevel);
            mDeviceDataHandler.setSensitiveLevel(_sensitivelevel);
            mSensitiveModeListPref.setSummary(mSensitiveModeListPref.getEntries()[_sensitivelevel]);
            mSensitiveModeListPref.setValueIndex(_sensitivelevel);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Logger.d("START! : " + preference.getKey());
        switch (preference.getKey()) {
            case "pref_license":
                Logger.d("pref_license is clicked.");
                showLicenseInfo();
                break;
        }
        return true;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SCREEN_UPDATE:
                    Logger.d("EVENT_SCREEN_UPDATE");
                    pref();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onPause() {
//        Logger.d("START!");
        super.onPause();
        mIsRunning = false;
    }

    @Override
    public void onStop() {
//        Logger.d("START!");
        super.onStop();
        mIsRunning = false;
    }

    @Override
    public void onResume() {
        super.onResume();
//        Logger.d("START!");
    }

//    public void setArraysAsHandSelect(CaptureMode _capturemode)
//    {
//        if(_capturemode == CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS)
//        {
//            mFingerSelect.setEntries(R.array.left_fingerselect);
//            mFingerSelect.setEntryValues(R.array.left_fingerselect_value);
//        }
//        else if(_capturemode == CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS)
//        {
//            mFingerSelect.setEntries(R.array.right_fingerselect);
//            mFingerSelect.setEntryValues(R.array.right_fingerselect_value);
//        }
//        mSettingFragment.updateSettingMenu();
//    }
    public void sendMessageToAcitivityHandler(int what, Object arg1)
    {
        Message msg = new Message();
        msg.what = what;
        msg.obj = arg1;
        mActivity.mHandler.sendMessage(msg);
    }
    public void updateSettingMenu()
    {
        mHandler.sendEmptyMessage(EVENT_SCREEN_UPDATE);
    }
    public void resetPreference(boolean flag) {
        mImgFormatList.setEnabled(flag);
        mFingerSelect.setEnabled(flag);
        mFingerActivate.setEnabled(flag);
        mMinimumNumberOfFingerPref.setEnabled(flag);
        mCustomSegSizeActivatePref.setEnabled(flag);
        mCustomSegSizeWidthPref.setEnabled(flag);
        mCustomSegSizeHeightPref.setEnabled(flag);
        mCheckCustomerIdValuePref.setEnabled(flag);
        mCheckCustomerIdPref.setEnabled(flag);
        mDirtyCheckPref.setEnabled(flag);
        mDirtyCheckLevelPref.setEnabled(flag);
        mNfiqModePref.setEnabled(flag);
        mNfiqScorePref.setEnabled(flag);
        mImgFormatList.setEnabled(flag);
        mCaptureTimeoutPref.setEnabled(flag);
        mRollDirectionPref.setEnabled(flag);
        mSegmentationPref.setEnabled(flag);
        mSequenceCheckCaptureModePref.setEnabled(flag);
        mLfdModePref.setEnabled(flag);
        mEnhancedPreviewModePref.setEnabled(flag);
        mHaloReductionEnhandedModePref.setEnabled(flag);
        mCaptureAreaExpandModePref.setEnabled(flag);
        mLedSettingCustomeEnableModePref.setEnabled(flag);
        modeLEDPreference.setEnabled(flag);
        statusLEDPreference.setEnabled(flag);



       mS60ExternalBrightEnableModePref.setEnabled(flag);
       mS60CaptureBrightMeasureListPref.setEnabled(flag);
       mSensitiveModeListPref.setEnabled(flag);
    }

    public void setCustomerIdPref(boolean _flag)
    {
        mCheckCustomerIdPref.setEnabled(_flag);
    }

    public void updateNfiqScore(boolean needClear) {
        if (needClear) {
            mNfiqScorePref.setSummary("");
            return;
        }
    }

    public void updateNfiqScore(boolean needClear, int nErrorCode, int fingerCnt) {
        if (needClear) {
            mNfiqScorePref.setSummary("");
            return;
        }

        if (mNfiqScorePref == null || mDeviceDataHandler == null) {
            return;
        }

        Set<String> missingFingers = mDeviceDataHandler.getFingerSelectSet();
        boolean isMissingFingerEnabled = mDeviceDataHandler.isMissingFingerActivate();

        int[] fingerCodes;
        int segmentCount;

        if (nErrorCode
            != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode()) {
            switch (mDeviceDataHandler.getCaptureMode()) {
                case RS_CAPTURE_FLAT_SINGLE_FINGER_EX:
                case RS_CAPTURE_FLAT_SINGLE_FINGER:
                case RS_CAPTURE_ROLL_FINGER:
                    fingerCodes = new int[]{1};  // Thumb or one finger
                    segmentCount = 1;
                    break;

                case RS_CAPTURE_FLAT_TWO_FINGERS_EX:
                case RS_CAPTURE_FLAT_TWO_FINGERS:
                case RS_CAPTURE_FLAT_TWO_THUMB:
                    fingerCodes = new int[]{6, 1};  // Two thumbs
                    break;

                case RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS:
                    fingerCodes = new int[]{10, 9, 8, 7};  // Left hand
                    break;

                case RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS:
                    fingerCodes = new int[]{2, 3, 4, 5};  // Right hand
                    break;

                default:
                    fingerCodes = new int[fingerCnt];
                    break;
            }

            StringBuilder summaryBuilder = new StringBuilder();

            for (int viewIndex = 0, segmentIndex = 1; viewIndex < fingerCodes.length; viewIndex++) {
                int fingerCode = fingerCodes[viewIndex];

                if (isMissingFingerEnabled && missingFingers.contains(String.valueOf(fingerCode))) {
                    continue;
                }

                String segmentText ="segment" + (viewIndex + 1) + " : ";
                switch (viewIndex) {
                    case 0:
                        segmentText += mDeviceDataHandler.getNfiqScoreSegment1();
                        break;
                    case 1:
                        segmentText += mDeviceDataHandler.getNfiqScoreSegment2();
                        break;
                    case 2:
                        segmentText += mDeviceDataHandler.getNfiqScoreSegment3();
                        break;
                    case 3:
                        segmentText += mDeviceDataHandler.getNfiqScoreSegment4();
                        break;
                    default:
                        segmentText = "";
                        break;
                }

                if (!segmentText.isEmpty()) {
                    summaryBuilder.append(segmentText).append("\n");
                }

                segmentIndex++;
            }

            int length = summaryBuilder.length();
            if (length > 0 && summaryBuilder.charAt(length - 1) == '\n') {
                summaryBuilder.deleteCharAt(length - 1);
            }
            mNfiqScorePref.setSummary(summaryBuilder.toString());
        } else {
            switch (mDeviceDataHandler.getCaptureMode())
            {
                case RS_CAPTURE_FLAT_SINGLE_FINGER_EX:
                case RS_CAPTURE_FLAT_SINGLE_FINGER:
                case RS_CAPTURE_ROLL_FINGER:
                    mNfiqScorePref.setSummary("segment1 : " + mDeviceDataHandler.getNfiqScoreSegment1());
                    break;
                case RS_CAPTURE_FLAT_TWO_FINGERS_EX:
                case RS_CAPTURE_FLAT_TWO_FINGERS:
                case RS_CAPTURE_FLAT_TWO_THUMB:
                    mNfiqScorePref.setSummary("segment1 : " + mDeviceDataHandler.getNfiqScoreSegment1() + "\nsegment2 : " + mDeviceDataHandler.getNfiqScoreSegment2());
                    break;
                case RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS:
                case RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS:
                    mNfiqScorePref.setSummary("segment1 : " + mDeviceDataHandler.getNfiqScoreSegment1() + "\nsegment2 : " + mDeviceDataHandler.getNfiqScoreSegment2()
                        +"\nsegment3 : " + mDeviceDataHandler.getNfiqScoreSegment3() + "\nsegment4 : " + mDeviceDataHandler.getNfiqScoreSegment4());
                    break;
            }
        }
    }
    public void updateLfdScore(int nFingerCnt, int nErrorCode) {
        int score1 = -1;
        int score2 = -1;
        int score3 = -1;
        int score4 = -1;

        String result1 = "";
        String result2 = "";
        String result3 = "";
        String result4 = "";

        String lfdForSegment1 = "";
        String lfdForSegment2 = "";
        String lfdForSegment3 = "";
        String lfdForSegment4 = "";
        if(mDeviceDataHandler.getLfdScore() != null) {
            Set<String> missingFingers = mDeviceDataHandler.getFingerSelectSet();
            List<Integer> lfdScores = mDeviceDataHandler.getLfdScore();
            List<Integer> lfdResults = mDeviceDataHandler.getLfdResult();

            int[] fingerCodes = new int[0];

            if (nErrorCode
                != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode()) {
                if (mDeviceDataHandler.getCaptureMode()
                    == CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS) {
                    fingerCodes = new int[]{2, 3, 4, 5}; // 오른손 4지
                } else if (mDeviceDataHandler.getCaptureMode()
                    == CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS) {
                    fingerCodes = new int[]{10, 9, 8, 7}; // 왼손 4지
                } else if (mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_TWO_THUMB) {
                    fingerCodes = new int[]{6, 1}; // two thumb
                } else {
                    fingerCodes = new int[nFingerCnt]; // 처리 안 함
                }
                int lfdIndex = 0;

                for (int viewIndex = 0; viewIndex < fingerCodes.length; viewIndex++) {
                    int fingerCode = fingerCodes[viewIndex];

                    if (mDeviceDataHandler.isMissingFingerActivate() && missingFingers.contains(
                        String.valueOf(fingerCode))) {
                        continue; // 빠진 손가락은 건너뜀
                    }

                    if (lfdIndex >= lfdScores.size()) {
                        break;
                    }

                    int score = lfdScores.get(lfdIndex);
                    int result = lfdResults.get(lfdIndex);
                    String resultStr = (result == 0) ? "RS_LFD_LIVE" : "RS_LFD_FAKE";
                    String segmentText = (score == -1)
                        ? "segment" + (viewIndex + 1) + " : NOT DETECTED"
                        : "segment" + (viewIndex + 1) + " : " + score + ", Result : " + resultStr;

                    switch (viewIndex) {
                        case 0:
                            lfdForSegment1 = segmentText;
                            break;
                        case 1:
                            lfdForSegment2 = segmentText;
                            break;
                        case 2:
                            lfdForSegment3 = segmentText;
                            break;
                        case 3:
                            lfdForSegment4 = segmentText;
                            break;
                        default:
                            break;
                    }

                    lfdIndex++;
                }

                StringBuilder summaryBuilder = new StringBuilder();

                if (lfdForSegment1 != null && !lfdForSegment1.isEmpty()) {
                    summaryBuilder.append(lfdForSegment1).append("\n");
                }
                if (lfdForSegment2 != null && !lfdForSegment2.isEmpty()) {
                    summaryBuilder.append(lfdForSegment2).append("\n");
                }
                if (lfdForSegment3 != null && !lfdForSegment3.isEmpty()) {
                    summaryBuilder.append(lfdForSegment3).append("\n");
                }
                if (lfdForSegment4 != null && !lfdForSegment4.isEmpty()) {
                    summaryBuilder.append(lfdForSegment4).append("\n");
                }


                if (summaryBuilder.length() > 0
                    && summaryBuilder.charAt(summaryBuilder.length() - 1) == '\n') {
                    summaryBuilder.setLength(summaryBuilder.length() - 1);
                }

                mLfdScorePref.setSummary(summaryBuilder.toString());
            } else {
                for (int i = 0; i < nFingerCnt; i++) {
                    if (i == 0) {
                        score1 = mDeviceDataHandler.getLfdScore().get(0);
                        result1 = score1 == -1 ? ""
                            : (mDeviceDataHandler.getLfdResult().get(0) == 0 ? "RS_LFD_LIVE"
                                : "RS_LFD_FAKE");
                        lfdForSegment1 = score1 == -1 ? "segment1 : NOT DETECTED"
                            : "segment1 : " + score1 + ", Result : " + result1;
                    }
                    if (i == 1) {
                        score2 = mDeviceDataHandler.getLfdScore().get(1);
                        result2 = score2 == -1 ? ""
                            : (mDeviceDataHandler.getLfdResult().get(1) == 0 ? "RS_LFD_LIVE"
                                : "RS_LFD_FAKE");
                        lfdForSegment2 = score2 == -1 ? "segment2 : NOT DETECTED"
                            : "segment2 : " + score2 + ", Result : " + result2;
                    }
                    if (i == 2) {
                        score3 = mDeviceDataHandler.getLfdScore().get(2);
                        result3 = score3 == -1 ? ""
                            : (mDeviceDataHandler.getLfdResult().get(2) == 0 ? "RS_LFD_LIVE"
                                : "RS_LFD_FAKE");
                        lfdForSegment3 = score3 == -1 ? "segment3 : NOT DETECTED"
                            : "segment3 : " + score3 + ", Result : " + result3;
                    }
                    if (i == 3) {
                        score4 = mDeviceDataHandler.getLfdScore().get(3);
                        result4 = score4 == -1 ? ""
                            : (mDeviceDataHandler.getLfdResult().get(3) == 0 ? "RS_LFD_LIVE"
                                : "RS_LFD_FAKE");
                        lfdForSegment4 = score4 == -1 ? "segment4 : NOT DETECTED"
                            : "segment4 : " + score4 + ", Result : " + result4;
                    }
                }
                StringBuilder summaryBuilder = new StringBuilder();

                if (lfdForSegment1 != null && !lfdForSegment1.isEmpty()) {
                    summaryBuilder.append(lfdForSegment1).append("\n");
                }
                if (lfdForSegment2 != null && !lfdForSegment2.isEmpty()) {
                    summaryBuilder.append(lfdForSegment2).append("\n");
                }
                if (lfdForSegment3 != null && !lfdForSegment3.isEmpty()) {
                    summaryBuilder.append(lfdForSegment3).append("\n");
                }
                if (lfdForSegment4 != null && !lfdForSegment4.isEmpty()) {
                    summaryBuilder.append(lfdForSegment4).append("\n");
                }

                int length = summaryBuilder.length();
                if (length > 0 && summaryBuilder.charAt(length - 1) == '\n') {
                    summaryBuilder.deleteCharAt(length - 1);
                }

                mLfdScorePref.setSummary(summaryBuilder.toString());
            }
        }
    }

    private int getIndexCaptureMode(int captureModeValue){
        String[] captureModeValues = mContext.getResources().getStringArray(R.array.custom_size_capture_mode_value);

        int index = -1;
        for (int i = 0; i < captureModeValues.length; i++) {
            if (Integer.parseInt(captureModeValues[i]) == captureModeValue) {
                index = i;
                break;
            }
        }
        return index;
    }

    private String getCaptureModeLabel(int captureModeValue) {
        String[] modeLabels = mContext.getResources().getStringArray(R.array.custom_size_capture_mode);
        String[] modeValues = mContext.getResources().getStringArray(R.array.custom_size_capture_mode_value);

        if (modeLabels.length != modeValues.length) {
            Logger.e("Mismatch in modeLabels and modeValues array length.");
            return "Invalid";
        }

        for (int i = 0; i < modeValues.length; i++) {
            if (Integer.parseInt(modeValues[i]) == captureModeValue) {
                return modeLabels[i];
            }
        }

        return "Unknown";
    }
    private String getPreviewQualityModeLabel(int captureModeValue) {
        String[] modeLabels = mContext.getResources().getStringArray(R.array.preview_qualitry_mode);
        String[] modeValues = mContext.getResources().getStringArray(R.array.preview_quality_value);

        if (modeLabels.length != modeValues.length) {
            Logger.e("Mismatch in modeLabels and modeValues array length.");
            return "Invalid";
        }

        for (int i = 0; i < modeValues.length; i++) {
            if (Integer.parseInt(modeValues[i]) == captureModeValue) {
                return modeLabels[i];
            }
        }

        return "Unknown";
    }

    private void showLicenseInfo() {
        Logger.start();
        try {
            mBuilder = new AlertDialog.Builder(mContext);

            mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Logger.d("setPositiveButton clicked");
                    dialog.dismiss();
                }
            });
            CharSequence cs = "[Copyright notice]\n\n"+
                    "The copyright of this document is vested in Xperix Inc.\n"+
                    "The rights of other product names, trademarks and registered\ntrademarks are vested in each individual or organization that owns such rights.\n\n"+
                    "[Open Source License]\n\n"+
                    "This product uses the libUSB1.0 library, which is licensed under LGPL 2.1.\n" +
                    "This product uses the NFIQ, which is licensed under NIST License. \n"+
                    "This product uses the BiomDI, which is licensed under NIST license.\n\n"+
                    "For more details, refer to chapter 3. Appendix of SW SDK Manual.";

            mBuilder.setMessage(cs);
            mBuilder.setCancelable(false);
            mBuilder.show();
        }catch (Exception e)
        {
            Logger.d("e: " +e.getMessage());
        }
    }
}
