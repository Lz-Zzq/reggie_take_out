package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 新增分类
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        //设置分页
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //设置排序条件
        LambdaQueryWrapper<Category> queryWrap = new LambdaQueryWrapper<>();
        queryWrap.orderByAsc(Category::getSort);
        //进行分页查新
        categoryService.page(pageInfo,queryWrap);
        return R.success(pageInfo);
    }

    /**
     * 根据id删除分类
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除分类 id:{}",ids);
        //categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    /**
     * 修改分类信息
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息,{}",category);

        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据  展示在下拉框的分类数据
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件   所有类型为1的套餐
        if(category == null) return R.error("null");
        //在此处添加条件category.getType() 是指如果传递的category 的 type 为null 就不做比较
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件  将所有类型为1的套餐升序排序 如果相同则按照修改时间排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //调用list方法将符合条件的数据进行返回成list集合
        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);

    }
}
