package com.example.sbarai.openkart.Models;

/**
 * Created by zaheenkhan on 4/17/18.
 */

public class CatalogueItem {
    private String ProductDetails;
    private String ProductPriceRating;
    private String Productlink;
    private String category;
    private String category_link;

    public String getProductDetails() {
        return ProductDetails;
    }

    public void setProductDetails(String productDetails) {
        ProductDetails = productDetails;
    }

    public String getProductPriceRating() {
        return ProductPriceRating;
    }

    public void setProductPriceRating(String productPriceRating) {
        ProductPriceRating = productPriceRating;
    }

    public String getProductlink() {
        return Productlink;
    }

    public void setProductlink(String productlink) {
        Productlink = productlink;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory_link() {
        return category_link;
    }

    public void setCategory_link(String category_link) {
        this.category_link = category_link;
    }


}
