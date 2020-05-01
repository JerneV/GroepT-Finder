package be.groept.gt_finder.Social;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import be.groept.gt_finder.Main.MainActivity;
import be.groept.gt_finder.R;
import be.groept.gt_finder.User.User;

public class BuddyFinder extends AppCompatActivity {

    private StorageReference mStorageRef;
    private RequestQueue requestQueue;
    private User theUser;
    private User aUser;
    private arrayAdapter arrayAdapter;
    final String dbUrl = "https://studev.groept.be/api/a19sd405/getAllUsersForMatches/";
    private SwipeFlingAdapterView flingContainer;
    private ArrayList<String[]> usersToCreate = new ArrayList<>();
    private ArrayList<User> interestedUsers = new ArrayList<>();
    private boolean userPresent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_finder);

        Intent intent = getIntent();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://groept-softdev.appspot.com/");
        theUser = intent.getParcelableExtra("theUser");
        assert theUser != null;
        String username = theUser.getUsername();

        requestQueue = Volley.newRequestQueue(this);

        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, dbUrl + username, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 0) {
                    for (int i = 0; i < response.length(); ++i) {
                        JSONObject o = null;
                        //System.out.println("In de for loop " + i);
                        try {
                            o = response.getJSONObject(i);
                            String displayName = o.get("displayName").toString();
                            String description = o.get("description").toString();
                            String username = o.get("username").toString();
                            String[] blub = {displayName, description, username};
                            if (!username.equals(theUser.getUsername())) {
                                usersToCreate.add(blub);
                            }
                            else{userPresent=true;}
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    doAfterDb();
                } else {
                    Toast.makeText(BuddyFinder.this, "JSONObject is empty!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(BuddyFinder.this, "Unable to communicate with the server", Toast.LENGTH_LONG).show();

            }
        });
        requestQueue.add(request);

    }

    private void doAfterDb() {
        User instructions = new User("instructionssss", null, null);
        instructions.setDescription("swipe right for like");
        instructions.setDisplayName("swipe left for no like");
        interestedUsers.add(instructions);
        for (String[] toCreate : usersToCreate) {
            aUser = new User(toCreate[2], null, null);
            aUser.setDescription(toCreate[1]);
            aUser.setDisplayName(toCreate[0]);
            fotosPull(aUser);
            interestedUsers.add(aUser);
        }

        cardsSetup(true);
    }


    private void fotosPull(final User fotoUser) {
        if(!fotoUser.getUsername().equals("instructions")){
            aUser = fotoUser;
            // Remove 630x630 if we ever stop using the upscaler!
            StorageReference ref = mStorageRef.child(fotoUser.getUsername() + "_630x630");
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri ) {
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
            });}

    }

    private void cardsSetup(final boolean userPresent) {
        if (!userPresent) {
            User arrayEmpty = new User("test", null, null);
            arrayEmpty.setDescription("No other users interested");
            arrayEmpty.setDisplayName(":'(");
            interestedUsers.add(arrayEmpty);
        }
        arrayAdapter = new arrayAdapter(BuddyFinder.this, R.layout.itembuddy, interestedUsers);
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.buddyFling);
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
                String URL = "https://studev.groept.be/api/a19sd405/addToMatches/" + aUser.getUsername() + "/" + theUser.getUsername() + "/" + "buddy"+ "/" + "null"+ "/" + "L";
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
                String URL = "https://studev.groept.be/api/a19sd405/addToMatches/" + aUser.getUsername() + "/" + theUser.getUsername() + "/" + "buddy"+ "/" + "null"+ "/" + "R";
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


    public void onBtnClick(View caller){
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("theUser", theUser);
        startActivity(mainIntent);
    }
}
