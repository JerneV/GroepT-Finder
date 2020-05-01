package be.groept.gt_finder.Social;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import be.groept.gt_finder.R;
import be.groept.gt_finder.User.User;

public class swipingGroepswerk extends AppCompatActivity {

    private ArrayList<User> interestedUsers;
    private boolean userPresent = false;
    private arrayAdapter arrayAdapter;
    private int i;
    private String groepswerk;
    private String course;
    private User theUser;
    private String members;
    private StorageReference mStorageRef;
    private RequestQueue requestQueue;
    private SwipeFlingAdapterView flingContainer;
    private User aUser;
    private ArrayList<String[]> usersToCreate;
    private int backPressed;


    //https://studev.groept.be/api/a19sd405/selectAllTeamwork
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swiping_groepswerk);
        Intent intent = getIntent();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://groept-softdev.appspot.com/");
        usersToCreate = new ArrayList<>();
        theUser = intent.getParcelableExtra("theUser");
        Bundle bund = intent.getExtras();
        requestQueue = Volley.newRequestQueue(this);
        assert bund != null;
        groepswerk = bund.getString("groepswerk").toString();
        course = bund.getString("vak").toString();
        //THIS PULLS EVERY USER INTERESTED IN THE ASSIGNMENT, THE USER HASNT SWIPED ON BEFORE FROM DATABASE
        String URL = "https://studev.groept.be/api/a19sd405/selectAllTeamwork/" + groepswerk + "/" + course + "/" + theUser.getUsername() + "/" + groepswerk + "/" + course;
        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 0) {
                    for (int i = 0; i < response.length(); ++i) {
                        JSONObject o = null;
                        try {
                            o = response.getJSONObject(i);
                            String displayName = o.get("displayName").toString();
                            String description = o.get("description").toString();
                            String groepswerk = o.get("groepswerk").toString();
                            String username = o.get("username").toString();
                            String[] blub = {displayName, description, groepswerk, username};
                            if (!username.equals(theUser.getUsername())) {
                                usersToCreate.add(blub);
                            } else {
                                userPresent = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    afterDb(true);
                } else {
                    Toast.makeText(swipingGroepswerk.this, "JSONObject is empty!", Toast.LENGTH_SHORT).show();
                    afterDb(false);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(swipingGroepswerk.this, "Unable to communicate with the server", Toast.LENGTH_LONG).show();

            }
        });
        requestQueue.add(request);

    }


    private void afterDb(boolean b) {
        interestedUsers = new ArrayList<>();
        User instructions = new User("instructionssss", null, null);
        instructions.setDescription("Swipe right for like!");
        instructions.setDisplayName("Swipe left for a dislike!");
        interestedUsers.add(instructions);
        if (!b || userPresent) {
            doPost();
        }
        else{teamworkDoesExist();}
        cardsSetup();
    }


    private void teamworkDoesExist() {
        for (String[] toCreate : usersToCreate) {
            aUser = new User(toCreate[3], null, null);
            System.out.println(toCreate[0]);
            System.out.println(toCreate[3]);
            System.out.println(toCreate[1]);
            aUser.setDescription(toCreate[1]);
            aUser.setDisplayName(toCreate[0]);
            fotosPull(aUser);
            interestedUsers.add(aUser);
        }
    }


    private void fotosPull(final User fotoUser) {
        if (!fotoUser.getUsername().equals("instructionssss")) {
            aUser = fotoUser;
            // Remove 630x630 if we ever stop using the upscaler!
            StorageReference ref = mStorageRef.child(fotoUser.getUsername() + "_630x630");
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    try {
                        URL url = new URL(String.valueOf(uri));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        fotoUser.setUserPicBit(IOUtils.toByteArray(input));
                        // sets the picture on the home screen to the picture we stored in theUser
                        Bitmap myBitmap = BitmapFactory.decodeByteArray(fotoUser.getUserPicBit(), 0, fotoUser.getUserPicBit().length);
                        fotoUser.setUserBitMap(myBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("Something went horribly wrong!");
                }
            });
        }

    }

    private void doPost() {
        //https://studev.groept.be/api/a19sd405/addTeamwork/groepswerk/user/course
        String URL = "https://studev.groept.be/api/a19sd405/addTeamwork/" + groepswerk + "/" + theUser.getUsername() + "/" + course;
        StringRequest requestPost = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("post done");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        requestQueue.add(requestPost);
    }

    private void cardsSetup() {
        arrayAdapter = new arrayAdapter(swipingGroepswerk.this, R.layout.item, interestedUsers);
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frameFling);
        flingContainer.setAdapter(arrayAdapter);
        System.out.println("we get here");
        arrayAdapter.notifyDataSetChanged();
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                interestedUsers.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object swipedUser) {
                //Do something on the left!
                aUser = (User) swipedUser;
                //https://studev.groept.be/api/a19sd405/addToMatches/username/likedby/course/groepswerk/swipedirection
                String URL = "https://studev.groept.be/api/a19sd405/addToMatches/" + aUser.getUsername() + "/" + theUser.getUsername() + "/" + course + "/" + groepswerk + "/" + "L";
                StringRequest requestPost = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("post done");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
                requestQueue.add(requestPost);
            }

            @Override
            public void onRightCardExit(Object swipedUser) {
                aUser = (User) swipedUser;
                //https://studev.groept.be/api/a19sd405/addToMatches/username/likedby/course/groepswerk/swipedirection
                String URL = "https://studev.groept.be/api/a19sd405/addToMatches/" + aUser.getUsername() + "/" + theUser.getUsername() + "/" + course + "/" + groepswerk + "/" + "R";
                StringRequest requestPost = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("post done");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
                requestQueue.add(requestPost);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });
    }

    public void notInterested(View caller) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(swipingGroepswerk.this);
        TextView et = new TextView(this);
        et.setText("are u sure you want to unsubscribe from this group assinment?");
        alertDialogBuilder.setView(et);
        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //https://studev.groept.be/api/a19sd405/deleteUserFromTeamwork/username/groepswerk/vak
                String URL = "https://studev.groept.be/api/a19sd405/deleteUserFromTeamwork/" + theUser.getUsername() + "/" + groepswerk + "/" + course;
                StringRequest requestPost = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("post done");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
                requestQueue.add(requestPost);
                onBtnback(null);
            }

        });
        alertDialogBuilder.setCancelable(false).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }

        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    /**
     * goes back
     */
    public void onBtnback(View caller) {
        Intent mainIntent = new Intent(this, GroepswerkVakX.class);
        mainIntent.putExtra("theUser", (Parcelable) theUser);
        mainIntent.putExtra("vak", course);
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
            Intent goBack = new Intent(this, GroepswerkVakX.class);
            goBack.putExtra("theUser", (Parcelable) theUser);
            goBack.putExtra("vak", course);
            startActivity(goBack);
        }
    }


}