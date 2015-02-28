package org.kealinghornets.jchuah.mapsapplication;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by jychuah on 2/28/15.
 */
public class OAuthAwareDownloader {
    public static void get(String url, RequestParams params, String OAuthToken, AsyncHttpResponseHandler responseHandler) {
        genClient(OAuthToken).get(url, params, responseHandler);
    }
    public static void post(String url, RequestParams params, String OAuthToken, AsyncHttpResponseHandler responseHandler) {
        genClient(OAuthToken).post(url, params, responseHandler);
    }
    private static AsyncHttpClient genClient(String OAuthToken) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("bearer", OAuthToken);
        return client;
    }
}
