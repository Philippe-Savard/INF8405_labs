package com.example.tp2_inf8405;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


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
    private HashMap<String, String[]> devices = new HashMap<String, String[]>();
    private ArrayList<Marker> markers = new ArrayList<>();
    private Stack<String> favorites = new Stack<String>();
    private static Boolean isFavoriteView = false;
    private static Boolean threadStarted = false;
    static boolean isDayMode = true;
    SharedPreferences sharedPref;
    Thread newThread;


    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        hideToolBr();
        sharedPref = getSharedPreferences("BluetoothDevices", MODE_PRIVATE);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        newThread = new Thread(() -> {
            try {
                scanBluetooth();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    // Hide the view of top tool bar with TP1 name on it
    public void hideToolBr() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
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
                FetchBluetoothDevices();
                bluetooth_layout = findViewById(R.id.bluetooth_list);
                if (!threadStarted) {
                    newThread.start();
                    threadStarted = true;
                }
            }
        }.start();
    }

    private void FetchBluetoothDevices() {
        Map<String, String> rawBluetoothDevices = (Map<String, String>) sharedPref.getAll();
        for (Map.Entry<String, String> device : rawBluetoothDevices.entrySet()) {
            devices.put(device.getKey(), device.getValue().split(","));
            if (CheckFavorite(device.getKey()))
                favorites.push(device.getKey());
            AddDevice(device.getKey());
            PlaceMarker(device.getKey(), new LatLng(Double.parseDouble(devices.get(device.getKey())[6]), Double.parseDouble(devices.get(device.getKey())[7])));
        }
    }

    private Boolean CheckFavorite(String deviceAddress) {
        String deviceInfo = sharedPref.getString(deviceAddress, null);
        String[] infoArray = deviceInfo.split(",");
        return Boolean.parseBoolean(infoArray[5]);
    }

    private void UpdateFavorites() {
        bluetooth_layout.removeAllViews();
        for (HashMap.Entry<String, String[]> device : devices.entrySet()) {
            if (Boolean.parseBoolean(device.getValue()[5]) || !isFavoriteView)
                AddDevice(device.getKey());
        }
    }

    public void ToggleFavorites(View view) {
        isFavoriteView = !isFavoriteView;
        UpdateFavorites();
    }

    private void ChangeFavorite(String deviceAddress) {
        String deviceInfo = sharedPref.getString(deviceAddress, null);
        String[] infoArray = deviceInfo.split(",");
        Boolean favorite = !CheckFavorite(deviceAddress);
        if (favorite)
            favorites.push(deviceAddress);
        else
            favorites.remove(deviceAddress);
        devices.get(deviceAddress)[5] = String.valueOf(favorite);
        String device = infoArray[0] + "," + infoArray[1] + "," + infoArray[2] + "," + infoArray[3] + "," + infoArray[4] + "," + favorite + "," + infoArray[6] + "," + infoArray[7];
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(deviceAddress, device);
        editor.apply();
    }

    private void SaveBluetoothDevice(String deviceName, String deviceClass, String deviceAddress, String deviceBondState, String deviceType) {
        sharedPref = getSharedPreferences("BluetoothDevices", MODE_PRIVATE);
        if (!sharedPref.contains(deviceAddress)) {
            if ( !devices.containsKey(deviceAddress)){
                String[] info = {deviceName, deviceClass, deviceAddress, deviceBondState, deviceType, "false"};
                devices.put(deviceAddress, info);
                AddDevice(deviceAddress);
            }
        }
        else {
            ChangeMarker(deviceAddress);
        }


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
                        PlaceMarker(deviceAddress, deviceLocation);
                        String[] tempDevice = devices.get(deviceAddress);
                        devices.put(deviceAddress, new String[]{tempDevice[0], tempDevice[1], tempDevice[2], tempDevice[3], tempDevice[4], tempDevice[5], String.valueOf(lastKnownLocation.getLatitude()), String.valueOf(lastKnownLocation.getLongitude())});
                    }
                } else {
                    map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                    map.getUiSettings().setMyLocationButtonEnabled(false);
                }

                String[] moreInfo = devices.get(deviceAddress);
                String device = moreInfo[0] + "," + moreInfo[1] + "," + moreInfo[2] + "," + moreInfo[3] + "," + moreInfo[4] + "," + moreInfo[5] + "," + moreInfo[6] + "," + moreInfo[7];
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(deviceAddress, device);
                editor.apply();
            }
        });
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
                    SaveBluetoothDevice(device.getName(), DeviceInfoConverter.ConvertDeviceClass(classNo), device.getAddress(), DeviceInfoConverter.ConvertBondState(device.getBondState()), DeviceInfoConverter.ConvertType(device.getType()));
                }
            }
        }
    };

    private void PlaceMarker(String deviceAddress, LatLng location) {
        Marker marker = map.addMarker(new MarkerOptions()
                .position(location)
                .title(devices.get(deviceAddress)[0]));
        marker.setTag(deviceAddress);
        markers.add(marker);
    }

    private void ChangeMarker(String deviceAddress) {
        for (Marker marker: markers) {
            if (marker.getTag().equals(deviceAddress)) {
                marker.remove();
                markers.remove(marker);
                return;
            }
        }
    }

    private void AddDevice(String deviceAddress) {
        TextView elementName = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        elementName.setLayoutParams(params);
        elementName.setClickable(true);
        // inflate the layout of the popup window
        elementName.setOnClickListener(view -> this.bluetoothWindow(view, deviceAddress));
        String deviceInfo = devices.get(deviceAddress)[0] + "\n" + deviceAddress + "\n___________________________________";

        elementName.setText(deviceInfo);
        bluetooth_layout.addView(elementName);
}

    private void scanBluetooth() throws InterruptedException {
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
        while(true) {
            bluetoothAdapter.startDiscovery();
            TimeUnit.SECONDS.sleep(15);
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Retrieve the data from the marker.
        this.bluetoothWindow(findViewById(R.id.layout_map_view), (String) marker.getTag());

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
        } catch (SecurityException e) {
            e.printStackTrace();
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

    private void DisplayTrack(LatLng start, String endLatitude, String endLongitude){
        // If the device does not have a map installed redirect to to google maps
        String startLatitude = String.valueOf(start.latitude);
        String startLongitude = String.valueOf(start.longitude);
        try {
            // when google maps is installed initialise uri
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir/" + startLatitude + "," + startLongitude + "/" + endLatitude + "," + endLongitude);
            //Initialise intent with action view
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            //Set package
            intent.setPackage("com.google.android.apps.maps");
            //Set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //start activity
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            // when google maps is not installed
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps%22");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    public void onDirectionsButtonClick(View view){
        String deviceAddress = (String) device_info_layout.getTag();
        DisplayTrack(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), devices.get(deviceAddress)[6], devices.get(deviceAddress)[7]);
    }

    public void onFavoritesButtonClick(View view){
        String deviceAddress = (String) device_info_layout.getTag();
        ChangeFavorite(deviceAddress);
        device_info_layout.removeAllViews();
        putDeviceInfo(deviceAddress);
        UpdateFavorites();
    }

    public void onShareButtonClick(View view) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, PrettyPrint((String) device_info_layout.getTag()));
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }


    public String PrettyPrint(String deviceAddress) {
        String[] info = devices.get(deviceAddress);
        return "- Device Name: " + info[0] + "\n" + "- Device Class: " + info[1] + "\n"+ "- MAC address: " + info[2] + "\n" +
                "- Bond state of the device: " + info[3] + "\n" + "- Type of bluetooth device: " + info[4] + "\n" + "- Favorites: " + info[5];
    }

    public void putDeviceInfo(String id){
        TextView elementName = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        elementName.setLayoutParams(params);
        String[] info = devices.get(id);
        assert info != null;
        String deviceInfo = PrettyPrint(id);

        elementName.setText(deviceInfo);
        elementName.setTextColor(Color.CYAN);
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
