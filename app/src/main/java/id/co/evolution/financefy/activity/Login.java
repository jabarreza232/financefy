package id.co.evolution.financefy.activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.File;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.model.ModelUser;

public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int GOOGLE_REEGISTER = 9001;
    private GoogleApiClient mGoogleApiClient;
    @BindView(R.id.register_with_google)
    Button register_gmail;
    @BindView(R.id.loginFacebook)
    Button loginButton;
    CallbackManager callbackManager;
    String TAG = Login.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    TinyDb tinydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppEventsLogger.activateApp(getApplication());
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        AppEventsLogger.activateApp(getApplication());
        tinydb = new TinyDb(this);
        authListener();
        tinydb = new TinyDb(this);
        hashLogin();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if (isLoggedIn) {
            LoginManager.getInstance().logOut();
        }

        loginFacebook();
        loginGmail();
    }

    private void hashLogin() {
        if (tinydb.getBoolean("isLogin")) {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }
        return;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_REEGISTER) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Log.d("loginSuccess", "success login" + result.getStatus().getStatusMessage());
            } else {
                // Google Sign In failed, update UI appropriately
                Log.e("errorlogingoogle", "failed login" + result.getStatus().getStatusMessage());
            }

        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void authListener() {

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    tinydb.putBoolean("isLogin", true);
                } else {
                    // User is signed out
                    tinydb.putBoolean("isLogin", false);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void loginGmail() {
        Log.e("cek web api", getString(R.string.default_web_client_id));
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void loginFacebook() {
//        loginButton.setReadPermissions("email", "public_profile");
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(Login.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                handleFacebookAccessToken(loginResult.getAccessToken());
                            }

                            @Override
                            public void onCancel() {
                                Toast.makeText(Login.this, "Login Cancel", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                Toast.makeText(Login.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });


            }
        });
    }


    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        Log.d("tokenGmail", acct.getIdToken());
//        acct.getServerAuthCode()
        showProgressDialog();
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithCredential", task.getException());
                    Toast.makeText(Login.this, "Authentication failed google.",
                            Toast.LENGTH_SHORT).show();

                    // If sign in fails, display a message to the user.
                } else {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    ModelUser userModel = new ModelUser();

                    userModel.setImage(user.getPhotoUrl().toString());
                    userModel.setEmail(user.getEmail());
                    userModel.setName(user.getDisplayName());
                    tinydb.putObject("user", userModel);
                    Intent i = new Intent(Login.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    hideProgressDialog();
                }

            }
        });
    }

    //
//    // [END auth_with_google]
    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d("tokenFacebook:", token.getToken());
        showProgressDialog();
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {

                            //                            jika akun dengan email sama sudah terdaftar dengan provider lain ex:sudah login dengan gmail
                            Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            hideProgressDialog();
                            FirebaseUser user = mAuth.getCurrentUser();
                            ModelUser userModel = new ModelUser();
                            userModel.setImage(user.getPhotoUrl().toString());
                            userModel.setEmail(user.getEmail());
                            userModel.setName(user.getDisplayName());
                            tinydb.putObject("user", userModel);
                            Intent i = new Intent(Login.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            //jika login dengan facebook berhasil dan belum pernah di pakai emailnya
                            Log.d("cek:", "provider:" + credential.getProvider() + "token:" + token.getToken());
                        }
                    }
                });
    }


    @OnClick(R.id.register_with_google)
    protected void onClickGmail() {
        signIn();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_REEGISTER);
    }

    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

}
