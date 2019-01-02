package com.ads2dios.adrieladtan.letstalk;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {

    ImageView profile;
    TextView headerTV;
    EditText firstET;
    EditText secondET;
    EditText thirdET;
    EditText fourthET;
    Button firstButt;
    Button secondButt;
    Button thirdButt;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    int mode;
    final int LOGGED_OUT = 0;
    final int LOGGED_IN = 1;
    final int CHANGE_NAME = 2;
    final int CHANGE_PASSWORD = 3;
    final int CHANGE_PROFILE = 4;
    final int SIGN_UP = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        profile = findViewById(R.id.profile);
        headerTV = findViewById(R.id.headerTV);
        firstET = findViewById(R.id.firstET);
        secondET = findViewById(R.id.secondET);
        thirdET = findViewById(R.id.thirdET);
        fourthET = findViewById(R.id.fourthET);
        firstButt = findViewById(R.id.firstButt);
        secondButt = findViewById(R.id.secondButt);
        thirdButt = findViewById(R.id.thirdButt);

        firstButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mode){
                    case LOGGED_OUT:
                        mAuth.signInWithEmailAndPassword(firstET.getText().toString(), secondET.getText().toString())
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            currentUser = mAuth.getCurrentUser();
                                            setMode(LOGGED_IN);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        break;
                    case LOGGED_IN:
                        setMode(CHANGE_NAME);
                        break;
                    case CHANGE_NAME:
                        final String newName = firstET.getText().toString();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build();
                        currentUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Name changed to " + newName, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        setMode(LOGGED_IN);
                        break;
                    case CHANGE_PASSWORD:
                        final String newPassword = secondET.getText().toString();
                        if (newPassword.equals(thirdET.getText().toString())){
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(currentUser.getEmail(), firstET.getText().toString());
                            currentUser.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                currentUser.updatePassword(newPassword)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(LoginActivity.this, "Password changed", Toast.LENGTH_SHORT).show();
                                                                    setMode(LOGGED_IN);
                                                                }
                                                                else{
                                                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                            else{
                                                Toast.makeText(LoginActivity.this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case SIGN_UP:
                        final String thePassword = thirdET.getText().toString();
                        if (thePassword.equals(fourthET.getText().toString())) {
                            mAuth.createUserWithEmailAndPassword(secondET.getText().toString(), thePassword)
                                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(firstET.getText().toString())
                                                        .build();
                                                currentUser.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(LoginActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                setMode(LOGGED_IN);
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
        secondButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mode){
                    case LOGGED_OUT:
                        setMode(SIGN_UP);
                        break;
                    case LOGGED_IN:
                        setMode(CHANGE_PASSWORD);
                        break;
                }
            }
        });
        thirdButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode==LOGGED_IN){
                    mAuth.signOut();
                    currentUser = null;
                    setMode(LOGGED_OUT);
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            setMode(LOGGED_IN);
        }
        else {
            setMode(LOGGED_OUT);
        }
    }

    private void setMode(int newMode){
        mode = newMode;
        currentUser = mAuth.getCurrentUser();

        switch (mode){
            case LOGGED_OUT:
                profile.setVisibility(View.GONE);
                headerTV.setVisibility(View.GONE);
                firstET.setVisibility(View.VISIBLE);
                firstET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                firstET.setText("");
                firstET.setHint("Email");
                secondET.setVisibility(View.VISIBLE);
                secondET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                secondET.setText("");
                secondET.setHint("Password");
                thirdET.setVisibility(View.GONE);
                fourthET.setVisibility(View.GONE);
                firstButt.setVisibility(View.VISIBLE);
                firstButt.setText("Login");
                secondButt.setVisibility(View.VISIBLE);
                secondButt.setText("Sign up");
                thirdButt.setVisibility(View.GONE);
                break;
            case LOGGED_IN:
                profile.setVisibility(View.VISIBLE);
                profile.setImageURI(currentUser.getPhotoUrl());
                headerTV.setVisibility(View.VISIBLE);
                headerTV.setText("Welcome, " + currentUser.getDisplayName());
                firstET.setVisibility(View.GONE);
                secondET.setVisibility(View.GONE);
                thirdET.setVisibility(View.GONE);
                fourthET.setVisibility(View.GONE);
                firstButt.setVisibility(View.VISIBLE);
                firstButt.setText("Change Name");
                secondButt.setVisibility(View.VISIBLE);
                secondButt.setText("Change Password");
                thirdButt.setVisibility(View.VISIBLE);
                thirdButt.setText("Sign Out");
                break;
            case CHANGE_NAME:
                profile.setVisibility(View.GONE);
                headerTV.setVisibility(View.VISIBLE);
                headerTV.setText("Change name for " + currentUser.getEmail());
                firstET.setVisibility(View.VISIBLE);
                firstET.setInputType(InputType.TYPE_CLASS_TEXT);
                firstET.setText("");
                firstET.setHint("New name");
                secondET.setVisibility(View.GONE);
                thirdET.setVisibility(View.GONE);
                fourthET.setVisibility(View.GONE);
                firstButt.setVisibility(View.VISIBLE);
                firstButt.setText("Update");
                secondButt.setVisibility(View.GONE);
                thirdButt.setVisibility(View.GONE);
                break;
            case CHANGE_PASSWORD:
                profile.setVisibility(View.GONE);
                headerTV.setVisibility(View.VISIBLE);
                headerTV.setText("Change password");
                firstET.setVisibility(View.VISIBLE);
                firstET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                firstET.setText("");
                firstET.setHint("Old password");
                secondET.setVisibility(View.VISIBLE);
                secondET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                secondET.setText("");
                secondET.setHint("New Password");
                thirdET.setVisibility(View.VISIBLE);
                thirdET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                thirdET.setText("");
                thirdET.setHint("Confirm New Password");
                fourthET.setVisibility(View.GONE);
                firstButt.setVisibility(View.VISIBLE);
                firstButt.setText("Change Password");
                secondButt.setVisibility(View.GONE);
                thirdButt.setVisibility(View.GONE);
                break;
            case SIGN_UP:
                profile.setVisibility(View.GONE);
                headerTV.setVisibility(View.VISIBLE);
                headerTV.setText("Create a new account");
                firstET.setVisibility(View.VISIBLE);
                firstET.setInputType(InputType.TYPE_CLASS_TEXT);
                firstET.setText("");
                firstET.setHint("Name");
                secondET.setVisibility(View.VISIBLE);
                secondET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                secondET.setText("");
                secondET.setHint("Email");
                thirdET.setVisibility(View.VISIBLE);
                thirdET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                thirdET.setText("");
                thirdET.setHint("Password");
                fourthET.setVisibility(View.VISIBLE);
                fourthET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                fourthET.setText("");
                fourthET.setHint("Confirm Password");
                firstButt.setVisibility(View.VISIBLE);
                firstButt.setText("Sign up");
                secondButt.setVisibility(View.GONE);
                thirdButt.setVisibility(View.GONE);
                break;
        }
    }
}
