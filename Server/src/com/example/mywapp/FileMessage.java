package com.example.mywapp;

public class FileMessage extends Message{
    String fileName;
    byte fileData[];

        FileMessage (byte data[], String fileName, String toImei, String fromImei) {
            super(MessageType.FILE, toImei, fromImei);
            this.fileName = fileName;
            fileData = data;
        }    
}




