package com.vinay.spyder.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.SignInButton;
import com.maksim88.easylogin.AccessToken;
import com.maksim88.easylogin.EasyLogin;
import com.maksim88.easylogin.listener.OnLoginCompleteListener;
import com.maksim88.easylogin.networks.GooglePlusNetwork;
import com.maksim88.easylogin.networks.SocialNetwork;
import com.vinay.spyder.R;
import com.vinay.spyder.utils.Preferences;

public class LoginActivity extends AppCompatActivity implements OnLoginCompleteListener {

    private EasyLogin easyLogin;

    private SignInButton gPlusButton;

    private GooglePlusNetwork gPlusNetwork;

    CheckBox save_details;
    EditText et_uname, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasyLogin.initialize();
        easyLogin = EasyLogin.getInstance();
        setContentView(R.layout.activity_login);
        et_uname = findViewById(R.id.et_uname);
        et_password = findViewById(R.id.et_password);
        save_details = findViewById(R.id.is_savepass);
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Checking credentials");
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkEmail() && checkPassword()) {
                    progressDialog.show();
                    String url = "https://whencutwini.000webhostapp.com/spyder/login.php?email=" + et_uname.getText() + "&pwd=" + et_password.getText();
                    RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            //Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                            if (Integer.parseInt(response) > 0) {
                                if (save_details.isChecked())
                                    Preferences.saveCredentials(LoginActivity.this, et_uname.getText().toString(), et_password.getText().toString());
                                else
                                    Preferences.saveCredentials(LoginActivity.this, et_uname.getText().toString(), "");

                                if (com.vinay.spyder.utils.Preferences.isIntialRated(LoginActivity.this)) {
                                    startActivity(new Intent(LoginActivity.this, RatingActivity.class));
                                } else {
                                    startActivity(new Intent(LoginActivity.this, AskToRate.class));
                                }
                            } else
                                Toast.makeText(getApplicationContext(), "wrong credentials", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    requestQueue.add(stringRequest);
                }
            }
        });

        findViewById(R.id.tv_sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        easyLogin.addSocialNetwork(new GooglePlusNetwork(this));
        gPlusNetwork = (GooglePlusNetwork) easyLogin.getSocialNetwork(SocialNetwork.Network.GOOGLE_PLUS);
        gPlusNetwork.setListener(this);

        gPlusButton = (SignInButton) findViewById(R.id.gplus_sign_in_button);

        gPlusNetwork.setSignInButton(gPlusButton);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (gPlusNetwork.isConnected()) {
            gPlusNetwork.silentSignIn();
        } else {
            gPlusButton.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatuses();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        easyLogin.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoginSuccess(SocialNetwork.Network network) {
        if (network == SocialNetwork.Network.GOOGLE_PLUS) {
            AccessToken token = easyLogin.getSocialNetwork(SocialNetwork.Network.GOOGLE_PLUS).getAccessToken();
            Log.d("MAIN", "G+ Login successful: " + token.getToken() + "|||" + token.getEmail());
            final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Authenticating..");
            progressDialog.show();
            String url = "https://whencutwini.000webhostapp.com/spyder/sign_up.php?" +
                    "email=" + token.getEmail() + "&" +
                    "password=" + token.getToken();

            RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    if (com.vinay.spyder.utils.Preferences.isIntialRated(LoginActivity.this)) {
                        startActivity(new Intent(LoginActivity.this, RatingActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, AskToRate.class));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                }
            });
            requestQueue.add(stringRequest);
            gPlusButton.setEnabled(false);
            Preferences.saveCredentials(LoginActivity.this, token.getEmail(), token.getToken());
        }
        updateStatuses();
    }

    @Override
    public void onError(SocialNetwork.Network socialNetwork, String errorMessage) {
        Log.e("MAIN", "ERROR!" + socialNetwork + "|||" + errorMessage);
        Toast.makeText(getApplicationContext(), socialNetwork.name() + ": " + errorMessage,
                Toast.LENGTH_SHORT).show();
    }

    private void updateStatuses() {
        StringBuilder content = new StringBuilder();
        for (SocialNetwork socialNetwork : easyLogin.getInitializedSocialNetworks()) {
            content.append(socialNetwork.getNetwork())
                    .append(": ")
                    .append(socialNetwork.isConnected())
                    .append("\n");
        }
    }

    public void logoutAllNetworks(View view) {
        for (SocialNetwork socialNetwork : easyLogin.getInitializedSocialNetworks()) {
            socialNetwork.logout();
        }
        updateStatuses();
    }

    private boolean checkPassword() {
        if (et_password.getText().toString().trim().length() == 0) {
            et_password.setError("password cannot be empty");
            return false;
        }
        return true;
    }

    private boolean checkEmail() {
        if (et_uname.getText().toString().trim().length() == 0) {
            et_uname.setError("email cannot be empty");
            return false;
        }
        if (!et_uname.getText().toString().contains("@")) {
            et_uname.setError("invalid email");
            return false;
        }
        return true;
    }
}
