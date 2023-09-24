package com.hcc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcc.reggie.common.R;
import com.hcc.reggie.entity.Orders;
import com.hcc.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    /** 用户下单 */
    @PostMapping("submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("提交订单：{}", orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /** 后台订单分页查询接口 */
    @GetMapping("/page")
    public R<Page<Orders>> page(int page,
                                int pageSize,
                                String number,
                                String beginTime,
                                String endTime) {
        Page<Orders> pageInfo = new Page<>();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(number), Orders::getNumber, number);
        queryWrapper.between(beginTime != null && endTime != null, Orders::getCheckoutTime, beginTime, endTime);
        ordersService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }
}
