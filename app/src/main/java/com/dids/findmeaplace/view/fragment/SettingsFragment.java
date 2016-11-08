package com.dids.findmeaplace.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dids.findmeaplace.R;
import com.dids.findmeaplace.controller.utility.PreferencesUtility;
import com.dids.findmeaplace.view.dialog.MaxCountDialog;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private AppCompatCheckBox mHiResCheckBox;
    private AppCompatCheckBox mDynamicCheckBox;
    private TextView mCountView;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        PreferencesUtility prefUtil = PreferencesUtility.getInstance();
        View hiResView = view.findViewById(R.id.high_res_support);
        hiResView.setOnClickListener(this);
        mHiResCheckBox = (AppCompatCheckBox) view.findViewById(R.id.hi_res_cb);
        mHiResCheckBox.setChecked(prefUtil.isHiResImageSupported());

        View dynamic = view.findViewById(R.id.dynamic_images);
        dynamic.setOnClickListener(this);
        mDynamicCheckBox = (AppCompatCheckBox) view.findViewById(R.id.dynamic_images_cb);
        mDynamicCheckBox.setChecked(prefUtil.isDynamicImagesSupported());

        View maxImage = view.findViewById(R.id.max_image_count);
        maxImage.setOnClickListener(this);
        mCountView = (TextView) view.findViewById(R.id.tv_max_image_count);
        mCountView.setText(String.format(getResources().getQuantityString(R.plurals.
                settings_max_image_subtext, prefUtil.getMaxImageCount()), prefUtil.getMaxImageCount()));
        return view;
    }

    @Override
    public void onClick(View view) {
        DialogFragment dialog;
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
                dialog = MaxCountDialog.getInstance();
                dialog.show(getFragmentManager(), "max_count");
                break;
        }
    }

    public void updateData() {
        PreferencesUtility prefUtil = PreferencesUtility.getInstance();
        mCountView.setText(String.format(getResources().getQuantityString(R.plurals.
                settings_max_image_subtext, prefUtil.getMaxImageCount()), prefUtil.getMaxImageCount()));
    }
}
