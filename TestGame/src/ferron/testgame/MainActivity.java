package ferron.testgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

      
	    
	    ImageButton mNew = (ImageButton) findViewById(R.id.btn_start);
		mNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				  Intent intent = new Intent(MainActivity.this, GameActivity.class);
				  startActivity(intent);
				  finish();
			}
		});
	    
	    }

}
