package com.hcc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hcc.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {
    /** 用户下单 */
    void submit(Orders orders);
}
