package nl.ferron.saan;

/**
 * Class to handle the saving and retrieving of data stored in SharedPreferences
 * @author Ferron
 */

import android.content.Context;
import android.content.SharedPreferences;

public class UserDataManager {

	// ====================================================
	// CONSTANTS
	// ====================================================
	private static UserDataManager INSTANCE = new UserDataManager();;
	private static final String PREFS_NAME = "myGameData";
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;

	private static final String SOUND_KEY = "soundKey";
	private static final String SCORE_KEY_LEVEL1 = "scoreKeyLevel1";
	private static final String SCORE_KEY_LEVEL2 = "scoreKeyLevel2";

	// ====================================================
	// VARIABLES
	// ====================================================
	private boolean mSoundMuted;
	private float mMaxScoreLevel1;
	private float mMaxScoreLevel2;

	// ====================================================
	// CONSTRUCTOR
	// ====================================================
	private UserDataManager() {
	}

	// ====================================================
	// GETTERS & SETTERS
	// ====================================================
	// Retrieves a global instance of the UserDataManager
	public synchronized static UserDataManager getInstance() {
		return INSTANCE;
	}

	// ====================================================
	// SETUP
	// ====================================================
	public synchronized void setup(Context pContext) {
		if (mSettings == null) {
			mSettings = pContext.getSharedPreferences(PREFS_NAME, 0);
			mEditor = mSettings.edit();

			// Retrieve values
			mSoundMuted = mSettings.getBoolean(SOUND_KEY, false);
			mMaxScoreLevel1 = mSettings.getFloat(SCORE_KEY_LEVEL1, (float) 0);
			mMaxScoreLevel2 = mSettings.getFloat(SCORE_KEY_LEVEL2, (float) 0);
		}
	}

	// ====================================================
	// METHODS
	// ====================================================

	// ============================ SOUND ================= //
	public synchronized boolean isSoundMuted() {
		return mSoundMuted;
	}

	public synchronized void setSoundMuted(boolean pEnableSound) {
		mSoundMuted = pEnableSound;
		mEditor.putBoolean(SOUND_KEY, mSoundMuted);
		mEditor.commit();
	}

	// ============================ SCORE ================= //
	public float getMaxScore(int id) {
		if (id == 1) {
			return getMaxScoreLevel1();
		}
		if (id == 2) {
			return getMaxScoreLevel2();
		} else {
			return 0;
		}
	}

	public void setMaxScore(int id, float pMaxScore) {
		if (id == 1) {
			setMaxScoreLevel1(pMaxScore);
		}
		if (id == 2) {
			setMaxScoreLevel2(pMaxScore);
		}
	}

	// ============================ SCORE LEVEL 1 ================= //
	private float getMaxScoreLevel1() {
		return mMaxScoreLevel1;
	}

	private void setMaxScoreLevel1(float pMaxScore) {
		mMaxScoreLevel1 = pMaxScore;
		mEditor.putFloat(SCORE_KEY_LEVEL1, mMaxScoreLevel1);
		mEditor.commit();
	}

	// ============================ SCORE LEVEL 1 ================= //
	private float getMaxScoreLevel2() {
		return mMaxScoreLevel2;
	}

	private void setMaxScoreLevel2(float pMaxScore) {
		mMaxScoreLevel2 = pMaxScore;
		mEditor.putFloat(SCORE_KEY_LEVEL2, mMaxScoreLevel2);
		mEditor.commit();
	}
}
