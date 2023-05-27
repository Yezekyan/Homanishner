package com.example.homanishner;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User user;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            user = task.getResult().toObject(User.class);
            ((TextView) findViewById(R.id.display_name_edittext)).setHint("Currently set \""+user.getDisplay_name()+"\"");
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void confirmUserChange(View view) {
        String newUsername = ((EditText)findViewById(R.id.display_name_edittext)).getText().toString();
        if (newUsername.replaceAll(" ","").length()<5){
            Toast.makeText(this, "Username must contain at least 5 non whitespace characters!", Toast.LENGTH_SHORT).show();
            return;
        }
        user = new User(newUsername,user.getScore());
        db.collection("users").document(userId).set(user);
        Toast.makeText(this, "Username successfully changed!", Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("display_name",newUsername);
        setResult(RESULT_OK,resultIntent);
        finish();
    }
}