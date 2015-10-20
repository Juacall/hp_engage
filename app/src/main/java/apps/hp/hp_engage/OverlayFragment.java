package apps.hp.hp_engage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * Created by bernajua on 10/6/2015.
 */
public class OverlayFragment extends Fragment {

    FragmentInteractionListener listener;
    private View view;
    private Button  scan;


    public OverlayFragment() {

}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        listener = (FragmentInteractionListener) getActivity();
        view =  inflater.inflate(R.layout.overlay_layout, container, false);
         scan = (Button) view.findViewById(R.id.close_info);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.closeInfo();

            }
        });

        return view;


    }


}

