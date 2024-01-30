package com.bizlijakaria.cheackairquality;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AirviewActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    ProgressBar CoprogressBar1, Pm25progressBar2, No2progressBar3, O3progressBar4, so2progressBar5, NoprogressBar6;
    private Geocoder geocoder;
    private TextView co, no, no2, o3, so2, pm2_5,AQI,AIECondition,Location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airview);



        // Initialize TextViews
        co = findViewById(R.id.co);
        no = findViewById(R.id.no);
        no2 = findViewById(R.id.no2);
        o3 = findViewById(R.id.o3);
        so2 = findViewById(R.id.so2);
        pm2_5 = findViewById(R.id.pm2_5);
        AQI=findViewById(R.id.textview2);
        AIECondition=findViewById(R.id.textView5);
        Location=findViewById(R.id.textView);
        CoprogressBar1 = findViewById(R.id.progressBar);
        Pm25progressBar2 = findViewById(R.id.progressBar6);
        No2progressBar3 = findViewById(R.id.progressBar5);
        O3progressBar4 = findViewById(R.id.progressBar3);
        so2progressBar5 = findViewById(R.id.progressBar4);
        NoprogressBar6 = findViewById(R.id.progressBar2);
        CoprogressBar1.setMax(2000);
        Pm25progressBar2.setMax(50);
        No2progressBar3.setMax(60);
        O3progressBar4.setMax(150);
        so2progressBar5 .setMax(200);
        NoprogressBar6.setMax(50);


        // Initialize FusedLocationProviderClient and Geocoder
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    getAddressFromLocation(latitude, longitude);
                    // Update API link with latitude and longitude
                    updateAPI(latitude, longitude);
                }
            }
        });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String areaName = address.getSubLocality();
                String cityName = address.getLocality(); // or address.getAdminArea() depending on your preference
                String countryName = address.getCountryName();

                // Concatenate the address components into a single string
                String formattedAddress = "";
                if (areaName != null) {
                    formattedAddress += areaName + ", ";
                }
                if (cityName != null) {
                    formattedAddress += cityName + ", ";
                }
                if (countryName != null) {
                    formattedAddress += countryName;
                }

                // Set the formatted address to your TextView
                Location.setText(formattedAddress);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAPI(double latitude, double longitude) {
        String apiKey = "210fbf9674a021a25b20d456a43e1c88";
        String apiLink = "https://api.openweathermap.org/data/2.5/air_pollution?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey;


        // Check network availability
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is available, proceed with the request
            makeApiRequest(apiLink);
        } else {
            // Network is not available
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeApiRequest(String apiLink) {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiLink,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("API Response", response);
                        try {
                            // Parse JSON response
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray listArray = jsonObject.getJSONArray("list");
                            if (listArray.length() > 0) {
                                JSONObject listItem = listArray.getJSONObject(0); // Assuming you only need data from the first item
                                JSONObject components = listItem.getJSONObject("components");
                                JSONObject jsonMain=listItem.getJSONObject("main");
                                int aqi=jsonMain.getInt("aqi");
                                double coValue = components.getDouble("co");
                                double noValue = components.getDouble("no");
                                double no2Value = components.getDouble("no2");
                                double o3Value = components.getDouble("o3");
                                double so2Value = components.getDouble("so2");
                                double pm2_5Value = components.getDouble("pm2_5");


                                CoprogressBar1.setProgress((int) coValue);
                                NoprogressBar6.setProgress((int) noValue);
                                O3progressBar4.setProgress((int) o3Value);
                                so2progressBar5 .setProgress((int) so2Value);
                                No2progressBar3.setProgress((int) no2Value);
                                Pm25progressBar2.setProgress((int) pm2_5Value);




                                // Update TextViews with respective values
                                co.setText("CO\n"+String.valueOf(coValue));
                                no.setText("NO\n"+String.valueOf(noValue));
                                no2.setText("NO2\n"+String.valueOf(no2Value));
                                o3.setText("O3\n"+String.valueOf(o3Value));
                                so2.setText("SO2\n"+String.valueOf(so2Value));
                                pm2_5.setText("pm2_5\n"+String.valueOf(pm2_5Value));
                                AQI.setText(String.valueOf("AQI INDEX= "+aqi));
                                if(aqi==1)
                                {
                                    AIECondition.setText("Air Condition Good");
                                }else if(aqi==2)
                                {
                                    AIECondition.setText("Air Condition Fair");
                                }else if(aqi==3)
                                {
                                    AIECondition.setText("Air Condition Moderate");
                                }else if(aqi==4)
                                {
                                    AIECondition.setText("Air Condition Poor");
                                }else
                                {
                                    AIECondition.setText("Air Condition very poor");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("API Error", "Error parsing JSON response");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    Log.e("API Error", "TimeoutError: Unable to connect to the server");
                } else {
                    Log.e("API Error", "VolleyError: " + error.toString());
                }
            }
        });

        // Set retry policy
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
