package be.groept.gt_finder.LoginActivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import be.groept.gt_finder.Main.MainActivity;
import be.groept.gt_finder.R;
import be.groept.gt_finder.User.User;

public class LoginActivity extends AppCompatActivity {

    private EditText loginId;
    private EditText loginPswd;

    /**
     * Variables used for the LOGIN of users. URL is the DB URL. The map is used to keep track of users.
     */
    private static final String LOGIN_INFO_URL = "https://studev.groept.be/api/a19sd405/getLoginInfo";
    private Map<String, String> users = new HashMap<>();

    /**
     * These arraylists are used to pass trough to the next activity Register. There they can be used to check if a username/mail already is used.
     * This way we only perform this DB request once.
     */
    private ArrayList<String> usernames = new ArrayList<>();
    private ArrayList<String> mails = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        //Get the users from DB
        RequestQueue requestQueue = Volley.newRequestQueue(this);

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

        /**
         * This chunk of code performs a Database request to the RESTAPI. Using the LOGIN_INFO_URL we get all info of the users (name, pswd and mail)
         * We then parse these JSON objects to get the name and pswds of all users.
         * This info is then put in a HashMap (since only unique pairs can exist)
         * Later we can then check if a user already exists and can login given the right password (hashed by algorithm)
         */
        final JsonArrayRequest loginRequest = new JsonArrayRequest(Request.Method.GET, LOGIN_INFO_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 0) {
                    for (int i = 0; i < response.length(); ++i) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(i);
                            String username = o.get("username").toString();
                            String email = o.get("email").toString();
                            String password = o.get("password").toString();
                            users.put(username, URLDecoder.decode(password));
                            usernames.add(username);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "JSONObject is empty!", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "Unable to communicate with the server", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(loginRequest);
        // Defining
        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        Button registerBtn = (Button) findViewById(R.id.registerBtn);
        loginId = (EditText) findViewById(R.id.loginId);
        loginPswd = (EditText) findViewById(R.id.loginPswd);

        loginId.setFilters(new InputFilter[]{filter});
        loginPswd.setFilters(new InputFilter[]{filter});

    }

    /**
     * Function performed when the LOGIN button is clicked. username and password can not be empty. User needs to exist and password correct!
     *
     * @param caller ?
     * @throws NoSuchAlgorithmException When SHA-256 not found!
     */
    public void onBtnLogin_Clicked(View caller) throws NoSuchAlgorithmException {
        String username = loginId.getText().toString().toLowerCase();
        String password = computeHashFromPassword(username, loginPswd.getText().toString());

        if (!TextUtils.isEmpty(loginId.getText()) && !TextUtils.isEmpty(loginPswd.getText())) {
            if (users.containsKey(username)) {
                if (Objects.equals(users.get(username), password)) {
                    Toast.makeText(getApplicationContext(), "Password correct! Logging you in.", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.putExtra("theUser", (Parcelable) new User(username, password, null));
                    startActivity(mainIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong password! Try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "This username is not known. Please register first!", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enter your info!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Starts the register activity.
     *
     * @param caller ?
     */
    public void onBtnRegister_Clicked(View caller) {
        Intent registerIntent = new Intent(this, registerScreen.class);
        registerIntent.putExtra("usernames", usernames);
        registerIntent.putExtra("mails", mails);
        startActivity(registerIntent);

    }


    /**
     * Calculates the hash from the username and password entered by the user. This hash is then stored in the database to increase safety.
     *
     * @param user     STRING is the username. Always in lower case!
     * @param password STRING is the password in non-hash form. Lower case.
     * @return STRING returns the hash formed by the algorithm.
     * @throws NoSuchAlgorithmException When SHA-256 is not detected.
     */
    public String computeHashFromPassword(String user, String password) throws NoSuchAlgorithmException {
        String combo = user + password;
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(combo.getBytes());
        String hashString = new String(messageDigest.digest());
        return password;


    }


}


