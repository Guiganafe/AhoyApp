package com.guiganafe.ahoy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private EditText userEmail, userPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserLoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void InitializeFields() {
        createAccountButton = (Button) findViewById(R.id.register_button);
        userEmail = (EditText)findViewById(R.id.register_email);
        userPassword = (EditText)findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.loggin_existing_account);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void CreateNewAccount(){
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Por favor, insira um e-mail!", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Por favor, insira uma senha!", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Criando uma nova conta");
            loadingBar.setMessage("Por favor, espere enquanto criamos a sua conta...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                     if(task.isSuccessful()){
                         String currentUserId = mAuth.getCurrentUser().getUid();
                         rootRef.child("Users").child(currentUserId).setValue("");

                         SendUserToMainActivity();
                         Toast.makeText(RegisterActivity.this, "Conta criada com sucesso", Toast.LENGTH_SHORT).show();
                         loadingBar.dismiss();
                     }else{
                         String message = task.getException().toString();
                         Toast.makeText(RegisterActivity.this, "Erro: " + message, Toast.LENGTH_SHORT).show();
                         loadingBar.dismiss();
                     }
                }
            });
        }

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
