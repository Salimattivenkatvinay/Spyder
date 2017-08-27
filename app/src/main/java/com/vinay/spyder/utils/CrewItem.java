package com.vinay.spyder.utils;

/**
 * Created by Vinay Sajjanapu on 8/27/2017.
 */

public class CrewItem {
    private String name, id,role,profile_path;
    public CrewItem(String name, String id, String role, String profile_path){
        this.name = name;
        this.id = id;
        this.role = role;
        this.profile_path = profile_path;
    }

    public String getName(){
        return name;
    }
    public String getId(){
        return id;
    }
    public String getRole(){
        return role;
    }
    public String getProfile_path(){
        return profile_path;
    }

}
