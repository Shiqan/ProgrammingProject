package nl.ferron.saan;

/**
 * Activity to show a splashscreen when the app starts for 1 second
 * @author Ferron
 */


import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class SplashScreenActivity extends Activity {

	// SplashScreen timer
	private int mSplashTime = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_splash);
		
		// Change font
		TextView txtTitle = (TextView) findViewById(R.id.txt_title_splash);
		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/bigjohn.otf");
		txtTitle.setTypeface(type);
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent i = new Intent(getApplicationContext(),
						MainActivity.class);
				startActivity(i);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
			}
		}, mSplashTime);
	}

}
