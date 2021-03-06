package com.ambarella.remotecamera.connectivity;


import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ambarella.remotecamera.CommonUtility;

public abstract class CmdChannel {
    private static final String TAG = "CmdChannel";
    private static final int RX_TIMEOUT = 3000;
    Timer timer;
    TimerTask timerTask;
    private  boolean timerFlag;
    public   String  msg;
    public boolean fetchFlag;


    /*
     * control if we should check the session ID
     */
    private boolean mCheckSessionId;

    /*
     * control if we should auto start-session in case that JSON
     * command needs a valid ID but the session is not started yet.
     */
    private boolean mAutoStartSession;

    private final Object  mRxLock = new Object();
    private boolean mReplyReceived;
    final Handler handler = new Handler();

    private static final int AMBA_GET_SETTING   = 0x001;
    private static final int AMBA_SET_SETTING   = 0x002;
    private static final int AMBA_GET_ALL       = 0x003;
    private static final int AMBA_FORMAT_SD     = 0x004;
    private static final int AMBA_GET_SPACE     = 0x005;
    private static final int AMBA_GET_NUM_FILES = 0x006;
    private static final int AMBA_NOTIFICATION  = 0x007;
    private static final int AMBA_BURN_FW       = 0x008;
    private static final int AMBA_GET_OPTIONS   = 0x009;
    private static final int AMBA_GET_DEVINFO   = 0x00B;
    private static final int AMBA_POWER_MANAGE  = 0x00C;
    private static final int AMBA_BATTERY_LEVEL = 0x00D;
    private static final int AMBA_ZOOM          = 0x00E;
    private static final int AMBA_ZOOM_INFO     = 0x00F;
    private static final int AMBA_SET_BITRATE   = 0x010;
    private static final int AMBA_START_SESSION = 0x101;
    private static final int AMBA_STOP_SESSION  = 0x102;
    private static final int AMBA_RESETVF       = 0x103;
    private static final int AMBA_STOP_VF       = 0x104;
    private static final int AMBA_SET_CLINT_INFO= 0x105;
    private static final int AMBA_RECORD_START  = 0x201;
    private static final int AMBA_RECORD_STOP   = 0x202;
    private static final int AMBA_RECORD_TIME   = 0x203;
    private static final int AMBA_FORCE_SPLIT   = 0x204;
    private static final int AMBA_TAKE_PHOTO    = 0x301;
    private static final int AMBA_STOP_PHOTO    = 0x302;
    private static final int AMBA_GET_THUMB     = 0x401;
    private static final int AMBA_GET_MEDIAINFO = 0x402;
    private static final int AMBA_SET_ATTRIBUTE = 0x403;
    private static final int AMBA_DEL           = 0x501;
    private static final int AMBA_LS            = 0x502;
    private static final int AMBA_CD            = 0x503;
    private static final int AMBA_PWD           = 0x504;
    private static final int AMBA_GET_FILE      = 0x505;
    private static final int AMBA_PUT_FILE      = 0x506;
    private static final int AMBA_CANCLE_XFER   = 0x507;
    private static final int AMBA_WIFI_RESTART  = 0x601;
    private static final int AMBA_SET_WIFI_SETTING  = 0x602;
    private static final int AMBA_GET_WIFI_SETTING  = 0x603;
    private static final int AMBA_WIFI_STOP     = 0x604;
    private static final int AMBA_WIFI_START    = 0x605;
    private static final int AMBA_GET_WIFI_STATUS = 0x606;
    private static final int AMBA_QUERY_SESSION_HOLDER = 0x701;

    private static final String ERR_CODE[] = {
            "OK",
            "UNKNOW(-1)",
            "INVALID_ERROR(-2)",
            "SESSION_START_FAIL(-3)",
            "INVALID_SESSION(-4)",
            "REACH_MAX_CLIENT(-5)",
            "INVALID_ERROR(-6)",
            "JSON_PACKAGE_ERROR(-7)",
            "JSON_PACKAGE_TIMEOUT(-8)",
            "JSON_SYNTAX_ERROR(-9)",
            "INVALID_ERROR(-10)",
            "INVALID_ERROR(-11)",
            "INVALID_ERROR(-12)",
            "INVALID_OPTION_VALUE(-13)",
            "INVALID_OPERATION(-14)",
            "INVALID_ERROR(-15)",
            "HDMI_INSERTED(-16)",
            "NO_MORE_SPACE(-17)",
            "CARD_PROTECTED(-18)",
            "NO_MORE_MEMORY(-19)",
            "PIV_NOT_ALLOWED(-20)",
            "SYSTEM_BUSY(-21)",
            "APP_NOT_READY(-22)",
            "OPERATION_UNSUPPORTED(-23)",
            "INVALID_TYPE(-24)",
            "INVALID_PARAM(-25)",
            "INVALID_PATH(-26)"
    };
    private static final int ERR_INVALID_TOKEN  = -4;
    private static final int ERR_MAX_NUM = 26;

    private int mSessionId;

    protected static IChannelListener mListener;

    protected abstract String readFromChannel();
    protected abstract void writeToChannel(byte[] buffer);


    public CmdChannel(IChannelListener listener) {
        mListener = listener;
        mCheckSessionId = false;
        mAutoStartSession = true;
    }

    public void startIO() {
        (new Thread(new QueueRunnable())).start();
    }

    public void reset() {
        mSessionId = 0;
    }

    private void addLog(String log) {
        if (mListener != null) {
            mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_LOG, log);
        }
    }

    private boolean checkSessionID() {
        if (!mCheckSessionId || (mSessionId > 0))
            return true;

        if (!mAutoStartSession) {
            mListener.onChannelEvent(
                    IChannelListener.CMD_CHANNEL_ERROR_INVALID_TOKEN, null);
            return false;
        }

        startSession();
        return true;
    }

    private boolean waitForReply() {
        int tmpLoop;

        try {
            synchronized (mRxLock) {
                mRxLock.wait(RX_TIMEOUT);
            }

            if (!mReplyReceived) {
                for (tmpLoop=1; tmpLoop < 10;tmpLoop++){
                    synchronized (mRxLock) {
                        mRxLock.wait(RX_TIMEOUT);
                    } if (mReplyReceived) {
                        tmpLoop = 99;
                        break;
                    }
                }
                // mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_ERROR_TIMEOUT, null);
                Log.e(TAG, "RX_TIMEOUT" + "response longer then 30 sec");// Add Json Segment to logView
                addLog("TimerOut Debug Dump msg buffer: " + msg + "<br >");
                msg = "";
                Log.e(TAG, "RX_TIMEOUT ::" + msg);
                fetchFlag = false;
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean sendRequest(String req) {
        addLog("<font color=#0000ff>" + req + "<br /></font>");
        Log.e(TAG, req);

        mReplyReceived = false;

        writeToChannel(req.getBytes());

        timer = new Timer();
        initTimerTask();
        timer.schedule(timerTask, 30000);//,30000);//timertask will run after every 30 secs
        timerFlag = true;
        fetchFlag = true;

        return waitForReply();
    }
    private void initTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Timeout 30 sec inform user
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_ERROR_TIMEOUT, null);
                        fetchFlag = false;
                        stopTimerTask();
                        msg = "";
                        Log.e(TAG, "timerwatchdog ::" + msg);

                    }
                });
            }
        };
    }

    private void stopTimerTask() {
        if(timer != null){
            timer.cancel();
            timer = null;

            timerFlag = false;
            fetchFlag = false;
            msg = "";
           Log.e(TAG, "stopTimerTask()::" + msg);
        }
    }

    public synchronized boolean startSession() {
        return sendRequest("{\"token\":0,\"msg_id\":" + AMBA_START_SESSION + "}");
    }

    public synchronized boolean standBy() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_POWER_MANAGE
                + ",\"param\":\"cam_stb\"}");
    }

    public synchronized boolean setClntInfo(String type, String param) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_SET_CLINT_INFO
                + ",\"type\":\"" + type + "\""
                + ",\"param\":\"" + param + "\"}");
    }

    public synchronized boolean stopSession() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_STOP_SESSION + "}");
    }

    public synchronized boolean setSetting(String setting) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_SET_SETTING
                + "," + setting + "}");
    }

    public synchronized boolean getSettingOptions(String setting) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_OPTIONS
                + ",\"param\":\"" + setting + "\"}");
    }

    public synchronized boolean getAllSettings() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_ALL + "}");
    }

    public synchronized boolean appStatus() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_SETTING +",\"type\": \"app_status\" }");
    }
    public synchronized boolean listDir(String dir) {
        if (!checkSessionID())
            return false;
        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_START_LS, null);
        return sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_LS
                + ",\"param\":\"" + dir + " -D -S\"}");
    }


    public synchronized boolean changeDirectory(String path) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_CD
                + ",\"param\":\"" + path + "\"}");
    }

    public synchronized boolean getCurrentWorkingDir() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_PWD + "}");
    }

    public synchronized boolean deleteFile(String path) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_DEL
                + ",\"param\":\"" + path + "\"}");
    }

    public synchronized boolean burnFW(String path) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_BURN_FW
                + ",\"param\":\"" + path + "\"}");
    }

    public synchronized boolean setBitRate(int bitRate) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_SET_BITRATE
                + ",\"param\":\"" + bitRate + "\"}");
    }

    public synchronized boolean getThumb(String path, String type) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_THUMB
                + ",\"param\":\"" + path + "\""
                + ",\"type\":\"" + type + "\"}");
    }

    public synchronized boolean getFile(String path) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_FILE
                + ",\"param\":\"" + path
                + "\",\"offset\":0,\"fetch_size\":0}");
    }

    public synchronized boolean getInfo(String path) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_MEDIAINFO
                + ",\"param\":\"" + path + "\"}");
    }

    public synchronized boolean setMediaAttribute(String path, int flag) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_SET_ATTRIBUTE
                + ",\"type\":" + flag
                + ",\"param\":\"" + path + "\"}");
    }

    public synchronized boolean putFile(String to, String md5, long size) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_PUT_FILE
                + ",\"param\":\"" + to + "\""
                + ",\"size\":" + size
                + ",\"md5sum\":\"" + md5
                + "\",\"offset\":0}");
    }

    public synchronized boolean resetViewfinder() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_RESETVF
                + ",\"param\":\"none_force\"}");
    }

    public synchronized boolean stopViewfinder() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_STOP_VF + "}");
    }

    public synchronized boolean takePhoto() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_TAKE_PHOTO + "}");
    }

    public synchronized boolean stopPhoto() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_STOP_PHOTO + "}");
    }

    public synchronized boolean startRecord() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_RECORD_START + "}");
    }

    public synchronized boolean stopRecord() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_RECORD_STOP + "}");
    }

    public synchronized boolean getRecordTime() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_RECORD_TIME + "}");
    }

    public synchronized boolean forceSplit() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_FORCE_SPLIT + "}");
    }

    public synchronized boolean getNumFiles(String type) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_NUM_FILES
                + ",\"type\":\"" + type + "\"}");
    }

    public synchronized boolean getSpace(String type) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_SPACE
                + ",\"type\":\"" + type + "\"}");
    }

    public synchronized boolean getDevInfo() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_DEVINFO + "}");
    }

    //No AlertView
    public synchronized boolean getDeviceInfo() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_DEVINFO + "}");
    }



    public synchronized boolean cancelGetFile(String path) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_CANCLE_XFER
                + ",\"param\":\"" + path + "\"}");
    }

    public synchronized boolean cancelPutFile(String path, int size) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_CANCLE_XFER
                + ",\"param\":\"" + path
                + "\",\"sent_size\":" + size + "}");
    }

    public synchronized boolean formatSD(String slot) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_FORMAT_SD
                + ",\"param\":\"" + slot + "\"}");
    }

    public synchronized boolean getBatteryLevel() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_BATTERY_LEVEL + "}");
    }

    public synchronized boolean setZoom(String type, int level) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_ZOOM
                + ",\"type\":\"" + type + "\""
                + ",\"param\":\"" + level + "\"}");
    }

    public synchronized boolean getZoomInfo(String type) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_ZOOM_INFO
                + ",\"type\":\"" + type + "\"}");
    }

    public synchronized boolean replyQuerySessionHolder() {
        return checkSessionID() && sendRequest("{\"msg_id\":" + AMBA_QUERY_SESSION_HOLDER + "}");
    }

    public synchronized boolean getAmbaWifiSettings() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_GET_WIFI_SETTING + " }");
    }
    public synchronized boolean setWifiSettings(String wifiConfig) {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"param\":\"" + wifiConfig + "\""
                + ",\"msg_id\":" + AMBA_SET_WIFI_SETTING + "}");
    }
    public synchronized boolean stopWifi() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_WIFI_STOP + "}");
    }
    public synchronized boolean startWifi() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_WIFI_START + "}");
    }
    public synchronized boolean restartWifi() {
        return checkSessionID() && sendRequest("{\"token\":" + mSessionId
                + ",\"msg_id\":" + AMBA_WIFI_RESTART + "}");
    }

    class QueueRunnable implements Runnable {
        private void handleNotification(String msg) {
            try {
                JSONObject parser = new JSONObject(msg);

                if (parser.getInt("msg_id") == AMBA_NOTIFICATION) {
                    String type = parser.getString("type");
                    if (type.equals("fw_upgrade_failed") ||
                            type.equals("fw_upgrade_complete")) {
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_SHOW_ALERT, type);
                    } else {
                        Log.e(CommonUtility.LOG_TAG, "unhandled notification " + type + "!!!");
                    }
                }
                if (parser.getInt("msg_id") == AMBA_QUERY_SESSION_HOLDER) {
                    mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_QUERY_SESSION_HOLDER, null);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        private void handleResponse(String msg) {
            try {
                JSONObject parser = new JSONObject(msg);
                int rval = parser.getInt("rval");
                int msgId = parser.getInt("msg_id");
                String str;

                switch (rval) {
                    case ERR_INVALID_TOKEN:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_ERROR_INVALID_TOKEN, null);
                        return;
                }

                switch (msgId) {
                    case AMBA_START_SESSION:
                        str = parser.getString("param");
                        Pattern p = Pattern.compile("\\d+");
                        Matcher m = p.matcher(str);
                        if (m.find())
                            mSessionId = Integer.parseInt(m.group(0));
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_START_SESSION,
                                mSessionId);
                        break;
                    case AMBA_STOP_SESSION:
                        mSessionId = 0;
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_STOP_SESSION,mSessionId);
                        break;
                    case AMBA_SET_SETTING:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_SET_SETTING, parser);
                        break;
                    case AMBA_GET_OPTIONS:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_OPTIONS, parser);
                        break;
                    case AMBA_GET_ALL:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_ALL_SETTINGS, parser);
                        break;
                    case AMBA_LS:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_LS, parser);
                        break;
                    case AMBA_DEL:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_DEL, null);
                        break;
                    case AMBA_GET_THUMB:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_THUMB, parser);
                        break;
                    case AMBA_GET_FILE:
                        int size = parser.getInt("size");
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_FILE,
                                Integer.toString(size));
                        break;
                    case AMBA_PUT_FILE:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_PUT_FILE, null);
                        break;
                    case AMBA_GET_MEDIAINFO:
                        str = (!parser.has("thumb_file")) ? "" :
                                "thumb: " + parser.getString("thumb_file");
                        if (parser.has("duration"))
                            str += "\nduration: " + parser.getString("duration");
                        str += "\nresolution: " + parser.getString("resolution")
                                + "\nsize: " + parser.getString("size")
                                + "\ndate: " + parser.getString("date")
                                + "\ntype: " + parser.getString("media_type");
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_INFO, str);
                        break;
                    case AMBA_GET_SPACE:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_SPACE, parser.getString("param"));
                        break;
                    case AMBA_GET_NUM_FILES:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_NUM_FILES, parser.getString("param"));
                        break;
                    case AMBA_GET_DEVINFO:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_DEVINFO, parser);
                        break;
                    case AMBA_RESETVF:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_RESETVF, null);
                        break;
                    case AMBA_STOP_VF:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_STOP_VF, null);
                        break;
                    case AMBA_RECORD_TIME:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_RECORD_TIME, parser.getString("param"));
                        break;
                    case AMBA_FORMAT_SD:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_FORMAT_SD, null);
                        break;
                    case AMBA_BATTERY_LEVEL:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_BATTERY_LEVEL,
                                "Battery Level: " + parser.getString("param"));
                        break;
                    case AMBA_SET_ATTRIBUTE:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_SET_ATTRIBUTE,
                                rval);
                        break;
                    case AMBA_ZOOM_INFO:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_ZOOM_INFO,
                                parser.getString("param"));
                        break;
                    case AMBA_ZOOM:
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_SET_ZOOM,
                                rval);
                        break;
                    case AMBA_GET_WIFI_SETTING:
                        str = parser.getString("param");
                        mListener.onChannelEvent(IChannelListener.CMD_CHANNEL_EVENT_GET_WIFI_SETTING, str);
                        break;
                }
            } catch (JSONException e) {
                Log.e(CommonUtility.LOG_TAG, "JSON Error: " + e.getMessage());
            }
        }

        public void run () {
            try {
                //String msg;
                if (fetchFlag) {
                    msg = "";
                    Log.e(TAG, "START NEW COMMAND");
                }
                while (true) {
                    msg = readFromChannel();
                    if (msg == null)
                        break;

                    // continue to read until we got a valid JSON message
                    //while (true) {
                    while (fetchFlag) {
                        try {
                            new JSONObject(msg);
                            //msg = msg.replaceAll(".*",msg);
                            break;
                        } catch (JSONException e) {
                            ////Log.i(TAG, "JSON segment: " + msg);
                            // Add Json Segment to logView
                            //addLog("JSON segment: " + msg + "<br >");
                            msg += readFromChannel();
                            //msg = msg.replaceAll(".*",msg);

                            try {
                                JSONObject parser = new JSONObject(msg);
                                mReplyReceived = true;
                                break;
                            } catch (JSONException e1) {  }
                            if (timerFlag == false)
                                break;
                        }
                    }
                    if (timerFlag) {
                        addLog("<font color=#cc0029>" + msg + "<br ></font>");
                        Log.e(TAG, msg);
                        if (msg.contains("rval")) {
                            handleResponse(msg);
                            mReplyReceived = true;
                            //msg = msg.replaceAll(".*", msg);
                            stopTimerTask();
                            synchronized (mRxLock) {
                                mRxLock.notify();
                                //reset the msg to nothing
                                ////if (timerFlag == false) {
                                ////    msg = "";
                                ////}
                            }
                        } else {
                            handleNotification(msg);
                            stopTimerTask();
                        }
                    } ////else {
                        ////msgRecv = msgRecv.replaceAll(".*", "");
                    ////}
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
