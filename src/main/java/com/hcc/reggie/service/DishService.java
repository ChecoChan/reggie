package com.hcc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hcc.reggie.dto.DishDto;
import com.hcc.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    /** 新增菜品，同时插入菜品对应的口味数据 */
    void saveWithFlavor(DishDto dishDto);

    /** 删除菜品，同时删除菜品对应的口味数据 */
    void deleteWithFlavor(List<Long> ids);

    /** 根据菜品 id 获取菜品信息 */
    DishDto getByIdWithFlavor(Long id);

    /** 根据 id 更新菜品信息 */
    void updateWithFlavor(DishDto dishDto);
}
