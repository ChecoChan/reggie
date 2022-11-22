package com.hcc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hcc.reggie.common.CustomException;
import com.hcc.reggie.dto.DishDto;
import com.hcc.reggie.entity.Dish;
import com.hcc.reggie.entity.DishFlavor;
import com.hcc.reggie.mapper.DishMapper;
import com.hcc.reggie.service.DishFlavorService;
import com.hcc.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService{
    @Autowired
    private DishFlavorService dishFlavorService;

    /** 新增菜品，同时插入菜品对应的口味数据 */
    @Override
    // 涉及多张表的操作，需要开启事务
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 保存基本信息到菜品表 dish
        this.save(dishDto);

        // 保存菜品口味到菜品口味表 dish_flavor
        // 获取菜品 id
        Long dishId = dishDto.getId();
        // 获取菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 给菜品口味赋上菜品 id
        flavors = flavors.stream().peek((item) -> item.setDishId(dishId)).collect(Collectors.toList());
        // 保存菜品
        dishFlavorService.saveBatch(flavors);
    }

    /** 删除菜品，同时删除菜品对应的口味数据 */
    @Override
    @Transactional
    public void deleteWithFlavor(List<Long> ids) {
        // 查询菜品状态，确定是否可以删除
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.in(Dish::getId, ids);
        dishQueryWrapper.eq(Dish::getStatus, 1);
        int count = this.count(dishQueryWrapper);

        // 如果菜品在售，不能删除，则抛出一个业务异常
        if (count > 0)
            throw new CustomException("菜品在售，不能删除，要删除请先停售");

        // 如果可以删除，则删除菜品表 dish 中的数据，再删除菜品口味表 dish_flavor 中的数据
        this.removeByIds(ids);
        LambdaQueryWrapper<DishFlavor> dishFlavorQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorQueryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(dishFlavorQueryWrapper);
    }

    /** 根据菜品 id 获取菜品信息 */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();

        // 查询菜品的基本信息
        Dish dish = this.getById(id);
        BeanUtils.copyProperties(dish, dishDto);

        // 查询菜品的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    /** 根据 id 更新菜品信息 */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新 dish 表基本信息
        this.updateById(dishDto);

        // 清理当前菜品对应口味数据，dish_flavor 表的 delete 操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        // 添加当前提交过来的口味数据，dish_flavor 表的 insert 操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().peek((item) -> item.setId(dishDto.getId())).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}
