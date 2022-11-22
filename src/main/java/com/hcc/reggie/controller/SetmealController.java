package com.hcc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcc.reggie.common.R;
import com.hcc.reggie.dto.SetmealDto;
import com.hcc.reggie.entity.Category;
import com.hcc.reggie.entity.Setmeal;
import com.hcc.reggie.entity.SetmealDish;
import com.hcc.reggie.service.CategoryService;
import com.hcc.reggie.service.SetmealDishService;
import com.hcc.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping(("/setmeal"))
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /** 新增套餐 */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("新增套餐：{}", setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /** 套餐分页显示 */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPageInfo = new Page<>();
        // 条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        // 添加过滤条件
        queryWrapper.eq(name != null, Setmeal::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        // 执行查询
        setmealService.page(pageInfo, queryWrapper);

        // 对象属性拷贝, 除开 records 属性
        BeanUtils.copyProperties(pageInfo, dtoPageInfo, "records");
        // 对象属性拷贝，records 属性
        List<Setmeal> pageInfoRecords = pageInfo.getRecords();
        List<SetmealDto> dtoPageInfoRecords = getSetmealDto(pageInfoRecords);
        dtoPageInfo.setRecords(dtoPageInfoRecords);

        // 返回结果
        return R.success(dtoPageInfo);
    }

    /** 删除套餐 */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除套餐：{}", ids);
        setmealService.deleteWithDish(ids);
        return R.success("删除套餐成功");
    }

    /** 根据条件查询对应分类菜品信息 */
    @GetMapping("/list")
    public R<List<SetmealDto>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        // 状态 1 是起售，状态 0 是停售
        queryWrapper.eq(Setmeal::getStatus, 1);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        List<SetmealDto> setmealDtoList = getSetmealDto(setmealList);
        return R.success(setmealDtoList);
    }

    /** 从 List<Setmeal> 获得 List<SetmealDto> */
    public List<SetmealDto> getSetmealDto(List<Setmeal> setmealList) {
        return setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();

            // 复制 Setmeal 的属性
            BeanUtils.copyProperties(item, setmealDto);

            // 给 SetmealDto 的 setmealDishes 属性赋值
            Long setmealId = item.getId();
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
            List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
            setmealDto.setSetmealDishes(setmealDishList);

            // 给 SetmealDto 的 categoryName 属性赋值
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            setmealDto.setCategoryName(category.getName());

            // 返回 SetmealDto 对象
            return setmealDto;
        }).collect(Collectors.toList());
    }
}
