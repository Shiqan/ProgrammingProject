package ferron.testgame;

import java.io.IOException;

import org.andengine.engine.Engine;
import org.andengine.engine.FixedStepEngine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.particle.BatchedSpriteParticleSystem;
import org.andengine.entity.particle.emitter.CircleParticleEmitter;
import org.andengine.entity.particle.initializer.AccelerationParticleInitializer;
import org.andengine.entity.particle.initializer.ColorParticleInitializer;
import org.andengine.entity.particle.modifier.ScaleParticleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.UncoloredSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.SAXUtils;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.view.KeyEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

public class GameActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener {

	// ====================================================
	// SCREEN RESOLUTION
	// ====================================================

	// The resolution of the screen with which you are developing
	static float DESIGN_SCREEN_WIDTH_PIXELS = 800f;
	static float DESIGN_SCREEN_HEIGHT_PIXELS = 480f;
	static float DESIGN_SCREEN_WIDTH_INCHES = 4.472441f;
	static float DESIGN_SCREEN_HEIGHT_INCHES = 2.805118f;
	
	// Minimum and maximum screen resolution
	static float MIN_WIDTH_PIXELS = 320f;
	static float MIN_HEIGHT_PIXELS = 240f;
	static float MAX_WIDTH_PIXELS = 1600f;
	static float MAX_HEIGHT_PIXELS = 960f;
	
	// ====================================================
	// VARIABLES
	// ====================================================
	// These variables will be set in onCreateEngineOptions()
	public static BoundCamera mCamera;
	public float mCameraWidth;
	public float mCameraHeight;
	public float mActualScreenWidthInches;
	public float mActualScreenHeightInches;
	
	// Game Scene variables
	private Scene mGameScene;
	private HUD mGameHud = new HUD();
	private HUD layer = new HUD();
	public static Player mPlayer;
	private PhysicsWorld mPhysicsWorld;
	private boolean mFirstTouch = false;
	public static boolean mContinuousTouch = false;
	private boolean mDied = false;
	
	// LevelLoader tags
	private final String TAG_ENTITY = "entity";
	private final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
	
	private final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR1 = "floor1";
	private final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR2 = "floor2";
	private final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BOX = "box";
	private final String TAG_ENTITY_ATTRIBUTE_TYPE_ALPHA_BOX = "alpha";
	private final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
	private final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE = "levelComplete";

	// ====================================================
	// CREATE ENGINE 
	// ====================================================
	@Override
	public Engine onCreateEngine(final EngineOptions pEngineOptions) {
		return new FixedStepEngine(pEngineOptions, 60);
	}
		
	// ====================================================
	// CREATE ENGINE OPTIONS
	// ====================================================
	@Override
	public EngineOptions onCreateEngineOptions() {
		// Determine the device's physical screen size
		mActualScreenWidthInches = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().xdpi;
		mActualScreenHeightInches = getResources().getDisplayMetrics().heightPixels / getResources().getDisplayMetrics().ydpi;
		// Set the Camera's Width & Height according to the device with which you design the game
		mCameraWidth = Math.round(Math.max(Math.min(DESIGN_SCREEN_WIDTH_PIXELS * (mActualScreenWidthInches / DESIGN_SCREEN_WIDTH_INCHES),MAX_WIDTH_PIXELS),MIN_WIDTH_PIXELS));
		mCameraHeight = Math.round(Math.max(Math.min(DESIGN_SCREEN_HEIGHT_PIXELS * (mActualScreenHeightInches / DESIGN_SCREEN_HEIGHT_INCHES),MAX_HEIGHT_PIXELS),MIN_HEIGHT_PIXELS));
		
		// Set Bounds of the camera to the screen size
		mCamera = new BoundCamera(0, 0, mCameraWidth, mCameraHeight);
		
		// Create the EngineOptions
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new FillResolutionPolicy(), mCamera);
		// Enable sounds
		engineOptions.getAudioOptions().setNeedsSound(true);
		// Enable music
		engineOptions.getAudioOptions().setNeedsMusic(true);
		// Turn on Dithering to smooth texture gradients
		engineOptions.getRenderOptions().setDithering(true);
		// Turn on MultiSampling to smooth the alias of hard-edge elements
		engineOptions.getRenderOptions().getConfigChooserOptions().setRequestedMultiSampling(true);
		// Set the Wake Lock options to prevent the engine from dumping textures when focus changes
		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
				
		return engineOptions;
	}

	// ====================================================
	// CREATE RESOURCES
	// ====================================================
	@Override
	protected void onCreateResources()  {
		// Setup the ResourceManager
		ResourceManager.getInstance().setup(this.getEngine(), GameActivity.this, mCameraWidth, mCameraHeight, mCameraWidth/DESIGN_SCREEN_WIDTH_PIXELS, mCameraHeight/DESIGN_SCREEN_HEIGHT_PIXELS);
	}


	// ====================================================
	// CREATE SCENE
	// ====================================================
	@Override
	protected Scene onCreateScene() {
		// Register an FPSLogger to output the game's FPS during development
		mEngine.registerUpdateHandler(new FPSLogger());

		ResourceManager.loadGameResources();
		
		// Create scene
		this.mGameScene = new Scene();
		this.mGameScene.setOnSceneTouchListener(this);
		//this.mGameScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		this.mGameScene.setBackground(new SpriteBackground(new Sprite(mCameraWidth/2,mCameraHeight/2,mCameraWidth, mCameraHeight, ResourceManager.gameBackgroundTextureRegion, ResourceManager.getInstance().engine.getVertexBufferObjectManager())));
		
		// Create HUD, PhysicsWorld and load the level
		createHUD();
		createPhysics();
		loadLevel(2);
		
		// Set the HUD to the camera
		mCamera.setHUD(mGameHud);
		
		// Scale the scene and the HUD to the device size
		mGameScene.setScale(ResourceManager.getInstance().cameraScaleFactorX, ResourceManager.getInstance().cameraScaleFactorY);
		mGameScene.setPosition(0, ResourceManager.getInstance().cameraHeight/2f);
		mGameHud.setScaleCenter(0f, 0f);
		mGameHud.setScale(ResourceManager.getInstance().cameraScaleFactorX,ResourceManager.getInstance().cameraScaleFactorY);

		return mGameScene;
	}
	
	// ====================================================
	// METHODS
	// ====================================================
	/** 
	 * Handle the backbutton
	 */
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if(ResourceManager.getInstance().engine!=null) {
				if(mCamera.getHUD().equals(layer) && !mDied) {
					layer.registerUpdateHandler(SlideOut);
					mCamera.setHUD(mGameHud);
					mGameScene.setIgnoreUpdate(false);
					ResourceManager.gameMusic.play();
				}
				else {
					ResourceManager.unloadGameResources();
					Intent intent = new Intent(ResourceManager.getInstance().context, MainActivity.class);
				    startActivity(intent);
				    finish();
				}
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	/**
	 * Handle the touch events
	 */
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.isActionDown()) {
            Log.i("TOUCH EVENT", "TOUCHED" + mPlayer.getX());        
            
            // Start the game on the first touch
            if (!mFirstTouch) {
            	mPlayer.setRunning();
            	mFirstTouch = true;
            	ResourceManager.gameMusic.play();
            	ResourceManager.gameMusic.setOnCompletionListener(new OnCompletionListener() {
            		@Override
            		public void onCompletion(MediaPlayer mp) {
            			Log.i("COMPLETION", mPlayer.getX()+"");
            			// TODO play victory music
            		}
            	});
			} else {
            	mContinuousTouch = true;
				mPlayer.jump();
			}
        } else if (pSceneTouchEvent.isActionUp()) {
        	mContinuousTouch = false;
        }
        return true;
	}

	/**
	 * Create the basic game HUD
	 */
	private void createHUD() {	
		// Create Pause button
		ButtonSprite pPauseButton = new ButtonSprite(0f,0f, 
				ResourceManager.pausebuttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.pausebuttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		//pPauseButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
		pPauseButton.setPosition(DESIGN_SCREEN_WIDTH_PIXELS-((pPauseButton.getWidth()*pPauseButton.getScaleX())/2f), DESIGN_SCREEN_HEIGHT_PIXELS-(pPauseButton.getHeight()*pPauseButton.getScaleY())/2f);
		pPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// Play the click sound and show the Layer
				ResourceManager.clickSound.play();
				ResourceManager.gameMusic.pause();
				showLayer(0);
		}});		
		//Text pPauseButtonText = new Text(0,0,ResourceManager.fontDefault32Bold,"PAUSE",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		//pPauseButtonText.setPosition((pPauseButton.getWidth())/2, (pPauseButton.getHeight())/2);
		//pPauseButton.attachChild(pPauseButtonText);
		mGameHud.attachChild(pPauseButton);
		mGameHud.registerTouchArea(pPauseButton);
	}
	
	/**
	 * Create the physicsworld with 60 steps per second and a gravity of -28
	 */
	private void createPhysics()
	{
		mPhysicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -28), false);	
		mPhysicsWorld.setContactListener(contactListener());
		mGameScene.registerUpdateHandler(mPhysicsWorld);
	}
	
	/**
	 * Load a xml file and register the entities to the mGameScene
	 * @param levelID name of lvl file
	 */
	private void loadLevel(int levelID) {
		final SimpleLevelLoader levelLoader = new SimpleLevelLoader(ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		
		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
		
		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
		{
			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
			{
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
				
				// Set camera bounds
				mCamera.setBounds(0, 0, width, height); 
				mCamera.setBoundsEnabled(true);
				
				return mGameScene;
			}
		});
		
		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY) {
			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException {	
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
				final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
				
				Sprite plevelObject;
				
				if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BOX)) {
					boolean alpha = SAXUtils.getBooleanAttribute(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE_ALPHA_BOX, false);
					plevelObject = new Sprite(x, y, ResourceManager.box_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					plevelObject.setScale(0.2f);
					
					if (alpha) {
						plevelObject.registerEntityModifier(new LoopEntityModifier(new AlphaModifier(2f, 1.0f, 0.0f)));
					}
					
					Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, plevelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("box");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(plevelObject, body, true, false));
				
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER)) {
					mPlayer = new Player(x, y, ResourceManager.getInstance().engine.getVertexBufferObjectManager(), mCamera, mPhysicsWorld) {
						@Override
						public void onDie()	{
							if (!mDied) {
								mCamera.setChaseEntity(null);
								ResourceManager.gameMusic.pause();							
								explode((int) mPlayer.getX(), (int) mPlayer.getY());
														
								mPlayer.setVisible(false);
								mDied = true;
								showLayer(1);
							}
						}
					};
					plevelObject = mPlayer;
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR1)) {
					int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
					int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
									
					plevelObject = new Sprite(x+width/2, y, ResourceManager.floor1_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					plevelObject.setWidth(width);
					plevelObject.setHeight(height);
					Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, plevelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("floor1");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(plevelObject, body, true, false));
				
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR2)) {
					int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
					int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
					
					plevelObject = new Sprite(x+width/2, y, ResourceManager.floor2_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					plevelObject.setWidth(width);
					plevelObject.setHeight(height);
					Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, plevelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("floor2");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(plevelObject, body, true, false));
				
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE)) {
					plevelObject = new Sprite(x, y, ResourceManager.complete_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					plevelObject.setScale(0.2f);				
					Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, plevelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("levelComplete");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(plevelObject, body, true, false));
				}				
				else {
					throw new IllegalArgumentException();
				}

				plevelObject.setCullingEnabled(true);

				return plevelObject;
			}
		});
		
		levelLoader.loadLevelFromAsset(this.getAssets(), "level/" + levelID + ".lvl");
	}
	
	/**
	 * Create ContactListener to handle collision between two entities
	 * @return ContactListener
	 */
	private ContactListener contactListener() {
		ContactListener contactListener = new ContactListener() {
			public void beginContact(Contact contact) {
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();
				
				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null) {
					
					if (x1.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("player")) {
						mPlayer.setfloorContact();
					}
					
					// FLOOR 1
					if ((x1.getBody().getUserData().equals("floor1") && x2.getBody().getUserData().equals("player")) 
							|| (x1.getBody().getUserData().equals("player") && x2.getBody().getUserData().equals("floor1"))) {
						//Log.i("TOUCHED", "floor1");
					}
					
					// FLOOR 2
					if ((x1.getBody().getUserData().equals("floor2") && x2.getBody().getUserData().equals("player")) 
							|| (x1.getBody().getUserData().equals("player") && x2.getBody().getUserData().equals("floor2"))) {
						mPlayer.onDie();
					}
					
					// BOX				
					// TODO improve collision detection
					// box height = 25.6
					if (x1.getBody().getUserData().equals("box") && x2.getBody().getUserData().equals("player")) {
						Log.i("CONTACT EVENT", "BOXTOUCHED" + mPlayer.getX());        
						if (x2.getBody().getPosition().x < x1.getBody().getPosition().x && x2.getBody().getPosition().y < x1.getBody().getPosition().y) {
							mPlayer.onDie();
						}
					}
					
					if (x1.getBody().getUserData().equals("player") && x2.getBody().getUserData().equals("box")) {
						Log.i("CONTACT EVENT", "BOXTOUCHED" + mPlayer.getX());        
						if (x1.getBody().getPosition().x < x2.getBody().getPosition().x && x1.getBody().getPosition().y < x2.getBody().getPosition().y) {
							mPlayer.onDie();
						}
					}
					
					// LEVELCOMPLETE
					if ((x1.getBody().getUserData().equals("levelComplete") && x2.getBody().getUserData().equals("player")) 
							|| (x1.getBody().getUserData().equals("player") && x2.getBody().getUserData().equals("levelComplete"))) {
						Log.i("CONTACT EVENT", "levelComplete" + mPlayer.getX());
						mContinuousTouch = true;
						float alpha = mPlayer.getAlpha(); 
						if (alpha <= 0.0) {
							showLayer(2);
						}
						mPlayer.setAlpha(mPlayer.getAlpha()-0.1f);
						
					}
				}
			}

			public void endContact(Contact contact) {
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();

				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null) {
					if (x1.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("player")) {
						mPlayer.unsetfloorContact();
					}
				}
			}

			public void preSolve(Contact contact, Manifold oldManifold) {

			}

			public void postSolve(Contact contact, ContactImpulse impulse) {

			}
		};
		return contactListener;
	}
	
	/**
	 * Create a particleSystem to simulate explosion
	 * @param particleSpawnCenterX
	 * @param particleSpawnCenterY
	 */
	private void explode(int particleSpawnCenterX, int particleSpawnCenterY) {
		// Create Particle emitter
		int particleSpawnRadius = 10;
		CircleParticleEmitter particleEmitter = new CircleParticleEmitter(particleSpawnCenterX, particleSpawnCenterY, particleSpawnRadius);
		
		// Define particle system properties
		float minSpawnRate = 75;
		float maxSpawnRate = 75;
		int maxParticleCount = 75;
		
		// Create Particle system
		BatchedSpriteParticleSystem particleSystem = new BatchedSpriteParticleSystem(particleEmitter, minSpawnRate, maxSpawnRate, maxParticleCount, ResourceManager.getInstance().player_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		
		// Add particle modifiers
		particleSystem.addParticleInitializer(new AccelerationParticleInitializer<UncoloredSprite>(-500f, 500f, -500f, 500f));
		particleSystem.addParticleInitializer(new ColorParticleInitializer<UncoloredSprite>(0f,1f,0f,1f,0f,1f));
		particleSystem.addParticleModifier(new ScaleParticleModifier<UncoloredSprite>(0f,1.5f,0.2f,1f));
		
		mGameScene.attachChild(particleSystem);
	}
	
	// ===========================================================
	// LAYER
	// ===========================================================
	/**
	 * Create HUD to when game is paused, ended or completed
	 * @param pLayerType 0 (paused), 1 (game over), 2 (completed)
	 */
	private void showLayer(int pLayerType) {
		ResourceManager.gameMusic.pause();
		
		String pLayerTitleString = null;
		boolean pCreateResumeButton = false;
		layer = new HUD();
		
	    switch (pLayerType) {
		    // Pause
		    case 0:
		    	pLayerTitleString = "PAUSED";
		    	mGameScene.setIgnoreUpdate(true);
		    	pCreateResumeButton = true;
		    	break;
		    
		    // Game over
		    case 1:
		    	pLayerTitleString = "GAME OVER!";
		    	ResourceManager.getInstance().engine.registerUpdateHandler(new TimerHandler(5f, new ITimerCallback() 	{									
				    public void onTimePassed(final TimerHandler pTimerHandler) {
				    	mGameScene.setIgnoreUpdate(true);
				    }
				}));
		    	break;
		    	
		    // Game completed
		    case 2:
		    	pLayerTitleString = "COMPLETED!";
		    	mGameScene.setIgnoreUpdate(true);
		    	break;
	    }	
			
		// Create a transparent background		
		Rectangle pBackground = new Rectangle(mCameraWidth/2,mCameraHeight/2,mCameraWidth,mCameraHeight,ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pBackground.setColor(0f, 0f, 0f, 0.55f);
		layer.attachChild(pBackground);
		
		// Create the title text for the Layer
		Text pLayerTitle = new Text(0,0,ResourceManager.fontDefault32Bold, pLayerTitleString, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pLayerTitle.setPosition((pBackground.getWidth()/2f),(pBackground.getHeight()-pLayerTitle.getHeight()));
		layer.attachChild(pLayerTitle);
		
		// Create the score text for the Layer
		Text pScoreText = new Text(0,0,ResourceManager.fontDefault32Bold, "SCORE" ,ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pScoreText.setPosition((pBackground.getWidth()/3f)-pScoreText.getWidth(),pBackground.getHeight()-(3f*pScoreText.getHeight()));
		layer.attachChild(pScoreText);
		
		// Create a menu button
		ButtonSprite pMainMenuButton = new ButtonSprite(
				(2f*(pBackground.getWidth()/3f))+pScoreText.getWidth(),
				pBackground.getHeight()-(3f*pScoreText.getHeight()), 
				ResourceManager.menubuttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.menubuttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		//pMainMenuButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
		pMainMenuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// Play the click sound and show the Main Menu.
				ResourceManager.clickSound.play();
				ResourceManager.unloadGameResources();
				Intent intent = new Intent(ResourceManager.getInstance().context, MainActivity.class);
			    startActivity(intent);
			    finish();
			}});
		//Text pMainMenuButtonText = new Text(pMainMenuButton.getWidth()/2,pMainMenuButton.getHeight()/2,ResourceManager.fontDefault32Bold,"MENU",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		//pMainMenuButton.attachChild(pMainMenuButtonText);
		layer.attachChild(pMainMenuButton);
		layer.registerTouchArea(pMainMenuButton);
		
		// Create a restart button
		ButtonSprite pRestartButton = new ButtonSprite(
				(2f*(pBackground.getWidth()/3f))+pScoreText.getWidth(),
				pBackground.getHeight()-(5f*pScoreText.getHeight()), 
				ResourceManager.restartbuttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.restartbuttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		//pRestartButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
		pRestartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// Play the click sound and show the Main Menu.
				ResourceManager.clickSound.play();
				ResourceManager.unloadGameResources();
				Intent intent = new Intent(ResourceManager.getInstance().context, GameActivity.class);
			    startActivity(intent);
			    finish();
			}});
		//Text pRestartButtonText = new Text(pRestartButton.getWidth()/2,pRestartButton.getHeight()/2,ResourceManager.fontDefault32Bold,"RESTART",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		//pRestartButton.attachChild(pRestartButtonText);
		layer.attachChild(pRestartButton);
		layer.registerTouchArea(pRestartButton);
		
		// Create a resume button
		if (pCreateResumeButton) {
			ButtonSprite pResumeButton = new ButtonSprite(
					(2f*(pBackground.getWidth()/3f))+pScoreText.getWidth(),
					pBackground.getHeight()-(7f*pScoreText.getHeight()), 
					ResourceManager.resumebuttonTiledTextureRegion.getTextureRegion(0), 
					ResourceManager.resumebuttonTiledTextureRegion.getTextureRegion(1), 
					ResourceManager.getInstance().engine.getVertexBufferObjectManager());
			//pResumeButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
			pResumeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(ButtonSprite pButtonSprite,
						float pTouchAreaLocalX, float pTouchAreaLocalY) {
					// Play the click sound and show the Main Menu.
					ResourceManager.clickSound.play();
					layer.registerUpdateHandler(SlideOut);
					mCamera.setHUD(mGameHud);
					mGameScene.setIgnoreUpdate(false);
					ResourceManager.gameMusic.play();
				}});
			//Text pResumeButtonText = new Text(pResumeButton.getWidth()/2,pResumeButton.getHeight()/2,ResourceManager.fontDefault32Bold,"RESUME",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
			//pResumeButton.attachChild(pResumeButtonText);
			layer.attachChild(pResumeButton);
			layer.registerTouchArea(pResumeButton);
		}
	
		layer.registerUpdateHandler(SlideIn);
		mCamera.setHUD(layer);
	}
	
	// TODO animate layer
	// Animates the layer to slide in from the top
	IUpdateHandler SlideIn = new IUpdateHandler() {
		@Override
		public void onUpdate(float pSecondsElapsed) {
			if(layer.getY()>ResourceManager.getInstance().cameraHeight/2f) {
				layer.setPosition(layer.getX(), Math.max(layer.getY()-(3600*(pSecondsElapsed)),ResourceManager.getInstance().cameraHeight/2f));
			} else {
				layer.unregisterUpdateHandler(this);
			}
		}
		
		@Override 
		public void reset() {
			
		}
	};
	
	// Animates the layer to slide out
	IUpdateHandler SlideOut = new IUpdateHandler() {
		@Override
		public void onUpdate(float pSecondsElapsed) {
			if(layer.getY()<ResourceManager.getInstance().cameraHeight/2f+480f) {
				layer.setPosition(layer.getX(), Math.min(layer.getY()+(3600*(pSecondsElapsed)),ResourceManager.getInstance().cameraHeight/2f+480f));
			} else {
				layer.unregisterUpdateHandler(this);
			}
		}
		@Override public void reset() {
			
		}
	};
	
	// ===========================================================
	// LIFE CYCLE 
	// ===========================================================
	
	@Override
	public void onPauseGame() {
		// If music is playing, pause the music and the game
		if (ResourceManager.gameMusic != null && ResourceManager.gameMusic.isPlaying()) {
			ResourceManager.gameMusic.pause();
			showLayer(0);
		}
		super.onPauseGame();
	}



}