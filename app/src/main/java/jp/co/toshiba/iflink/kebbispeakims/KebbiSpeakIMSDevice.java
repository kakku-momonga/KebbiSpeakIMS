package jp.co.toshiba.iflink.kebbispeakims;

import android.Manifest;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Looper;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import java.util.HashMap;

//Toast Sample start
import android.widget.Toast;
//Toast Sample end

import com.nuwarobotics.service.IClientId;
import com.nuwarobotics.service.agent.NuwaRobotAPI;
import com.nuwarobotics.service.agent.RobotEventListener;
import com.nuwarobotics.service.agent.VoiceEventListener;
import com.nuwarobotics.service.agent.VoiceResultJsonParser;

import jp.co.toshiba.iflink.imsif.IfLinkConnector;
import jp.co.toshiba.iflink.imsif.DeviceConnector;
import jp.co.toshiba.iflink.imsif.IfLinkSettings;
import jp.co.toshiba.iflink.imsif.IfLinkAlertException;
import jp.co.toshiba.iflink.ui.PermissionActivity;

public class KebbiSpeakIMSDevice extends DeviceConnector {
    /**
     * ログ出力用タグ名.
     */
    private static final String TAG = "KEBBISPEAK-DEV";
    /**
     * メッセージを取得するキー.
     */
    private static final String KEBBI_SPEAK_JOB_KEY = "kebbi_speak_job_key";
    /**
     * ログ出力切替フラグ.
     */
    private boolean bDBG = false;
    //Toast Sample start
    /**
     * 処理実行のハンドラ.
     */
    //private Handler handler = new Handler(Looper.getMainLooper());
    //Toast Sample end

    static NuwaRobotAPI mRobotAPI;
    static IClientId mClientId;
    static Context mContext ;
    ArrayList<String> cmdTTS = new ArrayList<String>();
    ArrayList<String> cmdMotion = new ArrayList<String>();
    Handler mHandler = new Handler(Looper.getMainLooper());

    private int mCmdStep = 0 ;
    private boolean mTts_complete = true;
    private boolean mMotion_complete = true;

    /**
     * コンストラクタ.
     *
     * @param ims IMS
     */
    public KebbiSpeakIMSDevice(final IfLinkConnector ims) {
        super(ims, MONITORING_LEVEL0, PermissionActivity.class);
        Log.d(TAG, "KebbiSpeakIMSDevice called("+mIms.getPackageName()+")");
        mDeviceName = "KebbiSpeakIMSDevice";
        mDeviceSerial = "epa";

        mSchemaName = "kebbispeakdevice";
        setSchema();

        mCookie = IfLinkConnector.EPA_COOKIE_KEY_TYPE + "=" + IfLinkConnector.EPA_COOKIE_VALUE_CONFIG
                + IfLinkConnector.COOKIE_DELIMITER
                + IfLinkConnector.EPA_COOKIE_KEY_TYPE + "=" + IfLinkConnector.EPA_COOKIE_VALUE_ALERT
                + IfLinkConnector.COOKIE_DELIMITER
                + IfLinkConnector.EPA_COOKIE_KEY_TYPE + "=" + IfLinkConnector.EPA_COOKIE_TYPE_VALUE_JOB
                + IfLinkConnector.COOKIE_DELIMITER
                + IfLinkConnector.EPA_COOKIE_KEY_DEVICE + "=" + mDeviceName
                + IfLinkConnector.COOKIE_DELIMITER
                + IfLinkConnector.EPA_COOKIE_KEY_ADDRESS + "=" + IfLinkConnector.EPA_COOKIE_VALUE_ANY;

        mAssetName = "KEBBISPEAK_EPA";

        try {
            //Step 1 : Initial Nuwa API Object
            mClientId = new IClientId(mIms.getPackageName());
            mRobotAPI = new NuwaRobotAPI(mIms, mClientId);
            mRobotAPI.registerRobotEventListener(robotEventListener);//listen callback of robot service event
            Log.d(TAG, "kebbi initialized");
        }catch (Exception ex){
            Log.e(TAG, ex.getLocalizedMessage());
            Log.e(TAG, "ケビー初期化エラー");
        }

        // サンプル用：ここでデバイスを登録します。
        // 基本は、デバイスとの接続確立後、デバイスの対応したシリアル番号に更新してからデバイスを登録してください。
        addDevice();
        // 基本は、デバイスとの接続が確立した時点で呼び出します。
        notifyConnectDevice();
    }

    @Override
    public boolean onStartDevice() {
        if (bDBG) Log.d(TAG, "onStartDevice");

        // 送信開始が別途完了通知を受ける場合には、falseを返してください。
        return true;
    }

    @Override
    public boolean onStopDevice() {
        if (bDBG) Log.d(TAG, "onStopDevice");
        // デバイスからのデータ送信停止処理を記述してください。

        // 送信停止が別途完了通知を受ける場合には、falseを返してください。
        return true;
    }

    @Override
    public boolean onJob(final HashMap<String, Object> map) {
        //Toast Sample start
        if (map.containsKey(KEBBI_SPEAK_JOB_KEY)) {
            final String strVal = String.valueOf(map.get(KEBBI_SPEAK_JOB_KEY));
            // 抽出したパラメータの型変換
            String val = strVal;
            cmdTTS.clear();
            cmdTTS.add(val);

            Log.d(TAG,"onClick to start start demo") ;
            //Step 3 : reset command step and trigger action start thread
            mCmdStep = 0 ;
            mHandler.post(robotAction);//play next action

            /*
            // 抽出したパラメータを元に実際の制御を記述してください。
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(mIms, strVal, Toast.LENGTH_LONG).show();
                }
            });

             */
        }
        //Toast Sample end
        return false;
    }

    @Override
    public void enableLogLocal(final boolean enabled) {
        bDBG = enabled;
    }

    @Nullable
    @Override
    protected XmlResourceParser getResourceParser(final Context context) {
        Resources resources = context.getResources();
        if (resources != null) {
            return context.getResources().getXml(R.xml.schema_kebbispeakimsdevice);
        } else {
            return null;
        }

    }

    @Override
    protected void onUpdateConfig(@NonNull IfLinkSettings settings) throws IfLinkAlertException {
        if (bDBG) Log.d(TAG, "onUpdateConfig");
        String key = mIms.getString(R.string.pref_kebbispeakimsdevice_settings_parameter_key);
        String param = settings.getStringValue(key, "1");
        if (bDBG) Log.d(TAG, "parameter[" + key + "] = " + param);
        // 設定パラメータを更新する処理を記述してください。
        // insert routine for reflecting received parameter

    }

    @Override
    protected final String[] getPermissions() {
        if (bDBG) Log.d(TAG, "getPermissions");
        return new String[]{};
    }

    @Override
    protected void onPermissionGranted() {
        // パーミッションを許可された後の処理を記述してください。
    }
    Runnable robotAction = new Runnable() {
        @Override
        public void run() {
            String current_tts = cmdTTS.get(mCmdStep);
            String current_motion = "";//cmdMotion.get(mCmdStep);
            Log.d(TAG,"Action Step "+mCmdStep+" TTS:"+current_tts+" motion:"+current_motion);
            //Config waiting flag first.   (Example : use to wait two callback ready)
            if(current_tts != "") mTts_complete = false;
            if(current_motion != "") mMotion_complete = false;

            //Start play tts and motion if need
            if(current_tts != "") mRobotAPI.startTTS(current_tts);

            //Please NOTICE that auto_fadein should assign false when motion file nothing to display
            if(current_motion != "") mRobotAPI.motionPlay(current_motion,false);


            while(mTts_complete == false  || mMotion_complete == false){
                //wait both action complete
            }

            //both TTS and Motion complete, we play next action
            mCmdStep ++ ;//next action step
            if(mCmdStep < cmdTTS.size()) {
                mHandler.post(robotAction);//play next action
            }else{
                mRobotAPI.motionReset();//Reset Robot pose to default
            }
        }
    };
    RobotEventListener robotEventListener = new RobotEventListener() {
        @Override
        public void onWikiServiceStart() {
            // Nuwa Robot SDK is ready now, you call call Nuwa SDK API now.
            Log.d(TAG,"onWikiServiceStart, robot ready to be control ") ;
            //Step 3 : Start Control Robot after Service ready.
            //Register Voice Callback event
            mRobotAPI.registerVoiceEventListener(voiceEventListener);//listen callback of robot voice related event
            //Allow user start demo after service ready
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Allow user click button.
                    mStartDemoBtn.setEnabled(true);//when service ready, we start allow user start API function call
                }
            });*/

        }

        @Override
        public void onWikiServiceStop() {

        }

        @Override
        public void onWikiServiceCrash() {

        }

        @Override
        public void onWikiServiceRecovery() {

        }

        @Override
        public void onStartOfMotionPlay(String s) {

        }

        @Override
        public void onPauseOfMotionPlay(String s) {

        }

        @Override
        public void onStopOfMotionPlay(String s) {

        }

        @Override
        public void onCompleteOfMotionPlay(String s) {
            Log.d(TAG,"Play Motion Complete " + s);
            mMotion_complete = true;
        }

        @Override
        public void onPlayBackOfMotionPlay(String s) {

        }

        @Override
        public void onErrorOfMotionPlay(int i) {

        }

        @Override
        public void onPrepareMotion(boolean b, String s, float v) {

        }

        @Override
        public void onCameraOfMotionPlay(String s) {

        }

        @Override
        public void onGetCameraPose(float v, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8, float v9, float v10, float v11) {

        }

        @Override
        public void onTouchEvent(int i, int i1) {

        }

        @Override
        public void onPIREvent(int i) {

        }

        @Override
        public void onTap(int i) {

        }

        @Override
        public void onLongPress(int i) {

        }

        @Override
        public void onWindowSurfaceReady() {

        }

        @Override
        public void onWindowSurfaceDestroy() {

        }

        @Override
        public void onTouchEyes(int i, int i1) {

        }

        @Override
        public void onRawTouch(int i, int i1, int i2) {

        }

        @Override
        public void onFaceSpeaker(float v) {

        }

        @Override
        public void onActionEvent(int i, int i1) {

        }

        @Override
        public void onDropSensorEvent(int i) {

        }

        @Override
        public void onMotorErrorEvent(int i, int i1) {

        }
    };
    VoiceEventListener voiceEventListener = new VoiceEventListener() {
        @Override
        public void onWakeup(boolean b, String s, float v) {
            Log.d(TAG, "onWakeup:" + !b + ", score:" + s);

        }

        @Override
        public void onTTSComplete(boolean b) {
            Log.d(TAG, "onTTSComplete" + !b);
            mTts_complete = true;

        }

        @Override
        public void onSpeechRecognizeComplete(boolean b, ResultType resultType, String s) {

        }

        @Override
        public void onSpeech2TextComplete(boolean b, String s) {

        }

        @Override
        public void onMixUnderstandComplete(boolean b, ResultType resultType, String s) {
            Log.d(TAG, "onMixUnderstandComplete isError:" + !b + ", json:" + s);
            //Step 7 : Robot recognized the word of user speaking on  onMixUnderstandComplete
            //both startMixUnderstand and startLocalCommand will receive this callback
            String result_string = VoiceResultJsonParser.parseVoiceResult(s);
            Log.d(TAG,"onMixUnderstandComplete isError=" +b+" result_string="+result_string );
            //Step 8 : Request Robot speak what you want.
            /*
            switch(result_string){
                case "天気予報教えて":
                    mRobotAPI.startTTS("今日の天気は、晴れると思います");

                    break;
                case "おはよう":
                    mRobotAPI.startTTS("おはようございます");

                    break;
            }

             */
        }

        @Override
        public void onSpeechState(ListenType listenType, SpeechState speechState) {

        }

        @Override
        public void onSpeakState(SpeakType speakType, SpeakState speakState) {

        }

        @Override
        public void onGrammarState(boolean b, String s) {
            Log.d(TAG,"onGrammarState isError=" +b+" info="+s );
            //Step 5 : Aallow user press button to trigger startLocalCommand after grammar setup ready
            //startLocalCommand only allow calling after Grammar Ready
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Allow user click button.
                    //mStartWakeupBtn.setEnabled(true);//when service ready, we start allow user start API function call
                }
            });

             */


        }

        @Override
        public void onListenVolumeChanged(ListenType listenType, int i) {

        }

        @Override
        public void onHotwordChange(VoiceEventListener.HotwordState hotwordState, HotwordType hotwordType, String s) {

        }
    };
}