package tptest.test.esgi.com.testgooglemap;

import android.Manifest;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity
        extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
{

    private GoogleApiClient googleApiClient;

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .build();

        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);


    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (googleApiClient != null)
        {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (googleApiClient != null && googleApiClient.isConnected())
        {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        /*
        // How to modify settings outside XML attr
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(48.8534100, 2.3488000)));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.8534100, 2.3488000), 10));
        // just change zomm level
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        */
        this.googleMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap
                .addMarker(new MarkerOptions().position(new LatLng(48.8534100, 2.3488000))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title("Paris").rotation(45.0f).draggable(true));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(10, 10)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("Nigeria"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(40, 40)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.audio_test)).title("Turquie"));
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if ("Paris".equals(marker.getTitle()))
        {
            Toast.makeText(getApplicationContext(), "Paris", Toast.LENGTH_LONG).show();
        }

        return false;

    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {

    }

    @Override
    public void onMarkerDrag(Marker marker)
    {

    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {

    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        Toast.makeText(getApplicationContext(), "You clicked on " + marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        MainActivityPermissionsDispatcher.goToLastPositionWithPermissionCheck(this);
        lookForPlaces();

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void goToLastPosition()
    {
        final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        final LatLng pos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        final Marker marker = googleMap.addMarker(new MarkerOptions().position(pos).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("You ae HERE !"));

        marker.showInfoWindow();
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRationaleForLocation(final PermissionRequest request)
    {
        new AlertDialog.Builder(this)
                .setMessage("Please accept permission ")
                .setPositiveButton("ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        request.proceed();
                    }
                })
                .setNegativeButton("cancel", (new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        request.cancel();
                    }
                }))
                .show();
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void lookForPlaces()
    {
        final PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(googleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>()
        {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces)
            {
                for (PlaceLikelihood placeLikelihood : likelyPlaces)
                {
                    Log.i("thomasecalle",
                            String.format("Place '%s' has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));
                }
                likelyPlaces.release();
            }
        });
    }
}
