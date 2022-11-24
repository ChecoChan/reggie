package com.hcc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcc.reggie.common.R;
import com.hcc.reggie.dto.DishDto;
import com.hcc.reggie.entity.Category;
import com.hcc.reggie.entity.Dish;
import com.hcc.reggie.entity.DishFlavor;
import com.hcc.reggie.service.CategoryService;
import com.hcc.reggie.service.DishFlavorService;
import com.hcc.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /** 新增菜品 */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("添加菜品成功");
    }

    /** 删除菜品 */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除菜品 id：{}", ids);
        dishService.deleteWithFlavor(ids);
        return R.success("删除菜品信息成功");
    }

    /** 菜品分页查询 */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 分页构造器
        Page<Dish> dishPageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPageInfo = new Page<>();
        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 添加过滤条件
        queryWrapper.eq(StringUtils.isNotEmpty(name), Dish::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        // 执行查询
        dishService.page(dishPageInfo, queryWrapper);

        // 对象属性拷贝, 除开 records 属性
        BeanUtils.copyProperties(dishPageInfo, dishDtoPageInfo, "records");
        // 对象属性拷贝，records 属性
        List<Dish> dishPageInfoRecords = dishPageInfo.getRecords();
        /*
        List<DishDto>  dishDtoPageInfoRecords = dishPageInfoRecords.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 分类 id
            Long categoryId = item.getCategoryId();
            // 根据 id 查询分类对象
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());
        */
        List<DishDto>  dishDtoPageInfoRecords = getDishDto(dishPageInfoRecords);
        dishDtoPageInfo.setRecords(dishDtoPageInfoRecords);

        // 返回结果
        return R.success(dishDtoPageInfo);
    }

    /** 根据 id 查询菜品信息 */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /** 根据 id 修改菜品信息 */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        // 清理被更新菜品的 Redis 缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("修改成功");
    }

    /** 起售菜品 */
    @PostMapping("/status/1")
    public R<String> status1(@RequestParam List<Long> ids) {
        log.info("起售菜品 id：{}", ids);
        LambdaUpdateWrapper<Dish> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 0);
        queryWrapper.set(Dish::getStatus, 1);
        dishService.update(queryWrapper);
        return R.success("菜品起售成功");
    }

    /** 停售菜品 */
    @PostMapping("/status/0")
    public R<String> status0(@RequestParam List<Long> ids) {
        log.info("停售菜品 id：{}", ids);
        LambdaUpdateWrapper<Dish> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.set(Dish::getStatus, 0);
        dishService.update(queryWrapper);
        return R.success("菜品停售成功");
    }

    /*
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 状态 1 是起售，状态 0 是停售
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);
        return R.success(dishList);
    }
    */
    /*
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 状态 1 是起售，状态 0 是停售
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);
        List<DishDto> dtoDishList = getDishDto(dishList);
        return R.success(dtoDishList);
    }
     */
    /** 根据条件查询对应的菜品数据，添加 Redis 缓存 */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dtoDishList = null;
        // 动态构造 key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

        // 从 Redis 中获取缓存数据
        dtoDishList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        // 如果 Redis 不存在数据，需要查数据库，并将结果缓存到 Redis
        if (dtoDishList == null) {
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
            // 状态 1 是起售，状态 0 是停售
            queryWrapper.eq(Dish::getStatus, 1);
            queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
            List<Dish> dishList = dishService.list(queryWrapper);
            dtoDishList = getDishDto(dishList);
            // 将数据库查询到的数据缓存到 Redis，过期时间是 60 分钟
            redisTemplate.opsForValue().set(key, dtoDishList, 60, TimeUnit.MINUTES);
        }

        // 返回结果
        return R.success(dtoDishList);
    }

    /** 从 List<Dish> 获得 List<DishDto> */
    public List<DishDto> getDishDto(List<Dish> dishList) {
        return dishList.stream().map((item) -> {
            // 复制基本属性
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            // 复制分类名
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);

            // 复制口味列表
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());
    }
}
