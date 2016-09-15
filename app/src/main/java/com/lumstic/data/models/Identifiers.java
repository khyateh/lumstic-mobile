package com.lumstic.data.models;

import java.io.Serializable;

public class Identifiers extends Answers implements Serializable {

    IdentifierChoices IdentifierChoices = new IdentifierChoices();

    public IdentifierChoices getIdentifierChoices() {
        return IdentifierChoices;
    }

    public void setIdentifierChoices(IdentifierChoices choices) {
        this.IdentifierChoices = choices;
    }
}
