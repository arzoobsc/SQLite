package com.ap.sqlite;
//part 4 complete

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    //    views
    private FloatingActionButton addRecordBtn;
    private RecyclerView recordsRv;

    //    DB helper
    private MyDbHelper dbHelper;

    //    action bar
    ActionBar actionBar;

    //    sort options
    String orderByNewest = Constants.C_ADDED_TIMESTAMP + " DESC";
    String orderByOldest = Constants.C_ADDED_TIMESTAMP + " ASC";
    String orderByTitleAsc = Constants.C_NAME + " ASC";
    String orderByTitleDesc = Constants.C_NAME + " DESC";

    //    for refreshing records, refresh with last choosen sort option
    String currentOrderByStatus = orderByNewest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setTitle("All Records");

//        init views
        addRecordBtn = findViewById(R.id.addRecordBtn);
        recordsRv = findViewById(R.id.recordsRv);

//        init db helper class
        dbHelper = new MyDbHelper(this);

//        loads records (by default newest first)
        loadRecords(orderByNewest);

//        click to start add record activity
        addRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                start add record activity
                Intent intent = new Intent(MainActivity.this, AddUpdateRecordActivity.class);
                intent.putExtra("isEditMode", false);   //  want to add new data, set false
                startActivity(intent);
            }
        });
    }

    private void loadRecords(String orderBy) {
        currentOrderByStatus = orderBy;

        AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this,
                dbHelper.getAllRecords(orderBy));

        recordsRv.setAdapter(adapterRecord);

//        set num of records
        actionBar.setSubtitle("Total: " + dbHelper.getRecordsCount());
    }

    private void searchRecords(String query) {
        AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this,
                dbHelper.searchRecords(query));

        recordsRv.setAdapter(adapterRecord);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecords(currentOrderByStatus);  //  refresh records list
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        inflate menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

//        searchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                search when search button on keyboard clicked
                searchRecords(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                search as you type
                searchRecords(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        handle menu items
        int id = item.getItemId();
        if (id == R.id.action_sort) {
//            show sort options (show in dialog)
            sortOptionDialog();
        }else if (id == R.id.action_delete) {
//            delete all records
            dbHelper.deleteAllData();
            onResume();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortOptionDialog() {
//        options to display in dialog
        String[] options = {"Title Ascending", "Title Descending", "Newest", "Oldest"};
//        dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort By")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
//                            title ascending
                            loadRecords(orderByTitleAsc);
                        } else if (i == 1) {
//                            title descending
                            loadRecords(orderByTitleDesc);
                        } else if (i == 2) {
//                            Newest
                            loadRecords(orderByNewest);
                        } else if (i == 3) {
//                            Oldest
                            loadRecords(orderByOldest);
                        }
                    }
                }).create().show(); //  show dialog
    }
}