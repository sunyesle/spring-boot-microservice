package com.sunyesle.order_service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.avro.Schema;

public interface AvroIgnoreMixIn {
    @JsonIgnore
    Schema getSchema();

    @JsonIgnore
    abstract void getSpecificData();
}
