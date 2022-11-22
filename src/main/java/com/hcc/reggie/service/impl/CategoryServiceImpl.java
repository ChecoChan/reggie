package com.hcc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcc.reggie.common.CustomException;
import com.hcc.reggie.entity.Category;
import com.hcc.reggie.entity.Dish;
import com.hcc.reggie.entity.Setmeal;
import com.hcc.reggie.mapper.CategoryMapper;
import com.hcc.reggie.service.CategoryService;
import com.hcc.reggie.service.DishService;
import com.hcc.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long id) {
        // 查询当前分类是否关联了菜品，如果已经关联菜品，抛出业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if (count1 > 0)
            throw new CustomException("当前分类下关联了菜品，不能删除");

        // 查询当前分类是否关联了套餐，如果已经关联菜品，抛出业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if (count2 > 0)
            throw new CustomException("当前分类下关联了套餐，不能删除");

        // 正常删除
        super.removeById(id);
    }
}
