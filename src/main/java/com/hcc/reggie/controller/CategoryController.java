package com.hcc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hcc.reggie.common.R;
import com.hcc.reggie.entity.Category;
import com.hcc.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /** 新增分类 */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category{}", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /** 分页查询 */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        // 构造分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        // 添加排序条件
        queryWrapper.orderByDesc(Category::getSort);

        // 执行查询
        categoryService.page(pageInfo, queryWrapper);

        // 返回结果
        return R.success(pageInfo);
    }

    /** 根据 id 删除分类 */
    @DeleteMapping
    public R<String> delete(Long id) {
        log.info("删除分类, id 为 {}", id);
        categoryService.remove(id);
        return R.success("分类已删除");
    }

    /** 根据 id 修改分类 */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Category category) {
        log.info("修改分类信息：{}", category);
        // 更新信息
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /** 根据条件查询分类 */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        // 添加排序条件
        queryWrapper.orderByDesc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
