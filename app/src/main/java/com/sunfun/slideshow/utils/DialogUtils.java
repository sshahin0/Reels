package com.sunfun.slideshow.utils;

import android.content.Context;

import com.sunfun.slideshow.view.dialog.IOSDialog;

public class DialogUtils {

    public void createWarningDialog(Context context,
                                    String title,
                                    int warning_message,
                                    String positive,
                                    String negative,
                                    IOSDialog.Listener positiveBtnListener,
                                    boolean isCheckBoxShowing,
                                    IOSDialog.CheckListener checkListener) {
        new IOSDialog.Builder(context)
                .title(title)
                .message(warning_message)
                .positiveButtonText(positive)
                .negativeButtonText(negative)
                .cancelable(false)
                .enableAnimation(false)
                .enableCheckBox(isCheckBoxShowing)
                .positiveClickListener(positiveBtnListener)
                .negativeClickListener(IOSDialog::dismiss)
                .checkListener(checkListener)
                .build()
                .show();
    }

    public void createWarningDialog(Context context, String title, int warning_message, String positive, String negative, IOSDialog.Listener positiveBtnListener) {
        new IOSDialog.Builder(context)
                .title(title)
                .message(warning_message)
                .positiveButtonText(positive)
                .negativeButtonText(negative)
                .cancelable(false)
                .enableAnimation(false)
                .positiveClickListener(positiveBtnListener)
                .negativeClickListener(IOSDialog::dismiss)
                .build()
                .show();
    }

    public void createWarningDialog(Context context, String title, int warning_message, String positive, String negative, IOSDialog.Listener positiveBtnListener, IOSDialog.Listener negativeBtnListener) {
        new IOSDialog.Builder(context)
                .title(title)
                .message(warning_message)
                .positiveButtonText(positive)
                .negativeButtonText(negative)
                .cancelable(false)
                .enableAnimation(false)
                .positiveClickListener(positiveBtnListener)
                .negativeClickListener(negativeBtnListener)
                .build()
                .show();
    }

}
