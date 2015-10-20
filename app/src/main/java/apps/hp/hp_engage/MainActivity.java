package apps.hp.hp_engage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aurasma.aurasmasdk.Aurasma;
import com.aurasma.aurasmasdk.AurasmaContext;
import com.aurasma.aurasmasdk.AuthState;
import com.aurasma.aurasmasdk.CloudService;
import com.aurasma.aurasmasdk.ResultHandler;
import com.aurasma.aurasmasdk.SyncService;
import com.aurasma.aurasmasdk.errors.AurasmaException;
import com.aurasma.aurasmasdk.http.AuthService;


public class MainActivity extends AppCompatActivity implements FragmentInteractionListener{

    static final String USERNAME_KEY ="Hp_Engage_Username";
    static final String AURSAMPLE_PREFERENCES_KEY = "LjXKzwfTxnbC7G40TTBxgg";
    static final String AppLogName = "Hp_Engage";
    private static final String TAG = "MainActivity" ;
    static Fragment AurViewFragment,OverLayFragment;
    static ActionBar actionBar;
    static Intent mServiceIntent;
    static AuraLocator auraLocator;


    //local enum for setting the slider, related to SyncService
    public enum SyncState {
        StartSync,
        StopSync
    }

    //local enum for setting the slider, related to CloudService
    public enum CloudState {
        StartCloud,
        StopCloud
    }

    private boolean sessionFailureScenario = false;
    private boolean attemptingLogin = false;
    private boolean startingAurasma;

    AurasmaContext aurasmaContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Aurasma.deviceSupportsAurasmaSDK(this)) {
            // If device does not support aurasma, load a different view
            // Ideally this should be checked before going into an activity where any Aurasma calls are made.
            setContentView(R.layout.activity_main_no_aurasma);
            return;
        }
        setContentView(R.layout.activity_main);

        appStartSetup();

        if(auraLocator == null)
        auraLocator = new AuraLocator(this);
        auraLocator.startLocationMonitoring();


        try {
            // Create an AurasmaContext with your licence key.
            aurasmaContext = AurasmaContextHolder.getAurasmaContext(this);
        } catch (AurasmaException aurasmaException) {
            // If this fails there should be a log message in the "Aurasma" channel.
            Log.e("AUR-Sample", "Oh no! Aurasma wasn't able to create a context! Check your license key credentials! errorType: " + aurasmaException.getErrorType(), aurasmaException);
            Toast.makeText(getApplicationContext(), R.string.aurasma_context_creation_failure, Toast.LENGTH_SHORT).show();
            finish();
            return;

        }
    }

    public void appStartSetup()
    {
        actionBar = getSupportActionBar();
        actionBar.hide();
        OverLayFragment = new OverlayFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_content, new MainActivityFragment());
        fragmentTransaction.commit();
    }

    @Override
    public void closeInfo() {

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(OverLayFragment);
        fragmentTransaction.commit();

    }

    @Override
    public void openAurasma() {
        startAurasma();
    }

    @Override
    public void showAuraList() {

        actionBar.show();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0096D6")));
        FragmentManager fragmentManager = getSupportFragmentManager();
        actionBar.setHomeButtonEnabled(false);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //   fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
        fragmentTransaction.replace(R.id.main_content, new AuraListFragment());
        fragmentTransaction.add(R.id.fragment_container, OverLayFragment);
        //fragmentTransaction.remove(fragmentManager.findFragmentById(R.id.main_content));

        fragmentTransaction.commit();

    }

    @Override
    public void showDetails(AuraDetails aura) {
        AuraDetailsFragment ADF = new AuraDetailsFragment();
        ADF.setAura(aura);
        FragmentManager fragmentManager = getSupportFragmentManager();
        setActionBarAsBack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("Details");

        fragmentTransaction.replace(R.id.main_content, ADF);
        fragmentTransaction.commit();

    }

    @Override
    public void aurasLoaded(AuraDetails[] auras) {
        auraLocator.setAuras(auras);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {


            if(OverLayFragment.isAdded())
            fragmentTransaction.show(OverLayFragment);
            else
            fragmentTransaction.add(R.id.fragment_container,OverLayFragment);

            fragmentTransaction.commit();

            return true;
        }
        if(id == android.R.id.home)
        {
            setActionBarNormal();
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startingAurasma = false;
        if (!Aurasma.deviceSupportsAurasmaSDK(this)) {
            return;
        }

        //These lines set a listener to ensure the tracker is ready before showing tracking.
        //It is OPTIONAL to do this- it may only come in use in some circumstances where you are
        //frequently re-loading tracking, and you don't want to show a delay in the UI or want to
        //show a spinner.



        // Will retry login if connection initially fails, but then returns
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        try {
            // Get the AuthService  for logging in as a guest.
            final AuthService authService = AuthService.getService(aurasmaContext);

            if (sessionFailureScenario || authService.getState() != AuthState.Guest) {
                // Login as a guest, if it previously failed or is a new login
                attemptAurasmaGuestLogin(authService);
            } else {
                // Already logged in
               // Toast.makeText(getApplicationContext(), R.string.aurasma_restored_last_session, Toast.LENGTH_LONG).show();
                startSync();
                startCloud();
                //turnOnFollowingButton();
            }
        } catch (AurasmaException e) {
            Toast.makeText(getApplicationContext(), "Failed to get Auth service", Toast.LENGTH_LONG).show();
            Log.e(AppLogName, "Failed to get authService, errorType: " + e.getErrorType(), e);
        }
    }

    @Override
    protected void onPause() {
        if (Aurasma.deviceSupportsAurasmaSDK(this)) {
            unregisterReceiver(connectivityReceiver);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(isFinishing() && Aurasma.deviceSupportsAurasmaSDK(this)) {
            // Stop sync service when exiting
            try {
                final CloudService cloudService = CloudService.getService(aurasmaContext);
                final SyncService syncService = SyncService.getService(aurasmaContext);
                try {
                      cloudService.stop();
                    if (!syncService.stop()) {
                        Log.w("AUR-Sample", "Sync service did not stop in timely fashion. May cause instabilities on context destroy.");
                    }
                } catch (InterruptedException e) {
                    Log.w("AUR-Sample", "Interrupted stopping sync service. May cause instabilities on context destroy.");
                }
            } catch (AurasmaException e) {
                Log.e(AppLogName, "Failed to get syncService, errorType: " + e.getErrorType(), e);
            }
        }
        super.onStop();
    }

    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle extras = intent.getExtras();
            if(extras != null) {
                if(extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
                    Toast.makeText(getApplicationContext(), R.string.aurasma_connectivity_warning, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        final AuthService authService = AuthService.getService(aurasmaContext);
                        if (sessionFailureScenario || authService.getState() != AuthState.Guest) {
                            attemptAurasmaGuestLogin(authService);
                        }
                    } catch (AurasmaException e) {
                        Log.e(AppLogName, "Failed to get auth, errorType: " + e.getErrorType(), e);
                    }
                }
            }
        }
    };

    private synchronized void attemptAurasmaGuestLogin(final AuthService authService) {
        if(attemptingLogin) {
            return;
        }

       // Toast.makeText(getApplicationContext(), R.string.logging_in_toast, Toast.LENGTH_SHORT).show();
        attemptingLogin = true;

        String username = getPersistedUser();
        if (username != null) {
            Log.i(AppLogName, "A user exists. Reuse it");
            //displayGuestName(username);
            loginUser(username, authService);
            return;
        }

        Log.i(AppLogName, "Attempt to create a guest user.");
        // Attempt to create a guest user
        try {

            authService.createGuest(null, new ResultHandler<String>() {
                @Override
                public void handleResult(String result, Throwable error) {
                    if (error == null) {
                        // No error, guest creation has worked
                        // displayGuestName(result);
                        // Log the Guest in.
                        loginUser(result, authService);
                    } else {
                        // Creating a guest failed
                        Log.e("AUR-Sample", "Creating a guest went wrong!", error);
                        if (error instanceof AurasmaException) {
                            Log.e(AppLogName, "Failed to create guest, errorType: " + ((AurasmaException) error).getErrorType(), error);
                        }
                        //in some circumstances this may fire off looper thread.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.unable_to_make_guest, Toast.LENGTH_LONG).show();
                            }
                        });
                        sessionFailureScenario = true;
                        attemptingLogin = false;
                    }
                }
            });
        } catch (AurasmaException e) {
            Log.e(AppLogName, "Failed to create guest from call, errorType: " + e.getErrorType(), e);
        }
    }

    private void loginUser (final String result, final AuthService authService) {
        try {
            authService.loginGuest(result, new ResultHandler<String>() {

                @Override
                public void handleResult(String result, Throwable error) {
                    if (error == null) {
                        // Logging in has worked, so start syncing data with the server.
                        Log.i("Hp_Engage", "Logged in a guest user!");
                        sessionFailureScenario = false;

                        //Always persist the username
                        persistUser(result);

                        startSync();
                        startCloud();
                        //turnOnFollowingButton();
                    } else {
                        // Logging in has failed
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.aurasma_login_guest_failed, Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.e("AUR-Sample", "logging in a guest went wrong! Here is a stack trace: ", error);
                        if (error instanceof AurasmaException) {
                            Log.e(AppLogName, "Failed to loginGuest, errorType: " + ((AurasmaException) error).getErrorType(), error);
                        }
                        sessionFailureScenario = true;
                    }
                    attemptingLogin = false;
                }
            });
        } catch (AurasmaException e) {
            e.printStackTrace();
        }
    }

    private void startSync() {
        try {
            final SyncService syncService = SyncService.getService(aurasmaContext);
            // Start the sync service
            syncService.start();
            if (syncService.isRunning()) {
                syncStateChanged(SyncState.StartSync);
            }
        } catch (AurasmaException e) {
            Log.e(AppLogName, "Failed to get Sync service, errorType: " + e.getErrorType(), e);
        }
    }

    private void startCloud() {
        try {
            final CloudService cloudService = CloudService.getService(aurasmaContext);
            // Start the cloud service
            cloudService.start();
            if (cloudService.isRunning()) {
                cloudStateChanged(CloudState.StartCloud);
            }
        } catch (AurasmaException e) {
            Log.e(AppLogName, "Failed to get Cloud service, errorType: " + e.getErrorType(), e);
        }
    }


    private void persistUser(String username) {
        final SharedPreferences.Editor sharedPrefsEditor = getApplicationContext().getSharedPreferences(AURSAMPLE_PREFERENCES_KEY, Context.MODE_PRIVATE).edit();
        sharedPrefsEditor.putString(USERNAME_KEY, username);
        sharedPrefsEditor.commit();
    }

    private String getPersistedUser() {
        final SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(AURSAMPLE_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedPrefs.getString(USERNAME_KEY, null);
    }

    public synchronized void startAurasma (View button) {
        if(startingAurasma) {
            return;
        }
        startingAurasma = true;
        // Go to the tracking screen, AurViewActivity
        Intent contextActivityIntent = new Intent(this, AurViewActivity.class);
        startActivity(contextActivityIntent);
    }

    public synchronized void startAurasma () {
        if(startingAurasma) {
            return;
        }
        startingAurasma = true;
        if(AurViewFragment == null)
        AurViewFragment = new AurViewFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        setActionBarAsBack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


       // fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
        fragmentTransaction.addToBackStack("Aurasma");
        // Go to the tracking screen, AurViewActivity
        fragmentTransaction.replace(R.id.main_content, AurViewFragment).commit();
        startingAurasma = false;



    }

    public void setActionBarAsBack() {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.mipmap.icn_back_arrow);



    }

    public void setActionBarNormal() {
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);


    }

    private void syncStateChanged (final SyncState syncState) {
        // helper method
        //final Switch syncSwitch = (Switch)  findViewById(R.id.syncSwitch);
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (syncState == SyncState.StartSync) {
                    // syncSwitch.setChecked(true);
                } else {
                    // syncSwitch.setChecked(false);
                }
            }
        });
    }

    private void cloudStateChanged (final CloudState cloudState) {
        // helper method
        //final Switch cloudSwitch = (Switch)  findViewById(R.id.cloudSwitch);
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (cloudState == CloudState.StartCloud) {
                    // cloudSwitch.setChecked(true);
                } else {
                    // cloudSwitch.setChecked(false);
                }
            }
        });
    }





}
