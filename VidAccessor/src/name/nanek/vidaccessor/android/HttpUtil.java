package name.nanek.vidaccessor.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.util.Log;

public class HttpUtil {
	/**
	 * This is the developer key for accessing the YouTube data API. 
	 * See details here to get one: 
	 * http://code.google.com/apis/youtube/2.0/developers_guide_protocol.html#Developer_Key 
	 * Then paste it in as the value of this String constant.
	 */
	public static final String YOUTUBE_API_DEVELOPER_KEY = "INSERT_YOUR_DEV_KEY_HERE";
	
	private static final String LOG = HttpUtil.class.getSimpleName();

    public static String getStringResult(String url, List<NameValuePair> postData) {
    	
    	Log.e(LOG, "Contacting URL: " + url);
    	
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpRequestBase request;
			if ( null == postData ) {
				request = new HttpGet(url);
			} else {
				HttpPost postRequest = new HttpPost(url);
				//StringEntity se = new StringEntity(postData);
				UrlEncodedFormEntity se = new UrlEncodedFormEntity(postData, HTTP.UTF_8);
				postRequest.setEntity(se);
				request = postRequest;
			}
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");
			request.setHeader("Accept-Encoding", "gzip");

			HttpResponse response = (HttpResponse) client.execute(request);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				Header contentEncoding = response.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					instream = new GZIPInputStream(instream);
				}
				String resultString = convertStreamToString(instream);
				instream.close();
				return resultString;
			}

		} catch (Exception e) {
			Log.e(LOG, "Error getting results.", e);
		}
		return null;
    	
    	
    }
    
    public static JSONObject getJSONResults(String url) {
    	
		try {
				String resultString = getStringResult(url, null);	
			
				Log.i(LOG, "results = " + resultString);
				//Remove wrapping []
				//resultString = resultString.substring(1, resultString.length() - 1);

				JSONObject jsonObjRecv = new JSONObject(resultString);

				return jsonObjRecv;

		} catch (Exception e) {
			Log.e(LOG, "Error getting results.", e);
		}
		return null;
    	
    	
    }

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
