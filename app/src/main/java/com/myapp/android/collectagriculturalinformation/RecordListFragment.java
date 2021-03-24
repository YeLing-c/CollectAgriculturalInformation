package com.myapp.android.collectagriculturalinformation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordListFragment extends Fragment {


    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mRecordRecyclerView;
    private RecordAdapter mAdapter;
    private boolean mSubtitleVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_list, container, false);

        mRecordRecyclerView = (RecyclerView) view
                .findViewById(R.id.record_recycler_view);
        mRecordRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_record_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_record:
                Record record = new Record();
                RecordLab.get(getActivity()).addRecord(record);
                Intent intent = RecordPagerActivity
                        .newIntent(getActivity(), record.getId());
                startActivity(intent);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        RecordLab recordLab = RecordLab.get(getActivity());
        int recordCount = recordLab.getRecords().size();
        String subtitle = getString(R.string.subtitle_format, recordCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private void updateUI() {
        RecordLab recordLab = RecordLab.get(getActivity());
        List<Record> records = recordLab.getRecords();

        if (mAdapter == null) {
            mAdapter = new RecordAdapter(records);
            mRecordRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setRecords(records);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    private class RecordHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Record mRecord;

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;

        public RecordHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_record, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.record_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.record_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.record_solved);
        }

        public void bind(Record record) {
            mRecord = record;
            mTitleTextView.setText(mRecord.getTitle());
            mDateTextView.setText(mRecord.getDate().toString());
            mSolvedImageView.setVisibility(record.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = RecordPagerActivity.newIntent(getActivity(), mRecord.getId());
            startActivity(intent);
        }
    }

    private class RecordAdapter extends RecyclerView.Adapter<RecordHolder> {

        private List<Record> mRecords;

        public RecordAdapter(List<Record> records) {
            mRecords = records;
        }

        @Override
        public RecordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new RecordHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(RecordHolder holder, int position) {
            Record record = mRecords.get(position);
            holder.bind(record);
        }

        @Override
        public int getItemCount() {
            return mRecords.size();
        }

        public void setRecords(List<Record> records) {
            mRecords = records;
        }
    }


}
