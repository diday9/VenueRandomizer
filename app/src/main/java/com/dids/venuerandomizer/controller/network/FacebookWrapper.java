package com.dids.venuerandomizer.controller.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;

import com.dids.venuerandomizer.model.Category;
import com.dids.venuerandomizer.model.Venue;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareDialog;

public class FacebookWrapper {
    private static final String URL_PREFIX = "http://www.facebook.com/";

    public static void launch(Context context, String user) {
        Uri uri = Uri.parse(URL_PREFIX + user);
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().
                    getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                uri = Uri.parse("fb://facewebmodal/f?href=" + URL_PREFIX + user);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    public static void share(Activity activity, Venue venue, Bitmap bitmap, CallbackManager manager,
                             FacebookCallback<Sharer.Result> callback) {
        String categoryString = "";
        if (venue.getCategories() != null && !venue.getCategories().isEmpty()) {
            for (Category category : venue.getCategories()) {
                if (category.isPrimary()) {
                    categoryString = category.getName();
                    break;
                }
            }
        }
        /** Create a restaurant object */
        ShareOpenGraphObject.Builder restoBuilder = new ShareOpenGraphObject.Builder()
                .putString("og:type", "restaurant.restaurant")
                .putString("og:title", venue.getName())
                .putString("og:description", categoryString)
                .putString("al:android", "https://fb.me/1102903826496238")
                .putString("place:location:latitude", String.valueOf(venue.getLatitude()))
                .putString("place:location:longitude", String.valueOf(venue.getLongitude()));
        /** Create photo */
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .setUserGenerated(true)
                .build();
        /**  Create an action */
        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("restaurants.visited")
                .putObject("restaurant", restoBuilder.build())
                .putPhoto("image", photo)
                .build();
        /** Create the content */
        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("restaurant")
                .setAction(action)
                .build();
        ShareDialog dialog = new ShareDialog(activity);
        dialog.registerCallback(manager, callback);
        dialog.show(content);
    }
}
