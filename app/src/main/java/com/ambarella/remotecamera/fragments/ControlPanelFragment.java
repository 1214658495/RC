package com.ambarella.remotecamera.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.media.effect.Effect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.ambarella.remotecamera.R;
import com.ambarella.remotecamera.RemoteCam;
import com.ambarella.remotecamera.connectivity.CmdChannelBLE;

/**
 * Created by test on 12/1/15.
 */
public class ControlPanelFragment  extends Fragment {
    private Boolean sessionStatus = false;
    private IFragmentListener mListener;

    private Button startCameraSession, stopCameraSession, cameraCommands, cameraFileOperations;
    private Button cameraSettings,cameraWifiSettings, setClientInfo, disconnectBtn, viewLogBtn;
    private TextView clientWifiIPTextField,clientBTAddressField;

    private String mClientWifiIPAddr,mClientBTAddr;

    private Switch  querySessionHolderSwitch;
    public ControlPanelFragment() {}

    public void setSessionStatus(Boolean flag) {
        sessionStatus = flag;
    }

    public ControlPanelFragment setClientWifiIP(String IpAddr) {
        mClientWifiIPAddr = IpAddr;
        if (clientWifiIPTextField != null)
            clientWifiIPTextField.setText(IpAddr);
        return this;
    }


    public ControlPanelFragment setClientBtAddr(String BTAddr) {
        mClientBTAddr = BTAddr;
        if (clientBTAddressField != null)
            clientBTAddressField.setText(BTAddr);
        return this;
    }
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_panel, container, false);

        startCameraSession = (Button) view.findViewById(R.id.startSessionBtnId);
        if (!sessionStatus) {
            startCameraSession.setEnabled(true);
        } else {
            startCameraSession.setEnabled(false);
        }
        startCameraSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_BC_START_SESSION, null);
                }
            }
        });

        stopCameraSession = (Button) view.findViewById(R.id.stopSessionBtnId);
        if (sessionStatus) {
            stopCameraSession.setEnabled(true);
        } else {
            stopCameraSession.setEnabled(false);
        }
        stopCameraSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_BC_STOP_SESSION, null);
                }
            }
        });

        cameraCommands = (Button) view.findViewById(R.id.cameraCommandsBtnId);
        if (sessionStatus) {
            cameraCommands.setEnabled(true);
        } else {
            cameraCommands.setEnabled(false);
        }
        cameraCommands.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null ) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_CAMERA_COMMANDS, null);
                }
            }
        });

        cameraFileOperations = (Button) view.findViewById(R.id.cameraFileOperationsBtnId);
        if (sessionStatus) {
            cameraFileOperations.setEnabled(true);
        } else {
            cameraFileOperations.setEnabled(false);
        }
        cameraFileOperations.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null ) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_CAMERA_FILE_CMDS, null);
                }
            }
        });

        cameraSettings = (Button) view.findViewById(R.id.cameraParameterSettingBtnId);
        if (sessionStatus) {
            cameraSettings.setEnabled(true);
        } else {
            cameraSettings.setEnabled(false);
        }
        cameraSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null ) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_CAMERA_SETTINGS, null);
                }
            }
        });

        cameraWifiSettings = (Button) view.findViewById(R.id.cameraWifiCommandsBtnId);
        if (sessionStatus) {
            cameraWifiSettings.setEnabled(true);
        } else {
            cameraWifiSettings.setEnabled(false);
        }
        cameraWifiSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null ) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_CAMERA_WIFI_SETTINGS, null);
                }
            }
        });

        clientWifiIPTextField = (TextView) view.findViewById(R.id.clientWifiIPTextFieldId);
        clientWifiIPTextField.setText(mClientWifiIPAddr);

        //TODO: Set Client INFO for BT device addr:
        clientBTAddressField = (TextView) view.findViewById(R.id.clientBTAddressField);
        clientBTAddressField.setText(mClientBTAddr);

        setClientInfo = (Button) view.findViewById(R.id.setClientInfoBtnId);
        if (sessionStatus) {
            setClientInfo.setEnabled(true);
        } else {
            setClientInfo.setEnabled(false);
        }
        setClientInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_BC_SET_CLIENT_INFO, null);
                } //connectToDataChannel will make a WIFI Data channel setting the client ip
            }
        });

        disconnectBtn = (Button) view.findViewById(R.id.disconnectBtnId);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionStatus) {
                    Toast.makeText(getActivity(), "Stop Session and then Disconnect from camera", Toast.LENGTH_LONG).show();
                } else {
                    //CLose and exit the app
                    //finish();
                    if (mListener != null) {
                        mListener.onFragmentAction(IFragmentListener.ACTION_CLOSE_BLE, null);
                        mListener.onFragmentAction(IFragmentListener.ACTION_CLOSE_EXTERNAL_LOG_FILE, null);
                    }

                    android.os.Process.killProcess(android.os.Process.myPid());
                    //System.exit(0);
                }
            }
        });
        viewLogBtn = (Button) view.findViewById(R.id.logViewBtnId);
        if (sessionStatus) {
            viewLogBtn.setEnabled(true);
        } else {
            viewLogBtn.setEnabled(true);//change to false if want to disable log before startsession
        }
        viewLogBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_LOG_VIEW, null);
                }
            }
        });

        querySessionHolderSwitch = (Switch) view.findViewById(R.id.querySessionHolderId);
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (mySharedPreferences.getBoolean("querry_session_holder_checkbox", true)) {
            querySessionHolderSwitch.setChecked(true);
        } else {
            querySessionHolderSwitch.setChecked(false);
        }

        querySessionHolderSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener () {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (mListener != null) {
                        mListener.onFragmentAction(IFragmentListener.ACTION_SET_QUERY_SESSION_HOLDER, null);
                    }
                    Toast.makeText( getActivity(),"Query Session Holder: SET", Toast.LENGTH_SHORT).show();
                } else {
                    if (mListener != null) {
                        mListener.onFragmentAction(IFragmentListener.ACTION_UNSET_QUERY_SESSION_HOLDER, null);
                    }
                    Toast.makeText(getActivity(),"Query Session Holder: UNSET", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;

    }
    @Override
    public void onAttach(Activity activity) {
        Log.e("ControlPanel:", "onAttach");
        super.onAttach(activity);
        try {
            mListener = (IFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IFragmentListener");
        }
    }
    @Override
    public void onDetach() {
        Log.e("ControlPanel:", "onDetach");
        super.onDetach();
        mListener = null;
    }
}
