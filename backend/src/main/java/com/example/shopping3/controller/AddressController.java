package com.example.shopping3.controller;

import com.example.shopping3.entity.Address;
import com.example.shopping3.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 标记为REST控制器，返回JSON数据
@RestController
// 统一前缀：所有地址相关接口都以/address开头
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // 查询用户所有地址：GET请求，示例：http://localhost:8080/address/user/1
    @GetMapping("/user/{userId}")
    public List<Address> getUserAddresses(@PathVariable int userId) {
        return addressService.getUserAddresses(userId);
    }

    // 根据ID查询地址：GET请求，示例：http://localhost:8080/address/1
    @GetMapping("/{id}")
    public Address getAddressById(@PathVariable int id) {
        return addressService.getAddressById(id);
    }

    // 新增地址：POST请求，JSON传参
    @PostMapping
    public String addAddress(@RequestBody Address address) {
        addressService.addAddress(address);
        return "新增地址成功";
    }

    // 修改地址：PUT请求，JSON传参
    @PutMapping
    public String editAddress(@RequestBody Address address) {
        addressService.editAddress(address);
        return "修改地址成功";
    }

    // 删除地址：DELETE请求，示例：http://localhost:8080/address/1
    @DeleteMapping("/{id}")
    public String removeAddress(@PathVariable int id) {
        addressService.removeAddress(id);
        return "删除地址成功";
    }
}