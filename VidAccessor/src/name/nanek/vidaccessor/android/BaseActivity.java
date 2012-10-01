package name.nanek.vidaccessor.android;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BaseActivity extends Activity {
	
	private static final String LOG = BaseActivity.class.getSimpleName();
	
	protected static final String RATINGS_ERROR = "Sorry. There was an error contacting the ratings server. Please try again later.";
	
	protected static final String YOUTUBE_ERROR = "Sorry. There was an error contacting YouTube. Please try again later.";
	
	protected ProgressDialog progressDialog;
	
	private Handler showErrorDialogHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			showErrorDialog((String) msg.obj);
		}
	};
	
	protected void showErrorDialogFromOtherThread(final String errorMessage) {
		Message uiMessage = showErrorDialogHandler.obtainMessage(0, errorMessage);
		showErrorDialogHandler.sendMessage(uiMessage);
	}

	protected void showErrorDialog(final String message) {
		if ( null != progressDialog && progressDialog.isShowing() ) {
			progressDialog.dismiss();
		}
		Dialog errorDialog = new AlertDialog.Builder(this).setMessage(message).create();
		errorDialog.setOnDismissListener(new OnDismissListener() {			
			@Override
			public void onDismiss(DialogInterface dialog) {
				BaseActivity.this.finish();
			}
		});
		errorDialog.show();
	}
	
	protected void showProgressDialog() {
		if ( null == progressDialog ) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(getString(R.string.results_loading));    	
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					BaseActivity.this.finish();
				}
			});
		}
		if ( !progressDialog.isShowing() ) {
			progressDialog.show();
		}
	}
	
	public static final String encode(String value) {
		if ( null == value ) return "";
		
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(LOG, "Error encoding parameter with value: " + value, e);
			return "";
		}		
	}
}
