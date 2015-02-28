package org.kealinghornets.jchuah.mapsapplication;

import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class GetUsernameTask extends AsyncTask{
    Activity mActivity;
    TokenListener mTokenListener;
    String mScope;
    String mEmail;
  	 String mToken;
  	 Exception mException;

    GetUsernameTask(Activity activity, String name, String scope, TokenListener listener ) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = name;
        this.mTokenListener = listener;
    }

    /**
     * Executes the asynchronous job. This runs when you call execute()
     * on the AsyncTask instance.
     */
    @Override
    protected Void doInBackground(Object... params) {
        try {
            mToken = fetchToken();
            if (mToken != null) {
                // Insert the good stuff here.
                // Use the token to access the user's Google data.
                mActivity.runOnUiThread(new Runnable() {
                		public void run() {
                				mTokenListener.onFetchToken(mToken);       
                     }
                });
                
            }
        } catch (IOException e) {
            // The fetchToken() method handles Google-specific exceptions,
            // so this indicates something went wrong at a higher level.
            // TIP: Check for network connectivity before starting the AsyncTask.
            mException = e;
				mActivity.runOnUiThread(new Runnable() {
              		public void run() {
               			mTokenListener.onFetchTokenException(mException); 
               	}
            }); 
        }
        return null;
    }

    /**
     * Gets an authentication token from Google and handles any
     * GoogleAuthException that may occur.
     */
    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken((Activity)mActivity, mEmail, mScope);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
//            mActivity.handleException(userRecoverableException);
//            
           
				mException = userRecoverableException;
          	mActivity.runOnUiThread(new Runnable() {
              		public void run() {
            			mTokenListener.onFetchTokenException(mException);        	
                  }
            });
          
           
            
        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
          	mException = fatalException;
          	mActivity.runOnUiThread(new Runnable() {
              		public void run() {
                    	mTokenListener.onFetchTokenException(mException);
                  }
            });
          

        }
        return null;
    }

  	 public interface TokenListener {
    		public void onFetchToken(String s);
     		public void onFetchTokenException(Exception e);
    }

}