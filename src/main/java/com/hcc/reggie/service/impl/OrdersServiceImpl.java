package com.hcc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcc.reggie.common.BaseContext;
import com.hcc.reggie.common.CustomException;
import com.hcc.reggie.entity.*;
import com.hcc.reggie.mapper.OrdersMapper;
import com.hcc.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        // 获得用户 id
        Long userId = BaseContext.getCurrentId();

        // 查询用户当前购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> cartList = shoppingCartService.list(queryWrapper);

        // 如果购物车为空则不能下单，抛出业务异常
        if (cartList == null || cartList.size() == 0)
            throw new CustomException("购物车为空，无法下单");

        // 查询用户数据和地址数据
        User user = userService.getById(userId);
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null)
            throw new CustomException("地址信息有误，不能下单");

        // 向订单表插入数据，一条数据
        // 设置订单 id 和订单号
        long orderId = IdWorker.getId();
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        // 设置下单时间和结账时间，订单状态为待派送
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        // 设置订单实收金额
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> detailList = cartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        orders.setAmount(new BigDecimal(amount.get()));
        // 设置用户 Id、用户名、联系人、联系方式和地址
        orders.setUserId(user.getId());
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                        + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                        + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                        + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        // 保存订单信息
        this.save(orders);

        // 向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(detailList);

        // 清空购物车
        shoppingCartService.remove(queryWrapper);
    }
}
