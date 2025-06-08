package com.example.app.Model;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Data model representing a table tennis product.
 * This class maps directly to documents in the "products" Firestore collection.
 */
public class TableTennisProduct {
    // Firestore document ID (not stored as a field in Firestore itself)
    private String id;

    // Basic product info
    private String name;
    private String description;
    private double price;
    private String categoryID;

    // Tags for search/filter functionality
    private List<String> tags;

    // Quantity of this product added to the user's cart
    private int cartQuantity;

    // Number of times this product has been viewed
    private int views;

    // URLs to product images
    private List<String> imageUrls;

    // Required no-argument constructor for Firestore deserialisation
    public TableTennisProduct() {}

    // Full constructor
    public TableTennisProduct(String id,
                              String name,
                              String description,
                              double price,
                              String categoryID,
                              List<String> tags,
                              int cartQuantity,
                              int views,
                              List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryID = categoryID;
        this.tags = tags;
        this.cartQuantity = cartQuantity;
        this.views = views;
        this.imageUrls = imageUrls;
    }

    // Getters and setters, used by Firestore and UI binding logic
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

    // For debugging/logging, prints out all the fields of the product
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
                ", cartQuantity=" + cartQuantity +
                ", views=" + views +
                ", imageUrls=" + imageUrls +
                '}';
    }
}
