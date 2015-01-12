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
import org.andengine.entity.scene.background.Background;
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
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
	
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3 = "platform3";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR1 = "floor1";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR2 = "floor2";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BOX = "box";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_ALPHA_BOX = "alpha";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE = "levelComplete";

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
		
		mCamera = new BoundCamera(0, 0, mCameraWidth, mCameraHeight);
		
		// Create the EngineOptions
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
	protected void onCreateResources()  {
		// Setup the ResourceManager
		ResourceManager.getInstance().setup(this.getEngine(), this.getApplicationContext(), mCameraWidth, mCameraHeight, mCameraWidth/DESIGN_SCREEN_WIDTH_PIXELS, mCameraHeight/DESIGN_SCREEN_HEIGHT_PIXELS);
	}


	// ====================================================
	// CREATE SCENE
	// ====================================================
	@Override
	protected Scene onCreateScene() {
		// Register an FPSLogger to output the game's FPS during development
		mEngine.registerUpdateHandler(new FPSLogger());

		ResourceManager.loadGameResources();
		
		this.mGameScene = new Scene();
		this.mGameScene.setOnSceneTouchListener(this);
		this.mGameScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		
		createHUD();
		createPhysics();
		loadLevel(2);
		
		mGameScene.setScale(ResourceManager.getInstance().cameraScaleFactorX, ResourceManager.getInstance().cameraScaleFactorY);
		mGameScene.setPosition(0, ResourceManager.getInstance().cameraHeight/2f);
		mGameHud.setScaleCenter(0f, 0f);
		mGameHud.setScale(ResourceManager.getInstance().cameraScaleFactorX,ResourceManager.getInstance().cameraScaleFactorY);
		
		mCamera.setHUD(mGameHud);
		return mGameScene;
	}



	// ===========================================================
	// Methods
	// ===========================================================

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.isActionDown()) {
            Log.i("TOUCH EVENT", "TOUCHED" + mPlayer.getX());        
            if (!mFirstTouch) {
            	mPlayer.setRunning();
            	mFirstTouch = true;
            	ResourceManager.gameMusic.play();
            	ResourceManager.gameMusic.setOnCompletionListener(new OnCompletionListener() {
            		@Override
            		public void onCompletion(MediaPlayer mp) {
            			Log.i("COMPLETION", mPlayer.getX()+"");
            			
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

	private void createHUD() {	
		// TODO change pause position??
		ButtonSprite pPauseButton = new ButtonSprite(0f,0f, 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pPauseButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
		pPauseButton.setPosition(800f-((pPauseButton.getWidth()*pPauseButton.getScaleX())/2f), (pPauseButton.getHeight()*pPauseButton.getScaleY())/2f);
		pPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// Play the click sound and show the Options Layer
				ResourceManager.clickSound.play();
				ResourceManager.gameMusic.pause();
				showLayer(true, 0);
		}});		
		Text pPauseButtonText = new Text(0,0,ResourceManager.fontDefault32Bold,"PAUSE",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pPauseButtonText.setPosition((pPauseButton.getWidth())/2, (pPauseButton.getHeight())/2);
		pPauseButton.attachChild(pPauseButtonText);
		mGameHud.attachChild(pPauseButton);
		mGameHud.registerTouchArea(pPauseButton);
	}
	
	private void createPhysics()
	{
		mPhysicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -28), false);	
		mPhysicsWorld.setContactListener(contactListener());
		mGameScene.registerUpdateHandler(mPhysicsWorld);
	}
	
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
				
				Sprite levelObject;
				
				if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1))
				{
					levelObject = new Sprite(x, y, ResourceManager.platform1_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					PhysicsFactory.createBoxBody(mPhysicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
				} 
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2))
				{
					levelObject = new Sprite(x, y, ResourceManager.platform2_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					final Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("platform2");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3))
				{
					levelObject = new Sprite(x, y, ResourceManager.platform3_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					final Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("platform3");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BOX)) {
					boolean alpha = SAXUtils.getBooleanAttribute(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE_ALPHA_BOX, false);
					levelObject = new Sprite(x, y, ResourceManager.testTextureRegion, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					levelObject.setScale(0.2f);
					
					if (alpha) {
						// TODO should change more gradually...
						levelObject.registerEntityModifier(new LoopEntityModifier(new AlphaModifier(2f, 1.0f, 0.0f)));
					}
					
					Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("box");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				
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
								showLayer(false, 1);
							}
						}
					};
					levelObject = mPlayer;
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR1)) {
					int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
					int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
									
					levelObject = new Sprite(x+width/2, y, ResourceManager.platform3_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					levelObject.setWidth(width);
					levelObject.setHeight(height);
					Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("floor1");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR2)) {
					int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
					int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
					
					levelObject = new Sprite(x+width/2, y, ResourceManager.platform2_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					levelObject.setWidth(width);
					levelObject.setHeight(height);
					Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("floor2");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE)) {
					levelObject = new Sprite(x, y, ResourceManager.complete_stars_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager()) {
						@Override
						protected void onManagedUpdate(float pSecondsElapsed) {
							super.onManagedUpdate(pSecondsElapsed);

							if (mPlayer.collidesWith(this)) {
								Log.i("COMPLETED", "LEVEL");
								
								this.setVisible(false);
								this.setIgnoreUpdate(true);
								
							}
						}
					};
					levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
				}				
				else {
					throw new IllegalArgumentException();
				}

				levelObject.setCullingEnabled(true);

				return levelObject;
			}
		});
		
		levelLoader.loadLevelFromAsset(this.getAssets(), "level/" + levelID + ".lvl");
	}
	
	private ContactListener contactListener() {
		ContactListener contactListener = new ContactListener() {
			public void beginContact(Contact contact) {
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();
				
				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null) {
					
					if (x1.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("player")) {
						mPlayer.setfloorContact();
					}
					
					if ((x1.getBody().getUserData().equals("platform2") && x2.getBody().getUserData().equals("player"))
							|| (x1.getBody().getUserData().equals("player") && x2.getBody().getUserData().equals("platform2")) ) {
						ResourceManager.getInstance().engine.registerUpdateHandler(new TimerHandler(0.2f, new ITimerCallback() 	{									
						    public void onTimePassed(final TimerHandler pTimerHandler) {
						    	pTimerHandler.reset();
						    	ResourceManager.getInstance().engine.unregisterUpdateHandler(pTimerHandler);
						    	x1.getBody().setType(BodyType.DynamicBody);
						    }
						}));
					}
					
					if ((x1.getBody().getUserData().equals("platform3") && x2.getBody().getUserData().equals("player")) 
							|| (x1.getBody().getUserData().equals("player") && x2.getBody().getUserData().equals("platform3"))) {
						x1.getBody().setType(BodyType.DynamicBody);
					}
					
					if ((x1.getBody().getUserData().equals("floor1") && x2.getBody().getUserData().equals("player")) 
							|| (x1.getBody().getUserData().equals("player") && x2.getBody().getUserData().equals("floor1"))) {
						//Log.i("TOUCHED", "floor1");
					}
					if ((x1.getBody().getUserData().equals("floor2") && x2.getBody().getUserData().equals("player")) 
							|| (x1.getBody().getUserData().equals("player") && x2.getBody().getUserData().equals("floor2"))) {
						mPlayer.onDie();
					}
					
					// if side is touched
					// box height = 25.6
					if (x1.getBody().getUserData().equals("box") && x2.getBody().getUserData().equals("player")) {
						if (x2.getBody().getPosition().x < x1.getBody().getPosition().x && x2.getBody().getPosition().y < x1.getBody().getPosition().y) {
							mPlayer.onDie();
						}
					}
					
					if (x1.getBody().getUserData().equals("player") && x2.getBody().getUserData().equals("box")) {
						if (x1.getBody().getPosition().x < x2.getBody().getPosition().x && x1.getBody().getPosition().y < x2.getBody().getPosition().y) {
							mPlayer.onDie();
						}
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
	private void showLayer(final boolean pSuspendSceneUpdates, int pLayerType) {
		ResourceManager.gameMusic.pause();
		
		if (pSuspendSceneUpdates) {
			mGameScene.setIgnoreUpdate(true);
		}
		else {
			ResourceManager.getInstance().engine.registerUpdateHandler(new TimerHandler(5f, new ITimerCallback() 	{									
			    public void onTimePassed(final TimerHandler pTimerHandler) {
			    	mGameScene.setIgnoreUpdate(true);
			    }
			}));
		}
		
		String pLayerTitleString = null;
		boolean pCreateResumeButton = false;
		layer = new HUD();
		
	    switch (pLayerType) {
		    // Pause
		    case 0:
		    	pLayerTitleString = "PAUSED";
		    	pCreateResumeButton = true;
		    	break;
		    
		    // Game over
		    case 1:
		    	pLayerTitleString = "GAME OVER!";
		    	break;
		    	
		    // Game completed
		    case 2:
		    	pLayerTitleString = "COMPLETED!";
		    	break;
	        
	    }	
			
		// Create a transparent background		
		Rectangle pBackground = new Rectangle(mCameraWidth/2,mCameraHeight/2,mCameraWidth,mCameraHeight,ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pBackground.setColor(0f, 0f, 0f, 0.85f);
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
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pMainMenuButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
		pMainMenuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// Play the click sound and show the Main Menu.
				ResourceManager.clickSound.play();
				// TODO start MainActivity and unload Resources
			}});
		Text pMainMenuButtonText = new Text(pMainMenuButton.getWidth()/2,pMainMenuButton.getHeight()/2,ResourceManager.fontDefault32Bold,"MENU",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pMainMenuButton.attachChild(pMainMenuButtonText);
		layer.attachChild(pMainMenuButton);
		layer.registerTouchArea(pMainMenuButton);
		
		// Create a restart button
		ButtonSprite pRestartButton = new ButtonSprite(
				(2f*(pBackground.getWidth()/3f))+pScoreText.getWidth(),
				pBackground.getHeight()-(5f*pScoreText.getHeight()), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pRestartButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
		pRestartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// Play the click sound and show the Main Menu.
				ResourceManager.clickSound.play();
				ResourceManager.unloadGameResources();
				ResourceManager.unloadSharedResources();
				Intent intent = new Intent(ResourceManager.getInstance().context, GameActivity.class);
			    startActivity(intent);
			    finish();
			}});
		Text pRestartButtonText = new Text(pRestartButton.getWidth()/2,pRestartButton.getHeight()/2,ResourceManager.fontDefault32Bold,"RESTART",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pRestartButton.attachChild(pRestartButtonText);
		layer.attachChild(pRestartButton);
		layer.registerTouchArea(pRestartButton);
		
		// Create a resume button
		if (pCreateResumeButton) {
			ButtonSprite pResumeButton = new ButtonSprite(
					(2f*(pBackground.getWidth()/3f))+pScoreText.getWidth(),
					pBackground.getHeight()-(7f*pScoreText.getHeight()), 
					ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
					ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
					ResourceManager.getInstance().engine.getVertexBufferObjectManager());
			pResumeButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
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
			Text pResumeButtonText = new Text(pResumeButton.getWidth()/2,pResumeButton.getHeight()/2,ResourceManager.fontDefault32Bold,"RESUME",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
			pResumeButton.attachChild(pResumeButtonText);
			layer.attachChild(pResumeButton);
			layer.registerTouchArea(pResumeButton);
		}
	
		layer.registerUpdateHandler(SlideIn);
		mCamera.setHUD(layer);
	}
	
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
	public void onResumeGame() {
		
		super.onResumeGame();
	}
	
	@Override
	public void onPauseGame() {
		// If music is playing, pause the music and the game
		if (ResourceManager.gameMusic != null && ResourceManager.gameMusic.isPlaying()) {
			ResourceManager.gameMusic.pause();
			showLayer(true, 0);
		}
		super.onResumeGame();
	}



}
