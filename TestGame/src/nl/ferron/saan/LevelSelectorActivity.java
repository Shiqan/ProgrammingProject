package nl.ferron.saan;

/**
 * Activity with viewflipper to select a level to play
 * @author Ferron
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class LevelSelectorActivity extends Activity {
	private ViewFlipper mViewFlipper;
    private float mStartX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.act_level_selector);
        mViewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        
        // Change font
        TextView txtLevel1 = (TextView) findViewById(R.id.txt_lvl1);
        TextView txtLevel2 = (TextView) findViewById(R.id.txt_lvl2);
        TextView txtLevel3 = (TextView) findViewById(R.id.txt_lvl3);
		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/slimjoe.otf");
		txtLevel1.setTypeface(type);
		txtLevel2.setTypeface(type);
		txtLevel3.setTypeface(type);
		
		ImageView imgLevel1 = (ImageView) findViewById(R.id.img_lvl1);
		ImageView imgLevel2 = (ImageView) findViewById(R.id.img_lvl2);
		
		// When level is completed set to color img
		if (UserDataManager.getInstance().getMaxScore(1) == 100) {
			imgLevel1.setImageResource(R.drawable.level1_completed);
		}
		if (UserDataManager.getInstance().getMaxScore(2) == 100) {
			imgLevel2.setImageResource(R.drawable.level2_completed);
		}
		
		OnClickListener test = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int id = mViewFlipper.getDisplayedChild() + 1;
				Intent intent = new Intent(LevelSelectorActivity.this, GameActivity.class);
				intent.putExtra("level", id);
	        	startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
				
			}
		};
		imgLevel1.setOnClickListener(test);
		imgLevel2.setOnClickListener(test);
		
    }

    /**
     * Handle swipe events
     */
    public boolean onTouchEvent(MotionEvent touchevent) {
    	
    	switch (touchevent.getAction()) {
	        case (MotionEvent.ACTION_DOWN) : 
	        	mStartX = touchevent.getX();
	            return true;
	        case (MotionEvent.ACTION_UP) : 
	            float currentX = touchevent.getX();
	            
	            // Previous
	            if (mStartX < currentX) {
	                            
	                // Animate transition
	            	mViewFlipper.setInAnimation(this, android.R.anim.fade_in);
	            	mViewFlipper.setOutAnimation(this, android.R.anim.fade_out);
	       
	            	mViewFlipper.showPrevious();
	             }
	                                     
	             // Next
	             if (mStartX > currentX) {
	            	 // Animate transition
	            	 mViewFlipper.setInAnimation(this, android.R.anim.fade_in);
	            	 mViewFlipper.setOutAnimation(this, android.R.anim.fade_out);
	                 
	            	 mViewFlipper.showNext();
	             }
	             return true;
	        default : 
	            return super.onTouchEvent(touchevent);
    	 }
    }
}
