package com.example.tp2_inf8405;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;

public class MapsActivity extends AppCompatActivity
        implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private GoogleMap map;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ENABLE_BLUETOOTH = 2;
    private boolean locationPermissionGranted;
    private boolean bluetoothPermissionGranted;
    private LinearLayout bluetooth_layout;
    private LinearLayout device_info_layout;
    private ArrayList<TextView> bluetoothList;
    private HashMap<String, String[]> devices = new HashMap<String, String[]>();
    static boolean isDayMode = true;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    private View view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothList = new ArrayList<>();
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.setOnMarkerClickListener(this);

        new CountDownTimer(1500, 1000) { // Create a time for 3 seconds for the pop up to automatically disappear
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }

            public void onFinish() {
                // Prompt the user for permission.
                getLocationPermission();

                // Turn on the My Location layer and the related control on the map.
                updateLocationUI();

                // Get the current location of the device and set the position of the map.
                getDeviceLocation();
                // Set a listener for marker click.
                bluetooth_layout = findViewById(R.id.bluetooth_list);
                scanBluetooth();
            }
        }.start();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                BluetoothClass deviceClass = device.getBluetoothClass();
                int classNo = deviceClass.getDeviceClass();
                if (classNo != 7936) {
                    AddDevice(device.getName(), deviceClass.getDeviceClass(), device.getAddress(), device.getBondState(), device.hashCode(), device.getType());
                    AddMarker(device.getName(), deviceClass.getDeviceClass(), device.getAddress(), device.getBondState(), device.hashCode(), device.getType());
                }
            }
        }
    };

    private void AddMarker(String deviceName, int deviceClass, String deviceAddress, int deviceBondState, int deviceHashCode, int deviceType) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.getResult();
                    if (lastKnownLocation != null) {
                        LatLng deviceLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(deviceLocation)
                                .title(deviceName));
                        marker.setTag(deviceAddress);
                    }
                } else {
                    map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                    map.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        });
    }

    private void AddDevice(String deviceName, int deviceClass, String deviceAddress, int deviceBondState, int deviceHashCode, int deviceType) {
        TextView elementName = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        elementName.setLayoutParams(params);
        elementName.setClickable(true);
        // inflate the layout of the popup window
        elementName.setOnClickListener(view -> this.bluetoothWindow(view, deviceAddress));

        String[] info = {deviceName, String.valueOf(deviceClass), deviceAddress, String.valueOf(deviceBondState), String.valueOf(deviceType)};
        devices.put(deviceAddress, info);
        String deviceInfo = deviceName + "\n" + deviceAddress + "\n___________________________________";

        elementName.setText(deviceInfo);
        bluetooth_layout.addView(elementName);
    }

    private void scanBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return;

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED) {
            bluetoothPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH},
                    PERMISSIONS_REQUEST_ENABLE_BLUETOOTH);
        }

        bluetoothAdapter.startDiscovery();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Retrieve the data from the marker.
        this.bluetoothWindow(findViewById(R.id.map_view), (String) marker.getTag());

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    public void bluetoothWindow(View view, String id) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.on_bluetooth_click, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        device_info_layout = popupView.findViewById(R.id.bluetooth_device_info);

        putDeviceInfo(id);
    }

    public void onShare(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello world from TP2-INF8405 :)");
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
        }
        if(isDayMode){
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.day_theme));
        } else {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_theme));
        }
    }

    @SuppressLint("ResourceType")
    public void onChangeThemeButton(View view){
        int nightModeFlags =
                view.getContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isDayMode = true;
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isDayMode = false;
                break;
        }
    }

    public void onDirectionsButtonClick(View view){
        Log.d("directions", Integer.toString(view.getId()));

    }

    public void onFavoritesButtonClick(View view){
        Log.d("Favorites", "CLICKED FAVORITES");

    }

    public void onShareButtonClick(View view){
        Log.d("Share", "CLICKED SHARE");
    }

    public void putDeviceInfo(String id){
        TextView elementName = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        elementName.setLayoutParams(params);
        String[] info = devices.get(id);
        String deviceInfo = info[0] + "\n" + info[1] + "\n" + info[2] + "\n" + info[3] + "\n" + info[4];

        elementName.setText(deviceInfo);
        device_info_layout.addView(elementName);
        device_info_layout.setTag(id);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}
