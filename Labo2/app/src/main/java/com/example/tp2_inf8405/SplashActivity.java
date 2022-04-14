package com.example.tp2_inf8405;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplashActivity extends AppCompatActivity {
    TextView textView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage;
    StorageReference storageReference;
    Bitmap user_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        imageView = findViewById(R.id.avatar);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            user_photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(user_photo);
        }
    }

    public void saveUserPicture(String name){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        user_photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
        db.collection("users").add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("tag", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("tag", "Error adding document", e);
                    }
                });
    }

    public void verifyUser(String email, String password){
        final boolean[] user_validated = {false};
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData().get("email"));
                                if (email.equals(document.getData().get("email").toString())){
                                    if (password.equals(document.getData().get("password").toString())){
                                        Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                                        intent.putExtra("user_email", email);
                                        startActivity(intent);
                                        user_validated[0] = true;
                                    }
                                }
                            }
                            if (!user_validated[0]){
                                findViewById(R.id.incorrect_login).setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
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
}