package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("shoppingCart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }


    /**
     * 查询shoppingCart 数据
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        Long currentId = BaseContext.getCurrentId();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 添加菜品 [购物车]
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}", shoppingCart.toString());

        /*
         * 首先获取当前登陆用户的id
         * 将前端传递的购物车对象设置为当前登陆用户创建的
         * 获取当前菜品的id
         * 查询当前购物车中所有属于当前用户的数据
         * 判断当前菜品id是否为null,是 则是套餐 不是 则是菜品
         * 如果是那就把数据库中购物车菜品的id 和 传递过来菜品的id 进行查询
         * 如果不是那就把数据库中购物车的套餐id 和传递过来的套餐id 进行查询
         * 获取到购物车查询后的对象数据
         * 判断,如果数据为null,证明购物车中没有,则进行创建
         * 如果不为null,则将菜品的数量+1
         */

        //获取当前登陆用户
        Long currentId = BaseContext.getCurrentId();
        //设置当前菜品是当前用户添加的
        shoppingCart.setUserId(currentId);

        //获取购物车中符合条件的数据
        ShoppingCart one = addAndSub(shoppingCart, currentId);

        if (one != null) {
            //不为null则证明存在,需要在基础上+1
            one.setNumber(one.getNumber() + 1);
            shoppingCartService.updateById(one);
        } else {
            //如果为null证明不存在则添加并且设置基础值为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }

        return R.success(one);
    }

    /**
     * 将购物车数据减少
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {

        Long currentId = BaseContext.getCurrentId();

        /*
         * 获取购物车中: 是菜品还是套餐 获取当前登陆用户的购物车中数据
         */
        ShoppingCart one = addAndSub(shoppingCart, currentId);

        //获取one的个数
        Integer number = one.getNumber();
        if (number > 1) {
            one.setNumber(number - 1);
            shoppingCartService.updateById(one);
        } else {
            log.info("执行del语句");
            one.setNumber(0);
            shoppingCartService.updateById(one);
            shoppingCartService.removeById(one);
        }

        return R.success(one);
    }

    /**
     * 增加与删除的重复代码块
     * 获取购物车中数据符合条件的数据
     */
    private ShoppingCart addAndSub(ShoppingCart shoppingCart, Long currentId) {
        //获取当前菜品的id
        Long dishId = shoppingCart.getDishId();

        //获取当前登陆的用户
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        //判断是否菜品
        if (dishId != null) {
            //是菜品就获取当前菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //是套餐就获取当前套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        return shoppingCartService.getOne(queryWrapper);
    }

    /**
     * 清除购物车
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);

        return R.success("清除购物车成功");
    }

}
