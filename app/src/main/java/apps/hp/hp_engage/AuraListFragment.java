package apps.hp.hp_engage;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.aurasma.aurasmasdk.Aura;
import com.aurasma.aurasmasdk.QueryService;
import com.aurasma.aurasmasdk.RelationService;
import com.aurasma.aurasmasdk.ResultHandler;
import com.aurasma.aurasmasdk.UniversalService;
import com.aurasma.aurasmasdk.errors.AurasmaException;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by bernajua on 10/13/2015.
 */
public class AuraListFragment extends Fragment implements View.OnClickListener{

    private final String TAG = "AurasmaList";
    private  RecyclerView mRecyclerView;
    private List<String> idsToGet = new ArrayList<>();
    private AuraAdapter auraAdapter;
    private AuraDetails[] auras;
    private final Semaphore intentSemaphore = new Semaphore(0);
    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private FragmentInteractionListener listener;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.aura_list, parent, false);

        listener = (FragmentInteractionListener) getActivity();
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.openAurasma();
            }
        });

        if(auras != null && auras[0] != null)
        {
            hideProgressBar();
            setAdapter();
        }else
        {
            showProgressBar();
            listSuper();
        }


        return v;
    }

    private void listSuper() {
        idsToGet.clear();
        try {
            UniversalService service = UniversalService.getService(AurasmaContextHolder.getAurasmaContext(getActivity()));

            service.listChannels(null, new ResultHandler<List<String>>() {
                @Override
                public void handleResult(final List<String> results, final Throwable throwable) {
                    if (throwable != null) {
                        String logMessage = "listChannels failed:";
                        if (throwable instanceof AurasmaException) {
                            Log.e(TAG, logMessage + ((AurasmaException) throwable).getErrorType(), throwable);
                        } else {
                            Log.e(TAG, logMessage, throwable);
                        }
                        return;
                    }

                    if (mRecyclerView == null) {
                        Log.e(TAG, "No list adapter");
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //       listAdapter.clear();
                            //       listAdapter.addAll(results);
                            // mRecyclerView.setAdapter((auraAdapter = new AuraAdapter(thumbMap)));
                            idsToGet.addAll(results);
                            getChannelAuraIds();


                        }
                    });


                    //   getNextThumbnail();
                }
            });
        }  catch (AurasmaException aurasmaException) {
            Log.e(TAG, "Failed to get universal, errorType: " + aurasmaException.getErrorType(), aurasmaException);
        }
    }

    private void getChannelAuraIds()  {
        try {
            RelationService service = RelationService.getService(AurasmaContextHolder.getAurasmaContext(getActivity()));
            service.listAurasForChannel(idsToGet.get(0), null, new ResultHandler<List<String>>() {
                @Override
                public void handleResult(final List<String> results, Throwable throwable) {

                    if (throwable != null) {
                        String logMessage = "listChannels failed:";
                        if (throwable instanceof AurasmaException) {
                            Log.e(TAG, logMessage + ((AurasmaException) throwable).getErrorType(), throwable);
                        } else {
                            Log.e(TAG, logMessage, throwable);
                        }
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            idsToGet.clear();
                            idsToGet.addAll(results);
                            auras = new AuraDetails[idsToGet.size()];
                            getAuraDetails();

                        }
                    });


                }
            });
        } catch (AurasmaException e) {
            e.printStackTrace();
        }

    }


    private void getAuraDetails()
    {
        intentSemaphore.drainPermits();
        try {
            for(int i=0; i < auras.length; i++)
            {
                Log.i(TAG, "Getting Aura Details For Position " + i);
                auras[i] = new AuraDetails();
                String id = idsToGet.remove(0);
                getAura(id, auras[i]);

            }
            intentSemaphore.acquire(auras.length);
           setAdapter();
            hideProgressBar();
            listener.aurasLoaded(auras);
          //  listener.startService(auras);

        } catch (AurasmaException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private void getAura(final String id,final AuraDetails a) throws AurasmaException {

        final AuraDetails  auraDetails = new AuraDetails();
        QueryService queryService = QueryService.getService(AurasmaContextHolder.getAurasmaContext(getActivity()));
       //  Log.i(TAG,"Getting Aura... ID: " + id);
        queryService.getAura(id, null, new ResultHandler<Aura>() {
            @Override
            public void handleResult(Aura aura, Throwable throwable) {
                if (throwable != null) {
                    String logMessage = "getAura failed:";
                    if (throwable instanceof AurasmaException) {
                        Log.e(TAG, logMessage + ((AurasmaException) throwable).getErrorType(), throwable);
                    } else {
                        Log.e(TAG, logMessage, throwable);
                    }
                } else {
                    try {
                        a.setAura(aura);
                        //Log.i(TAG, "Aura Retrieved... ID: " + id);

                        getThumbnail(id, a);
                        getLargeImage(id, a);

                    } catch (AurasmaException e) {
                        e.printStackTrace();
                    }

                }

            }
        });
    }

    private void getThumbnail(final String id, final AuraDetails a) throws AurasmaException {



            QueryService queryService = QueryService.getService(AurasmaContextHolder.getAurasmaContext(getActivity()));

      //      Log.i(TAG,"Getting Thumbnail... ID: " + id);

            /////////Retrieve aura image and assign to auradetails class /////////////
            queryService.getThumbnail(a.aura.getTriggerId(), null, new ResultHandler<Bitmap>() {
                @Override
                public void handleResult(Bitmap result, Throwable throwable) {
                    if (throwable != null) {
                        String logMessage = "getThumbfailed failed:";
                        if (throwable instanceof AurasmaException) {
                            Log.e(TAG, logMessage + ((AurasmaException) throwable).getErrorType(), throwable);
                        } else {
                            Log.e(TAG, logMessage, throwable);
                        }
                    } else {

                        a.auraImage = result;
                        intentSemaphore.release();
                        // Log.i(TAG, "Thumbnail Retrieved... ID: " + id);
                    }


                }
            });

    }

    private void getLargeImage(final String id, final AuraDetails a) throws AurasmaException {



        QueryService queryService = QueryService.getService(AurasmaContextHolder.getAurasmaContext(getActivity()));

       // Log.i(TAG,"Getting Large Image... ID: " + id);
        /////////Retrieve aura image and assign to auradetails class /////////////
        queryService.getImagePreview(a.aura.getTriggerId(), null, new ResultHandler<Bitmap>() {
            @Override
            public void handleResult(Bitmap result, Throwable throwable) {
                if (throwable != null) {
                    String logMessage = "getLargeThumbnail failed:";
                    if (throwable instanceof AurasmaException) {
                        Log.e(TAG, logMessage + ((AurasmaException) throwable).getErrorType(), throwable);
                    } else {
                        Log.e(TAG, logMessage, throwable);
                    }
                } else {


                    a.largeAuraImage = result;
                    //Log.i(TAG, "Large Image Retrieved... ID: " + id);
                }


            }
        });

    }

    @Override
    public void onClick(View v) {
        int itemPosition = mRecyclerView.getChildLayoutPosition(v);
      //  String item = auras[itemPosition].aura.getName();
     //   Toast.makeText(getContext(), item, Toast.LENGTH_LONG).show();
        listener.showDetails(auras[itemPosition]);
    }

    private void setAdapter()
    {
        mRecyclerView.setAdapter((auraAdapter = new AuraAdapter(auras, this)));
    }

    private void hideProgressBar()
    {
        progressBar.setVisibility(View.GONE);
    }

    private void showProgressBar()
    {
        progressBar.setVisibility(View.VISIBLE);
    }
}
