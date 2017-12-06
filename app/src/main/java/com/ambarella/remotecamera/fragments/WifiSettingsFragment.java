package com.ambarella.remotecamera.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ambarella.remotecamera.R;


/**
 * Created by ram on 12/5/15.
 */
public class WifiSettingsFragment extends Fragment {

    private IFragmentListener mListener;
    private AlertDialog mWifiAlertDialog;

    EditText wifiText;
    Button getWificonfigBtn, updateWifiSettingsBtn, wifiStopBtn, wifiStartBtn,wifiRestartBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_wifi_settings, container, false);
        wifiText = (EditText) view.findViewById(R.id.wifieditTextId);
        wifiText = (EditText) view.findViewById(R.id.wifieditTextId);

        getWificonfigBtn = (Button) view.findViewById(R.id.getWificonfig);
        getWificonfigBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_GET_WIFI_SETTINGS, null);
                    //Toast.makeText(getActivity(), "Get Wifi Settings", Toast.LENGTH_LONG).show();
                }
            }

        });


        updateWifiSettingsBtn = (Button) view.findViewById(R.id.updateWifiSettings);
        updateWifiSettingsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (wifiText.length() > 0) {
                    if (mListener != null) {
                        mListener.onFragmentAction(IFragmentListener.ACTION_SET_WIFI_SETTINGS, wifiText.getText().toString());
                    }
                } else {
                    Toast.makeText(getActivity(), "!!! Empty Wifi Config !!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        wifiStopBtn = (Button) view.findViewById(R.id.wifiStop);
        wifiStopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ( mListener != null) {
                    //mListener.onFragmentAction(IFragmentListener.ACTION_WIFI_STOP, null);
                    showAlerWifitDialog("NOTE: Reload/Stop wifi will disconnect current session",
                            IFragmentListener.ACTION_WIFI_STOP);
                }
            }
        });

        wifiStartBtn = (Button) view.findViewById(R.id.wifiStart);
        wifiStartBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_WIFI_START, null);
                }
            }
        });

        wifiRestartBtn = (Button) view.findViewById(R.id.wifiReload);
        wifiRestartBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ( mListener != null ) {
                    //mListener.onFragmentAction(IFragmentListener.ACTION_WIFI_RESTART, null);
                    showAlerWifitDialog("NOTE: Reload/Stop wifi will disconnect current session",
                            IFragmentListener.ACTION_WIFI_RESTART);
                }
            }
        });

        return view;
    }


    public void updateWifiConfigOptions (String param) {
        wifiText.setText(param);
    }

    public void reset() {

    }

    @Override
    public void onDetach() {
        Log.e("WifiSettings:", "onDetach");
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.e("WifiSettings", "onAttach");
        super.onAttach(activity);
        try {
            mListener = (IFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IFragmentListener");
        }
    }

    private void showAlerWifitDialog( String msg, final int cmdId) {
        dismissDialog();
        mWifiAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("!!! Warning !!1")
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onFragmentAction(cmdId, null);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setIcon(android.R.drawable.ic_popup_sync).show();
    }

    private void dismissDialog() {
        if (mWifiAlertDialog != null) {
            mWifiAlertDialog.dismiss();
        }
    }
}