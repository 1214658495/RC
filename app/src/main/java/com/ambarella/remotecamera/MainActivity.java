package com.ambarella.remotecamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.ambarella.remotecamera.connectivity.IChannelListener;
import com.ambarella.remotecamera.fragments.AppSettingsFragment;
import com.ambarella.remotecamera.fragments.CameraFragment;
import com.ambarella.remotecamera.fragments.CommandsFragment;
import com.ambarella.remotecamera.fragments.ControlPanelFragment;
import com.ambarella.remotecamera.fragments.IFragmentListener;
import com.ambarella.remotecamera.fragments.LogViewFragment;
import com.ambarella.remotecamera.fragments.MediaFragment;
import com.ambarella.remotecamera.fragments.NetworkModeSelectFrag;
import com.ambarella.remotecamera.fragments.SettingsFragment;
import com.ambarella.remotecamera.fragments.SetupBLEFragment;
import com.ambarella.remotecamera.fragments.SetupBLEWIFIFragment;
import com.ambarella.remotecamera.fragments.BLESelectFragment;
import com.ambarella.remotecamera.fragments.SetupWifiFragment;
import com.ambarella.remotecamera.fragments.WifiSettingsFragment;
import com.ipaulpro.afilechooser.FileChooserActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements
        NetworkModeSelectFrag.NetworkModeSelectFragmentListener,
        IFragmentListener, IChannelListener
{

    private static final String TAG="MainActivity::";
    private static final int REQUEST_CODE_UPLOAD = 6384;
    private static final int REQUEST_CODE_WIFI_SETTINGS = 6385;

    final static String KEY_CONNECTIVITY_TYPE = "connectivity_type";
    final static String KEY_SELECTED_MODULE = "selected_module";
    final static String KEY_SELECTED_BT_DEVICE_NAME = "selected_bt_device_name";
    final static String KEY_SELECTED_BT_DEVICE_ADDR = "selected_bt_device_addr";

    private int mFragContainerId;
    private int mSessionId;

    private int mConnectivityType;
    private SharedPreferences mPref;
    private String mBTDeviceName;
    private String mBTDeviceAddr;
    private String mWifiSsidName;
    private String mGetFileName;
    private String mPutFileName;
    private RemoteCam mRemoteCam;
    private ListView mListViewDrawer;
    private DrawerLayout mDrawerLayout;
    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;
    private boolean mIsPreview;
    //getram
    public String mFormatSdParam;

    private WifiStatusReceiver mWifiReceiver = new WifiStatusReceiver();

    static private CameraFragment mCameraFrag = new CameraFragment();
    static private LiveViewWithAudio mLiveAudioViewFrag = new LiveViewWithAudio();
    //static private SetupFragment    mSetupFrag = new SetupFragment();
    static private SetupWifiFragment    mSetupWifiFrag = new SetupWifiFragment();
    static private ControlPanelFragment mControlPanelFrag = new ControlPanelFragment();
    static private CommandsFragment mCameraCommandsFrag = new CommandsFragment();
    static private SettingsFragment mSettingsFrag = new SettingsFragment();
    static private LogViewFragment mLogViewFrag = new LogViewFragment();
    static private MediaFragment mMediaFrag = new MediaFragment();
    static private SetupBLEWIFIFragment  mSetupBLEWIFIFragment = new SetupBLEWIFIFragment();
    static private SetupBLEFragment mSetupBLEFragment = new SetupBLEFragment();
    static private WifiSettingsFragment mWifiSettingsFragment = new WifiSettingsFragment();
    ////static private AppSettingsFragment mAppSettings = new AppSettingsFragment();



    static private int mSelectedModule = ModuleContent.MODULE_POSE_NONE;
    private boolean saveLogToExternalStorage;
    private String  extrnal_path;
    private FileOutputStream external_logFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPref = getPreferences(Context.MODE_PRIVATE);
        getPrefs(mPref);

        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mRemoteCam = new RemoteCam(this);
        mRemoteCam.setChannelListener(this)
                .setBtDeviceAddr(mBTDeviceAddr)
                .setConnectivity(mConnectivityType)
                .setWifiInfo(wifiManager.getConnectionInfo().getSSID().replace("\"", ""),
                        getWifiIpAddr());
        mMediaFrag.setRemoteCam(mRemoteCam);

        mRemoteCam.setQuerySessionFlag(true);

        NetworkModeSelectFrag frag = new NetworkModeSelectFrag();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction tr = fm.beginTransaction();
        tr.add(R.id.fragment_placeholder,frag);
        tr.addToBackStack(null);
        tr.commit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuId;
        switch(mSelectedModule) {
            case ModuleContent.MODULE_POS_MEDIA:
                menuId = R.menu.menu_action_media;
                break;
            case ModuleContent.MODULE_POS_SETUP:
                menuId = R.menu.menu_action_setup;
                break;
            case ModuleContent.MODULE_POS_SETTINGS:
                menuId = R.menu.menu_action_settings;
                break;
            case ModuleContent.MODULE_POSE_NONE:
                menuId = R.menu.menu_item_none;
                break;
            case ModuleContent.MODULE_POSE_WIFI_SETTINGS:
                menuId = R.menu.menu_item_none;
                break;
            default:
                return false;
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(menuId, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemParentDir:
                mMediaFrag.goParentDir();
                return true;
            case R.id.itemMediaRefresh:
                mMediaFrag.refreshDirContents();
                return true;
            case R.id.itemFormatSD:
                mMediaFrag.formatSD();
                return true;
            case R.id.itemUpload:
                Intent intent = new Intent(this, FileChooserActivity.class);
                startActivityForResult(intent, REQUEST_CODE_UPLOAD);
                return true;

            case R.id.itemDevInfo:
                mRemoteCam.getMediaInfo();
                return true;
            case R.id.itemBatteryLevel:
                mRemoteCam.getBatteryLevel();
                return true;

            case R.id.itemSettingsRefresh:
                mSettingsFrag.refreshSettings();
                return true;

            case R.id.item_info:
                new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage("Ambarella Remote Camera Debug App. For additional info contact www.Ambarella.com")
                        .setPositiveButton("OK", null)
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_UPLOAD:
                // If the file selection was successful.
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        final String srcPath = uri.getPath();
                        final String fileName = srcPath.substring(srcPath.lastIndexOf('/')+1);
                        mPutFileName = mMediaFrag.getPWD() + fileName;
                        mRemoteCam.putFile(srcPath, mPutFileName);
                    }
                }
                break;
            case REQUEST_CODE_WIFI_SETTINGS:
                dismissDialog();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick_network_type(String netType) {

        if (netType.equals("WIFI")){
            Log.e(TAG, "AMBA:WiFi setup frag");
            mSetupWifiFrag.setConnectivityType(mConnectivityType)
                    .setWifiDevice(mWifiSsidName);
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, mSetupWifiFrag)
                    .commit();

        }
        if (netType.equals("BLE")) {
            Log.e(TAG, "AMBA:BLE_WIFI setup frag");
            mSetupBLEFragment.setConnectivityType(mConnectivityType)
                    .setBTDevice(mBTDeviceName);
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, mSetupBLEFragment)
                    .commit();

        }
        if (netType.equals("BLE+WIFI")) {
            Log.e(TAG, "AMBA:BLE_WIFI setup frag");
            presentBLEWifiSetupFragment();

        }
        if (netType.equals("app_pref")) {
            appSettings();
        }
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        saveLogToExternalStorage = mySharedPreferences.getBoolean("save_external_session_log_checkbox", false);
        if (saveLogToExternalStorage) {
            extrnal_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/" + "AmbaRemoteCamlog.txt";
            try {
                external_logFile = new FileOutputStream(extrnal_path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String getWifiIpAddr() {
        WifiManager mgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        int ip = mgr.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d",
                (ip & 0xFF), (ip >> 8 & 0xFF), (ip >> 16 & 0xFF), ip >> 24);
    }

    private String getLocalBTHWAddress() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null){
            Log.d(TAG, "device does not support bluetooth");
            return null;
        }

        return mBluetoothAdapter.getAddress();
    }


    private void getPrefs(SharedPreferences preferences) {
        mConnectivityType = mPref.getInt(KEY_CONNECTIVITY_TYPE, RemoteCam.CAM_CONNECTIVITY_WIFI_WIFI);
        //mSelectedModule = mPref.getInt(KEY_SELECTED_MODULE, ModuleContent.MODULE_POS_SETUP);
        mBTDeviceName = mPref.getString(KEY_SELECTED_BT_DEVICE_NAME, "");
        mBTDeviceAddr = mPref.getString(KEY_SELECTED_BT_DEVICE_ADDR, "00:00:00:00:00:00");
    }

    private void putPrefs(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_CONNECTIVITY_TYPE, mConnectivityType);
        //editor.putInt(KEY_SELECTED_MODULE, mSelectedModule);
        editor.putString(KEY_SELECTED_BT_DEVICE_NAME, mBTDeviceName);
        editor.putString(KEY_SELECTED_BT_DEVICE_ADDR, mBTDeviceAddr);
        editor.commit();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mWifiReceiver);
        putPrefs(mPref);
        super.onPause();
    }
    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        //filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filter);
        super.onResume();
    }

    private void presentBLEWifiSetupFragment() {
        mSetupBLEWIFIFragment.setConnectivityType(mConnectivityType)
                .setBTDevice(mBTDeviceName)
                .setWifiDevice(mWifiSsidName);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, mSetupBLEWIFIFragment)
                .commit();

    }
    public void openControlPanel() {
        mSelectedModule = ModuleContent.MODULE_POS_SETUP;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, mControlPanelFrag)
                .commit();
        invalidateOptionsMenu();
    }
    public void openCommandsFragment() {
        //Toast.makeText(getApplicationContext(), "commandsFragment", Toast.LENGTH_SHORT).show();

        mSelectedModule = ModuleContent.MODULE_POSE_NONE;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, mCameraCommandsFrag)
                .commit();
        invalidateOptionsMenu();
    }
    public void openSettingsFragment() {

        mSelectedModule = ModuleContent.MODULE_POS_SETTINGS;

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, mSettingsFrag)
                .commit();
        invalidateOptionsMenu();
    }
    public void openWifiSettingsFragment() {
        mSelectedModule = ModuleContent.MODULE_POSE_WIFI_SETTINGS;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder,mWifiSettingsFragment)
                .commit();
        invalidateOptionsMenu();
    }
    public void openLogView() {

        mSelectedModule = ModuleContent.MODULE_POS_SETUP;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, mLogViewFrag)
                .commit();
        invalidateOptionsMenu();
    }

    private void openCameraFragment() {

        mSelectedModule = ModuleContent.MODULE_POS_SETUP;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, mCameraFrag)
                .commit();
        invalidateOptionsMenu();
    }

    private void openMediaFragment() {
        mSelectedModule = ModuleContent.MODULE_POS_MEDIA;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, mMediaFrag)
                .commit();
        invalidateOptionsMenu();
    }

    private void appSettings() {
       /* mSelectedModule = ModuleContent.MODULE_POSE_NONE;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder,mAppSettings)
                .commit();
        invalidateOptionsMenu();
        appSettingsFlag = 1;
        */
    }

    @Override
    public void onFragmentAction(int type, Object param, Integer... array) {
        Intent intent;
        switch (type) {

            case IFragmentListener.ACTION_CONNECTIVITY_SELECTED:
                mConnectivityType = (Integer)param;
                resetRemoteCamera();
                mRemoteCam.setConnectivity(mConnectivityType);
                break;
            case IFragmentListener.ACTION_WIFI_LIST:
                intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(intent, REQUEST_CODE_WIFI_SETTINGS);
                break;
            case IFragmentListener.ACTION_SET_CAMERA_WIFI_IP:  //Getram
                mRemoteCam.setWifiIP((String) param, 7878, 8787);
                break;
            case IFragmentListener.ACTION_OPEN_CONTROLPANEL:
                mControlPanelFrag.setClientBtAddr(getLocalBTHWAddress());
                openControlPanel();
                break;
            case IFragmentListener.ACTION_BC_START_SESSION:
                SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                mRemoteCam.ble_connection_timeout = Integer.parseInt(mySharedPreferences.getString("ble_connection_retry_timeout","10"));
                mRemoteCam.gatt_autoConnect_flag = mySharedPreferences.getBoolean("ble_gatt_auto_reconnect_checkbox",false);
                mRemoteCam.startSession();
                break;
            case IFragmentListener.ACTION_BC_STOP_SESSION:
                mRemoteCam.stopSession();
                break;
            case IFragmentListener.ACTION_BC_SEND_COMMAND:
                mRemoteCam.sendCommand((String) param);
                break;
            case IFragmentListener.ACTION_SET_QUERY_SESSION_HOLDER:
                mRemoteCam.setQuerySessionFlag(true);
                break;
            case IFragmentListener.ACTION_UNSET_QUERY_SESSION_HOLDER:
                mRemoteCam.setQuerySessionFlag(false);
                break;
            case IFragmentListener.ACTION_OPEN_CAMERA_COMMANDS:
                openCommandsFragment();
                break;
            case IFragmentListener.ACTION_OPEN_CAMERA_LIVEVIEW:
                openCameraFragment();
                break;
            case IFragmentListener.ACTION_OPEN_CAMERA_FILE_CMDS:
                openMediaFragment();
                break;
            case IFragmentListener.ACTION_OPEN_CAMERA_SETTINGS:
                openSettingsFragment();
                break;
            case IFragmentListener.ACTION_OPEN_CAMERA_WIFI_SETTINGS:
                openWifiSettingsFragment();
                break;
            case IFragmentListener.ACTION_OPEN_LOG_VIEW:
                openLogView();
                break;
            case IFragmentListener.ACTION_BC_SET_CLIENT_INFO:
                mRemoteCam.setClientInfo();
                break;
            case IFragmentListener.ACTION_DISC_SPACE:
                mRemoteCam.getTotalDiskSpace();
                break;
            case IFragmentListener.ACTION_DISC_FREE_SPACE:
                mRemoteCam.getTotalFreeSpace();
                break;
            case IFragmentListener.ACTION_APP_STATUS:
                mRemoteCam.appStatus();
                break;
            case IFragmentListener.ACTION_DEVICE_INFO:
                mRemoteCam.getDeviceInfo();
                break;
            case IFragmentListener.ACTION_BATTERY_INFO:
                mRemoteCam.getBatteryLevel();
                break;
            case IFragmentListener.ACTION_BC_GET_CURRENT_SETTING:
                mRemoteCam.getAllSettings();
                break;
            case IFragmentListener.ACTION_BC_GET_ALL_SETTINGS:
                showWaitDialog("Fetching Settings Info");
                mRemoteCam.getAllSettings();
                break;
            case IFragmentListener.ACTION_BC_GET_ALL_SETTINGS_DONE:
                dismissDialog();
                break;
            case IFragmentListener.ACTION_BC_GET_SETTING_OPTIONS:
                mRemoteCam.getSettingOptions((String) param);
                break;
            case IFragmentListener.ACTION_BC_SET_SETTING:
                mRemoteCam.setSetting((String) param);
                break;
            case IFragmentListener.ACTION_BC_SET_BITRATE:
                mRemoteCam.setBitRate((Integer) param);
                break;
            case IFragmentListener.ACTION_PHOTO_START:
                mRemoteCam.takePhoto();
                break;
            case IFragmentListener.ACTION_PHOTO_STOP:
                mRemoteCam.stopPhoto();
            case IFragmentListener.ACTION_RECORD_START:
                mRemoteCam.startRecord();
                mCameraFrag.startRecord();
                break;
            case IFragmentListener.ACTION_RECORD_STOP:
                mRemoteCam.stopRecord();
                mCameraFrag.stopRecord();
                break;
            case IFragmentListener.ACTION_RECORD_TIME:
                mRemoteCam.getRecordTime();
                break;
            case IFragmentListener.ACTION_FORCE_SPLIT:
                mRemoteCam.forceSplit();
                break;
            case IFragmentListener.ACTION_VF_START:
                mRemoteCam.startVF();
                break;
            case IFragmentListener.ACTION_VF_STOP:
                mRemoteCam.stopVF();
                break;
            case IFragmentListener.ACTION_PLAYER_START:
                mRemoteCam.startLiveStream();
                break;
            case IFragmentListener.ACTION_PLAYER_STOP:
                mRemoteCam.stopLiveStream();
                mCameraFrag.stopStreamView();
                break;
            case IFragmentListener.ACTION_FS_GET_FILE_INFO:
                mRemoteCam.getMediaInfo();
                break;
            case IFragmentListener.ACTION_FS_GET_ALL_FILE_COUNT:
                mRemoteCam.getTotalFileCount();
                break;
            case IFragmentListener.ACTION_FS_GET_ALL_VIDEO_FILES:
                mRemoteCam.getAllVideoFilesCount();
                break;
            case IFragmentListener.ACTION_FS_GET_ALL_PHOTO_FILES:
                mRemoteCam.getAllPhotoFilesCount();
                break;
            case IFragmentListener.ACTION_FS_FORMAT_SD:

                mFormatSdParam = (String)param;
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Are you sure to format SD card?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mRemoteCam.formatSD((String)mFormatSdParam);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
                break;
            case IFragmentListener.ACTION_FS_GET_PWD:
                mRemoteCam.getPWD();
                break;
            case IFragmentListener.ACTION_FS_LS:
                mRemoteCam.listDir((String) param);
                break;
            case IFragmentListener.ACTION_FS_CD:
                mRemoteCam.changeFolder((String) param);
                break;
            case IFragmentListener.ACTION_FS_DELETE:
                mRemoteCam.deleteFile((String)param);
                break;
            case IFragmentListener.ACTION_FS_DOWNLOAD:
                mGetFileName = (String)param;
                mRemoteCam.getFile(mGetFileName);
                break;
            case IFragmentListener.ACTION_FS_INFO:
                mRemoteCam.getInfo((String)param);
                break;
            case IFragmentListener.ACTION_FS_SET_RO:
                mRemoteCam.setMediaAttribute((String)param, 0);
                break;
            case IFragmentListener.ACTION_FS_SET_WR:
                mRemoteCam.setMediaAttribute((String)param, 1);
                break;
            case IFragmentListener.ACTION_FS_GET_THUMB:
                mRemoteCam.getThumb((String)param);
                break;
            /*case IFragmentListener.ACTION_FS_BURN_FW:
                mRemoteCam.burnInFw((String) param);
                break;*/
            case IFragmentListener.ACTION_FS_VIEW:
                String path = (String) param;
                if (path.endsWith(".jpg")) {
                    mIsPreview = true;
                    mRemoteCam.getFile(path);
                } else {
                    String uri = mRemoteCam.streamFile(path);
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(uri), "video/mp4");
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(this)
                                .setTitle("Warning")
                                .setMessage("You don't have any compatible video player installed in your device. " +
                                        "Please install one (such as RTSP player) first.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
                break;

            case IFragmentListener.ACTION_GET_ZOOM_INFO:
                mRemoteCam.getZoomInfo((String)param);
                break;
            case IFragmentListener.ACTION_SET_ZOOM:
                mRemoteCam.setZoom((String)param, array[0]);
                break;

            case IFragmentListener.ACTION_BT_LIST:
               /* getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_placeholder, new BTSelectFragment())
                        .commit();
                break;*/
                break;
            case IFragmentListener.ACTION_BLE_LIST:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_placeholder, new BLESelectFragment())
                        .commit();
                break;

            case IFragmentListener.ACTION_BT_SELECTED:
                String device[] = (String[]) param;
                if (!mBTDeviceAddr.equals(device[1])) {
                    mBTDeviceName = device[0];
                    mBTDeviceAddr = device[1];
                    resetRemoteCamera();
                    mRemoteCam.setBtDeviceAddr(mBTDeviceAddr);
                }
            case IFragmentListener.ACTION_BT_CANCEL:
                presentBLEWifiSetupFragment();
                break;
            case IFragmentListener.ACTION_BT_ENABLE:
                startBluetoothSettings();
                break;
            case IFragmentListener.ACTION_SHOW_LAST_CMD_RESP:
                String debgString;
                debgString = mRemoteCam.lastCommandResponse;
                debgString = debgString.replaceAll("<font color=#cc0029>", "[");
                debgString = debgString.replaceAll("<br ></font>", "]");
                Toast.makeText(getApplicationContext(), debgString , Toast.LENGTH_SHORT).show();
                break;

            case IFragmentListener.ACTION_GET_WIFI_SETTINGS:
                mRemoteCam.getWifiSettings();
                break;
            case IFragmentListener.ACTION_SET_WIFI_SETTINGS:
                mRemoteCam.setWifiSettings((String) param);
                break;
            case IFragmentListener.ACTION_WIFI_STOP:
                mRemoteCam.stopWifi();
                break;
            case IFragmentListener.ACTION_WIFI_START:
                mRemoteCam.startWifi();
                break;
            case IFragmentListener.ACTION_WIFI_RESTART:
                mRemoteCam.restartWifi();
                break;
            case IFragmentListener.ACTION_CLOSE_BLE:
                mRemoteCam.closeBLEConnection();
                break;
            case IFragmentListener.ACTION_FS_BURN_FW:
                mRemoteCam.burnFW((String) param);
                break;
            case IFragmentListener.ACTION_CLOSE_EXTERNAL_LOG_FILE:
                if(saveLogToExternalStorage){
                    try {
                        external_logFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

        }
    }

    //wifi related
    private class WifiStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                String ssid;
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getType() != ConnectivityManager.TYPE_WIFI)
                    return;
                Log.e(TAG, "wifi intent " + info.toString());
                Log.e(TAG, "wifi intent " + info.toString());
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    ssid = info.getExtraInfo().replaceAll("\"", "");
                } else {
                    ssid = "Invalid";
                }
                if (!ssid.equals(mWifiSsidName)) {
                    mWifiSsidName = ssid;
                    resetRemoteCamera();
                    mSetupWifiFrag.setWifiDevice(mWifiSsidName);
                    mRemoteCam.setWifiInfo(mWifiSsidName, getWifiIpAddr());
                    //getram
                    mControlPanelFrag.setClientWifiIP(getWifiIpAddr());
                }
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (mSessionId != 0) {
            Toast.makeText(getApplicationContext(), "< Control Panel >", Toast.LENGTH_SHORT).show();
            openControlPanel();
        } else {
            Log.e(TAG, "kill the process to force fresh launch next time");
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }


    /**
     * IChannelListener
     */
    public void onChannelEvent(final int type, final Object param, final String...array) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type & IChannelListener.MSG_MASK) {
                    case IChannelListener.CMD_CHANNEL_MSG:
                        handleCmdChannelEvent(type, param, array);
                        return;
                    case IChannelListener.DATA_CHANNEL_MSG:
                        handleDataChannelEvent(type, param);
                        return;
                    case IChannelListener.STREAM_CHANNEL_MSG:
                        handleStreamChannelEvent(type, param);
                        return;
                }
            }
        });
    }

    private void handleCmdChannelEvent(int type, Object param, String...array) {
        if (type >= 80) {
            handleCmdChannelError(type, param);
            return;
        }

        switch(type) {

            case IChannelListener.CMD_CHANNEL_EVENT_START_SESSION:
                mSessionId = (Integer)param;
                //mCommandsFrag.setSessionId(mSessionId);
                if (mSessionId > 0 ) {
                    Toast.makeText(getApplicationContext(), "< Session Start >", Toast.LENGTH_SHORT).show();
                    mControlPanelFrag.setSessionStatus(true);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(mControlPanelFrag).attach(mControlPanelFrag).commit();
                } else {
                    mControlPanelFrag.setSessionStatus(false);
                }
                mLogViewFrag.setSessionId(mSessionId);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_STOP_SESSION:
                mSessionId = (Integer)param;
                if (mSessionId != 0) {
                    Toast.makeText(getApplicationContext(), "< !! SessionClose:FAIL !! >", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "< !! SessionClosed !! >", Toast.LENGTH_SHORT).show();
                    mControlPanelFrag.setSessionStatus(false);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(mControlPanelFrag).attach(mControlPanelFrag).commit();
                    mSessionId = 0;
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_SHOW_ALERT:
                showAlertDialog("Warning", (String)param);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_LOG:
                LogViewFragment.addLog((String) param);
                if(saveLogToExternalStorage){
                    String logString = param+"\r\n";
                    try {
                        external_logFile.write(((String) logString).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mRemoteCam.debugLastCmdResponse((String) param);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_LS:
                dismissDialog();
                mMediaFrag.updateDirContents((JSONObject) param);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_SET_ATTRIBUTE:
                showAlertDialog("Info",
                        ((int)param != 0) ? "Set_Attribute failed" : "Set_Attribute OK");
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_FORMAT_SD:
                mMediaFrag.showSD();
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_DEL:
                mMediaFrag.refreshDirContents();
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_BATTERY_LEVEL:
            case IChannelListener.CMD_CHANNEL_EVENT_GET_INFO:
            case IChannelListener.CMD_CHANNEL_EVENT_GET_DEVINFO:
                //getram
                if (getFragmentManager().findFragmentById(R.id.fragment_placeholder) instanceof CommandsFragment)
                    dismissDialog();
                else
                    showAlertDialog("Info", (String) param);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_RESETVF:
                //getram
                // rest vf from CameraFragment
                ////mCameraFrag.onVFReset();
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_STOP_VF:
                //getram
                //do this only from CameraFragment
                ////mCameraFrag.onVFStopped();
                break;

            case IChannelListener.CMD_CHANNEL_EVENT_RECORD_TIME:
                ////mCameraFrag.upDateRecordTime((String)param);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_SET_ZOOM:
                mCameraFrag.setZoomDone((Integer)param);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_QUERY_SESSION_HOLDER:
                mRemoteCam.actionQuerySessionHolder();
                break;

            case IChannelListener.CMD_CHANNEL_EVENT_GET_WIFI_SETTING:
                mWifiSettingsFragment.updateWifiConfigOptions((String) param);
                break;

            case IChannelListener.CMD_CHANNEL_EVENT_GET_ZOOM_INFO:
                mCameraFrag.setZoomInfo((String)param, array[0]);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_GET_ALL_SETTINGS:
                JSONObject parser = (JSONObject)param;
                try {
                    if (parser.getInt("rval") < 0)
                        showAlertDialog("Warning", "Setting is not support by remote camera !");
                    else {
                        if (getFragmentManager().findFragmentById(R.id.fragment_placeholder) instanceof CommandsFragment)
                            dismissDialog();
                        else
                            mSettingsFrag.updateAllSettings(parser);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_GET_OPTIONS:
                mSettingsFrag.updateSettingOptions((JSONObject)param);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_SET_SETTING:
                mSettingsFrag.updateSettingNotification((JSONObject)param);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_START_CONNECT:
                showWaitDialog("Connecting to Remote Camera");
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_START_LS:
                showWaitDialog("Fetching Directory Info");
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_WAKEUP_START:
                showWaitDialog("Waking up the Remote Camera");
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_CONNECTED:
            case IChannelListener.CMD_CHANNEL_EVENT_WAKEUP_OK:
                dismissDialog();
                break;

        }
    }

    private void handleCmdChannelError(int type, Object param) {
        switch (type) {
            case IChannelListener.CMD_CHANNEL_ERROR_INVALID_TOKEN:
                showAlertDialog("Error", "Invalid Session! Please start session first!");
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_TIMEOUT:
                showAlertDialog("Error", "Timeout! No response from Remote Camera!");
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_BLE_INVALID_ADDR:
                showAlertDialog("Error", "Invalid bluetooth device");
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_BLE_DISABLED:
                startBluetoothSettings();
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_BROKEN_CHANNEL:
                showAlertDialog("Error", "Lost connection with Remote Camera!");
                resetRemoteCamera();
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_CONNECT:
                showAlertDialog("Error",
                        "Cannot connect to the Camera. \n" +
                                "Please make sure the selected camera is on. \n" +
                                "If problem persists, please reboot both camera and this device.");
                break;
            case IChannelListener.CMD_CHANNEL_ERROR_WAKEUP:
                showAlertDialog("Error", "Cannot wakeup the Remote Camera");
                break;
        }
    }

    private void handleDataChannelEvent(int type, Object param) {
        switch(type) {
            case IChannelListener.DATA_CHANNEL_EVENT_GET_START:
                String str = mIsPreview ? "Please wait ..." : "Downloading ,,,";
                showProgressDialog(str,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface Dialog, int which) {
                                mRemoteCam.cancelGetFile(mGetFileName);
                            }
                        });
                break;
            case IChannelListener.DATA_CHANNEL_EVENT_GET_PROGRESS:
                mProgressDialog.setProgress((Integer) param);
                break;
            case IChannelListener.DATA_CHANNEL_EVENT_GET_FINISH:
                String path = (String)param;
                if (!mIsPreview) {
                    showAlertDialog("Info", "Downloaded to " + path);
                    mGetFileName = null;
                } else {
                    dismissDialog();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + path), "image/*");
                    startActivity(intent);
                    mIsPreview = false;
                }
                break;

            case IChannelListener.DATA_CHANNEL_EVENT_PUT_MD5:
                showWaitDialog("Calculating MD5");
                break;
            case IChannelListener.DATA_CHANNEL_EVENT_PUT_START:
                showProgressDialog("Uploading...",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface Dialog, int which) {
                                mRemoteCam.cancelPutFile(mPutFileName);
                            }
                        });
                break;
            case IChannelListener.DATA_CHANNEL_EVENT_PUT_PROGRESS:
                mProgressDialog.setProgress((Integer) param);
                break;
            case IChannelListener.DATA_CHANNEL_EVENT_PUT_FINISH:
                showAlertDialog("Info", "Uploaded to " + mPutFileName);
                mPutFileName = null;
                mMediaFrag.refreshDirContents();
                break;
        }
    }

    private void handleStreamChannelEvent(int type, Object param) {
        switch(type) {
            case IChannelListener.STREAM_CHANNEL_EVENT_BUFFERING:
                showWaitDialog("Buffering...");
                break;
            case IChannelListener.STREAM_CHANNEL_EVENT_PLAYING:
                dismissDialog();
                mCameraFrag.startStreamView();
                break;
            case IChannelListener.STREAM_CHANNEL_ERROR_PLAYING:
                mRemoteCam.stopLiveStream();
                mCameraFrag.resetStreamView();
                showAlertDialog("Error", "Cannot connect to LiveView!");
                break;
        }
    }
    private void startBluetoothSettings() {
        dismissDialog();
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("Bluetooth is disabled currently. \nPlease turn it on first.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent  intent = new
                                Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intent);
                    }
                })
                .show();
    }

    // Misc Operations
    /**
     * reset RemoteCam due to:
     *     1. Lost of connections.
     *     2. Use selected a different camera
     */
    private void resetRemoteCamera() {
        mRemoteCam.reset();
        mCameraFrag.reset();
        mMediaFrag.reset();
        mLogViewFrag.reset();
        mCameraCommandsFrag.reset();
        mSettingsFrag.reset();
        mWifiSettingsFragment.reset();
    }

    private void dismissDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }

    private void showAlertDialog(String title, String msg) {
        dismissDialog();
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showWaitDialog(String msg) {
        dismissDialog();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("PLEASE WAIT ...");
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void showProgressDialog(String title,
                                    DialogInterface.OnClickListener listener) {
        dismissDialog();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancel", listener);
        mProgressDialog.show();
    }
}
