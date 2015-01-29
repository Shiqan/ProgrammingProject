package nl.ferron.saan;

/**
 * Activity for main menu
 * @author Ferron
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

public class MainActivity extends Activity implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private TextView mTxtPlayer;
	private MediaPlayer mMediaPlayer;

	// Client used to interact with Google APIs
	private static GoogleApiClient mGoogleApiClient;

	// Are we currently resolving a connection failure?
	private boolean mResolvingConnectionFailure = false;

	// Has the user clicked the sign-in button?
	public static boolean mSignInClicked = false;
	public static boolean mSignOutClicked = false;
	public static boolean mSignInFailed = false;

	// Automatically start the sign-in flow when the Activity starts
	private boolean mAutoStartSignInFlow = true;

	// request codes we use when invoking an external activity
	private static final int RC_RESOLVE = 5000;
	private static final int RC_UNUSED = 5001;
	private static final int RC_SIGN_IN = 9001;

	private static String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		// Create the Google API Client with access to Plus and Games
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN).addApi(Games.API)
				.addScope(Games.SCOPE_GAMES).build();

		// Initialise UserDataManager
		UserDataManager.getInstance().setup(MainActivity.this);

		// Change font of textviews
		TextView txtTitle = (TextView) findViewById(R.id.txt_title_main);
		Typeface type = Typeface.createFromAsset(getAssets(),
				"fonts/bigjohn.otf");
		txtTitle.setTypeface(type);

		mTxtPlayer = (TextView) findViewById(R.id.txt_player);
		final Typeface type2 = Typeface.createFromAsset(getAssets(),
				"fonts/slimjoe.otf");
		mTxtPlayer.setTypeface(type2);

		TextView txtStart = (TextView) findViewById(R.id.txt_start);
		txtStart.setTypeface(type2);

		// Set tween animation on start text
		Animation myFadeInAnimation = AnimationUtils.loadAnimation(
				MainActivity.this, R.anim.tween);
		txtStart.startAnimation(myFadeInAnimation);

		// Show info dialog
		ImageButton btnInfo = (ImageButton) findViewById(R.id.btn_info);
		btnInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Custom dialog
				final Dialog dialog = new Dialog(MainActivity.this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.dialog_info);

				TextView text = (TextView) dialog
						.findViewById(R.id.dialog_text);
				text.setTypeface(type2);

				// Open browser
				ImageButton dialogWebButton = (ImageButton) dialog
						.findViewById(R.id.dialog_btn_image);
				dialogWebButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW,
								Uri.parse("http://www.andengine.org"));
						startActivity(browserIntent);
					}
				});

				// Back to menu
				ImageButton dialogMenuButton = (ImageButton) dialog
						.findViewById(R.id.dialog_btn_menu);
				dialogMenuButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});

		// Sign in or sign out
		ImageButton btnGameServices = (ImageButton) 
				findViewById(R.id.btn_game_services);
		btnGameServices.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSignedIn()) {
					mGoogleApiClient.disconnect();
					mSignInClicked = false;
					mSignOutClicked = true;
					mTxtPlayer.setText(MainActivity.this
							.getString(R.string.sign_in));
				} else {
					mGoogleApiClient.connect();
					mSignInClicked = true;
					mSignOutClicked = false;
				}
			}
		});

		// Mute or unmute sound and save the choice
		final ImageButton btnSound = (ImageButton) findViewById(R.id.btn_sound);
		if (UserDataManager.getInstance().isSoundMuted()) {
			btnSound.setBackgroundResource(R.drawable.btn_sound_off);
		} else {
			btnSound.setBackgroundResource(R.drawable.btn_sound_on);
		}
		btnSound.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserDataManager.getInstance().setSoundMuted(
						!UserDataManager.getInstance().isSoundMuted());
				if (UserDataManager.getInstance().isSoundMuted()) {
					btnSound.setBackgroundResource(R.drawable.btn_sound_off);
					mMediaPlayer.stop();
					mMediaPlayer.release();
				} else {
					btnSound.setBackgroundResource(R.drawable.btn_sound_on);
					mMediaPlayer = MediaPlayer.create(MainActivity.this,
							R.raw.menu);
					mMediaPlayer.setLooping(true);
					mMediaPlayer.start();
				}
			}
		});

		// Show Google Achievements
		ImageButton btnAchievements = (ImageButton) 
				findViewById(R.id.btn_achievements);
		btnAchievements.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onShowAchievementsRequested();
			}
		});

		// Show Google Leaderboards
		ImageButton btnLeaderboards = (ImageButton) 
				findViewById(R.id.btn_leaderboards);
		btnLeaderboards.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onShowLeaderboardsRequested();
			}
		});

	}

	@Override
	public boolean onTouchEvent(MotionEvent touchevent) {
		switch (touchevent.getAction()) {
		case (MotionEvent.ACTION_UP):
			// Intent intent = new Intent(MainActivity.this,
			// GameActivity.class);
			Intent intent = new Intent(MainActivity.this,
					LevelSelectorActivity.class);
			startActivity(intent);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
			finish();
			return true;
		default:
			return super.onTouchEvent(touchevent);
		}
	}

	// ====================================================
	// GOOGLE GAME SERVICES
	// ====================================================

	/**
	 * Check if someone is signed in to google services
	 * 
	 * @return boolean
	 */
	private boolean isSignedIn() {
		return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
	}

	/**
	 * Show Google achievements or a dialog if not available
	 */
	private void onShowAchievementsRequested() {
		if (isSignedIn()) {
			startActivityForResult(
					Games.Achievements.getAchievementsIntent(mGoogleApiClient),
					RC_UNUSED);
		} else {
			createDialog(getString(R.string.achievements_not_available));
		}
	}

	/**
	 * Show Google leaderbords or a dialog if not available
	 */
	private void onShowLeaderboardsRequested() {
		if (isSignedIn()) {
			startActivityForResult(
					Games.Leaderboards
							.getAllLeaderboardsIntent(mGoogleApiClient),
					RC_UNUSED);
		} else {
			createDialog(getString(R.string.leaderboards_not_available));

		}
	}

	/**
	 * Create dialog when Google Play services is not available
	 * 
	 * @param s
	 *            string to display in dialog
	 */
	private void createDialog(String s) {
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_error);

		TextView text = (TextView) dialog.findViewById(R.id.dialog2_text);
		text.setText(s);
		text.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/slimjoe.otf"));

		// Stats
		ImageButton dialogStatsButton = (ImageButton) dialog
				.findViewById(R.id.dialog2_btn_stats);
		dialogStatsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						StatsActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
				finish();
			}
		});
		// Back to menu
		ImageButton dialogMenuButton = (ImageButton) dialog
				.findViewById(R.id.dialog2_btn_menu);
		dialogMenuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == RC_SIGN_IN) {
			mSignInClicked = false;
			mResolvingConnectionFailure = false;
			if (resultCode == RESULT_OK) {
				mGoogleApiClient.connect();
			} else {
				BaseGameUtils.showActivityResultError(this, requestCode,
						resultCode, R.string.signin_other_error);
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
			displayName = "???";
		} else {
			displayName = p.getDisplayName();
		}
		mTxtPlayer.setText("Hello " + displayName);
		mSignInFailed = false;
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
			if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
					connectionResult, RC_SIGN_IN,
					getString(R.string.signin_other_error))) {
				mResolvingConnectionFailure = false;
			}
		}

		// Sign-in failed, so show failed message on main menu
		mTxtPlayer.setText(getString(R.string.sign_in));
		mSignInFailed = true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart(): connecting");
		if (!isSignedIn() && mAutoStartSignInFlow & !mSignInFailed) {
			mGoogleApiClient.connect();
		}

		if (!UserDataManager.getInstance().isSoundMuted()) {
			mMediaPlayer = MediaPlayer.create(this, R.raw.menu);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.start();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop(): disconnecting");
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}

		if (!UserDataManager.getInstance().isSoundMuted()) {
			mMediaPlayer.release();
		}
	}
}
