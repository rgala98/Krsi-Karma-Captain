package com.krsikarma.captain.Adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.krsikarma.captain.Activities.EarningsActivity;
import com.krsikarma.captain.Activities.GetStartedActivity;
import com.krsikarma.captain.Activities.YourOrdersActivity;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.Utils;

import java.util.ArrayList;

public class SettingsRecyclerAdapter extends RecyclerView.Adapter<SettingsRecyclerAdapter.ViewHolder> {

    ArrayList<String> settingNameArrayList;
    Activity mActivity;

    Utils utils;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    public SettingsRecyclerAdapter(Activity mActivity, ArrayList<String> settingNameArrayList) {
        this.mActivity = mActivity;
        this.settingNameArrayList = settingNameArrayList;

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        utils = new Utils();
    }

    @NonNull
    @Override
    public SettingsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity.getApplicationContext());
        View view = layoutInflater.inflate(R.layout.row_settings, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsRecyclerAdapter.ViewHolder holder, int position) {
        holder.tv_settings_name.setText(settingNameArrayList.get(position));

        if (position == 5) {
            // Logout
            holder.tv_settings_name.setTextColor(mActivity.getColor(R.color.brand_color));
        }

        holder.itemView.setOnClickListener(view -> {

            Intent intent;

            switch (position) {
                case 0: //Account
                    break;
                case 1: // Your Orders

                    intent = new Intent(mActivity, YourOrdersActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                    break;

                case 2:  // Your Earnings
                    intent = new Intent(mActivity, EarningsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                    break;
                case 3: //Privacy Policy
                    break;
                case 4://Terms
                    break;
                case 5:
                    //logout

                    AlertDialog.Builder builder;
                    AlertDialog alert;

                    builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(mActivity.getString(R.string.are_you_sure_logout))
                            .setCancelable(true)
                            .setPositiveButton(mActivity.getString(R.string.logout), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mAuth.signOut();
                                    mActivity.finishAffinity();
                                    Intent intent = new Intent(mActivity, GetStartedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mActivity.startActivity(intent);

                                }
                            });
                    alert = builder.create();
                    alert.setTitle("");
                    alert.show();
                    break;
            }

        });
    }

    @Override
    public int getItemCount() {
        return settingNameArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_settings_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_settings_name = (TextView) itemView.findViewById(R.id.tv_settings_name);
        }
    }
}
