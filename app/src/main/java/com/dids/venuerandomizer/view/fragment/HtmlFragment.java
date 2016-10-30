package com.dids.venuerandomizer.view.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.utility.Utilities;

public class HtmlFragment extends Fragment {
    public static final int LICENSE = 1;
    public static final int PRIVACY = 2;
    private static final String TYPE = "type";

    public static HtmlFragment newInstance(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        HtmlFragment fragment = new HtmlFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_html, container, false);
        int type = getArguments().getInt(TYPE);
        if (type == LICENSE) {
            TextView textView = (TextView) view.findViewById(R.id.text_header);
            textView.setText(R.string.drawer_license);

            TextView content = (TextView) view.findViewById(R.id.content);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                content.setText(Html.fromHtml(Utilities.getStringFromAsset(getContext(), "opensource.html"),
                        Html.FROM_HTML_MODE_LEGACY));
            } else {
                //noinspection deprecation
                content.setText(Html.fromHtml(Utilities.getStringFromAsset(getContext(), "opensource.html")));
            }
            content.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            TextView textView = (TextView) view.findViewById(R.id.text_header);
            textView.setText(R.string.drawer_privacy);

            TextView content = (TextView) view.findViewById(R.id.content);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                content.setText(Html.fromHtml(Utilities.getStringFromAsset(getContext(), "privacy.html"),
                        Html.FROM_HTML_MODE_LEGACY));
            } else {
                //noinspection deprecation
                content.setText(Html.fromHtml(Utilities.getStringFromAsset(getContext(), "privacy.html")));
            }
            content.setMovementMethod(LinkMovementMethod.getInstance());
        }
        return view;
    }
}
