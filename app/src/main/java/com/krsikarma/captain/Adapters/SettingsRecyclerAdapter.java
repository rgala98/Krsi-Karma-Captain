package com.krsikarma.captain.Adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.krsikarma.captain.Activities.AccountActivity;
import com.krsikarma.captain.Activities.AddDocumentsActivity;
import com.krsikarma.captain.Activities.BankDetailsActivity;
import com.krsikarma.captain.Activities.ContactUsActivity;
import com.krsikarma.captain.Activities.EarningsActivity;
import com.krsikarma.captain.Activities.GetStartedActivity;
import com.krsikarma.captain.Activities.YourOrdersActivity;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.Utils;

import java.util.ArrayList;

public class SettingsRecyclerAdapter extends RecyclerView.Adapter<SettingsRecyclerAdapter.ViewHolder> {

    public static final String TAG = "SettingsRecyclerAdapter";
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

        if (settingNameArrayList.get(position).equals(mActivity.getString(R.string.logout))) {
            // Logout
            holder.tv_settings_name.setTextColor(mActivity.getColor(R.color.brand_color));
        }

        holder.itemView.setOnClickListener(view -> {

            Intent intent;

            switch (position) {
                case 0: //Account
                    intent = new Intent(mActivity, AccountActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
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
                case 3: // Submitted Documents
                    intent = new Intent(mActivity, AddDocumentsActivity.class);
                    intent.putExtra("activity_from", "settingsActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                    break;
                case 4:// Bank Details
                    intent = new Intent(mActivity, BankDetailsActivity.class);
                    intent.putExtra("activity_from", "settingsActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                    break;
                case 5: //Privacy Policy
                    break;
                case 6: //Terms of Use
                    break;
                case 7: // Contact Us
                    intent = new Intent(mActivity, ContactUsActivity.class);
                    intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK));
                    mActivity.startActivity(intent);
                    break;
                case 8:
                    // Logout

                    AlertDialog.Builder builder;
                    AlertDialog alert;

                    builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(mActivity.getString(R.string.are_you_sure_logout))
                            .setCancelable(true)
                            .setPositiveButton(mActivity.getString(R.string.logout), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseMessaging.getInstance().getToken()
                                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                                @Override
                                                public void onComplete(@NonNull Task<String> task) {
                                                    if (!task.isSuccessful()) {
                                                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                                        return;
                                                    }

                                                    // Get new FCM registration token
                                                    String token = task.getResult();

                                                    final DocumentReference doc_id = FirebaseFirestore.getInstance().collection(mActivity.getString(R.string.partners)).document(firebaseUser.getUid());


                                                    doc_id.update("token", FieldValue.arrayRemove(token)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.i(TAG,"Token Removed");
                                                            mAuth.signOut();
                                                            mActivity.finishAffinity();
                                                            Intent intent = new Intent(mActivity, GetStartedActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            mActivity.startActivity(intent);
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.i(TAG,"An error occurred : "+e.getMessage());
                                                        }
                                                    });


                                                }
                                            });


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
