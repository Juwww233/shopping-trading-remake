package com.example.shopping3.service;

import com.example.shopping3.entity.Address;
import java.util.List;

public interface AddressService {
    // 业务方法与Mapper对应，保持语义一致
    List<Address> getUserAddresses(int userId);
    Address getAddressById(int id);
    void addAddress(Address address);
    void editAddress(Address address);
    void removeAddress(int id);
}