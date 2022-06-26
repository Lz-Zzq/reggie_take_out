package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    private final SetmealService setmealService;


    private final CategoryService categoryService;

    public SetmealController(SetmealService setmealService, CategoryService categoryService) {
        this.setmealService = setmealService;
        this.categoryService = categoryService;
    }

    /*
     * 分页显示套餐管理界面
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {
        //分页构造器对象  创建此分页构造器对象用于Setmeal表的数据分页查询
        Page<Setmeal> setPage = new Page<>(page, pageSize);
        //创建此分页构造器对象用于最终返回
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrap = new LambdaQueryWrapper<>();

        //添加过滤条件  如果模糊查询
        queryWrap.like(name != null, Setmeal::getName, name);

        //添加排序条件
        queryWrap.orderByDesc(Setmeal::getUpdateTime);

        //执行分页查询
        setmealService.page(setPage, queryWrap);

        //数据拷贝  将分页数据拷贝到最终返回的分页构造器中  去除"records"是一个纯净的分页   setMealDtoPage 包含了纯净的分页数据
        BeanUtils.copyProperties(setPage, setmealDtoPage, "records");

        //获取setMeal表中的数据
        List<Setmeal> records = setPage.getRecords();

        //  ------- 最后返回的list集合 此时这是一条返回到前端的完整数据
        List<SetmealDto> list = records.stream().map((item) -> {
            //创建dto对象,用于数据合并
            SetmealDto setmealDto = new SetmealDto();

            //查找setMeal表中与Category表关联的id
            Long setMealId = item.getCategoryId();

            //查找category表与setMale表中关联的数据
            Category category = categoryService.getById(setMealId);

            //将 item{setMeal  [纯净的{setMeal}表中的数据拷贝到 setMealDto 对象中  ]   此时 setMealDto对象中包含了setMeal表的数据
            BeanUtils.copyProperties(item, setmealDto);

            //判断是否查找到
            if (category != null) {
                //获取匹配的对象的名称 {category}表
                String categoryName = category.getName();
                //将与{setMeal}表关联的数据存放到setMealDto对象中   此时  setMealDto对象中包含了setMeal表的数据 和 categoryName
                setmealDto.setCategoryName(categoryName);
            }
            //将对象装载到集合中
            return setmealDto;

        }).collect(Collectors.toList()); // 设置成list集合

        //将数据存储到分页构造对象中
        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * 新增套餐
     */
    @PostMapping
    public R<String> saveCombo(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息:{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("添加菜品成功");
    }

    /**
     * 删除套餐  多删除
     */
    @DeleteMapping
    public R<String> deleteCombo(@RequestParam List<Long> ids) {
        //删除是删除套餐setMeal,但是会连带setMealDish中对应的菜品
        setmealService.deleteWithDish(ids);

        return R.success("数据删除成功");
    }

    /**
     * 显示移动端套餐页面  根据条件查询套餐数据
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //传递一个当前套餐的id 传递一个状态   查询数据库中匹配的套餐数据
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //将匹配的套餐数据进行存储
        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

}
