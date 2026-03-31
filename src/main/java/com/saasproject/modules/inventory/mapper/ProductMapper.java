package com.saasproject.modules.inventory.mapper;

import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.inventory.dto.ProductDto;
import com.saasproject.modules.inventory.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Product mapper for entity-DTO conversions.
 */
@Component
public class ProductMapper {

    public Product toEntity(ProductDto.CreateRequest dto) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .sku(dto.getSku())
                .barcode(dto.getBarcode())
                .category(dto.getCategory())
                .brand(dto.getBrand())
                .unit(dto.getUnit())
                .costPrice(dto.getCostPrice())
                .sellingPrice(dto.getSellingPrice())
                .taxRate(dto.getTaxRate())
                .currentStock(dto.getCurrentStock())
                .minStockLevel(dto.getMinStockLevel())
                .trackInventory(dto.isTrackInventory())
                .service(dto.isService())
                .active(true)
                .build();
    }

    public void updateEntity(Product entity, ProductDto.UpdateRequest dto) {
        if (dto.getName() != null)
            entity.setName(dto.getName());
        if (dto.getDescription() != null)
            entity.setDescription(dto.getDescription());
        if (dto.getSku() != null)
            entity.setSku(dto.getSku());
        if (dto.getBarcode() != null)
            entity.setBarcode(dto.getBarcode());
        if (dto.getCategory() != null)
            entity.setCategory(dto.getCategory());
        if (dto.getBrand() != null)
            entity.setBrand(dto.getBrand());
        if (dto.getUnit() != null)
            entity.setUnit(dto.getUnit());
        if (dto.getCostPrice() != null)
            entity.setCostPrice(dto.getCostPrice());
        if (dto.getSellingPrice() != null)
            entity.setSellingPrice(dto.getSellingPrice());
        if (dto.getTaxRate() != null)
            entity.setTaxRate(dto.getTaxRate());
        if (dto.getMinStockLevel() != null)
            entity.setMinStockLevel(dto.getMinStockLevel());
        if (dto.getMaxStockLevel() != null)
            entity.setMaxStockLevel(dto.getMaxStockLevel());
        if (dto.getReorderQuantity() != null)
            entity.setReorderQuantity(dto.getReorderQuantity());
        if (dto.getImageUrl() != null)
            entity.setImageUrl(dto.getImageUrl());
        if (dto.getActive() != null)
            entity.setActive(dto.getActive());
    }

    public ProductDto.Response toResponse(Product entity) {
        return ProductDto.Response.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .sku(entity.getSku())
                .barcode(entity.getBarcode())
                .category(entity.getCategory())
                .brand(entity.getBrand())
                .unit(entity.getUnit())
                .costPrice(entity.getCostPrice())
                .sellingPrice(entity.getSellingPrice())
                .taxRate(entity.getTaxRate())
                .priceWithTax(entity.getPriceWithTax())
                .currentStock(entity.getCurrentStock())
                .minStockLevel(entity.getMinStockLevel())
                .lowStock(entity.isLowStock())
                .active(entity.isActive())
                .service(entity.isService())
                .trackInventory(entity.isTrackInventory())
                .imageUrl(entity.getImageUrl())
                .createdAt(CommonUtils.formatDateTime(entity.getCreatedAt()))
                .updatedAt(CommonUtils.formatDateTime(entity.getUpdatedAt()))
                .build();
    }
}
