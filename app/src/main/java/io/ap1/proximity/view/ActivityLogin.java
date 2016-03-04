package io.ap1.proximity.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

import io.ap1.proximity.R;

public class ActivityLogin extends AppCompatActivity {

    FragmentManager fragmentManager;
    BackendlessUser backendlessUser;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Backendless.initApp(this, getString(R.string.BACKENDLESS_APP_ID),
                getString(R.string.BACKENDLESS_SECRET_KEY), getString(R.string.BACKENDLESS_APP_VERSION));
        Backendless.setUrl("http://159.203.15.85/api"); //Digital Ocean host
        backendlessUser = new BackendlessUser();

        if(findViewById(R.id.fragment_login_container) != null){
            if(savedInstanceState != null){
                return;
            }

            FragmentLogin fragmentLogin = new FragmentLogin();
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

    public void goToMainUI(){
        startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
        finish();
    }
}