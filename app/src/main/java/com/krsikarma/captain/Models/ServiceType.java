package com.krsikarma.captain.Models;

import java.util.HashMap;

public class ServiceType {
    String service_name;
    String service_id;

    public ServiceType(String service_name, String service_id) {
        this.service_name = service_name;
        this.service_id = service_id;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }
}
