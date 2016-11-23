package com.dominoxpgaming.android;


/**
 * Created by Jan on 16.09.2016.
 */
public class SQL_images {
    private int _id;
    private String _name;
    private byte _image;

    public SQL_images(){

    }

    public SQL_images(int id){
        this._id=id;
    }

    public byte get_image(){
        return _image;
    }

    public int get_id(){
        return this._id;
    }

    public String get_name(){
        return this._name;
    }
}
