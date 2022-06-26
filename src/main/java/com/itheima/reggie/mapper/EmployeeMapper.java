package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author 刘政
 * @Date 2022/5/27 21:11
 * @Software IDEA
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
