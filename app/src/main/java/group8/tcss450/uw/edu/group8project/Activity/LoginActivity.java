package group8.tcss450.uw.edu.group8project.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import group8.tcss450.uw.edu.group8project.GetWebServiceTask;
import group8.tcss450.uw.edu.group8project.GetWebServiceTaskDelegate;
import group8.tcss450.uw.edu.group8project.R;

/**
 * LoginActivity class display log in layout
 * where user input there email and password
 * System will check if the email and password is registered
 * and email is veried yet.
 * It failed -> staying on login layout
 *    passed -> go to DisplayActivty
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GetWebServiceTaskDelegate {

    private final AppCompatActivity activity = LoginActivity.this;

    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputEditText edittextEmail;
    private TextInputEditText edittextPassword;

    private AppCompatButton logIN;
    private AppCompatTextView signUP;
    private AppCompatButton resendVerification;
    private CheckBox rememberMeCheckBox;

    private InputValidation inputValidation;
    private String email;
    private String password;
    private SharedPreferences settings;
    private FirebaseAuth mAuth;
    private String serviceName = null;


    /*
     * Initialize activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        //initialize views
        layoutEmail = (TextInputLayout) findViewById(R.id.layoutEmail);
        layoutPassword = (TextInputLayout) findViewById(R.id.layoutPassword);

        edittextEmail = (TextInputEditText) findViewById(R.id.edittextEmail);
        edittextPassword = (TextInputEditText) findViewById(R.id.edittextPassword);

        logIN = (AppCompatButton) findViewById(R.id.logIN);
        resendVerification = (AppCompatButton) findViewById(R.id.resend);
        signUP = (AppCompatTextView) findViewById(R.id.signUP);
        rememberMeCheckBox = (CheckBox) findViewById(R.id.rememberMe);
        mAuth = FirebaseAuth.getInstance();


        //initialize listeners
        logIN.setOnClickListener(this);
        signUP.setOnClickListener(this);
        resendVerification.setOnClickListener(this);


        //initialize objects
        inputValidation = new InputValidation(activity);
        settings = getSharedPreferences("FoodRecipes", MODE_PRIVATE);
        String currentUserEmail = settings.getString("email", null);
        if (currentUserEmail != null) {
            edittextEmail.setText(currentUserEmail);
        }
    }

    /*
     * Launch activity if different button is pressed
     * If click signup -> launch RegisterActivity
     * If click log in -> check authentication and email verification
     */
    @Override
    public void onClick(View v){
        if(serviceName != null) {
            return;
        }
        switch (v.getId()){
            case R.id.logIN:
                if (!areInputsValid(true)) {
                    break;
                } else {
                    serviceName = "login";
                    email = edittextEmail.getText().toString().trim();
                    password = edittextPassword.getText().toString();
                    GetWebServiceTask task = new GetWebServiceTask();
                    task.execute("http://cssgate.insttech.washington.edu/~davidmk/login.php", email, password);
                    task.delegate = this;
                }
                break;

            case R.id.signUP:
                Intent intentRegister = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intentRegister);
                break;

            case R.id.resend:
                if (!areInputsValid(false)) {
                    break;
                } else {
                    serviceName = "resendverification";
                    email = edittextEmail.getText().toString().trim();
                    GetWebServiceTask task = new GetWebServiceTask();
                    task.execute("http://cssgate.insttech.washington.edu/~davidmk/resendverification.php", email, "");
                    task.delegate = this;
                }
        }
    }

    /*
     * This method checks if the validation of input
     */
    private boolean areInputsValid(boolean shouldCheckPassword){
        boolean mybool = true;


        //check if password field is filled.
        if (shouldCheckPassword && !inputValidation.isTextEditFilled(edittextPassword, layoutPassword, getString(R.string.error_empty_password))) {
            edittextPassword.requestFocus();
            mybool = false;
        }

        //check if email field is filled.
        if (!inputValidation.isTextEditFilled(edittextEmail, layoutEmail, getString(R.string.error_empty_email))) {
            edittextEmail.requestFocus();
            mybool = false;
        }

        //check if email is in valid format (example@hehe.haha)
        if (!inputValidation.isEmailValid(edittextEmail, layoutEmail, getString(R.string.error_message_email))) {
            edittextEmail.requestFocus();
            mybool = false;
        }


        return mybool;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void handleSuccess() {
        if (serviceName.equals("login")) {


            email = edittextEmail.getText().toString().trim();
            password = edittextPassword.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                        }
                    });

            if (rememberMeCheckBox.isChecked()) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("email", edittextEmail.getText().toString().trim());
                editor.putBoolean("isLoggedIn", true);
                // Commit the edits!
                editor.commit();
            }
            Intent accountsIntent = new Intent(activity, DisplayActivity.class);
            accountsIntent.putExtra("EMAIL", edittextEmail.getText().toString().trim());
            startActivity(accountsIntent);
        } else {
            Toast.makeText(activity, "Verification email sent.",
                    Toast.LENGTH_SHORT).show();
        }
        serviceName = null;
    }

    @Override
    public void handleFailure(int status) {
        if (serviceName.equals("login")) {
            if (status == 404) {
                Toast.makeText(activity, "Sign In failed, please try again.",
                        Toast.LENGTH_SHORT).show();
            } else if (status == 401) {
                Toast.makeText(activity, "This email has not been verified yet.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Unknown error occurred, please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            if (status == 404) {
                Toast.makeText(activity, "Email not found",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Unknown Error",
                        Toast.LENGTH_SHORT).show();
            }
        }
        serviceName = null;

    }
}