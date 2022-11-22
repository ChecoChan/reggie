package com.hcc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcc.reggie.common.BaseContext;
import com.hcc.reggie.common.R;
import com.hcc.reggie.entity.ShoppingCart;
import com.hcc.reggie.service.ShoppingCartService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;


    /** 添加购物车 */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("添加菜品：{}", shoppingCart);

        // 设置用户 id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        // 判断添加到购物车的是菜品还是套餐
        if (dishId != null) {
            // 添加的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            // 添加的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);


        if (cart != null) {
            // 如果添加的菜品或套餐已存在，则数量加一
            Integer number = cart.getNumber();
            cart.setNumber(number + 1);
            shoppingCartService.updateById(cart);
        } else {
            // 如果添加的菜品或套餐已存在，数量设置为一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }

        // 返回结果
        return R.success(cart);
    }

    /** 查看购物车 */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车");
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> cartList = shoppingCartService.list(queryWrapper);
        return R.success(cartList);
    }


    /** 情况购物车 */
    @DeleteMapping("clean")
    public R<String> clean() {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        shoppingCartService.remove(queryWrapper);
        return R.success("清除购物车成功");
    }
}
