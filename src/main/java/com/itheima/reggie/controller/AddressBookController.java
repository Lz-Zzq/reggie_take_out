package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    private final AddressBookService addressBookService;

    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    /**
     * 显示所有的地址
     */
    @GetMapping("/list")
    public R<List<AddressBook>> listAddress(){

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 修改默认地址
     */
    @PutMapping("/default")
    public R<String> defaultAddressUpdate(@RequestBody AddressBook addressBook){
        log.info("默认地址Id: {}",addressBook);
        //需要修改默认地址数据的id

        LambdaUpdateWrapper<AddressBook> queryWrap = new LambdaUpdateWrapper<>();
        //数据库中是否存在与当前登陆id相同的数据  找到所有关于当前账户的地址
        queryWrap.eq(AddressBook::getUserId,BaseContext.getCurrentId());

        log.info("账户的id:{}",BaseContext.getCurrentId());

        //对当前账户对应的地址
        queryWrap.set(AddressBook::getIsDefault,0);

        addressBookService.update(queryWrap);

        //将当前选中的地址设置为默认值
        addressBook.setIsDefault(1);
        //更新到数据库中
        addressBookService.updateById(addressBook);


        return R.success("默认地址更新成功");
    }

    /**
     * 默认地址
     */
    @GetMapping("/default")
    public R<AddressBook> defaultAddress(){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook one = addressBookService.getOne(queryWrapper);
        return R.success(one);
    }


    /*
     * 添加地址
     */
    @PostMapping
    public R<String> saveAddress(@RequestBody AddressBook addressBook){
        log.info("当前登陆的用户id{}",BaseContext.getCurrentId());
        addressBook.setCreateUser(BaseContext.getCurrentId());
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        return R.success("添加成功");
    }

    /**
     * 数据回显
     */
    @GetMapping("/{id}")
    public R<AddressBook> addressBookEcho(@PathVariable Long id){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getId,id);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        return R.success(addressBook);
    }

    /**
     * 修改方法
     */
    @PutMapping
    public R<String> updateAddress(@RequestBody AddressBook addressBook){
        LambdaUpdateWrapper<AddressBook> queryWrap = new LambdaUpdateWrapper<>();
        queryWrap.eq(AddressBook:: getId,addressBook.getId());

        addressBookService.update(addressBook,queryWrap);

        return R.success("修改成功");
    }


}
