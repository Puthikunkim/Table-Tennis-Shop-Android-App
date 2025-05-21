package com.example.app.Model;

import androidx.annotation.NonNull;

import java.util.List;

public class TableTennisCategory {
    private String id;
    private String name;
    private String iconUrl;
    private List<TableTennisProduct> productList;

    // No-arg constructor for Firestore
    public TableTennisCategory() {}

    public TableTennisCategory(String id,
                               String name,
                               String iconUrl,
                               List<TableTennisProduct> productList) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.productList = productList;
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
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<TableTennisProduct> getProductList() {
        return productList;
    }
    public void setProductList(List<TableTennisProduct> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public String toString() {
        return "TableTennisCategory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", productList=" + productList +
                '}';
    }
}