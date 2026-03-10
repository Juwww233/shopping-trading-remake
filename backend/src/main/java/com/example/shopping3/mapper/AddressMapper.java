package com.example.shopping3.mapper;

import com.example.shopping3.entity.Address;
import java.util.List;

// 已通过启动类@MapperScan扫描，无需再加@Mapper
public interface AddressMapper {
    // 方法名与XML中SQL的id完全一致
    List<Address> getUserAddresses(int user_id);
    Address getAddressById(int id);
    void insertAddress(Address address);
    void updateAddress(Address address);
    void deleteAddress(int id);
}