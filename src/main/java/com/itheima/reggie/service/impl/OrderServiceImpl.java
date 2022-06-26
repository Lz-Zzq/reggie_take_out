package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j


public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    private final ShoppingCartService shoppingCartService;

    private final UserService userService;

    private final AddressBookService addressBookService;

    private final OrderDetailService orderDetailService;

    public OrderServiceImpl(ShoppingCartService shoppingCartService, UserService userService, AddressBookService addressBookService, OrderDetailService orderDetailService) {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
        this.addressBookService = addressBookService;
        this.orderDetailService = orderDetailService;
    }

    /**
     * 用户下单
     */
    @Transactional
    public void submit(Orders orders) {
        /*
         * 获取用户的id
         * 查询用户购物车的数据
         * 将当前用户购物车的数据存放起来
         * 获取用户的信息 获取前台传递过来的用户地址的信息
         * 设置订单号
         * 设置订单详细表数据 item为购物车对象
         * 订单详细表中添加每条对应数据,并且计算金额
         * 设置订单表数据,一条数据
         * 将订单表对象数据存放到订单表中
         * 将订单详细表集合数据批量添加到数据表中
         * 删除购物车
         */


        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);

        //将当前用户所有的购物车数据存放到list集合中
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }

        //获取当前用户信息 用于添加到表中
        User user = userService.getById(userId);

        //获取到需要配送的地址
        Long addressBookId = orders.getAddressBookId();

        //根据前台发送的地址找到地址对象数据
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }
        //获取订单号
        long orderId = IdWorker.getId();

        //原子对象 多线程安全
        AtomicInteger amount = new AtomicInteger(0);

        //填充订单详细表
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            //设置订单明细的订单号
            orderDetail.setOrderId(orderId);
            //设置订单当前单条数据的数量
            orderDetail.setNumber(item.getNumber());
            //设置订单菜品口味
            orderDetail.setDishFlavor(item.getDishFlavor());
            //设置订单菜品id
            orderDetail.setDishId(item.getDishId());
            //设置套餐id
            orderDetail.setSetmealId(item.getSetmealId());
            //菜品或者套餐
            orderDetail.setName(item.getName());
            //图片名称
            orderDetail.setImage(item.getImage());
            //当前这一份的金额
            orderDetail.setAmount(item.getAmount());
            // 相当于 +=    金额*份数 转换成valueInt   算当前菜品或者套餐的总价钱
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        //向订单表插入一条数据

        //订单号设置为id
        orders.setId(orderId);
        //设置下单的时间
        orders.setOrderTime(LocalDateTime.now());
        //设置支付的时间
        orders.setCheckoutTime(LocalDateTime.now());
        //设置待派送
        orders.setStatus(2);
        //总金额
        orders.setAmount(new BigDecimal(amount.get()));
        //设置用户id
        orders.setUserId(userId);
        //设置订单号
        orders.setNumber(String.valueOf(orderId));
        //用户的名称
        orders.setUserName(user.getName());
        //收货人
        orders.setConsignee(addressBook.getConsignee());
        //手机号
        orders.setPhone(addressBook.getPhone());
        //详细的地址信息
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //向订单表插入数据，一条数据 最终只需要
        this.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(wrapper);
    }
}