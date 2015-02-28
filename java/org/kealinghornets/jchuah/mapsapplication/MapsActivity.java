package org.kealinghornets.jchuah.mapsapplication;


import android.net.ConnectivityManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;

import android.support.v4.app.FragmentActivity;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.common.AccountPicker;
import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;
import android.accounts.AccountManager;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, 
  GoogleApiClient.OnConnectionFailedListener, LocationListener, GetUsernameTask.TokenListener, OnMapLoadedCallback {


    protected GoogleMap mMap; // Might be null if Google Play services APK is not available
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
  	 private Location mCurrentLocation = null;
    private LocationRequest mLocationRequest;
	 private boolean mRequestingLocationUpdates = true;
    private boolean mResolvingError = false;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    
    private static final int REQUEST_RESOLVE_USER_RECOVERABLE_AUTH_ERROR = 1002;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error

    protected String OAuthToken;
    protected String mEmail;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

            mEmail = savedInstanceState.getString(STATE_EMAIL);
            OAuthToken = savedInstanceState.getString(STATE_OAUTHTOKEN);
          
        }
        createLocationRequest();
        setContentView(R.layout.fragment_maps);
        buildGoogleApiClient();
        setUpMapIfNeeded();
    }

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    private void pickUserAccount() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                null, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    protected synchronized void buildGoogleApiClient() {
      mGoogleApiClient = new GoogleApiClient.Builder(this)
      .addConnectionCallbacks(this)
      .addOnConnectionFailedListener(this)
      .addApi(LocationServices.API)
      .build();
    } 
  


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    		setUpMapIfNeeded();
    }
    
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
              	 mMap.setOnMapLoadedCallback(this);
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    
    private void setUpMap() {
      mMap.setMyLocationEnabled(true);
      

       // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
           
        }
    	  if (mRequestingLocationUpdates) {
        		startLocationUpdates(); 

        }        
    }
  
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO Auto-generated method stub
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        } 
        
        
    }
    
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }
    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MapsActivity)getActivity()).onDialogDismissed();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
      if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
        // Receiving a result from the AccountPicker
        if (resultCode == RESULT_OK) {
            mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            // With the account name acquired, go get the auth token
            getUsername();
        } else if (resultCode == RESULT_CANCELED) {
            // The account picker dialog closed without selecting an account.
            // Notify users that they must pick an account to proceed.
            Toast.makeText(this, "Choose an account with map access", Toast.LENGTH_SHORT).show();
        }
    	}
      if (requestCode == REQUEST_RESOLVE_USER_RECOVERABLE_AUTH_ERROR) {
        if (resultCode == RESULT_OK) {
          getUsername();
        }
      }
    }
    

	 private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    
    public void getUsername() {
        if (mEmail == null) {
        		pickUserAccount();
    	  } else {
            if (isDeviceOnline()) {
                new GetUsernameTask(MapsActivity.this, mEmail, SCOPE, this).execute();
            } else {
                Toast.makeText(this, "You are not online", Toast.LENGTH_LONG).show();
            }
    	  }
    }
    
    public boolean isDeviceOnline() {
    		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
      	return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        mLastLocation = mCurrentLocation;
        mCurrentLocation = location;
    }

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
	 private static final String STATE_EMAIL = "org.kealinghornets.jchuah.mapsapplication.STATE_EMAIL";
    private static final String STATE_OAUTHTOKEN = "org.kealinghornets.jchuah.mapsapplication.STATE_OAUTHTOKEN";
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        outState.putString(STATE_EMAIL, mEmail);
        outState.putString(STATE_OAUTHTOKEN, OAuthToken);
    }

    @Override
    public void onFetchToken(String token) {
        // TODO Auto-generated method stub
        OAuthToken = token;
        Toast.makeText(this, "OAuthToken granted", Toast.LENGTH_LONG).show();
        
    }

    @Override
    public void onFetchTokenException(Exception e) {
        // TODO Auto-generated method stub
        OAuthToken = null;

        if (e instanceof UserRecoverableAuthException) {
		       startActivityForResult(
                  ((UserRecoverableAuthException)e).getIntent(),
                  REQUEST_RESOLVE_USER_RECOVERABLE_AUTH_ERROR);
          
        }
    }

    @Override
    public void onMapLoaded() {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Map loaded", Toast.LENGTH_SHORT).show();
        if (mEmail == null || OAuthToken == null) {
          pickUserAccount();
        }
    }
    
}
