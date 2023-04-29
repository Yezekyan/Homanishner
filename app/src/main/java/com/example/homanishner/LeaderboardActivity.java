package com.example.homanishner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<User> leaderboard = new ArrayList<>();
        Query leaderboardQuery = db.collection("users")
                .orderBy("score", Query.Direction.DESCENDING).limit(50);
        leaderboardQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> leaderboard_ = task.getResult().getDocuments();
                for (DocumentSnapshot data: leaderboard_) {
                    leaderboard.add(data.toObject(User.class));
                }
                LeaderboardAdapter adapter = new LeaderboardAdapter(leaderboard);
                RecyclerView rv = ((RecyclerView) findViewById(R.id.recycler_view));
                rv.setAdapter(adapter);
                rv.setLayoutManager(new LinearLayoutManager(LeaderboardActivity.this));
                (findViewById(R.id.progressBar2)).setVisibility(View.GONE);
            }
        });
    }
}