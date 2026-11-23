package com.example.roadsideassistance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextInputEditText etFeedbackComment;
    private MaterialButton btnSubmitFeedback;
    private ProgressBar progressBar;
    private String requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestId = getIntent().getStringExtra("requestId");

        ratingBar = findViewById(R.id.ratingBar);
        etFeedbackComment = findViewById(R.id.etFeedbackComment);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);
        progressBar = findViewById(R.id.progressBar);

        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to submit feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBar.getRating();
        String comment = etFeedbackComment.getText() != null ? etFeedbackComment.getText().toString().trim() : "";

        if (rating <= 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmitFeedback.setEnabled(false);

        String feedbackId = FirebaseDatabase.getInstance().getReference().child("service_feedback").push().getKey();
        String userId = currentUser.getUid();
        long timestamp = System.currentTimeMillis();

        FeedbackModel feedback = new FeedbackModel(feedbackId, requestId, userId, rating, comment, timestamp);

        if (feedbackId == null) {
            feedbackId = "fb_" + timestamp;
        }

        FirebaseDatabase.getInstance().getReference().child("service_feedback")
                .child(feedbackId)
                .setValue(feedback)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitFeedback.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(FeedbackActivity.this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(FeedbackActivity.this, "Failed to submit feedback. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
