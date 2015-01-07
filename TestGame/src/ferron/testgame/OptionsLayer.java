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
		@Override public void reset() {}
	};
	
	public void onLoadLayer() {
		String pScoreString = "Reached " + Math.round((GameScene.mPlayer.getX()/GameScene.mGameWidth)*100) + "%";
		
		// Create a background
		final float BackgroundX = 0f;
		final float BackgroundY = 0f;
		final float BackgroundWidth = 760f;
		final float BackgroundHeight = 440f;
		
		Rectangle pBackground = new Rectangle(BackgroundX,BackgroundY,BackgroundWidth,BackgroundHeight,ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pBackground.setColor(0f, 0f, 0f, 0.85f);
		this.attachChild(pBackground);
		
		// Create the title text for the Layer
		Text pLayerTitle = new Text(0,0,ResourceManager.fontDefault32Bold,"GAME OVER!",ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pLayerTitle.setPosition(0f,BackgroundHeight/2f-pLayerTitle.getHeight());
		this.attachChild(pLayerTitle);
		
		// Create the score text for the Layer
		Text pScoreText = new Text(0,0,ResourceManager.fontDefault32Bold, pScoreString ,ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pScoreText.setPosition(0f,BackgroundHeight/3f-pScoreText.getHeight());
		this.attachChild(pScoreText);
		
		// Create a restart button 
		ButtonSprite pRestartButton = new ButtonSprite(
				(ResourceManager.getInstance().cameraWidth-ResourceManager.buttonTiledTextureRegion.getTextureRegion(0).getWidth())/2f,
				(ResourceManager.getInstance().cameraHeight-ResourceManager.buttonTiledTextureRegion.getTextureRegion(0).getHeight())*(1f/3f), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(0), 
				ResourceManager.buttonTiledTextureRegion.getTextureRegion(1), 
				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		Text pRestartButtonText = new Text(0, 0, ResourceManager.fontDefault32Bold, "RESTART", ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pRestartButtonText.setPosition((pRestartButton.getWidth())/2, (pRestartButton.getHeight())/2);
		pRestartButton.attachChild(pRestartButtonText);
		this.attachChild(pRestartButton);
		pRestartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				SceneManager.getInstance().showScene(new GameScene());
				onUnloadLayer();
				ResourceManager.clickSound.play();
			}});
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
