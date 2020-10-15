package com.ap.sqlite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/* Custom Adapter class for recyclerView
 * Here we will inflate row_record.xml
 * get and set data to views */
public class AdapterRecord extends RecyclerView.Adapter<AdapterRecord.HolderRecord> {

    //    variables
    private Context context;
    private ArrayList<ModelRecord> recordsList;

//    DB helper
    MyDbHelper dbHelper;

    //    constructor
    public AdapterRecord(Context context, ArrayList<ModelRecord> recordsList) {
        this.context = context;
        this.recordsList = recordsList;

        dbHelper = new MyDbHelper(context);
    }

    @NonNull
    @Override
    public HolderRecord onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_record, parent, false);

        return new HolderRecord(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderRecord holder, final int position) {
//        get data, set data, handel view clicks in this method

//        get data
        ModelRecord model = recordsList.get(position);
        final String id = model.getId();
        final String name = model.getName();
        final String image = model.getImage();
        final String bio = model.getBio();
        final String phone = model.getPhone();
        final String email = model.getEmail();
        final String dob = model.getDob();
        final String addedTime = model.getAddedTime();
        final String updatedTime = model.getUpdatedTime();

//        set data to views
        holder.nameTv.setText(name);
        holder.phoneTv.setText(phone);
        holder.emailTv.setText(email);
        holder.dobTv.setText(dob);
//                if user doesn't attach image then imageUri will be null, so set a default image in that case
        if (image.equals("null")){
//                    no image in record, set default
            holder.profileIv.setImageResource(R.drawable.ic_person_black);

        }else {
//                    have image in record
            holder.profileIv.setImageURI(Uri.parse(image));
        }

//        handle item clicks (go to detail record activity)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                pass record id to next activity to show details of that record
                Intent intent = new Intent(context, RecordDetailActivity.class);
                intent.putExtra("RECORD_ID", id);
                context.startActivity(intent);
            }
        });

//        handle more button click listener (show options like edit, delete etc)
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                show options menu
                showMoreDialog(
                        ""+position,
                        ""+id,
                        ""+name,
                        ""+phone,
                        ""+email,
                        ""+dob,
                        ""+bio,
                        ""+image,
                        ""+addedTime,
                        ""+updatedTime);
            }
        });
    }

    private void showMoreDialog(final String position, final String id, final String name, final String phone,
                                final String email, final String dob,final String bio, final String image,
                                final String addedTime, final String updatedTime) {
//        options to display in dialog
        String[] options = {"Edit", "Delete"};
//        dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        add items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0){
//                    Edit is clicked

//                    start AddUpdateRecordActivity to update existing record
                    Intent intent = new Intent(context, AddUpdateRecordActivity.class);
                    intent.putExtra("POSITION", position);
                    intent.putExtra("ID", id);
                    intent.putExtra("NAME", name);
                    intent.putExtra("PHONE", phone);
                    intent.putExtra("EMAIL", email);
                    intent.putExtra("DOB", dob);
                    intent.putExtra("BIO", bio);
                    intent.putExtra("IMAGE", image);
                    intent.putExtra("ADDED_TIME", addedTime);
                    intent.putExtra("UPDATED_TIME", updatedTime);
                    intent.putExtra("isEditMode", true);    //  need to update existing data, set true
                    context.startActivity(intent);
                }else if (which == 1){
//                    Delete is clicked
                    dbHelper.deleteData(id);
//                    refreshing record by calling activitie's onResume method
                    ((MainActivity)context).onResume();
                }
            }
        });
//        show dialog
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return recordsList.size();  //  return size of list/number or records
    }

    class HolderRecord extends RecyclerView.ViewHolder {

        //        views
        ImageView profileIv;
        TextView nameTv, phoneTv, emailTv, dobTv;
        ImageButton moreBtn;

        public HolderRecord(@NonNull View itemView) {
            super(itemView);

//            init views
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            dobTv = itemView.findViewById(R.id.dobTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
        }
    }
}
