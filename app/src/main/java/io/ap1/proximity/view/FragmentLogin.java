package io.ap1.proximity.view;

import android.app.Application;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ap1.proximity.Constants;
import io.ap1.proximity.MyApplication;
import io.ap1.proximity.R;

/**
 * Created by admin on 08/02/16.
 */
public class FragmentLogin extends Fragment implements View.OnClickListener {
    private static final String TAG = "FragmentLogin";

    @Bind(R.id.et_login_username)
    EditText etLoginUsername;
    @Bind(R.id.et_signIn_password)
    EditText etSignInPassword;
    @Bind(R.id.btn_login)
    Button btnLogin;
    @Bind(R.id.btn_login_facebook)
    Button btnLoginFacebook;
    @Bind(R.id.tv_forget_password)
    TextView tvForgetPassword;
    @Bind(R.id.tv_switch_to_signup)
    TextView tvSwitchToSignup;

    BackendlessUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);
        bindOnClickListeners();
        user = ((ActivityLogin)getActivity()).backendlessUser;

        Bundle userInfo = getArguments();
        if(userInfo != null){
            final String loginName = userInfo.getString(Constants.USER_LOGIN_KEY_LOGINNAME);
            String loginPassword = userInfo.getString(Constants.USER_LOGIN_KEY_LOGINPASSWORD);
            Log.e(TAG, "onCreateView: " + loginName + loginPassword);
            if(loginName != null && loginPassword != null){
                Backendless.UserService.login(loginName, loginPassword, new BackendlessCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser backendlessUser) {
                        String userObjectId = backendlessUser.getObjectId();
                        setGlobalUserInfo(loginName, userObjectId);
                        ((ActivityLogin)getActivity()).goToMainUI(userObjectId, loginName);
                    }
                    @Override
                    public void handleFault(BackendlessFault fault){
                        Toast.makeText(getActivity(), fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        return view;
    }

    private void bindOnClickListeners(){
        btnLogin.setOnClickListener(this);
        btnLoginFacebook.setOnClickListener(this);
        tvForgetPassword.setOnClickListener(this);
        tvSwitchToSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                //Backendless.UserService.login("cat@cat.cat", "cat", new BackendlessCallback<BackendlessUser>() {
                Backendless.UserService.login(etLoginUsername.getText().toString(), etSignInPassword.getText().toString(), new BackendlessCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser backendlessUser) {
                        String userObjectId = backendlessUser.getObjectId();
                        String inputName = etLoginUsername.getText().toString();
                        String inputPassoword = etSignInPassword.getText().toString();
                        Log.e(TAG, "intputInfo: " + inputName + inputPassoword);
                        SharedPreferences sharedPreferences = ((ActivityLogin)getActivity()).spUserInfo;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.USER_LOGIN_KEY_LOGINNAME, inputName);
                        editor.putString(Constants.USER_LOGIN_KEY_LOGINPASSWORD, inputPassoword);
                        editor.commit();
                        setGlobalUserInfo(inputName, userObjectId);
                        ((ActivityLogin)getActivity()).goToMainUI(userObjectId, inputName);
                    }
                    @Override
                    public void handleFault(BackendlessFault fault){
                        Toast.makeText(getActivity(), fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.btn_login_facebook:
                break;
            case R.id.tv_forget_password:
                break;
            case R.id.tv_switch_to_signup:
                ((ActivityLogin)getActivity()).switchLoginSignupFragment(new FragmentSignup());
                break;
            default:
        }
    }

    /**
     the user info would be used as the identity when sending crash report to our server
     */
    private void setGlobalUserInfo(String username, String userObjectId){
        MyApplication myApplication = (MyApplication)getActivity().getApplication();
        myApplication.setUsername(username);
        myApplication.setUserObjectId(userObjectId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
