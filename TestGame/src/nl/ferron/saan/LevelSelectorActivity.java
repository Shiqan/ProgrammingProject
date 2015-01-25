/**
 * 
 */
package nl.ferron.saan;

/**
 * @author Ferron
 *
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
	private ViewFlipper viewFlipper;
    private float lastX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.act_level_selector);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        
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
		
		if (UserDataManager.getInstance().getMaxScore(1) == 100) {
			imgLevel1.setImageResource(R.drawable.level1_completed);
		}
		if (UserDataManager.getInstance().getMaxScore(2) == 100) {
			imgLevel2.setImageResource(R.drawable.level2_completed);
		}
		
		OnClickListener test = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int id = viewFlipper.getDisplayedChild() + 1;
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

    // Handle swipe events
    public boolean onTouchEvent(MotionEvent touchevent) {
    	
    	switch (touchevent.getAction()) {
	        case MotionEvent.ACTION_DOWN: 
	        	lastX = touchevent.getX();
	            break;
	        case MotionEvent.ACTION_UP: 
	            float currentX = touchevent.getX();
	            
	            // Previous
	            if (lastX < currentX) {
	                            
	                // Animate transition
	                viewFlipper.setInAnimation(this, android.R.anim.fade_in);
	                viewFlipper.setOutAnimation(this, android.R.anim.fade_out);
	       
	                viewFlipper.showPrevious();
	             }
	                                     
	             // Next
	             if (lastX > currentX) {
	            	 // Animate transition
	            	 viewFlipper.setInAnimation(this, android.R.anim.fade_in);
	            	 viewFlipper.setOutAnimation(this, android.R.anim.fade_out);
	                 
	                 viewFlipper.showNext();
	             }
	             break;
    	 }
         return false;
    }
}