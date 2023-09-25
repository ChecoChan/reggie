package com.hcc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcc.reggie.common.CustomException;
import com.hcc.reggie.common.R;
import com.hcc.reggie.dto.DishDto;
import com.hcc.reggie.dto.SetmealDto;
import com.hcc.reggie.entity.Category;
import com.hcc.reggie.entity.Dish;
import com.hcc.reggie.entity.Setmeal;
import com.hcc.reggie.entity.SetmealDish;
import com.hcc.reggie.mapper.SetmealMapper;
import com.hcc.reggie.service.CategoryService;
import com.hcc.reggie.service.SetmealDishService;
import com.hcc.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmaelServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealService setmealService;

    @Autowired CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /** 新增套餐，以及菜品关联的菜品信息 */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息，操作 setmeal 表
        this.save(setmealDto);

        // 保存套餐对应的菜品信息，操作 setmeal_dish 表
        List<SetmealDish> dishList = setmealDto.getSetmealDishes();
        dishList = dishList.stream().peek((item) -> {
            item.setSetmealId(setmealDto.getId());
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(dishList);
    }

    /** 删除套餐，同时删除套餐关联的菜品信息 */
    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        // 查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);

        // 不能删除，抛出一个业务异常
        if (count > 0)
            throw new CustomException("套餐中的菜品在售卖状态，不能删除");

        // 可以删除，先删除套餐表 setmeal 中的数据，再删除关系表 setmeal_dish 中的数据
        this.removeByIds(ids);
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(dishLambdaQueryWrapper);
    }

    @Override
    public R<SetmealDto> getByIdWithDish(String setmealId) {
        Setmeal setmeal = setmealService.getById(setmealId);
        if (setmeal == null)
            return R.error("查询套餐数据不存在");
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        String categoryName = categoryService.getById(setmealDto.getCategoryId()).getName();
        setmealDto.setCategoryName(categoryName);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        List<SetmealDish> setmealDish = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDish);
        return R.success(setmealDto);
    }
}
