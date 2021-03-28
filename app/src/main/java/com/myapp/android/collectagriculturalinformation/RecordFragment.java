package com.myapp.android.collectagriculturalinformation;
/**
 * Description 向用户展示record明细界面
 */
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MyLocationData;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class RecordFragment extends Fragment {

    private static final String ARG_RECORD_ID = "record_id";
    private static final String DIALOG_DATE = "DialogDate";

    //请求代码常量，目标Fragment通过请求代码常量确认是哪个Fragment在回传数据。
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    private Record mRecord;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckbox;
    private Button mReportButton;
    private Button mContactsButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private TextView mLocation;
    private Button mLocationButton;
    //定位客户端
    private LocationClient mLocationClient;
    private Button mDeleteButton;

    /**
     * 完成fragment实例及Bundle对象的创建
     * 每个fragment实例都可以附带一个Bundle对象。
     * 该bundle包含键值对，一个键值对即一个argument。
     */
    public static RecordFragment newInstance(UUID recordId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECORD_ID, recordId);

        RecordFragment fragment = new RecordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //从fragment的argument中获取ID
        UUID recordId = (UUID) getArguments().getSerializable(ARG_RECORD_ID);
        mRecord = RecordLab.get(getActivity()).getRecord(recordId);
        mPhotoFile = RecordLab.get(getActivity()).getPhotoFile(mRecord);
    }

    /**
     * 实例化fragment视图的布局，将实例化的View返回给托管activity。
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_record, container, false);

        //标题
        mTitleField = (EditText) v.findViewById(R.id.record_title);
        mTitleField.setText(mRecord.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRecord.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //日期
        mDateButton = (Button) v.findViewById(R.id.record_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mRecord.getDate());
                //设置DatePickerFragment的目标Fragment为RecordFragment，请求代码常量REQUEST_DATE
                dialog.setTargetFragment(RecordFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        //是否解决
        mSolvedCheckbox = (CheckBox) v.findViewById(R.id.record_solved);
        mSolvedCheckbox.setChecked(mRecord.isSolved());
        mSolvedCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                mRecord.setSolved(isChecked);
            }
        });

        //发送记录
        //发送一段文本消息，隐式intent操作是ACTION_SEND，指定数据类型为text/plain
        mReportButton = (Button) v.findViewById(R.id.record_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getRecordReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.record_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        //选择联系人
        //创建隐式intent，操作为ACTION_PICK，联系人数据获取位置为ContactsContract.Contacts.CONTENT_URI
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mContactsButton = (Button) v.findViewById(R.id.record_contacts);
        mContactsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mRecord.getContacts() != null) {
            mContactsButton.setText(mRecord.getContacts());
        }
        //检查是否存在联系人应用
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mContactsButton.setEnabled(false);
        }

        //照相
        mPhotoButton = (ImageButton) v.findViewById(R.id.record_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.myapp.android.collectagriculturalinformation.fileprovider",
                        mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.record_photo);
        updatePhotoView();

        //定位
        mLocation = (TextView) v.findViewById(R.id.record_location);
        mLocationButton = (Button) v.findViewById(R.id.record_location_button);
        if(mRecord.getLocation()!=null){
            mLocationButton.setEnabled(false);
            mLocation.setText(mRecord.getLocation());

        }else{
            mLocationButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //定位初始化
                    mLocationClient = new LocationClient(getActivity().getApplicationContext());
                    //注册LocationListener监听器
                    mLocationClient.registerLocationListener(new MyLocationListener());
                    SDKInitializer.initialize(getActivity().getApplicationContext());
                    //通过LocationClientOption设置LocationClient相关参数
                    LocationClientOption option = new LocationClientOption();
                    //设置扫描时间间隔
                    option.setScanSpan(1000);
                    //设置定位模式，三选一
                    option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
                    //设置需要地址信息
                    option.setIsNeedAddress(true);
                    //保存定位参数
                    mLocationClient.setLocOption(option);
                    //开启地图定位图层
                    mLocationClient.start();

                }
            });
        }

        //删除
        mDeleteButton = (Button) v.findViewById(R.id.record_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                RecordLab.get(getActivity()).deleteRecord(mRecord);
            }
        });

        return v;
    }

    //内部类，百度位置监听器
    private class MyLocationListener  implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mLocation.setText(bdLocation.getAddrStr());
            mRecord.setLocation(mLocation.getText().toString());
        }
    }

    /**
     * RecordFragment中修改完Record实例后，刷新RecordLab中的Record数据
     */
    @Override
    public void onPause() {
        super.onPause();

        RecordLab.get(getActivity())
                .updateRecord(mRecord);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mRecord.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // 返回查询的联系人姓名字段
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // 执行查询操作，contactUri类似"where"语句
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                // 检查是否得到结果
                if (c.getCount() == 0) {
                    return;
                }
                // 拉出第一行数据的第一列，即联系人姓名
                c.moveToFirst();
                String contacts = c.getString(0);
                mRecord.setContacts(contacts);
                mContactsButton.setText(contacts);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFile);

            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
        }
    }

    private void updateDate() {
        mDateButton.setText(mRecord.getDate().toString());
    }

    /**
     * 创建五段字符串信息，并返回拼接完整的消息，作为要发送的report
     */
    private String getRecordReport() {
        String solvedString = null;
        if (mRecord.isSolved()) {
            solvedString = getString(R.string.record_report_solved);
        } else {
            solvedString = getString(R.string.record_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mRecord.getDate()).toString();
        String contacts = mRecord.getContacts();
        String location = mRecord.getLocation();
        if (contacts == null) {
            contacts = getString(R.string.record_report_no_contacts);
        } else {
            contacts = getString(R.string.record_report_contacts, contacts);
        }
        String report = getString(R.string.record_report,
                mRecord.getTitle(), dateString, solvedString, contacts, location);
        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
