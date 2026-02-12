package com.getourguide.interview.helpers;

import com.getourguide.interview.entity.Supplier;

public class SupplierHelper {
    public static Supplier createSupplier(Long id, String name) {
        var supplier = new Supplier();
        supplier.setId(id);
        supplier.setName(name);
        return supplier;
    }
    
    public static Supplier createSupplier(Long id, String name, String address, String zip, String city, String country) {
        var supplier = new Supplier();
        supplier.setId(id);
        supplier.setName(name);
        supplier.setAddress(address);
        supplier.setZip(zip);
        supplier.setCity(city);
        supplier.setCountry(country);
        return supplier;
    }
}
