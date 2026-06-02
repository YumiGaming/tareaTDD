package com.moiko.tareatdd.validator;

import com.moiko.tareatdd.exception.InvalidProductException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ProductValidator {

    public void validate(String code, String name, String stockStr, String priceStr, String rut, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductException("El nombre del producto no puede estar vacío");
        }

        if (!isValidName(name)) {
            throw new InvalidProductException("El nombre debe contener a lo más 30 letras y solo caracteres alfabéticos y espacios");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new InvalidProductException("El código del producto no puede estar vacío");
        }

        if (!isValidCode(code, name)) {
            throw new InvalidProductException("El código debe contener la inicial del nombre en Mayúscula, seguida de una fecha y hora válidas (formato ddMMyyHHmm)");
        }

        if (stockStr == null || stockStr.trim().isEmpty()) {
            throw new InvalidProductException("El stock no puede estar vacío");
        }

        int stock = validateNumericStock(stockStr);

        if (priceStr == null || priceStr.trim().isEmpty()) {
            throw new InvalidProductException("El precio no puede estar vacío");
        }

        double price = validateNumericPrice(priceStr);

        if (rut == null || rut.trim().isEmpty()) {
            throw new InvalidProductException("El RUT del proveedor no puede estar vacío");
        }

        if (!isValidRutFormatAndDv(rut)) {
            throw new InvalidProductException("El RUT ingresado no es un RUT válido");
        }

        if (!isRutInCsv(rut)) {
            throw new InvalidProductException("El proveedor con el RUT indicado no se encuentra en la lista de proveedores autorizados");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new InvalidProductException("El mail del proveedor no puede estar vacío");
        }

        if (!isValidEmail(email)) {
            throw new InvalidProductException("El mail del proveedor debe tener un formato válido");
        }
    }

    private boolean isValidName(String name) {
        if (name.length() > 30) {
            return false;
        }
        // Allows alphabetical letters (including Spanish accents/ntilde) and spaces
        return name.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$");
    }

    private boolean isValidCode(String code, String name) {
        if (code.length() != 11) {
            return false;
        }
        
        char expectedInitial = Character.toUpperCase(name.charAt(0));
        if (code.charAt(0) != expectedInitial) {
            return false;
        }

        String dateStr = code.substring(1);
        if (!dateStr.matches("^[0-9]{10}$")) {
            return false;
        }

        // Validate date-time parts ddMMyyHHmm
        try {
            int day = Integer.parseInt(dateStr.substring(0, 2));
            int month = Integer.parseInt(dateStr.substring(2, 4));
            int year = Integer.parseInt(dateStr.substring(4, 6)) + 2000;
            int hour = Integer.parseInt(dateStr.substring(6, 8));
            int minute = Integer.parseInt(dateStr.substring(8, 10));

            if (month < 1 || month > 12) return false;
            if (hour < 0 || hour > 23) return false;
            if (minute < 0 || minute > 59) return false;

            int[] daysInMonth = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            if (month == 2) {
                boolean isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
                int maxDays = isLeap ? 29 : 28;
                if (day < 1 || day > maxDays) return false;
            } else {
                if (day < 1 || day > daysInMonth[month]) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int validateNumericStock(String stockStr) {
        try {
            int stock = Integer.parseInt(stockStr.trim());
            if (stock < 0) {
                throw new InvalidProductException("El stock debe ser un valor numérico no negativo");
            }
            return stock;
        } catch (NumberFormatException e) {
            throw new InvalidProductException("El stock debe ser numérico");
        }
    }

    private double validateNumericPrice(String priceStr) {
        try {
            double price = Double.parseDouble(priceStr.trim());
            if (price < 0) {
                throw new InvalidProductException("El precio debe ser un valor numérico no negativo");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new InvalidProductException("El precio debe ser numérico");
        }
    }

    private boolean isValidRutFormatAndDv(String rut) {
        // General format check (dots are optional, hyphen is required, DV is digit or K/k)
        if (!rut.matches("^(\\d{1,2}(\\.\\d{3}){2}-[\\dkK]|\\d{7,8}-[\\dkK])$")) {
            return false;
        }

        String cleanRut = rut.replace(".", "").trim();
        int hyphenIndex = cleanRut.indexOf('-');
        if (hyphenIndex == -1) {
            return false;
        }

        String body = cleanRut.substring(0, hyphenIndex);
        char dv = cleanRut.charAt(hyphenIndex + 1);

        return validateRutDv(body, dv);
    }

    private boolean validateRutDv(String body, char dv) {
        int sum = 0;
        int factor = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int remainder = sum % 11;
        int dvCalculated = 11 - remainder;
        char expectedDv;
        if (dvCalculated == 11) {
            expectedDv = '0';
        } else if (dvCalculated == 10) {
            expectedDv = 'K';
        } else {
            expectedDv = Character.forDigit(dvCalculated, 10);
        }
        return Character.toUpperCase(dv) == Character.toUpperCase(expectedDv);
    }

    private boolean isRutInCsv(String rut) {
        String cleanInputRut = normalizeRut(rut);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("proveedores.csv")) {
            if (is == null) {
                throw new RuntimeException("El archivo proveedores.csv no fue encontrado en el classpath");
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length > 0) {
                        String csvRut = normalizeRut(parts[0]);
                        if (csvRut.equals(cleanInputRut)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // If exception, we log and return false
        }
        return false;
    }

    private String normalizeRut(String rut) {
        if (rut == null) return "";
        return rut.replace(".", "").replace("-", "").trim().toUpperCase();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}
