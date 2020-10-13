package com.ap.sqlite;
//part 2 complete
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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

//        loads records
        loadRecords();

//        click to start add record activity
        addRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                start add record activity
                startActivity(new Intent(MainActivity.this, AddUpdateRecordActivity.class));
            }
        });
    }

    private void loadRecords() {
        AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this,
                dbHelper.getAllRecords(Constants.C_ADDED_TIMESTAMP+" DESC"));

        recordsRv.setAdapter(adapterRecord);

//        set num of records
        actionBar.setSubtitle("Total: "+dbHelper.getRecordsCount());
    }

    private void searchRecords(String query){
        AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this,
                dbHelper.searchRecords(query));

        recordsRv.setAdapter(adapterRecord);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();  //  refresh records list
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        inflate menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

//        searchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)item.getActionView();
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
        return super.onOptionsItemSelected(item);
    }
}