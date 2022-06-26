package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;
import org.springframework.stereotype.Service;

/**
 * @Author 刘政
 * @Date 2022/6/18 11:48
 * @Software IDEA
 */
@Service
public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
