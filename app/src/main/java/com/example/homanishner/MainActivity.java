package com.example.homanishner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 88;
    private FirebaseAuth auth;
    private GoogleSignInOptions gso;
    private GoogleSignInClient signInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)
                ).build();
        signInClient = GoogleSignIn.getClient(this,gso);
        setContentView(R.layout.activity_main);
    }


    public void signInStarted(View btn) {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
            signInClient.signOut();
            ((Button) btn).setText(R.string.login_txt);
            ((ImageView) findViewById(R.id.profileAvatar)).setImageResource(R.drawable.baseline_person_off_24);
            ((TextView) findViewById(R.id.title_text_view)).setText("Anonymous");
            return;
        }
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void updateUI(FirebaseUser user){
        if (user == null){
            return;
        }
        String name = user.getDisplayName();
        Uri photo = user.getPhotoUrl();
        ((TextView)findViewById(R.id.title_text_view)).setText(name);
        ((TextView)findViewById(R.id.login_btn)).setText(R.string.logout_txt);
        ImageView profile_icon = (ImageView) findViewById(R.id.profileAvatar);
        Picasso.get().load(photo).transform(new CircleTransform()).into(profile_icon);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("HOMANISHNER","WORKED? "+task.isSuccessful());
                if (task.isSuccessful()){
                    Log.d("HOMANISHNER","signInCredential");
                    FirebaseUser user = auth.getCurrentUser();
                    updateUI(user);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }
            catch (ApiException e){
                Log.d("HOMANISHNER","FAILED_SIGNIN "+e.getMessage());
            }
        }
    }

}