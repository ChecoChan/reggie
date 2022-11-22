package com.hcc.reggie.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hcc.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    /**
     * 根据 id 删除分类
     * 删除前要判断分类是否与具体菜品或套餐关联
     * */
    void remove(Long id);
}
