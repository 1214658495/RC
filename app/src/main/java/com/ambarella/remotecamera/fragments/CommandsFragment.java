package com.ambarella.remotecamera.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ambarella.remotecamera.LiveViewWithAudio;
import com.ambarella.remotecamera.R;
/**
 * Created by test on 12/1/15.
 */
public class CommandsFragment extends Fragment {

    private IFragmentListener mListener;

    public void reset() {

    }
    
    Button shutterBtn, recBtn, stopRecBtn, recTimeBtn, splitRecBtn;
    Button resetVFBtn, stopVFBtn, bitrateBtn,viewLogBtn;
    Button viewRTSPBtn, diskSpaceBtn, freeBtn, appStatusBtn, viewRtspWithAudioBtn;
    Button deviceInfoBtn, batteryInfoBtn, cameraSettingsBtn, showDebug;
    EditText bitrateField;


    public CommandsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_commands, container, false) ;

        shutterBtn = (Button) view.findViewById(R.id.takePhotoBtnId);
        shutterBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_PHOTO_START, null);
                }
            }
        });
        recBtn = (Button) view.findViewById(R.id.startRecBtnId);
        recBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_RECORD_START, null);
                }
            }
        });
        stopRecBtn = (Button) view.findViewById(R.id.stopRecBtnId);
        stopRecBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_RECORD_STOP, null);
                }
            }
        });
        recTimeBtn = (Button) view.findViewById(R.id.recordingTimeBtnId);
        recTimeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_RECORD_TIME, null);
                }
            }
        });
        splitRecBtn = (Button) view.findViewById(R.id.splitRecordingBtnId);
        splitRecBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_FORCE_SPLIT, null);
                }
            }
        });
        resetVFBtn = (Button) view.findViewById(R.id.resetVFBtnId);
        resetVFBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_VF_START, null);
                }
            }
        });

        viewRTSPBtn = (Button) view.findViewById(R.id.viewRTSPStreamBtnId);
        viewRTSPBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_CAMERA_LIVEVIEW, null);
                }
            }

        });
        stopVFBtn = (Button) view.findViewById(R.id.stopVFBtnId);
        stopVFBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_VF_STOP, null);
                }
            }
        });
        diskSpaceBtn = (Button) view.findViewById(R.id.diskSpaceBtnId);
        diskSpaceBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_DISC_SPACE, null);
                }
            }
        });
        freeBtn = (Button) view.findViewById(R.id.freeSpaceBtnId);
        freeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_DISC_FREE_SPACE, null);
                }
            }
        });
        appStatusBtn = (Button) view.findViewById(R.id.appStatusBtnId);
        appStatusBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_APP_STATUS, null);
                }
            }
        });
        deviceInfoBtn = (Button) view.findViewById(R.id.deviceInfoBtnId);
        deviceInfoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_DEVICE_INFO, null);
                }
            }
        });
        batteryInfoBtn = (Button) view.findViewById(R.id.batteryLevelBtnId);
        batteryInfoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_BATTERY_INFO, null);
                }
            }
        });
        cameraSettingsBtn = (Button) view.findViewById(R.id.cameraSettingsBtnId);
        cameraSettingsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_BC_GET_ALL_SETTINGS, null);
                }
            }
        });

        bitrateField = (EditText) view.findViewById(R.id.bitrateTextId);

        bitrateBtn = (Button) view.findViewById(R.id.bitRateBtnId);
        bitrateBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    String bitRate = bitrateField.getText().toString();
                    mListener.onFragmentAction(IFragmentListener.ACTION_BC_SET_BITRATE, Integer.parseInt(bitRate));
                }
            }
        });

        viewLogBtn = (Button) view.findViewById(R.id.logViewBtnId);
        viewLogBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_LOG_VIEW, null);
                }
            }
        });

        showDebug = (Button) view.findViewById(R.id.showDebug);
        showDebug.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onFragmentAction(IFragmentListener.ACTION_SHOW_LAST_CMD_RESP, null);
            }
        });

        viewRtspWithAudioBtn = (Button) view.findViewById(R.id.liveAudioViewId);
        viewRtspWithAudioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // if (mListener != null) {
               //     mListener.onFragmentAction(IFragmentListener.ACTION_OPEN_CAMERA_LIVEVIEW_WITH_AUDIO, null);
               // }
                Intent intent = new Intent(getActivity(), LiveViewWithAudio.class);
                intent.putExtra("videoPath", "rtsp://192.168.42.1/live");
                intent.putExtra("videoTitle", "Live");
                startActivity(intent);
            }

        });
        return view;

    }

    @Override
    public void onAttach(Activity activity) {
        Log.e("CAM_Commands:", "onAttach");
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
        Log.e("CAM_Commands:", "onDetach");
        super.onDetach();
        mListener = null;
    }
}
