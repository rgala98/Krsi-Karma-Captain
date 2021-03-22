package com.krsikarma.captain.Adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krsikarma.captain.Activities.RaiseAComplaintActivity;
import com.krsikarma.captain.Models.Transaction;
import com.krsikarma.captain.R;
import com.krsikarma.captain.Utility.Utils;

import java.util.ArrayList;

public class TransactionRecyclerAdapter extends RecyclerView.Adapter<TransactionRecyclerAdapter.ViewHolder> {

    public static final String TAG = "TransactionRecyclerAdap";
    ArrayList<Transaction> transactionArrayList;
    Activity mActivity;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    Utils utils;


    public TransactionRecyclerAdapter(ArrayList<Transaction> transactionArrayList, Activity mActivity) {
        this.transactionArrayList = transactionArrayList;
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
        View view = layoutInflater.inflate(R.layout.row_transactions, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Transaction transaction = transactionArrayList.get(position);


        holder.tv_amount.setText("₹ " + transaction.getRequest_amount());
        holder.tv_date.setText(transaction.getDate_created());
        holder.tv_status.setText(transaction.getRequest_status().substring(0, 1).toUpperCase() + transaction.getRequest_status().substring(1));


        if (transaction.getRequest_status().equalsIgnoreCase("pending")) {
            holder.img_status.setImageResource(R.drawable.ic_pending);
            holder.tv_amount.setTextColor(mActivity.getColor(R.color.yellow));
            holder.btn_cancel.setVisibility(View.VISIBLE);
        } else if (transaction.getRequest_status().equalsIgnoreCase("rejected")) {

            holder.img_status.setImageResource(R.drawable.ic_cancelled);
            holder.tv_amount.setTextColor(mActivity.getColor(R.color.brand_color));
            holder.btn_cancel.setVisibility(View.GONE);
        } else if (transaction.getRequest_status().equalsIgnoreCase("cancelled")) {

            holder.img_status.setImageResource(R.drawable.ic_cancelled);
            holder.tv_amount.setTextColor(mActivity.getColor(R.color.brand_color));
            holder.btn_cancel.setVisibility(View.GONE);
        } else if (transaction.getRequest_status().equalsIgnoreCase("received")) {

            holder.img_status.setImageResource(R.drawable.ic_received);
            holder.tv_amount.setTextColor(mActivity.getColor(R.color.green));
            holder.btn_cancel.setVisibility(View.GONE);
        }

        holder.btn_raise_complaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, RaiseAComplaintActivity.class);
                intent.putExtra("transactionArrayList", transactionArrayList);
                intent.putExtra("position", position);
                intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK));
                mActivity.startActivity(intent);
            }
        });

        holder.btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder;
                AlertDialog alert;

                builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(mActivity.getString(R.string.are_you_sure_cancel_withdraw_request))
                        .setNegativeButton(mActivity.getString(R.string.do_not_wish_to_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton(mActivity.getString(R.string.cancel_request), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                cancelRequest(transaction);
                            }
                        });
                alert = builder.create();
                alert.setTitle("");
                alert.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionArrayList.size();
    }

    private void cancelRequest(Transaction transaction) {
        db.collection(mActivity.getString(R.string.partners)).document(firebaseUser.getUid())
                .collection(mActivity.getString(R.string.withdraw_requests))
                .document(transaction.getDocument_id())
                .update(mActivity.getString(R.string.request_status), "cancelled")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.request_cancelled), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "could not cancel request");
                    }
                });
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img_status;
        TextView tv_status;
        TextView tv_date;
        TextView tv_amount;
        Button btn_raise_complaint;
        Button btn_cancel;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img_status = (ImageView) itemView.findViewById(R.id.img_status);
            tv_amount = (TextView) itemView.findViewById(R.id.tv_amount);
            tv_status = (TextView) itemView.findViewById(R.id.tv_status);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            btn_cancel = (Button) itemView.findViewById(R.id.btn_cancel);
            btn_raise_complaint = itemView.findViewById(R.id.btn_raise_complaint);
        }
    }
}
