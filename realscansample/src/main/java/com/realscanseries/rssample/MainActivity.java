package com.realscanseries.rssample;

import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.realscanseries.rsalib.BuildConfig;
import com.realscanseries.rsalib.DeviceManager;
import com.realscanseries.rsalib.RealScanAPI;
import com.realscanseries.rsalib.devices.RealScanDevice;
import com.realscanseries.rsalib.enums.CaptureData;
import com.realscanseries.rsalib.enums.CaptureMode;
import com.realscanseries.rsalib.enums.DeviceDataHandler;
import com.realscanseries.rsalib.enums.DeviceType;
import com.realscanseries.rsalib.enums.EnrollData;
import com.realscanseries.rsalib.enums.ErrorCode;
import com.realscanseries.rsalib.enums.LEDMode;
import com.realscanseries.rsalib.enums.MsgCode;
import com.realscanseries.rsalib.idp.IDPEnums;
import com.realscanseries.rsalib.interfaces.DataCallback;
import com.realscanseries.rsalib.interfaces.DeviceCallback;
import com.realscanseries.rsalib.struct.DeviceInfo;
import com.realscanseries.rsalib.struct.SlapInfo;
import com.realscanseries.rsalib.util.Logger;
import com.realscanseries.rsalib.util.Util;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BasicFuncFragment.OnFragmentInteractionListener,
        EnrollFragment.OnFragmentInteractionListener,SettingFragment.OnFragmentInteractionListener,View.OnClickListener,
        ModeLEDPreference.OnModeLedApplyButtonClickListener, StatusLEDPreference.OnStatusLedApplyButtonClickListener
{


    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerFragmentAdapter mAdapter;

    public static Context mContext = null;
    public PowerManager.WakeLock    mWakeLock = null;
    public boolean mIsEnrollment = false;
    public AlertDialog.Builder mBuilder = null;
    public Toolbar mToolbar = null;
    public DeviceInfo mCurrentDeviceInfo = null;

    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent= null;
    private boolean mIsCapturing = false;
    public boolean mCurrentNightMode = false;
    private CaptureData mFullCaptureData = null;
    private CaptureData mSegmentData = null;
    private Util mImageUtil = null;

    public static SettingFragment mSettingFragment = null;
    public static EnrollFragment  mEnrollFragment = null;
    public static BasicFuncFragment mBasicFuncFragment = null;
    public  static DataStorage mDataStorage;
    private boolean mIsCheckCustomerId = false;
    private HashMap<Integer,CaptureData> mSequenceCheckMap = new HashMap<>();
    private boolean mIsAutoCapture = false;

    private Bitmap fullBitmapData;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private static final int PERMISSION_ALL = 786;

    String[] PERMISSIONS = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    String[] PERMISSIONS_33 = new String[] {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO};

    //define Enrollment Function Event
    public static final int base = 1000;
    public static final int FLAT_ONE = base + 1;
    public static final int FLAT_TWO = base + 2;
    public static final int FLAT_FOUR_LEFT = base + 3;
    public static final int FLAT_FOUR_RIGHT = base + 4;
    public static final int FLAT_TWO_THUMB = base + 5;

    public static int currenViewPosition = 0;

    private static final int BASE_EVENT = 3000;
    private static final int ACTIVATE_USB_DEVICE = BASE_EVENT + 1;
    private static final int REMOVE_USB_DEVICE = BASE_EVENT + 2;
    private static final int UPDATE_DEVICE_INFO = BASE_EVENT + 3;
    private static final int REQUEST_USB_PERMISSION = BASE_EVENT+4;
    private static final int MAKE_DELAY_1SEC = BASE_EVENT+5;
    private static final int ADD_DEVICE = BASE_EVENT+6;
    public static final int UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE = BASE_EVENT+7;
    public static final int UPDATE_ERROR_TEXTVIEW_TO_DEFAULT = BASE_EVENT+8;
    public static final int UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_LEFT = BASE_EVENT+9;
    public static final int UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_RIGHT = BASE_EVENT+10;
    public static final int UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_THUMB = BASE_EVENT+11;
    public static final int ABORT_CAPTURE_BY_ERROR = BASE_EVENT+12;
    public static final int FINISH_CAPTURE_PROCESS = BASE_EVENT+13;
    private static final int SHOW_CAPTURE_IMAGE_TO_UI = BASE_EVENT+15;
    private static final int VIEW_INIT_BY_ENROLL_CANEL = BASE_EVENT+16;

    //error event
    private static final int BASE_ERROR_EVENT = 4000;
    private static final int CUSTOMER_ID_IS_NOT_CONFIRMED = BASE_ERROR_EVENT + 1;
    private static final int CUSTOMER_ID_IS_NOT_MATCHED = BASE_ERROR_EVENT + 2;
    private static final int DEVICE_IS_NOT_CONNECTED = BASE_ERROR_EVENT + 3;
    private static final int DEVICE_INIT_FAILED = BASE_ERROR_EVENT + 4;
    private static final int DEVICE_DATAHANDLER_OBJ_IS_NULL = BASE_ERROR_EVENT + 5;

    /**
     * Image save directory.
     */
    private String mDeviceName;
    private String mSdkVersionInfo;
    private String mProductDate;
    private int mPreviewCount = 0;
    /**
     * BasicFunction Widgets
     */
    TextView _tvDevice;
    TextView _tvSdkInfo;
    TextView _tvSdkInfoEnroll;
    TextView _tvMode;
    TextView _tvModeEnroll;

    TextView _tv_leftpinky;
    TextView _tv_leftringer;
    TextView _tv_leftmiddle;
    TextView _tv_leftindex;
    TextView _tv_leftthumbroll;

    TextView _tv_rightthumbroll;
    TextView _tv_rightindex;
    TextView _tv_rightmiddle;
    TextView _tv_rightringer;
    TextView _tv_rightpinky;

    TextView _tv_leftfour;
    TextView _tv_leftthumbseg;
    TextView _tv_rightthumbseg;
    TextView _tv_rightfour;

    Button _btSelectDevice;
    Button _btSelectMode;
    Button _btSaveImage;
    Button _btStartPreview;
    Button _btManualCapture;
    Button _btAutoCapture;
    Button _btAbortCapture;
    ImageView _ivCapture;
    ImageView _ivSeg1;
    ImageView _ivSeg2;
    ImageView _ivSeg3;
    ImageView _ivSeg4;
    TextView _tvSeg1;
    TextView _tvSeg2;
    TextView _tvSeg3;
    TextView _tvSeg4;

    TextView seg1_Info_result;
    TextView seg2_Info_result;
    TextView seg3_Info_result;
    TextView seg4_Info_result;
    /**
     * Enrollment Widgets
     */
    ImageView enrollfullimage;
    Button _btSelectDeviceEnroll;
    TextView _tvDeviceEnroll;

    ImageView _ivLeftSeg1;
    ImageView _ivLeftSeg2;
    ImageView _ivLeftSeg3;
    ImageView _ivLeftSeg4;
    ImageView _ivLeftThumbRoll;

    ImageView _ivRightSeg1;
    ImageView _ivRightSeg2;
    ImageView _ivRightSeg3;
    ImageView _ivRightSeg4;
    ImageView _ivRightThumbRoll;

    ImageView _ivThumbLeftSeg;
    ImageView _ivThumbRightSeg;
    ImageView _ivLeftFour;
    ImageView _ivRightFour;

    ImageView mPreviewDialogImgView;

    Button enrollstartbutton;
    Button _btAbortEnroll;

    Dialog mPreviewDialog;
    /**
     * RealScan SDK main interface
     */
    RealScanAPI _rsApi;

    /**
     * Callback to receive notifications such as captured images after capture is finished.
     */
    long mStartTime = 0;
    long mElapsedTime = 0;

    DataCallback _dataCallback = new DataCallback() {
        @Override
        public void onPreviewDataReceived(int errorCode, byte[] previewImage, int imgWidth, int imgHeight) {
            mStartTime = SystemClock.currentThreadTimeMillis();
            if(mPreviewCount == 0)
            {
                if(mDeviceDataHandler.getEnhancedPreviewMode())
                    makePreviewDialog();
            }
            mPreviewCount++;
            if (_rsApi != null) {
                Logger.d("currenViewPosition : " + currenViewPosition + " imgWidth: " +imgWidth+" imgHeight: " + imgHeight);

                Message msg = new Message();
                msg.what = SHOW_CAPTURE_IMAGE_TO_UI;
                msg.obj = previewImage;
                msg.arg1 = imgWidth;
                msg.arg2 = imgHeight;
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onCaptureDataReceived(int errorCode, byte[] captureImage, int imgWidth, int imgHeight) {
            mStartTime = SystemClock.currentThreadTimeMillis();
            if(mPreviewDialog != null)
                mPreviewDialog.dismiss();
            if (_rsApi != null) {
                Logger.i();
                Logger.d("onCaptureDataReceived! : " + errorCode);
                Logger.d("currenViewPosition : " + currenViewPosition + " imgWidth : " + imgWidth + " imgheight : " + imgHeight);
                mFullCaptureData = new CaptureData(captureImage,  imgWidth, imgHeight);
                mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_FULL.value(),mFullCaptureData);

                if(currenViewPosition == 0) {
                    if(mDeviceDataHandler.getSegmentationActivate()){
                        fullBitmapData = getBitmapFromByte(captureImage,imgWidth,imgHeight);
                    }else{
                        showBitmapToImageView(_ivCapture,captureImage,imgWidth,imgHeight);
                    }
                } else if(currenViewPosition == 1) {
                    showBitmapToImageView(enrollfullimage,captureImage, imgWidth, imgHeight);
                    if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS)
                    {
                        showBitmapToImageView(_ivLeftFour,captureImage,imgWidth,imgHeight);
                    }
                    if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS)
                    {
                        showBitmapToImageView(_ivRightFour,captureImage,imgWidth,imgHeight);
                    }
                }
            }
            mElapsedTime = SystemClock.currentThreadTimeMillis() - mStartTime;
            Toast.makeText(MainActivity.this, "Capture is complete", Toast.LENGTH_SHORT).show();
            Logger.d("mElapsedTime : " + mElapsedTime);
        }

        @Override
        public void onSegmentDataReceivedWithSlapInfo(int errorCode, int nFingerCnt, int nFingerIndex, int nFingerType, byte[] segmentImage,
                                                      int imgWidth, int imgHeight, CaptureMode captureMode, SlapInfo[] slapInfo) {
            if(slapInfo != null){
                Logger.d("slapInfo length : " + slapInfo.length);
                for(int i =0 ; i < slapInfo.length ; i++){
                    Logger.d("slapInfo" + "[" + i + "]" + slapInfo[i].toString());
                }
                if(currenViewPosition == 0) {
                    if (nFingerCnt == nFingerIndex) {
                        drawRectSlapInfo(slapInfo, nFingerCnt);
                    }
                }
            }
            onSegmentDataReceived(errorCode, nFingerCnt, nFingerIndex, nFingerType, segmentImage,
                imgWidth, imgHeight, captureMode);
        }

        private float calculateDistance(int x1, int y1, int x2, int y2) {
            float dx = x2 - x1;
            float dy = y2 - y1;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        private void drawRectSlapInfo(SlapInfo[] slapInfo, int fingerTotalCnt){
            if(fullBitmapData != null){
                Canvas canvas = new Canvas(fullBitmapData);
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#1B3574"));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);

                float textSizeInDp = 20;
                float scale = getResources().getDisplayMetrics().density;
                float textSizeInPixels = textSizeInDp * scale;

                for(int i =0 ; i < /*slapInfo.length*/fingerTotalCnt ; i++){
                    int x0 = slapInfo[i].fingerPosition_x0, y0 = slapInfo[i].fingerPosition_y0;
                    int x1 = slapInfo[i].fingerPosition_x1, y1 = slapInfo[i].fingerPosition_y1;
                    int x2 = slapInfo[i].fingerPosition_x3, y2 = slapInfo[i].fingerPosition_y3;
                    int x3 = slapInfo[i].fingerPosition_x2, y3 = slapInfo[i].fingerPosition_y2;

                    Path path = new Path();
                    path.moveTo(x0, y0);
                    path.lineTo(x1, y1);
                    path.lineTo(x2, y2);
                    path.lineTo(x3, y3);
                    path.lineTo(x0, y0);

                    float centerX01 = (x0 + x1) / 2.0f;
                    float centerY01 = (y0 + y1) / 2.0f;
                    float centerX12 = (x1 + x2) / 2.0f;
                    float centerY12 = (y1 + y2) / 2.0f;
                    float centerX23 = (x2 + x3) / 2.0f;
                    float centerY23 = (y2 + y3) / 2.0f;
                    float centerX30 = (x3 + x0) / 2.0f;
                    float centerY30 = (y3 + y0) / 2.0f;

                    float length01 = calculateDistance(x0, y0, x1, y1);
                    float length12 = calculateDistance(x1, y1, x2, y2);
                    float length23 = calculateDistance(x2, y2, x3, y3);
                    float length30 = calculateDistance(x3, y3, x0, y0);
                    Logger.d("x0 : " + x0 + ", y0 : " + y0 + ", x1 : " + x1 + ", y1 : " + y1);
                    Logger.d("x1 : " + x1 + ", y1 : " + y1 + ", x2 : " + x2 + ", y2 : " + y2);
                    Logger.d("x2 : " + x2 + ", y2 : " + y2 + ", x3 : " + x3 + ", y3 : " + y3);
                    Logger.d("x3 : " + x3 + ", y3 : " + y3 + ", x0 : " + x0 + ", y0 : " + y0);
                    Logger.d("length01 : " + length01 + ", length12 : " + length12 + ", length23 : " + length23 + ", length30 : " + length30);

                    paint.setTextAlign(Paint.Align.CENTER);

                    float textX01 = centerX01;
                    float textY01 = centerY01 - textSizeInPixels / 2;
                    float textX12 = centerX12;
                    float textY12 = centerY12 + textSizeInPixels / 2;
                    float textX23 = centerX23;
                    float textY23 = centerY23 + textSizeInPixels / 2;
                    float textX30 = centerX30;
                    float textY30 = centerY30 - textSizeInPixels / 2;

                    if (textY01 - textSizeInPixels / 2 < 0) {
                        textY01 = textSizeInPixels / 2;
                    }
                    if (textY12 + textSizeInPixels / 2 > fullBitmapData.getHeight()) {
                        textY12 = fullBitmapData.getHeight() - textSizeInPixels / 2;
                    }
                    if (textY23 + textSizeInPixels / 2 > fullBitmapData.getHeight()) {
                        textY23 = fullBitmapData.getHeight() - textSizeInPixels / 2;
                    }
                    if (textY30 - textSizeInPixels / 2 < 0) {
                        textY30 = textSizeInPixels / 2;
                    }

                    paint.setTextSize(textSizeInPixels);

                    canvas.drawText(String.format("%.1f", length01), textX01, textY01, paint);
                    //canvas.drawText(String.format("%.1f", length12), textX12, textY12, paint);
                    //canvas.drawText(String.format("%.1f", length23), textX23, textY23, paint);
                    canvas.drawText(String.format("%.1f", length30), textX30, textY30, paint);

                    canvas.drawPath(path, paint);
                }
                Logger.d("drawRectSlapInfo, _ivCapture.setImageBitmap");
                //_ivCapture.setImageBitmap(fullBitmapData);
                showBitmapToImageView(_ivCapture, fullBitmapData);
            }
        }

        @Override
        public void onNotificationMsg(int id, String msg) {
            if(id == MsgCode.MSG_ID_S60_EXT_EXTBRIGHT_INFO.getMsgCode()) {
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            }else if(id == MsgCode.MSG_ID_SAVE_IMG_FILENAME_PATH.getMsgCode()) {
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onSegmentDataReceived(int errorCode, int nFingerCnt, int nFingerIndex, int nFingerType, byte[] segmentImage,
                                          int imgWidth, int imgHeight, CaptureMode captureMode) {

            if (_rsApi == null) {
                Logger.w("RS API is null, skipping segment handling.");
                return;
            }

            if (_rsApi != null) {
                Logger.i();
                Logger.d("capture mode name: " + captureMode.getCaptureDesc()  + " errorCode: "+errorCode);
                Logger.d("nFingerCnt : "+nFingerCnt + " nFingerIndex : " + nFingerIndex +" segmentImage length : " + segmentImage.length+ " nFingerType : " + nFingerType + " imgWidth : " + imgWidth + " imgHeight : " + imgHeight);
                if(!mDeviceDataHandler.isSequenceCheck()) {
                    byte[] templateData = _rsApi.getExtractfromFingerprint(segmentImage, imgWidth, imgHeight);
                    mSegmentData = convertAndScaleToCaptureData(segmentImage, templateData, imgWidth, imgHeight);
                }
                if(mDeviceDataHandler.isEnrollment() == false)
                {
                    Logger.d("mIsEnrollment is false!!  : " + nFingerIndex);

                    if(mDeviceDataHandler.isSequenceCheck())
                    {
                        if(errorCode == ErrorCode.OK.getErrorCode()|| errorCode ==ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode())
                        {
                            //do sequence check with enroll image
                            Logger.d("current sequnce check finger : " + DeviceDataHandler.FingerType.Companion.fromInt(mDeviceDataHandler.getCurrentSeqCheckFinger()));
                            Logger.d("getEnrollmentImageMap().size = " + mDeviceDataHandler.getEnrollmentImageMap().size());
                            Logger.d("mSequenceCheckMap.size = " + mSequenceCheckMap.size());
                            ArrayList<Integer> sequenceCheckSuccList = new ArrayList<>();

                            int count = mDeviceDataHandler.getEnrollmentImageMap().size();
                            byte[][] pTemplates = new byte[count][];
                            int[] pTemplatesLength = new int[count];
                            int[] output = new int[1];
                            int index = 0;
                            for (Map.Entry<Integer, CaptureData> entry : mDeviceDataHandler.getEnrollmentImageMap().entrySet()) {
                                CaptureData data = entry.getValue();

                                if (data.getTemplateData() != null) {
                                    pTemplates[index] = data.getTemplateData();
                                    pTemplatesLength[index] = data.getTemplateData().length;
                                    index++;
                                } else {
                                    Logger.d("Template is null for fingerCode: " + entry.getKey());
                                }
                            }

                            byte[] templateData = _rsApi.getExtractfromFingerprint(segmentImage, imgWidth, imgHeight);

                            if(templateData != null && _rsApi.identify(templateData, templateData.length,
                                pTemplates, pTemplatesLength, pTemplates.length, 5000, output)){
                                sequenceCheckSuccList.add(output[0]+1);
                                Logger.d("identify Success output index = " + (output[0]+1));

                            }

                            Logger.d("sequenceCheckSuccList size = " + sequenceCheckSuccList.size());
                            for(int i = 0 ; i < sequenceCheckSuccList.size(); i++)
                            {
                                Logger.d("sequenceCheckSuccList["+i+"] : " + sequenceCheckSuccList.get(i));
                            }
                            if(sequenceCheckSuccList.size() == 0 || sequenceCheckSuccList.size() > 1)
                            {

                                showSeqCheckConfirmDialog(true,"VERIFICATION_FAIL",mDeviceDataHandler.getCurrentSeqCheckFinger());
                            }
                            else
                            {
                                byte[] showImage = mDeviceDataHandler.getEnrollmentImageMap().get(sequenceCheckSuccList.get(0)).getCaptureImgData();
                                int width = mDeviceDataHandler.getEnrollmentImageMap().get(sequenceCheckSuccList.get(0)).getCaptureImgWidth();;
                                int height = mDeviceDataHandler.getEnrollmentImageMap().get(sequenceCheckSuccList.get(0)).getCaptureImgHeight();;
                                Bitmap confirmedBmp = mImageUtil.getBitmapFromRaw(showImage, width, height);
                                if(mSequenceCheckMap.containsKey(sequenceCheckSuccList.get(0)))
                                    mSequenceCheckMap.remove(sequenceCheckSuccList.get(0));
                                if(_rsApi.getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_D)
                                {
                                    if(mSequenceCheckMap.size() > 0)
                                        showSeqCheckConfirmDialog(false,"",sequenceCheckSuccList.get(0));
                                }
                                else
                                {
                                    setConfirmedEnrollImgToUI(sequenceCheckSuccList.get(0),confirmedBmp);
                                    mDeviceDataHandler.setSequenceCheckStarted(false);
                                }
                                if(mSequenceCheckMap.size() == 0)
                                {
                                    mDeviceDataHandler.setSequenceCheck(false);
                                    viewPager.setUserInputEnabled(true); // sequencecheck is done.
                                    Toast.makeText(mContext,"SequenceCheck is complete.",Toast.LENGTH_LONG).show();
                                    _btAbortEnroll.setEnabled(false);
                                    enrollstartbutton.setEnabled(true);
                                }
                            }
                        }
                        else
                        {
                            //show error popup
                        }

                    }
                    else
                    {
                        if(errorCode != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode()
                                && !( mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_ROLL_FINGER
                                ||mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_TWO_FINGERS
                                || mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER
                                || mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER_EX
                                || mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_TWO_FINGERS_EX))
                        {
                            mSegmentData = convertAndScaleToCaptureData(segmentImage,null, imgWidth,imgHeight);
                            mDeviceDataHandler.getSegmentImageMap().put(nFingerType,mSegmentData);

                            setTextViewStringFromFingerType(nFingerType);
                            if (nFingerType == DeviceDataHandler.FingerType.RS_FGP_LEFT_LITTLE.value()||
                                    nFingerType == DeviceDataHandler.FingerType.RS_FGP_RIGHT_INDEX.value() ||
                                    nFingerType == DeviceDataHandler.FingerType.RS_FGP_LEFT_THUMB.value())
                            {
                                showBitmapToImageView(_ivSeg1,segmentImage,imgWidth,imgHeight);

                                //process nfiq score
                                if(mDeviceDataHandler.getNfiqmode())
                                    mDeviceDataHandler.setNfiqScoreSegment1(_rsApi.getFPQuality(segmentImage,imgWidth,imgHeight, DeviceDataHandler.FpQualityMode.NQS_MODE_NFIQ.value()));
                            }
                            else if (nFingerType == DeviceDataHandler.FingerType.RS_FGP_LEFT_RING.value()||
                                    nFingerType == DeviceDataHandler.FingerType.RS_FGP_RIGHT_MIDDLE.value()||
                                    nFingerType == DeviceDataHandler.FingerType.RS_FGP_RIGHT_THUMB.value())
                            {
                                showBitmapToImageView(_ivSeg2,segmentImage,imgWidth,imgHeight);
                                //process nfiq score
                                if(mDeviceDataHandler.getNfiqmode())
                                    mDeviceDataHandler.setNfiqScoreSegment2(_rsApi.getFPQuality(segmentImage,imgWidth,imgHeight, DeviceDataHandler.FpQualityMode.NQS_MODE_NFIQ.value()));
                            }
                            else if (nFingerType == DeviceDataHandler.FingerType.RS_FGP_LEFT_MIDDLE.value()||
                                    nFingerType == DeviceDataHandler.FingerType.RS_FGP_RIGHT_RING.value())
                            {
                                showBitmapToImageView(_ivSeg3,segmentImage,imgWidth,imgHeight);
                                //process nfiq score
                                if(mDeviceDataHandler.getNfiqmode())
                                    mDeviceDataHandler.setNfiqScoreSegment3(_rsApi.getFPQuality(segmentImage,imgWidth,imgHeight, DeviceDataHandler.FpQualityMode.NQS_MODE_NFIQ.value()));
                            }
                            else if (nFingerType == DeviceDataHandler.FingerType.RS_FGP_LEFT_INDEX.value()||
                                    nFingerType == DeviceDataHandler.FingerType.RS_FGP_RIGHT_LITTLE.value())
                            {
                                showBitmapToImageView(_ivSeg4,segmentImage,imgWidth,imgHeight);
                                //process nfiq score
                                if(mDeviceDataHandler.getNfiqmode())
                                    mDeviceDataHandler.setNfiqScoreSegment4(_rsApi.getFPQuality(segmentImage,imgWidth,imgHeight, DeviceDataHandler.FpQualityMode.NQS_MODE_NFIQ.value()));
                            }
                        }
                        else
                        {
                            mHandler.sendEmptyMessage(UPDATE_ERROR_TEXTVIEW_TO_DEFAULT);
                            if (nFingerIndex == 1)
                            {
                                Logger.d("start display index");
                                showBitmapToImageView(_ivSeg1,segmentImage,imgWidth,imgHeight);
                                if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS) {
                                    mDeviceDataHandler.getSegmentImageMap().put(
                                        DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_SEGMENT1.value(),
                                        mSegmentData);
                                }
                                else if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS) {
                                    mDeviceDataHandler.getSegmentImageMap().put(
                                        DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_SEGMENT1.value(),
                                        mSegmentData);
                                }
                                else if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_TWO_THUMB) {
                                    mDeviceDataHandler.getSegmentImageMap().put(
                                        DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_THUMB.value(),
                                        mSegmentData);
                                }
                                else {
                                    mDeviceDataHandler.getSegmentImageMap().put(
                                        DeviceDataHandler.FingerType.RS_UNKNOWN_SEGMENT1.value(),
                                        mSegmentData);
                                }

                                //process nfiq score
                                if(mDeviceDataHandler.getNfiqmode())
                                    mDeviceDataHandler.setNfiqScoreSegment1(_rsApi.getFPQuality(segmentImage,imgWidth,imgHeight, DeviceDataHandler.FpQualityMode.NQS_MODE_NFIQ.value()));
                            }
                            else if (nFingerIndex == 2)
                            {
                                Logger.d("start display middle");
                                showBitmapToImageView(_ivSeg2,segmentImage,imgWidth,imgHeight);
                                if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS)
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_SEGMENT2.value(),mSegmentData);
                                else if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS)
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_SEGMENT2.value(),mSegmentData);
                                else if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_TWO_THUMB)
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_THUMB.value(),mSegmentData);
                                else
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_SEGMENT2.value(),mSegmentData);
                                //process nfiq score
                                if(mDeviceDataHandler.getNfiqmode())
                                    mDeviceDataHandler.setNfiqScoreSegment2(_rsApi.getFPQuality(segmentImage,imgWidth,imgHeight, DeviceDataHandler.FpQualityMode.NQS_MODE_NFIQ.value()));
                            }
                            else if (nFingerIndex == 3)
                            {
                                Logger.d("start display ringger");
//                                _ivSeg3.setImageBitmap(bitmap);
                                showBitmapToImageView(_ivSeg3,segmentImage,imgWidth,imgHeight);
                                if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS)
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_SEGMENT3.value(),mSegmentData);
                                else if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS)
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_SEGMENT3.value(),mSegmentData);
                                else
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_SEGMENT3.value(),mSegmentData);
                                //process nfiq score
                                if(mDeviceDataHandler.getNfiqmode())
                                    mDeviceDataHandler.setNfiqScoreSegment3(_rsApi.getFPQuality(segmentImage,imgWidth,imgHeight, DeviceDataHandler.FpQualityMode.NQS_MODE_NFIQ.value()));

                            }
                            else if (nFingerIndex == 4)
                            {
                                Logger.d("start display little");
                                showBitmapToImageView(_ivSeg4,segmentImage,imgWidth,imgHeight);
                                if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS)
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_SEGMENT4.value(),mSegmentData);
                                else if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS)
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_SEGMENT4.value(),mSegmentData);
                                else
                                    mDeviceDataHandler.getSegmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_SEGMENT4.value(),mSegmentData);
                                //process nfiq score
                                if(mDeviceDataHandler.getNfiqmode())
                                    mDeviceDataHandler.setNfiqScoreSegment4(_rsApi.getFPQuality(segmentImage,imgWidth,imgHeight, DeviceDataHandler.FpQualityMode.NQS_MODE_NFIQ.value()));
                            }
                        }
                    }

                }
                if(mDeviceDataHandler.isEnrollment() == true)
                {
                    switch (captureMode.getCaptureModeId())
                    {
                        case 7: //RS_CAPTURE_FLAT_TWO_THUMB
                            Logger.d("Enrollment : " + captureMode.getCaptureDesc());
                            mDeviceDataHandler.setLastEnrollSequence(true);
                            if(!mDeviceDataHandler.isMissingFingerActivate() && mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_TWO_THUMB
                                    && errorCode != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode())
                            {
                                Logger.d("deviceType : " + _rsApi.getCurrentDevice().getDeviceType());
                                mDeviceDataHandler.getEnrollmentImageMap().put(nFingerType,mSegmentData);
                                if(nFingerType == DeviceDataHandler.FingerType.RS_FGP_LEFT_THUMB.value())
                                {
                                    if(_rsApi.getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_D)
                                        showBitmapToImageView(_ivLeftThumbRoll,segmentImage,imgWidth,imgHeight);
                                    else
                                        showBitmapToImageView(_ivThumbLeftSeg,segmentImage,imgWidth,imgHeight);
                                }
                                if(nFingerType == DeviceDataHandler.FingerType.RS_FGP_RIGHT_THUMB.value())
                                {
                                    if(_rsApi.getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_D)
                                        showBitmapToImageView(_ivRightThumbRoll,segmentImage,imgWidth,imgHeight);
                                    else
                                        showBitmapToImageView(_ivThumbRightSeg,segmentImage,imgWidth,imgHeight);
                                }
                            }
                            else
                            {
                                Logger.d("errorCode : " + errorCode + " isMissingFingerActivate : " + mDeviceDataHandler.isMissingFingerActivate());
                                if(mDeviceDataHandler.isMissingFingerActivate() && errorCode != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode())
                                {
                                    mDeviceDataHandler.getEnrollmentImageMap().put(nFingerType,mSegmentData);
                                    if(nFingerType == DeviceDataHandler.FingerType.RS_FGP_LEFT_THUMB.value())
                                    {
                                        if(_rsApi.getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_D)
                                            showBitmapToImageView(_ivLeftThumbRoll,segmentImage,imgWidth,imgHeight);
                                        else
                                            showBitmapToImageView(_ivThumbLeftSeg,segmentImage,imgWidth,imgHeight);
                                    }
                                    if(nFingerType == DeviceDataHandler.FingerType.RS_FGP_RIGHT_THUMB.value())
                                    {
                                        if(_rsApi.getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_D)
                                            showBitmapToImageView(_ivRightThumbRoll,segmentImage,imgWidth,imgHeight);
                                        else
                                            showBitmapToImageView(_ivThumbRightSeg,segmentImage,imgWidth,imgHeight);
                                    }
                                }
                                else
                                {
                                    if (nFingerIndex == 1)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_THUMB.value(),mSegmentData);
                                        showBitmapToImageView(_ivThumbLeftSeg,segmentImage,imgWidth,imgHeight);
                                    }
                                    if (nFingerIndex == 2)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_THUMB.value(),mSegmentData);
                                        showBitmapToImageView(_ivThumbRightSeg,segmentImage,imgWidth,imgHeight);
                                    }
                                }
                            }
                            if(nFingerCnt == nFingerIndex)
                            {
                                if(errorCode == ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode())
                                {
                                    Logger.d("UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_THUMB");
                                    mHandler.sendEmptyMessage(UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_THUMB);
                                }
                                Logger.d("Enroll process is done.");
                                mDeviceDataHandler.setLastEnrollSequence(true);
                                mDeviceDataHandler.setSequenceCheck(true);
                                for( Map.Entry<Integer, CaptureData> entry : mDeviceDataHandler.getEnrollmentImageMap().entrySet())
                                {
                                    mSequenceCheckMap.put(entry.getKey(),entry.getValue());
                                }
                                Logger.d("getEnrollmentImageMap size : "+mDeviceDataHandler.getEnrollmentImageMap().size());
                                Logger.d("mSequenceCheckMap size : "+mSequenceCheckMap.size());
                            }
                            break;
                        case 3://RS_CAPTURE_FLAT_TWO_FINGERS
                            Logger.d("Enrollment : " + captureMode.getCaptureDesc());
                            if(_selectedDeviceType == DeviceType.REALSCAN_D)
                            {
                                Logger.d("RealScan D Enrollment Segment Image : " + mEnrollData.getMode());
                                if(mEnrollData == EnrollData.RS_ENROLL_LEFT_TWO_1st)
                                {
                                    if (nFingerIndex == 1)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_LEFT_LITTLE.value(),mSegmentData);
                                        showBitmapToImageView(_ivLeftSeg4,segmentImage,imgWidth,imgHeight);
                                    }
                                    if (nFingerIndex == 2)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_LEFT_RING.value(),mSegmentData);
                                        showBitmapToImageView(_ivLeftSeg3,segmentImage,imgWidth,imgHeight);
                                    }
                                    if(nFingerIndex == nFingerCnt)
                                    {
                                        mEnrollData = EnrollData.RS_ENROLL_LFET_TWO_2nd;
                                        SystemClock.sleep(1000);
                                        setEnrollmentEvent(FLAT_TWO);
                                    }
                                }
                                else if(mEnrollData == EnrollData.RS_ENROLL_LFET_TWO_2nd)
                                {
                                    if (nFingerIndex == 1)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_LEFT_MIDDLE.value(),mSegmentData);
                                        showBitmapToImageView(_ivLeftSeg2,segmentImage,imgWidth,imgHeight);
                                    }
                                    if (nFingerIndex == 2)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_LEFT_INDEX.value(),mSegmentData);
                                        showBitmapToImageView(_ivLeftSeg1,segmentImage,imgWidth,imgHeight);
                                    }
                                    if(nFingerCnt == nFingerIndex)
                                    {
                                        mEnrollData = EnrollData.RS_ENROLL_RIGHT_TWO_1st;
                                        SystemClock.sleep(1000);
                                        setEnrollmentEvent(FLAT_TWO);
                                    }
                                }
                                else if(mEnrollData == EnrollData.RS_ENROLL_RIGHT_TWO_1st)
                                {
                                    if (nFingerIndex == 1)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_RIGHT_INDEX.value(),mSegmentData);
                                        showBitmapToImageView(_ivRightSeg1,segmentImage,imgWidth,imgHeight);
                                    }
                                    if (nFingerIndex == 2)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_RIGHT_MIDDLE.value(),mSegmentData);
                                        showBitmapToImageView(_ivRightSeg2,segmentImage,imgWidth,imgHeight);
                                    }
                                    if(nFingerCnt == nFingerIndex)
                                    {
                                        mEnrollData = EnrollData.RS_ENROLL_RIGHT_TWO_2nd;
                                        SystemClock.sleep(1000);
                                        setEnrollmentEvent(FLAT_TWO);
                                    }
                                }
                                else if(mEnrollData == EnrollData.RS_ENROLL_RIGHT_TWO_2nd)
                                {
                                    if (nFingerIndex == 1)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_RIGHT_RING.value(),mSegmentData);
                                        showBitmapToImageView(_ivRightSeg3,segmentImage,imgWidth,imgHeight);
                                    }
                                    if (nFingerIndex == 2)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_RIGHT_LITTLE.value(),mSegmentData);
                                        showBitmapToImageView(_ivRightSeg4,segmentImage,imgWidth,imgHeight);
                                    }
                                    if(nFingerCnt == nFingerIndex)
                                    {
                                        mEnrollData = EnrollData.RS_ENROLL_THUMB;
                                        SystemClock.sleep(1000);
                                        setEnrollmentEvent(FLAT_TWO_THUMB);
                                    }
                                }
                                else if(mEnrollData == EnrollData.RS_ENROLL_THUMB)
                                {
                                    if (nFingerIndex == 1)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_LEFT_THUMB.value(),mSegmentData);
                                        showBitmapToImageView(_ivLeftThumbRoll,segmentImage,imgWidth,imgHeight);
                                    }
                                    if (nFingerIndex == 2)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_FGP_RIGHT_THUMB.value(),mSegmentData);
                                        showBitmapToImageView(_ivRightThumbRoll,segmentImage,imgWidth,imgHeight);
                                    }
                                    if(nFingerCnt == nFingerIndex)
                                    {
                                        mEnrollData = EnrollData.RS_ENROLL_INIT_STATE;
                                        mDeviceDataHandler.setLastEnrollSequence(true);
                                    }
                                }
                            }
                            break;
                        case 4://RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS
                            Logger.d("Enrollment : " + captureMode.getCaptureDesc());

                            if(!mDeviceDataHandler.isMissingFingerActivate() &&  mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS
                                /*&&  errorCode != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode()*/)
                            {
                                mDeviceDataHandler.getEnrollmentImageMap().put(nFingerType,mSegmentData);
                            }
                            else
                            {
                                if(mDeviceDataHandler.isMissingFingerActivate() && errorCode != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode())
                                {
                                    mDeviceDataHandler.getEnrollmentImageMap().put(nFingerType,mSegmentData);
                                }
                                else
                                {
                                    if (nFingerIndex == 1)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_SEGMENT4.value(),mSegmentData);

                                    }
                                    else if (nFingerIndex == 2)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_SEGMENT3.value(),mSegmentData);;

                                    }
                                    else if (nFingerIndex == 3)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_SEGMENT2.value(),mSegmentData);

                                    }
                                    else if (nFingerIndex == 4)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_LEFT_SEGMENT1.value(),mSegmentData);

                                    }
                                }
                            }
                            if(nFingerCnt == nFingerIndex)
                            {
                                if(errorCode == ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode())
                                {
                                    mHandler.sendEmptyMessage(UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_LEFT);
//                                    showEnrollErrorDialog(ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode());
                                }
//                                else
//                                {
                                    SystemClock.sleep(1000);
                                    setEnrollmentEvent(FLAT_FOUR_RIGHT);
//                                }
                            }
                            break;
                        case 5://RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS
                            Logger.d("Enrollment : " + captureMode.getCaptureDesc());
                            if(!mDeviceDataHandler.isMissingFingerActivate() /*&&  errorCode != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode()*/)
                            {
                                mDeviceDataHandler.getEnrollmentImageMap().put(nFingerType,mSegmentData);
                            }
                            else
                            {
                                if(mDeviceDataHandler.isMissingFingerActivate() && errorCode != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode())
                                {
                                    mDeviceDataHandler.getEnrollmentImageMap().put(nFingerType,mSegmentData);
                                }
                                else
                                {
                                    if (nFingerIndex == 1)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_SEGMENT1.value(),mSegmentData);

                                    }
                                    else if (nFingerIndex == 2)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_SEGMENT2.value(),mSegmentData);;

                                    }
                                    else if (nFingerIndex == 3)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_SEGMENT3.value(),mSegmentData);

                                    }
                                    else if (nFingerIndex == 4)
                                    {
                                        mDeviceDataHandler.getEnrollmentImageMap().put(DeviceDataHandler.FingerType.RS_UNKNOWN_RIGHT_SEGMENT4.value(),mSegmentData);

                                    }
                                }
                            }
                            if(nFingerCnt == nFingerIndex)
                            {
                                if(errorCode == ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode())
                                {
                                    mHandler.sendEmptyMessage(UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_RIGHT);
                                }
                                SystemClock.sleep(1000);
                                setEnrollmentEvent(FLAT_TWO_THUMB);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            if(mDeviceDataHandler.getNfiqmode()) {
                mSettingFragment.updateNfiqScore(mDeviceDataHandler.isEnrollment(), errorCode, nFingerCnt);
            }
            if( mDeviceDataHandler.isLfdActivated() > 0
                    && mDeviceDataHandler.getCaptureMode() != CaptureMode.RS_CAPTURE_ROLL_FINGER
                    && mDeviceDataHandler.isEnrollment() == false
                    && _selectedDeviceType != DeviceType.REALSCAN_D
                    && _selectedDeviceType != DeviceType.REALSCAN_F
                    && _selectedDeviceType != DeviceType.REALSCAN_F_C
                    && _selectedDeviceType != DeviceType.REALSCAN_F_CiBIOS){
                mSettingFragment.updateLfdScore(nFingerCnt, errorCode);
                updateBasicLfdScore(nFingerCnt, nFingerIndex, errorCode);
            }
        }

        @Override
        public void onCaptureProcessCompleted() {
            Logger.start();
            finishCaptureProcess();
        }

        @Override
        public void onParameterChanged(String prameterName, Object oldValue, Object newValue) {
            Logger.d("prameterName : " + prameterName + " oldValue : "+oldValue + " newValue : " + newValue);
        }

        @Override
        public void onParameterNotChanged(String prameterName, Object oldValue, Object newValue, int errorCode) {
            Logger.d("prameterName : " + prameterName + " oldValue : "+oldValue + " newValue : " + newValue + " errorCode : " + errorCode);
        }

        @Override
        public void onError(String errMessage) {
            Logger.d(errMessage);
            Logger.d("isSequenceCheck : " + mDeviceDataHandler.isSequenceCheck());
            Logger.d("getCaptureMode : "+mDeviceDataHandler.getCaptureMode());
            if(mDeviceDataHandler.isSequenceCheck() && (mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_ROLL_FINGER ||
                    mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER
                    || mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER_EX))
            {
                mHandler.sendEmptyMessage(ABORT_CAPTURE_BY_ERROR);
                showSeqCheckConfirmDialog(true,errMessage,mDeviceDataHandler.getCurrentSeqCheckFinger());
            }
            else
            {
                if(errMessage.equals(ErrorCode.ERROR_SEGMENT_FEWER_FINGER.toString()))
                {
                    showErrorDialog(getResources().getString(R.string.err_fewerfinger));
                }
                else if(errMessage.equals(ErrorCode.ERROR_SEGMENT_WRONG_HAND.toString()))
                {
//                    Toast.makeText(mContext,getResources().getString(R.string.err_wronghand),Toast.LENGTH_SHORT).show();
                    showErrorDialog(getResources().getString(R.string.err_wronghand));
                    mHandler.sendEmptyMessage(ABORT_CAPTURE_BY_ERROR);
                }
                else if(errMessage.equals(ErrorCode.ERROR_FINGER_COUNT_NOT_MATCHED.toString()))
                {
                    showErrorDialog(getResources().getString(R.string.err_fingercount_not_matched));

                }
                else if(errMessage.equals(ErrorCode.ERROR_CAN_NOT_SEGMENT.toString()))
                {
//                    Toast.makeText(mContext,getResources().getString(R.string.err_can_not_segment),Toast.LENGTH_SHORT).show();
                    showErrorDialog(getResources().getString(R.string.err_can_not_segment));
                }
                else
                {
                    showErrorDialog(errMessage);
                }
                if(!errMessage.equals(ErrorCode.ERROR_SEGMENT_WRONG_HAND.toString())){
                    mHandler.sendEmptyMessage(ABORT_CAPTURE_BY_ERROR);
                }
            }
        }
    };

    private void setTextViewStringFromFingerType(int nFingerType) {
        Logger.d("nFingerType: " + nFingerType);
        switch (nFingerType)
        {
            case 1: //RS_FGP_RIGHT_THUMB
                _tvSeg2.setText("RIGHT_THUMB");
                break;
            case 2: //RS_FGP_RIGHT_INDEX
                _tvSeg1.setText("RIGHT_INDEX");
                break;
            case 3: //RS_FGP_RIGHT_MIDDLE
                _tvSeg2.setText("RIGHT_MIDDLE");
                break;
            case 4: //RS_FGP_RIGHT_RING
                _tvSeg3.setText("RIGHT_RING");
                break;
            case 5: //RS_FGP_RIGHT_LITTLE
                _tvSeg4.setText("RIGHT_LITTLE");
                break;
            case 6: //RS_FGP_LEFT_THUMB
                _tvSeg1.setText("LEFT_THUMB");
                break;
            case 7: //RS_FGP_LEFT_INDEX
                _tvSeg4.setText("LEFT_INDEX");
                break;
            case 8: //RS_FGP_LEFT_MIDDLE
                _tvSeg3.setText("LEFT_MIDDLE");
                break;
            case 9: //RS_FGP_LEFT_RING
                _tvSeg2.setText("LEFT_RING");
                break;
            case 10: //RS_FGP_LEFT_LITTLE
                _tvSeg1.setText("LEFT_LITTLE");
                break;
            case 17: //RS_FGP_FULL
                break;
            case 18: //RS_UNKNOWN_SEGMENT1
                _tvSeg1.setText("UNKNOWN_SEGMENT1");
                break;
            case 19: //RS_UNKNOWN_SEGMENT2
                _tvSeg2.setText("UNKNOWN_SEGMENT2");
                break;
            case 20: //RS_UNKNOWN_SEGMENT3
                _tvSeg3.setText("UNKNOWN_SEGMENT3");
                break;
            case 21: //RS_UNKNOWN_SEGMENT4
                _tvSeg4.setText("UNKNOWN_SEGMENT4");
                break;
            case 22:
                _tv_leftthumbroll.setText("UNKNOWN_LEFT_THUMB");
                break;
            case 23:
                _tv_leftindex.setText("UNKNOWN_LEFT_SEGMENT1");
                break;
            case 24:
                _tv_leftmiddle.setText("UNKNOWN_LEFT_SEGMENT2");
                break;
            case 25:
                _tv_leftringer.setText("UNKNOWN_LEFT_SEGMENT3");
                break;
            case 26:
                _tv_leftpinky.setText("UNKNOWN_LEFT_SEGMENT4");
                break;
            case 27:
                _tv_rightthumbroll.setText("UNKNOWN_RIGHT_THUMB");
                break;
            case 28:
                _tv_rightindex.setText("RS_UNKNOWN_RIGHT_SEGMENT1");
                break;
            case 29:
                _tv_rightmiddle.setText("RS_UNKNOWN_RIGHT_SEGMENT2");
                break;
            case 30:
                _tv_rightringer.setText("RS_UNKNOWN_RIGHT_SEGMENT3");
                break;
            case 31:
                _tv_rightpinky.setText("RS_UNKNOWN_RIGHT_SEGMENT4");
                break;
        }
    }

    /**
     * Callback to be notified when a device is connected or disconnected.
     */
    DeviceCallback _deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceAttached(UsbDevice usbDevice) {
            Logger.d("onDeviceAttached");
            mHandler.sendEmptyMessage(ACTIVATE_USB_DEVICE);
        }

        @Override
        public void onDeviceDetached(UsbDevice usbDevice) {
            deviceDetached(usbDevice);
        }

        @Override
        public void onDeviceConnected(RealScanDevice device) {
            Logger.d("onDeviceConnected!");
            mIsCheckCustomerId = true;
            mCurrentDeviceInfo = _rsApi.getDeviceList().get(0);
            mHandler.sendEmptyMessage(UPDATE_DEVICE_INFO);
            mHandler.sendEmptyMessage(UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE);
            mDeviceName = mCurrentDeviceInfo.DeviceType.getProductName();
            mSdkVersionInfo = _rsApi.getSdkVersionInfo();
            mSettingFragment.mDeviceDataHandler = mDeviceDataHandler;
            mSettingFragment.resetPreference(true);
            mSettingFragment.updateSettingMenu();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkWritePermission();
            }
            cleanupBasicImgView();
            cleanupEnrollImgView();
            cleanupBasicTextView();


            if(device.getDeviceType() == DeviceType.REALSCAN_S60)
            {
               connectToLedDevice();
            }
            if(device.getDeviceType() == DeviceType.REALSCAN_D)
                enrollstartbutton.setText(getResources().getString(R.string.bt_startenrollment_D));
            else
                enrollstartbutton.setText(getResources().getString(R.string.bt_startenrollment));

            mProductDate = _rsApi.getProductDate();
            Logger.d("mProductDate : " + mProductDate);
        }

        @Override
        public void onDeviceDisConnected(RealScanDevice device) {
            Logger.d("onDeviceDisConnected!");
        }
    };

    /**
     * A variable that receives and holds the list of currently connected devices.
     */
    List<DeviceInfo> _deviceList;
    /**
     * Selected capture mode.
     */
    /**
     * Device type of selected device.
     */
    DeviceType _selectedDeviceType = DeviceType.INVALID_DEVICE;
    EnrollData  mEnrollData = EnrollData.RS_ENROLL_INIT_STATE;
    private DeviceManager mDeviceManager;
    public DeviceDataHandler mDeviceDataHandler;

    /**
     * Called when the app is created in the lifecycle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Logger();
        Logger.d("START! : " + this);
        mContext = this;

        requestWakeLock();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Xperix Inc.");
        mToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(mToolbar);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager2) findViewById(R.id.pager);

        initParamToSdk();
        if(viewPager != null)
        {
            setupTablayout();
            setupViewPager(viewPager);
        }
        initUsbListener();
    }

    private boolean initParamToSdk() {
        Logger.start();
        if(mUsbManager == null)
            mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        try {
            mDeviceDataHandler = DeviceDataHandler.getInstance();
            this._rsApi = RealScanAPI.getInstance(this,mUsbManager);
            if (this._rsApi == null) {
//                showErrorDialog(ErrorCode.ERROR_NOT_FOUND_DEVICE.getErrorCode());
                return false;
            }
            // Register callback to receive notifications such as captured images after capture is finished.
            if (this._rsApi != null) {
                this._rsApi.setDataCallback(this._dataCallback);
            }
            // Register callback to be notified when a device is connected or disconnected.
            if (this._rsApi != null) {
                this._rsApi.setDeviceCallback(this._deviceCallback);
            }
            mDataStorage = DataStorage.getInstance(mContext);
            mImageUtil = mDeviceDataHandler.getImageUtil();
            // Set the size of the captured image to be returned.
        } catch (Exception ex) {
            //Log.d("=====Test", "onCreate() Error = " + ex.getMessage());
        }
        Logger.end();
        return true;
    }

    private void setupTablayout() {

        Logger.d("setupTablayout");

        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            //tab.setCustomView(adapter.getTabView(i));
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                int count = 0;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Logger.d("mIsCapturing : " + mIsCapturing + " count : " +count);
                    if(mIsCapturing)
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            });
        }

    }

    private void setupViewPager(ViewPager2 viewPager) {
        Logger.d("setupViewPager");
        mAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle());

        mAdapter.createFragment(0);
        mAdapter.createFragment(1);
        mAdapter.createFragment(2);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(0);
        currenViewPosition =0;
        viewPager.setOffscreenPageLimit(2);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
                @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    //Configure your tabs...
                    if(position == 0 )
                        tab.setText("BASIC FUNCTION");
                    if(position == 1)
                        tab.setText("ENROLLMENT");
                    if(position == 2)
                        tab.setText("SETTING");
                }
            }).attach();
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(mIsCapturing == false)
                {
                    TabLayout.Tab tab =  tabLayout.getTabAt(position);
                    tab.select();
                }

                if(position == 0)
                {
                    Logger.d("onPageSelected : " + position + " This is Basic Function Page.");
                    currenViewPosition = 0;
                    if(mCurrentDeviceInfo == null)
                    {
                        resetUiComponent(false);
                        mSettingFragment.resetPreference(false);
                    }
                    if(mDeviceDataHandler != null) mDeviceDataHandler.setBEnrollmentPage(false);
                }
                if(position == 1)
                {
                    Logger.d("onPageSelected : " + position + " This is Enrollment Function Page.");
                    currenViewPosition = 1;
                    if(mCurrentDeviceInfo == null)
                    {
                        resetUiComponent(false);
                        mSettingFragment.resetPreference(false);
                    }
                    if(mDeviceDataHandler != null) mDeviceDataHandler.setBEnrollmentPage(true);
                }
                if(position == 2)
                {
                    Logger.d("onPageSelected : " + position + " This is Setting Page.");
                    currenViewPosition = 2;
                    if(mCurrentDeviceInfo == null)
                    {
                        resetUiComponent(false);
                        mSettingFragment.resetPreference(false);
                    }
                    if(mDeviceDataHandler != null) mDeviceDataHandler.setBEnrollmentPage(false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem developItem = menu.findItem(R.id.develop_menu);
        if(BuildConfig.BUILD_TYPE.equals("debug")) {
            developItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int selectedItem = item.getItemId();
        {
            if(RealScanAPI.getInstance() == null) {
                try {
                    _rsApi = RealScanAPI.getInstance(mContext,mUsbManager);
                    if(_rsApi == null) {
                        showErrorDialog(ErrorCode.ERROR_NOT_FOUND_DEVICE.getErrorCode());
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            switch (selectedItem)
            {
                case R.id.sdk_init:
                    Logger.d("sdk init is clicked : " + RealScanAPI.getInstance().hasSelectedDevice());
                    if(RealScanAPI.getInstance().hasSelectedDevice()) {
                        if(mIsCapturing) {
                            abortCapture();
                        }
                        //removeDevice();
                        mHandler.sendEmptyMessageDelayed(REMOVE_USB_DEVICE, 500);
                    }else{
                        //addDeviceToUsbDeviceList();
                        try {
                            boolean result = initParamToSdk();
                            if(result) {
                                addDeviceToUsbDeviceList(null);
                            }else {
                                showErrorDialog(ErrorCode.ERROR_NOT_FOUND_DEVICE.getErrorCode());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case R.id.develop_menu:
                    int result = RealScanAPI.getInstance().launchDevelopMenu(mContext);
                    Logger.d("result : " + result);
                    if(result != ErrorCode.OK.getErrorCode()) {
                        mToolbar.getMenu().removeItem(selectedItem);
                        showErrorDialog(result);
                    }
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadResourceEnrollment(View view) {
        Logger.d("start!");

        this._btSelectDeviceEnroll = (Button)view.findViewById(R.id.btSelectDeviceEnroll);
        this.enrollfullimage = (ImageView) view.findViewById(R.id.enrollfullimage);
        enrollfullimage.setOnClickListener(this);
        this._tvSdkInfoEnroll = (TextView) view.findViewById(R.id.tvsdkinfoEnroll);
        this._tvModeEnroll = (TextView) view.findViewById(R.id.tvModeEnroll);
        this._tvDeviceEnroll = (TextView) view.findViewById(R.id.tvDeviceEnroll);

        this._tv_leftindex = (TextView) view.findViewById(R.id.tv_leftindex);
        Logger.d("tvleftindex: " + _tv_leftindex);
        this._tv_leftmiddle = (TextView) view.findViewById(R.id.tv_leftmiddle);
        this._tv_leftringer = (TextView) view.findViewById(R.id.tv_leftringer);
        this._tv_leftthumbroll = (TextView) view.findViewById(R.id.tv_leftthumbroll);
        this._tv_leftpinky = (TextView) view.findViewById(R.id.tv_leftpinky);

        this._tv_rightindex = (TextView) view.findViewById(R.id.tv_rightindex);
        this._tv_rightmiddle = (TextView) view.findViewById(R.id.tv_rightmiddle);
        this._tv_rightringer = (TextView) view.findViewById(R.id.tv_rightringer);
        this._tv_rightthumbroll = (TextView) view.findViewById(R.id.tv_rightthumbroll);
        this._tv_rightpinky = (TextView) view.findViewById(R.id.tv_rightpinky);

        this._tv_leftfour = (TextView) view.findViewById(R.id.tv_leftfour);
        this._tv_leftthumbseg = (TextView) view.findViewById(R.id.tv_leftthumbseg);
        this._tv_rightthumbseg = (TextView) view.findViewById(R.id.tv_rightthumbseg);
        this._tv_rightfour = (TextView) view.findViewById(R.id.tv_rightfour);

        this._ivLeftSeg1 = (ImageView) view.findViewById(R.id._ivLeftSeg1);
        this._ivLeftSeg2 = (ImageView) view.findViewById(R.id._ivLeftSeg2);
        this._ivLeftSeg3 = (ImageView) view.findViewById(R.id._ivLeftSeg3);
        this._ivLeftSeg4 = (ImageView) view.findViewById(R.id._ivLeftSeg4);
        this._ivLeftThumbRoll = (ImageView) view.findViewById(R.id._ivThumbLeftRoll);

        this._ivRightSeg1 = (ImageView) view.findViewById(R.id._ivRightSeg1);
        this._ivRightSeg2 = (ImageView) view.findViewById(R.id._ivRightSeg2);
        this._ivRightSeg3 = (ImageView) view.findViewById(R.id._ivRightSeg3);
        this._ivRightSeg4 = (ImageView) view.findViewById(R.id._ivRightSeg4);
        this._ivRightThumbRoll = (ImageView) view.findViewById(R.id._ivThumbRightRoll);

        this._ivLeftFour = (ImageView) view.findViewById(R.id._ivLeftFour);
        this._ivThumbLeftSeg = (ImageView) view.findViewById(R.id._ivThumbLeftSeg);
        this._ivThumbRightSeg = (ImageView) view.findViewById(R.id._ivThumbRightSeg);
        this._ivRightFour = (ImageView) view.findViewById(R.id._ivRightFour);

        this.enrollstartbutton = (Button) view.findViewById(R.id.enrollstartbutton);
        this._btAbortEnroll = (Button)view.findViewById(R.id.btAbortEnroll);

        _btAbortEnroll.setOnClickListener(this);
        enrollstartbutton.setOnClickListener(this);
        _btSelectDeviceEnroll.setOnClickListener(this);

        Logger.d("UI resource load complete!");
    }
    public void cleanupEnrollImgView()
    {
        Logger.d("start!");
        if(mCurrentNightMode == true) {
            enrollfullimage.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivLeftSeg1.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivLeftSeg2.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivLeftSeg3.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivLeftSeg4.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivLeftThumbRoll.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivRightSeg1.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivRightSeg2.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivRightSeg3.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivRightSeg4.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivRightThumbRoll.setBackgroundColor(getResources().getColor(R.color.colordarkmode));

            _ivLeftFour.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivThumbLeftSeg.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivThumbRightSeg.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivRightFour.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
        }
        enrollfullimage.setImageResource(0);

        _ivLeftSeg1.setImageResource(0);
        _ivLeftSeg2.setImageResource(0);
        _ivLeftSeg3.setImageResource(0);
        _ivLeftSeg4.setImageResource(0);
        _ivLeftThumbRoll.setImageResource(0);

        _ivRightSeg1.setImageResource(0);
        _ivRightSeg2.setImageResource(0);
        _ivRightSeg3.setImageResource(0);
        _ivRightSeg4.setImageResource(0);
        _ivRightThumbRoll.setImageResource(0);

        _ivLeftFour.setImageResource(0);
        _ivThumbLeftSeg.setImageResource(0);
        _ivThumbRightSeg.setImageResource(0);
        _ivRightFour.setImageResource(0);

    }
    public void cleanupBasicImgView() {
        Logger.d("start!");
        if(mCurrentNightMode == true) {
            _ivCapture.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivSeg1.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivSeg2.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivSeg3.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
            _ivSeg4.setBackgroundColor(getResources().getColor(R.color.colordarkmode));
        }
        _ivCapture.setImageResource(0);
        _ivSeg1.setImageResource(0);
        _ivSeg2.setImageResource(0);
        _ivSeg3.setImageResource(0);
        _ivSeg4.setImageResource(0);
        _btSaveImage.setEnabled(false);
    }

    public void cleanupBasicTextView() {
        Logger.d("start!");
        _tvSeg1.setText("segment #1");
        _tvSeg2.setText("segment #2");
        _tvSeg3.setText("segment #3");
        _tvSeg4.setText("segment #4");

        seg1_Info_result.setTextColor(Color.BLACK);
        seg2_Info_result.setTextColor(Color.BLACK);
        seg3_Info_result.setTextColor(Color.BLACK);
        seg4_Info_result.setTextColor(Color.BLACK);

        seg1_Info_result.setText("");
        seg2_Info_result.setText("");
        seg3_Info_result.setText("");
        seg4_Info_result.setText("");
    }
    /**
     * Load resources.
     */
    public void loadResource(View view) {
        Logger.d("start!");
        this._tvDevice = (TextView)view.findViewById(R.id.tvDevice);
        this._tvSdkInfo = (TextView) view.findViewById(R.id.tvsdkinfo);
        this._tvMode = (TextView)view.findViewById(R.id.tvMode);

        this._btSelectDevice = (Button)view.findViewById(R.id.btSelectDevice);
        this._btSelectDevice.setOnClickListener(this);

        this._btSelectMode = (Button)view.findViewById(R.id.btSelectMode);
        this._btSelectMode.setOnClickListener(this);

        this._btSaveImage = (Button)view.findViewById(R.id.btSaveImage);
        this._btSaveImage.setOnClickListener(this);

        this._btStartPreview = (Button)view.findViewById(R.id.btStartPreview);
        this._btStartPreview.setOnClickListener(this);

        this._btManualCapture = (Button)view.findViewById(R.id.btManualCapture);
        this._btManualCapture.setOnClickListener(this);

        this._btAutoCapture = (Button)view.findViewById(R.id.btAutoCapture);
        this._btAutoCapture.setOnClickListener(this);

        this._btAbortCapture = (Button)view.findViewById(R.id.btAbortCapture);
        _btAbortCapture.setOnClickListener(this);

        this._ivCapture = (ImageView)view.findViewById(R.id._ivCapture);
        _ivCapture.setOnClickListener(this);
        this._ivSeg1 = (ImageView)view.findViewById(R.id._ivSeg1);
        _ivSeg1.setOnClickListener(this);
        this._ivSeg2 = (ImageView)view.findViewById(R.id._ivSeg2);
        _ivSeg2.setOnClickListener(this);
        this._ivSeg3 = (ImageView)view.findViewById(R.id._ivSeg3);
        _ivSeg3.setOnClickListener(this);
        this._ivSeg4 = (ImageView)view.findViewById(R.id._ivSeg4);
        _ivSeg4.setOnClickListener(this);

        this._tvSeg1 = (TextView)view.findViewById(R.id._tvSeg1);
        this._tvSeg2 = (TextView)view.findViewById(R.id._tvSeg2);
        this._tvSeg3 = (TextView)view.findViewById(R.id._tvSeg3);
        this._tvSeg4 = (TextView)view.findViewById(R.id._tvSeg4);

        this.seg1_Info_result = (TextView)view.findViewById(R.id._seg1_Info_result);
        this.seg2_Info_result = (TextView)view.findViewById(R.id._seg2_Info_result);
        this.seg3_Info_result = (TextView)view.findViewById(R.id._seg3_Info_result);
        this.seg4_Info_result = (TextView)view.findViewById(R.id._seg4_Info_result);
        Logger.d("End!");
    }

    private boolean hasPermissions(Context context, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission: permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false;
                }
            }
        }
        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkWritePermission() {
        Logger.d("start!");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasPermissions(this, PERMISSIONS_33)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions((String[]) PERMISSIONS_33, PERMISSION_ALL);
                    }
                }else{
                    Logger.d("WRITE_EXTERNAL_STORAGE permission already granted!");
                    requestBatteryOptimization();
                }
            }else{
                if (!hasPermissions(this, PERMISSIONS)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions((String[]) PERMISSIONS, PERMISSION_ALL);
                    }else{
                        Logger.d("WRITE_EXTERNAL_STORAGE permission already granted!");
                        requestBatteryOptimization();
                    }
                }
            }
        }catch (Exception e){
            Logger.d("e: " + e.getMessage());
        }
        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getPermissionIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                getPermissionIntent.setData(uri);
                if (getPermissionIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(getPermissionIntent);
                }
            }
        }
    }

    private void requestBatteryOptimization() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    /**
     * Select a capture mode.
     */
    protected void selectCaptureMode() {
        final ArrayAdapter<String> listadapter = new ArrayAdapter<String>(
                this,
                android.R.layout.select_dialog_singlechoice);
        if (this._selectedDeviceType == DeviceType.REALSCAN_G10
                || this._selectedDeviceType == DeviceType.REALSCAN_G10i
                || this._selectedDeviceType == DeviceType.REALSCAN_G10iBIOS
                || this._selectedDeviceType == DeviceType.REALSCAN_S60
                || this._selectedDeviceType == DeviceType.REALSCAN_SG10) {
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_TWO_FINGERS.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_TWO_THUMB.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_ROLL_FINGER.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER_EX.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_TWO_FINGERS_EX.getCaptureDesc());
        } else if (this._selectedDeviceType == DeviceType.REALSCAN_D) {
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_TWO_FINGERS.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_TWO_THUMB.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_ROLL_FINGER.getCaptureDesc());
        } else if (this._selectedDeviceType == DeviceType.REALSCAN_F_C
            || this._selectedDeviceType == DeviceType.REALSCAN_F_CiBIOS){
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_TWO_FINGERS.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_TWO_THUMB.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_ROLL_FINGER.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_SINGLE_FINGER_EX.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_TWO_FINGERS_EX.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_LEFT_PALM.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_RIGHT_PALM.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_LEFT_SIDE_PALM.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_RIGHT_SIDE_PALM.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_LEFT_WRITERS_PALM.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_RIGHT_WRITERS_PALM.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_LEFT_UPPER_PALM.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_RIGHT_UPPER_PALM.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_LEFT_LOWER_PALM.getCaptureDesc());
            listadapter.add(CaptureMode.RS_CAPTURE_FLAT_RIGHT_LOWER_PALM.getCaptureDesc());
        }

        if (listadapter != null) {
            showModeChoiceDialog(listadapter, getResources().getString(R.string.dialog_title_capturemod), 0, this);
        }
    }

    /**
     * Show capture mode list and select one.
     * @param listadapter Capture mode list.
     * @param title The title of the dialog.
     * @param defaultItem The default selection value.
     * @param context App's context.
     */
    public void showModeChoiceDialog(final ArrayAdapter<String> listadapter, String title, int defaultItem, Context context) {
        final ArrayList<Integer> selectedItem = new ArrayList<>();

        if (defaultItem >= 0 && defaultItem < listadapter.getCount()) {
            selectedItem.add(defaultItem);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
//        builder.setAdapter(listadapter, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                mDeviceDataHandler.setCaptureMode(CaptureMode.fromString(listadapter.getItem(which)));
//                updateMinimumFingerCount(mDeviceDataHandler.getCaptureMode());
//                updateCustomSegmentSize(mDeviceDataHandler.getCaptureMode());
//                _tvMode.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());
//                Logger.d("which = "+which+"_selectedCaptureMode = " + mDeviceDataHandler.getCaptureMode());
//                Message msg = new Message();
//                msg.what = UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE;
//                msg.obj = mDeviceDataHandler.getCaptureMode();
//                mHandler.sendMessage(msg);
//                cleanupBasicImgView();
//                cleanupBasicTextView();
//                dialog.dismiss();
//            }
//        });
        builder.setSingleChoiceItems(
                listadapter,
                listadapter.getPosition(mDeviceDataHandler.getCaptureMode().getCaptureDesc()),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeviceDataHandler.setCaptureMode(
                                CaptureMode.fromString(listadapter.getItem(which)));
                        updateMinimumFingerCount(mDeviceDataHandler.getCaptureMode());
                        updateCustomSegmentSize(mDeviceDataHandler.getCaptureMode());
                        _tvMode.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());

                        Logger.d("which = " + which + "_selectedCaptureMode = "
                                + mDeviceDataHandler.getCaptureMode());

                        Message msg = new Message();
                        msg.what = UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE;
                        msg.obj = mDeviceDataHandler.getCaptureMode();
                        mHandler.sendMessage(msg);

                        cleanupBasicImgView();
                        cleanupBasicTextView();
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.show();
    }

    private void updateMinimumFingerCount(CaptureMode captureMode) {
        Logger.start();
        //update minimum finger
        Logger.d("captureMode : " + captureMode);
        if(mDeviceDataHandler.getFingerCountFromCaptureMode() != mDeviceDataHandler.getMinimumNumberOfFinger() &&
                mDeviceDataHandler.getCaptureMode() != CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED)
        {
            mDeviceDataHandler.setMinimumNumberOfFinger(mDeviceDataHandler.getFingerCountFromCaptureMode());
            mDataStorage.setMinimumNumberOfFinger(mDeviceDataHandler.getFingerCountFromCaptureMode());
            mSettingFragment.pref();
        }
    }

    private boolean updateCustomSegmentSize(CaptureMode captureMode){
        Logger.start();
        Logger.d("captureMode : " + captureMode);

        if(mDataStorage.getCustomsegSizeActivate() > 0){
            if(captureMode.getCaptureModeId() == mDataStorage.getCustomsegSizeActivate()){
                return true;
            }
        }else{
            return true;
        }
        Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_wanning_custom_segment), Toast.LENGTH_SHORT).show();
        return false;
    }
    /**
     * Start preview capture.
     */
    protected void startCapture() {
        int result = 0;
        try {
            if ((this._rsApi != null) && (mDeviceDataHandler.getCaptureMode() != CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED)) {
                if (!this._rsApi.hasSelectedDevice()) {
                    //Log.d("=====Test", "There is no selected device.");
                    return;
                } else {
                    //Log.d("=====Test", "startCapture() permission check ok.");
                }
                if(mDataStorage.getCustomerIdActivate() && mIsCheckCustomerId == false)
                {
                    showErrorDialog(CUSTOMER_ID_IS_NOT_CONFIRMED);
                    return;
                }
                Logger.d("mIsCapturing = " + mIsCapturing);
                if(mIsCapturing == true) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_wanning_capturing), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_ROLL_FINGER
                    && mDeviceDataHandler.isLfdActivated() > 0) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_wanning_lfd_rollfinger), Toast.LENGTH_SHORT).show();
                }
                if(!updateCustomSegmentSize(mDeviceDataHandler.getCaptureMode())){
                    return;
                }
                Logger.i();
                cleanupBasicImgView();
                cleanupBasicTextView();

                mFullCaptureData = null;
                mSegmentData = null;
                mDeviceDataHandler.getSegmentImageMap().clear();
                clearNfiqScore();
                mPreviewCount = 0;
                mPreviewDialog = null;
                mIsAutoCapture = false;
                mDeviceDataHandler.setSequenceCheck(false);

                mHandler.sendEmptyMessage(UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE);

                viewPager.setUserInputEnabled(false);
                result = this._rsApi.startCapture(mDeviceDataHandler.getCaptureMode());
                if(result != 0)
                {
                    showErrorDialog(result);
                    viewPager.setUserInputEnabled(true);
                }
                else
                {
                    Logger.d("StartCapture started successfully.");
                    mIsCapturing = true;
                    Toast.makeText(MainActivity.this, "Please select a Manual Capture Button.", Toast.LENGTH_SHORT).show();
                    _btManualCapture.setEnabled(true);
                    _btAutoCapture.setEnabled(false);
                    _btAbortCapture.setEnabled(true);
                    _btSaveImage.setEnabled(false);
                }
            }
        } catch (Exception ex) {
            //Log.d("=====Test", "StartCapture Error = " + ex.getMessage());
        }
    }

    private void clearNfiqScore() {
        mDeviceDataHandler.setNfiqScoreSegment1(0);
        mDeviceDataHandler.setNfiqScoreSegment2(0);
        mDeviceDataHandler.setNfiqScoreSegment3(0);
        mDeviceDataHandler.setNfiqScoreSegment4(0);
        mSettingFragment.updateNfiqScore(true, ErrorCode.OK.getErrorCode(), 0);
    }

    /**
     * Capture a still image.
     */
    protected void manualCapture() {
        try {
            if ((this._rsApi != null) && (mDeviceDataHandler.getCaptureMode() != CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED)) {
                if (!this._rsApi.hasSelectedDevice()) {
                    //Log.d("=====Test", "There is no selected device.");
                    return;
                } else {
                    //Log.d("=====Test", "manualCapture() permission check ok.");
                }
                if(mDataStorage.getCustomerIdActivate() && mIsCheckCustomerId == false) {
                    showErrorDialog(CUSTOMER_ID_IS_NOT_CONFIRMED);
                    return;
                }
                _btManualCapture.setEnabled(false);
                int result = this._rsApi.manualCapture();
                if(result == ErrorCode.ERROR_CAPTURE_IS_RUNNING.getErrorCode()){
                    Toast.makeText(mContext, "Maunal capture is already in progress.", Toast.LENGTH_SHORT).show();
                }

            }
        } catch (Exception ex) {
            //Log.d("=====Test", "ManualCapture Error = " + ex.getMessage());
        }
    }

    /**
     * Start automatic capture.
     */
    protected void autoCapture() {
        int result = 0;
        try {
            if ((this._rsApi != null) && (mDeviceDataHandler.getCaptureMode() != CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED)) {
                if (!this._rsApi.hasSelectedDevice()) {
                    //Log.d("=====Test", "There is no selected device.");
                    return;
                } else {
                    //Log.d("=====Test", "manualCapture() permission check ok.");
                }
                if(mDataStorage.getCustomerIdActivate() && mIsCheckCustomerId == false)
                {
                    showErrorDialog(CUSTOMER_ID_IS_NOT_CONFIRMED);
                    return;
                }
                if(mIsCapturing == true)
                {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_wanning_capturing), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_ROLL_FINGER
                        && mDeviceDataHandler.isLfdActivated() > 0) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_wanning_lfd_rollfinger), Toast.LENGTH_SHORT).show();
                }
                if(!updateCustomSegmentSize(mDeviceDataHandler.getCaptureMode())){
                    return;
                }
                Logger.i();
                cleanupBasicImgView();
                cleanupBasicTextView();

                mFullCaptureData = null;
                mSegmentData = null;
                mDeviceDataHandler.getSegmentImageMap().clear();
                clearNfiqScore();
                mPreviewCount = 0;
                mPreviewDialog = null;
                mIsAutoCapture = true;
                mDeviceDataHandler.setSequenceCheck(false);

                mHandler.sendEmptyMessage(UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE);

                viewPager.setUserInputEnabled(false);
                Logger.d("this._selectedCaptureMode = " + mDeviceDataHandler.getCaptureMode());
                result = this._rsApi.autoCapture(mDeviceDataHandler.getCaptureMode());
                if(result != 0)
                {
                    viewPager.setUserInputEnabled(true);
                    showErrorDialog(result);
                }
                else
                {
                    Logger.d("Auto Capture started successfully.");
                    mIsCapturing = true;
                    _btStartPreview.setEnabled(false);
                    _btAbortCapture.setEnabled(true);
                    _btSaveImage.setEnabled(false);
                }
            }
        } catch (Exception ex) {
            //Log.d("=====Test", "AutoCapture Error = " + ex.getMessage());
        }
    }

    private void checkMissingFingerConfig() {
        Logger.d("START!");
        mDeviceDataHandler.setMissingFingerActivate(mDataStorage.getMissingFingerActivateValue());
        mHandler.sendEmptyMessage(UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE);
        Logger.d("END!");
    }

    // region : Device attach/detach notification

    /**
     * Called when a device is connected, if you have registered a callback to receive notification when the state of a device has changed.
     * @param usbDevice Connected usb device.
     */
    protected void deviceAttached(UsbDevice usbDevice) {
        Logger.d("deviceAttached");

    }

    /**
     * Called when a device is disconnected, if you have registered a callback to receive notification when the state of a device has changed.
     * @param usbDevice Disconnected usb device.
     */
    protected void deviceDetached(UsbDevice usbDevice) {
        Logger.d("deviceDetached");

    }

    /**
     * Called when the app is resumed in the lifecycle.
     */
    @Override
    protected void onResume() {
        super.onResume();
        //if Android Target SDK >=30
        if (Build.VERSION.SDK_INT >= 30) {
            if (!isExternalStorageManager()) {
                showSettingAccessFolder();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(!mWakeLock.isHeld())
            mWakeLock.acquire();
    }

    /**
     * Called when the app is paused in the lifecycle.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCapturing)
            abortCapture();
        
        if(mWakeLock.isHeld())
            mWakeLock.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mIsCapturing)
            abortCapture();

        if(mWakeLock.isHeld())
            mWakeLock.release();
    }

    /**
     * Called when the app is destroyed in the lifecycle.
     * Terminate the SDK.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy!");
        if(mIsCapturing)
            abortCapture();
        // Terminate the RealScan SDK at the end of the app.
        if (this._rsApi != null) {
//            this._rsApi.terminate();
            removeDevice();
            //Log.d("=====Test", "terminate...");

        }
        try {
            if (mUsbReceiver != null) {
                this.unregisterReceiver(mUsbReceiver);
            }
        } catch (IllegalArgumentException e) {
            mUsbReceiver = null;
        }
        _rsApi = null;
        mDeviceDataHandler = null;
        mContext = null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Logger.d("start : " + uri);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestBatteryOptimization();
        }
    }

    @Override
    public void onClick(View v) {
        Logger.d("start!");
        switch (v.getId())
        {
            case R.id.enrollstartbutton:
                Logger.d("enrollstartbutton clicked! ");
                if(!mDeviceDataHandler.getSegmentationActivate())
                {
                    Toast.makeText(this,"Please enable segmentation.",Toast.LENGTH_LONG).show();
                    return;
                }
                Logger.d("deviceType: " + mCurrentDeviceInfo.DeviceType);
                Logger.d("isMissingFingerActivate: "+mDeviceDataHandler.isMissingFingerActivate());

                cleanupEnrollImgView();
                startEnrollment();
                break;
            case R.id.btAbortCapture:
                Logger.d("btAbortCapture");
                abortCapture();
                break;
            case R.id.btAbortEnroll:
                Logger.d("btAbortEnroll");
                abortCapture();
                break;
            case R.id.btAutoCapture:
                Logger.d("btAutoCapture");
                if (mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED) {
                    Toast.makeText(MainActivity.this, "Please select a capture mode.", Toast.LENGTH_SHORT).show();
                    return;
                }
                autoCapture();
                break;
            case R.id.btStartPreview:
                Logger.d("btStartPreview");
                if (mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED) {
                    Toast.makeText(MainActivity.this, "Please select a capture mode.", Toast.LENGTH_SHORT).show();
                    return;
                }
                startCapture();

                break;
            case R.id.btManualCapture:
                Logger.d("btManualCapture");
                if (mDeviceDataHandler.getCaptureMode() == CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED) {
                    Toast.makeText(MainActivity.this, "Please select a device.", Toast.LENGTH_SHORT).show();
                    return;
                }
                manualCapture();
//                Toast.makeText(MainActivity.this, "Capture is complete", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btSaveImage:
                Logger.d("btSaveBitmap");

                saveCaptureImage();
                break;
            case R.id.btSelectMode:
                Logger.d("btSelectMode");
                if(mIsCapturing)
                {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_viewpager_capturing), Toast.LENGTH_SHORT).show();
                    return;
                }
                selectCaptureMode();
                break;
            default:
                break;
        }
    }

    public void checkCustomerId(String _id) {
        Logger.start();
        int result = 0;
        result =  _rsApi.checkCustomerId(_id);
        if(result != ErrorCode.OK.getErrorCode())
        {
            showErrorDialog(result);
            return;
        }
        else
        {
            if(_rsApi != null)
            {
                removeDevice();
                initParamToSdk();
                addDeviceToUsbDeviceList(null);
            }
        }
    }

    @SuppressLint("ResourceType")
    private void showImageInDialog(final Bitmap bitmap, boolean fullimage) {

        if(bitmap == null)
        {
            Logger.d("Capture Image is null!");
            return;
        }

        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });
        WindowManager.LayoutParams layoutParams = builder.getWindow().getAttributes();
        int imageWidth = 0;
        int imageHeight = 0;

        Display display = getWindowManager().getDefaultDisplay();

        Point displaySize = new Point();

        display.getSize(displaySize);

        Logger.d( ">>> size.x : " + displaySize.x + ", size.y : " + displaySize.y);
        if(fullimage)
        {
            imageWidth = WindowManager.LayoutParams.MATCH_PARENT;
            imageHeight = WindowManager.LayoutParams.MATCH_PARENT;
        }
        else
        {
            if(displaySize.x > 1280)
            {
                imageWidth = bitmap.getWidth()*2;
                imageHeight = bitmap.getHeight()*2;
            }
            else
            {
                imageWidth = (int) (bitmap.getWidth()*1.5);
                imageHeight = (int) (bitmap.getHeight()*1.5);
            }
        }

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        builder.addContentView(imageView, new LinearLayout.LayoutParams(
                imageWidth,
                imageHeight));
        builder.show();
    }
    private void makePreviewDialog()
    {
        mPreviewDialog = new Dialog(this);
        mPreviewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPreviewDialog.setContentView(R.layout.preivew_dialog);
        mPreviewDialog.getWindow( ).setFlags( WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                , WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL );
        mPreviewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });
        mPreviewDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    abortCapture();
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });


        WindowManager.LayoutParams layoutParams = mPreviewDialog.getWindow().getAttributes();
        int imageWidth = 0;
        int imageHeight = 0;

        Display display = getWindowManager().getDefaultDisplay();

        Point displaySize = new Point();

        display.getSize(displaySize);

        Logger.d( ">>> size.x : " + displaySize.x + ", size.y : " + displaySize.y);

        if(displaySize.x < displaySize.y)
        {
            imageWidth = displaySize.x;
            imageHeight = displaySize.x-50;
        }
        else if(displaySize.x > displaySize.y)
        {
            imageWidth = displaySize.y;
            imageHeight = displaySize.y-50;
        }


        mPreviewDialogImgView = mPreviewDialog.findViewById(R.id.previewdialog_imageview);
        Button dialogManualCaptureBtn = mPreviewDialog.findViewById(R.id.previewdialog_manualcapture);
        Button dialogAbortCaptureBtn = mPreviewDialog.findViewById(R.id.previewdialog_abortcapture);

        if(mIsAutoCapture == false)
            dialogManualCaptureBtn.setEnabled(true);

        dialogManualCaptureBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                manualCapture();
            }
        });
        dialogAbortCaptureBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                abortCapture();
            }
        });
        layoutParams.width = imageWidth;
        layoutParams.height = imageHeight;
        mPreviewDialog.getWindow().setAttributes(layoutParams);
        mPreviewDialog.show();
    }

    private void startEnrollment() {
        Logger.d("start! : " + _selectedDeviceType);
        if (!this._rsApi.hasSelectedDevice()) {
            showErrorDialog(ErrorCode.ERROR_NOT_FOUND_DEVICE.toString());
            return;
        } else {
            //Log.d("=====Test", "startCapture() permission check ok.");
        }
        if(mDataStorage.getCustomerIdActivate() && mIsCheckCustomerId == false)
        {
            showErrorDialog(ErrorCode.ERROR_CUSTOMER_ID_IS_NOT_CONFIRMED.getErrorCode());
            return;
        }
        if(mIsCapturing == true)
        {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_wanning_capturing), Toast.LENGTH_SHORT).show();
            return;
        }
        if(_selectedDeviceType == DeviceType.REALSCAN_D) {
            if (mDeviceDataHandler.isMissingFingerActivate()) {
                Toast.makeText(mContext, getResources().getString(R.string.err_missing_finger_not_support_rs_d_enrollment), Toast.LENGTH_LONG).show();
                return;
            }
        }
        _btAbortEnroll.setEnabled(true);

        cleanupEnrollImgView();

        mFullCaptureData = null;
        mSegmentData = null;
        mDeviceDataHandler.getSegmentImageMap().clear();
        mDeviceDataHandler.getEnrollmentImageMap().clear();
        mSequenceCheckMap.clear();

        mDeviceDataHandler.setEnrollment(true);
        mSettingFragment.updateNfiqScore(mDeviceDataHandler.isEnrollment());

        mHandler.sendEmptyMessage(UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE);
        enrollstartbutton.setEnabled(false);
        mDeviceDataHandler.setLastEnrollSequence(false);
        mDeviceDataHandler.setSequenceCheckStarted(false);
        mDeviceDataHandler.setSequenceCheck(false);
        if(_selectedDeviceType == DeviceType.REALSCAN_G10
                || _selectedDeviceType == DeviceType.REALSCAN_G10i
                || _selectedDeviceType == DeviceType.REALSCAN_G10iBIOS
                || _selectedDeviceType == DeviceType.REALSCAN_F_C
                || _selectedDeviceType == DeviceType.REALSCAN_F_CiBIOS
                || _selectedDeviceType == DeviceType.REALSCAN_S60
                || _selectedDeviceType == DeviceType.REALSCAN_SG10) {
            setEnrollmentEvent(FLAT_FOUR_LEFT);
        }else if(_selectedDeviceType == DeviceType.REALSCAN_D) {
            mEnrollData = EnrollData.RS_ENROLL_LEFT_TWO_1st;
            setEnrollmentEvent(FLAT_TWO);
        }
    }
    private void setEnrollmentEvent(int event)
    {
        switch (event)
        {
            case FLAT_FOUR_LEFT:
                Logger.d("FLAT_FOUR_LEFT");
                doEnrollmentProcess(event);
                break;
            case FLAT_FOUR_RIGHT:
                Logger.d("FLAT_FOUR_RIGHT");
                doEnrollmentProcess(event);
                break;
            case FLAT_TWO:
                Logger.d("FLAT_TWO");
                doEnrollmentProcess(event);
                break;
            case FLAT_TWO_THUMB:
                Logger.d("FLAT_TWO_THUMB");
                doEnrollmentProcess(event);
                break;
            default:
                break;
        }
    }

    private void showErrorDialog(String _errorMsg)
    {
        Logger.start();
        try {
            mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setTitle("Error Msg");

            CharSequence cs = _errorMsg;

            mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Logger.d("setPositiveButton clicked");
                    dialog.dismiss();
                }
            });

            mBuilder.setMessage(cs);
            mBuilder.setCancelable(false);
            //mBuilder.show();
            final AlertDialog dialog = mBuilder.create();
            dialog.show();
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (dialog.isShowing()) {
//                        dialog.dismiss();
//                    }
//                }
//            }, 2000); // 2000 milliseconds = 3 seconds
        }catch (Exception e)
        {
            Logger.d("e: " +e.getMessage());
        }
    }
    private void showErrorDialog(int _event)
    {
        Logger.d("event = " + _event);
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle("Error");
        CharSequence cs = "";
        if(_event == ErrorCode.ERROR_CUSTOMER_ID_IS_NOT_CONFIRMED.getErrorCode())
        {
            cs = "ERROR_CUSTOMER_ID_IS_NOT_CONFIRMED";
        }
        else if(_event == ErrorCode.ERROR_BACKGROUND_DIRTY.getErrorCode())
        {
            cs = "ERROR_BACKGROUND_DIRTY";
        }
        else if(_event == ErrorCode.ERROR_BACKGROUND_FINGER_EXIST.getErrorCode())
        {
            cs = "ERROR_BACKGROUND_FINGER_EXIST";
        }
        else if(_event == ErrorCode.ERROR_PARAMETER_VALUE_IS_INVALID.getErrorCode())
        {
            cs = "ERROR_PARAMETER_VALUE_IS_INVALID";
        }
        else {
            Logger.d("error event = " + ErrorCode.fromInt(_event).toString());
            cs = ErrorCode.fromInt(_event).toString();
        }

        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mBuilder.setMessage(cs);
        mBuilder.setCancelable(false);
        //mBuilder.show();
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (dialog.isShowing()) {
//                    dialog.dismiss();
//                }
//            }
//        }, 2000); // 2000 milliseconds = 3 seconds
    }

    /**
     * Capture 이벤트에 따라 Enrollment 동작을 수행하기 위한 API
     * Enrollemt는 Capture 모드를 조합하여 enrollment sequnence를 sample 상에 구현한 것일 뿐 별도의 enrollment mode가 존재하는 것이 아님
     * ex) G10, S60 - LEFT four - Right four - FLAT_TWO_THUMB 을 수행하도록 구현되어 있음.
     * RS-D의 경우 Flat Two - Flat Two - Flat Two - Flat Two - Flat Two_Thumb 를 수행하도록 구현됨.
     * RS-D의 경우 Missing Finger 메뉴 설정 시 사용성이 매우 떨어지므로 미지원하도록 협의함.
     * @param event
     */
    private void doEnrollmentProcess(int event) {
        int result = 0;

        mIsEnrollment = true;
        mIsCapturing = true;
        viewPager.setUserInputEnabled(false);
        mPreviewCount = 0;
        mPreviewDialog = null;

        Logger.d("getMissingFingerActivate = " + mDeviceDataHandler.isMissingFingerActivate());

        _btAbortEnroll.setEnabled(true);
        switch (event)
        {
            case FLAT_FOUR_LEFT :
                Logger.d("FLAT_FOUR_LEFT");
                _ivLeftFour.setImageBitmap(null);
                mDeviceDataHandler.setCaptureMode(CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS);
                updateMinimumFingerCount(mDeviceDataHandler.getCaptureMode());
                _tvMode.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());
                _tvModeEnroll.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());

                result = this._rsApi.autoCapture(mDeviceDataHandler.getCaptureMode());
                break;
            case FLAT_FOUR_RIGHT:
                Logger.d("FLAT_FOUR_RIGHT");
                _ivRightFour.setImageBitmap(null);
                mDeviceDataHandler.setCaptureMode(CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS);
                updateMinimumFingerCount(mDeviceDataHandler.getCaptureMode());
                _tvMode.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());
                _tvModeEnroll.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());

               result = this._rsApi.autoCapture(mDeviceDataHandler.getCaptureMode());
                break;
            case FLAT_TWO:
                Logger.d("FLAT_TWO");
                mDeviceDataHandler.setCaptureMode(CaptureMode.RS_CAPTURE_FLAT_TWO_FINGERS);
                updateMinimumFingerCount(mDeviceDataHandler.getCaptureMode());
                _tvMode.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());
                _tvModeEnroll.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());

                result = this._rsApi.autoCapture(mDeviceDataHandler.getCaptureMode());
                break;
            case FLAT_TWO_THUMB:
                Logger.d("FLAT_TWO_THUMB");
                _ivThumbLeftSeg.setImageBitmap(null);
                _ivThumbRightSeg.setImageBitmap(null);
                mDeviceDataHandler.setCaptureMode(CaptureMode.RS_CAPTURE_FLAT_TWO_THUMB);
                updateMinimumFingerCount(mDeviceDataHandler.getCaptureMode());
                _tvMode.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());
                _tvModeEnroll.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());

                result = this._rsApi.autoCapture(mDeviceDataHandler.getCaptureMode());
                break;
            default:
                break;
        }
        if(result != 0)
        {
            showErrorDialog(result);
            mDeviceDataHandler.setLastEnrollSequence(true);
            finishCaptureProcess();
        }
    }

    @Override
    public void onLedColorModeApplyButtonClick(int onoff, int ledmode, int color) {
        Logger.d("onLedColorModeApplyButtonClick onoff : " + onoff + ", ledmode : " +  ledmode + ", color : " + color);
        if(_rsApi != null){
            _rsApi.setModeLED(LEDMode.fromInt(ledmode), color, (onoff != 0) );
        }
    }

    @Override
    public void onStatusColorModeApplyButtonClick(int color) {
        Logger.d("onStatusColorModeApplyButtonClick color : " + color);
        if(_rsApi != null){
            _rsApi.setStatusLED(color);
        }
    }

    public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

        private ArrayList<Fragment> arrayList = new ArrayList<>();


        public ViewPagerFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {

                case 0:
                    mBasicFuncFragment =  new BasicFuncFragment();
                    return mBasicFuncFragment;
                case 1:
                    mEnrollFragment = new EnrollFragment();
                    return mEnrollFragment;
                case 2:
                    mSettingFragment = new SettingFragment(mContext);
                    return mSettingFragment;

            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    private void initUsbListener() {
        Logger.d("start!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            mContext.registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION), Context.RECEIVER_EXPORTED | Context.RECEIVER_VISIBLE_TO_INSTANT_APPS);
            mContext.registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED), Context.RECEIVER_EXPORTED);
            mContext.registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED), Context.RECEIVER_EXPORTED);
        } else {
            mContext.registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
            mContext.registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
            mContext.registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
        }
    }
    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action)
            {
                case ACTION_USB_PERMISSION:
                    Logger.d("ACTION_USB_PERMISSION");
                    boolean hasUsbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false);
                    if(hasUsbPermission) {
                        UsbDevice _Usbdevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        Logger.d(_Usbdevice.getDeviceName() + " is acquire the usb permission. activate this device.");
                        Logger.d( "ACTION_USB_PERMISSION, getProductId : "+ _Usbdevice.getProductId());
                        Message msg = new Message();
                        msg.what = ACTIVATE_USB_DEVICE;
                        msg.obj = (UsbDevice) _Usbdevice;
                        mHandler.sendMessage(msg);
                    } else {
                        Logger.d("USB permission is not granted!");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Logger.d("ACTION_USB_DEVICE_ATTACHED");
                    UsbDevice _usbdevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Logger.d("ACTION_USB_DEVICE_ATTACHED, getProductId : " +  _usbdevice.getProductId());
                    if(_rsApi != null){
                        if(RealScanAPI.getInstance().hasSelectedDevice()) {
                            if(mIsCapturing) {
                                abortCapture();
                            }
                            mHandler.sendEmptyMessageDelayed(REMOVE_USB_DEVICE, 10);
                        }
                    }
                    if(/*_rsApi == null && */_usbdevice.getProductId() != 0x1061) {
                        try {
                            boolean result = initParamToSdk();
                            if(result) {
                                addDeviceToUsbDeviceList(_usbdevice);
                            }else {
                                showErrorDialog(ErrorCode.ERROR_NOT_FOUND_DEVICE.getErrorCode());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Logger.d("ACTION_USB_DEVICE_DETACHED");
                    if(mIsCapturing) {
                        abortCapture();
                    }
                    //removeDevice();
                    mHandler.sendEmptyMessageDelayed(REMOVE_USB_DEVICE, 500);
                    break;
                default:
                    break;
            }
        }
    };

    public void addDeviceToUsbDeviceList(UsbDevice connectUsbDevice)
    {
        Logger.d("start!");
        if(mUsbManager == null)
        {
            Logger.d("mUsbManager is null");
            return;
        }
        if(_rsApi == null) {
            try {
                _rsApi = RealScanAPI.getInstance(mContext,mUsbManager);
                if(_rsApi == null)
                {
                    showErrorDialog(ErrorCode.ERROR_NOT_FOUND_DEVICE.getErrorCode());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        HashMap<String , UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIter = deviceList.values().iterator();
        boolean isFound = false;
        Logger.d("deviceList size = " + deviceList.size());
        while(deviceIter.hasNext()){
            UsbDevice _device = deviceIter.next();
            if( _device.getVendorId() ==0x16d1 ){
                Logger.d("found realscan usb device : " +  _device.getProductId());
                if(_rsApi.getDeviceTypeByProductId(_device) != DeviceType.INVALID_DEVICE) {
                    if(connectUsbDevice != null){
                        //It is very import, If not selectDevice, remove in usbDeviceList
                        if(_device.getProductId() == connectUsbDevice.getProductId()){
                            isFound = true;
                            Logger.d("This device need to Usb Permission!");
                            Message msg = new Message();
                            msg.what = REQUEST_USB_PERMISSION;
                            msg.obj = _device;
                            mHandler.sendMessage(msg);
                        }
                    }else{
                        isFound = true;
                        Logger.d("This device need to Usb Permission!");
                        Message msg = new Message();
                        msg.what = REQUEST_USB_PERMISSION;
                        msg.obj = _device;
                        mHandler.sendMessage(msg);
                        break;
                    }
                }else {
                    Logger.d("this is not realscan device.");
                }
            } else {
                Logger.d("This device is not realscan device!  : " + _device.getVendorId());
            }
        }

        if(isFound == false) {
            showErrorDialog(ErrorCode.ERROR_NOT_FOUND_DEVICE.getErrorCode());
            return;
        }

    }

    Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case ACTIVATE_USB_DEVICE:
                    UsbDevice _permssionGrantedDevice = (UsbDevice) msg.obj;
                    Logger.d("ACTIVATE_USB_DEVICE : " + _permssionGrantedDevice.getDeviceName());
                    Logger.d("ACTIVATE_USB_DEVICE, getProductId : " + _permssionGrantedDevice.getProductId());
                    if(_rsApi == null) {
                        _rsApi=RealScanAPI.getInstance();
                    }
                    initDevice(_permssionGrantedDevice);
                    break;
                case REMOVE_USB_DEVICE:
                    Logger.d("REMOVE_USB_DEVICE");
                    //_rsApi.terminate();
                    if(_selectedDeviceType == DeviceType.REALSCAN_SG10){
                        turnOffAllLEDs();
                    }
                    removeDevice();
                    break;
                case UPDATE_DEVICE_INFO:
                    Logger.d("UPDATE_DEVICE_INFO");
                    if(mCurrentDeviceInfo == null)
                    {
                        Logger.d("mCurrentDeviceInfo is null");
                        _tvDevice.setText("");
                        _tvDeviceEnroll.setText("");
                        _tvSdkInfo.setText("");
                        _tvSdkInfoEnroll.setText("");
                        _tvMode.setText("");
                        _tvDeviceEnroll.setText("");

                        _selectedDeviceType = DeviceType.INVALID_DEVICE;
                        mToolbar.setTitle("Xperix Inc");
                        resetUiComponent(false);
                    }
                    else
                    {
                        _tvDevice.setText(mCurrentDeviceInfo.DeviceType.getProductName());
                        _tvDeviceEnroll.setText(mCurrentDeviceInfo.DeviceType.getProductName());
                        _tvSdkInfo.setText(mSdkVersionInfo);
                        _tvSdkInfoEnroll.setText(mSdkVersionInfo);
                        _tvMode.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());
                        _tvModeEnroll.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());

                        _selectedDeviceType = mCurrentDeviceInfo.DeviceType;
                        if(mProductDate != null && !mProductDate.isEmpty()){
                            String productDate = "(" + mProductDate + ")";
                            mToolbar.setTitle(mDeviceName+productDate);
                        }else{
                            mToolbar.setTitle(mDeviceName);
                        }

                        _btSelectMode.setEnabled(true);
                        _btStartPreview.setEnabled(true);
                        _btManualCapture.setEnabled(false);
                        _btAutoCapture.setEnabled(true);
                        _btAbortCapture.setEnabled(false);
                        enrollstartbutton.setEnabled(true);
                        _btAbortEnroll.setEnabled(false);
                        _btSaveImage.setEnabled(false);
                    }
                    break;
                case REQUEST_USB_PERMISSION:
                    UsbDevice _usbDeviceToRequest = (UsbDevice) msg.obj;
                    //https://developer.android.com/reference/android/app/PendingIntent#FLAG_MUTABLE
                    int FLAG_MUTABLE = 0; //PendingIntent.FLAG_MUTABLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        FLAG_MUTABLE = PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT;
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        FLAG_MUTABLE = PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT; // setting the mutability flag
                    }
                    mPermissionIntent = PendingIntent.getBroadcast(mContext,0,new Intent(ACTION_USB_PERMISSION), FLAG_MUTABLE);
                    mUsbManager.requestPermission(_usbDeviceToRequest , mPermissionIntent);
                    break;
                case MAKE_DELAY_1SEC:
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case ADD_DEVICE:
                    addDeviceToUsbDeviceList(null);
                    break;
                case UPDATE_DEFAULT_TEXTVIEW_AS_CAPTURE_MODE:
//                    Logger.d("msg.obj : " + msg.obj + " capturemode : " + mDeviceDataHandler.getCaptureMode().getCaptureDesc());
                    CaptureMode _captureMode = (CaptureMode)msg.obj;
                        if(currenViewPosition == 0)
                        {

                            _tvSeg1.setText(mContext.getResources().getString(R.string.textview_segment1));
                            _tvSeg2.setText(mContext.getResources().getString(R.string.textview_segment2));
                            _tvSeg3.setText(mContext.getResources().getString(R.string.textview_segment3));
                            _tvSeg4.setText(mContext.getResources().getString(R.string.textview_segment4));
                        }
                        else
                        {
                            _tv_leftindex.setText(mContext.getResources().getString(R.string.left_finger_index));
                            _tv_leftmiddle.setText(mContext.getResources().getString(R.string.left_finger_middle));
                            _tv_leftringer.setText(mContext.getResources().getString(R.string.left_finger_ringer));
                            _tv_leftpinky.setText(mContext.getResources().getString(R.string.left_finger_little));
                            _tv_rightindex.setText(mContext.getResources().getString(R.string.right_finger_index));
                            _tv_rightmiddle.setText(mContext.getResources().getString(R.string.right_finger_middle));
                            _tv_rightringer.setText(mContext.getResources().getString(R.string.right_finger_ringer));
                            _tv_rightpinky.setText(mContext.getResources().getString(R.string.right_finger_little));
                            _tv_leftthumbroll.setText(mContext.getResources().getString(R.string.left_finger_thumb));
                            _tv_rightthumbroll.setText(mContext.getResources().getString(R.string.right_finger_thumb));
                        }
                        if(mDeviceDataHandler.getCustomLedMode())
                            setControlLEDMode();
                    break;
                case UPDATE_ERROR_TEXTVIEW_TO_DEFAULT:
                    _tvSeg1.setText("unknwon segment 1");
                    _tvSeg2.setText("unknwon segment 2");
                    _tvSeg3.setText("unknwon segment 3");
                    _tvSeg4.setText("unknwon segment 4");
                    break;
                case UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_LEFT:
                    _tv_leftindex.setText("unknwon segment 1");
                    _tv_leftmiddle.setText("unknwon segment 2");
                    _tv_leftringer.setText("unknwon segment 3");
                    _tv_leftpinky.setText("unknwon segment 4");
                    break;
                case UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_RIGHT:
                    _tv_rightindex.setText("unknwon segment 1");
                    _tv_rightmiddle.setText("unknwon segment 2");
                    _tv_rightringer.setText("unknwon segment 3");
                    _tv_rightpinky.setText("unknwon segment 4");
                    break;
                case UPDATE_ERROR_TEXTVIEW_TO_DEFAULT_ENROLL_THUMB:
                    _tv_leftthumbroll.setText("unknwon thumb segment 1");
                    _tv_rightthumbroll.setText("unknwon thumb segment 2");
                    break;
                case ABORT_CAPTURE_BY_ERROR:
                    Logger.d("ABORT_CAPTURE_BY_ERROR");
                    abortCapture();
                    break;
                case VIEW_INIT_BY_ENROLL_CANEL:
                    setViewInitByEnrollCancel();
                    break;
                case FINISH_CAPTURE_PROCESS:
                    _btStartPreview.setEnabled(true);
                    _btAutoCapture.setEnabled(true);
                    _btManualCapture.setEnabled(false);
                    _btAbortCapture.setEnabled(false);

                    if(mDeviceDataHandler == null)
                        return;

                    if(mDeviceDataHandler.isEnrollment()
                            && !mDeviceDataHandler.isSequenceCheck()) {
                        _btAbortEnroll.setEnabled(false);
                    }

                    if(mIsCapturing)
                        _btSaveImage.setEnabled(true);

                    if(currenViewPosition == 0)
                        viewPager.setUserInputEnabled(true);

                    Logger.d("mDeviceDataHandler.isLastEnrollSequence() : " + mDeviceDataHandler.isLastEnrollSequence());
                    if(currenViewPosition == 1 && mDeviceDataHandler.isLastEnrollSequence()) {
                        mDeviceDataHandler.setEnrollment(false);
                        mDeviceDataHandler.setLastEnrollSequence(false);
                        viewPager.setUserInputEnabled(true);
                        if(!mDeviceDataHandler.isSequenceCheck()){
                            enrollstartbutton.setEnabled(true);
                        }
                    }

                    mIsCapturing = false;
                    Logger.d("isSequenceCheck : " + mDeviceDataHandler.isSequenceCheck() + " isSequenceCheckStarted : " + mDeviceDataHandler.isSequenceCheckStarted());
                    if(mDeviceDataHandler.isSequenceCheck() && mDeviceDataHandler.isSequenceCheckStarted() == false
                        && mSequenceCheckMap.size() > 0)
                    {
                        SystemClock.sleep(1000);
                        makeSequenceCheckList();
                        mDeviceDataHandler.setSequenceCheckStarted(true);
                    }
                    if(mPreviewDialog != null)
                        mPreviewDialog.dismiss();
                    break;
                case SHOW_CAPTURE_IMAGE_TO_UI:
                    byte[] previewImage = (byte[]) msg.obj;
                    int imgWidth = msg.arg1;
                    int imgHeight = msg.arg2;
                    Logger.d("imgWidth : " + imgWidth + " imgHeight : " + imgHeight);

                    if(currenViewPosition == 0)
                    {
                        if(mDeviceDataHandler.getEnhancedPreviewMode())
                            showBitmapToImageView(mPreviewDialogImgView,previewImage,imgWidth,imgHeight);
                        else
                            showBitmapToImageView(_ivCapture,previewImage,imgWidth,imgHeight);

                    }
                    else if(currenViewPosition == 1)
                    {
                        if(mDeviceDataHandler.getEnhancedPreviewMode())
                            showBitmapToImageView(mPreviewDialogImgView,previewImage,imgWidth,imgHeight);
                        else
                            showBitmapToImageView(enrollfullimage,previewImage,imgWidth,imgHeight);
                    }
                    mElapsedTime = SystemClock.currentThreadTimeMillis() - mStartTime;
                    Logger.d("mElapsedTime : " + mElapsedTime);
                    break;
            }
        }
    };


    private void setControlLEDMode() {
        if (this._selectedDeviceType != DeviceType.REALSCAN_SG10) {
            return;
        }
        // Initialize all LEDs to OFF
        turnOffAllLEDs();

        // Turn on the LED mode according to the capture mode
        LEDMode ledMode = getLEDModeForCapture(mDeviceDataHandler.getCaptureMode());
        if (ledMode != null) {
            _rsApi.setModeLED(ledMode, IDPEnums.COLOR.WHITE.getValue(), true);
        }
    }

    /**
     * Returns the corresponding LED mode based on the capture mode.
     *
     * @param captureMode the current capture mode
     * @return the corresponding LEDMode, or null if not applicable
     */
    @Nullable
    private LEDMode getLEDModeForCapture(CaptureMode captureMode) {
        switch (captureMode) {
            case RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS:
                return LEDMode.fromInt(1);
            case RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS:
                return LEDMode.fromInt(2);
            case RS_CAPTURE_FLAT_TWO_THUMB:
                return LEDMode.fromInt(3);
            case RS_CAPTURE_ROLL_FINGER:
                return LEDMode.fromInt(4);
            default:
                return null;
        }
    }

    /**
     * Turns off all LEDs.
     */
    private void turnOffAllLEDs() {
        if(_rsApi != null) {
            _rsApi.setModeLED(LEDMode.fromInt(0), 1, false);
        }
    }

    private void initDevice(UsbDevice _usbDevice) {
        Logger.start();

        if(_rsApi == null) {
            try {
                _rsApi = RealScanAPI.getInstance(mContext,mUsbManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(mDataStorage == null) {
            mDataStorage = DataStorage.getInstance(mContext);
        }
        if(mDeviceDataHandler == null) {
            mDeviceDataHandler = DeviceDataHandler.getInstance();
        }
        mDataStorage.setInitializeDevice(_usbDevice.getProductId());

//        mDataStorage.setDirtyCheckActivate(true);
//        mDataStorage.setDirtyCheckLevelValue(5);

        mDeviceDataHandler.setUseDirtyCheck(mDataStorage.getDirtyCheckActivate());
        mDeviceDataHandler.setDirtyCheckLevel(Integer.valueOf(mDataStorage.getDirtyCheckLevelValue()));
        if(_usbDevice.getProductId() != DeviceType.REALSCAN_G10.getProductId()
                && _usbDevice.getProductId() != DeviceType.REALSCAN_F_C.getProductId()
                && _usbDevice.getProductId() != DeviceType.REALSCAN_F_CiBIOS.getProductId()
                && _usbDevice.getProductId() != DeviceType.REALSCAN_G10i.getProductId()
                && _usbDevice.getProductId() != DeviceType.REALSCAN_G10iBIOS.getProductId()
                && _usbDevice.getProductId() != DeviceType.REALSCAN_SG10.getProductId()) {
            mDataStorage.setCustomerIdActivate(false);
            mDataStorage.setCustomerIdValue("");
        }
        mDeviceDataHandler.setUseCustomerId(mDataStorage.getCustomerIdActivate());
        mDeviceDataHandler.setCustomerIdValue(mDataStorage.getCustomerIdValue());


        mDeviceDataHandler.setMissingFingerActivate(mDataStorage.getMissingFingerActivateValue());

//        mDataStorage.setLfdMode(0);
        mDeviceDataHandler.setLfdActivated(mDataStorage.getLfdMode());

        mDeviceDataHandler.setSegmentationActivate(mDataStorage.getSegmentationActivateValue());
        mDeviceDataHandler.setCustomSegSizeActivate(mDataStorage.getCustomsegSizeActivate());
        mDeviceDataHandler.setNativelibUsb(mDataStorage.getUseNativeUsbModeParam());
        mDeviceDataHandler.setLowSpeedUsbMode(mDataStorage.getLowCpuDeviceModeParam());

        if(mDeviceDataHandler.isCustomSegSizeActivate() > 0) {
            mDeviceDataHandler.setCustomSegmentSizeWidth(mDataStorage.getCustomSegSizeWidth());
            mDeviceDataHandler.setCustomSegmentSizeHeight(mDataStorage.getCustomSegSizeHeight());
        }

//        if(_usbDevice.getProductId() == DeviceType.REALSCAN_G10.getProductId()
//                || _usbDevice.getProductId() == DeviceType.REALSCAN_G10i.getProductId()
//                || _usbDevice.getProductId() == DeviceType.REALSCAN_G10iBIOS.getProductId()
//                || _usbDevice.getProductId() == DeviceType.REALSCAN_F_C.getProductId()
//                || _usbDevice.getProductId() == DeviceType.REALSCAN_F_CiBIOS.getProductId()
//                || _usbDevice.getProductId() == DeviceType.REALSCAN_S60.getProductId()
//                || _usbDevice.getProductId() != DeviceType.REALSCAN_SG10.getProductId()) {
//            mDataStorage.removeKey(mDataStorage.CAPTURE_AREA_EXPAND_MODE_KEY);
//        }
        mDeviceDataHandler.setCaptureAreaExpandMode(mDataStorage.getCaptureAreaExpandMode());

//        mDataStorage.setHaloReductionEnhandedMode(false);
        mDeviceDataHandler.setHaloReductionEnhanced(mDataStorage.getHaloReductionEnhandedExpandMode());

//        if(_usbDevice.getProductId() == DeviceType.REALSCAN_S60.getProductId()) {
//            mDeviceDataHandler.setS60CaptureExternalBrightMeasureMode(mDataStorage.getS60CaptureExternalBrightMeasureModeValue());
//            mDataStorage.setSensitiveMode(0);
//            mDeviceDataHandler.setSensitiveLevel(mDataStorage.getSensitiveMode());
//            mSettingFragment.updateSensitiveModePref(true);
//            _rsApi.setSensitiveLevelValue(mDataStorage.getSensitiveMode());
//        }

        if(_rsApi == null){
            _rsApi = RealScanAPI.getInstance();
        }
        int result = _rsApi.selectDevice(_usbDevice, mDeviceDataHandler);
        mDeviceDataHandler.setDeviceName(_usbDevice.getProductName());
        mDeviceDataHandler.setCaptureMode(mDeviceDataHandler.getCaptureMode());
        if(result != 0 ) {
            Logger.d("device init failed. error code = " + result);
            if(result == ErrorCode.ERROR_CUSTOMER_ID_IS_NOT_MATCHED.getErrorCode()) {
            }
            else if(result == ErrorCode.ERROR_BACKGROUND_DIRTY.getErrorCode()) {
            }
            else if(result == ErrorCode.ERROR_USB_DATA_TRANSFER_IS_FAILED.getErrorCode()) {
                Logger.d("ERROR_USB_DATA_TRANSFER_IS_FAILED. try again with enhancedusbmode");
            }
            showErrorDialog(result);
            return;
        } else {
            Logger.d("init device is succ. enable setting ui.");
            return;
        }
    }

    private void abortCapture() {
        Logger.d("start!");
        viewPager.setUserInputEnabled(true);
        if(_rsApi != null)
        {
            boolean result =  _rsApi.abortCapture();
            if(result == true) {
                Toast.makeText(MainActivity.this, "Capture Process is aborted!", Toast.LENGTH_LONG).show();
                mIsCapturing = false;
                mDeviceDataHandler.setEnrollment(false);
                _btManualCapture.setEnabled(false);
                _btAbortCapture.setEnabled(false);
                _btAbortEnroll.setEnabled(false);
                _btStartPreview.setEnabled(true);
                _btAutoCapture.setEnabled(true);
                enrollstartbutton.setEnabled(true);
            }  else {
                Toast.makeText(MainActivity.this, "Capture abort is failed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setViewInitByEnrollCancel() {
        Logger.d("start!");
        viewPager.setUserInputEnabled(true);
        if (_rsApi != null) {
            mIsCapturing = false;
            mDeviceDataHandler.setEnrollment(false);
            _btManualCapture.setEnabled(false);
            _btAbortCapture.setEnabled(false);
            _btAbortEnroll.setEnabled(false);
            _btStartPreview.setEnabled(true);
            _btAutoCapture.setEnabled(true);
            enrollstartbutton.setEnabled(true);
        }
    }


    /**
     * 연결 중인 장치를 제거하기 위한 API
     */
    public void removeDevice() {
        Logger.d("START!");
        if(RealScanAPI.getInstance() != null)
        {
            Logger.d("hasSelectedDevice : " + RealScanAPI.getInstance().hasSelectedDevice());
            if(RealScanAPI.getInstance().hasSelectedDevice())
            {
                viewPager.setCurrentItem(0);
                mDeviceDataHandler.setCaptureMode(CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED);
                mSettingFragment.resetPreference(false);
                mSettingFragment.pref();
                if(_rsApi != null)
                {
                    _rsApi.terminate();
                    _rsApi = null;
                }
                mHandler.sendEmptyMessage(UPDATE_DEVICE_INFO);
                cleanupBasicImgView();
                cleanupEnrollImgView();
                cleanupBasicTextView();
                mCurrentDeviceInfo = null;
                mIsCapturing = false;
                mDeviceDataHandler.setEnrollment(false);
            }
        }

    }
    private void saveCaptureImage() {
        Logger.d("START!");
        int result = 0;
        if(mCurrentDeviceInfo == null)
        {
            Toast.makeText(mContext,"REAL SCAN Device is not connected",Toast.LENGTH_LONG).show();
            return;
        }
        Logger.d("Current Capture Mode : " + mDeviceDataHandler.getCaptureMode().getCaptureDesc());

        String currentTime = getCurrentTime();
        String filename = currentTime + "_captured";
        result = _rsApi.saveCaptureImage(filename);

        if(result == 0)
        {
            Toast.makeText(mContext,"The captured image was saved as a file.",Toast.LENGTH_LONG).show();
        }
        else
        {
            showErrorDialog(result);
        }
    }


    private String getCurrentTime() {
        Logger.d("START");
        String result = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss.SSS");
        Date time = new Date();
        result = dateFormat.format(time);
        Logger.d("result : " + result);

        return result;
    }

    private void requestWakeLock() {
        Logger.d("START!");
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,":RealScan WakeLock");
        mWakeLock.acquire();
    }

    public void resetUiComponent(boolean _flag)
    {
        Logger.d("START!");

         _btSelectDevice.setEnabled(_flag);
         _btSelectMode.setEnabled(_flag);
         _btSaveImage.setEnabled(_flag);
         _btStartPreview.setEnabled(_flag);
         _btManualCapture.setEnabled(_flag);
         _btAutoCapture.setEnabled(_flag);
         _btAbortCapture.setEnabled(_flag);
         enrollstartbutton.setEnabled(_flag);
         _btAbortEnroll.setEnabled(_flag);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d("newConfig : " + newConfig.orientation);
        recreate();
    }

    public static synchronized Context getInstance() {
        return mContext;

    }
    private void finishCaptureProcess()
    {
        Logger.start();
        mHandler.sendEmptyMessage(FINISH_CAPTURE_PROCESS);
    }
    private void connectToLedDevice() {
        Logger.d("check led device");
        HashMap<String , UsbDevice> usbDevList = mUsbManager.getDeviceList();
        Set<String> _keySet = usbDevList.keySet();
        Logger.d("_keySet.size() = " + _keySet.size() + " containsLedDev = " + usbDevList.containsValue(0x1061));
        if(_keySet.size() > 1)
        {
            for(String i : _keySet)
            {
                if(usbDevList.get(i).getProductId() == 0x1061)
                {
                    Logger.d("This device need to Usb Permission!");
                    Message msg = new Message();
                    msg.what = REQUEST_USB_PERMISSION;
                    msg.obj = usbDevList.get(i);
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    private void makeSequenceCheckList() {
        Logger.start();
        Set<Integer> targetFingerSet = mSequenceCheckMap.keySet();
        ArrayList<Integer> targetFingerList = new ArrayList(targetFingerSet);
        Collections.sort(targetFingerList);

        for(int i=0;i<targetFingerList.size();i++)
        {
            Logger.d("targetFingerList["+i+"] : " + targetFingerList.get(i));
        }
        ArrayList<Integer> leftFingerList = new ArrayList<>();
        ArrayList<Integer> rightFingerList = new ArrayList<>();

        for(int i = 0; i < targetFingerList.size(); i++)
        {
            if(targetFingerList.get(i) < 6 || targetFingerList.get(i) > 26 )
            {
                rightFingerList.add(targetFingerList.get(i));
            }
            else
            {
                leftFingerList.add(targetFingerList.get(i));
            }
        }
        Collections.reverse(leftFingerList);
        mDeviceDataHandler.setCaptureMode(mDeviceDataHandler.getSequenceCheckCaptureMode());
        _tvMode.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());
        _tvModeEnroll.setText(mDeviceDataHandler.getCaptureMode().getCaptureDesc());
        updateMinimumFingerCount(mDeviceDataHandler.getCaptureMode());
        for(int i = 0 ; i < leftFingerList.size(); i++)
        {
            Logger.d("leftFingerList["+i+"] : " + leftFingerList.get(i));
        }
        if(leftFingerList.size() > 0)
        {
            doCaptureForSequenceCheck(leftFingerList.get(0));
        }
        else
        {
            if(rightFingerList.size() > 0)
                doCaptureForSequenceCheck(rightFingerList.get(0));
        }
        for(int i = 0 ; i < mSequenceCheckMap.keySet().size(); i++)
        {
            Logger.d("mSequenceCheckMap["+i+"] : " + mSequenceCheckMap.keySet().toArray()[i]);
        }
    }

    private void doCaptureForSequenceCheck(int _fingerType) {
        Logger.start();
        int result = 0;
        try {
            if ((this._rsApi != null) && (mDeviceDataHandler.getCaptureMode() != CaptureMode.RS_CAPTURE_RS_CAPTURE_DISABLED)) {
                if (!this._rsApi.hasSelectedDevice()) {
                    //Log.d("=====Test", "There is no selected device.");
                    return;
                } else {
                    //Log.d("=====Test", "manualCapture() permission check ok.");
                }
                if(mDataStorage.getCustomerIdActivate() && mIsCheckCustomerId == false)
                {
                    showErrorDialog(CUSTOMER_ID_IS_NOT_CONFIRMED);
                    return;
                }
                if(mIsCapturing == true)
                {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_wanning_capturing), Toast.LENGTH_SHORT).show();
                    return;
                }
                Logger.i();
                cleanupBasicImgView();

                mFullCaptureData = null;
                mSegmentData = null;
                mDeviceDataHandler.getSegmentImageMap().clear();
                clearNfiqScore();
                mPreviewCount = 0;
                mPreviewDialog = null;
                enrollstartbutton.setEnabled(false);
                _btAbortEnroll.setEnabled(true);
                mDeviceDataHandler.setCurrentSeqCheckFinger(_fingerType);
                Logger.d("_fingerType : "+_fingerType+" CurrentSeqCheckFinger : " + DeviceDataHandler.FingerType.Companion.fromInt(_fingerType));
                viewPager.setUserInputEnabled(false);
                Logger.d("this._selectedCaptureMode = " + mDeviceDataHandler.getCaptureMode());
                result = this._rsApi.autoCapture(mDeviceDataHandler.getCaptureMode());
                if(result != 0)
                {
                    viewPager.setUserInputEnabled(true);
                    showErrorDialog(result);
                }
                else
                {
                    Logger.d("Auto Capture started successfully.");
                    mIsCapturing = true;
                    _btStartPreview.setEnabled(false);
                    _btAbortCapture.setEnabled(true);
                    _btSaveImage.setEnabled(false);
                }
            }
        } catch (Exception ex) {
            //Log.d("=====Test", "AutoCapture Error = " + ex.getMessage());
        }

    }

    private void showSeqCheckConfirmDialog(boolean isError,String errMsg,int currentSeqCheckFinger) {
        Logger.start();
        try {
            mBuilder = new AlertDialog.Builder(mContext);
            if(isError)
                mBuilder.setTitle("Error Msg");
            else
                mBuilder.setTitle("Confirm Msg");
            String fingerType =  DeviceDataHandler.FingerType.Companion.fromInt(currentSeqCheckFinger).toString();
            CharSequence cs = "";
            if(isError)
                cs = "Sequence check for" + fingerType + " is failed by "+ errMsg +". \nWould you like to continue?";
            else
                cs = "Sequence check for" + fingerType + " is successful. \n remain check list: "+ mSequenceCheckMap.size() +"\nWould you like to continue?";

            mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Logger.d("setNegativeButton clicked");
                    dialog.dismiss();
                    if(mDeviceDataHandler.isSequenceCheck()) {
                        mDeviceDataHandler.setSequenceCheck(false);
                    }
                    mHandler.sendEmptyMessage(VIEW_INIT_BY_ENROLL_CANEL);
                }
            });
            mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Logger.d("setPositiveButton clicked");
                    dialog.dismiss();
                    makeSequenceCheckList();
                }
            });
            mBuilder.setNeutralButton("Try again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Logger.d("setNeutralButton clicked");
                    dialog.dismiss();
                    doCaptureForSequenceCheck(currentSeqCheckFinger);
                }
            });


            mBuilder.setMessage(cs);
            mBuilder.setCancelable(false);
            mBuilder.show();
        }catch (Exception e)
        {
            Logger.d("e: " +e.getMessage());
        }
    }
    private void setConfirmedEnrollImgToUI(int targetkey, Bitmap confirmedBmp)
    {
        DeviceDataHandler.FingerType fingerType = DeviceDataHandler.FingerType.Companion.fromInt(targetkey);
        Toast.makeText(mContext,fingerType.name() + " is confirmed.",Toast.LENGTH_LONG).show();
        switch (fingerType.value())
        {
            case 1:
                _ivRightThumbRoll.setImageBitmap(confirmedBmp);
                break;
            case 2:
                _ivRightSeg1.setImageBitmap(confirmedBmp);
                break;
            case 3:
                _ivRightSeg2.setImageBitmap(confirmedBmp);
                break;
            case 4:
                _ivRightSeg3.setImageBitmap(confirmedBmp);
                break;
            case 5:
                _ivRightSeg4.setImageBitmap(confirmedBmp);
                break;
            case 6:
                _ivLeftThumbRoll.setImageBitmap(confirmedBmp);
                break;
            case 7:
                _ivLeftSeg1.setImageBitmap(confirmedBmp);
                break;
            case 8:
                _ivLeftSeg2.setImageBitmap(confirmedBmp);
                break;
            case 9:
                _ivLeftSeg3.setImageBitmap(confirmedBmp);
                break;
            case 10:
                _ivLeftSeg4.setImageBitmap(confirmedBmp);
                break;
            case 22:
                _ivLeftThumbRoll.setImageBitmap(confirmedBmp);
                break;
            case 23:
                _ivLeftSeg1.setImageBitmap(confirmedBmp);
                break;
            case 24:
                _ivLeftSeg2.setImageBitmap(confirmedBmp);
                break;
            case 25:
                _ivLeftSeg3.setImageBitmap(confirmedBmp);
                break;
            case 26:
                _ivLeftSeg4.setImageBitmap(confirmedBmp);
                break;
            case 27:
                _ivRightThumbRoll.setImageBitmap(confirmedBmp);
                break;
            case 28:
                _ivRightSeg1.setImageBitmap(confirmedBmp);
                break;
            case 29:
                _ivRightSeg2.setImageBitmap(confirmedBmp);
                break;
            case 30:
                _ivRightSeg3.setImageBitmap(confirmedBmp);
                break;
            case 31:
                _ivRightSeg4.setImageBitmap(confirmedBmp);
                break;
        }
    }
    public void showBitmapToImageView(ImageView imageView,byte[] image, int width, int height) {
        Logger.start();
//        byte[] bitmapData = mImageUtil.getCaptureImageAsBmp(image, width, height);
        Bitmap bitmapData = getBitmapFromByte(image,width,height);

        int viewId = imageView.getId();

        Set<Integer> segmentImageViews = new HashSet<>(Arrays.asList(
                R.id._ivSeg1, R.id._ivSeg2, R.id._ivSeg3, R.id._ivSeg4
        ));

        if (mDeviceDataHandler.isCustomSegSizeActivate() > 0 && segmentImageViews.contains(viewId)) {
            bitmapData = scaleImageToCustomSize(bitmapData,
                mDeviceDataHandler.getCustomSegmentSizeHeight(),
                mDeviceDataHandler.getCustomSegmentSizeWidth()
            );
        }

        try {
            if(_rsApi.getCurrentDevice().getDeviceType() == DeviceType.REALSCAN_SG10){
                imageView.setImageBitmap(bitmapData);
            }else {
                Glide.with(getApplicationContext())
                        .load(bitmapData)
                        .dontAnimate()
                        .placeholder(imageView.getDrawable())
                        .into(imageView);
            }
        }
        catch (Exception e)
        {
            Logger.d("error = " + e.getMessage());
//            imageView.setImageBitmap(mImageUtil.getBitmapFromRaw(image,width,height));
        }

    }

    public void showBitmapToImageView(ImageView imageView, Bitmap bitmap) {
        Logger.start();
        try {
            Glide.with(getApplicationContext())
                    .load(bitmap)
                    .dontAnimate()
                    .placeholder(imageView.getDrawable())
                    .into(imageView);
        }
        catch (Exception e)
        {
            Logger.d("error = " + e.getMessage());
        }
    }
    public Bitmap getBitmapFromByte(byte[] image, int width, int height)
    {
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        byte[] Bits = new byte[width * height * 4];
        for (int i = 0; i < width * height; i++) {
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = image[i];
            Bits[i * 4 + 3] = -1;
        }

        bm.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bm;
    }

    public byte[] getByteFromBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        byte[] result = new byte[width * height];

        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;

            // Convert to grayscale using luminosity formula
            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            result[i] = (byte) gray;
        }

        return result;
    }


    public int setTransferMode(boolean _value, boolean _prefchanged)
    {
        Logger.d("setTransferMode : " + _value + " _prefchanged : " + _prefchanged);
        int result = 0;

        viewPager.setCurrentItem(0);

        if(_prefchanged == true){
            recreate();
        }

        return result;
    }

    private void updateBasicLfdScore(int nFingerCnt, int nFingerIndex, int nErrorCode) {
        int score1 = -1;
        int score2 = -1;
        int score3 = -1;
        int score4 = -1;

        String result1 = "";
        String result2 = "";
        String result3 = "";
        String result4 = "";

        if (mDeviceDataHandler.getLfdScore() != null) {
            Set<String> missingFingers = mDeviceDataHandler.getFingerSelectSet();
            List<Integer> lfdScores = mDeviceDataHandler.getLfdScore();
            List<Integer> lfdResults = mDeviceDataHandler.getLfdResult();

            int[] rightFourFingers = {2, 3, 4, 5};
            int[] leftFourFingers = {10, 9, 8, 7};
            int[] twoThumbFingers = {6, 1};

            if (mDeviceDataHandler.getCaptureMode()
                == CaptureMode.RS_CAPTURE_FLAT_RIGHT_FOUR_FINGERS && nErrorCode
                != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode()) {
                applyLfdResultForFourFingers(rightFourFingers, missingFingers, lfdScores,
                    lfdResults);
            } else if (mDeviceDataHandler.getCaptureMode()
                == CaptureMode.RS_CAPTURE_FLAT_LEFT_FOUR_FINGERS && nErrorCode
                != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode()) {
                applyLfdResultForFourFingers(leftFourFingers, missingFingers, lfdScores,
                    lfdResults);
            } else if (mDeviceDataHandler.getCaptureMode()
                == CaptureMode.RS_CAPTURE_FLAT_TWO_THUMB && nErrorCode
                != ErrorCode.ERROR_SEGMENT_RESULT_NOT_MATCHED_FINGER_SELECT.getErrorCode()) {
                applyLfdResultForFourFingers(twoThumbFingers, missingFingers, lfdScores,
                    lfdResults);
            } else {
                if (nFingerCnt == nFingerIndex) {
                    for (int i = 0; i < nFingerCnt; i++) {
                        if (i == 0) {
                            score1 = mDeviceDataHandler.getLfdScore().get(0);
                            Logger.d("Score 1 : " + score1);
                            result1 = score1 == -1 ? ""
                                : (mDeviceDataHandler.getLfdResult().get(0) == 0 ? "LIVE" + "("
                                    + String.valueOf(score1) + ")"
                                    : "FAKE" + "(" + String.valueOf(score1) + ")");
                            Logger.d("LFD#1 Result : " + result1);
                            seg1_Info_result.setText(result1);
                            if (mDeviceDataHandler.getLfdResult().get(0) == 0) {//Live
                                seg1_Info_result.setTextColor(Color.BLUE);
                            } else {//Fake
                                seg1_Info_result.setTextColor(Color.RED);
                            }
                        }
                        if (i == 1) {
                            score2 = mDeviceDataHandler.getLfdScore().get(1);
                            Logger.d("Score 2 : " + score2);
                            result2 = score2 == -1 ? ""
                                : (mDeviceDataHandler.getLfdResult().get(1) == 0 ? "LIVE" + "("
                                    + String.valueOf(score2) + ")"
                                    : "FAKE" + "(" + String.valueOf(score2) + ")");
                            Logger.d("LFD#2 Result : " + result2);
                            seg2_Info_result.setText(result2);
                            if (mDeviceDataHandler.getLfdResult().get(1) == 0) {//Live
                                seg2_Info_result.setTextColor(Color.BLUE);
                            } else {//Fake
                                seg2_Info_result.setTextColor(Color.RED);
                            }
                        }
                        if (i == 2) {
                            score3 = mDeviceDataHandler.getLfdScore().get(2);
                            Logger.d("Score 3 : " + score3);
                            result3 = score3 == -1 ? ""
                                : (mDeviceDataHandler.getLfdResult().get(2) == 0 ? "LIVE" + "("
                                    + String.valueOf(score3) + ")"
                                    : "FAKE" + "(" + String.valueOf(score3) + ")");
                            Logger.d("LFD#3 Result : " + result3);
                            seg3_Info_result.setText(result3);
                            if (mDeviceDataHandler.getLfdResult().get(2) == 0) {//Live
                                seg3_Info_result.setTextColor(Color.BLUE);
                            } else {//Fake
                                seg3_Info_result.setTextColor(Color.RED);
                            }
                        }
                        if (i == 3) {
                            score4 = mDeviceDataHandler.getLfdScore().get(3);
                            Logger.d("Score 4 : " + score4);
                            result4 = score4 == -1 ? ""
                                : (mDeviceDataHandler.getLfdResult().get(3) == 0 ? "LIVE" + "("
                                    + String.valueOf(score4) + ")"
                                    : "FAKE" + "(" + String.valueOf(score4) + ")");
                            Logger.d("LFD#4 Result : " + result4);
                            seg4_Info_result.setText(result4);
                            if (mDeviceDataHandler.getLfdResult().get(3) == 0) {//Live
                                seg4_Info_result.setTextColor(Color.BLUE);
                            } else {//Fake
                                seg4_Info_result.setTextColor(Color.RED);
                            }
                        }
                    }
                }
            }
        }
    }
    private void applyLfdResultForFourFingers(
            int[] fingerCodes,
            Set<String> missingFingers,
            List<Integer> lfdScores,
            List<Integer> lfdResults
    ) {
        int lfdIndex = 0;

        for (int viewIndex = 0; viewIndex < fingerCodes.length; viewIndex++) {
            int fingerCode = fingerCodes[viewIndex];

            if (mDeviceDataHandler.isMissingFingerActivate() && missingFingers.contains(String.valueOf(fingerCode))) {
                continue;
            }

            if (lfdIndex >= lfdScores.size()) {
                break;
            }

            int score = lfdScores.get(lfdIndex);
            int result = lfdResults.get(lfdIndex);
            String resultText = (score == -1)
                    ? ""
                    : (result == 0 ? "LIVE" : "FAKE") + "(" + score + ") ";

            Logger.d("Finger #" + fingerCode + ": " + resultText);

            TextView targetTextView = getResultTextView(viewIndex);
            if (targetTextView != null) {
                targetTextView.setText(resultText);
                targetTextView.setTextColor(result == 0 ? Color.BLUE : Color.RED);
            }

            lfdIndex++;
        }
    }
    private TextView getResultTextView(int fingerIndex) {
        switch (fingerIndex) {
            case 0:
                return seg1_Info_result;
            case 1:
                return seg2_Info_result;
            case 2:
                return seg3_Info_result;
            case 3:
                return seg4_Info_result;
            default:
                return null;
        }
    }
    private Bitmap scaleImageToCustomSize(Bitmap source, int newHeight, int newWidth)
    {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        Logger.d("sourceWidth : " + sourceWidth + " sourceHeight : " + sourceHeight);

        float left = (newWidth - sourceWidth) / 2;
        float top = (newHeight - sourceHeight) / 2;

        // be
        RectF targetRect = new RectF(left, top, left+sourceWidth, top+sourceHeight);

        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        dest.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }


    // if Android Target SDK >=30
    private void showSettingAccessFolder() {
        if (!isExternalStorageManager()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                accessManageFolder();
            }
        }
    }

    private boolean isExternalStorageManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }

    @RequiresApi(Build.VERSION_CODES.R)
    void accessManageFolder() {
        if (!Environment.isExternalStorageManager()) {
            Intent getPermissionIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            getPermissionIntent.setData(uri);
            if (getPermissionIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(getPermissionIntent);
            }
        }
    }

    public CaptureData convertAndScaleToCaptureData(
        byte[] image,
        byte[] templatedata,
        int width,
        int height
    ) {
        Bitmap bitmap = getBitmapFromByte(image, width, height);

        if (mDeviceDataHandler.isCustomSegSizeActivate() > 0) {
            bitmap = scaleImageToCustomSize(
                bitmap,
                mDeviceDataHandler.getCustomSegmentSizeHeight(),
                mDeviceDataHandler.getCustomSegmentSizeWidth()
            );
            width = bitmap.getWidth();
            height = bitmap.getHeight();
        }

        // Convert the final bitmap back to grayscale byte array
        byte[] processedImage = getByteFromBitmap(bitmap);

        // Return CaptureData based on whether templateData exists
        if (templatedata != null) {
            return new CaptureData(processedImage, templatedata, width, height);
        } else {
            return new CaptureData(processedImage, width, height);
        }
    }
}
