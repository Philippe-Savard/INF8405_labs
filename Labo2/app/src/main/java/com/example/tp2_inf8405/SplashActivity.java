package com.example.tp2_inf8405;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplashActivity extends AppCompatActivity {
    TextView textView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Bitmap user_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Retrieve language preferences
        loadLocale();
        setContentView(R.layout.activity_splash);
        waitForTimer();
        hideToolBr();
    }

    public void waitForTimer(){
        new CountDownTimer(3000, 1000) { // Create a timer for 3 seconds for the pop up to automatically disappear
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }

            public void onFinish() {
                setContentView(R.layout.login_page);
                findViewById(R.id.incorrect_email).setVisibility(View.GONE);
                findViewById(R.id.incorrect_login).setVisibility(View.GONE);
                findViewById(R.id.incorrect_password).setVisibility(View.GONE);
            }
        }.start();
    }

    public void on_sign_up_click(View view){
        setContentView(R.layout.sign_up_page);
        findViewById(R.id.incorrect_email).setVisibility(View.GONE);
        findViewById(R.id.incorrect_password).setVisibility(View.GONE);
        findViewById(R.id.incorrect_name).setVisibility(View.GONE);
    }

    public void on_login_click(View view){
        setContentView(R.layout.login_page);
        findViewById(R.id.incorrect_email).setVisibility(View.GONE);
        findViewById(R.id.incorrect_login).setVisibility(View.GONE);
        findViewById(R.id.incorrect_password).setVisibility(View.GONE);
    }

    public void on_login_button_click(View view){
        EditText email = findViewById(R.id.login_email);
        EditText password = findViewById(R.id.login_password);

        if (verify_params(true, email, password)){
            verifyUser(email.getText().toString(), password.getText().toString());
        }
    }

    public void on_signup_button_click(View view){
        EditText name = findViewById(R.id.signup_name);
        EditText email = findViewById(R.id.signup_email);
        EditText password = findViewById(R.id.signup_password);

        if (verify_params(true, email, password, name)){
            saveUserPicture(name.getText().toString());
            registerUser(email.getText().toString(), password.getText().toString(), name.getText().toString());
            Intent intent = new Intent(getBaseContext(), MapsActivity.class);
            intent.putExtra("user_email", email.getText().toString());
            startActivity(intent);
        }
    }
    
    public void on_picture_button_click(View view){
        this.imageView = findViewById(R.id.avatar);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle  extras = data.getExtras();
            Bitmap  user_photo = (Bitmap) extras.get("data");
            if (user_photo != null) {
                this.imageView.setImageBitmap(user_photo);
            }
        }
    }

    public void saveUserPicture(String name){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.user_photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageEncoded = Base64.encode(baos.toByteArray(), Base64.DEFAULT);
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("Base64 - " + name);
        ref.putBytes(imageEncoded);
    }

    public void registerUser(String email, String password, String name){
        Log.i("tag", "registering user");
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);
        user.put("name", name);
        // Add a new document with a generated ID
        db.collection("users").document(email).set(user);
    }

    public void verifyUser(String email, String password){
        Task<DocumentSnapshot> document = db.collection("users").document(email).get();
        document.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (password.equals(task.getResult().getData().get("password").toString())){
                    Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                    intent.putExtra("user_email", email);
                    startActivity(intent);
                } else {
                    findViewById(R.id.incorrect_login).setVisibility(View.VISIBLE);
                }
            } else {
                Log.w("TAG", "Error getting documents.", task.getException());
            }
        });

    }

    public boolean verify_params(boolean isSignUp, EditText email, EditText password){
        boolean valid_params = true;
        if (!isEmailValid(email.getText().toString())){
            findViewById(R.id.incorrect_email).setVisibility(View.VISIBLE);
            valid_params = false;
        } else {
            findViewById(R.id.incorrect_email).setVisibility(View.GONE);
        }

        if (password.getText().toString().isEmpty()){
            findViewById(R.id.incorrect_password).setVisibility(View.VISIBLE);
            valid_params = false;
        } else {
            findViewById(R.id.incorrect_password).setVisibility(View.GONE);
        }
        return valid_params;
    }

    public boolean verify_params(boolean isSignUp, EditText email, EditText password, EditText name){
        boolean valid_params = true;
        valid_params = verify_params(isSignUp, email, password);

        if (isSignUp){
            if (name.getText().toString().isEmpty()){
                findViewById(R.id.incorrect_name).setVisibility(View.VISIBLE);
                valid_params = false;
            } else {
                findViewById(R.id.incorrect_name).setVisibility(View.GONE);
            }
        }
        return valid_params;
    }

    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Hide the view of top tool bar with TP2 name on it
    public void hideToolBr() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }
    /**
     * Function that displays the device information in a uniform way.
     * @param lang the choosen language to set
     */
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        //Save Data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My Language", lang);
        editor.apply();
    }

    /**
     *  Function that retrieves the language preferences of the user in the MapsActivity
     */
    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String langue = prefs.getString("My Language", "");
        setLocale(langue);
    }
}