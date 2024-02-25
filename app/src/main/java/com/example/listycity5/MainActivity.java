package com.example.listycity5;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ListView cityList;
    ArrayList<City> cityDataList;
    CityArrayAdapter cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    EditText newCityName;
    EditText newProvinceName;
    Button addCityButton;
    public static String CITY_COLLECTION = "cities";
    public static final String ProvinceKey = "province";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection(CITY_COLLECTION);

        cityList = findViewById(R.id.city_list);
        newCityName = findViewById(R.id.city_name);
        newProvinceName = findViewById(R.id.province_name);
        addCityButton = findViewById(R.id.add_city_button);
        cityDataList = new ArrayList<>();
        //addCitiesInit();

        cityArrayAdapter = new CityArrayAdapter(this, cityDataList);
        cityList.setAdapter(cityArrayAdapter);

        addNewCity();

        cityList.setOnItemLongClickListener((parent, view, position, id) -> {
            City city = cityDataList.get(position);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete City")
                    .setMessage("Are you sure you want to delete " + city.getCityName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteCity(city.getCityName());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });

        citiesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    cityDataList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String city = doc.getId();
                        String province = doc.getString(ProvinceKey);
                        cityDataList.add(new City(city, province));
                        Log.d("Firestore", String.format("City(%s, %s) fetched", city, province));
                    }
                    cityArrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void deleteCity(String cityName) {
        citiesRef.document(cityName).delete().addOnSuccessListener(aVoid -> {
            Log.d("DEBUG", "successfully deleted from Firestore");
        });
    }

    /**
     * Adds the initial city objects to the ArrayList
     */
    private void addCitiesInit() {
        String[] cities = {"Edmonton", "Vancouver", "Toronto", "Hamilton", "Denver", "Los Angeles"};
        String[] provinces = {"AB", "BC", "ON", "ON", "CO", "CA"};
        for (int i = 0; i < cities.length; i++) {
            cityDataList.add(new City(cities[i], provinces[i]));
        }
    }

    private void addNewCity() {
        addCityButton.setOnClickListener(v -> {
            String cityName = newCityName.getText().toString().trim();
            String provinceName = newProvinceName.getText().toString().trim();

            if (cityName.isEmpty() || provinceName.isEmpty()) {
                Log.d("AddNewCity", "City name or province name is empty.");
                return;
            }

            HashMap<String, String> data = new HashMap<>();
            data.put(ProvinceKey, provinceName);
            data.put("Country", "Canada");
            citiesRef.document(cityName)
                    .set(data)
                    .addOnSuccessListener(unused -> {
                        Log.d("DEBUG", "City added successfully");
                        newCityName.setText("");
                        newProvinceName.setText("");
                    })
                    .addOnFailureListener(e -> Log.d("DEBUG", "Error adding city", e));
        });
    }
}
