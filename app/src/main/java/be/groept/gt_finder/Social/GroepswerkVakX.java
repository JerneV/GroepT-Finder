package be.groept.gt_finder.Social;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import be.groept.gt_finder.R;
import be.groept.gt_finder.User.User;

public class GroepswerkVakX extends AppCompatActivity {
    private User theUser;
    private String course;
    private String longGroepswerkString;
    private ArrayList<String> groepswerken = new ArrayList<>();
    private Button btn;
    private LinearLayout layout;
    private LinearLayout.LayoutParams layoutParams;

    /**
     * sets up looks of screen first
     * adds button which can add an group assignment for when a certain group assignment isnt in the list already
     * gives this button a listener that executes add popup() method when clicked
     * recieves intent from last activity and dispays course in textview
     * does database pull to recieve all group assignments for said course
     * continues in aftherDb()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //https://studev.groept.be/api/a19sd405/getGroepswerkenVak/course
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groepswerk_vak_x);
        layout = (LinearLayout) findViewById(R.id.linearLayoutGroepsWerk);
        Display screensize = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screensize.getSize(size);
        int width = size.x;
        layoutParams = new LinearLayout.LayoutParams(width - 100, 100);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        layoutParams.setMargins(0, 5, 0, 5);
        btn = new Button(GroepswerkVakX.this);
        btn.setWidth(40);
        btn.setTextColor(Color.parseColor("#B2FFFFFF"));
        btn.setHeight(20);
        btn.setText("add a group work");
        layout.addView(btn, layoutParams);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                popUp();
            }
        });
        //SETTING UP RECEIVING USER INFO
        Intent intent = getIntent();
        theUser = intent.getParcelableExtra("theUser");
        Bundle bund = intent.getExtras();
        course = bund.getString("vak").toString();
        System.out.println(course);
        TextView courze = (TextView) findViewById(R.id.courseName);
        courze.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30.f);
        courze.setTextColor(Color.parseColor("#B2FFFFFF"));
        courze.setText(course);
        String URL = "https://studev.groept.be/api/a19sd405/getGroepswerkenVak/" + course;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JsonArrayRequest loginRequest = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 0) {
                    for (int i = 0; i < response.length(); ++i) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(i);
                            longGroepswerkString = o.get("groepswerken").toString();
                            if (longGroepswerkString.equals("null")) {
                                longGroepswerkString = "labo";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    afterDB();
                } else {
                    Toast.makeText(GroepswerkVakX.this, "JSONObject is empty!", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(GroepswerkVakX.this, "Unable to communicate with the server", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(loginRequest);

    }

    /**
     * creates button for each received group assignment
     */
    private void afterDB() {
        String[] slitted = longGroepswerkString.split("_");
        groepswerken.addAll(Arrays.asList(slitted));
        System.out.println(groepswerken);
        for (int i = 0; i < groepswerken.size(); i++) {
            btn = new Button(GroepswerkVakX.this);
            btn.setWidth(40);
            btn.setBackgroundColor(Color.parseColor("#006064"));
            btn.setTextColor(Color.parseColor("#B2FFFFFF"));
            btn.setHeight(20);
            btn.setText(groepswerken.get(i));
            layout.addView(btn, layoutParams);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    toTinder(v);
                }
            });
        }


    }

    /**
     * creates popup w textview where user can add extra group assignment
     * checks if it already excists
     * pushes it to the db of said course
     */
    private void popUp() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GroepswerkVakX.this);
        final EditText et = new EditText(GroepswerkVakX.this);
        et.setHint("new group work");
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(et);
        // set dialog message
        alertDialogBuilder.setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String viewText = et.getText().toString().replace(" ", "-");
                if (!groepswerken.contains(viewText)) {
                    btn = new Button(GroepswerkVakX.this);
                    btn.setHeight(20);
                    btn.setWidth(40);
                    btn.setBackgroundColor(Color.parseColor("#006064"));
                    btn.setTextColor(Color.parseColor("#B2FFFFFF"));
                    btn.setText(viewText);
                    layout.addView(btn, layoutParams);
                    btn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            toTinder(v);
                        }
                    });
                    groepswerken.add(viewText);
                    RequestQueue requestQueue = Volley.newRequestQueue(GroepswerkVakX.this);
                    String blub = "";
                    try {
                        blub = longGroepswerkString.concat("_").concat(viewText);
                    } catch (NullPointerException fix) {
                        blub = viewText;
                    }
                    String URL = "https://studev.groept.be/api/a19sd405/setGroepswerkenVak/" + blub + "/" + course;
                    System.out.println(URL);
                    StringRequest requestPost = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
                    requestQueue.add(requestPost);
                    Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "group assignment already exists", Toast.LENGTH_LONG).show();
                }
            }

        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    /**
     * goes to next activity
     * passes user and name of group assignment
     */
    private void toTinder(View v) {
        Button b = (Button) v;
        Intent teamIntent = new Intent(this, swipingGroepswerk.class);
        teamIntent.putExtra("theUser", (Parcelable) theUser);
        teamIntent.putExtra("groepswerk", b.getText().toString());
        teamIntent.putExtra("vak", course);
        startActivity(teamIntent);
    }

    /**
     * This method handles backpresses. This way it's harder to accidentally press back when you're already logged in.
     */

    @Override
    public void onBackPressed() {
        Intent goToMain = new Intent(this, Teamwork.class);
        goToMain.putExtra("theUser", (Parcelable) theUser);
        startActivity(goToMain);

    }


}
