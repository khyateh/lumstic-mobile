package com.lumstic.ashoka.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Identifiers extends Answers implements Serializable {

    IdentifierChoices IdentifierChoices = new IdentifierChoices();

    public IdentifierChoices getIdentifierChoices() {
        return IdentifierChoices;
    }

    public void setIdentifierChoices(IdentifierChoices choices) {
        this.IdentifierChoices = choices;
    }
}
