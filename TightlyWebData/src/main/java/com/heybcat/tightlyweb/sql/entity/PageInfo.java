package com.heybcat.tightlyweb.sql.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Fetters
 */
@Data
@AllArgsConstructor
public class PageInfo {

    private Integer current;

    private Integer size;

    private Integer total;

}
