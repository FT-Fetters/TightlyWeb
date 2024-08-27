package com.heybcat.tightlyweb.sql.entity;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * @author Fetters
 */
@Getter
public class Page<T> {

    private int total;

    private final int size;

    private final int current;

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

    @FunctionalInterface
    public interface ConvertFunction<T, M>{

        /**
         * Covert T to M
         * @param t the object to convert
         * @return the converted object
         */
        M convert(T t);
    }

    public <M> Page<M> convert(ConvertFunction<T, M> convertFunction){
        List<M> convertedRecords = this.records.stream()
            .map(convertFunction::convert)
            .collect(Collectors.toList());
        return new Page<>(size, current, convertedRecords, total);
    }


}
