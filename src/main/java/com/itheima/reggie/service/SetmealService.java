package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @Author 刘政
 * @Date 2022/6/18 15:00
 * @Software IDEA
 */
public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐,同时需要保存套餐和菜品的关联关系
     */
    void saveWithDish(SetmealDto setmealDto);

    void deleteWithDish(List<Long> ids);
}
