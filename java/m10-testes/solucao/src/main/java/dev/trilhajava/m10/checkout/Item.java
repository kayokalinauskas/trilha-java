package dev.trilhajava.m10.checkout;

import java.math.BigDecimal;

public record Item(String sku, int quantidade, BigDecimal precoUnitario) {
}
