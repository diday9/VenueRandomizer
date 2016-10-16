package com.dids.venuerandomizer.controller.utility;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Interpolator;

public class AnimationUtility {
    private static final String VERTICAL_POSITION_PROPERTY = "Y";
    private static final String VERTICAL_TRANSLATION_PROPERTY = "translationY";

    public static void animateVerticalPosition(View view, int pxHeight, int duration) {
        animateVerticalPosition(view, pxHeight, duration, null, 0, null);
    }

    public static void animateVerticalPosition(View view, int pxHeight, int duration,
                                               Interpolator interpolator) {
        animateVerticalPosition(view, pxHeight, duration, interpolator, 0, null);
    }

    public static void animateVerticalPosition(View view, int pxHeight, int duration,
                                               Interpolator interpolator, int delay,
                                               Animator.AnimatorListener listener) {
        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(view, VERTICAL_POSITION_PROPERTY,
                pxHeight);
        moveAnim.setDuration(duration);
        moveAnim.setStartDelay(delay);
        if (interpolator != null) {
            moveAnim.setInterpolator(interpolator);
        }
        if (listener != null) {
            moveAnim.addListener(listener);
        }
        moveAnim.start();
    }

    public static void animateVerticalTranslation(View view, int pxHeight, int duration) {
        animateVerticalTranslation(view, pxHeight, duration, null, 0, null);
    }

    public static void animateVerticalTranslation(View view, int pxHeight, int duration,
                                                  Animator.AnimatorListener listener) {
        animateVerticalTranslation(view, pxHeight, duration, null, 0, listener);
    }

    private static void animateVerticalTranslation(View view, int pxHeight, int duration,
                                                   Interpolator interpolator, int delay,
                                                   Animator.AnimatorListener listener) {
        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(view, VERTICAL_TRANSLATION_PROPERTY,
                pxHeight);
        moveAnim.setDuration(duration);
        moveAnim.setStartDelay(delay);
        if (interpolator != null) {
            moveAnim.setInterpolator(interpolator);
        }
        if (listener != null) {
            moveAnim.addListener(listener);
        }
        moveAnim.start();
    }
}
