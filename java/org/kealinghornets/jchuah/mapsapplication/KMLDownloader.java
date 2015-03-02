package org.kealinghornets.jchuah.mapsapplication;


import org.apache.http.Header;

import com.google.android.gms.maps.GoogleMap;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class KMLDownloader {

  
  public void load(GoogleMap map, String url) {
  		this.mMap = map;
    	this.mUrl = url;
  }
  
  class KMLHandler extends AsyncHttpResponseHandler {
    @Override
    public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                          Throwable arg3) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] body) {
      // TODO Auto-generated method stub
      if (statusCode == 200) {

      }
    }
    
  }
  

}
