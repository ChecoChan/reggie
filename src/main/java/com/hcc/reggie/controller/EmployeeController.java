package com.hcc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcc.reggie.common.R;
import com.hcc.reggie.entity.Employee;
import com.hcc.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /** 员工登录 */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        /**
         * 1. 将页面提交的 password 进行 md5 加密处理
         * 2. 根据页面提交的用户 username 查询数据库
         * 3. 如果查询不到数据则返回账号不存在
         * 4. 密码比对，如果不一致则返回密码错误
         * 5. 查看员工状态，如果为已禁用状态，则返回员工已禁用
         * 6. 登录成功，将员工 id 存入 Session 并返回登录成功
         */

        // 1. 将页面提交的 password 进行 md5 加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2. 根据页面提交的用户 username 查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3. 如果查询不到数据则返回账号不存在
        if (emp == null)
            return R.error("账号不存在");

        // 4. 密码比对，如果不一致则返回登录失败
        if (!emp.getPassword().equals(password))
            return R.error("密码错误");


        // 5. 查看员工状态，如果为已禁用状态，则返回员工已禁用
        if (emp.getStatus() == 0)
            return R.error("账号已禁用");

        // 6. 登录成功，将员工 id 存入 Session 并返回登录成功
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /** 员工退出 */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        /**
         * 1. 清理 Session 中的用户 id
         * 2. 返回结果
         */

        // 1. 清理 Session 中的用户 id
        request.getSession().removeAttribute("employee");

        // 2. 返回结果
        return R.success("退出成功");
    }

    /** 添加员工 */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        HttpSession session = request.getSession();

        // 设置初始密码 123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 设置创建时间
        employee.setCreateTime(LocalDateTime.now());

        // 开启 MybatisPlus 字段自动填充，不在需要手动设置一个个字段了
        // 设置更新时间
        // employee.setUpdateTime(LocalDateTime.now());
        // 设置用户创建人 id
        // Long empId = (Long) session.getAttribute("employee");
        // employee.setCreateUser(empId);
        // 设置用户更新人 id
        // employee.setUpdateUser(empId);

        // 保存用户
        employeeService.save(employee);
        log.info("新增员工：{}", employee.toString());

        // 返回添加成功信息
        return R.success("新增员工成功");
    }

    /** 员工分页查询 */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        // 条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        // 添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(pageInfo, queryWrapper);

        // 返回结果
        return R.success(pageInfo);
    }

    /** 根据 id 修改员工信息 */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        HttpSession session = request.getSession();
        /*
        // 设置更新时间
        employee.setUpdateTime(LocalDateTime.now());
        // 设置发起更新的用户
        employee.setUpdateUser((Long) session.getAttribute("employee"));
        */
        // 更新信息
        employeeService.updateById(employee);

        // 返回成功信息
        return R.success("员工信息更改成功");
    }

    /** 根据 id 查询员工信息 */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据 id 查询员工信息......");
        Employee employee = employeeService.getById(id);
        if (employee != null)
            return R.success(employee);
        return R.error("没有查询到对应员工信息");
    }
}
