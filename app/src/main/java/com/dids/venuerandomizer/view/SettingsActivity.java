package com.dids.venuerandomizer.view;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.utility.PreferencesUtility;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.dids.venuerandomizer.view.dialog.MaxCountDialog;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {
    private AppCompatCheckBox mHiResCheckBox;
    private AppCompatCheckBox mDynamicCheckBox;
    private TextView mCountView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setToolbar(R.id.toolbar, true);
        TextView toolbar = (TextView) findViewById(R.id.toolbar_text);
        toolbar.setText(R.string.settings);

        PreferencesUtility prefUtil = PreferencesUtility.getInstance();
        mHiResCheckBox = (AppCompatCheckBox) findViewById(R.id.hi_res_cb);
        mHiResCheckBox.setChecked(prefUtil.isHiResImageSupported());

        mDynamicCheckBox = (AppCompatCheckBox) findViewById(R.id.dynamic_images_cb);
        mDynamicCheckBox.setChecked(prefUtil.isDynamicImagesSupported());

        mCountView = (TextView) findViewById(R.id.tv_max_image_count);
        mCountView.setText(String.format(getString(R.string.settings_max_image_subtext),
                prefUtil.getMaxImageCount()));
    }

    @Override
    public void onClick(View view) {
        PreferencesUtility preferencesUtility = PreferencesUtility.getInstance();
        switch (view.getId()) {
            case R.id.high_res_support:
                mHiResCheckBox.setChecked(!mHiResCheckBox.isChecked());
                preferencesUtility.setHiResImageSupport(mHiResCheckBox.isChecked());
                break;
            case R.id.dynamic_images:
                mDynamicCheckBox.setChecked(!mDynamicCheckBox.isChecked());
                preferencesUtility.setDynamicImagesSupport(mDynamicCheckBox.isChecked());
                break;
            case R.id.max_image_count:
                DialogFragment dialog = MaxCountDialog.getInstance();
                dialog.show(getSupportFragmentManager(), "max_count");
                break;
        }
    }

    public void onDismiss() {
        PreferencesUtility prefUtil = PreferencesUtility.getInstance();
        mCountView.setText(String.format(getString(R.string.settings_max_image_subtext),
                prefUtil.getMaxImageCount()));
    }
}
