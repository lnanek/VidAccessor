package name.nanek.vidaccessor.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Search extends Activity {
    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.search);
        
        final EditText terms = (EditText) findViewById(R.id.search_terms);
        
        final Button searchVideos = (Button) findViewById(R.id.search_videos);
        searchVideos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				VideoResults.launchResultsActivity(Search.this, "" + terms.getText(), 0);
			}
		});
        
        final Button searchChannels = (Button) findViewById(R.id.search_channels);
        searchChannels.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChannelResults.launchResultsActivity(Search.this, "" + terms.getText(), 0);
			}
		});
    }
}