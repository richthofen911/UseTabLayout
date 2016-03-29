package io.ap1.proximity.view;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
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
import io.ap1.proximity.R;

/**
 * Created by admin on 08/02/16.
 */
public class FragmentLogin extends Fragment implements View.OnClickListener {
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
                Backendless.UserService.login("cat@cat.cat", "cat", new BackendlessCallback<BackendlessUser>() {
                //Backendless.UserService.login(etLoginUsername.getText().toString(), etSignInPassword.getText().toString(), new BackendlessCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser backendlessUser) {
                        Log.e("login", backendlessUser.getEmail() + " successfully login " + backendlessUser.getObjectId());
                        ((ActivityLogin)getActivity()).goToMainUI(backendlessUser.getObjectId());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
