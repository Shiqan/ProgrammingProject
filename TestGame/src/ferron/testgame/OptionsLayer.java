package ferron.testgame;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.text.Text;

public class OptionsLayer extends HUD {
	private static final OptionsLayer INSTANCE = new OptionsLayer();
		
	public static OptionsLayer getInstance(){
		return INSTANCE;
	}
	
	// Animates the layer to slide in from the top
	IUpdateHandler SlideIn = new IUpdateHandler() {
		@Override
		public void onUpdate(float pSecondsElapsed) {
			if(OptionsLayer.getInstance().getY()>ResourceManager.getInstance().cameraHeight/2f) {
				OptionsLayer.getInstance().setPosition(OptionsLayer.getInstance().getX(), Math.max(OptionsLayer.getInstance().getY()-(3600*(pSecondsElapsed)),ResourceManager.getInstance().cameraHeight/2f));
			} else {
				OptionsLayer.getInstance().unregisterUpdateHandler(this);
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
			if(OptionsLayer.getInstance().getY()<ResourceManager.getInstance().cameraHeight/2f+480f) {
				OptionsLayer.getInstance().setPosition(OptionsLayer.getInstance().getX(), Math.min(OptionsLayer.getInstance().getY()+(3600*(pSecondsElapsed)),ResourceManager.getInstance().cameraHeight/2f+480f));
			} else {
				OptionsLayer.getInstance().unregisterUpdateHandler(this);
				SceneManager.getInstance().hideLayer();
			}
		}
		@Override public void reset() {
			
		}
	};
	
	public void onLoadLayer() {
		String pScoreString = "Reached " + Math.round((GameScene.mPlayer.getX()/GameScene.mGameWidth)*100) + "%";
		
		// Create a transparent background
		final float BackgroundX = 0f;
		final float BackgroundY = 0f;
		final float BackgroundWidth = 760f;
		final float BackgroundHeight = 440f;
		
		Rectangle pBackground = new Rectangle(BackgroundX,BackgroundY,BackgroundWidth,BackgroundHeight,ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pBackground.setColor(0f, 0f, 0f, 0.85f);
		this.attachChild(pBackground);
		
		// Create the title text for the Layer
		Text pLayerTitle = new Text(0,0,ResourceManager.fontDefault32Bold,"GAME OVER!",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pLayerTitle.setPosition(0f,(BackgroundHeight/2f)-pLayerTitle.getHeight());
		this.attachChild(pLayerTitle);
		
		// Create the score text for the Layer
		Text pScoreText = new Text(0,0,ResourceManager.fontDefault32Bold, pScoreString ,ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pScoreText.setPosition(0f-pScoreText.getWidth(),(BackgroundHeight/3f)-pScoreText.getHeight());
		this.attachChild(pScoreText);
		
		// Create a menu button
		ButtonSprite pMainMenuButton = new ButtonSprite(
				0f+pScoreText.getWidth(),
				(BackgroundHeight/3f)-pScoreText.getHeight(), 
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
				onUnloadLayer();
				SceneManager.getInstance().showMainMenu();
			}});
		Text pMainMenuButtonText = new Text(pMainMenuButton.getWidth()/2,pMainMenuButton.getHeight()/2,ResourceManager.fontDefault32Bold,"MENU",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pMainMenuButton.attachChild(pMainMenuButtonText);
		this.attachChild(pMainMenuButton);
		this.registerTouchArea(pMainMenuButton);
		
		// Create a restart button 
		ButtonSprite pRestartButton = new ButtonSprite(
				0f+pScoreText.getWidth(),
				pMainMenuButton.getHeight()-pScoreText.getHeight(),
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pRestartButton.setScale(1/ResourceManager.getInstance().cameraScaleFactorX, 1/ResourceManager.getInstance().cameraScaleFactorY);
		pRestartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				SceneManager.getInstance().showScene(new GameScene());
				onUnloadLayer();
				ResourceManager.clickSound.play();
			}});
		Text pRestartButtonText = new Text(pRestartButton.getWidth()/2, pRestartButton.getHeight()/2, ResourceManager.fontDefault32Bold, "RESTART", ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pRestartButton.attachChild(pRestartButtonText);
		this.attachChild(pRestartButton);
		this.registerTouchArea(pRestartButton);
		
		
		
		this.setPosition(ResourceManager.getInstance().cameraWidth/2f, ResourceManager.getInstance().cameraHeight/2f+480f);
	}

	public void onShowLayer() {
		onLoadLayer();
		this.registerUpdateHandler(SlideIn);
	}

	public void onHideLayer() {
		this.registerUpdateHandler(SlideOut);
		onUnloadLayer();
	}

	public void onUnloadLayer() {
		this.detachChildren();
	}
}
