package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    private final SetmealDishService setmealDishService;

    public SetmealServiceImpl(SetmealDishService setmealDishService) {
        this.setmealDishService = setmealDishService;
    }

    /**
     * 新增套餐,同时需要保存套餐和菜品的关联关系
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息  继承了setmeal 直接保存  添加此套餐
        this.save(setmealDto);  // -- 保存一张表数据

        //获取setmealDish[套餐菜品关系表]表的数据   套餐的菜关联套餐
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //获取每一个菜的信息
        setmealDishes.stream().map((item)->{
            //将每一个菜的 id[setmelDish] 关联到 对应的套餐 [setMeal]中
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息,操作setmeal_dish 执行insert
        setmealDishService.saveBatch(setmealDishes); //--保存两张表数据

    }

    /**
     * 删除套餐以及对应的菜单  多删除  删除套餐和餐品的关联数据
     */
    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //查询套餐状态,确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrap = new LambdaQueryWrapper<>();
        //select count(*) from setMeal where id in (1,2,3) and status = 1  查询setMeal表中 所有id匹配的ids字段 中所有字段的数据
        queryWrap.in(Setmeal::getId,ids);
        //setMeal中的每一个数据状态是否为1
        queryWrap.eq(Setmeal::getStatus,1);
        //this === SetMealService 接口
        int count = this.count(queryWrap);
        //如果不能删除,抛出一个业务异常  有一个为1都不能删除
        if(count > 0){
            throw new CustomException("套餐正在售卖中,无法删除!");
        }

        //如果可以删除,先删除套餐表中的数据
        this.removeByIds(ids);

        //delete from setMeal_dish where setMeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrap = new LambdaQueryWrapper<>();

        //条件找出 所有 setMeal_Id 所关联的 setMeal表中的 id
        lambdaQueryWrap.in(SetmealDish::getSetmealId,ids);

        //删除关系表中的数据setMeal_Dish  所有setMeal_Id关联setMeal表id的数据全部删除 lam中存储的就是符合条件的数据
        setmealDishService.remove(lambdaQueryWrap);


    }
}

