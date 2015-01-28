package nl.ferron.saan;

/**
 * Class to handle the loading and unloading of all the objects needed for the game
 * @author Ferron
 */


import java.io.IOException;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import android.content.Context;
import android.util.Log;

public class ResourceManager extends Object {
	
	//====================================================
	// CONSTANTS
	//====================================================
	private static final ResourceManager INSTANCE = new ResourceManager();
	
	//====================================================
	// VARIABLES
	//====================================================
	// We include these objects in the resource manager for
	// easy accessibility across our project.
	public Engine mEngine;
	public Context mContext;
	public int mLevelId;
	public float mCameraWidth;
	public float mCameraHeight;
	public float mCameraScaleFactorX;
	public float mCameraScaleFactorY;
	
	// The resource variables listed should be kept public, allowing us easy access
	// to them when creating new Sprite and Text objects and to play sound files.
	// ======================== Game Resources ================= //
	public BuildableBitmapTextureAtlas mButtonAtlas;
	public BuildableBitmapTextureAtlas mGameAtlas;
	
	public static ITextureRegion mBackgroundRegion;
	public static ITextureRegion mBoxRegion;
	public static ITextureRegion mFloor1Region;
	public static ITextureRegion mFloor2Region;
	public ITiledTextureRegion mPlayerRegion;
	public static ITextureRegion mCompleteRegion;
	
	public static ITextureRegion mHandRegion;
	public static ITextureRegion mClingRegion;
	
	public static ITiledTextureRegion mMenuButtonRegion;
	public static ITiledTextureRegion mPauseButtonRegion;
	public static ITiledTextureRegion mRestartButtonRegion;
	public static ITiledTextureRegion mResumeButtonRegion;
	
	public static Music mGameMusic;
	public static Music mOnVictorySound;
	public static Sound mOnDieSound;
	
	public static Font mFontSlimJoe;
	public static Font mFontBigJohn;
	
	// This variable will be used to revert the TextureFactory's default path when we change it.
	private String mPreviousAssetBasePath = "";

	//====================================================
	// CONSTRUCTOR
	//====================================================
	private ResourceManager(){
	}

	//====================================================
	// GETTERS & SETTERS
	//====================================================
	// Retrieves a global instance of the ResourceManager
	public static ResourceManager getInstance(){
		return INSTANCE;
	}
	
	//====================================================
	// PUBLIC METHODS
	//====================================================
	// Setup the ResourceManager
	public void setup(final Engine pEngine, final Context pContext, int id, final float pCameraWidth, final float pCameraHeight, final float pCameraScaleX, final float pCameraScaleY){
		mEngine = pEngine;
		mContext = pContext;
		mLevelId = id;
		mCameraWidth = pCameraWidth;
		mCameraHeight = pCameraHeight;
		mCameraScaleFactorX = pCameraScaleX;
		mCameraScaleFactorY = pCameraScaleY;
	}
	
	// Loads all game resources
	public static void loadGameResources() {
		getInstance().loadGameTextures();
		getInstance().loadSharedResources();
	}
	
	// Unloads all game resources
	public static void unloadGameResources() {
		getInstance().unloadGameTextures();
		getInstance().unloadSharedResources();
	}
	
	// Load all shared resources
	private void loadSharedResources(){
		loadButtonTextures();
		loadSounds();
		loadFonts();
	}
	
	// Unloads all shared resources
	private void unloadSharedResources() {
		getInstance().unloadButtonTextures();
		getInstance().unloadSounds();
		getInstance().unloadFonts();
	}
	
	// ============================ LOAD TEXTURES (GAME) ================= //
	private void loadGameTextures(){
		// Store the current asset base path to apply it after we've loaded our textures
		mPreviousAssetBasePath = BitmapTextureAtlasTextureRegionFactory.getAssetBasePath();
		// Set our game assets folder to "assets/gfx/game/"
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");

		mGameAtlas = new BuildableBitmapTextureAtlas(mEngine.getTextureManager(), 1024, 1536);
		
		mBackgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mGameAtlas, mContext, "bg.png");
		mBoxRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mGameAtlas, mContext, "square.png");
		mCompleteRegion = mBoxRegion;
		mFloor1Region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mGameAtlas, mContext, "floor1.png");			
		mFloor2Region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mGameAtlas, mContext, "floor2.png");
		mPlayerRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mGameAtlas, mContext, "player.png", 6, 1);
		mHandRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mGameAtlas, mContext, "hand.png");
		mClingRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mGameAtlas, mContext, "cling.png");
		      
		
		try {
			mGameAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
			mGameAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		
		// Revert the Asset Path.
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(mPreviousAssetBasePath);
	}
	// ============================ UNLOAD TEXTURES (GAME) =============== //
	private void unloadGameTextures(){
		if (mGameAtlas!=null) {
			mGameAtlas.unload();
			mGameAtlas = null;
			mBackgroundRegion = null;
			mBoxRegion = null;
			mFloor1Region = null;
			mFloor2Region = null;
			mPlayerRegion = null;
			mCompleteRegion = null;
			mHandRegion = null;
			mClingRegion = null;
		}
	}
		
	// ============================ LOAD TEXTURES (SHARED) ================= //
	private void loadButtonTextures() {
		// Store the current asset base path to apply it after we've loaded our textures
		mPreviousAssetBasePath = BitmapTextureAtlasTextureRegionFactory.getAssetBasePath();
		// Set our shared assets folder to "assets/gfx/"
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		mButtonAtlas = new BuildableBitmapTextureAtlas(mEngine.getTextureManager(), 1420, 340);

		mMenuButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mButtonAtlas, mContext, "menu.png", 2, 1);
		mPauseButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mButtonAtlas, mContext, "pause.png",2, 1);
		mResumeButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mButtonAtlas, mContext, "resume.png", 2, 1);
		mRestartButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mButtonAtlas, mContext, "restart.png", 2, 1);
			
			
		try {
			mButtonAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 4));
			mButtonAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		
		// Revert the Asset Path
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(mPreviousAssetBasePath);
	}
	// ============================ UNLOAD TEXTURES (SHARED) ============= //
	private void unloadButtonTextures() {
		if (mButtonAtlas!=null) {
			mButtonAtlas.unload();
			
			mMenuButtonRegion = null;
			mPauseButtonRegion = null;
			mRestartButtonRegion = null;
			mResumeButtonRegion = null;
		}
	}
	
	// =========================== LOAD SOUNDS ======================== //
	private void loadSounds(){
		SoundFactory.setAssetBasePath("sfx/");
		if (mOnDieSound==null) {
			try {
				mOnDieSound	= SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), mContext, "die.ogg");
			} catch (final IOException e) {
				Log.v("Sounds Load","Exception:" + e.getMessage());
			}
		}
	
		MusicFactory.setAssetBasePath("sfx/");
		if (mLevelId == 1) {
			if (mGameMusic==null) {
				try {
					mGameMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), mContext, "vanguard.mp3");
				} catch (final IOException e) {
					Log.e("Music Load","Exception:" + e.getMessage());
				}
			}
		} 
		if (mLevelId == 2) {
			if (mGameMusic==null) {
				try {
					mGameMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), mContext, "monarchy.mp3");
				} catch (final IOException e) {
					Log.e("Music Load","Exception:" + e.getMessage());
				}
			}
		}
		if (mOnVictorySound==null) {
			try {
				mOnVictorySound	= MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), mContext, "victory.ogg");
			} catch (final IOException e) {
				Log.v("Sounds Load","Exception:" + e.getMessage());
			}
		}
	}
	// =========================== UNLOAD SOUNDS ====================== //
	// Unload the sounds and music and make sure to stop it first
	private void unloadSounds(){
		if (mOnDieSound!=null) {
			if (mOnDieSound.isLoaded()) {
				mOnDieSound.stop();
				mEngine.getSoundManager().remove(mOnDieSound);
				mOnDieSound = null;
			}
		}
		if (mOnVictorySound!=null) {
			mOnVictorySound.stop();
			mEngine.getMusicManager().remove(mOnVictorySound);
			mOnVictorySound = null;
		}
		if (mGameMusic!=null) {
			mGameMusic.stop();
			mEngine.getMusicManager().remove(mGameMusic);
			mGameMusic = null;
		}
	}

	// ============================ LOAD FONTS ========================== //
	private void loadFonts(){
		FontFactory.setAssetBasePath("fonts/");
		if (mFontSlimJoe==null) {
			mFontSlimJoe = FontFactory.createFromAsset(mEngine.getFontManager(), mEngine.getTextureManager(), 256, 256, TextureOptions.BILINEAR, mContext.getAssets(), "slimjoe.otf", 32f, true, Color.WHITE_ARGB_PACKED_INT);
			mFontSlimJoe.load(); 
		}
		if (mFontBigJohn==null) {
			mFontBigJohn = FontFactory.createFromAsset(mEngine.getFontManager(), mEngine.getTextureManager(), 512, 512, TextureOptions.BILINEAR, mContext.getAssets(), "bigjohn.otf", 52f, true, Color.WHITE_ARGB_PACKED_INT);
			mFontBigJohn.load(); 
		}
	}
	// ============================ UNLOAD FONTS ======================== //
	private void unloadFonts(){
		if (mFontSlimJoe!=null) {
			mFontSlimJoe.unload();
			mFontSlimJoe = null;
		}
		if (mFontBigJohn!=null) {
			mFontBigJohn.unload();
			mFontBigJohn = null;
		}
	}
}