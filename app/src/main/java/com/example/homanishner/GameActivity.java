package com.example.homanishner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private QuizModel game;
    private List<Button> btns;
    private ArrayList<String> chosenWords_;
    private Integer score;
    private Integer index;
    private String userId;
    private TextView questionView;

    private static <T> T getRandomElement(List<T> list) {
        Random rand = new Random();
        int index = rand.nextInt(list.size());
        return list.get(index);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        questionView = findViewById(R.id.textView);
        btns = new ArrayList<Button>(){{
            add(findViewById(R.id.button1));
            add(findViewById(R.id.button2));
            add(findViewById(R.id.button3));
            add(findViewById(R.id.button4));
        }};
        Collections.shuffle(btns);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ArmenianWords dictionary = new ArmenianWords();
        Intent payloadIntent = getIntent();
        index = payloadIntent.getIntExtra("index",0);
        score = payloadIntent.getIntExtra("score",0);
        chosenWords_ = payloadIntent.getStringArrayListExtra("words");
        String id = payloadIntent.getStringExtra("quiz_id");
        if (chosenWords_ == null){
            chosenWords_ = new ArrayList<>();
        }
        chosenWords_.add(id);
        if (index>=5){
            Toast.makeText(this, "Total score gained: "+ score, Toast.LENGTH_SHORT).show();
            finish();
        }
        db.collection("quizes").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot response = task.getResult();

                game = response.toObject(QuizModel.class);
                List<String> chosenWords = new ArrayList<>();
                questionView.setText(Html.fromHtml(String.format(questionView.getText().toString(),"<u>"+game.getQuestionWord()+"</u>")));
                for (int i = 0; i < btns.size()-1; i++) {
                    String word = getRandomElement(dictionary.words);
                    while (chosenWords.contains(word)){
                        word = getRandomElement(dictionary.words);
                    }
                    if (chosenWords_ == null){
                        chosenWords_ = new ArrayList<>();
                    }
                    chosenWords.add(word);
                    btns.get(i).setText(word);
                }
                btns.get(3).setText(game.getCorrectAnswer());
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
    }
    public void option_clicked(View view) {
        if (game == null){ finish(); }
        Button btn = (Button) view;
        if (!((Button) view).getText().toString().equalsIgnoreCase(game.getCorrectAnswer())){
            view.setBackgroundColor(Color.parseColor("#ff0000"));
        }
        for (Button btn_:btns) {
            btn_.setClickable(false);
            if (btn_.getText().toString().equalsIgnoreCase(game.getCorrectAnswer())){
                btn_.setBackgroundColor(Color.parseColor("#00ff00"));
            }
        }
        if (btn.getText().toString().equalsIgnoreCase(game.getCorrectAnswer())){
            db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    User user = task.getResult().toObject(User.class);
                    score+=1;
                    User user_ = new User(user.getDisplay_name(),user.getScore()+1);
                    db.collection("users").document(userId).set(user_);
                }
            });
        }
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                db.collection("quizes").orderBy(FieldPath.documentId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<QuizModel> quizList = new ArrayList<>();
                        for (DocumentSnapshot data : task.getResult().getDocuments()) {
                            QuizModel model = data.toObject(QuizModel.class);
                            model.setDocumentId(data);
                            quizList.add(model);
                        }
                        QuizModel randomQuiz = getRandomElement(quizList);
                        Log.d("chosenwords", "onComplete: "+chosenWords_);
                        while (chosenWords_.contains(randomQuiz.getDocumentId())){
                            randomQuiz = getRandomElement(quizList);
                        }
                        String randomQuizId = randomQuiz.getDocumentId();
                        Intent intent = new Intent(GameActivity.this, GameActivity.class);
                        intent.putExtra("quiz_id", randomQuizId);
                        intent.putExtra("score",score);
                        intent.putExtra("index",index+1);
                        intent.putStringArrayListExtra("words",chosenWords_);
                        finish();
                        startActivity(intent);
                    }
                });
            }
        },2500);
    }
}