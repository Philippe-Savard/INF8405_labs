package com.example.tp2_inf8405;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.hardware.*;



public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    /////////////////////////////////////
    //         MAPS ATTRIBUTES         //
    /////////////////////////////////////
    private GoogleMap map;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;
    // A default location (Sydney, Australia) and default zoom to use when location permission is not granted
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    /////////////////////////////////////
    //      BLUETOOTH ATTRIBUTES       //
    /////////////////////////////////////
    private LinearLayout bluetooth_layout;
    private LinearLayout device_info_layout;
    private static final int PERMISSIONS_REQUEST_ENABLE_BLUETOOTH = 2;

    /////////////////////////////////////
    //      SENSORS ATTRIBUTES         //
    /////////////////////////////////////
    private SensorManager sensorManager;
    protected List<Sensor> deviceSensors;
    private TextView stepCount;
    private Sensor stepSensor;
    private Sensor lightSensor;
    private boolean activityRecongnitionPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION = 2;

    /////////////////////////////////////
    //        OTHER ATTRIBUTES         //
    /////////////////////////////////////
    static boolean isDayMode = true;
    private final ArrayList<Marker> markers = new ArrayList<>();
    private final Stack<String> favorites = new Stack<String>();
    private final HashMap<String, String[]> devices = new HashMap<String, String[]>();
    SharedPreferences sharedPref;
    Thread discoveryThread;
    private static Boolean isFavoriteView = false;
    private static Boolean threadStarted = false;
    private final int UNCATEGORIZED = 7936;
    BottomNavigationView bottomNavigationView;
    private TextView light;
    private TextView txt_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        hideToolBar();

        txt_device = findViewById(R.id.txt_devicetitle);

        bottomNavigationView = findViewById(R.id.nav_bar);
        //bottomNavigationView.setOnItemSelectedListener(this);

        // Get all the available sensors on the device
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        getPhysicalActivityPermission();
        if (activityRecongnitionPermissionGranted) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        sharedPref = getSharedPreferences("BluetoothDevices", MODE_PRIVATE);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        // Create receiver able to collect information upon bluetooth discovery
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        //Create new thread that will only scan for bluetooth devices
        discoveryThread = new Thread(() -> {
            try {
                startBluetoothDiscovery();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        SensorEventListener lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float lux = sensorEvent.values[0];
                light = findViewById(R.id.txt_lux);
                light.setText(String.valueOf(lux));

                if (lux >= 50 && !isDayMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    isDayMode = true;
                }
                if (lux <= 10 && isDayMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    isDayMode = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_UI);
        SensorEventListener stepCounterListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float totalStepCount = sensorEvent.values[0];
                stepCount = findViewById(R.id.txt_podo_count);
                stepCount.setText( String.valueOf(totalStepCount));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        sensorManager.registerListener(stepCounterListener, stepSensor, SensorManager.SENSOR_DELAY_UI);
    }


    /**
     * Manipulates the map when it's available.
     * @param map the map to update
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        this.map.setOnMarkerClickListener(this);

        // Change map's style
        if(isDayMode){
            this.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.day_theme));
        } else {
            this.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_theme));
        }

        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        bluetooth_layout = findViewById(R.id.bluetooth_list);

        fetchDevicesFromStorage(); // Retrieve previously saved bluetooth devices from storage
        if (!threadStarted) {
            discoveryThread.start(); // Start constant bluetooth device discovery for thread previously defined
            threadStarted = true;
        }
        // Should work without the timer now
//        new CountDownTimer(1500, 1000) { // Create a timer to wait for things to render
//            public void onTick(long millisUntilFinished) {
//                // Do nothing
//            }
//
//            public void onFinish() {
//                // Prompt the user for location permission.
//                getLocationPermission();
//
//                // Turn on the My Location layer and the related control on the map.
//                updateLocationUI();
//
//                // Get the current location of the device and set the position of the map.
//                getDeviceLocation();
//                bluetooth_layout = findViewById(R.id.bluetooth_list);
//
//                fetchDevicesFromStorage(); // Retrieve previously saved bluetooth devices from storage
//                if (!threadStarted) {
//                    discoveryThread.start(); // Start constant bluetooth device discovery for thread previously defined
//                    threadStarted = true;
//                }
//            }
//        }.start();
    }

    ////////////////////////////////////////////
    //  FILE MANIPULATION RELATED FUNCTIONS   //
    ////////////////////////////////////////////

    /**
     * Function to get device from the storage
     */
    private void fetchDevicesFromStorage() {
        // Retrieve BluetoothDevices from saved files and create a map with the MAC address as Key value
        Map<String, String> rawBluetoothDevices = (Map<String, String>) sharedPref.getAll();
        for (Map.Entry<String, String> device : rawBluetoothDevices.entrySet()) {
            // Add it to runtime reference of all devices
            devices.put(device.getKey(), device.getValue().split(","));
            if (checkFavorite(device.getKey())) {
                // Adds it to favorites if saved as favorites
                favorites.push(device.getKey());
            }
            // Add device to the view. Maps and side panel
            addDeviceToSideView(device.getKey());
            addMarker(device.getKey(), new LatLng(Double.parseDouble(devices.get(device.getKey())[6]), Double.parseDouble(devices.get(device.getKey())[7])));
        }
    }

    /**
     * Function to save a device to storage
     * @param deviceName name of the device
     * @param deviceClass class of the device
     * @param deviceMACAddress MAC address of the device
     * @param deviceBondState bonding state of the device
     * @param deviceType type of the device
     */
    private void saveDeviceToStorage(String deviceName, String deviceClass, String deviceMACAddress, String deviceBondState, String deviceType) {
        sharedPref = getSharedPreferences("BluetoothDevices", MODE_PRIVATE);
        // Check to see if device exists in storage
        if (!sharedPref.contains(deviceMACAddress)) {
            // Check to see if device exists in runtime saved devices
            if ( !devices.containsKey(deviceMACAddress)){
                // Create string array that holds all device info. Favorite is set to False by default
                String[] info = {deviceName, deviceClass, deviceMACAddress, deviceBondState, deviceType, "false"};
                devices.put(deviceMACAddress, info);
                addDeviceToSideView(deviceMACAddress);
            }
        }
        else { removeMarker(deviceMACAddress); }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        // Get current location
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = task.getResult();
                if (lastKnownLocation != null) {
                    LatLng deviceLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    // Add marker to map
                    addMarker(deviceMACAddress, deviceLocation);
                    // Update info device with location (latitude and longitude)
                    String[] tempDevice = devices.get(deviceMACAddress);
                    devices.put(deviceMACAddress, new String[]{Objects.requireNonNull(tempDevice)[0], tempDevice[1], tempDevice[2],
                            tempDevice[3], tempDevice[4], tempDevice[5], String.valueOf(lastKnownLocation.getLatitude()),
                            String.valueOf(lastKnownLocation.getLongitude())});
                }
            } else { // If we cannot access the current location
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                map.getUiSettings().setMyLocationButtonEnabled(false);
            }

            String[] moreInfo = devices.get(deviceMACAddress);
            assert moreInfo != null;
            String device = moreInfo[0] + "," + moreInfo[1] + "," + moreInfo[2] + "," + moreInfo[3] + "," + moreInfo[4] + "," + moreInfo[5] +
                    "," + moreInfo[6] + "," + moreInfo[7];
            // Save device with all info (device and location) to storage
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(deviceMACAddress, device);
            editor.apply();
        });
    }

    ////////////////////////////////////////////
    //       BLUETOOTH RELATED FUNCTIONS      //
    ////////////////////////////////////////////

    /**
     * Create a BroadcastReceiver for ACTION_FOUND.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                int classNo = device.getBluetoothClass().getDeviceClass();
                if (classNo != UNCATEGORIZED) {
                    // Use the DeviceInfoConverter to convert the class, bond state and type of device
                    saveDeviceToStorage(device.getName(), DeviceInfoConverter.ConvertDeviceClass(classNo), device.getAddress(),
                            DeviceInfoConverter.ConvertBondState(device.getBondState()), DeviceInfoConverter.ConvertType(device.getType()));
                }
            }
        }
    };

    /**
     * Function to start the bluetooth discovery
     * @throws InterruptedException
     */
    private void startBluetoothDiscovery() throws InterruptedException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) return;

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PERMISSIONS_REQUEST_ENABLE_BLUETOOTH);
        }

        while(!Thread.currentThread().isInterrupted()) {
            // Task done in perpetuity by dedicated thread
            bluetoothAdapter.startDiscovery();
            TimeUnit.SECONDS.sleep(15); // Discovery takes 12 seconds so we wait a little extra to ensure no discovery overlap
        }
    }

    ////////////////////////////////////////////
    //       FAVORITES RELATED FUNCTIONS      //
    ////////////////////////////////////////////

    /**
     * Function that checks if the device stored is tagged as favorite or not and returns bool value
     * @param deviceMACAddress the desired MAC address to be checked
     * @return true if the device for the provided MAC address is favorite
     */
    private Boolean checkFavorite(String deviceMACAddress) {
        String deviceInfo = sharedPref.getString(deviceMACAddress, null);
        String[] infoArray = deviceInfo.split(",");
        return Boolean.parseBoolean(infoArray[5]);
    }

    /**
     *    Function that changes the favorite status of a specific bluetooth device.
     */
    private void modifyDeviceFavoriteStatus(String deviceMACAddress) {
        String deviceInfo = sharedPref.getString(deviceMACAddress, null);
        String[] infoArray = deviceInfo.split(",");
        Boolean favorite = !checkFavorite(deviceMACAddress);
        // We either add or take out of favorites based on the current state of the device
        if (favorite)
            favorites.push(deviceMACAddress);
        else
            favorites.remove(deviceMACAddress);
        Objects.requireNonNull(devices.get(deviceMACAddress))[5] = String.valueOf(favorite);
        String device = infoArray[0] + "," + infoArray[1] + "," + infoArray[2] + "," + infoArray[3] + "," + infoArray[4] + ","
                + favorite + "," + infoArray[6] + "," + infoArray[7];
        // Change status in storage
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(deviceMACAddress, device);
        editor.apply();
    }

    ////////////////////////////////////////////
    //         MARKER RELATED FUNCTIONS       //
    ////////////////////////////////////////////

    /**
     * Function to add a device to the device's list
     * @param deviceMACAddress the MAC Address of the device to add to the marker's list
     * @param location position of the device
     */
    private void addMarker(String deviceMACAddress, LatLng location) {
        Marker marker = map.addMarker(new MarkerOptions()
                .position(location)
                .title(Objects.requireNonNull(devices.get(deviceMACAddress))[0]));
        Objects.requireNonNull(marker).setTag(deviceMACAddress);
        markers.add(marker);
    }

    /**
     * Function to remove a tag from the marker's list
     * @param deviceMACAddress the MAC Address of the device to remove
     */
    private void removeMarker(String deviceMACAddress) {
        for (Marker marker: markers) {
            if (Objects.requireNonNull(marker.getTag()).equals(deviceMACAddress)) {
                marker.remove();
                markers.remove(marker);
                return;
            }
        }
    }

    /**
     * onClikEvent for the markers
     * @param marker current marker that have been clicked
     * @return
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Retrieve the data from the marker.
        this.devicePopUpWindow(findViewById(R.id.layout_map_view), (String) marker.getTag());
        return false;
    }

    ////////////////////////////////////////////
    //         VIEW RELATED FUNCTIONS         //
    ////////////////////////////////////////////

    /**
     *  Function that updates the devices on the side view depending if we are in favorite view or not
     */
    private void updateSideViewDevices() {
        // Take out all current devices from the view
        bluetooth_layout.removeAllViews();
        for (HashMap.Entry<String, String[]> device : devices.entrySet()) {
            // Checks if favorite is set to true or if we are in list all devices mode
            if (Boolean.parseBoolean(device.getValue()[5]) || !isFavoriteView)
                addDeviceToSideView(device.getKey());
        }
    }

    /**
     * Add a device to the side view.
     * @param deviceMACAddress the MAC address to add to the side view.
     */
    private void addDeviceToSideView(String deviceMACAddress) {
        TextView elementName = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        elementName.setLayoutParams(params);
        elementName.setClickable(true);
        // Assures to create bluetooth popUp with MAC address upon device click
        elementName.setOnClickListener(view -> this.devicePopUpWindow(view, deviceMACAddress));
        // Only add device name and MAC to side panel view
        String deviceInfo = Objects.requireNonNull(devices.get(deviceMACAddress))[0] + "\n" + deviceMACAddress;

        elementName.setText(deviceInfo);
        bluetooth_layout.addView(elementName); // Add device to the view
    }

    /**
     * Function that retrieves all the device information and displays it on the popupWindow
     * @param deviceMACAddress the MAC address to retrieve to the side view.
     */
    public void displayDeviceInfo(String deviceMACAddress){
        TextView elementName = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        elementName.setLayoutParams(params);
        elementName.setTextSize(14);
        String[] info = devices.get(deviceMACAddress);
        assert info != null;
        String deviceInfo = prettyPrint(deviceMACAddress);

        elementName.setText(deviceInfo);
        device_info_layout.addView(elementName);
        device_info_layout.setTag(deviceMACAddress);
    }

    /**
     * Makes the AlertDialog appear
     * @param view the current view
     * @param deviceMACAddress the MAC address to display.
     */
    public void devicePopUpWindow(View view, String deviceMACAddress) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.on_bluetooth_click, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        device_info_layout = popupView.findViewById(R.id.bluetooth_device_info);
        builder.setView(popupView);

        displayDeviceInfo(deviceMACAddress);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    ///////////////////////////////////////////////////////////
    //         PERMISSION AND MAPS RELATED FUNCTIONS         //
    ///////////////////////////////////////////////////////////

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        }
                    } else {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Prompts the user for permission to use the device physical activity detection.
     */
    private void getPhysicalActivityPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            activityRecongnitionPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     * @param requestCode request code number
     * @param permissions required permissions to be granted
     * @param grantResults result of the request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else if (requestCode == PERMISSIONS_REQUEST_ACCESS_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activityRecongnitionPermissionGranted = true;
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
        } catch (SecurityException ignored)  { }
    }

    //////////////////////////////////////////////
    //         BUTTON RELATED FUNCTIONS         //
    //////////////////////////////////////////////

    /**
     * Function that changes the theme of the application upon change theme button click
     */
    public void onChangeThemeButton(MenuItem item) {
        // Get the current state of day or night mode
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        // Switch based on current state
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

    /**
     * Makes the favorite view appear or disappear
     */
    public void toggleFavorites(MenuItem item) {
        // Set isFavoriteView to the opposite bool value
        isFavoriteView = !isFavoriteView;
        if (isFavoriteView){
            this.txt_device.setText("Favorites");
        } else {
            this.txt_device.setText("Device list");
        }
        updateSideViewDevices();
    }

    /**
     * Change the language
     */
    public void onLanguageChange(MenuItem item) {
        // TODO : Mettre le code pour changer la langue ici
    }

    /**
     * Handles the direction button onClick event in the alertDialog view.
     * @param view the current view
     */
    public void onDirectionsButtonClick(View view){
        String deviceMACAddress = (String) device_info_layout.getTag();
        // Create directions using current location as start and device position as destination
        openGoogleMapsDirections(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()),
                Objects.requireNonNull(devices.get(deviceMACAddress))[6], Objects.requireNonNull(devices.get(deviceMACAddress))[7]);
    }

    /**
     * Handles the favorites button onClick event.
     * @param view the current view
     */
    public void onFavoritesButtonClick(View view){
        String deviceMACAddress = (String) device_info_layout.getTag();
        modifyDeviceFavoriteStatus(deviceMACAddress);
        device_info_layout.removeAllViews();
        displayDeviceInfo(deviceMACAddress);
        updateSideViewDevices();
    }

    /**
     * Handles the share button onClick event.
     * @param view the current view
     */
    public void onShareButtonClick(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        // Use the PrettyPrint output as sharable content
        sendIntent.putExtra(Intent.EXTRA_TEXT, prettyPrint((String) device_info_layout.getTag()));
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    /**
     * Function that opens the Google Map application for directions
     * @param start
     * @param endLatitude
     * @param endLongitude
     */
    private void openGoogleMapsDirections(LatLng start, String endLatitude, String endLongitude){
        // If the device does not have a map installed redirect to to google maps
        String startLatitude = String.valueOf(start.latitude);
        String startLongitude = String.valueOf(start.longitude);
        try {
            // when google maps is installed initialise uri
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir/" + startLatitude + "," + startLongitude + "/" + endLatitude + "," + endLongitude);
            //Initialise intent with action view and start activity
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            // when google maps is not installed
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps%22");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    //////////////////////////////////////
    //         OTHER FUNCTIONS         //
    /////////////////////////////////////

    /**
     * Function that displays the device information in a uniform way.
     * @param deviceMACAddress the MAC address of the device to display
     * @return the string formatted for printing
     */
    public String prettyPrint(String deviceMACAddress) {
        String[] info = devices.get(deviceMACAddress);
        return "- Device Name: " + info[0] + "\n" + "- Device Class: " + info[1] + "\n"+ "- MAC address: " + info[2] + "\n" +
                "- Bond state of the device: " + info[3] + "\n" + "- Type of bluetooth device: " + info[4] + "\n" + "- Favorites: " + info[5];
    }

    /**
     *  Hide the view of top tool bar with TP2 name on it
     */
    public void hideToolBar() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}