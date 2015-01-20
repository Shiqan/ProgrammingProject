/**
 * 
 */
package nl.ferron.saan;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Ferron
 *
 */
public class UserDataManager {
	
	//====================================================
	// CONSTANTS
	//====================================================
	private static UserDataManager INSTANCE = new UserDataManager();;
	private static final String PREFS_NAME = "myGameData";
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;

	private static final String SOUND_KEY = "soundKey";
	private static final String PLAYED_KEY = "playedKey";
	private static final String SCORE_KEY = "scoreKey";
	
	//====================================================
	// VARIABLES
	//====================================================
	private boolean mSoundMuted;
	private int mNumberOfGames;
	private float mMaxScore;
	
	//====================================================
	// CONSTRUCTOR
	//====================================================
	private UserDataManager(){
	}

	//====================================================
	// GETTERS & SETTERS
	//====================================================
	// Retrieves a global instance of the UserDataManager
	public synchronized static UserDataManager getInstance() {
		return INSTANCE;
	}
	
	//====================================================
	// PUBLIC METHODS
	//====================================================
	public synchronized void setup(Context pContext) {
		if (mSettings == null) {
			mSettings = pContext.getSharedPreferences(PREFS_NAME, 0);
			mEditor = mSettings.edit();
			
			mEditor.remove("deathKey");
			
			// Retrieve values
			mSoundMuted = mSettings.getBoolean(SOUND_KEY, false);
			mNumberOfGames = mSettings.getInt(PLAYED_KEY, (int) 0);
			mMaxScore = mSettings.getFloat(SCORE_KEY, (float) 0);
		}
	}

	// ============================ SOUND ================= //
	public synchronized boolean isSoundMuted() {
		return mSoundMuted;
	}

	public synchronized void setSoundMuted(boolean pEnableSound) {
		mSoundMuted = pEnableSound;
		mEditor.putBoolean(SOUND_KEY, mSoundMuted);
		mEditor.commit();
	}
	
	// ============================ DEATHS ================= //
	public synchronized int getNumberOfGames() {
		return mNumberOfGames;
	}
	
	public synchronized void increaseNumberOfGames() {
		mNumberOfGames++;
		mEditor.putInt(PLAYED_KEY, mNumberOfGames);
		mEditor.commit();
	}
	
	// ============================ SCORE ================= //
	public synchronized float getMaxScore() {
		return mMaxScore;
	}
	
	public synchronized void setMaxScore(float pMaxScore) {
		mMaxScore = pMaxScore;
		mEditor.putFloat(SCORE_KEY, mMaxScore);
		mEditor.commit();
	}
}
