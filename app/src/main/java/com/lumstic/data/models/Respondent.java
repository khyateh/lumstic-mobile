package com.lumstic.data.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by frank on 25/05/2016.
 */
public class Respondent extends Responses implements Serializable {

    List<Identifiers> identifiers = new ArrayList<>();
    String tag;

    public List<Identifiers> getIdentifiers() {
        return identifiers ;
    }

    public void setIdentifiers(List<Identifiers> identifiers) {
        this.identifiers = identifiers;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
