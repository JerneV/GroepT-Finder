package be.groept.gt_finder.Main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;

import be.groept.gt_finder.R;
import be.groept.gt_finder.User.User;

public class settingsActivity extends AppCompatActivity {
    private EditText dispName;
    private EditText description;
    private Spinner phase;
    private Spinner special;
    private String usernameS;
    protected Button selectCoursesButton;
    //this charSequence contains all extra courses one can take for groupswork for every year
    protected CharSequence[] courses1 = {"EE2", "Philo", "Dynamics"};
    protected CharSequence[] courses2Algemeen = {"Electromagnetism", "Strength-of-materials", "Thermodynamica", "EE3", "STE", "IC", "ComII"};
    protected CharSequence[] courses2EM = {"EE4-EM", "Elektrotechniek", "Dynamica-Starre-Lichamen"};
    protected CharSequence[] courses2EA = {"ElOn", "SoftDev", "EE4-EA"};
    protected CharSequence[] courses2CHEM = {"EE4-chemie", "EE4-BIOCHEM", "Biochemie", "Industriele-chemie"};
    protected CharSequence[] courses3Algemeen = {"Religions", "Management", "CommunicationIII"};
    protected CharSequence[] courses3EM = {"EE5-EM", "MachineParts", "Material-Selection", "Mechanical-design", "Electric-installations"};
    protected CharSequence[] courses3EA = {"ComputerNetworks", "Controlsystems", "Datacommunication-WLAN", "DataScience", "Digitale-Signaalverwerking", "Digitale-Signaalverwerking", "Elektrische-motoren"};
    protected CharSequence[] courses3CHEM = {"Chemical-engineering", "New-materials", "Fysico-chemie", "EE5-CHEM"};
    protected CharSequence[] courses3CHEMBIO = {"FoodTechnology", "Microbiologie", "Moleculaire-Celbiologie", "EE5"};
    protected CharSequence[] master = {"helpikkengeenmastervakken", "moeilijkvak1", "mamagement17", "EE15"};
    protected ArrayList<CharSequence> extraCourses = new ArrayList<CharSequence>();
    private User theUser;
    private ArrayList<CharSequence> userHasCourses = new ArrayList<>();
    private boolean changedPhaseOrSpecial = false;
    private ArrayList<CharSequence> oldCourses = new ArrayList<>();
    private int backPressed;


    /**
     * onCreate initializes EditText fields& Buttons
     * Gets intent from login aka A list with all user data
     * sets listeners to phase and special spinner  used so special gets disabled for first years
     * resets text (&selected extra courses by user) on big courses spinner when phase or special changes
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SETUP BUTTONS
        setContentView(R.layout.activity_settings);
        dispName = (EditText) findViewById(R.id.dispNameText);
        description = (EditText) findViewById(R.id.description);
        phase = (Spinner) findViewById(R.id.spinnerFase);
        selectCoursesButton = (Button) findViewById(R.id.selectCourses);
        special = (Spinner) findViewById(R.id.spinnerSpecial);
        ImageButton buttonBackHome = (ImageButton) findViewById(R.id.backButton);
        Button changeAll = (Button) findViewById(R.id.changeAll);
        //SETTING UP RECEIVING USER INFO
        Intent intent = getIntent();
        theUser = intent.getParcelableExtra("theUser");
        usernameS = theUser.getUsername();
        String disName = theUser.getDisplayName().replace("_", " ");
        String des = theUser.getDescription();
        int fase = theUser.getPhase();
        String spes = theUser.getSpecial();
    //GETTING ALL THE OLD INFO FROM USER
        //Getting old extra courses from user
        String prevCourses = theUser.getCourses();
        try {
            if (!prevCourses.equals("null")) {
                String[] slitted = prevCourses.split("_");
                oldCourses.addAll(Arrays.asList(slitted));
            }
        }catch(NullPointerException e){
            prevCourses = "";
            System.out.println(e);
        }
        //Setting up old settings
        if (!disName.equals("null")) {
            dispName.setText(disName);
        }
        if (!des.equals("null")) {
            description.setText(des);
        }
        if (fase != 0) {
            int in = getIndex(phase, String.valueOf(fase));
            phase.setSelection(in);
            if (in > 1) {
                if (!special.equals("null")) {
                    special.setSelection(getIndex(special, spes));
                } else {
                    special.setSelection(0);
                    special.setEnabled(false);
                }
            }
        } else {
            special.setSelection(0);
            special.setEnabled(false);
        }

        //SETTING UP LISTENERS SPINNERS PHASE AND SPECIAL
        phase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position < 2) {
                    special.setSelection(0);
                    special.setEnabled(false);

                } else {
                    special.setEnabled(true);
                }
                if (position != getIndex(phase, String.valueOf(theUser.getPhase()))) {//when the user changes phase/special we want to clear the old extra courses he has slected
                    theUser.setPhase(position);
                    changedPhaseOrSpecial = true;
                    System.out.println("Phase has changed");
                    selectCoursesButton.setText("None Selected");
                    extraCourses.clear();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        special.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position > 0) {
                    selectCoursesButton.setEnabled(true);
                } else {
                    selectCoursesButton.setEnabled(false);
                }
                if (position != getIndex(special, theUser.getSpecial())) {  //when the user changes phase/special we want to clear the old extra courses he has slected
                    theUser.setSpecial((String) special.getSelectedItem());
                    changedPhaseOrSpecial = true;
                    System.out.println("phase has changed");
                    selectCoursesButton.setText("none selected");
                    extraCourses.clear();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        //set text on big spinner button with old courses
        if (!oldCourses.isEmpty()) {
            selectCoursesButton.setText(myStringBuilder(oldCourses));
        } else {
            selectCoursesButton.setText("none selected");
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
     * POSTS all extras to database
     * checks if every field is filled out correctly (1st years dont have to select special)
     * parses URL spaces to underscores (' ' to '_')
     * goes back to main menu if successful
     */
    public void on_Change(View caller) {
        //https://studev.groept.be/api/a19sd405/addExtrasToUser/disp/descr/phase/special/courses/user
        //BUILDING STRING URL
        String ADD_USEREXTRA_BASE_URL = "https://studev.groept.be/api/a19sd405/addExtrasToUser/";
        String dispNameString = dispName.getText().toString().trim();
        if (TextUtils.isEmpty(dispName.getText())) {
            Toast.makeText(settingsActivity.this, "Please fill out your displayName", Toast.LENGTH_SHORT).show();
            return;
        } else {
            ADD_USEREXTRA_BASE_URL = ADD_USEREXTRA_BASE_URL + dispNameString + "/";
        }
        String descripString = description.getText().toString().trim();
        if (TextUtils.isEmpty(description.getText())) {
            Toast.makeText(settingsActivity.this, "Please fill out your description ", Toast.LENGTH_SHORT).show();
            return;
        } else {
            ADD_USEREXTRA_BASE_URL = ADD_USEREXTRA_BASE_URL + descripString + "/";
        }

        String phaseString = phase.getSelectedItem().toString();
        if (phaseString.equals("none")) {
            Toast.makeText(settingsActivity.this, "Please fill in your phase", Toast.LENGTH_SHORT).show();
            return;
        } else {
            ADD_USEREXTRA_BASE_URL = ADD_USEREXTRA_BASE_URL + phaseString + "/";
        }


        String specialString = special.getSelectedItem().toString();
        if (specialString.equals("none")) {
            //If user is a first year we dont need to set a special
            if (getIndex(phase, phaseString) < 2) {
                ADD_USEREXTRA_BASE_URL = ADD_USEREXTRA_BASE_URL + "null" + "/";
            } else {
                Toast.makeText(settingsActivity.this, "Please fill in your specialization |UwU/", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            ADD_USEREXTRA_BASE_URL = ADD_USEREXTRA_BASE_URL + specialString + "/";
        }
        //extra courses arent mandatory
        if (extraCourses.isEmpty()) {
            ADD_USEREXTRA_BASE_URL = ADD_USEREXTRA_BASE_URL + "null" + "/";
        } else {
            ADD_USEREXTRA_BASE_URL = ADD_USEREXTRA_BASE_URL + myStringBuilder(extraCourses).toString() + "/";
        }
        ADD_USEREXTRA_BASE_URL = ADD_USEREXTRA_BASE_URL + usernameS;
        ADD_USEREXTRA_BASE_URL = ADD_USEREXTRA_BASE_URL.replaceAll(" ", "_");
        System.out.println(ADD_USEREXTRA_BASE_URL);
        //POSTING
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest requestPost = new StringRequest(Request.Method.POST, ADD_USEREXTRA_BASE_URL, new Response.Listener<String>() {
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
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("theUser", (Parcelable) theUser);
        startActivity(mainIntent);
    }
    /**
     * On click big spinner
     * Gives a list of all extra courses a user might want to take
     * when it is clicked we want to check off any courses previously selected
     * set text on button= courses the user selected
     */
    public void onClickBigSpin(View view) {
        if (view.getId() == R.id.selectCourses) {
            //get all possible extra courses for user
            userHasCourses = getUserNONPhaseCourses();
            //boolean array for checked boxes per course
            boolean[] checkedCourses = new boolean[userHasCourses.size()];
            //setup charsequence for OnMultiChoiceClickListener
            final CharSequence[] selection = userHasCourses.toArray(new CharSequence[userHasCourses.size()]);
            //is the user hasn't phase/specialty we display his previously selected extra courses
            if (!changedPhaseOrSpecial) {
                extraCourses.addAll(oldCourses);
                //checks of previously selected courses
                int count = selection.length;
                for (int i = 0; i < count; i++) {
                    checkedCourses[i] = extraCourses.contains(selection[i]);
                }
            }
            //big spinner setup
            DialogInterface.OnMultiChoiceClickListener DialogListener = new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (isChecked) {
                        extraCourses.add(selection[which]);
                    } else {
                        extraCourses.remove(selection[which]);
                    }
                    System.out.println(extraCourses);
                    selectCoursesButton.setText(myStringBuilder(extraCourses).toString());
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select extra courses");
            builder.setMultiChoiceItems(selection, checkedCourses, DialogListener);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

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

    /**
     * WHAT FOLLOWS ARE UTILITY FUNCTIONS
     */


    /**
     * Builds a String with " " inbetween different objects in charSequence arrayList
     */
    private StringBuilder myStringBuilder(ArrayList<CharSequence> seq) {
        StringBuilder stringBuilder = new StringBuilder();
        for (CharSequence x : seq) {
            stringBuilder.append(x + " ");
        }
        return stringBuilder;
    }
    /**
     * gets the index of an item on a spinner
     */
    private int getIndex(Spinner spin, String stingetje) {
        int index = 0;
        for (int i = 0; i < phase.getCount(); i++) {
            if (spin.getItemAtPosition(i).equals(stingetje)) {
                index = i;
            }
        }
        return index;
    }

    /**
     *  returns all courses that belong tho the phase below and above the user's phase aka all courses he might have to redo/does from a higher year
     */
    private ArrayList<CharSequence> getUserNONPhaseCourses() {
        userHasCourses = new ArrayList<>();
        System.out.println(theUser.getPhase());
        switch (theUser.getPhase()) {
            case 1:
                userHasCourses.addAll(Arrays.asList(courses2Algemeen));
                userHasCourses.addAll(Arrays.asList(courses2EA));
                userHasCourses.addAll(Arrays.asList(courses2EM));
                userHasCourses.addAll(Arrays.asList(courses2CHEM));
                System.out.println(1);
                break;
            case 2:
                System.out.println(2);
                userHasCourses.addAll(Arrays.asList(courses1));
                userHasCourses.addAll(Arrays.asList(courses3Algemeen));
                System.out.println(theUser.getSpecial());
                switch (theUser.getSpecial()) {
                    case "EM":
                        userHasCourses.addAll(Arrays.asList(courses3EM));
                        break;
                    case "EAICT":
                        userHasCourses.addAll(Arrays.asList(courses3EA));
                        break;
                    case "BioChem":
                        userHasCourses.addAll(Arrays.asList(courses3CHEMBIO));
                        break;
                    case "Chemie":
                        userHasCourses.addAll(Arrays.asList(courses3CHEM));
                        break;
                }
                break;
            case 3:
                System.out.println(3);
                userHasCourses.addAll(Arrays.asList(courses2Algemeen));
                userHasCourses.addAll(Arrays.asList(master));
                switch (theUser.getSpecial()) {
                    case "EM":
                        userHasCourses.addAll(Arrays.asList(courses2EM));
                        break;
                    case "EAICT":
                        userHasCourses.addAll(Arrays.asList(courses2EA));
                        break;
                    case "BioChem":
                    case "Chemie":
                        userHasCourses.addAll(Arrays.asList(courses2CHEM));
                        break;
                }
                break;
            case 4:
                System.out.println(4);
                userHasCourses.addAll(Arrays.asList(courses3Algemeen));
                switch (theUser.getSpecial()) {
                    case "EM":
                        userHasCourses.addAll(Arrays.asList(courses3EM));
                        break;
                    case "EAICT":
                        userHasCourses.addAll(Arrays.asList(courses3EA));
                        break;
                    case "BioChem":
                        userHasCourses.addAll(Arrays.asList(courses3CHEMBIO));
                        break;
                    case "Chemie":
                        userHasCourses.addAll(Arrays.asList(courses3CHEM));
                        break;
                }
                break;
        }
        return userHasCourses;
    }




}

