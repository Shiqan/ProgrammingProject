package ferron.testgame;

import java.io.IOException;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.particle.BatchedSpriteParticleSystem;
import org.andengine.entity.particle.emitter.CircleParticleEmitter;
import org.andengine.entity.particle.initializer.AccelerationParticleInitializer;
import org.andengine.entity.particle.initializer.ColorParticleInitializer;
import org.andengine.entity.particle.modifier.ScaleParticleModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.UncoloredSprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.SAXUtils;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

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

public class GameScene extends ManagedScene implements IOnSceneTouchListener {
	// Create an easy to manage HUD that we can attach/detach when the game scene is shown or hidden.
	public HUD mGameHud = new HUD();
	public GameScene mGameScene = this;
	
	// Loading scene objects
	private Text mLoadingText;
	private Scene mLoadingScene;
	
	// Game scene objects
	public static Player mPlayer;
	public static int mGameWidth;
	private PhysicsWorld mPhysicsWorld;
	
	private boolean mFirstTouch = false;
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
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_LEVEL_COMPLETE = "levelComplete";
	
	// Create a 2 second loading screen
	public GameScene() {
		this(2f);
	};

	public GameScene(float pLoadingScreenMinimumSecondsShown) {
		super(pLoadingScreenMinimumSecondsShown);
		// Setup the touch attributes for the Game Scenes
		this.setOnSceneTouchListenerBindingOnActionDownEnabled(true);
		this.setTouchAreaBindingOnActionDownEnabled(true);
		this.setTouchAreaBindingOnActionMoveEnabled(true);
		// Scale the Game Scenes according to the Camera's scale factor
		this.setScale(ResourceManager.getInstance().cameraScaleFactorX, ResourceManager.getInstance().cameraScaleFactorY);
		this.setPosition(0, ResourceManager.getInstance().cameraHeight/2f);
		mGameHud.setScaleCenter(0f, 0f);
		mGameHud.setScale(ResourceManager.getInstance().cameraScaleFactorX,ResourceManager.getInstance().cameraScaleFactorY);
	}
	
	// ====================================================
	// LOADING SCENE
	// ====================================================
	@Override
	public Scene onLoadingScreenLoadAndShown() {
		// Setup and return the loading screen
		mLoadingScene = new Scene();
		mLoadingScene.setBackgroundEnabled(true);
		mLoadingText = new Text(0,0,ResourceManager.fontDefault32Bold,"Loading...",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		mLoadingText.setPosition(ResourceManager.getInstance().cameraWidth/2f, ResourceManager.getInstance().cameraHeight/2f);
		mLoadingScene.attachChild(mLoadingText);
		return mLoadingScene;
	}

	@Override
	public void onLoadingScreenUnloadAndHidden() {
		// detach the loading screen resources
		mLoadingText.detachSelf();
		mLoadingText = null;
		mLoadingScene = null;
	}
	
	// ====================================================
	// GAME SCENE
	// ====================================================
	@Override
	public void onLoadScene() {
		// Load the resources to be used in the Game Scenes.
		ResourceManager.loadGameResources();
		
		this.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
	    
	    this.setOnSceneTouchListener(new IOnSceneTouchListener() {

            @Override
            public boolean onSceneTouchEvent(Scene pScene,TouchEvent pSceneTouchEvent) {
            	if (pSceneTouchEvent.isActionDown()) {
		            Log.i("TOUCH EVENT", "TOUCHED");
		            	            
		            if (!mFirstTouch) {
		            	mPlayer.setRunning();
		            	mFirstTouch = true;
					}
					else {
						mPlayer.jump();
					}
		        }
		        return true;
            }
        });
	    
		createHUD();
		createPhysics();
		loadLevel(1);
	}
	
	private void createHUD() {
		// Setup the HUD Buttons and Button Texts and ScoreText.
		ButtonSprite MainMenuButton = new ButtonSprite(0f,0f, 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		MainMenuButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
		MainMenuButton.setPosition((MainMenuButton.getWidth()*MainMenuButton.getScaleX())/2f, (MainMenuButton.getHeight()*MainMenuButton.getScaleY())/2f);
		MainMenuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// Play the click sound and show the Main Menu.
				ResourceManager.clickSound.play();
				SceneManager.getInstance().showMainMenu();
			}});
		
		Text MainMenuButtonText = new Text(MainMenuButton.getWidth()/2,MainMenuButton.getHeight()/2,ResourceManager.fontDefault32Bold,"MENU",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		MainMenuButton.attachChild(MainMenuButtonText);
		mGameHud.attachChild(MainMenuButton);
		mGameHud.registerTouchArea(MainMenuButton);
		
		ButtonSprite OptionsButton = new ButtonSprite(0f,0f, 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		OptionsButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
		OptionsButton.setPosition(800f-((OptionsButton.getWidth()*OptionsButton.getScaleX())/2f), (OptionsButton.getHeight()*OptionsButton.getScaleY())/2f);
		OptionsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// Play the click sound and show the Options Layer.
				ResourceManager.clickSound.play();
				SceneManager.getInstance().showOptionsLayer(true);
		}});		
		Text OptionsButtonText = new Text(0,0,ResourceManager.fontDefault32Bold,"OPTIONS",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		OptionsButtonText.setPosition((OptionsButton.getWidth())/2, (OptionsButton.getHeight())/2);
		OptionsButton.attachChild(OptionsButtonText);
		mGameHud.attachChild(OptionsButton);
		mGameHud.registerTouchArea(OptionsButton);
		
		// play pause button
		final ButtonSprite playPauseButton = new ButtonSprite(0f,0f, 
				ResourceManager.play_pause_region.getTextureRegion(0), 
				ResourceManager.play_pause_region.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		playPauseButton.setCurrentTileIndex(1);
		playPauseButton.setScale(0.5f);
		playPauseButton.setPosition((ResourceManager.getInstance().cameraWidth-playPauseButton.getWidth())/2f, (playPauseButton.getHeight()*playPauseButton.getScaleY())/2f);
		playPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				ResourceManager.clickSound.play();
				
				if (!mGameScene.isIgnoreUpdate()) {
					mGameScene.setIgnoreUpdate(true);
					playPauseButton.setCurrentTileIndex(0);
				} else {
					mGameScene.setIgnoreUpdate(false);
					playPauseButton.setCurrentTileIndex(1);	
				}
		}});
		mGameHud.attachChild(playPauseButton);
		mGameHud.registerTouchArea(playPauseButton);
	}
	
	private void createPhysics()
	{
		mPhysicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false);	
		mPhysicsWorld.setContactListener(contactListener());
		registerUpdateHandler(mPhysicsWorld);
	}
	
	private void loadLevel(int levelID) {
		final SimpleLevelLoader levelLoader = new SimpleLevelLoader(ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		
		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
		
		levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
		{
			public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
			{
				mGameWidth = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
				
				// set camera bounds
				ApplyingSceneManager.mCamera.setBounds(0, 0, mGameWidth, height); 
				ApplyingSceneManager.mCamera.setBoundsEnabled(true);
				
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
					levelObject = new Sprite(x, y, ResourceManager.testTextureRegion, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
					levelObject.setScale(0.2f);
					levelObject.setY((levelObject.getHeight()/2)*0.2f);
					Body body = PhysicsFactory.createBoxBody(mPhysicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
					body.setUserData("box");
					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
				
				}
				else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER)) {
					mPlayer = new Player(x, y, ResourceManager.getInstance().engine.getVertexBufferObjectManager(), ApplyingSceneManager.mCamera, mPhysicsWorld) {
						@Override
						public void onDie()	{
							if (!mDied) {
								ApplyingSceneManager.mCamera.setChaseEntity(null);
															
								explode((int) mPlayer.getX(), (int) mPlayer.getY());
														
								mPlayer.setVisible(false);
								mDied = true;
								SceneManager.getInstance().showOptionsLayer(false);
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
		
		levelLoader.loadLevelFromAsset(ResourceManager.getInstance().context.getAssets(), "level/" + levelID + ".lvl");
	}
	
	private ContactListener contactListener() {
		ContactListener contactListener = new ContactListener() {
			public void beginContact(Contact contact) {
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();

				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null) {
					if (x2.getBody().getUserData().equals("player")) {
						mPlayer.setfloorContact();
					}
					
					if (x1.getBody().getUserData().equals("platform2") && x2.getBody().getUserData().equals("player")) {
						ResourceManager.getInstance().engine.registerUpdateHandler(new TimerHandler(0.2f, new ITimerCallback() 	{									
						    public void onTimePassed(final TimerHandler pTimerHandler) {
						    	pTimerHandler.reset();
						    	ResourceManager.getInstance().engine.unregisterUpdateHandler(pTimerHandler);
						    	x1.getBody().setType(BodyType.DynamicBody);
						    }
						}));
					}
					
					if (x1.getBody().getUserData().equals("platform3") && x2.getBody().getUserData().equals("player")) {
						x1.getBody().setType(BodyType.DynamicBody);
					}
					
					if (x1.getBody().getUserData().equals("floor1") && x2.getBody().getUserData().equals("player")) {
						Log.i("TOUCHED", "FLOOR1");
					}
					if (x1.getBody().getUserData().equals("floor2") && x2.getBody().getUserData().equals("player")) {
						mPlayer.onDie();
					}
					if (x1.getBody().getUserData().equals("box") && x2.getBody().getUserData().equals("player")) {
						// if side is touched
						if (x2.getBody().getPosition().x < x1.getBody().getPosition().x && x2.getBody().getPosition().y < x1.getBody().getPosition().y) {
							mPlayer.onDie();
						}
						
					}
				}
			}

			public void endContact(Contact contact) {
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();

				if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null) {
					if (x2.getBody().getUserData().equals("player")) {
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
		float minSpawnRate = 50;
		float maxSpawnRate = 100;
		int maxParticleCount = 150;
		
		// Create Particle system
		BatchedSpriteParticleSystem particleSystem = new BatchedSpriteParticleSystem(particleEmitter, minSpawnRate, maxSpawnRate, maxParticleCount, ResourceManager.getInstance().player_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		
		// Add particle modifiers
		particleSystem.addParticleInitializer(new AccelerationParticleInitializer<UncoloredSprite>(-50f,50f,-100f,100f));
		//particleSystem.addParticleInitializer(new ExpireParticleInitializer<UncoloredSprite>(4));
		particleSystem.addParticleInitializer(new ColorParticleInitializer<UncoloredSprite>(0f,1f,0f,1f,0f,1f));
		particleSystem.addParticleModifier(new ScaleParticleModifier<UncoloredSprite>(0f,3f,0.2f,1f));
		
		attachChild(particleSystem);
	}
	

	/* (non-Javadoc)
	 * @see org.andengine.entity.scene.IOnSceneTouchListener#onSceneTouchEvent(org.andengine.entity.scene.Scene, org.andengine.input.touch.TouchEvent)
	 */
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		return false;
	}
	
	// ==================
	
	@Override
	public void onShowScene() {
		// We want to wait to set the HUD until the scene is shown because otherwise it will appear on top of the loading screen.
		ResourceManager.getInstance().engine.getCamera().setHUD(mGameHud);
	}
	
	@Override
	public void onHideScene() {
		ResourceManager.getInstance().engine.getCamera().setHUD(null);
	}
	
	@Override
	public void onUnloadScene() {
		// detach and unload the scene.
		mGameScene.detachChildren();
		mGameScene.clearEntityModifiers();
		mGameScene.clearTouchAreas();
		mGameScene.clearUpdateHandlers();

	}
}