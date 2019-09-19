package retrofit.anew.winnertourist;



import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private ImageView myMarker;
    private FloatingActionButton floatingActionButton;
    private FloatingActionButton backFab;
    private View locationButton;
    private SupportMapFragment mapFragment;
    private TextView searchInput;
    private ImageView searchIcon;
    private Location currentLoc;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myMarker = findViewById(R.id.myMarker);
        backFab = findViewById(R.id.back_btn);
        searchInput = findViewById(R.id.address_search_txt);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.myLocationButton);
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        searchIcon =findViewById(R.id.search_icon);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnMyLocationClickListener(onMyLocationClickListener);
        enableMyLocationIfPermitted();
        mMap.setMinZoomPreference(5.0f);
        //choose location
        myMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
                mMap.getUiSettings().setAllGesturesEnabled(false);
                backFab.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, latLng + "", Toast.LENGTH_LONG).show();
            }
        });

        //magnifier on click
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animationShow();
                init();
            }
        });

        //done btn onclick
        searchInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    animationShow();
                    init();
                    return true;
                }
                return false;
            }
        });

        backFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.getUiSettings().setAllGesturesEnabled(true);
                backFab.setVisibility(View.GONE);
            }
        });
    }

    private void init() {
        //city Name init by location
        String searchString = searchInput.getText().toString();
        Geocoder geocoder = new Geocoder(mapFragment.getContext());
        //search
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            System.out.println("addressInfo" + address.toString());
            LatLng coordinate = new LatLng(address.getLatitude(), address.getLongitude()); //Store these lat lng values somewhere. These should be constant.
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                    coordinate, 18);
            mMap.animateCamera(location);
        } else {
            Toast.makeText(MainActivity.this, "مکان مورد نظر شما پیدا نشد", Toast.LENGTH_LONG).show();
        }
    }
    //magnifier animation
    private void animationShow() {
        ObjectAnimator  rotateAnim = ObjectAnimator.ofFloat(searchIcon , "rotation" , 0f , 360f);
        rotateAnim.setDuration(700);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotateAnim);
        animatorSet.start();
    }

    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            floatingActionButton.setVisibility(View.GONE);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            showDefaultLocation();
            floatingActionButton.setVisibility(View.VISIBLE);
            currentLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            mMap.setMyLocationEnabled(true);

            locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            if (locationButton != null)
                locationButton.setVisibility(View.GONE);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (locationButton != null)
                        locationButton.callOnClick();
                }
            });
        }
    }

    private void showDefaultLocation() {
        Toast.makeText(this, "Location permission not granted, " +
                        "showing default location",
                Toast.LENGTH_SHORT).show();
        LatLng shrine = new LatLng(34.6418764575, 50.87900737300);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((shrine), 16.0f));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocationIfPermitted();
                } else {
                    showDefaultLocation();
                }
                return;
            }
        }
    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mMap.setMinZoomPreference(5.0f);
                    return false;
                }
            };

    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
            new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {
                    mMap.setMinZoomPreference(13.0f);
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(new LatLng(location.getLatitude(),
                            location.getLongitude()));
                }
            };
}
