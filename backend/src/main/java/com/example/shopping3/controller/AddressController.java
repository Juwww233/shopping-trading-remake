package com.example.shopping3.controller;

import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Address;
import com.example.shopping3.entity.User;
import com.example.shopping3.service.AddressService;
import com.example.shopping3.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private SessionUtil sessionUtil;

    @GetMapping("/user/{userId}")
    public Result<List<Address>> getUserAddresses(@PathVariable int userId) {
        try {
            List<Address> list = addressService.getUserAddresses(userId);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取地址列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Address> getAddressById(@PathVariable int id) {
        try {
            Address address = addressService.getAddressById(id);
            if (address == null) {
                return Result.error("地址不存在");
            }
            return Result.success(address);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取地址失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<String> addAddress(@RequestBody Address address,
                                     @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录或会话过期");
        }
        try {
            address.setUserId(user.getId());
            addressService.addAddress(address);
            return Result.success("新增地址成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增地址失败：" + e.getMessage());
        }
    }

    @PutMapping
    public Result<String> editAddress(@RequestBody Address address,
                                      @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录或会话过期");
        }
        try {
            addressService.editAddress(address);
            return Result.success("修改地址成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改地址失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> removeAddress(@PathVariable int id,
                                        @RequestHeader("X-Session-Id") String sessionId) {
        User user = (User) sessionUtil.getSession(sessionId);
        if (user == null) {
            return Result.error("未登录或会话过期");
        }
        try {
            addressService.removeAddress(id);
            return Result.success("删除地址成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除地址失败：" + e.getMessage());
        }
    }
}