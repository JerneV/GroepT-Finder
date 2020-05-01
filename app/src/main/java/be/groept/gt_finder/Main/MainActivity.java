package be.groept.gt_finder.Main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import be.groept.gt_finder.LoginActivities.LoginActivity;
import be.groept.gt_finder.R;
import be.groept.gt_finder.Social.BuddyFinder;
import be.groept.gt_finder.Social.Teamwork;
import be.groept.gt_finder.Social.matchesActivity;
import be.groept.gt_finder.User.User;


public class MainActivity extends AppCompatActivity {

    private static final String url = "https://studev.groept.be/api/a19sd405/getSpecificUser/";

    /**
     * Following variables used for customized greeting.
     */
    Calendar rightNow = Calendar.getInstance();
    StringBuilder builder = new StringBuilder();
    private User theUser;
    private TextView welcomeMain;
    private TextView usernameMain;
    private TextView passwordMain;
    private TextView descriptionMain;
    private TextView miscMain;
    private Button settings;
    private Button goToTeamwork;
    private Button goToBuddy;
    private ImageButton profilePic;
    private Bitmap bitmap;
    private Uri path;
    private int backPressed = 0;
    private String username;
    private RequestQueue requestQueue;
    private int currentTime = rightNow.get(Calendar.HOUR_OF_DAY);
    private String longStringCouses;
    private ProgressBar uploadBar;
    private StorageReference mStorageRef;

    public MainActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://groept-softdev.appspot.com/");


        welcomeMain = (TextView) findViewById(R.id.welcomeMain);
        uploadBar = findViewById(R.id.progressBar);

        usernameMain = (TextView) findViewById(R.id.usernameMain);
        passwordMain = (TextView) findViewById(R.id.passwordMain);
        descriptionMain = (TextView) findViewById(R.id.descriptionMain);
        miscMain = (TextView) findViewById(R.id.miscMain);

        settings = (Button) findViewById(R.id.settings);
        goToTeamwork = (Button) findViewById(R.id.goToTeam);
        goToBuddy = (Button) findViewById(R.id.goToBuddy);

        profilePic = (ImageButton) findViewById(R.id.profilePic);


        Intent intent = getIntent();
        User loginUser = (User) intent.getParcelableExtra("theUser");
        assert loginUser != null; // should never return null
        username = loginUser.getUsername();

        requestQueue = Volley.newRequestQueue(this);

        final JsonArrayRequest queueRequest = new JsonArrayRequest(Request.Method.GET, url + username, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); ++i) {
                    JSONObject o = null;
                    try {
                        o = response.getJSONObject(i);
                        theUser = new User(username, o.getString("email"), o.getString("password"));
                        System.out.println(theUser);
                        theUser.setDescription(o.get("description").toString().replace("_", " "));
                        theUser.setDisplayName(o.get("displayName").toString());
                        theUser.setPhase(o.getInt("phase"));
                        theUser.setSpecial(o.get("special").toString());
                        longStringCouses = o.get("courses").toString().trim();
                        if (!longStringCouses.equals("null")) {
                            longStringCouses = longStringCouses.substring(0, longStringCouses.length() - 1);
                        } else {
                            longStringCouses = "";
                        }
                        System.out.println(longStringCouses);
                        theUser.setCourses(longStringCouses);
                        doAfterDB();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Unable to communicate with the server", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(queueRequest);


    }

    /**
     * Since the DB pull and push are ASYNC operations we only do these things after the DB PULLs are done
     * @throws IOException !!
     */
    @SuppressLint("SetTextI18n")
    public void doAfterDB() throws IOException {
        Log.d("User", theUser.toString());

        if(theUser.getDescription().equals("null") || theUser.getDisplayName().equals("null") || theUser.getPhase() == 0){
            goToBuddy.setEnabled(false);
            goToTeamwork.setEnabled(false);
            goToBuddy.setEnabled(false);
        }

        if (currentTime >= 0 && currentTime < 12) {
            builder.append("Good morning");
        } else if (currentTime >= 12 && currentTime < 16) {
            builder.append("Good afternoon");
        } else if (currentTime >= 16 && currentTime < 21) {
            builder.append("Good evening");
        } else if (currentTime >= 21 && currentTime < 24) {
            builder.append("Good night");
        }
        builder.append(" ");
        if (!theUser.getDisplayName().equals("null")) {
            builder.append(theUser.getDisplayName().replace("_", " "));
        } else {
            builder.append(theUser.getUsername());
        }


        System.out.println(theUser.getCourses());

        welcomeMain.setText(builder.toString());
        usernameMain.setText("Your username is: " + theUser.getUsername());
        passwordMain.setText("You password is: " + theUser.getPassword());


        if (!theUser.getDescription().equals("null")) {
            descriptionMain.setText("Your description is: " + "\n" + theUser.getDescription().replace("_", " "));
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Your mail is: ");
        builder.append(theUser.getEmail()).append("\n");
        if (theUser.getPhase() != 0) {
            builder.append("Your phase is: ").append(theUser.getPhase()).append("\n");
        }
        if(!theUser.getSpecial().equals("null")){
            builder.append("Your special is: ").append(theUser.getSpecial());
        }



        miscMain.setText(builder.toString());
        getPicture(theUser.getUsername());


    }

    /**
     * Button handler to go to the settings page. Will probably not stay.
     */
    public void onBtnSettings_Clicked(View caller) {
        Intent settingsIntent = new Intent(this, settingsActivity.class);
        settingsIntent.putExtra("theUser", (Parcelable) theUser);
        startActivity(settingsIntent);
    }

    public void onBtnBuddy_Clicked(View caller) {
        Intent buddyIntent = new Intent(this, BuddyFinder.class);
        buddyIntent.putExtra("theUser", (Parcelable) theUser);
        startActivity(buddyIntent);
    }

    public void onBtnTeam_Clicked(View caller) {
        Intent teamIntent = new Intent(this, Teamwork.class);
        teamIntent.putExtra("theUser", (Parcelable) theUser);
        startActivity(teamIntent);
    }

    public void onBtnMatches_Clicked(View caller){
        Intent matchesIntent = new Intent(this, matchesActivity.class);
        matchesIntent.putExtra("theUser", (Parcelable) theUser);
        startActivity(matchesIntent);
    }

    public void onBtnImage_Clicked(View caller) {
        CropImage.startPickImageActivity(this);
    }

    /**
     * This method will catch results from previous intents. So we get the Uri from the crop activity
     * After the image has been cropped we generate a bitmap for the profilepicture (all still offline)
     * Then we start the upload process of the cropped image via URI
     * @param requestCode the requestcode passed on via the intent
     * @param resultCode the resultcode we expect
     * @param data the data that has been returned, in this case
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                path = imageUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                path = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                    profilePic.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // This gets executed after picture is chosen
                        postImage(path);
                        uploadBar.setVisibility(View.VISIBLE);
                    }
                }, 1000);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Starts the cropping activity with the provide imageURI
     * @param imageUri The URI of the image we want to crop
     */
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(false)
                .setActivityTitle("Crop Image")
                .setInitialCropWindowPaddingRatio(0)
                .setFixAspectRatio(true)
                .setMinCropResultSize(450, 450)
                //.setMaxCropResultSize(630, 630)
                .setAspectRatio(1, 1)
                .setOutputCompressQuality(100)
                //.setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    /**
     * Posts the image to the firebase DB
     * @param path the URI of the cropped image
     */
    public void postImage(Uri path) {
        final StorageReference ref = mStorageRef.child(theUser.getUsername());
        System.out.println("Trying to upload image! " + path.toString());
        ref.putFile(path)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        System.out.println(ref.getDownloadUrl());
                        uploadBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    /**
     * Gets the picture from the Firebase DB provided with a username
     * This corresponds with username.jpeg on the DB
     * THIS IS A SLIGHTLY MODIFIED VERSION! WE IMMEDIATELY SET THE IMAGEVIEW TO THE CORRECT IMAGE
     * @param username STRING of the username for whom we want the picture in BITMAP
     */
    public void getPicture(String username) {
        // Remove 630x630 if we ever stop using the upscaler!
        StorageReference ref = mStorageRef.child(username + "_630x630");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    URL url = new URL(String.valueOf(uri));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    // Here we set the userPic as a byte[]
                    theUser.setUserPicBit(IOUtils.toByteArray(input));
                    // sets the picture on the home screen to the picture we stored in theUser
                    Bitmap myBitmap = BitmapFactory.decodeByteArray(theUser.getUserPicBit(), 0, theUser.getUserPicBit().length);
                    // or you can set a global Bitmap variable to myBitmap
                    profilePic.setImageBitmap(myBitmap);
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
            Intent goToLogin = new Intent(this, LoginActivity.class);
            startActivity(goToLogin);
        }
    }

}
