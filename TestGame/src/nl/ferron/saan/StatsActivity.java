package nl.ferron.saan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class StatsActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.act_stats);
        
		Typeface type = Typeface.createFromAsset(getAssets(),"fonts/bigjohn.otf");
		Typeface type2 = Typeface.createFromAsset(getAssets(),"fonts/slimjoe.otf");

		// Change font
        TextView txtTitle = (TextView) findViewById(R.id.txt_title_stats);
		txtTitle.setTypeface(type);
		
		TextView txtLevel1 = (TextView) findViewById(R.id.txt_level1);
		txtLevel1.setTypeface(type2);
		
		TextView txtLevel2 = (TextView) findViewById(R.id.txt_level2);
		txtLevel2.setTypeface(type2);
		
		TextView txtShouldSignIn = (TextView) findViewById(R.id.txt_should_sign_in);
		txtShouldSignIn.setTypeface(type2);
		
		// Change font and text
		TextView txtLeve1Value = (TextView) findViewById(R.id.txt_level1_value);
		txtLeve1Value.setTypeface(type2);
		txtLeve1Value.setText(UserDataManager.getInstance().getMaxScore(1)+"%");

		TextView txtLeve2Value = (TextView) findViewById(R.id.txt_level2_value);
		txtLeve2Value.setTypeface(type2);
		txtLeve2Value.setText(UserDataManager.getInstance().getMaxScore(2)+"%");

		// Menu button
	    ImageButton btnMenu = (ImageButton) findViewById(R.id.btn_menu);
	    btnMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(StatsActivity.this, MainActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
			}
		});
	    
	    }

}
