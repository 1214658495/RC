package com.ambarella.remotecamera.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.ambarella.remotecamera.AppPreferenceActivity;
import com.ambarella.remotecamera.R;

/**
 * Created by RamKumar on 11/19/15.
 */
public class NetworkModeSelectFrag extends Fragment implements ListView.OnItemClickListener{

    public NetworkModeSelectFrag() {}

    String[] networkTypes = new String[] {
            "WIFI",
            "BLE+WIFI"
    };
    ListView mListView;
    ImageButton appPrefButton;
    private IFragmentListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_mode_select,container, false);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, networkTypes);
        mListView = (ListView) view.findViewById(R.id.network_list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);

        appPrefButton = (ImageButton) view.findViewById(R.id.appPreferences);
        appPrefButton.setOnClickListener(new View.OnClickListener() {
            public  void onClick(View v) {
                //((NetworkModeSelectFragmentListener) getActivity()).onClick_network_type("app_pref");
                Intent intent = new Intent(getActivity(), AppPreferenceActivity.class);
                startActivity(intent);
                }
        });

        //setListAdapter(adapter);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String itemValue = (String) parent.getItemAtPosition(position);
        ((NetworkModeSelectFragmentListener) getActivity()).onClick_network_type(itemValue);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (! (activity instanceof NetworkModeSelectFragmentListener) )
            throw new ClassCastException();
    }
    public  interface NetworkModeSelectFragmentListener {
         void onClick_network_type(String netType);
    }


}
