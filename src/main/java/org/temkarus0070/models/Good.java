package org.temkarus0070.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class Good implements Serializable {
    private long id;
    private String name;
    private double price;
    private int count;
    private double sum;
}
