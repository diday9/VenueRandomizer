package com.dids.venuerandomizer.view;

import android.os.Bundle;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.view.base.BaseActivity;

public class SettingsActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setToolbar(R.id.toolbar, true);
        TextView toolbar = (TextView) findViewById(R.id.toolbar_text);
        toolbar.setText(R.string.settings);
    }
}
