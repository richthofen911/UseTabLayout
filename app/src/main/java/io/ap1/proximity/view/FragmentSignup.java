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
public class FragmentSignup extends Fragment implements View.OnClickListener {
    @Bind(R.id.tv_title_signup)
    TextView tvTitleSignup;
    @Bind(R.id.et_signup_username)
    EditText etSignupUsername;
    @Bind(R.id.et_signup_email)
    EditText etSignupEmail;
    @Bind(R.id.et_signup_password)
    EditText etSignupPassword;
    @Bind(R.id.btn_signup)
    Button btnSignup;
    @Bind(R.id.btn_signup_facebook)
    Button btnSignupFacebook;
    @Bind(R.id.tv_switch_to_login)
    TextView tvSwitchToLogin;

    BackendlessUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        ButterKnife.bind(this, view);
        bindOnClickListeners();
        user = ((ActivityLogin)getActivity()).backendlessUser;

        return view;
    }

    private void bindOnClickListeners(){
        btnSignup.setOnClickListener(this);
        btnSignupFacebook.setOnClickListener(this);
        tvSwitchToLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_signup:
                user.setProperty("name", etSignupUsername.getText().toString());
                user.setEmail(etSignupEmail.getText().toString());
                user.setPassword(etSignupPassword.getText().toString());
                Backendless.UserService.register(user, new BackendlessCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser backendlessUser) {
                        Log.e("Registration", backendlessUser.getEmail() + " successfully registered");
                        etSignupUsername.setText(null);
                        etSignupEmail.setText(null);
                        etSignupEmail.setText(null);
                        user.clearProperties();
                        ((ActivityLogin)getActivity()).switchLoginSignupFragment(new FragmentLogin());
                    }
                    @Override
                    public void handleFault(BackendlessFault fault){
                        Toast.makeText(getActivity(), fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.btn_signup_facebook:
                break;
            case R.id.tv_switch_to_login:
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_login_container, new FragmentLogin());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            default:
        }
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
