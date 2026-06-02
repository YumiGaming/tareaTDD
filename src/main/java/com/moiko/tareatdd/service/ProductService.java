package com.moiko.tareatdd.service;

import com.moiko.tareatdd.exception.InvalidProductException;
import com.moiko.tareatdd.model.Product;
import com.moiko.tareatdd.validator.ProductValidator;

public class ProductService {

    private final ProductValidator validator = new ProductValidator();

    public Product createProductFromText(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            throw new InvalidProductException("El texto de entrada no puede estar vacío");
        }

        // Split keeping empty trailing values to prevent ignoring missing last fields
        String[] parts = plainText.split(",", -1);
        if (parts.length != 6) {
            throw new InvalidProductException("El formato del texto plano es incorrecto. Debe contener exactamente 6 campos separados por coma.");
        }

        String code = parts[0].trim();
        String name = parts[1].trim();
        String stockStr = parts[2].trim();
        String priceStr = parts[3].trim();
        String rut = parts[4].trim();
        String email = parts[5].trim();

        // Validate fields
        validator.validate(code, name, stockStr, priceStr, rut, email);

        // Parse types (validator ensures these will parse successfully without exception)
        int stock = Integer.parseInt(stockStr);
        double price = Double.parseDouble(priceStr);

        return new Product(code, name, stock, price, rut, email);
    }
}
