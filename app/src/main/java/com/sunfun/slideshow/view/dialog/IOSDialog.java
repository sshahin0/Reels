package com.sunfun.slideshow.view.dialog;

import android.content.Context;
import android.view.View;


/**
 * Created by Varun John on August 2018.
 */
public class IOSDialog {


    public CheckListener getCheckListener() {
        return checkListener;
    }

    public interface Listener {
        void onClick(IOSDialog iosDialog);
    }

    public interface CheckListener {
        void onCheck(IOSDialog iosDialog);
    }

    private String title;
    private String message;
    private String positiveButtonText;
    private String negativeButtonText;
    private Listener positiveClickListener;
    private Listener negativeClickListener;
    private Listener cancelListener;
    private CheckListener checkListener;
    private boolean enableAnimation = true;
    private boolean cancelable = true;
    private boolean isCheckBoxShowing;
    public boolean isChecked;
    private View view;

    static IOSDialogActivity iosDialogActivity;

    private Context context;

    private IOSDialog(Context context) {
        this.context = context;
    }

    public void show() {
        IOSDialogActivity.openActivity(context, this);
    }

    public void dismiss() {
        if (iosDialogActivity != null) {
            iosDialogActivity.dismiss();
        }
    }

    public View getView() {
        return view;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getPositiveButtonText() {
        return positiveButtonText;
    }

    public String getNegativeButtonText() {
        return negativeButtonText;
    }

    public Listener getPositiveClickListener() {
        return positiveClickListener;
    }

    public Listener getNegativeClickListener() {
        return negativeClickListener;
    }

    public Listener getCancelListener() {
        return cancelListener;
    }

    public boolean isEnableAnimation() {
        return enableAnimation;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public boolean isCheckBoxShowing(){return isCheckBoxShowing; }

    public static class Builder {

        private IOSDialog iosDialog;
        private Context context;

        public Builder(Context context) {
            this.context = context;
            iosDialog = new IOSDialog(context);
        }

        public IOSDialog build() {
            return iosDialog;
        }

        public Builder title(String title) {
            iosDialog.title = title;
            return this;
        }

        public Builder message(String message) {
            iosDialog.message = message;
            return this;
        }

        public Builder title(int titleResourceId) {
            iosDialog.title = context.getString(titleResourceId);
            return this;
        }

        public Builder message(int messageResourceId) {
            try {
                iosDialog.message = context.getString(messageResourceId);
            }catch (Exception ex){
                iosDialog.message = null;
            }
            return this;
        }

        public Builder positiveButtonText(String positiveButtonText) {
            iosDialog.positiveButtonText = positiveButtonText;
            return this;
        }

        public Builder negativeButtonText(String negativeButtonText) {
            iosDialog.negativeButtonText = negativeButtonText;
            return this;
        }

        public Builder positiveButtonText(int positiveButtonTextResourceId) {
            iosDialog.positiveButtonText = context.getString(positiveButtonTextResourceId);
            return this;
        }

        public Builder negativeButtonText(int negativeButtonTextResourceId) {
            iosDialog.negativeButtonText = context.getString(negativeButtonTextResourceId);
            return this;
        }

        public Builder positiveClickListener(Listener positiveClickListener) {
            iosDialog.positiveClickListener = positiveClickListener;
            return this;
        }

        public Builder negativeClickListener(Listener negativeClickListener) {
            iosDialog.negativeClickListener = negativeClickListener;
            return this;
        }

        public Builder cancelListener(Listener cancelListener) {
            iosDialog.cancelListener = cancelListener;
            return this;
        }

        public Builder enableAnimation(boolean enableAnimation) {
            iosDialog.enableAnimation = enableAnimation;
            return this;
        }

        public Builder cancelable(boolean cancelable) {
            iosDialog.cancelable = cancelable;
            return this;
        }

        public Builder enableCheckBox(boolean isCheckBoxShowing) {
            iosDialog.isCheckBoxShowing = isCheckBoxShowing;
            return this;
        }

        public Builder checkListener(CheckListener checkListener) {
            iosDialog.checkListener = checkListener;
            return this;
        }
    }

}
