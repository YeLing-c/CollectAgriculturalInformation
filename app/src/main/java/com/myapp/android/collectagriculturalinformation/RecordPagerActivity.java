package com.myapp.android.collectagriculturalinformation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;
import java.util.UUID;

public class RecordPagerActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_ID =
            "com.myapp.android.collectagriculturalinformation.record_id";

    private ViewPager mViewPager;
    private List<Record> mRecords;

    public static Intent newIntent(Context packageContext, UUID recordId) {
        Intent intent = new Intent(packageContext, RecordPagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, recordId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_pager);

        UUID recordId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_CRIME_ID);

        mViewPager = (ViewPager) findViewById(R.id.record_view_pager);

        mRecords = RecordLab.get(this).getRecords();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                Record record = mRecords.get(position);
                return RecordFragment.newInstance(record.getId());
            }

            @Override
            public int getCount() {
                return mRecords.size();
            }
        });

        for (int i = 0; i < mRecords.size(); i++) {
            if (mRecords.get(i).getId().equals(recordId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }



}
