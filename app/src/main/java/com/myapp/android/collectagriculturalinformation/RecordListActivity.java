package com.myapp.android.collectagriculturalinformation;
/**
 * Description 托管RecordListFragment
 */
import androidx.fragment.app.Fragment;

public class RecordListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new RecordListFragment();
    }
}
