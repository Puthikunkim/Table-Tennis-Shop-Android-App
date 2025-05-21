package com.example.app.Model;

import androidx.annotation.NonNull;

import java.util.List;

public class TableTennisProduct {
    private String id;
    private String name;
    private String description;
    private double price;
    private String categoryID;
    private List<String> tags;
    private boolean isWishlisted;
    private int cartQuantity;
    private int views;
    private List<String> imageUrls;

    // Firestore requires a no-arg constructor
    public TableTennisProduct() {}

    public TableTennisProduct(String id,
                              String name,
                              String description,
                              double price,
                              String categoryID,
                              List<String> tags,
                              boolean isWishlisted,
                              int cartQuantity,
                              int views,
                              List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryID = categoryID;
        this.tags = tags;
        this.isWishlisted = isWishlisted;
        this.cartQuantity = cartQuantity;
        this.views = views;
        this.imageUrls = imageUrls;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public String getCategoryID() {
        return categoryID;
    }
    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public boolean isWishlisted() {
        return isWishlisted;
    }
    public void setWishlisted(boolean wishlisted) {
        isWishlisted = wishlisted;
    }
    public int getCartQuantity() {
        return cartQuantity;
    }
    public void setCartQuantity(int cartQuantity) {
        this.cartQuantity = cartQuantity;
    }
    public int getViews() {
        return views;
    }
    public void setViews(int views) {
        this.views = views;
    }
    public List<String> getImageUrls() {
        return imageUrls;
    }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public String toString() {
        return "TableTennisProduct{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", categoryID='" + categoryID + '\'' +
                ", tags=" + tags +
                ", isWishlisted=" + isWishlisted +
                ", cartQuantity=" + cartQuantity +
                ", views=" + views +
                ", imageUrls=" + imageUrls +
                '}';
    }
}
