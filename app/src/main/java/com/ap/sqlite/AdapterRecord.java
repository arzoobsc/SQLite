package com.ap.sqlite;

import android.content.Context;
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

    //    constructor
    public AdapterRecord(Context context, ArrayList<ModelRecord> recordsList) {
        this.context = context;
        this.recordsList = recordsList;
    }

    @NonNull
    @Override
    public HolderRecord onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_record, parent, false);

        return new HolderRecord(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderRecord holder, int position) {
//        get data, set data, handel view clicks in this method

//        get data
        ModelRecord model = recordsList.get(position);
        final String id = model.getId();
        String name = model.getName();
        String image = model.getImage();
        String bio = model.getBio();
        String phone = model.getPhone();
        String email = model.getEmail();
        String dob = model.getDob();
        String addedTime = model.getAddedTime();
        String updatedTime = model.getUpdatedTime();

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
//                will implement letter
            }
        });
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
