package com.krsikarma.captain.Models;

public class JobRequest {
    String requester_name;
    Double requester_quantity;
    String requester_service_id;
    String requester_service_name;
    String requester_postal_code;
    String requester_user_id;
    String request_status;
    Double request_address_lat;
    Double request_address_long;
    String service_price;
    String requester_acres;
    String requester_address;
    String request_date;
    String request_time;
    String document_id;


    public JobRequest(String requester_name, Double requester_quantity, String requester_service_id, String requester_service_name, String requester_postal_code, String requester_user_id, String request_status, Double request_address_lat, Double request_address_long, String service_price, String requester_acres, String requester_address, String request_date, String request_time, String document_id) {
        this.requester_name = requester_name;
        this.requester_quantity = requester_quantity;
        this.requester_service_id = requester_service_id;
        this.requester_service_name = requester_service_name;
        this.requester_postal_code = requester_postal_code;
        this.requester_user_id = requester_user_id;
        this.request_status = request_status;
        this.request_address_lat = request_address_lat;
        this.request_address_long = request_address_long;
        this.service_price = service_price;
        this.requester_acres = requester_acres;
        this.requester_address = requester_address;
        this.request_date = request_date;
        this.request_time = request_time;
        this.document_id = document_id;
    }

    public String getRequester_name() {
        return requester_name;
    }

    public void setRequester_name(String requester_name) {
        this.requester_name = requester_name;
    }

    public Double getRequester_quantity() {
        return requester_quantity;
    }

    public void setRequester_quantity(Double requester_quantity) {
        this.requester_quantity = requester_quantity;
    }

    public String getRequester_service_id() {
        return requester_service_id;
    }

    public void setRequester_service_id(String requester_service_id) {
        this.requester_service_id = requester_service_id;
    }

    public String getRequester_service_name() {
        return requester_service_name;
    }

    public void setRequester_service_name(String requester_service_name) {
        this.requester_service_name = requester_service_name;
    }

    public String getRequester_postal_code() {
        return requester_postal_code;
    }

    public void setRequester_postal_code(String requester_postal_code) {
        this.requester_postal_code = requester_postal_code;
    }

    public String getRequester_user_id() {
        return requester_user_id;
    }

    public void setRequester_user_id(String requester_user_id) {
        this.requester_user_id = requester_user_id;
    }

    public String getRequest_status() {
        return request_status;
    }

    public void setRequest_status(String request_status) {
        this.request_status = request_status;
    }

    public Double getRequest_address_lat() {
        return request_address_lat;
    }

    public void setRequest_address_lat(Double request_address_lat) {
        this.request_address_lat = request_address_lat;
    }

    public Double getRequest_address_long() {
        return request_address_long;
    }

    public void setRequest_address_long(Double request_address_long) {
        this.request_address_long = request_address_long;
    }

    public String getService_price() {
        return service_price;
    }

    public void setService_price(String service_price) {
        this.service_price = service_price;
    }


    public String getRequester_acres() {
        return requester_acres;
    }

    public void setRequester_acres(String requester_acres) {
        this.requester_acres = requester_acres;
    }

    public String getRequester_address() {
        return requester_address;
    }

    public void setRequester_address(String requester_address) {
        this.requester_address = requester_address;
    }

    public String getRequest_date() {
        return request_date;
    }

    public void setRequest_date(String request_date) {
        this.request_date = request_date;
    }

    public String getRequest_time() {
        return request_time;
    }

    public void setRequest_time(String request_time) {
        this.request_time = request_time;
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }
}
