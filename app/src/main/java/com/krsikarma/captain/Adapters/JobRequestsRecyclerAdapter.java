package com.krsikarma.captain.Adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krsikarma.captain.Models.JobRequest;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JobRequestsRecyclerAdapter extends RecyclerView.Adapter<JobRequestsRecyclerAdapter.ViewHolder> {

    public static final String TAG = "JobRequestsRAdapter";
    ArrayList<JobRequest> jobRequestArrayList;
    Activity mActivity;
    ListenerRegistration listenerRegistration;

    FirebaseFirestore db;
    String phoneLanguage;
    FirebaseUser firebaseUser;
    FirebaseAuth mAuth;

    Boolean isDriverAssigned = false;


    Utils utils;

    public JobRequestsRecyclerAdapter(ArrayList<JobRequest> jobRequestArrayList, String phoneLanguage, Activity mActivity) {
        this.jobRequestArrayList = jobRequestArrayList;
        this.phoneLanguage = phoneLanguage;
        this.mActivity = mActivity;


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        utils = new Utils();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity.getApplicationContext());
        View view = layoutInflater.inflate(R.layout.row_job_request, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JobRequest jobRequest = jobRequestArrayList.get(position);

        holder.tv_name.setText(jobRequest.getRequester_name());
        holder.tv_metric.setText(jobRequest.getRequester_acres() + " " + mActivity.getString(R.string.acres));
        holder.tv_service_name.setText(jobRequest.getRequester_service_name());
        holder.tv_price.setText("₹ " + Utils.getFormattedNumber(jobRequest.getService_price()));
        holder.tv_address.setText(jobRequest.getRequester_address());
        holder.tv_order_date.setText(jobRequest.getRequest_date());
        holder.tv_order_time.setText(jobRequest.getRequest_time());

        if (phoneLanguage.equals("hi")) {
            utils.translateEnglishToHindi(jobRequest.getRequester_name(), holder.tv_name);
            utils.translateEnglishToHindi(jobRequest.getRequester_service_name(), holder.tv_service_name);
            utils.translateEnglishToHindi(jobRequest.getRequester_address(), holder.tv_address);
        }

        checkForOngoingOrder();

        holder.btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isDriverAssigned) {
                    updateData(jobRequest);
                } else {
                    utils.alertDialogOK(mActivity, mActivity.getString(R.string.error_text), mActivity.getString(R.string.cannot_accept_order));
                }


            }
        });


    }

    @Override
    public int getItemCount() {
        return jobRequestArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_metric, tv_acres, tv_service_name, tv_price, tv_address, tv_order_date, tv_order_time;
        Button btn_accept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_metric = (TextView) itemView.findViewById(R.id.tv_metric);
            tv_acres = (TextView) itemView.findViewById(R.id.tv_acres);
            tv_service_name = (TextView) itemView.findViewById(R.id.tv_service_name);
            tv_price = (TextView) itemView.findViewById(R.id.tv_price);
            tv_address = (TextView) itemView.findViewById(R.id.tv_address);
            tv_order_date = (TextView) itemView.findViewById(R.id.tv_order_date);
            tv_order_time = (TextView) itemView.findViewById(R.id.tv_order_time);
            btn_accept = (Button) itemView.findViewById(R.id.btn_accept);


        }
    }

    private void updateData(JobRequest jobRequest) {
        Map<String, Object> data = new HashMap<>();
        data.put(mActivity.getString(R.string.order_status), mActivity.getString(R.string.order_status_type_driver_assigned));
        data.put(mActivity.getString(R.string.partner_id), FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection(mActivity.getString(R.string.orders)).document(jobRequest.getDocument_id())
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Partner has accepted this order");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.i(TAG, "An error occurred " + e.getLocalizedMessage());

                    }
                });
    }

    private void checkForOngoingOrder() {
        listenerRegistration = db.collection(mActivity.getString(R.string.orders))
                .whereEqualTo(mActivity.getString(R.string.partner_id), firebaseUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "error: ", error);
                            return;
                        }

                        String order_status;

                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get(mActivity.getString(R.string.order_status)) != null) {
                                order_status = doc.getString(mActivity.getString(R.string.order_status));

                                if (order_status.equals(mActivity.getString(R.string.order_status_type_ongoing)) ||
                                        order_status.equals(mActivity.getString(R.string.order_status_type_payment_pending)) ||
                                        order_status.equals(mActivity.getString(R.string.order_status_type_driver_assigned)))
                                {
                                    isDriverAssigned = true;
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        utils.closeTranslator();
        if (listenerRegistration != null) {
            listenerRegistration = null;
        }
    }
}
