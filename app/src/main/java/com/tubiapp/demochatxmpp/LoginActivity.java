package com.tubiapp.demochatxmpp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tubiapp.demochatxmpp.apis.Api;
import com.tubiapp.demochatxmpp.apis.model.MyResponse;
import com.tubiapp.demochatxmpp.apis.model.User;

import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText mEdtEmail;
    private EditText mEdtPassword;
    private Button mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initComponents();
        mBtnLogin.setOnClickListener(this);
    }

    private void initComponents() {
        mEdtEmail = (EditText) findViewById(R.id.edt_email);
        mEdtPassword = (EditText) findViewById(R.id.edt_password);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mEdtEmail.setText("admin@binhcq-imac.local");
        mEdtPassword.setText("admin");
    }

    @Override
    public void onClick(View view) {
        String email = mEdtEmail.getText().toString().trim();
        String password = mEdtPassword.getText().toString();
        final User user = new User(email, password);
        Api.getInstance().login(user, new Api.APICallback<MyResponse>() {
            @Override
            public void success(MyResponse myResponse, Response response) {
                Log.i("API", myResponse.toString());
                Toast.makeText(LoginActivity.this, myResponse.toString(), Toast.LENGTH_LONG).show();
                gotoChatActivity(user);
            }

            private void gotoChatActivity(User user) {
                Intent chatIntent = new Intent(LoginActivity.this, ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(ChatActivity.KEY_EMAIL, user.getEmail());
                bundle.putString(ChatActivity.KEY_PASSWORD, user.getPassword());
                chatIntent.putExtras(bundle);
                startActivity(chatIntent);
                finish();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(LoginActivity.this, "Error" + error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }
}
