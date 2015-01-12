package ferron.testgame;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;

public class OptionsScene extends ManagedScene {
	
	private static final OptionsScene INSTANCE = new OptionsScene();
	public static OptionsScene getInstance(){
		return INSTANCE;
	}
	
	public OptionsScene() {
		this.setOnSceneTouchListenerBindingOnActionDownEnabled(true);
		this.setTouchAreaBindingOnActionDownEnabled(true);
		this.setTouchAreaBindingOnActionMoveEnabled(true);
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
		//ResourceManager.loadOptionsResources();

		// Create the background
		Sprite pBackground = new Sprite(ResourceManager.getInstance().cameraWidth/2f,ResourceManager.getInstance().cameraHeight/2f,ResourceManager.menuBackgroundTextureRegion,ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		pBackground.setScaleX(ResourceManager.getInstance().cameraWidth);
		pBackground.setScaleY(ResourceManager.getInstance().cameraHeight/480f);
		pBackground.setZIndex(-5000);
		this.attachChild(pBackground);
			
		// Create a title
		Text pTitleText = new Text(0, 0, ResourceManager.fontDefault72Bold, "OPTIONS", ResourceManager.getInstance().engine.getVertexBufferObjectManager());
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
		this.detachChildren();
	}
}