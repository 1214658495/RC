package com.ambarella.remotecamera.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ambarella.remotecamera.R;
import com.ambarella.remotecamera.RemoteCam;



/**
 * Created by Ram on 11/29/15.
 */
public class SetupWifiFragment extends Fragment {
    private IFragmentListener mListener;
    private RadioGroup mRadioConnectivity;
    private int mConnectivityType;
    private String mWifiSsidName;

    private TextView mTextViewWifi;
    EditText ipAddrField;

    public SetupWifiFragment() {}

    public SetupWifiFragment setConnectivityType(int type) {
        mConnectivityType = type;
        return this;
    }
    public SetupWifiFragment setWifiDevice(String name) {
        mWifiSsidName = name;
        if (mTextViewWifi != null)
            mTextViewWifi.setText(name);
        return this;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_setup,container, false);


        mTextViewWifi = (TextView) view.findViewById(R.id.textViewWifiSelectedDevice);
        mTextViewWifi.setText(mWifiSsidName);

        ImageButton listButton = (ImageButton) view.findViewById(R.id.imageButtonWifiList);
        listButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                mListener.onFragmentAction(IFragmentListener.ACTION_WIFI_LIST, null);
            }
        });

        mConnectivityType = RemoteCam.CAM_CONNECTIVITY_WIFI_WIFI;
        mListener.onFragmentAction(IFragmentListener.ACTION_CONNECTIVITY_SELECTED,
                mConnectivityType);
        mRadioConnectivity = (RadioGroup) view.findViewById(R.id.radioConnectivity);

        switch (mConnectivityType) {
            case RemoteCam.CAM_CONNECTIVITY_WIFI_WIFI:
                mRadioConnectivity.check(R.id.radioButtonWiWi);
                break;
        }
        //always  keep enabled
        mRadioConnectivity.setEnabled(true);
        mRadioConnectivity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkID) {
                mConnectivityType = checkID;
                switch (checkID) {
                    case R.id.radioButtonWiWi:
                        mConnectivityType = RemoteCam.CAM_CONNECTIVITY_WIFI_WIFI;
                        break;
                    default:
                        mConnectivityType = RemoteCam.CAM_CONNECTIVITY_INVALID;
                }
                mListener.onFragmentAction(IFragmentListener.ACTION_CONNECTIVITY_SELECTED,
                        mConnectivityType);
            }
        });

        ipAddrField = (EditText) view.findViewById(R.id.cameraIPAddressTextFieldId);
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ipAddrField.setText(mySharedPreferences.getString("remote_camera_ip_address", ""));

        Button connectbtn = (Button) view.findViewById(R.id.tcpConnectBtn);
        connectbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if (mListener != null ){
                    mListener.onFragmentAction(IFragmentListener.ACTION_SET_CAMERA_WIFI_IP, ipAddrField.getText().toString());
                }
            }
        });

        Button controlPanelBtn = (Button) view.findViewById(R.id.controlPanelBtn);
        controlPanelBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_SET_CAMERA_WIFI_IP, ipAddrField.getText().toString());
                    mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_CONTROLPANEL, null);
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement OnFragmentInteractionListener" );
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mTextViewWifi = null;
    }
    @Override
    public void onResume() {
        super.onResume();
    }

}
