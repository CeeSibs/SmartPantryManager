package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.smartpantrymanager.data.PantryDatabase;
import com.example.smartpantrymanager.data.PantryItem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private PantryDatabase database;
    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = Room.databaseBuilder(
                getApplicationContext(),
                PantryDatabase.class,
                "pantry_database"
        ).build();

        EditText itemName = findViewById(R.id.etItemName);
        EditText quantity = findViewById(R.id.etQuantity);
        EditText category = findViewById(R.id.etCategory);
        EditText expiryDate = findViewById(R.id.etExpiryDate);

        Button addButton = findViewById(R.id.btnAddItem);
        Button viewButton = findViewById(R.id.btnViewItems);

        addButton.setOnClickListener(v -> {

            String name = itemName.getText().toString().trim();
            String quantityText = quantity.getText().toString().trim();
            String categoryText = category.getText().toString().trim();
            String expiryText = expiryDate.getText().toString().trim();

            if (name.isEmpty() ||
                    quantityText.isEmpty() ||
                    categoryText.isEmpty() ||
                    expiryText.isEmpty()) {

                Toast.makeText(
                        MainActivity.this,
                        "Please fill in all fields",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            int quantityValue;

            try {
                quantityValue = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {

                Toast.makeText(
                        MainActivity.this,
                        "Please enter a valid quantity",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            PantryItem item = new PantryItem(
                    0,
                    name,
                    quantityValue,
                    categoryText,
                    expiryText
            );

            executorService.execute(() -> {

                database.pantryDao().insertItem(item);

                runOnUiThread(() -> {

                    Toast.makeText(
                            MainActivity.this,
                            "Item added successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    itemName.setText("");
                    quantity.setText("");
                    category.setText("");
                    expiryDate.setText("");
                });
            });
        });
        viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ItemsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}