package io.ap1.proximity.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

import io.ap1.proximity.MyApplication;
import io.ap1.proximity.R;

public class ActivityLogin extends AppCompatActivity {

    FragmentManager fragmentManager;
    BackendlessUser backendlessUser;
    FragmentTransaction fragmentTransaction;

    MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

        Backendless.initApp(this, getString(R.string.BACKENDLESS_APP_ID),
                getString(R.string.BACKENDLESS_SECRET_KEY), getString(R.string.BACKENDLESS_APP_VERSION));
        Backendless.setUrl("http://159.203.15.85/api"); //Digital Ocean host
        backendlessUser = new BackendlessUser();

        if(findViewById(R.id.fragment_login_container) != null){
            if(savedInstanceState != null){
                return;
            }

            myApplication = (MyApplication) getApplication();
            Bundle userInfo = new Bundle();
            userInfo.putString("loginName", myApplication.getUserLoginName());
            userInfo.putString("loginPassword", myApplication.getUserLoginPassword());

            FragmentLogin fragmentLogin = new FragmentLogin();
            fragmentLogin.setArguments(userInfo);
            fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().add(R.id.fragment_login_container, fragmentLogin).commit();

        }
    }

    public void switchLoginSignupFragment(Fragment fragmentName){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_login_container, fragmentName);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void goToMainUI(String userObjectId){
        startActivity(new Intent(ActivityLogin.this, ActivityMain.class).putExtra("userObjectId", userObjectId));
        finish();
    }
}
