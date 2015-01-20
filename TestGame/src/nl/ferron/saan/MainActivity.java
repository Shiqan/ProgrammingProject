package nl.ferron.saan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	
	// Client used to interact with Google APIs
    private static GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // request codes we use when invoking an external activity
    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    
    private static String TAG = "TESTGAME";
    private TextView txtPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        
        // Create the Google API Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        
        // Initialise UserDataManager
        UserDataManager.getInstance().setup(MainActivity.this);
      
		TextView txtTitle = (TextView) findViewById(R.id.txt_title_main);
		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/bigjohn.otf");
		txtTitle.setTypeface(type);
		
		txtPlayer = (TextView) findViewById(R.id.txt_player);
		Typeface type2 = Typeface.createFromAsset(getAssets(),"fonts/slimjoe.otf");
		txtPlayer.setTypeface(type2);
			    
	    ImageButton btnStart = (ImageButton) findViewById(R.id.btn_start);
	    btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, GameActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
			}
		});

		ImageButton btnInfo = (ImageButton) findViewById(R.id.btn_info);
		btnInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO custom dialog
			}
		});
		
		ImageButton btnStats = (ImageButton) findViewById(R.id.btn_stats);
		btnStats.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSignedIn()) {
					onShowAchievementsRequested();
				} else {
					Intent intent = new Intent(MainActivity.this, StatsActivity.class);
					startActivity(intent);
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
					finish();
				}
			}
		});	
				
		ImageButton btnGameServices = (ImageButton) findViewById(R.id.btn_game_services);
		btnGameServices.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSignedIn()) {
					mGoogleApiClient.disconnect();
					mSignInClicked = false;
					txtPlayer.setText("Sign in");
				} else {
					mGoogleApiClient.connect();
					mSignInClicked = true;
				}
			}
		});	
		
		final ImageButton btnSound = (ImageButton) findViewById(R.id.btn_sound);
		if (UserDataManager.getInstance().isSoundMuted()) {
			btnSound.setBackgroundResource(R.drawable.btn_sound_off);
		} else {
			btnSound.setBackgroundResource(R.drawable.btn_sound_on);
		}
		btnSound.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserDataManager.getInstance().setSoundMuted(!UserDataManager.getInstance().isSoundMuted());
				if (UserDataManager.getInstance().isSoundMuted()) {
					btnSound.setBackgroundResource(R.drawable.btn_sound_off);
				} else {
					btnSound.setBackgroundResource(R.drawable.btn_sound_on);
				}
			}
		});
	}
	
	// ====================================================
	// GOOGLE GAME SERVICES
	// ====================================================
	
	/**
	 * Check if someone is signed in to google services
	 * @return boolean
	 */
	public static boolean isSignedIn() {
		return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
	}
	
    public void onShowAchievementsRequested() {
        if (isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.achievements_not_available)).show();
        }
    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        
        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
        }
        txtPlayer.setText("Hello " + displayName);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Sign-in failed, so show sign-in button on main menu
        txtPlayer.setText(getString(R.string.sign_in_failed));
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): connecting");
        if (!isSignedIn()) {
        	mGoogleApiClient.connect();
        }
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy(): disconnecting");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
