package com.hcc.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcc.reggie.entity.Employee;
import com.hcc.reggie.mapper.EmployeeMapper;
import com.hcc.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;


@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
