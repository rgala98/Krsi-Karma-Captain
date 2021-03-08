package com.krsikarma.captain.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krsikarma.captain.Models.ServiceType;
import com.krsikarma.captain.R;

import java.util.ArrayList;

public class ServiceTypeAdapter extends RecyclerView.Adapter<ServiceTypeAdapter.ViewHolder> {

    public static final String TAG = "ServiceTabAdapter";
    Activity mActivity;
    ArrayList<ServiceType> serviceTypeArrayList;
    ArrayList<ServiceType> usersSavedServices;


    public ServiceTypeAdapter(Activity mActivity, ArrayList<ServiceType> serviceTypeArrayList) {
        this.mActivity = mActivity;
        this.serviceTypeArrayList = serviceTypeArrayList;
        usersSavedServices = new ArrayList<>();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity.getApplicationContext());
        View view = layoutInflater.inflate(R.layout.row_service_type, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {

        ServiceType serviceType = serviceTypeArrayList.get(i);
        holder.checkBox.setText(serviceType.getService_name());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.checkBox.isChecked()) {
                    usersSavedServices.add(serviceType);
                } else {
                    usersSavedServices.remove(serviceType);
                }

            }
        });


    }

    public ArrayList<ServiceType> getUsersServices() {
        return usersSavedServices;
    }


    @Override
    public int getItemCount() {
        return serviceTypeArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }


}
