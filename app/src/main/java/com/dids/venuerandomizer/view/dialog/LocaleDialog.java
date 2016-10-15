package com.dids.venuerandomizer.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.network.FourSquareWrapper;
import com.dids.venuerandomizer.controller.utility.PreferencesUtility;
import com.dids.venuerandomizer.view.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class LocaleDialog extends DialogFragment {

    public static LocaleDialog getInstance() {
        return new LocaleDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.settings_locale);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_locale, null);
        builder.setView(view);
        final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        List<String> countList = new ArrayList<>(FourSquareWrapper.getLocaleMap().values());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, countList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(countList.indexOf(PreferencesUtility.getInstance().getLocale()));
        builder.setNegativeButton(R.string.control_cancel, null);
        builder.setPositiveButton(R.string.control_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PreferencesUtility.getInstance().setLocale((String) spinner.getSelectedItem());
            }
        });
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ((SettingsActivity) getActivity()).onDismiss();
    }
}
