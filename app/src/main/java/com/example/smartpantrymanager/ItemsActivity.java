package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.smartpantrymanager.data.EditItemActivity;
import com.example.smartpantrymanager.data.PantryDatabase;
import com.example.smartpantrymanager.data.PantryItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemsActivity extends AppCompatActivity {

    private PantryDatabase database;
    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private List<PantryItem> allItems;
    private ArrayList<String> itemList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        ListView listViewItems = findViewById(R.id.listViewItems);
        EditText searchBox = findViewById(R.id.etSearch);

        database = Room.databaseBuilder(
                getApplicationContext(),
                PantryDatabase.class,
                "pantry_database"
        ).build();

        executorService.execute(() -> {

            allItems = database.pantryDao().getAllItems();
            itemList = new ArrayList<>();

            for (PantryItem item : allItems) {
                itemList.add(createItemDetails(item));
            }

            runOnUiThread(() -> {

                adapter = new ArrayAdapter<>(
                        ItemsActivity.this,
                        android.R.layout.simple_list_item_1,
                        itemList
                );

                listViewItems.setAdapter(adapter);

                searchBox.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        String searchText =
                                s.toString().trim().toLowerCase();

                        itemList.clear();

                        for (PantryItem item : allItems) {

                            if (item.getName().toLowerCase().contains(searchText)
                                    || item.getCategory().toLowerCase().contains(searchText)) {

                                itemList.add(createItemDetails(item));
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                listViewItems.setOnItemClickListener(
                        (parent, view, position, id) -> {

                            String selectedText =
                                    itemList.get(position);

                            PantryItem selectedItem = null;

                            for (PantryItem item : allItems) {

                                if (createItemDetails(item).equals(selectedText)) {
                                    selectedItem = item;
                                    break;
                                }
                            }

                            if (selectedItem == null) {
                                return;
                            }

                            PantryItem finalSelectedItem = selectedItem;

                            new AlertDialog.Builder(ItemsActivity.this)
                                    .setTitle("Choose Action")
                                    .setItems(
                                            new String[]{
                                                    "Edit Item",
                                                    "Delete Item"
                                            },
                                            (dialog, which) -> {

                                                if (which == 0) {

                                                    Intent intent = new Intent(
                                                            ItemsActivity.this,
                                                            EditItemActivity.class
                                                    );

                                                    intent.putExtra(
                                                            "item_id",
                                                            finalSelectedItem.getId()
                                                    );

                                                    intent.putExtra(
                                                            "item_name",
                                                            finalSelectedItem.getName()
                                                    );

                                                    intent.putExtra(
                                                            "item_quantity",
                                                            finalSelectedItem.getQuantity()
                                                    );

                                                    intent.putExtra(
                                                            "item_category",
                                                            finalSelectedItem.getCategory()
                                                    );

                                                    intent.putExtra(
                                                            "item_expiry",
                                                            finalSelectedItem.getExpiryDate()
                                                    );

                                                    startActivity(intent);

                                                } else if (which == 1) {

                                                    new AlertDialog.Builder(
                                                            ItemsActivity.this
                                                    )
                                                            .setTitle("Delete Item")
                                                            .setMessage(
                                                                    "Do you want to delete "
                                                                            + finalSelectedItem.getName()
                                                                            + "?"
                                                            )
                                                            .setPositiveButton(
                                                                    "Delete",
                                                                    (deleteDialog, deleteWhich) -> {

                                                                        executorService.execute(() -> {

                                                                            database.pantryDao()
                                                                                    .deleteItem(finalSelectedItem);

                                                                            runOnUiThread(() -> {

                                                                                allItems.remove(finalSelectedItem);

                                                                                itemList.remove(
                                                                                        selectedText
                                                                                );

                                                                                adapter.notifyDataSetChanged();

                                                                                Toast.makeText(
                                                                                        ItemsActivity.this,
                                                                                        "Item deleted successfully",
                                                                                        Toast.LENGTH_SHORT
                                                                                ).show();
                                                                            });
                                                                        });
                                                                    }
                                                            )
                                                            .setNegativeButton(
                                                                    "Cancel",
                                                                    null
                                                            )
                                                            .show();
                                                }
                                            }
                                    )
                                    .setNegativeButton(
                                            "Cancel",
                                            null
                                    )
                                    .show();
                        }
                );
            });
        });
    }

    private String createItemDetails(PantryItem item) {

        String stockStatus = "";

        if (item.getQuantity() <= 5) {
            stockStatus = "\n⚠ LOW STOCK";
        }

        String expiryStatus = getExpiryStatus(item.getExpiryDate());

        return "Item: " + item.getName()
                + "\nQuantity: " + item.getQuantity()
                + stockStatus
                + "\nCategory: " + item.getCategory()
                + "\nExpiry Date: " + item.getExpiryDate()
                + expiryStatus;
    }

    private String getExpiryStatus(String expiryDateText) {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        dateFormat.setLenient(false);

        try {

            Date expiryDate = dateFormat.parse(expiryDateText);
            Date today = new Date();

            if (expiryDate == null) {
                return "";
            }

            long difference =
                    expiryDate.getTime() - today.getTime();

            long daysRemaining =
                    difference / (1000 * 60 * 60 * 24);

            if (daysRemaining < 0) {

                return "\n🔴 EXPIRED";

            } else if (daysRemaining <= 7) {

                return "\n⚠ EXPIRING SOON";
            }

        } catch (ParseException e) {

            return "\nInvalid expiry date";
        }

        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
