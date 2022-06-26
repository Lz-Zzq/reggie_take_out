package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {

    private final DishService dishService;

    private final SetmealService setmealService;

    public CategoryServiceImpl(DishService dishService, SetmealService setmealService) {
        this.dishService = dishService;
        this.setmealService = setmealService;
    }

    /**
     * 根据id删除分类
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dictWrapper = new LambdaQueryWrapper<>();

        //查询当前分类是否关联了菜品,如果已经关联,则抛出业务异常
        dictWrapper.eq(Dish::getCategoryId, id);
        int count = dishService.count(dictWrapper);

        //查询分类是否关联了套餐,如果关联,则抛出异常
        if(count > 0){
            //已经关联菜品,抛出一个业务异常
            throw new CustomException("当前分类项关联了菜品,不能删除");

        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件,根据分类id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2 > 0){
            //已经关联套餐,抛出一个业务异常
            throw new CustomException("当前分类项关联了套餐,不能删除");
        }

        //正常则删除分类
        super.removeById(id);
    }
}
