package com.example.homanishner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private QuizModel game;
    private List<Button> btns;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        btns = new ArrayList<Button>(){{
            add(findViewById(R.id.button1));
            add(findViewById(R.id.button2));
            add(findViewById(R.id.button3));
            add(findViewById(R.id.button4));
        }};
        ArmenianWords dictionary = new ArmenianWords();
        Random random = new Random();
        Intent payloadIntent = getIntent();
        String id = payloadIntent.getStringExtra("quiz_id");
        db.collection("quizes").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot response = task.getResult();
                game = response.toObject(QuizModel.class);

            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    public void option_clicked(View view) {
        if (game == null){ return; }
        Button btn = (Button) view;
        for (Button btn_:btns) {
            btn_.setClickable(false);
            if (btn_.getText().toString().toLowerCase().equals(game.getCorrectAnswer().toLowerCase())){
                btn_.setBackgroundColor(Color.parseColor("#00ff00"));
                continue;
            }
            btn_.setBackgroundColor(Color.parseColor("#ff0000"));
        }
        if (btn.getText().toString().toLowerCase().equals(game.getCorrectAnswer().toLowerCase())){
            db.collection("users")
        }
    }
}