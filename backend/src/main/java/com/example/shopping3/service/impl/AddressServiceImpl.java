package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Address;
import com.example.shopping3.mapper.AddressMapper;
import com.example.shopping3.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// 标记为Spring服务组件，纳入容器管理
@Service
public class AddressServiceImpl implements AddressService {

    // 自动注入Mapper（无需手动创建SqlSession）
    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> getUserAddresses(int userId) {
        return addressMapper.getUserAddresses(userId);
    }

    @Override
    public Address getAddressById(int id) {
        return addressMapper.getAddressById(id);
    }

    @Override
    public void addAddress(Address address) {
        addressMapper.insertAddress(address);
    }

    @Override
    public void editAddress(Address address) {
        addressMapper.updateAddress(address);
    }

    @Override
    public void removeAddress(int id) {
        addressMapper.deleteAddress(id);
    }
}