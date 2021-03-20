package com.krsikarma.captain.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Transaction implements Parcelable {
    String date_created;
    String document_id;
    String request_amount;
    String request_status;

    public Transaction(String date_created, String document_id, String request_amount, String request_status) {
        this.date_created = date_created;
        this.document_id = document_id;
        this.request_amount = request_amount;
        this.request_status = request_status;
    }

    protected Transaction(Parcel in) {
        date_created = in.readString();
        document_id = in.readString();
        request_amount = in.readString();
        request_status = in.readString();
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }

    public String getRequest_amount() {
        return request_amount;
    }

    public void setRequest_amount(String request_amount) {
        this.request_amount = request_amount;
    }

    public String getRequest_status() {
        return request_status;
    }

    public void setRequest_status(String request_status) {
        this.request_status = request_status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(date_created);
        parcel.writeString(document_id);
        parcel.writeString(request_amount);
        parcel.writeString(request_status);
    }
}
