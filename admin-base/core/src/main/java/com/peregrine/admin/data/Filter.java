package com.peregrine.admin.data;

/**
 * Created by schaefa on 6/2/17.
 */
public interface Filter<T> {
    /**
     * @param t Object to be checked if it is included
     * @return True if the object if included otherwise false
     */
    public <T> boolean include(T t);
}
