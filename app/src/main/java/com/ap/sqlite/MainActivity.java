package com.ap.sqlite;
//part 5 complete

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

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

    /*-----------For storage permission-----------*/
    private static final int STORAGE_REQUEST_CODE_EXPORT = 1;
    private static final int STORAGE_REQUEST_CODE_IMPORT = 2;
    private String[] storagePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setTitle("All Records");

        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

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

    private boolean checkStoragePermission(){
//        check if storage permission is enable or not and return true/false
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return result;
    }

    private void requestStoragePermissionImport(){
//        request storage permission for import
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE_IMPORT);
    }
    private void requestStoragePermissionExport(){
//        request storage permission for Export
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE_EXPORT);
    }

    private void exportCSV() {
//        path of csv file
        File folder = new File(Environment.getExternalStorageDirectory() +"/" + "SQLiteBackup");//SQLiteBackup is folder name

        boolean isFolderCreated = false;

        if (!folder.exists()){
            isFolderCreated = folder.mkdirs();  //create folder if not exists
        }

        Log.d("CSV_TAG", "exportCSV: "+isFolderCreated);

//        file name
        String csvFileName = "SQLite_Backup.csv";

//        complete path and name
        String filePathAndName = folder.toString() + "/" + csvFileName;

//        get records

        ArrayList<ModelRecord> recordsList = new ArrayList<>();
        recordsList.clear();
        recordsList = dbHelper.getAllRecords(orderByOldest);

        try {
//            write csv file
            FileWriter fw = new FileWriter(filePathAndName);
            for (int i = 0; i < recordsList.size(); i++){
                fw.append(""+recordsList.get(i).getId());   //  id
                fw.append(",");
                fw.append(""+recordsList.get(i).getName());   //  name
                fw.append(",");
                fw.append(""+recordsList.get(i).getImage());   //  image
                fw.append(",");
                fw.append(""+recordsList.get(i).getBio());   //  bio
                fw.append(",");
                fw.append(""+recordsList.get(i).getPhone());   //  phone
                fw.append(",");
                fw.append(""+recordsList.get(i).getEmail());   //  email
                fw.append(",");
                fw.append(""+recordsList.get(i).getDob());   //  dob
                fw.append(",");
                fw.append(""+recordsList.get(i).getAddedTime());   //  added time
                fw.append(",");
                fw.append(""+recordsList.get(i).getUpdatedTime());   //  updated time
                fw.append("\n");
            }
            fw.flush();
            fw.close();

            Toast.makeText(this, "Backup Exported to: "+filePathAndName, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
//            if there is any exception, show exception message
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importCSV() {
//        use same path and file name to import
        String filePathAndName = Environment.getExternalStorageDirectory()+"/SQLiteBackup/"+"SQLite_Backup.csv";

        File csvFile = new File(filePathAndName);

//        check if exists or not
        if (csvFile.exists()){
//            backup exists
            try {
                CSVReader csvReader = new CSVReader(new FileReader(csvFile.getAbsolutePath()));

                String[] nextLine;
                while ((nextLine = csvReader.readNext()) != null){
//                    use same order for import as used for export e.g. id is saved on 0 index
                    String ids = nextLine[0];
                    String name = nextLine[1];
                    String image = nextLine[2];
                    String bio = nextLine[3];
                    String phone = nextLine[4];
                    String email = nextLine[5];
                    String dob = nextLine[6];
                    String addedTime = nextLine[7];
                    String updatedTime = nextLine[8];

                    long timestamp = System.currentTimeMillis();
                    long id = dbHelper.insertRecord(
                            "" + name,
                            "" + image,
                            "" + bio,
                            "" + phone,
                            "" + email,
                            "" + dob,
                            "" + addedTime,
                            "" + updatedTime);
                }
                Toast.makeText(this, "Backup Restored...", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else {
//            backup doesn't exists
            Toast.makeText(this, "No backup Found...", Toast.LENGTH_SHORT).show();
        }
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
        }else if (id == R.id.action_delete_all) {
//            delete all records
            dbHelper.deleteAllData();
            onResume();
        }else if (id == R.id.action_backup) {
//            backup all records to csv file
            if (checkStoragePermission()){
//                permission allowed
                exportCSV();
            }else {
//                permission not allowed
                requestStoragePermissionExport();
            }
        }else if (id == R.id.action_restore) {
//            restore all records from csv file
            if (checkStoragePermission()){
//                permission allowed
                importCSV();
                onResume();
            }else {
//                permission not allowed
                requestStoragePermissionImport();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        handle permission result
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case STORAGE_REQUEST_CODE_EXPORT:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    permission granted
                    exportCSV();
                }else {
//                    permission denied
                    Toast.makeText(this, "Storage Permission Required...", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case STORAGE_REQUEST_CODE_IMPORT:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    permission granted
                    importCSV();
                }else {
//                    permission denied
                    Toast.makeText(this, "Storage Permission Required...", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
}