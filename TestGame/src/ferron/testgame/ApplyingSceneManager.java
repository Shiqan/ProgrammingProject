package ferron.testgame;

import org.andengine.engine.Engine;
import org.andengine.engine.FixedStepEngine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.util.FPSLogger;
import org.andengine.ui.activity.BaseGameActivity;

import android.view.KeyEvent;

public class ApplyingSceneManager extends BaseGameActivity {

	// ====================================================
	// CONSTANTS
	// ====================================================
	// We define these constants to setup the game to use an
	// appropriate camera resolution independent of the actual
	// end-user's screen resolution.
	
	// The resolution of the screen with which you are developing.
	static float DESIGN_SCREEN_WIDTH_PIXELS = 800f;
	static float DESIGN_SCREEN_HEIGHT_PIXELS = 480f;
	// The physical size of the screen with which you are developing.
	static float DESIGN_SCREEN_WIDTH_INCHES = 4.472441f;
	static float DESIGN_SCREEN_HEIGHT_INCHES = 2.805118f;
	
	// Define a minimum and maximum screen resolution (to prevent
	// cramped or overlapping screen elements).
	static float MIN_WIDTH_PIXELS = 320f;
	static float MIN_HEIGHT_PIXELS = 240f;
	static float MAX_WIDTH_PIXELS = 1600f;
	static float MAX_HEIGHT_PIXELS = 960f;
	
	// ====================================================
	// VARIABLES
	// ====================================================
	// These variables will be set in onCreateEngineOptions().
	public static BoundCamera mCamera;
	public float mCameraWidth;
	public float mCameraHeight;
	public float mActualScreenWidthInches;
	public float mActualScreenHeightInches;
	
	// If a Layer is open when the Back button is pressed, hide the layer.
	// If a Game scene or non-MainMenu is active, go back to the MainMenu.
	// Otherwise, exit the game.
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if(ResourceManager.getInstance().engine!=null) {
				if(SceneManager.getInstance().isLayerShown) {
					SceneManager.getInstance().currentLayer.onHideLayer();
				}
				else if (SceneManager.getInstance().mCurrentScene.getClass().equals(GameScene.class) 
						|| SceneManager.getInstance().mCurrentScene.getClass().equals(OptionsScene.class)
						|| SceneManager.getInstance().mCurrentScene.getClass().equals(StatsScene.class)
						) {
					SceneManager.getInstance().showMainMenu();
				}
				else {
					System.exit(0);
				}
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	@Override
	public Engine onCreateEngine(final EngineOptions pEngineOptions) {
		return new FixedStepEngine(pEngineOptions, 60);
	}
	
	// ====================================================
	// CREATE ENGINE OPTIONS
	// ====================================================
	@Override
	public EngineOptions onCreateEngineOptions() {
		// Determine the device's physical screen size.
		mActualScreenWidthInches = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().xdpi;
		mActualScreenHeightInches = getResources().getDisplayMetrics().heightPixels / getResources().getDisplayMetrics().ydpi;
		// Set the Camera's Width & Height according to the device with which you design the game.
		mCameraWidth = Math.round(Math.max(Math.min(DESIGN_SCREEN_WIDTH_PIXELS * (mActualScreenWidthInches / DESIGN_SCREEN_WIDTH_INCHES),MAX_WIDTH_PIXELS),MIN_WIDTH_PIXELS));
		mCameraHeight = Math.round(Math.max(Math.min(DESIGN_SCREEN_HEIGHT_PIXELS * (mActualScreenHeightInches / DESIGN_SCREEN_HEIGHT_INCHES),MAX_HEIGHT_PIXELS),MIN_HEIGHT_PIXELS));
		
		mCamera = new BoundCamera(0, 0, mCameraWidth, mCameraHeight);
		
		// Create the EngineOptions.
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new FillResolutionPolicy(), mCamera);
		// Enable sounds.
		engineOptions.getAudioOptions().setNeedsSound(true);
		// Enable music.
		engineOptions.getAudioOptions().setNeedsMusic(true);
		// Turn on Dithering to smooth texture gradients.
		engineOptions.getRenderOptions().setDithering(true);
		// Turn on MultiSampling to smooth the alias of hard-edge elements.
		engineOptions.getRenderOptions().getConfigChooserOptions().setRequestedMultiSampling(true);
		// Set the Wake Lock options to prevent the engine from dumping textures when focus changes.
		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
				
		return engineOptions;
	}
	
	// ====================================================
	// CREATE RESOURCES
	// ====================================================
	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) {
		// Setup the ResourceManager.
		ResourceManager.getInstance().setup(this.getEngine(), this.getApplicationContext(), mCameraWidth, mCameraHeight, mCameraWidth/DESIGN_SCREEN_WIDTH_PIXELS, mCameraHeight/DESIGN_SCREEN_HEIGHT_PIXELS);
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	// ====================================================
	// CREATE SCENE
	// ====================================================
	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) {
		// Register an FPSLogger to output the game's FPS during development.
		mEngine.registerUpdateHandler(new FPSLogger());
		// Tell the SceneManager to show the MainMenu.
		SceneManager.getInstance().showMainMenu();
		// Set the MainMenu to the Engine's scene.
		pOnCreateSceneCallback.onCreateSceneFinished(MenuScene.getInstance());
	}

	// ====================================================
	// POPULATE SCENE
	// ====================================================
	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) {
		// Our SceneManager will handle the population of the scenes, so we do nothing here.
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
}