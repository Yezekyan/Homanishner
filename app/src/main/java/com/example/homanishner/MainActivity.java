package com.example.homanishner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    String TAG = "HOMANISHNER";
    private static final int RC_SIGN_IN = 88;
    private static final int SETTINGS_CODE = 1;
    private FirebaseAuth auth;
    private GoogleSignInOptions gso;
    private String userId;
    private User cUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private GoogleSignInClient signInClient;
    private CollectionReference usersCol;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        usersCol = db.collection("users");
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)
                ).build();
        signInClient = GoogleSignIn.getClient(this,gso);
        setContentView(R.layout.activity_main);
        ((ImageView) findViewById(R.id.settings_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsClicked(view);
            }
        });
        if (auth.getCurrentUser() !=null){
            FirebaseUser user = auth.getCurrentUser();
            userId = user.getUid();
            DocumentReference userRef = usersCol.document(userId);
            userRef.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    cUser = document.toObject(User.class);
                    updateUI(user);
                }
            });
        }
    }

    private static <T> T getRandomElement(List<T> list) {
        Random rand = new Random();
        int index = rand.nextInt(list.size());
        return list.get(index);
    }
    public void signInStarted(View btn) {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
            signInClient.signOut();
            userId = "";
            cUser = null;
            ((Button) btn).setText(R.string.login_txt);
            ((ImageView) findViewById(R.id.profileAvatar)).setImageResource(R.drawable.baseline_person_off_24);
            ((TextView) findViewById(R.id.score_view)).setText("Score: 0");
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
        Uri photo = user.getPhotoUrl();
        ((TextView)findViewById(R.id.title_text_view)).setText(cUser.getDisplay_name());
        ((TextView)findViewById(R.id.login_btn)).setText(R.string.logout_txt);
        ((TextView)findViewById(R.id.score_view)).setText("Score: "+cUser.getScore());
        ImageView profile_icon = (ImageView) findViewById(R.id.profileAvatar);
        ((ImageView) findViewById(R.id.settings_btn)).setVisibility(View.VISIBLE);
        Picasso.get().load(photo).transform(new CircleTransform()).into(profile_icon);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null){
                        return;
                    }
                    userId = user.getUid();
                    DocumentReference userRef = usersCol.document(userId);
                    userRef.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            if (!document.exists()){
                                cUser = new User(user.getDisplayName(),0);
                                usersCol.document(userId).set(cUser);
                                updateUI(user);
                                return;
                            }
                            cUser = document.toObject(User.class);
                            updateUI(user);
                        }
                    });
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
        if (requestCode == SETTINGS_CODE && resultCode == RESULT_OK){
            if (data == null){
                return;
            }
            String displayName = data.getStringExtra("display_name");
            ((TextView) findViewById(R.id.title_text_view)).setText(displayName);
        }
    }
    public void settingsClicked(View view) {
        Log.d(TAG, "settingsClicked: "+view);
         if (view.getId() == R.id.settings_btn) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent,SETTINGS_CODE);
        }

    }

    public void startGame(View view) {
        if (cUser == null){
            Toast.makeText(this, "Please login before using this!", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("quizes").orderBy(FieldPath.documentId()).get().addOnCompleteListener(task -> {
            List<QuizModel> quizList = new ArrayList<>();
            for (DocumentSnapshot data : task.getResult().getDocuments()) {
                QuizModel model = data.toObject(QuizModel.class);
                model.setDocumentId(data);
                quizList.add(model);
            }
            QuizModel randomQuiz = getRandomElement(quizList);
            String randomQuizId = randomQuiz.getDocumentId();

            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("quiz_id", randomQuizId);
            startActivity(intent);
        });

    }

    public void leaderboardClicked(View view) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser()!=null){
            db.collection("users").document(auth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    cUser = task.getResult().toObject(User.class);
                    updateUI(auth.getCurrentUser());
                }
            });
        }
    }
}