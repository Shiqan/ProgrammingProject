package ferron.testgame;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public abstract class Player extends AnimatedSprite {

	private Body mBody;
	private boolean mRun = false;
	private boolean mFloorContact = true;
	
	// Constructor
	public Player(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld) {
		super(pX, pY, ResourceManager.getInstance().player_region, vbo);
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
				
				if (getY() <= 0) {
					onDie();
				}
				
				if (mRun) {	
					mBody.setLinearVelocity(new Vector2(5, mBody.getLinearVelocity().y)); 
				}
	        }
		});
	}
	
	public void setRunning() 	{
		mRun = true;
		
		final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100, 100, 100, 100 };
		
		/* 
		 * light = 0 - 5
		 * dark  = 6 - 11
		 * pink  = 48 - 53
		 * blue  = 54 - 59
		 */
		
		animate(PLAYER_ANIMATE, 0, 5, true);
	}
	
	public void jump() 	{
		if (!mFloorContact) {
			return; 
		}
		mBody.setLinearVelocity(new Vector2(mBody.getLinearVelocity().x, 12)); 
	}
	
	public void setfloorContact() {
		mFloorContact = true;
	}
	
	public void unsetfloorContact() {
		mFloorContact = false;
	}
	
	// Override onDie in each level
	public abstract void onDie();
}