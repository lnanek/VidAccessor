package name.nanek.vidaccessor.android;

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
import android.widget.TextView;

public class ChannelDetails  extends BaseActivity {

	private static final int NON_CAPTION_MESSAGE = 1;
	
	private static final int CAPTION_MESSAGE = 2;

	private static final int RATING_MESSAGE = 3;
	
	private static final String LOG = ChannelDetails.class.getSimpleName();
	
	private static final String NAME_EXTRA = ChannelDetails.class.getName() + ".NAME_EXTRA";
	
	private static final String TITLE_EXTRA = ChannelDetails.class.getName() + ".TITLE_EXTRA";
	
	public static void launchDetailsActivity(Context aContext, String aName, String aTitle) {
		Intent i = new Intent(aContext, ChannelDetails.class);
		i.putExtra(NAME_EXTRA, aName);
		i.putExtra(TITLE_EXTRA, aTitle);
		aContext.startActivity(i);
	}
	
	private String name;
	
	private String title;

	private TextView detailsRating;

	private TextView percentRating;
	
	private int nonCaptionResults;

	private int captionedResults;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if ( null == msg.obj ) {
				showErrorDialog(RATINGS_ERROR);
				return;
			}
			
			
			if ( NON_CAPTION_MESSAGE == msg.what ) {
				try {				
					//Log.i(LOG, "YouTube replied: " + msg.obj);
					JSONObject response = (JSONObject) msg.obj;
					JSONObject feed = response.getJSONObject("feed");	
					JSONObject totalResults = feed.getJSONObject("openSearch$totalResults");
					nonCaptionResults = totalResults.getInt("$t");
					Log.i(LOG, "nonCaptionResults: " + nonCaptionResults);	
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					showErrorDialog(YOUTUBE_ERROR);
				}
				
		        loadResults(buildCaptionUrl(name), CAPTION_MESSAGE);
			} else if ( CAPTION_MESSAGE == msg.what ) {
				try {				
					//Log.i(LOG, "YouTube replied: " + msg.obj);
					JSONObject response = (JSONObject) msg.obj;
					JSONObject feed = response.getJSONObject("feed");	
					
					if ( 0 == offset ) {
						JSONObject totalResults = feed.getJSONObject("openSearch$totalResults");
						captionedResults = totalResults.getInt("$t");
						Log.i(LOG, "captionedResults: " + captionedResults);	
	
						int captionedAndNonCaptioned = nonCaptionResults + captionedResults;
						float captionedResultsFloat = (float) captionedResults;
						float captionedAndNonCaptionedFloat = (float) captionedAndNonCaptioned;
						float percent = captionedResultsFloat / captionedAndNonCaptionedFloat;
						percentRating.setText(percent + "%");
					}
					offset += RESULS_PER_PAGE;
					if ( offset + 1 > captionedResults || offset >= 1000 ) {
						if (offset >= 1000) {
							detailsLimit.setVisibility(View.VISIBLE);
						}
						readAllCaptioned = true;
					}
					
					//this is always just the page size, actually, even if there are less results ;(
					JSONObject resultsThisPage = feed.getJSONObject("openSearch$itemsPerPage");
					int resultsThisPageValue = resultsThisPage.getInt("$t");
					
					//Getting the entry fails if there are 0 captioned videos, so check for that...
					if ( captionedResults > 0 ) {
						String[] ids = new String[resultsThisPageValue];		
						JSONArray entry = feed.getJSONArray("entry");						
						int i = 0;
						for( ; i < entry.length(); i++) {
							JSONObject item = entry.getJSONObject(i);
							JSONObject id = item.getJSONObject("id");
							final String idValue = id.getString("$t");
							Log.i(LOG, "id: " + idValue);
							ids[i] = idValue;
						}					
						
				        getRatings(ids, RATING_MESSAGE);
					} else {
						readAllCaptioned = true;
						Message message = handler.obtainMessage(RATING_MESSAGE, 0);
				    	handler.sendMessage(message);
					}
			        
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					showErrorDialog(YOUTUBE_ERROR);
				}				
				
				
				
			} else if ( RATING_MESSAGE == msg.what ) {
				
				currentRatingValue += (Integer) msg.obj;
				if (readAllCaptioned) {
					detailsRating.setText("" + currentRatingValue);
					progressDialog.dismiss();
				} else {
			        loadResults(buildCaptionUrl(name), CAPTION_MESSAGE);
				}
				
			}
		}
	};
	
	int currentRatingValue;
	
	boolean readAllCaptioned;
	
	TextView detailsLimit;
	
    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
    	super.onCreate(aSavedInstanceState);
    	
    	Intent i = getIntent();
    	Bundle extras = i.getExtras();
    	name = extras.getString(NAME_EXTRA);
    	title = extras.getString(TITLE_EXTRA);
    		
    	setContentView(R.layout.channel_details);
    	
    	final TextView titleView = (TextView) findViewById(R.id.details_title);
    	titleView.setText(title);
    	
    	detailsLimit = (TextView) findViewById(R.id.details_limit);
    	
    	detailsRating = (TextView) findViewById(R.id.details_rating);
    	
    	percentRating = (TextView) findViewById(R.id.details_percentage);
    	

        loadResults(buildNonCaptionUrl(name), NON_CAPTION_MESSAGE);
    }
    
    int offset = 0;
    
    //Unfortunately, the email doesn't say if channel stats should be mobile only or not...
	private String buildNonCaptionUrl(String authorName) {
		return
			"http://gdata.youtube.com/feeds/mobile/videos?" + 
//			"http://gdata.youtube.com/feeds/api/videos?" + 
//			"q=" + encode(terms) + 
			"author=" + encode(authorName) + 
//			"&start-index=" + (offset + 1) + 
			"&max-results=0" + 
			"&caption=false" + 
//			"&format" + format+ 
			"&alt=json" + 
			"&v=2" +
			"&key=" + HttpUtil.YOUTUBE_API_DEVELOPER_KEY;
	}
	
	private String buildCaptionUrl(String authorName) {
		return
			"http://gdata.youtube.com/feeds/mobile/videos?" + 
	//		"http://gdata.youtube.com/feeds/api/videos?" + 
	//		"q=" + encode(terms) + 
			"author=" + encode(authorName) + 
			"&start-index=" + (offset + 1) + 
			"&max-results=" + RESULS_PER_PAGE +  
			"&caption=true" + 
	//		"&format" + format+ 
			"&alt=json" + 
			"&v=2" +
			"&key=" + HttpUtil.YOUTUBE_API_DEVELOPER_KEY;
	}
       
	private void loadResults(final String loadUrl, final int messageId) {
		showProgressDialog();
    	
    	Thread thread = new Thread() {
			@Override
			public void run() {

				JSONObject data = HttpUtil.getJSONResults(loadUrl);
				
				Message message = handler.obtainMessage(messageId, data);
		    	handler.sendMessage(message);
			}
    	};
    	thread.start();
    }
	
	/*
	
	private static final int RESULS_PER_PAGE = 50;
	
	private String buildUrl() {
		//return "http://vidaccessor .appspot.com/";
		return "http://10.0.2.2:8888/";
	}
	  
	//414 Request-URI Too Large if we send too many to app engine at once via HTTP GET, unfortunately, so using POST
	private List <NameValuePair> buildPostData(String[] ids, int rating) {
		List<NameValuePair> pairs = new ArrayList <NameValuePair>();
		pairs.add(new BasicNameValuePair("rating", "" + rating));

		//StringBuilder url = new StringBuilder("rating=").append(rating);
		for( String id : ids) {
			if ( null == id ) {
				continue;
			}
			pairs.add(new BasicNameValuePair("videoId", encode(id)));

			//url.append("&videoId=").append(encode(id));
		}
		//return url.toString();
		return pairs;
	}
	 */
	
	private static final int RESULS_PER_PAGE = 25;
	
	private String buildUrl(String[] ids, int rating) {
		StringBuilder url = new StringBuilder("http://vidaccessor.appspot.com/?rating=").append(rating);
		for( String id : ids) {
			if ( null == id ) {
				continue;
			}
			url.append("&videoId=").append(encode(id));
		}
		return url.toString();
	}

	private void getRatings(final String[] ids, final int ratingMessage) {
		showProgressDialog();
    	   	
    	Thread thread = new Thread() {
			@Override
			public void run() {
		
				int currentRatingValue = 0;
				
				if ( null != ids && ids.length > 0 && null != ids[0] ) {
				//for( String id : ids) {
				
					String data = HttpUtil.getStringResult(buildUrl(ids, 0), null);
					if ( null != data ) {
						data = data.trim();
						try {
							int videoRating = Integer.parseInt(data);
							currentRatingValue += videoRating;
							
						} catch (NumberFormatException e) {
							e.printStackTrace();
							showErrorDialogFromOtherThread(RATINGS_ERROR);
							return;
						}
					}
				
				//}
				}
				
				Message message = handler.obtainMessage(ratingMessage, currentRatingValue);
		    	handler.sendMessage(message);
			}
    	};
    	thread.start();
	}
}
