package com.heybcat.tightlyweb.sql.entity;

import java.util.List;
import lombok.Getter;

/**
 * @author Fetters
 */
@Getter
public class Page<T> {

    private int total;

    private int size;

    private int current;

    private List<T> records;

    public Page(int size, int current, List<T> records, int total) {
        this.size = size;
        this.current = current;
        this.records = records;
        this.total = total;
    }

    public Page(int size, int current){
        this.size = size;
        this.current = current;
    }


}
