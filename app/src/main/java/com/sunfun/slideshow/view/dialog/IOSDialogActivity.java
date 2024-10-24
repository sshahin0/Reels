package com.sunfun.slideshow.view.dialog;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.sunfun.slideshow.R;

public class IOSDialogActivity extends AppCompatActivity implements View.OnClickListener {


    public static void openActivity(Context context, IOSDialog iosDialog) {
        IOSDialogActivity.iosDialog = iosDialog;
        context.startActivity(new Intent(context, IOSDialogActivity.class));
    }

    private Context context;
    private static IOSDialog iosDialog;
    private CardView layoutDialog;
    private ConstraintLayout layoutContent;
    private LinearLayout layoutNegative;
    private TextView textViewTitle;
    private TextView textViewMessage;
    private TextView textViewNegative;
    private TextView textViewPositive;
    private CheckBox checkBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_iosdialog);

        changeStatusBarColor();

        layoutDialog = findViewById(R.id.layoutDialog);
        layoutContent = findViewById(R.id.layoutContent);
        layoutNegative = findViewById(R.id.layoutNegative);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewMessage = findViewById(R.id.textViewMessage);
        textViewNegative = findViewById(R.id.textViewNegative);
        textViewPositive = findViewById(R.id.textViewPositive);
        checkBox = findViewById(R.id.checkbox);



        IOSDialog.iosDialogActivity = this;

        if (iosDialog.isEnableAnimation()) {
            layoutDialog.setScaleX(1.3f);
            layoutDialog.setScaleY(1.3f);
            layoutContent.setAlpha(0);
            layoutContent.animate().alpha(1f).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
            layoutDialog.animate().scaleX(1f).scaleY(1f).setDuration(250).setInterpolator(new DecelerateInterpolator()).start();
        }

        if(iosDialog.isCheckBoxShowing()){
            checkBox.setVisibility(View.VISIBLE);
        }

        if (iosDialog.getTitle() != null) {
            textViewTitle.setText(iosDialog.getTitle());
        } else {
            textViewTitle.setVisibility(View.GONE);
        }

        if (iosDialog.getMessage() != null) {
            textViewMessage.setText(iosDialog.getMessage());
        } else {
            textViewMessage.setVisibility(View.GONE);
        }

        if (iosDialog.getPositiveButtonText() != null) {
            textViewPositive.setText(iosDialog.getPositiveButtonText());
        } else {
            textViewPositive.setText("OK");
        }

        if (iosDialog.getNegativeButtonText() != null) {
            textViewNegative.setText(iosDialog.getNegativeButtonText());
        } else {
            textViewNegative.setText("");
            layoutNegative.setVisibility(View.GONE);
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            iosDialog.isChecked = isChecked;
            iosDialog.getCheckListener().onCheck(iosDialog);
        });

        textViewPositive.setOnClickListener(this);
        textViewNegative.setOnClickListener(this);

    }

    public void onOutsideClick(View view) {
        if (iosDialog.isCancelable()) {

            if (iosDialog.getCancelListener() != null) {
                iosDialog.getCancelListener().onClick(iosDialog);
            }

            onBackPressed();
        }
    }

    private boolean isAnimationExitDone;

    @Override
    public void onBackPressed() {

        if (isAnimationExitDone || !iosDialog.isEnableAnimation()) {
//            super.onBackPressed();
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
        }

        layoutContent.animate().alpha(0f).setDuration(200).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimationExitDone = true;
                onBackPressed();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        }).setInterpolator(new AccelerateInterpolator()).start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.textViewPositive) {
            if (iosDialog.getPositiveClickListener() != null) {
                iosDialog.getPositiveClickListener().onClick(iosDialog);
            } else {
                dismiss();
            }

        } else if (id == R.id.textViewNegative) {
            if (iosDialog.getNegativeClickListener() != null) {
                iosDialog.getNegativeClickListener().onClick(iosDialog);
            } else {
                dismiss();
            }
        } else {
        }
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.backgroundColor));
        }
    }

    public void dismiss() {
        onBackPressed();
    }
}
