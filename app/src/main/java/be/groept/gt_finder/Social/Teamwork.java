package be.groept.gt_finder.Social;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

import be.groept.gt_finder.Main.MainActivity;
import be.groept.gt_finder.R;
import be.groept.gt_finder.User.User;

public class Teamwork extends AppCompatActivity {
    private User theUser;
    private ArrayList<String> userCourses = new ArrayList<>();
    private Button btn;
    private String vakje;

    /**
     * Firstly receives all interesting data from intent
     * then sets up a list with all the courses user has access to
     * then setup a button for every course from ^^
     * gives button listeners
     * executes toGroepswerken when button is pressed
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SETTING UP RECEIVING USER INFO
        Intent intent = getIntent();
        theUser = intent.getParcelableExtra("theUser");
        String prevCourses = theUser.getCourses();
        //adding courses from phase
        String phaseSpecial = String.valueOf(theUser.getPhase()) + theUser.getSpecial();
        userCourses.addAll(Arrays.asList(theUser.getCoursesFromPhase(phaseSpecial)));
        //adding extra courses
        if (!prevCourses.equals("")) {
            String[] slitted = prevCourses.split("_");
            userCourses.addAll(Arrays.asList(slitted));
        }
        System.out.println(userCourses);
        //Setting up buttons for every course user had
        setContentView(R.layout.activity_teamwork);
        LinearLayout layout = (LinearLayout) findViewById(R.id.yourlayout);
        layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        Display screensize = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screensize.getSize(size);
        int width = size.x;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width - 100, 100);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, 5, 0, 5);

        for (int i = 0; i < userCourses.size(); i++) {
            btn = new Button(Teamwork.this);
            btn.setWidth(40);
            btn.setBackgroundColor(Color.parseColor("#006064"));
            btn.setTextColor(Color.parseColor("#B2FFFFFF"));
            btn.setHeight(20);
            btn.setText(userCourses.get(i));
            vakje = userCourses.get(i);
            layout.addView(btn, layoutParams);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    toGroepswerken(v);
                }
            });

        }
    }

    /**
     * gets view v from prev method cast to button since caller is always a button
     * goes to next activity, gives user and text on clicked button (name of course) to next activity
     */
    private void toGroepswerken(View v) {
        Button btn = (Button) v;
        Intent teamIntent = new Intent(this, GroepswerkVakX.class);
        teamIntent.putExtra("theUser", (Parcelable) theUser);
        teamIntent.putExtra("vak", btn.getText().toString());
        startActivity(teamIntent);
    }

    /**
     * This method handles backpresses. This way it's harder to accidentally press back when you're already logged in.
     */
    @Override
    public void onBackPressed() {
        Intent goToMain = new Intent(this, MainActivity.class);
        goToMain.putExtra("theUser", (Parcelable) theUser);
        startActivity(goToMain);

    }
}
