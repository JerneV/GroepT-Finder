package be.groept.gt_finder.Social;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import be.groept.gt_finder.R;
import be.groept.gt_finder.User.User;



public class arrayAdapter extends ArrayAdapter<User>{

    Context context;
    private ImageView image;
    private  User aUser;

    public arrayAdapter(Context c, int resourceId, List<User> users){
        super(c, resourceId, users);
        this.context = c;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        aUser = getItem(position);
        System.out.println(aUser);
        if (convertView == null){
            System.out.println("convertview werkt");
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView desc = (TextView) convertView.findViewById(R.id.desc);
        image = (ImageView) convertView.findViewById(R.id.itemImage);
        name.setText(aUser.getDisplayName().replace("_"," "));
        desc.setText(aUser.getDescription().replace("_"," "));
        try{image.setImageBitmap(aUser.getUserBitMap());}
        catch (NullPointerException addDefault){//TODO fix default picca
        }
        return convertView;

    }

}