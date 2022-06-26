package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    private final DishService dishService;

    private final DishFlavorService dishFlavorService;
    private final CategoryService categoryService;

    public DishController(DishService dishService, CategoryService categoryService, DishFlavorService dishFlavorService) {
        this.dishService = dishService;
        this.categoryService = categoryService;
        this.dishFlavorService = dishFlavorService;
    }

    /**
     * 新增菜品
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 分页显示
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {
        //构造分页构造器对象
        Page<Dish> pageDish = new Page<>(page, pageSize);
        Page<DishDto> pageDto = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrap = new LambdaQueryWrapper<>();
        //添加过滤条件  根据菜品名称进行过滤 ===>>>查询框
        queryWrap.like(name != null, Dish::getName, name);
        //添加排序条件 根据更新时间降序排序
        queryWrap.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageDish, queryWrap);

        //对象拷贝   忽略属性 records
        BeanUtils.copyProperties(pageDish, pageDto, "records");
        //records 是 纯净的 dish集合
        List<Dish> records = pageDish.getRecords();
        // item 相当于 Dish  循环  从records中 让item获取每一个dish
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //对象拷贝  将item(dish)上面的属性拷贝到 dishDto 中 records是每一个对象  item遍历
            BeanUtils.copyProperties(item, dishDto);

            //菜品分类ID   获取item(dish)对象的类别id
            Long categoryId = item.getCategoryId();
            //根据ID查询分类对象  根据item(dish)对象的id读取到category匹配的对象
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                //获取到匹配的对象的名称
                String categoryName = category.getName();
                //将名称设置到dishDto对象中  此时 dishDto中包含了 item(dish)中的属性与值,也包含了自己的属性 和 [categoryName] 的值
                dishDto.setCategoryName(categoryName);
            }
            //装载到集合中
            return dishDto;

        }).collect(Collectors.toList()); // 设置成list集合

        //将Page<DishDto> 的 Records 设置回来
        //pageDto 中包含了 需要分页显示的page纯净属性  添加的list集合包含了需要显示在 页面上的属性 Dish的[属性] 与 [categoryName] 属性
        pageDto.setRecords(list);

        return R.success(pageDto);
    }

    /**
     * 根据Id查询菜品信息与口味信息
     */
    @GetMapping("/{id}")
    public R<DishDto> getDishFlavorId(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 提交修改的数据
     */
    @PutMapping()
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }


    /**
     * 显示菜品的信息
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrap = new LambdaQueryWrapper<>();
        //判断是否接收到值,如果接收到值,则查找数据库中与 getCategoryId 对应的数据  获取菜品的类别
        queryWrap.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

        //添加一个条件 查询状态为1
        queryWrap.eq(Dish::getStatus, 1);

        //添加排序条件
        queryWrap.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //添加数据到List集合中
        List<Dish> list = dishService.list(queryWrap);

        /*
         *  功能需求 : 显示所有的菜品类别 显示各种类别的菜品 显示菜品所选择的口味
         *  首先查询出所有的菜品信息,装载到集合中
         *  之后创建dto对象 将菜品信息拷贝到dto对象中
         *  获取菜品的类别id 通过mp方法匹配到当前所有属于 [当前类别] 的 菜品 存放到wrapper中
         *  获取当前类别的name 存放到 dto 中用于前台显示
         *  获取菜品对应的口味信息 [对应口味的dishId] , 通过mp方法将与当前口味信息匹配菜品装载到 dto 中
         *  此时dto对象中存在 [基本的菜品信息] [对应菜品的类别名称] [对应菜品的口味信息]
         *  菜品信息属于拷贝   类别名称 菜品口味属于设置/填充 设置每一个dio对象的菜品name才能规矩显示
         *  之后将一个完整的dto对象装载到list集合中, 每次只返回一个类别,下次访问则再次调用  调用一次就存放在缓存当中
         */

        //移动端代码
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //对象拷贝  将dish菜品中的数据拷贝到dishDto中
            BeanUtils.copyProperties(item, dishDto);

            //菜品分类ID   获取item(dish)对象的类别id
            Long categoryId = item.getCategoryId();
            //根据ID查询分类对象  根据item(dish)对象的id读取到category匹配的对象   查询菜品对应的类别
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                //获取到匹配的对象的名称
                String categoryName = category.getName();
                //将名称设置到dishDto对象中  此时 dishDto中包含了 item(dish)中的属性与值,也包含了自己的属性 和 [categoryName] 的值
                dishDto.setCategoryName(categoryName);
            }

            Long dishId = item.getId();
            //口味实体
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //查询出口味集合
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            //装载到集合中  此时dto中存在 菜品的口味信息 菜品属于哪一类的类别名称  当前的菜品信息
            return dishDto;

        }).collect(Collectors.toList());


        return R.success(dishDtoList);

    }
}
