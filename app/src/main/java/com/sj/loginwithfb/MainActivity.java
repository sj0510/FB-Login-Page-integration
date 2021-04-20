package com.sj.loginwithfb;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private TextView userName,email_ID;
    private ImageView profilePic;
    private LoginButton loginButton;
    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.login_button);
        userName = findViewById(R.id.tvUser);
        email_ID = findViewById(R.id.tv_mailId);
        profilePic = findViewById(R.id.imgProfilePic);

        callbackManager = CallbackManager.Factory.create();

        //FacebookSdk.sdkInitialize(getApplicationContext());
        checkLoginStatus();

        loginButton.setReadPermissions(Arrays.asList("email","public_profile"));
        // If you are using in a fragment, call loginButton.setFragment(this);
        LoginWithFB();


    } // onCreate END


        private void LoginWithFB(){
            // Callback registration
            LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,Arrays.asList("email","public_profile"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Toast.makeText(MainActivity.this,"Login Successful..",Toast.LENGTH_LONG).show();
                    Intent nextIntent = new Intent(MainActivity.this,AnotherActivity.class);
                    startActivity(nextIntent);
                }

                @Override
                public void onCancel() {
                    userName.setText("Login CANCELED... Try Again...!");
                }

                @Override
                public void onError(FacebookException error) {

                    userName.setText("Error... Try again..!"+error.getMessage());
                }
            });
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            if (currentAccessToken==null){
                userName.setText("");
                email_ID.setText("");
                profilePic.setImageResource(0);

                Toast.makeText(MainActivity.this,"User Logged out successfully..!",Toast.LENGTH_LONG).show();

            }
            else {
                loadUserProfile(currentAccessToken);
            }
        }
    };

    private void loadUserProfile(AccessToken newAccessToken){

        GraphRequest request = GraphRequest.newMeRequest(newAccessToken,
                (JSONObject object, GraphResponse response) -> {

            try {
                String first_name = object.getString("first_name");
                String last_name = object.getString("last_name");
                String email = object.getString("email");
                String fbId = object.getString("id");

                // construct the URL to the profile picture, with a custom height
                // alternatively, use '?type=small|medium|large' instead of ?height=
                String photoUrl = "https://graph.facebook.com/" + fbId + "/picture?height=100";

                // (optional) use Picasso to download and show to image

                Picasso.get().load(photoUrl).into(profilePic);

                userName.setText(first_name+" "+last_name);
                email_ID.setText(email);

//

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields","first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void checkLoginStatus(){
        if (AccessToken.getCurrentAccessToken() !=null){
            loadUserProfile(AccessToken.getCurrentAccessToken());
        }
    }
}
