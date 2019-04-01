package com.studio.millionares.barberbooker;

import java.util.ArrayList;
import java.util.HashMap;

public class Service {

    private String id, serviceName, cost, expectedTime;
    ArrayList<String> barbers;

    public Service(String id, String serviceName, String cost, String expectedTime, ArrayList<String> barbers){
        this.id = id;
        this.serviceName = serviceName;
        this.cost = cost;
        this.expectedTime = expectedTime;
        this.barbers = barbers;
    }

    public Service(HashMap<String, String> serviceDetails){
        this.serviceName = serviceDetails.get("name");
        this.cost = serviceDetails.get("cost");
        this.expectedTime = serviceDetails.get("expectedTime");
    }

    public HashMap<String, Object> getServiceDetails(){
        HashMap<String, Object> service = new HashMap<>();

        service.put("id", id);
        service.put("name", serviceName);
        service.put("cost", cost);
        service.put("expectedTime", expectedTime);
        service.put("barbers", expectedTime);

        return service;
    }

    public HashMap<String, String> getServiceSimpleDetails(){
        HashMap<String, String> service = new HashMap<>();

        service.put("name", serviceName);
        service.put("cost", cost);

        return service;
    }

}
