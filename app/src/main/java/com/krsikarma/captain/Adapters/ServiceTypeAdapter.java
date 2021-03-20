package com.krsikarma.captain.Adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krsikarma.captain.Models.ServiceType;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class ServiceTypeAdapter extends RecyclerView.Adapter<ServiceTypeAdapter.ViewHolder> {

    public static final String TAG = "ServiceTypeAdapter";
    Activity mActivity;
    ArrayList<ServiceType> serviceTypeArrayList;
    ArrayList<ServiceType> usersSavedServices;
    ArrayList<ServiceType> previouslySelectedServices;

    String phoneLanguage;
    Utils utils;


    public ServiceTypeAdapter(Activity mActivity, ArrayList<ServiceType> serviceTypeArrayList, ArrayList<ServiceType> previouslySelectedServices) {
        this.mActivity = mActivity;
        this.serviceTypeArrayList = serviceTypeArrayList;
        this.previouslySelectedServices = previouslySelectedServices;
        usersSavedServices = new ArrayList<>();
        phoneLanguage = Locale.getDefault().getLanguage();
        utils = new Utils();
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

        if(phoneLanguage.equals("hi")){
            utils.translateEnglishToHindi(serviceType.getService_name(), holder.checkBox);
        }


        if(!previouslySelectedServices.isEmpty()) {

            for (int j = 0; j < previouslySelectedServices.size(); j++) {

                Map<String, ServiceType> map = new HashMap<>();
                map = ( Map<String, ServiceType>) previouslySelectedServices.get(j);

                if (serviceType.getService_id().equals(map.get("service_id"))) {
                    holder.checkBox.setChecked(true);
                }
            }
        }



        if(holder.checkBox.isChecked()){
            usersSavedServices.add(serviceType);
        }

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
