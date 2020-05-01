package be.groept.gt_finder.LoginActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import be.groept.gt_finder.R;

public class registerScreen extends AppCompatActivity {
    private EditText usernameEdit;
    private EditText emailEdit;
    private EditText passwordEdit;
    private EditText passConfirmEdit;
    private ArrayList<String> listEmail;
    private ArrayList<String> listUsername;

    /**
     * onCreate initializes EditText fields& Buttons
     * Gets intent from login aka A list with used usernames and email since no two accounts can share these
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);
        usernameEdit = (EditText) findViewById(R.id.usernameMain);
        emailEdit = (EditText) findViewById(R.id.email);
        passwordEdit = (EditText) findViewById(R.id.password);
        passConfirmEdit = (EditText) findViewById(R.id.confirmPass);
        Intent intent = getIntent();
        listEmail = (ArrayList<String>) intent.getSerializableExtra("mails");
        listUsername = (ArrayList<String>) intent.getSerializableExtra("usernames");

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) { // Accept only letter & digits ; otherwise just return
                        return "";
                    }
                }
                return null;
            }

        };

        usernameEdit.setFilters(new InputFilter[]{filter});
        //emailEdit.setFilters(new InputFilter[] { filter });
        passwordEdit.setFilters(new InputFilter[]{filter});
        passConfirmEdit.setFilters(new InputFilter[]{filter});
    }

    /**
     * @param caller ?
     *               goes back to login
     */
    public void onBtnbackToLogin_Clicked(View caller) {
        //
        Intent registerIntent = new Intent(this, LoginActivity.class);
        //registerIntent.putExtra("users", (Parcelable) users);
        startActivity(registerIntent);
    }


    /**
     * Happens when confirm is clicked
     * firstly gets contends from edittext fields
     * then checks on empty fields since everything has to be filled out
     * then checks if username already exists if yes: clears field and asks for other username
     * same for email
     * then checks if 2 passwords match
     * <p>
     * if all conditions pass, we make a post request by first constructing the url and constructing requestqueue and json post request
     * after post return to login screen
     */
    public void onBtnConfirm_Clicked(View caller) throws NoSuchAlgorithmException {
        //https://studev.groept.be/api/a19sd405/addToUsers/name/mail/password
        String usernameString = usernameEdit.getText().toString().toLowerCase().trim();
        String emailString = emailEdit.getText().toString().trim();
        String passwordString = passwordEdit.getText().toString().trim();
        String passwordConfirmString = passConfirmEdit.getText().toString().trim();
        if (!TextUtils.isEmpty(passwordEdit.getText())
                && !TextUtils.isEmpty(passConfirmEdit.getText())
                && !TextUtils.isEmpty(usernameEdit.getText())
                && !TextUtils.isEmpty(emailEdit.getText())) {
            if (isValidEmail(emailEdit.getText().toString())) {

                if (!listUsername.contains(usernameString)) {
                    if (!listEmail.contains(emailString)) {
                        if (passwordConfirmString.equals(passwordString)) {
                            String hashedPass = computeHashFromPassword(usernameString, passwordString);
                            String ADD_USER_BASE_URL = "https://studev.groept.be/api/a19sd405/addToUsers/";
                            String ADD_USER_URL = ADD_USER_BASE_URL + usernameString + "/" + emailString + "/" + URLEncoder.encode(hashedPass);
                            ADD_USER_URL = ADD_USER_URL.replace(" ", "_");
                            System.out.println(ADD_USER_URL);
                            RequestQueue requestQueue = Volley.newRequestQueue(this);
                            StringRequest requestPost = new StringRequest(Request.Method.POST, ADD_USER_URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            });
                            requestQueue.add(requestPost);
                            Toast.makeText(getApplicationContext(), "You can log in now!", Toast.LENGTH_LONG).show();
                            Intent registerIntent = new Intent(this, LoginActivity.class);
                            startActivity(registerIntent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Passwords don't match!", Toast.LENGTH_LONG).show();
                            passConfirmEdit.getText().clear();
                            passwordEdit.getText().clear();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "E-mail already in use!", Toast.LENGTH_LONG).show();
                        passConfirmEdit.getText().clear();
                        passwordEdit.getText().clear();
                        emailEdit.getText().clear();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Username already in use!", Toast.LENGTH_LONG).show();
                    passConfirmEdit.getText().clear();
                    passwordEdit.getText().clear();
                    usernameEdit.getText().clear();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please enter a valid mail!", Toast.LENGTH_LONG).show();
                emailEdit.getText().clear();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please fill out everything!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checks if the target Mail is a valid mail
     *
     * @param target STRING the target mail
     * @return returns TRUE if valid
     */
    public static boolean isValidEmail(String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * same hash method as login
     */

    public String computeHashFromPassword(String user, String password) throws NoSuchAlgorithmException {
        String combo = user + password;
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(combo.getBytes());
        String hashString = new String(messageDigest.digest());
        return password;
    }


}


