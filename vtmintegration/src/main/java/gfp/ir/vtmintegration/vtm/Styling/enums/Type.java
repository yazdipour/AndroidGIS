/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm.Styling.enums;

public enum  Type {
    GRADIENT("GRADIENT"),
    SINGLECOLOR("SINGLECOLOR"),

    CATEGOURIZE("CATEGOURIZE");


    private  String type_;

    private Type(String type){
        this.type_=type;
    }

    public String getType() {
        return type_;
    }
}
