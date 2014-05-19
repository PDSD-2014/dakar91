package com.example.mywapp;

public class Online extends Request {
    public final String password, name;
    
    public Online (String fromId, String passwd, String name) {
        super(RequestType.ONLINE, null, fromId);
        this.password = passwd;
        this.name = name;
    }
}
