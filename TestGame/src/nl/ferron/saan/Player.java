package nl.ferron.saan;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public abstract class Player extends AnimatedSprite {

	private Body mBody;
	private boolean mMove = false;
	private boolean mFloorContact = true;
	
	// Create AnimatedSprite
	public Player(float pX, float pY, Camera camera, PhysicsWorld physicsWorld) {
		super(pX, pY, ResourceManager.getInstance().player_region, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
		createPhysics(camera, physicsWorld);
		camera.setChaseEntity(this);
	}

	private void createPhysics(final Camera camera, PhysicsWorld physicsWorld) {		
		mBody = PhysicsFactory.createBoxBody(physicsWorld, this, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(0, 0, 0));

		mBody.setUserData("player");
		mBody.setFixedRotation(true);
		
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, mBody, true, false) {
			@Override
	        public void onUpdate(float pSecondsElapsed) {
				super.onUpdate(pSecondsElapsed);
				camera.onUpdate(0.1f);
				
				//Log.i("PLAYER", mBody.getPosition().x + "");
				
				if (GameActivity.mContinuousTouch) {
					jump();
				}
				
				if (mMove) {	
					mBody.setLinearVelocity(new Vector2(8, mBody.getLinearVelocity().y)); 
				} 
	        }
		});
	}
	
	public void start() 	{
		mMove = true;
		
		final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100, 100, 100, 100 };
		animate(PLAYER_ANIMATE, 0, 5, true);
	}
	
	public void jump() 	{
		if (!mFloorContact) {
			return; 
		}

		mBody.setLinearVelocity(new Vector2(0, 11));
	}
	
	public void setfloorContact() {
		mFloorContact = true;
	}
	
	public void unsetfloorContact() {
		mFloorContact = false;
	}
	
	// Override onDie in each level for custom deaths per level
	public abstract void onDie();
}