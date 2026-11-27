package com.example.roadsideassistance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
        float rating = ratingBar.getRating();
        String comment = etFeedbackComment.getText() != null ? etFeedbackComment.getText().toString().trim() : "";

        if (rating <= 0) {
            Toast.makeText(this, "Please select a star rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show a clean AlertDialog popup without saving to Firebase
        new AlertDialog.Builder(this)
                .setTitle("Feedback Submitted Successfully!")
                .setMessage("Thank you for giving a " + ((int) rating) + "-star rating! Your feedback helps us improve our roadside service.")
                .setIcon(R.drawable.ic_check_circle)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }
}
