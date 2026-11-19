package com.example.roadsideassistance;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String PREF_NAME = "RoadsideAssistancePrefs";
    private static final String KEY_PREFIX_PASS = "pass_";
    private static final String KEY_PREFIX_QUESTION = "question_";
    private static final String KEY_PREFIX_ANSWER = "answer_";

    public static void saveUser(Context context, String email, String password, String question, String answer) {
        if (email == null) return;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String cleanEmail = email.trim().toLowerCase();
        editor.putString(KEY_PREFIX_PASS + cleanEmail, password);
        if (question != null) editor.putString(KEY_PREFIX_QUESTION + cleanEmail, question);
        if (answer != null) editor.putString(KEY_PREFIX_ANSWER + cleanEmail, answer.trim().toLowerCase());
        editor.apply();
    }

    public static void updatePassword(Context context, String email, String newPassword) {
        if (email == null) return;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String cleanEmail = email.trim().toLowerCase();
        pref.edit().putString(KEY_PREFIX_PASS + cleanEmail, newPassword).apply();
    }

    public static String getPassword(Context context, String email) {
        if (email == null) return null;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_PREFIX_PASS + email.trim().toLowerCase(), null);
    }

    public static String getSecurityQuestion(Context context, String email) {
        if (email == null) return null;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_PREFIX_QUESTION + email.trim().toLowerCase(), null);
    }

    public static String getSecurityAnswer(Context context, String email) {
        if (email == null) return null;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_PREFIX_ANSWER + email.trim().toLowerCase(), null);
    }

    public static boolean verifySecurityAnswer(Context context, String email, String answer) {
        if (answer == null || answer.trim().isEmpty()) return false;
        String storedAnswer = getSecurityAnswer(context, email);
        if (storedAnswer != null) {
            return storedAnswer.equalsIgnoreCase(answer.trim());
        }
        return true;
    }
}