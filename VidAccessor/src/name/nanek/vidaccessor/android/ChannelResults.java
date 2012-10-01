package name.nanek.vidaccessor.android;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ChannelResults  extends BaseActivity {

	private static final String LOG = ChannelResults.class.getSimpleName();
	
	private static final String TERMS_EXTRA = ChannelResults.class.getName() + ".TERMS_EXTRA";
	
	private static final String OFFSET_EXTRA = ChannelResults.class.getName() + ".OFFSET_EXTRA";
	
	private static final int RESULS_PER_PAGE = 10;
	
	private static final String KEY = "AI39si41fycFShT2Vaqa4136YumWRgmPNl1fS4F58LIFDbOrBT8O4VVSrgxKBBkiPcNoOwXPxhAfofFRoWZYx_FehRKSEZIHSg";
	
	public static void launchResultsActivity(Context aContext, String aTerms, int aOffset) {
		Intent i = new Intent(aContext, ChannelResults.class);
		i.putExtra(TERMS_EXTRA, aTerms);
		i.putExtra(OFFSET_EXTRA, aOffset);
		aContext.startActivity(i);
	}
	
	private String terms;
	
	private int offset;
	
	private Button[] buttons;
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if ( null == msg.obj ) {
				showErrorDialog(YOUTUBE_ERROR);
				return;
			}
			
			
			try {
				//Log.i(LOG, "YouTube replied: " + msg.obj);
				
				JSONObject response = (JSONObject) msg.obj;
				
				JSONObject feed = response.getJSONObject("feed");
				
				JSONObject totalResults = feed.getJSONObject("openSearch$totalResults");
				int totalResultsValue = totalResults.getInt("$t");
				Log.i(LOG, "total results: " + totalResultsValue);
				next.setEnabled( totalResultsValue > offset + RESULS_PER_PAGE);
				
				JSONArray entry = feed.getJSONArray("entry");
						
				int i = 0;
				for( ; i < entry.length(); i++) {
					JSONObject item = entry.getJSONObject(i);
				
					//Log.i(LOG, "Entry item: " + item);

					Iterator<String> keyIterator = item.keys();
					while( keyIterator.hasNext() ) {
						String key = keyIterator.next();
					
						Log.i(LOG, "Item key: " + key);
					}

					
					JSONArray links = item.getJSONArray("gd$feedLink");
					final String linkValue = links.getJSONObject(0).getString("href");
					Log.i(LOG, "link: " + linkValue);

					JSONObject title = item.getJSONObject("title");
					final String titleValue = title.getString("$t");
					Log.i(LOG, "title: " + titleValue);

					JSONArray author = item.getJSONArray("author");
					JSONObject name = author.getJSONObject(0).getJSONObject("name");
					final String nameValue = name.getString("$t");
					Log.i(LOG, "name: " + nameValue);
					
					final String finalTitleValue;
					if ( null == titleValue || titleValue.trim().equals("") ) {
						finalTitleValue = "[Untitled]";
					} else {
						finalTitleValue = titleValue;
					}
					
					buttons[i].setText(finalTitleValue);
					buttons[i].setVisibility(View.VISIBLE);
					buttons[i].setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							ChannelDetails.launchDetailsActivity(ChannelResults.this, nameValue, finalTitleValue);
						}
					});
					
				}
				for( ; i < buttons.length; i++) {
					buttons[i].setVisibility(View.INVISIBLE);
				}
				
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				showErrorDialog(YOUTUBE_ERROR);
			}

			
			progressDialog.dismiss();
		}
	};
	
	private Button next;
	
	private String buildUrl() {
		return
			"http://gdata.youtube.com/feeds/api/channels?" + 
			"q=" + encode(terms) + 
			"&start-index=" + (offset + 1) + 
			"&max-results=10" + 
			"&alt=json" + 
			"&v=2" +
			"&key=" + KEY;
	}
	
    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
    	super.onCreate(aSavedInstanceState);
    	
    	Intent i = getIntent();
    	Bundle extras = i.getExtras();
    	terms = extras.getString(TERMS_EXTRA);
    	offset = extras.getInt(OFFSET_EXTRA);
    		
    	setContentView(R.layout.results);
    	
        final Button previous = (Button) findViewById(R.id.results_previous);
        previous.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChannelResults.launchResultsActivity(ChannelResults.this, terms, offset - RESULS_PER_PAGE);
				finish();
			}
		});
        if ( 0 == offset ) {
        	previous.setEnabled(false);
        }
        
        next = (Button) findViewById(R.id.results_next);
        next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChannelResults.launchResultsActivity(ChannelResults.this, terms, offset + RESULS_PER_PAGE);
				finish();
			}
		});
        
        //Should really be changed to a list view, this is ugly.
        buttons = new Button[] {
        		(Button) findViewById(R.id.results_1),
        		(Button) findViewById(R.id.results_2),
        		(Button) findViewById(R.id.results_3),
        		(Button) findViewById(R.id.results_4),
        		(Button) findViewById(R.id.results_5),
        		(Button) findViewById(R.id.results_6),
        		(Button) findViewById(R.id.results_7),
        		(Button) findViewById(R.id.results_8),
        		(Button) findViewById(R.id.results_9),
        		(Button) findViewById(R.id.results_10),
        };
    	
    	loadResults();
    }
    
    private void loadResults() {
    	showProgressDialog();
    	
    	Thread thread = new Thread() {
			@Override
			public void run() {
				String url = buildUrl();

				JSONObject data = HttpUtil.getJSONResults(url);
				
				Message message = handler.obtainMessage(0, data);
		    	handler.sendMessage(message);
			}
    	};
    	thread.start();
    }
    
}
