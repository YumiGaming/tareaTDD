package com.moiko.tareatdd.model;

public class Product {
    private String code;
    private String name;
    private int stock;
    private double price;
    private String supplierRut;
    private String supplierEmail;

    public Product() {
    }

    public Product(String code, String name, int stock, double price, String supplierRut, String supplierEmail) {
        this.code = code;
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.supplierRut = supplierRut;
        this.supplierEmail = supplierEmail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSupplierRut() {
        return supplierRut;
    }

    public void setSupplierRut(String supplierRut) {
        this.supplierRut = supplierRut;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    @Override
    public String toString() {
        return "Product{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", stock=" + stock +
                ", price=" + price +
                ", supplierRut='" + supplierRut + '\'' +
                ", supplierEmail='" + supplierEmail + '\'' +
                '}';
    }
}
