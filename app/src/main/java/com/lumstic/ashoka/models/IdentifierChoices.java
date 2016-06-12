package com.lumstic.ashoka.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IdentifierChoices extends Choices implements Serializable {
    List<Choices> choices = new ArrayList<>();

    public List<Choices> getChoices() {
        return choices;
    }

    public void setChoices(List<Choices> choices) {
        this.choices = choices;
    }
}
