package com.dids.venuerandomizer.view.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.dids.venuerandomizer.controller.task.SaveBitmapTask;
import com.dids.venuerandomizer.controller.utility.Utilities;
import com.dids.venuerandomizer.model.Assets;

public class NetworkImageView extends ImageView {
    private static final String TAG = "EventNetworkImageView";
    private String mUrl;

    private ImageLoader mImageLoader;

    private ImageLoader.ImageContainer mImageContainer;

    private ImageLoaderListener mListener;

    public NetworkImageView(Context context) {
        this(context, null);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageUrl(String url, ImageLoader imageLoader) {
        mUrl = url;
        mImageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }

    public void setImageUrl(Assets asset, ImageLoader imageLoader) {
        mUrl = asset.getUrl();
        mImageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(asset, false);
    }

    private void loadImageIfNecessary(final Assets asset, final boolean isInLayoutPass) {
        int width = ((View) getParent()).getWidth();
        int height = ((View) getParent()).getHeight();
        ScaleType scaleType = getScaleType();

        boolean wrapWidth = false, wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT;
            wrapHeight = getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        boolean isFullyWrapContent = wrapWidth && wrapHeight;
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            Log.e(TAG, "Dimension is 0");
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
            if (mImageContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                mImageContainer.cancelRequest();
            }
        }

        // Calculate the max image width / height to use while ignoring WRAP_CONTENT dimens.
        int maxWidth = wrapWidth ? 0 : width;
        int maxHeight = wrapHeight ? 0 : height;

        if (asset != null) {
            Bitmap bitmap = Utilities.getBitmapFromFile(asset.getPath());
            if (bitmap != null) {
                setImageBitmap(bitmap);
                if (mListener != null) {
                    mListener.onImageLoaded();
                }
                return;
            }
        }

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        mImageContainer = mImageLoader.get(mUrl,
                new ImageLoader.ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }

                    @Override
                    public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                        // If this was an immediate response that was delivered inside of a layout
                        // pass do not set the image immediately as it will trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image by posting back to
                        // the main thread.
                        if (isImmediate && isInLayoutPass) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }

                        if (response.getBitmap() != null) {
                            Log.d(TAG, "getbitmap ");
                            if (asset != null) {
                                Log.d(TAG, "saving " + asset.getPath());
                                new SaveBitmapTask(asset.getPath()).
                                        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                                response.getBitmap());
                            }
                            setImageBitmap(response.getBitmap());
                            if (mListener != null) {
                                mListener.onImageLoaded();
                            }
                        }
                    }
                }, maxWidth, maxHeight, scaleType);
    }

    private void loadImageIfNecessary(final boolean isInLayoutPass) {
        loadImageIfNecessary(null, isInLayoutPass);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mImageContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the container so we can reload the image if necessary.
            mImageContainer = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    public void setImageLoaderListener(ImageLoaderListener listener) {
        mListener = listener;
    }

    public interface ImageLoaderListener {
        void onImageLoaded();
    }
}
