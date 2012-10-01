package name.nanek.vidaccessor.android;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class VideoDetails  extends BaseActivity {

	private static final String LOG = VideoDetails.class.getSimpleName();
	
	private static final String URL_EXTRA = VideoDetails.class.getName() + ".URL_EXTRA";
	
	private static final String TITLE_EXTRA = VideoDetails.class.getName() + ".TITLE_EXTRA";
	
	private static final String ID_EXTRA = VideoDetails.class.getName() + ".ID_EXTRA";
	
	private static final String NAME_EXTRA = VideoDetails.class.getName() + ".NAME_EXTRA";
	
	public static void launchDetailsActivity(Context aContext, String aUrl, String aTitle, String id, String nameValue) {
		Intent i = new Intent(aContext, VideoDetails.class);
		i.putExtra(URL_EXTRA, aUrl);
		i.putExtra(TITLE_EXTRA, aTitle);
		i.putExtra(ID_EXTRA, id);
		i.putExtra(NAME_EXTRA, nameValue);
		aContext.startActivity(i);
	}
	
	private String url;
	
	private String title;
	
	private String id;
	
	private String name;

	private TextView detailsRating;
	
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if ( null == msg.obj ) {
				showErrorDialog(RATINGS_ERROR);
				return;
			}
			
			try {
				String serverRating = (String) msg.obj;
				
				detailsRating.setText(serverRating);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				showErrorDialog(RATINGS_ERROR);
			}

			
			progressDialog.dismiss();
		}
	};
	
    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
    	super.onCreate(aSavedInstanceState);
    	
    	Intent i = getIntent();
    	Bundle extras = i.getExtras();
    	url = extras.getString(URL_EXTRA);
    	title = extras.getString(TITLE_EXTRA);
    	id = extras.getString(ID_EXTRA);
    	name = extras.getString(NAME_EXTRA);
    		
    	setContentView(R.layout.video_details);
    	
    	final TextView titleView = (TextView) findViewById(R.id.details_title);
    	titleView.setText(title);
    	
    	detailsRating = (TextView) findViewById(R.id.details_rating);
    	
        final Button view = (Button) findViewById(R.id.details_view);
        view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent youtubeIntent = new Intent(Intent.ACTION_VIEW);
					youtubeIntent.setData(Uri.parse(url));
					youtubeIntent.setClassName("com.google.android.youtube", "com.google.android.youtube.WatchActivity");
					startActivity(youtubeIntent);
				} catch(ActivityNotFoundException e) {
					Intent youtubeIntent = new Intent(Intent.ACTION_VIEW);
					youtubeIntent.setData(Uri.parse(url));
					startActivity(youtubeIntent);	
				}
			}
		});
    	
        final Button share = (Button) findViewById(R.id.details_share);
        share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			    emailIntent .setType("plain/text");
			    emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "Sharing a Video With You!");			     
			    emailIntent .putExtra(android.content.Intent.EXTRA_TEXT, url);
			    startActivity(emailIntent);
			}
		});
    	
        final Button rateUp = (Button) findViewById(R.id.details_rate_up);
        rateUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadResults(1);
			}
		});
    	
        final Button rateDown = (Button) findViewById(R.id.details_rate_down);
        rateDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadResults(-1);
			}
		});
    	
        final Button channelStats = (Button) findViewById(R.id.details_channel_stats);
        channelStats.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChannelDetails.launchDetailsActivity(VideoDetails.this, name, name);
			}
		});

        loadResults(0);
    }
    
	private String buildUrl(int rating) {
		return
			"http://vidaccessor.appspot.com/?videoId=" + encode(id) + "&rating=" + rating;
	}

	private void loadResults(final int rating) {
    	showProgressDialog();
    	
    	Thread thread = new Thread() {
			@Override
			public void run() {
				String url = buildUrl(rating);

				String data = HttpUtil.getStringResult(url, null);
				
				Message message = handler.obtainMessage(0, data);
		    	handler.sendMessage(message);
			}
    	};
    	thread.start();
    }
}
