package com.hcc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hcc.reggie.common.R;
import com.hcc.reggie.dto.SetmealDto;
import com.hcc.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /** 新增套餐，以及套餐关联的菜品信息 */
    void saveWithDish(SetmealDto setmealDto);

    /** 删除套餐，同时删除套餐关联的菜品信息 */
    void deleteWithDish(List<Long> ids);

    /** 根据套餐 id 查询套餐关联菜品信息 */
    R<SetmealDto> getByIdWithDish(String setmealId);

    /** 修改套餐信息 */
    R<String> edit(SetmealDto setmealDto);
}
