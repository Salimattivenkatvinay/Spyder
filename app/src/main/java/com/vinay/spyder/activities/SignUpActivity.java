package com.vinay.spyder.activities;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vinay.spyder.R;

public class SignUpActivity extends AppCompatActivity {

    EditText et_uname,et_repassword,et_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        et_uname=findViewById(R.id.et_uname);
        et_password=findViewById(R.id.et_password);
        et_repassword=findViewById(R.id.et_repassword);
        final ProgressDialog progressDialog=new ProgressDialog(SignUpActivity.this);
        progressDialog.setMessage("creating account");
        findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkEmail() && checkPassword()) {
                    progressDialog.show();
                    String url = "https://whencutwini.000webhostapp.com/spyder/sign_up.php?email="+et_uname.getText()+"&password="+et_password.getText();
                    RequestQueue requestQueue = Volley.newRequestQueue(SignUpActivity.this);
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                    requestQueue.add(stringRequest);
                }
            }
        });
    }

    private boolean checkPassword() {
        if(et_password.getText().toString().trim().length()==0){
            et_password.setError("password cannot be empty");
            return false;
        }
        if(et_repassword.getText().toString().trim().length()==0){
            et_repassword.setError("re enter password");
            return false;
        }
        if(!et_repassword.getText().toString().equals(et_password.getText().toString())){
            et_repassword.setError("passwords didnot match");
            return false;
        }
        return true;
    }

    private boolean checkEmail() {
        if(et_uname.getText().toString().trim().length()==0) {
            et_uname.setError("email cannot be empty");
            return false;
        }
        if(!et_uname.getText().toString().contains("@")){
            et_uname.setError("invalid email");
            return false;
        }        
        return true;
    }

}
