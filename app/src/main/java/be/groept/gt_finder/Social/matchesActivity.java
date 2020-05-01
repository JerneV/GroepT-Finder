package be.groept.gt_finder.Social;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.groept.gt_finder.Main.MainActivity;
import be.groept.gt_finder.R;
import be.groept.gt_finder.User.User;

public class matchesActivity extends AppCompatActivity {

    private User theUser;
    private String username;
    private RequestQueue requestQueue;
    private LinearLayout linearLayout;
    private int backPressed;
    // Big boy contains all matches in arraylists!
    private List<List<String>> masterMatches = new ArrayList<>();
    // Small
    private List<String> matches;
    private TextView matchesText;
    private final String dbUrl = "https://studev.groept.be/api/a19sd405/getAllMatchesForUser/";
    private LinearLayout.LayoutParams layoutParams;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        linearLayout = findViewById(R.id.linLayoutMatches);
        Display screensize = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screensize.getSize(size);
        int width = size.x;
        layoutParams = new LinearLayout.LayoutParams(width- 100,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL;
        linearLayout.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        layoutParams.setMargins(0, 5, 0, 5);
        Intent intent = getIntent();
        theUser = (User) intent.getParcelableExtra("theUser");
        assert theUser != null; // should never return null
        username = theUser.getUsername();

        requestQueue = Volley.newRequestQueue(this);

        //https://studev.groept.be/api/a19sd405/getAllMatchesForUser/username
        final JsonArrayRequest matchesRequest = new JsonArrayRequest(Request.Method.GET, dbUrl + username, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 0) {
                    for (int i = 0; i < response.length(); ++i) {
                        System.out.println("In for loop!");
                        JSONObject o = null;
                        matches = new ArrayList<>();
                        try {
                            o = response.getJSONObject(i);
                            if (o.getString("swipeDirection").equals("R")) {
                                String likedBy = o.getString("likedBy");
                                matches.add(likedBy);
                                String course = o.getString("course");
                                matches.add(course);
                                String groepswerk = o.getString("groepswerk");
                                matches.add(groepswerk);
                                String likedOn = o.getString("likedOn");
                                matches.add(likedOn);
                                String mail = o.getString("email");
                                matches.add(mail);
                                masterMatches.add(matches);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        doAfterDB();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("No matches for user :" + username);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(matchesActivity.this, "Unable to communicate with the server", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(matchesRequest);

    }

    private void doAfterDB() throws ParseException {
        //unpack all matches from mastermatches and put them on the screen

        String stringToDatePattern = "yyyy-MM-dd hh:mm:ss";
        String dateToStringPattern = "dd-MM-yyyy";
        SimpleDateFormat stringToDateFormat = new SimpleDateFormat(stringToDatePattern);
        SimpleDateFormat dateToStringFormat = new SimpleDateFormat(dateToStringPattern);


        for (List<String> match : masterMatches) {
            StringBuilder builder = new StringBuilder();
            final String user = match.get(0);
             String course = match.get(1);
            assert match.get(2) == null;
            String groepswerk = match.get(2);
            final String likedOn = match.get(3);
            final String email = match.get(4);
            builder.append("User ");
            builder.append(user);
            builder.append(" liked you.");
            builder.append("\n");
            builder.append("In ");
            builder.append(course);
            builder.append(" ");
            if(!groepswerk.equals("null")){
                builder.append(groepswerk);
            }
            builder.append("\n");
            builder.append("Liked on: ");
            //TODO format this date
            Date date = stringToDateFormat.parse(likedOn);
            builder.append(dateToStringFormat.format(date));
            builder.append("\n");
            builder.append(email);
            builder.append("\n");
            builder.append("\n");
            Button blub = new Button(this);
            blub.setText(builder.toString());
            blub.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            blub.setHeight(250);
            blub.setBackgroundColor(Color.parseColor("#006064"));
            blub.setTextColor(Color.parseColor("#B2FFFFFF"));
            blub.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sendEmail(v,email, user, likedOn);
                }
            });
            linearLayout.addView(blub, layoutParams);
        }

    }

    @SuppressLint("IntentReset")
    private void sendEmail(View v,String email, String likedBy, String likedOn) {
        Button caller = (Button) v;
        Log.d("Mail", "Send email");
        String[] TO = {email};
        StringBuilder builder = new StringBuilder();
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "You liked me!");
        builder.append("Hi ");
        builder.append(likedBy);
        builder .append(", you liked me on GroepT Finder. And I want to see where this goes ;)\n");
        builder.append("Liked on: ");
        builder.append(likedOn);
        emailIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.d("Mail", "Finished sending email...");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(matchesActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * goes back to main menu
     */
    public void onBtnback_Clicked(View caller) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("theUser", (Parcelable) theUser);
        startActivity(mainIntent);
    }


    /**
     * This method handles backpresses. This way it's harder to accidentally press back when you're already logged in.
     */
    @Override
    public void onBackPressed() {
        if (backPressed != 1) {
            Toast.makeText(getApplicationContext(), "Press back once more to go back!", Toast.LENGTH_SHORT).show();
            backPressed += 1;
        } else {
            backPressed = 0;
            Intent goToMain = new Intent(this, MainActivity.class);
            goToMain.putExtra("theUser", (Parcelable) theUser);
            startActivity(goToMain);
        }
    }


}
