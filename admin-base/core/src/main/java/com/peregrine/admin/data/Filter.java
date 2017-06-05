package com.peregrine.admin.data;

/**
 * Created by schaefa on 6/2/17.
 */
public interface Filter<T> {
    public <T> boolean include(T t);
}
