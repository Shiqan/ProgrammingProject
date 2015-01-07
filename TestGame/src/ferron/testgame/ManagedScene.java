package ferron.testgame;

import org.andengine.entity.scene.Scene;

public abstract class ManagedScene extends Scene {
	public final boolean mHasLoadingScreen;
	public final float mMinLoadingScreenTime;
	public float mElapsedLoadingScreenTime = 0f;
	public boolean mIsLoaded = false;
	
	// Convenience constructor that disables the loading screen
	public ManagedScene() {
		this(0f);
	}
	
	// Constructor that sets the minimum length of the loading screen and sets hasLoadingScreen accordingly
	public ManagedScene(final float pLoadingScreenMinimumSecondsShown) {
		mMinLoadingScreenTime = pLoadingScreenMinimumSecondsShown;
		mHasLoadingScreen = (mMinLoadingScreenTime > 0f);
	}
	
	// Called by the Scene Manager. It calls onLoadScene if loading is needed, sets the isLoaded status, and pauses the scene while it's not shown.
	public void onLoadManagedScene() {
		if(!mIsLoaded) {
			onLoadScene();
			mIsLoaded = true;
			this.setIgnoreUpdate(true);
		}
	}
	// Called by the Scene Manager. It calls onUnloadScene if the scene has been previously loaded and sets the isLoaded status.
	public void onUnloadManagedScene() {
		if(mIsLoaded) {
			mIsLoaded = false;
			onUnloadScene();
		}
	}
	
	// Called by the Scene Manager. It unpauses the scene before showing it.
	public void onShowManagedScene() {
		this.setIgnoreUpdate(false);
		onShowScene();
	}
	
	// Called by the Scene Manager. It pauses the scene before hiding it.
	public void onHideManagedScene() {
		this.setIgnoreUpdate(true);
		onHideScene();
	}
	
	// Methods to Override in the subclasses.
	public abstract Scene onLoadingScreenLoadAndShown();
	public abstract void onLoadingScreenUnloadAndHidden();
	public abstract void onLoadScene();
	public abstract void onShowScene();
	public abstract void onHideScene();
	public abstract void onUnloadScene();
}