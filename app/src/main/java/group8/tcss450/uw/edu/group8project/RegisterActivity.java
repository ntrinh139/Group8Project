package group8.tcss450.uw.edu.group8project;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * RegisterActivity class display sign up layout
 * where user input there email, password and confirm password
 * System will check the validation of user's input,,
 * send email verification if pass authentication
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, GetWebServiceTaskDelegate {

    private final AppCompatActivity activity = RegisterActivity.this;

    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;

    private TextInputEditText edittextEmail;
    private TextInputEditText edittextPassword;
    private TextInputEditText edittextConfirmPassword;

    private AppCompatButton signUP;
    private AppCompatTextView logIN;

    private InputValidation inputValidation;

    private FirebaseAuth mAuth;


    /*
     * Initialize activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        //initialize views
        layoutEmail = (TextInputLayout) findViewById(R.id.layoutEmail);
        layoutPassword = (TextInputLayout) findViewById(R.id.layoutPassword);
        layoutConfirmPassword = (TextInputLayout) findViewById(R.id.layoutConfirmPassword);

        edittextEmail = (TextInputEditText) findViewById(R.id.edittextEmail);
        edittextPassword = (TextInputEditText) findViewById(R.id.edittextPassword);
        edittextConfirmPassword = (TextInputEditText) findViewById(R.id.edittextConfirmPassword);

        signUP = (AppCompatButton) findViewById(R.id.signUP);
        logIN = (AppCompatTextView) findViewById(R.id.logIN);

        //initialize listener
        signUP.setOnClickListener(this);
        logIN.setOnClickListener(this);

        //initialize objects
        inputValidation = new InputValidation(activity);
        mAuth = FirebaseAuth.getInstance();
    }

    /*
     * Launch activity if different button is pressed
     * If click signup -> check authentication and send email verification
     * If click log in -> launch Loginactivity
     */
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.signUP:
                if (!areInputsValid()) {
                    break;
                }

                final String email = edittextEmail.getText().toString().trim();
                final String password = edittextPassword.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d("TAG", "createUserWithEmail:onComplete:" + task.isSuccessful());
                                if (!task.isSuccessful()) {
                                    handleFailure("");
                                } else {
                                    handleSuccess();
                                }
                            }
                        });

                break;
            case R.id.logIN:
                Intent intentLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intentLogin);
                break;
        }
    }

    private boolean areInputsValid(){

        //check if email field is filled
        if (!inputValidation.isTextEditFilled(edittextEmail, layoutEmail, getString(R.string.error_empty_email))) {
            edittextEmail.requestFocus();
            return false;
        }

        //check if email is in valid format (example@hehe.haha)
        if (!inputValidation.isEmailValid(edittextEmail, layoutEmail, getString(R.string.error_message_email))) {
            edittextEmail.requestFocus();
            return false;
        }

        //check if password field is filled
        if (!inputValidation.isTextEditFilled(edittextPassword, layoutPassword, getString(R.string.error_empty_password))) {
            edittextPassword.requestFocus();
            return false;
        }

        //check if password is valid (at least 6 characters
        // including 1 lower, 1 upper, 1 special character and 1 number.
        if (!inputValidation.isPasswordValid(edittextPassword, layoutPassword,
                getString(R.string.error_validPassword1), getString(R.string.error_validPassword2))) {
            edittextPassword.requestFocus();
            return false;
        }

        //check if the confirm password is filled
        if (!inputValidation.isTextEditFilled(edittextConfirmPassword, layoutConfirmPassword, getString(R.string.error_empty_confirmPassword))) {
            edittextConfirmPassword.requestFocus();
            return false;
        }

        //check if password and confirm password are matched
        if (!inputValidation.isPasswordMatched(edittextPassword, edittextConfirmPassword,
                layoutConfirmPassword, getString(R.string.error_password_match))) {
            edittextConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    /*
     * The method will send user an email verification
     * after checking email and password authentication
     * Users need to verify their email before logging in
     */
    private void emailVerification() {
        // Send verification email
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Verification email sent to " + user.getEmail()
                                    + "\nPlease verify your email before logging in",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Log.e("TAG", "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void handleSuccess() {
        Toast.makeText(activity, "Authetication success. \nNow verify your email",
                Toast.LENGTH_SHORT).show();
        emailVerification();
    }

    @Override
    public void handleFailure(String errorMessage) {
        Toast.makeText(activity, "Authetication failed",
                Toast.LENGTH_SHORT).show();
    }
}

