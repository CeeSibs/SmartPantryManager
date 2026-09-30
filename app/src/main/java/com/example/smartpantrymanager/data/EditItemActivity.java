package com.example.smartpantrymanager.data;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.smartpantrymanager.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditItemActivity extends AppCompatActivity {

    private PantryDatabase database;
    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        EditText name = findViewById(R.id.etEditName);
        EditText quantity = findViewById(R.id.etEditQuantity);
        EditText category = findViewById(R.id.etEditCategory);
        EditText expiryDate = findViewById(R.id.etEditExpiryDate);
        Button updateButton = findViewById(R.id.btnUpdateItem);

        itemId = getIntent().getIntExtra("item_id", -1);

        name.setText(getIntent().getStringExtra("item_name"));
        quantity.setText(String.valueOf(
                getIntent().getIntExtra("item_quantity", 0)
        ));
        category.setText(getIntent().getStringExtra("item_category"));
        expiryDate.setText(getIntent().getStringExtra("item_expiry"));

        database = Room.databaseBuilder(
                getApplicationContext(),
                PantryDatabase.class,
                "pantry_database"
        ).build();

        updateButton.setOnClickListener(v -> {

            String newName = name.getText().toString().trim();
            String quantityText = quantity.getText().toString().trim();
            String newCategory = category.getText().toString().trim();
            String newExpiry = expiryDate.getText().toString().trim();

            if (newName.isEmpty() ||
                    quantityText.isEmpty() ||
                    newCategory.isEmpty() ||
                    newExpiry.isEmpty()) {

                Toast.makeText(
                        EditItemActivity.this,
                        "Please fill in all fields",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            int newQuantity;

            try {
                newQuantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {

                Toast.makeText(
                        EditItemActivity.this,
                        "Please enter a valid quantity",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            PantryItem updatedItem = new PantryItem(
                    itemId,
                    newName,
                    newQuantity,
                    newCategory,
                    newExpiry
            );

            executorService.execute(() -> {

                database.pantryDao().updateItem(updatedItem);

                runOnUiThread(() -> {

                    Toast.makeText(
                            EditItemActivity.this,
                            "Item updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}