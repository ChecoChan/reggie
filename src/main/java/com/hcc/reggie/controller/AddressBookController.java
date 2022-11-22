package com.hcc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hcc.reggie.common.BaseContext;
import com.hcc.reggie.common.R;
import com.hcc.reggie.dto.SetmealDto;
import com.hcc.reggie.entity.AddressBook;
import com.hcc.reggie.entity.Setmeal;
import com.hcc.reggie.entity.User;
import com.hcc.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("新增收获地址：{}", addressBook);
        addressBookService.save(addressBook);
        return R.success("添加成功");
    }

    /** 全部地址显示 */
    @GetMapping("/list")
    public R<List<AddressBook>> list() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    /** 设置默认地址 */
    @PutMapping("/default")
    public R<String> defaultAddress(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址的：{}", addressBook);
        LambdaUpdateWrapper<AddressBook> queryWrapper = new LambdaUpdateWrapper<>();

        // 将这个用户的所有地址都设为非默认地址
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(queryWrapper);

        // 设置选中的地址为默认地址
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success("设置默认地址成功");
    }

    /** 订单显示默认地址 */
    @GetMapping("/default")
    public R<AddressBook> getDefaultAddress() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        AddressBook defaultAddress = addressBookService.getOne(queryWrapper);
        return R.success(defaultAddress);
    }
}
