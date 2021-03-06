package com.ambarella.remotecamera.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ambarella.remotecamera.R;
import com.ambarella.remotecamera.RemoteCam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MediaFragment extends Fragment implements
        ListView.OnItemClickListener, ListView.OnCreateContextMenuListener,
        View.OnClickListener
{
    private final static String TAG = "CameraFrag";

    private String mPWD;
    private String mHome;
    private DentryAdapter mAdapter;
    private IFragmentListener mListener;
    private ListView mListView;
    private TextView mTextViewPath;
    private RemoteCam mRemoteCam;


    private String selectedFile;
    private String selectedFileName;
    private Button fileOperationsBtn;

    public MediaFragment() {
        reset();
    }

    public void reset() {
        mPWD = null;
        mHome = null;
        mAdapter = null;
    }

    public void setRemoteCam(RemoteCam cam) {mRemoteCam = cam;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media, container, false);



        mTextViewPath = (TextView) view.findViewById(R.id.textViewDentryPath);

        // Setup the list view
        mListView = (ListView) view.findViewById(R.id.listViewDentryName);
        mListView.setOnItemClickListener(this);
        mListView.setOnCreateContextMenuListener(this);
        registerForContextMenu(mListView);

        if (mAdapter == null) {
            mPWD = mHome = mRemoteCam.sdCardDirectory();
            listDirContents(mPWD);
        } else {
            showDirContents();
        }
        //Button fileDelete, mediaInfo, setROAttr, setRWAttr, pwdBtn;
        //Button noOfFiles, noVideoFiles, noPhotoFiles, downLoadFile, getThumb;

        fileOperationsBtn = (Button) view.findViewById(R.id.deleteFileId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.mediaInfId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.setROattrId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.setRWattrId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.pwdId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.totalFileCountId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.photoFileCountId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.videoFileCountId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.playBackId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.fileDownloadId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.getThumbId);
        fileOperationsBtn.setOnClickListener(this);
        fileOperationsBtn = (Button) view.findViewById(R.id.burnInFwId);
        fileOperationsBtn.setOnClickListener(this);
        /*(new View.OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentAction(IFragmentListener., null);
                }
            }
        });*/

        return view;
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.deleteFileId:
                if (selectedFile != null)
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE, selectedFile);
                break;
            case R.id.mediaInfId:
                if (selectedFile != null)
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_INFO, selectedFile);
                break;
            case R.id.setROattrId:
                if (selectedFile != null)
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_SET_RO, selectedFile);
                break;
            case R.id.setRWattrId:
                if (selectedFile != null)
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_SET_WR, selectedFile);
                break;
            case R.id.pwdId:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_GET_PWD, null);
                break;
            case R.id.totalFileCountId:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_GET_ALL_FILE_COUNT, null);
                break;
            case R.id.photoFileCountId:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_GET_ALL_PHOTO_FILES, null);
                break;
            case R.id.videoFileCountId:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_GET_ALL_VIDEO_FILES, null);
                break;
            case R.id.playBackId:
                if (selectedFile != null)
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_VIEW, selectedFile);
                break;
            case R.id.fileDownloadId:
                if (selectedFile != null)
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_DOWNLOAD, selectedFile);
                break;
            case R.id.getThumbId:
                if (selectedFile != null)
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_GET_THUMB, selectedFile);
                    //mListener.onFragmentAction(IFragmentListener.ACTION_FS_GET_THUMB, selectedFileName);
                break;
            case R.id.burnInFwId:
                if (selectedFile != null)
                    mListener.onFragmentAction(IFragmentListener.ACTION_FS_BURN_FW, selectedFile);
                break;
            default:
                break;
        }
    }
    @Override
    public void onAttach(Activity activity) {
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
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedFile = "";

        if (null != mListener) {
            Model item = (Model) parent.getItemAtPosition(position);
            if (item.isDirectory()) {
                mPWD += item.getName() + "/";
                listDirContents(mPWD);
            } else { //getram
                selectedFileName =  item.getName();
                selectedFile =  mPWD + item.getName();
                mListView.setSelector(R.drawable.scrubber_control_focused_holo);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Model model = mAdapter.getItem(info.position);
        if (model.isDirectory())
            return;

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_dentry, menu);
        String name = model.getName();
        int len = name.length();
        String surfix = name.substring(len-3, len).toLowerCase();
        if (!surfix.equals("jpg") && !surfix.equals("mp4")) {
            menu.removeItem(R.id.item_dentry_info);
            menu.removeItem(R.id.item_dentry_view);
            menu.removeItem(R.id.item_dentry_thumb);
        }
        if (!surfix.equals("bin")) {
            menu.removeItem(R.id.item_dentry_burning_fw);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Model model = mAdapter.getItem(info.position);
        String path = mPWD + model.getName();
        switch (item.getItemId()) {
            case R.id.item_dentry_delete:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE, path);
                return true;
            case R.id.item_dentry_download:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_DOWNLOAD, path);
                return true;
            case R.id.item_dentry_info:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_INFO, path);
                return true;
            case R.id.item_dentry_view:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_VIEW, path);
                return true;
            case R.id.item_dentry_set_RO:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_SET_RO, path);
                return true;
            case R.id.item_dentry_set_WR:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_SET_WR, path);
                return true;
            case R.id.item_dentry_burning_fw:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_BURN_FW, path);
                break;
            case R.id.item_dentry_thumb:
                mListener.onFragmentAction(IFragmentListener.ACTION_FS_GET_THUMB, path);
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public void refreshDirContents() {
        listDirContents(mPWD);
    }

    public void goParentDir() {
        int index = mPWD.lastIndexOf('/');
        if (index > 0) {
            index = mPWD.lastIndexOf('/', index - 1);
            mPWD = mPWD.substring(0, index + 1);
            listDirContents(mPWD);
        }
    }

    public void formatSD() {
        mListener.onFragmentAction(IFragmentListener.ACTION_FS_FORMAT_SD,
                mHome.equals("/tmp/fuse_d/") ? "D:" : "C:");
    }

    public void showSD() {
        mPWD = mHome;
        refreshDirContents();
    }

    private void changeFolder(String folderPath) {
        if (folderPath != null)
            mListener.onFragmentAction(IFragmentListener.ACTION_FS_CD, folderPath);
    }

    public String getPWD() {
        return mPWD;
    }

    private void listDirContents(String path) {
        if (path != null) {
            mListener.onFragmentAction(IFragmentListener.ACTION_FS_LS, path);
            changeFolder(path);
        }
    }

    private void showDirContents() {
        mTextViewPath.setText("Directory: " + mPWD);
        mListView.setAdapter(mAdapter);
    }

    public void updateDirContents(JSONObject parser) {
        ArrayList<Model> models = new ArrayList<Model>();

        try {
            JSONArray contents = parser.getJSONArray("listing");

            for (int i = 0; i < contents.length(); i++) {
                models.add(new Model(contents.getJSONObject(i).toString()));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        mAdapter = new DentryAdapter(models);
        showDirContents();
    }



    private class Model {
        private boolean isDirectory;
        private int size;
        private String name;
        private String time;

        public Model(String descriptor) {
            descriptor = descriptor.replaceAll("[{}\"]", "");

            // parse the name
            int index = descriptor.indexOf(':');
            name = descriptor.substring(0, index).trim();

            // figure out if this is file or directory
            isDirectory = name.endsWith("/");
            if (isDirectory)
                name = name.substring(0, name.length()-2);

            if (descriptor.contains("|")) {
                // get the size
                descriptor = descriptor.substring(index+1).trim();
                index = descriptor.indexOf(" ");
                size = Integer.parseInt(descriptor.substring(0, index));
                // get the time
                time = descriptor.substring(descriptor.indexOf('|')+1).trim();
            } else if (descriptor.contains("bytes")) {
                index = descriptor.indexOf(" ");
                size = Integer.parseInt(descriptor.substring(0, index));
                time = null;
            } else {
                size = -1;
                time = descriptor.substring(index+1).trim();
            }
        }

        public String getName() {
            return name;
        }

        public int getSize() {
            return size;
        }

        public String getTime() {
            return time;
        }

        public boolean isDirectory() {
            return isDirectory;
        }
    }

    private class DentryAdapter extends ArrayAdapter<Model> {
        final private ArrayList<Model> mArrayList;

        public DentryAdapter(ArrayList<Model> arrayList) {
            super(getActivity(), R.layout.listview_dentry, arrayList);
            mArrayList = arrayList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.listview_dentry, parent, false);
            Model model = mArrayList.get(position);

            TextView nameView = (TextView) view.findViewById(R.id.textViewDentryName);
            nameView.setText(model.getName());

            TextView timeView = (TextView) view.findViewById(R.id.textViewDentryTime);
            timeView.setText(model.getTime());

            ImageView imageView = (ImageView) view.findViewById(R.id.imageViewDentryType);
            TextView sizeView = (TextView) view.findViewById(R.id.textViewDentrySize);
            if (model.isDirectory()) {
                imageView.setImageResource(R.drawable.ic_folder);
                sizeView.setVisibility(View.INVISIBLE);
            } else {
                imageView.setImageResource(R.drawable.ic_file);
                int size = model.getSize();
                if (size > 0)
                    sizeView.setText(Integer.toString(model.getSize()) + " bytes");
                else
                    sizeView.setVisibility(View.INVISIBLE);
            }

            return view;
        }
    }
}
