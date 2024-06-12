package com.heybcat.tightlyweb.common.resource;


/**
 * @author Fetters
 */
public interface ResourceLoader {

  /**
   * 加载资源
   * @param path 路径
   * @return Resource
   */
  Resource load(String path);

}
