package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private final DishFlavorService dishService;

    public DishServiceImpl(DishFlavorService dishService) {
        this.dishService = dishService;
    }

    /**
     * 新增菜品,同时保存对应口味数据
     */
    @Transactional  // 操作两张表 保持事务一致性
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品dish表   默认使用mp雪花算法生成Id
        this.save(dishDto);

        //菜品ID  上方已经将id生成保存到dish表中
        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor   批量保存
        dishService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息  回显
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //不拷贝则属性中的数据不存在
        BeanUtils.copyProperties(dish, dishDto);

        //查询当前菜品对应的口味信息,从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrap = new LambdaQueryWrapper<>();
        //查找dishFlavor.getDishId 与 dish.getId 匹配的
        queryWrap.eq(DishFlavor::getDishId, dish.getId());
        //口味是多数据使用list集合存储
        List<DishFlavor> flavors = dishService.list(queryWrap);
        //存储到dishDto的属性中  回显
        dishDto.setFlavors(flavors);
        //包含了dishDto 中的 flavors[口味集合]  包含了 dish[所有的数据{对象}]
        return dishDto;
    }

    /**
     * 更新菜品信息,同时更新对应的口味信息
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        //删除匹配的dishFlavor表中的数据
        dishService.remove(queryWrapper);

        //添加当前提交过来的口味数据--dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            //设置每一个[口味] 匹配菜品的 id[dishId]  菜品只有一个dishDto  口味是多种item[dishFlavor]  多个口味设置同一个菜品的id
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        //批量添加  集合
        dishService.saveBatch(flavors);

    }

}
