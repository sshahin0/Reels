package com.sunfun.slideshow;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.sunfun.slideshow.fragment.MainFragment;
import com.sunfun.slideshow.utils.CheckInternetConnection;
import com.sunfun.slideshow.utils.StorageUtils;

public class MainActivity extends AppCompatActivity {

    FrameLayout fragmentContainer;
    MainFragment mainFragment;

    public boolean connectionAvailable = true;
    private CheckInternetConnection connectionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        changeStatusBarColor();
        mainFragment = MainFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, mainFragment, "main_fragment").commit();
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
        StorageUtils.getInstance().createFolder();
        connectionChecker = new CheckInternetConnection();
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.backgroundColor));
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if(mainFragment!=null)mainFragment.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StorageUtils.getInstance().deleteDataFolder();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionChecker.addConnectionChangeListener(isConnectionAvailable -> {
            if(connectionAvailable && !isConnectionAvailable) {
                connectionAvailable = false;
            }
            else if(!connectionAvailable && isConnectionAvailable) {
                connectionAvailable = true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionChecker.removeConnectionChangeListener();
    }
}
