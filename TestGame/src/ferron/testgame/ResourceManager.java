package ferron.testgame;

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
import android.graphics.Typeface;
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
	public Engine engine;
	public Context context;
	public float cameraWidth;
	public float cameraHeight;
	public float cameraScaleFactorX;
	public float cameraScaleFactorY;
	
	// The resource variables listed should be kept public, allowing us easy access
	// to them when creating new Sprite and Text objects and to play sound files.
	// ======================== Game Resources ================= //
	public static ITextureRegion gameBackgroundTextureRegion;
	public static ITextureRegion box_region;
	public static ITextureRegion floor1_region;
	public static ITextureRegion floor2_region;
	public ITiledTextureRegion player_region;
	public static ITextureRegion complete_region;
	
	public static ITextureRegion hand_region;
	public static ITextureRegion cling_region;
	
	public static ITiledTextureRegion menubuttonTiledTextureRegion;
	public static ITiledTextureRegion pausebuttonTiledTextureRegion;
	public static ITiledTextureRegion restartbuttonTiledTextureRegion;
	public static ITiledTextureRegion resumebuttonTiledTextureRegion;
	
	public static Music gameMusic;
	public static Music onVictorySound;
	public static Sound onClickSound;
	public static Sound onDieSound;
	
	public static Font fontDefault32Bold;
	public static Font fontDefault72Bold;
	public static Font fontSlimJoe;
	public static Font fontBigJohn;
	
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
	public void setup(final Engine pEngine, final Context pContext, final float pCameraWidth, final float pCameraHeight, final float pCameraScaleX, final float pCameraScaleY){
		engine = pEngine;
		context = pContext;
		cameraWidth = pCameraWidth;
		cameraHeight = pCameraHeight;
		cameraScaleFactorX = pCameraScaleX;
		cameraScaleFactorY = pCameraScaleY;
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

		// background texture - only load it if we need to:
		if(gameBackgroundTextureRegion==null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 1900, 1090);
			gameBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(texture, context, "background.png");
			try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 4));
				texture.load();
			} catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		
		// test texture:
		if(box_region==null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 256, 256);
			box_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(texture, context, "square.png");
			try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
				texture.load();
			} catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		
		if (floor1_region == null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
			floor1_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(texture, context, "floor1.png");			
	    	try {
	    		texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
	    		texture.load();
			} 
	    	catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		
		if (floor2_region == null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
			floor2_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(texture, context, "floor2.png");
	       try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
				texture.load();
			} 
			catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		
		if (player_region == null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 384, 256, TextureOptions.BILINEAR);
			player_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texture, context, "test.png", 12, 8);
	        try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
				texture.load();
			} 
			catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		
		if (complete_region == null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
			complete_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(texture, context, "square.png");
	        try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
				texture.load();
			} 
			catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		
		if (hand_region == null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 378, 521, TextureOptions.BILINEAR);
			hand_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(texture, context, "hand2.png");
			try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
				texture.load();
			} 
			catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		
		if (cling_region == null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 800, 800, TextureOptions.BILINEAR);
			cling_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(texture, context, "cling2.png");
			try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
				texture.load();
			} 
			catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		// Revert the Asset Path.
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(mPreviousAssetBasePath);
	}
	// ============================ UNLOAD TEXTURES (GAME) =============== //
	private void unloadGameTextures(){
		if (gameBackgroundTextureRegion!=null) {
			if (gameBackgroundTextureRegion.getTexture().isLoadedToHardware()) {
				gameBackgroundTextureRegion.getTexture().unload();
				gameBackgroundTextureRegion = null;
			}
		}
		if (box_region!=null) {
			if (box_region.getTexture().isLoadedToHardware()) {
				box_region.getTexture().unload();
				box_region = null;
			}
		}
		if (floor1_region!=null) {
			if (floor1_region.getTexture().isLoadedToHardware()) {
				floor1_region.getTexture().unload();
				floor1_region = null;
			}
		}
		if (floor2_region!=null) {
			if (floor2_region.getTexture().isLoadedToHardware()) {
				floor2_region.getTexture().unload();
				floor2_region = null;
			}
		}
		if (player_region!=null) {
			if (player_region.getTexture().isLoadedToHardware()) {
				player_region.getTexture().unload();
				player_region = null;
			}
		}
		if (complete_region!=null) { 
			if (complete_region.getTexture().isLoadedToHardware()) {
				complete_region.getTexture().unload();
				complete_region = null;
			}
		}
		if (hand_region!=null) { 
			if (hand_region.getTexture().isLoadedToHardware()) {
				hand_region.getTexture().unload();
				hand_region = null;
			}
		}
		if (cling_region!=null) { 
			if (cling_region.getTexture().isLoadedToHardware()) {
				cling_region.getTexture().unload();
				cling_region = null;
			}
		}
	}
		
	// ============================ LOAD TEXTURES (SHARED) ================= //
	private void loadButtonTextures() {
		// Store the current asset base path to apply it after we've loaded our textures
		mPreviousAssetBasePath = BitmapTextureAtlasTextureRegionFactory.getAssetBasePath();
		// Set our shared assets folder to "assets/gfx/"
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		// Menu button texture
		if (menubuttonTiledTextureRegion==null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 355, 85);
			menubuttonTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texture, context, "menu.png", 2, 1);
			try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 4));
				texture.load();
			} catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		// Pause button texture
		if (pausebuttonTiledTextureRegion==null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 355, 85);
			pausebuttonTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texture, context, "pause.png",2, 1);
			try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 4));
				texture.load();
			} catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		// Resume button texture
		if (resumebuttonTiledTextureRegion==null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 355, 85);
			resumebuttonTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texture, context, "resume.png", 2, 1);
			try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 4));
				texture.load();
			} catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}
		// Restart button texture
		if (restartbuttonTiledTextureRegion==null) {
			BuildableBitmapTextureAtlas texture = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 355, 85);
			restartbuttonTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texture, context, "restart.png", 2, 1);
			try {
				texture.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 4));
				texture.load();
			} catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		}

		// Revert the Asset Path
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(mPreviousAssetBasePath);
	}
	// ============================ UNLOAD TEXTURES (SHARED) ============= //
	private void unloadButtonTextures() {
		// button texture
		if (menubuttonTiledTextureRegion!=null) {
			if (menubuttonTiledTextureRegion.getTexture().isLoadedToHardware()) {
				menubuttonTiledTextureRegion.getTexture().unload();
				menubuttonTiledTextureRegion = null;
			}
		}
		if (pausebuttonTiledTextureRegion!=null) {
			if (pausebuttonTiledTextureRegion.getTexture().isLoadedToHardware()) {
				pausebuttonTiledTextureRegion.getTexture().unload();
				pausebuttonTiledTextureRegion = null;
			}
		}
		if (restartbuttonTiledTextureRegion!=null) { 
			if (restartbuttonTiledTextureRegion.getTexture().isLoadedToHardware()) {
				restartbuttonTiledTextureRegion.getTexture().unload();
				restartbuttonTiledTextureRegion = null;
			}
		} 
		if (resumebuttonTiledTextureRegion!=null) {
			if (resumebuttonTiledTextureRegion.getTexture().isLoadedToHardware()) {
				resumebuttonTiledTextureRegion.getTexture().unload();
				resumebuttonTiledTextureRegion = null;
			}
		}
	}
	
	// =========================== LOAD SOUNDS ======================== //
	private void loadSounds(){
		SoundFactory.setAssetBasePath("sfx/");
		if (onClickSound==null) {
			try {
				onClickSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), context, "click.mp3");
			} catch (final IOException e) {
				Log.v("Sounds Load","Exception:" + e.getMessage());
			}
		}
		if (onDieSound==null) {
			try {
				onDieSound	= SoundFactory.createSoundFromAsset(engine.getSoundManager(), context, "die.wav");
			} catch (final IOException e) {
				Log.v("Sounds Load","Exception:" + e.getMessage());
			}
		}
	
		MusicFactory.setAssetBasePath("sfx/");
		if (gameMusic==null) {
			try {
				gameMusic = MusicFactory.createMusicFromAsset(engine.getMusicManager(), context, "vanguard2.mp3");
			} catch (final IOException e) {
				Log.e("Music Load","Exception:" + e.getMessage());
			}
		}
		if (onVictorySound==null) {
			try {
				onVictorySound	= MusicFactory.createMusicFromAsset(engine.getMusicManager(), context, "victory.mp3");
			} catch (final IOException e) {
				Log.v("Sounds Load","Exception:" + e.getMessage());
			}
		}
	}
	// =========================== UNLOAD SOUNDS ====================== //
	// Unload the sounds and music and make sure to stop it first
	private void unloadSounds(){
		if (onClickSound!=null) {
			if (onClickSound.isLoaded()) {
				onClickSound.stop();
				engine.getSoundManager().remove(onClickSound);
				onClickSound = null;
			}
		}
		if (onDieSound!=null) {
			if (onDieSound.isLoaded()) {
				onDieSound.stop();
				engine.getSoundManager().remove(onDieSound);
				onDieSound = null;
			}
		}
		if (onVictorySound!=null) {
			onVictorySound.stop();
			engine.getMusicManager().remove(onVictorySound);
			onVictorySound = null;
		}
		if (gameMusic!=null) {
			gameMusic.stop();
			engine.getMusicManager().remove(gameMusic);
			gameMusic = null;
		}
	}

	// ============================ LOAD FONTS ========================== //
	private void loadFonts(){
		if (fontDefault32Bold==null) {
			fontDefault32Bold = FontFactory.create(engine.getFontManager(), engine.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD),  32f, true, Color.WHITE_ARGB_PACKED_INT);
			fontDefault32Bold.load();
		}
		if (fontDefault72Bold==null) {
			fontDefault72Bold = FontFactory.create(engine.getFontManager(), engine.getTextureManager(), 512, 512, Typeface.create(Typeface.DEFAULT, Typeface.BOLD),  72f, true, Color.WHITE_ARGB_PACKED_INT);
			fontDefault72Bold.load();
		}
		FontFactory.setAssetBasePath("fonts/");
		if (fontSlimJoe==null) {
			fontSlimJoe = FontFactory.createFromAsset(engine.getFontManager(), engine.getTextureManager(), 256, 256, TextureOptions.BILINEAR, context.getAssets(), "slimjoe.otf", 32f, true, Color.WHITE_ARGB_PACKED_INT);
			fontSlimJoe.load(); 
		}
		if (fontBigJohn==null) {
			fontBigJohn = FontFactory.createFromAsset(engine.getFontManager(), engine.getTextureManager(), 512, 512, TextureOptions.BILINEAR, context.getAssets(), "bigjohn.otf", 52f, true, Color.WHITE_ARGB_PACKED_INT);
			fontBigJohn.load(); 
		}
	}
	// ============================ UNLOAD FONTS ======================== //
	private void unloadFonts(){
		if (fontDefault32Bold!=null) {
			fontDefault32Bold.unload();
			fontDefault32Bold = null;
		}
		if (fontDefault72Bold!=null) {
			fontDefault72Bold.unload();
			fontDefault72Bold = null;
		}
		if (fontSlimJoe!=null) {
			fontSlimJoe.unload();
			fontSlimJoe = null;
		}
		if (fontBigJohn!=null) {
			fontBigJohn.unload();
			fontBigJohn = null;
		}
	}
}