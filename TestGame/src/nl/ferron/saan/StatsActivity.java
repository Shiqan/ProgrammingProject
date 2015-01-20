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
		
		TextView txtLevel = (TextView) findViewById(R.id.txt_level1);
		txtLevel.setTypeface(type2);
		
		TextView txtDeaths = (TextView) findViewById(R.id.txt_gamesTitle);
		txtDeaths.setTypeface(type2);
		
		// Change font and text
		TextView txtLevelValue = (TextView) findViewById(R.id.txt_level1_value);
		txtLevelValue.setTypeface(type2);
		txtLevelValue.setText(UserDataManager.getInstance().getMaxScore()+"%");

		TextView txtDeathsValue = (TextView) findViewById(R.id.txt_games_value);
		txtDeathsValue.setTypeface(type2);
		txtDeathsValue.setText(UserDataManager.getInstance().getNumberOfGames()+"");
	   
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
