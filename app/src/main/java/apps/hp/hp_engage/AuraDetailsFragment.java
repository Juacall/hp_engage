package apps.hp.hp_engage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by bernajua on 10/15/2015.
 */
public class AuraDetailsFragment extends Fragment{

    ImageView imageView;
    TextView auraName, auraBooth;
    AuraDetails aura;

    public  AuraDetailsFragment()
    {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.aura_detail_view, container, false);
        imageView = (ImageView) v.findViewById(R.id.large_aura_image);
        auraName = (TextView) v.findViewById(R.id.aura_name);
        auraBooth = (TextView)v.findViewById(R.id.booth);


        imageView.setImageBitmap(aura.largeAuraImage);
        auraName.setText(aura.aura.getName());
        auraBooth.setText( aura.booth);


        return v;
    }


    public void setAura(AuraDetails aura) {
        this.aura = aura;
    }
}

