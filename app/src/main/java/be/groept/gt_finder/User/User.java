package be.groept.gt_finder.User;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

public class User implements Parcelable {
    private String username;
    private String displayName;
    private String email;
    private String password;
    private String[] courses1 = {"EE2","Philo", "Dynamics"};
    private String[] courses2Algemeen = {"Electromagnetism", "Strength-of-materials", "Thermodynamica", "EE3", "STE", "IC", "ComII"};
    private String[] courses2EM = {"EE4-EM", "Elektrotechniek", "Dynamica-Starre-Lichamen"};
    private String[] courses2EA = {"ElOn", "SoftDev", "EE4-EA"};
    private String[] courses2CHEM = {"EE4-chemie", "EE4-BIOCHEM", "Biochemie", "Industriele-chemie"};
    private String[] courses3Algemeen = {"Religions", "Management", "CommunicationIII"};
    private String[] courses3EM = {"EE5-EM", "MachineParts", "Material-Selection", "Mechanical-design", "Electric-installations"};
    private String[] courses3EA = {"ComputerNetworks", "Controlsystems", "Datacommunication-WLAN", "DataScience", "Digitale-Signaalverwerking", "Digitale-systemen", "Elektrische-motoren"};
    private String[] courses3CHEM = {"Chemical-engineering", "New-materials", "Fysico-chemie", "EE5-CHEM"};
    private String[] courses3CHEMBIO = {"FoodTechnology", "Microbiologie", "Moleculaire-Celbiologie", "EE5-BIOCHEM"};
    private String[] master = {"helpikkengeenmastervakken", "moeilijkvak1", "mamagement17", "EE15"};
    private int phase;
    private String special;
    private String description;
    private String courses;
    private Bitmap userBitMap;

    private byte[] userPicBit;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    protected User(Parcel in) {
        username = in.readString();
        password = in.readString();
        email = in.readString();
        displayName = in.readString();
        special = in.readString();
        phase = in.readInt();
        description = in.readString();
        courses = in.readString();
        userPicBit = in.createByteArray();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    public void setCourses(String courses) {
        this.courses = courses;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPhase() {
        return phase;
    }

    public String getSpecial() {
        return special;
    }

    public String getDescription() {
        return description;
    }

    public String getCourses(){return courses;}

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public void setSpecial(String special) {
        this.special = special;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getUserPicBit() {
        return userPicBit;
    }

    public void setUserPicBit(byte[] userPicBit) {
        this.userPicBit = userPicBit;
    }

    public Bitmap getUserBitMap() { return userBitMap; }

    public void setUserBitMap(Bitmap userBitMap) { this.userBitMap = userBitMap; }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(email);
        parcel.writeString(displayName);
        parcel.writeString(special);
        parcel.writeInt(phase);
        parcel.writeString(description);
        parcel.writeString(courses);
        parcel.writeByteArray(userPicBit);
    }


    public String[] getCoursesFromPhase(String x){
        ArrayList<String> blub = new ArrayList<>();
        switch (x){
            case "1null":
                return courses1;
            case "2EM":
                blub.addAll(Arrays.asList(courses2Algemeen));
                blub.addAll(Arrays.asList(courses2EM));
                String[] toReturn = (String[]) blub.toArray(new String[0]);
                return toReturn;
            case "2EAICT":
                blub.addAll(Arrays.asList(courses2Algemeen));
                blub.addAll(Arrays.asList(courses2EA));
                String[] toReturn12 = (String[]) blub.toArray(new String[0]);
                return toReturn12;
            case "2Chemie":
                blub.addAll(Arrays.asList(courses2Algemeen));
                blub.addAll(Arrays.asList(courses2CHEM));
                String[] toReturn2 = (String[]) blub.toArray(new String[0]);
                return toReturn2;
            case"2BioChem":
                blub.addAll(Arrays.asList(courses2Algemeen));
                blub.addAll(Arrays.asList(courses2EM));
                String[] toReturn3 = (String[]) blub.toArray(new String[0]);
                return toReturn3;
            case"3BioChem":
                blub.addAll(Arrays.asList(courses3Algemeen));
                blub.addAll(Arrays.asList(courses3CHEMBIO));
                String[] toReturn4 = (String[]) blub.toArray(new String[0]);
                return toReturn4;
            case"3Chemie":
                blub.addAll(Arrays.asList(courses3Algemeen));
                blub.addAll(Arrays.asList(courses3CHEM));
                String[] toReturn5 = (String[]) blub.toArray(new String[0]);
                return toReturn5;
            case"3EAICT":
                blub.addAll(Arrays.asList(courses3Algemeen));
                blub.addAll(Arrays.asList(courses3EA));
                String[] toReturn6 = (String[]) blub.toArray(new String[0]);
                return toReturn6;
            case"3EM":
                blub.addAll(Arrays.asList(courses3Algemeen));
                blub.addAll(Arrays.asList(courses3EM));
                String[] toReturn7 = (String[]) blub.toArray(new String[0]);
                return toReturn7;
            case"4EAICT":
                blub.addAll(Arrays.asList(master));
                String[] toReturn8 = (String[]) blub.toArray(new String[0]);
                return toReturn8;
            case"4EM":
                blub.addAll(Arrays.asList(master));
                String[] toReturn9 = (String[]) blub.toArray(new String[0]);
                return toReturn9;
            case"4Chemie":
                blub.addAll(Arrays.asList(master));
                String[] toReturn10 = (String[]) blub.toArray(new String[0]);
                return toReturn10;
            case"4BioChem":
                blub.addAll(Arrays.asList(master));
                String[] toReturn11 = (String[]) blub.toArray(new String[0]);
                return toReturn11;
            default:
                return new String[0];
        }
    }


}
