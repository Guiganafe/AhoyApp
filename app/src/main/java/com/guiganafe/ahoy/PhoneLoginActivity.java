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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationButton, verifyButton;
    private EditText phoneNumber, verificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        sendVerificationButton = (Button) findViewById(R.id.send_verification_code);
        verifyButton = (Button) findViewById(R.id.verify_code);
        phoneNumber = (EditText)findViewById(R.id.phone_number_input);
        verificationCode = (EditText) findViewById(R.id.phone_code_input);
        loadingBar = new ProgressDialog(this);

        sendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phonenNumberInput = phoneNumber.getText().toString();
                if(TextUtils.isEmpty(phonenNumberInput)){
                    Toast.makeText(PhoneLoginActivity.this, "Por favor, digite um número válido",Toast.LENGTH_SHORT).show();
                }else{

                    loadingBar .setTitle("Verificaçãodo do número");
                    loadingBar.setMessage("Por favor espere, nós estamos autenticando seu número");
                    loadingBar.setCanceledOnTouchOutside(false );
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phonenNumberInput,                  // Phone number to verify
                            60,                              // Timeout duration
                            TimeUnit.SECONDS,                   // Unit of timeout
                            PhoneLoginActivity.this,     // Activity (for callback binding)
                            callbacks);                         // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationButton.setVisibility(View.INVISIBLE);
                phoneNumber.setVisibility(View.INVISIBLE);

                String codeVerified = verificationCode.getText().toString();

                if(TextUtils.isEmpty(codeVerified )){
                    Toast.makeText(PhoneLoginActivity.this, "Por favor, digite o código de verificação",Toast.LENGTH_SHORT).show();
                }else{

                    loadingBar .setTitle("Verificaçãodo do código");
                    loadingBar.setMessage("Por favor espere, nós estamos autenticando seu código");
                    loadingBar.setCanceledOnTouchOutside(false );
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codeVerified);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Número válido, digite um número correto associado a sua conta",Toast.LENGTH_SHORT).show();

                sendVerificationButton.setVisibility(View.VISIBLE);
                phoneNumber.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                verificationCode.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Código enviado com sucesso",Toast.LENGTH_SHORT).show();

                sendVerificationButton.setVisibility(View.INVISIBLE);
                phoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                verificationCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                                loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Você foi logado com sucesso",Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();

                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Erro: "+ message,Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
