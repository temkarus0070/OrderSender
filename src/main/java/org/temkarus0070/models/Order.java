package org.temkarus0070.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

 @Data
 @AllArgsConstructor
 @NoArgsConstructor
 public class Order implements Serializable {
     private String clientFIO;
     private long orderNum;
     private Collection<Good> goods;
     private Status status;
 }
