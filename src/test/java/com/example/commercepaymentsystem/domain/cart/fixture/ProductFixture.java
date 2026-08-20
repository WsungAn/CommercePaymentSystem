package com.example.commercepaymentsystem.domain.cart.fixture;

import com.example.commercepaymentsystem.domain.product.entity.Product;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductFixture {

    private ProductFixture() {}

    public static String PRODUCT_NAME = "Product Name";
    public static int PRODUCT_PRICE = 10000;
    public static int PRODUCT_STOCK = 5;
    public static String PRODUCT_DESCRIPTION = "Product Description";
    public static String PRODUCT_CATEGORY = "Product Category";

    public static Product createProduct() {
        Product product = new Product(
                PRODUCT_NAME,
                PRODUCT_PRICE,
                PRODUCT_STOCK,
                PRODUCT_DESCRIPTION,
                PRODUCT_CATEGORY
        );
        ReflectionTestUtils.setField(product, "id", 1L);
        return product;
    }
}
