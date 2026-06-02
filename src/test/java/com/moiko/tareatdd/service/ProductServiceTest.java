package com.moiko.tareatdd.service;

import com.moiko.tareatdd.exception.InvalidProductException;
import com.moiko.tareatdd.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    public void setUp() {
        productService = new ProductService();
    }

    // 1. Success Cases
    @Test
    public void testCreateProduct_SuccessWithValidRut() {
        // J1011221226 is initial 'J' + date 10/11/22 12:26. Name is "Jabón Copito".
        // RUT 16.827.524-2 is mathematically valid and in CSV.
        String plainText = "J1011221226,Jabón Copito,200,990,16.827.524-2,proveedor@copito.com";
        
        Product product = productService.createProductFromText(plainText);
        
        assertNotNull(product);
        assertEquals("J1011221226", product.getCode());
        assertEquals("Jabón Copito", product.getName());
        assertEquals(200, product.getStock());
        assertEquals(990.0, product.getPrice());
        assertEquals("16.827.524-2", product.getSupplierRut());
        assertEquals("proveedor@copito.com", product.getSupplierEmail());
    }

    @Test
    public void testCreateProduct_SuccessWithValidRutAlternative() {
        // P0206261530 is initial 'P' + date 02/06/26 15:30. Name is "Pan con Queso".
        // RUT 76.543.212-K is mathematically valid and in CSV.
        String plainText = "P0206261530,Pan con Queso,100,1200,76.543.212-K,distribuidora@moiko.cl";
        
        Product product = productService.createProductFromText(plainText);
        
        assertNotNull(product);
        assertEquals("P0206261530", product.getCode());
        assertEquals("Pan con Queso", product.getName());
        assertEquals(100, product.getStock());
        assertEquals(1200.0, product.getPrice());
        assertEquals("76.543.212-K", product.getSupplierRut());
        assertEquals("distribuidora@moiko.cl", product.getSupplierEmail());
    }

    // 2. Format / Parsing Cases
    @Test
    public void testCreateProduct_NullInput_ThrowsException() {
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(null);
        });
    }

    @Test
    public void testCreateProduct_EmptyInput_ThrowsException() {
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText("");
        });
    }

    @Test
    public void testCreateProduct_InsufficientFields_ThrowsException() {
        // Only 5 fields
        String plainText = "J1011221226,Jabón Copito,200,990,16.827.524-2";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    @Test
    public void testCreateProduct_TooManyFields_ThrowsException() {
        // 7 fields
        String plainText = "J1011221226,Jabón Copito,200,990,16.827.524-2,proveedor@copito.com,ExtraField";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    // 3. Product Code Validation Cases
    @Test
    public void testCreateProduct_CodeInitialMismatch_ThrowsException() {
        // Code starts with 'A' but name starts with 'J' ("Jabón")
        String plainText = "A1011221226,Jabón Copito,200,990,16.827.524-2,proveedor@copito.com";
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
        assertTrue(exception.getMessage().contains("inicial"), "Should mention initial mismatch");
    }

    @Test
    public void testCreateProduct_CodeInitialLowercase_ThrowsException() {
        // Code starts with lowercase 'j'
        String plainText = "j1011221226,Jabón Copito,200,990,16.827.524-2,proveedor@copito.com";
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
        assertTrue(exception.getMessage().contains("inicial") || exception.getMessage().toLowerCase().contains("mayúscula"));
    }

    @Test
    public void testCreateProduct_CodeInvalidDateFormat_ThrowsException() {
        // Date part is not 10 digits (9 digits)
        String plainText = "J101122122,Jabón Copito,200,990,16.827.524-2,proveedor@copito.com";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    @Test
    public void testCreateProduct_CodeNonNumericDate_ThrowsException() {
        // Date part contains letters
        String plainText = "J101122122A,Jabón Copito,200,990,16.827.524-2,proveedor@copito.com";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    @Test
    public void testCreateProduct_CodeInvalidDateTimeValue_ThrowsException() {
        // Month 13 is invalid
        String plainText = "J1013221226,Jabón Copito,200,990,16.827.524-2,proveedor@copito.com";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    // 4. Product Name Validation Cases
    @Test
    public void testCreateProduct_NameTooLong_ThrowsException() {
        // Name is 31 letters
        String longName = "Jabón Copito Jabón Copito Jabón";
        assertEquals(31, longName.length());
        String plainText = "J1011221226," + longName + ",200,990,16.827.524-2,proveedor@copito.com";
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
        assertTrue(exception.getMessage().contains("Nombre") || exception.getMessage().contains("30"));
    }

    @Test
    public void testCreateProduct_NameWithInvalidCharacters_ThrowsException() {
        // Name contains numbers or special characters (only letters and spaces allowed)
        String plainText = "J1011221226,Jabón Copito 123,200,990,16.827.524-2,proveedor@copito.com";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    // 5. Stock Validation Cases
    @Test
    public void testCreateProduct_StockNonNumeric_ThrowsException() {
        String plainText = "J1011221226,Jabón Copito,abc,990,16.827.524-2,proveedor@copito.com";
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
        assertTrue(exception.getMessage().contains("Stock") || exception.getMessage().contains("numérico"));
    }

    @Test
    public void testCreateProduct_StockNegative_ThrowsException() {
        String plainText = "J1011221226,Jabón Copito,-5,990,16.827.524-2,proveedor@copito.com";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    // 6. Price Validation Cases
    @Test
    public void testCreateProduct_PriceNonNumeric_ThrowsException() {
        String plainText = "J1011221226,Jabón Copito,200,xyz,16.827.524-2,proveedor@copito.com";
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
        assertTrue(exception.getMessage().contains("Precio") || exception.getMessage().contains("numérico"));
    }

    @Test
    public void testCreateProduct_PriceNegative_ThrowsException() {
        String plainText = "J1011221226,Jabón Copito,200,-100,16.827.524-2,proveedor@copito.com";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    // 7. RUT Validation Cases
    @Test
    public void testCreateProduct_RutInvalidFormat_ThrowsException() {
        // RUT is not in standard Chilean format
        String plainText = "J1011221226,Jabón Copito,200,990,16827524,proveedor@copito.com";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    @Test
    public void testCreateProduct_RutInvalidVerificationDigit_ThrowsException() {
        // RUT has invalid DV (DV is 1 but mathematically should be 2)
        String plainText = "J1011221226,Jabón Copito,200,990,16.827.524-1,proveedor@copito.com";
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
        assertTrue(exception.getMessage().contains("RUT") || exception.getMessage().contains("válido") || exception.getMessage().contains("verificador"));
    }

    @Test
    public void testCreateProduct_RutNotInCsv_ThrowsException() {
        // RUT is mathematically valid (18.256.784-1 is valid) but is not in proveedores.csv
        String plainText = "J1011221226,Jabón Copito,200,990,18.256.784-1,proveedor@copito.com";
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
        assertTrue(exception.getMessage().contains("proveedor") || exception.getMessage().contains("csv") || exception.getMessage().contains("lista"));
    }

    // 8. Email Validation Cases
    @Test
    public void testCreateProduct_EmailInvalidFormat_ThrowsException() {
        String plainText = "J1011221226,Jabón Copito,200,990,16.827.524-2,proveedorcopito.com";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }

    @Test
    public void testCreateProduct_EmailMissingAt_ThrowsException() {
        String plainText = "J1011221226,Jabón Copito,200,990,16.827.524-2,proveedor_at_copito.com";
        assertThrows(InvalidProductException.class, () -> {
            productService.createProductFromText(plainText);
        });
    }
}
