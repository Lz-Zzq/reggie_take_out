package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

/**
 * @Author 刘政
 * @Date 2022/6/18 15:01
 * @Software IDEA
 */
public interface DishService extends IService<Dish> {
    /**
     * 新增菜品,同时插入菜品对应的口味数据,需要操作两张表 dish dish_flavor
     */
    void saveWithFlavor(DishDto dishDto);

    /**
     * 根据id查询菜品信息和对应的口味信息
     */
    DishDto getByIdWithFlavor(Long id);

    /**
     * 更新菜品信息,同时更新对应的口味信息
     */
    void updateWithFlavor(DishDto dishDto);


}
