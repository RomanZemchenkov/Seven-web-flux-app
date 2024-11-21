package com.roman.dao.entity;

public interface BaseEntity<T>{

    T getId();

    void setId(T id);
}
