package com.ambarella.remotecamera.fragments;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ambarella.remotecamera.R;
import com.ambarella.remotecamera.RemoteCam;

import java.util.Set;

/**
 * Created by test on 12/2/15.
 */
public class SetupBLEFragment extends Fragment {
    private IFragmentListener mListener;
    private RadioGroup mRadioConnectivity;
    private int mConnectivityType;
    private String mBTDeviceName;
    private TextView mTextViewBT;



    public SetupBLEFragment() {}

    public SetupBLEFragment setConnectivityType(int type) {
        mConnectivityType = type;
        return this;
    }

    public SetupBLEFragment setBTDevice(String name) {
        mBTDeviceName = name;
        if (mTextViewBT != null)
            mTextViewBT.setText(name);
        return this;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ble_setup, container, false);

        mTextViewBT = (TextView) view.findViewById(R.id.textViewBTSelectedDevice);
        mTextViewBT.setText(mBTDeviceName);

        ImageButton listButton = (ImageButton) view.findViewById(R.id.imageButtonBTList);
        listButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // show Bluetooth list
                if (mListener != null) {
                    BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
                    if (!bta.isEnabled()) {
                        mListener.onFragmentAction(
                                IFragmentListener.ACTION_BT_ENABLE, null);
                        return;
                    }

                    if (mConnectivityType == RemoteCam.CAM_CONNECTIVITY_BLE_WIFI)
                        mListener.onFragmentAction(IFragmentListener.ACTION_BLE_LIST, null);
                    else
                        mListener.onFragmentAction(IFragmentListener.ACTION_BT_LIST, null);
                }
            }
        });

        mRadioConnectivity = (RadioGroup) view.findViewById(R.id.radioConnectivity);
        switch (mConnectivityType) {
            case RemoteCam.CAM_CONNECTIVITY_BLE:
                mRadioConnectivity.check(R.id.radioButtonBLE);
                break;
        }
        //always  keep enabled
        mRadioConnectivity.setEnabled(true);
        mRadioConnectivity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkID) {
                mConnectivityType = checkID;
                switch (checkID) {
                    case R.id.radioButtonBLE:
                        mConnectivityType = RemoteCam.CAM_CONNECTIVITY_BLE;
                        break;
                    default:
                        mConnectivityType = RemoteCam.CAM_CONNECTIVITY_INVALID;
                }
                mListener.onFragmentAction(IFragmentListener.ACTION_CONNECTIVITY_SELECTED,
                        mConnectivityType);
            }
        });

        Button controlPanelBtn = (Button) view.findViewById(R.id.controlPanelBtn);
        controlPanelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_CONTROLPANEL, null);
                }

            }
        });

        return view;
    }
}
