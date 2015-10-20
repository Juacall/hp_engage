package apps.hp.hp_engage;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    private final String TAG = "MainActivityFragment";
    private VideoView videoView;
    private FragmentInteractionListener listener;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_main, container, false);
        videoView = (VideoView) v.findViewById(R.id.videoView);
        videoView.setVisibility(View.GONE);
        listener = (FragmentInteractionListener) getActivity();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MediaController ctrl = new MediaController(getActivity());
                ctrl.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.engage));
                videoView.setMediaController(ctrl);
                videoView.requestFocus();
                videoView.start();
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                       // mp.release();
                        videoView.setVisibility(View.GONE);
                        listener.showAuraList();

                    }
                });
            }
        }, 2000);



        return v;
    }



}
