package ferron.testgame;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.util.math.MathUtils;

public class MenuScene extends ManagedScene {
	
	private static final MenuScene INSTANCE = new MenuScene();
	public static MenuScene getInstance(){
		return INSTANCE;
	}
	
	public MenuScene() {
		this.setTouchAreaBindingOnActionDownEnabled(true);
	}
	
	// No loading screen means no reason to use the following methods.
	@Override
	public Scene onLoadingScreenLoadAndShown() {
		return null;
	}
	@Override
	public void onLoadingScreenUnloadAndHidden() {
	}
	
	@Override
	public void onLoadScene() {
		// Load the menu resources
		ResourceManager.loadMenuResources();
		
		// Create the background
		Sprite pBackgroundSprite = new Sprite(ResourceManager.getInstance().cameraWidth/2f,ResourceManager.getInstance().cameraHeight/2f,ResourceManager.menuBackgroundTextureRegion,ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pBackgroundSprite.setScaleX(ResourceManager.getInstance().cameraWidth);
		pBackgroundSprite.setScaleY(ResourceManager.getInstance().cameraHeight/480f);
		pBackgroundSprite.setZIndex(-5000);
		this.attachChild(pBackgroundSprite);
		
		// Create clouds that move from one side of the screen to the other, and repeat
		Sprite[] CloudSprites = new Sprite[20];
		for (Sprite curCloudSprite: CloudSprites) {
			curCloudSprite = new Sprite(
					MathUtils.random(-(this.getWidth()*this.getScaleX())/2,ResourceManager.getInstance().cameraWidth+(this.getWidth()*this.getScaleX())/2),
					MathUtils.random(-(this.getHeight()*this.getScaleY())/2,ResourceManager.getInstance().cameraHeight + (this.getHeight()*this.getScaleY())/2),
					ResourceManager.cloudTextureRegion,
					ResourceManager.getInstance().engine.getVertexBufferObjectManager()) {
				private float XSpeed = MathUtils.random(0.2f, 2f);
				private boolean initialized = false;
				@Override
				protected void onManagedUpdate(final float pSecondsElapsed) {
					super.onManagedUpdate(pSecondsElapsed);
					if (!initialized) {
						initialized = true;
						this.setScale(XSpeed/2);
						this.setZIndex(-4000+Math.round(XSpeed*1000f));
						MenuScene.getInstance().sortChildren();
					}
					if (this.getX()<-(this.getWidth()*this.getScaleX())/2) {
						XSpeed = MathUtils.random(0.2f, 2f);
						this.setScale(XSpeed/2);
						this.setPosition(ResourceManager.getInstance().cameraWidth+(this.getWidth()*this.getScaleX())/2, MathUtils.random(-(this.getHeight()*this.getScaleY())/2,ResourceManager.getInstance().cameraHeight + (this.getHeight()*this.getScaleY())/2));
						
						this.setZIndex(-4000+Math.round(XSpeed*1000f));
						MenuScene.getInstance().sortChildren();
					}
					this.setPosition(this.getX()-(XSpeed*(pSecondsElapsed/0.016666f)), this.getY());
				}
			};
			this.attachChild(curCloudSprite);
		}
		
		// Create a Play button
		ButtonSprite pPlayButton = new ButtonSprite(
				(ResourceManager.getInstance().cameraWidth-ResourceManager.buttonTiledTextureRegion.getTextureRegion(0).getWidth())/2f,
				(ResourceManager.getInstance().cameraHeight-ResourceManager.buttonTiledTextureRegion.getTextureRegion(0).getHeight())*(1f/3f), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		Text PlayButtonText = new Text(0, 0, ResourceManager.fontDefault32Bold, "PLAY", ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		PlayButtonText.setPosition((pPlayButton.getWidth())/2, (pPlayButton.getHeight())/2);
		pPlayButton.attachChild(PlayButtonText);
		this.attachChild(pPlayButton);
		pPlayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				SceneManager.getInstance().showScene(new GameScene());
				ResourceManager.clickSound.play();
			}});
		this.registerTouchArea(pPlayButton);
		
		// Create a Stats Button
		ButtonSprite pStatsButton = new ButtonSprite(
				pPlayButton.getX()+pPlayButton.getWidth(), 
				pPlayButton.getY(),
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1),
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		Text pStatsButtonText = new Text(0,0,ResourceManager.fontDefault32Bold,"STATS",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pStatsButtonText.setPosition((pStatsButton.getWidth())/2, (pStatsButton.getHeight())/2);
		pStatsButton.attachChild(pStatsButtonText);
		this.attachChild(pStatsButton);
		pStatsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				SceneManager.getInstance().showScene(new StatsScene());
				ResourceManager.clickSound.play();
			}});
		this.registerTouchArea(pStatsButton);
		
		// Create an Option button
		ButtonSprite pOptionsButton = new ButtonSprite(				
				(ResourceManager.getInstance().cameraWidth-(ResourceManager.buttonTiledTextureRegion.getTextureRegion(0).getWidth()/2f)),
				(ResourceManager.getInstance().cameraHeight-ResourceManager.buttonTiledTextureRegion.getTextureRegion(0).getHeight()),
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		Text pOptionsButtonText = new Text(0,0,ResourceManager.fontDefault32Bold,"OPTIONS",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pOptionsButtonText.setPosition((pOptionsButton.getWidth())/2, (pOptionsButton.getHeight())/2);
		pOptionsButton.attachChild(pOptionsButtonText);
		this.attachChild(pOptionsButton);
		pOptionsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				SceneManager.getInstance().showScene(new OptionsScene());
				ResourceManager.clickSound.play();
			}});
		this.registerTouchArea(pOptionsButton);
			
		// Create a title
		Text pTitleText = new Text(0, 0, ResourceManager.fontDefault72Bold, "TEST", ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pTitleText.setPosition((ResourceManager.getInstance().cameraWidth)/2, (ResourceManager.getInstance().cameraHeight*2)/3f);
		pTitleText.setColor(0.153f, 0.290f, 0.455f);
		this.attachChild(pTitleText);
		
	}
	
	@Override
	public void onShowScene() {
	}
	
	@Override
	public void onHideScene() {
	}
	
	@Override
	public void onUnloadScene() {
	}
}