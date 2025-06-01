package com.example.app.Data;

import com.example.app.Model.TableTennisProduct;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FirestoreRepository {
    private static FirestoreRepository instance;
    private final FirebaseFirestore db;

    private FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreRepository getInstance() {
        if (instance == null) {
            instance = new FirestoreRepository();
        }
        return instance;
    }

    // Callback interfaces
    public interface ProductsCallback {
        void onSuccess(List<TableTennisProduct> products);
        void onError(Exception e);
    }

    public interface ProductDetailCallback {
        void onSuccess(TableTennisProduct product);
        void onError(Exception e);
    }

    public interface UserProfileCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // New: Callback for wishlist operations
    public interface WishlistOperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // New: Callback for fetching wishlist items
    public interface WishlistProductsCallback {
        void onSuccess(List<TableTennisProduct> products);
        void onError(Exception e);
    }


    /** Fetch all products in a given category */
    public void getProductsByCategory(String categoryId, ProductsCallback callback) {
        db.collection("products")
                .whereEqualTo("categoryID", categoryId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TableTennisProduct> list = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        TableTennisProduct p = doc.toObject(TableTennisProduct.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            list.add(p);
                        }
                    }
                    callback.onSuccess(list);

                })
                .addOnFailureListener(callback::onError);
    }

    /** Search products by name, description or tags (client‐side filtering) */
    public void searchProducts(String query, ProductsCallback callback) {
        db.collection("products")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String lower = query.toLowerCase();
                    List<TableTennisProduct> all = snapshot.toObjects(TableTennisProduct.class);
                    List<TableTennisProduct> filtered = new java.util.ArrayList<>();
                    for (int i = 0; i < all.size(); i++) {
                        TableTennisProduct p = all.get(i);
                        // set ID
                        p.setId(snapshot.getDocuments().get(i).getId());
                        boolean matches = (p.getName() != null && p.getName().toLowerCase().contains(lower))
                                || (p.getDescription() != null && p.getDescription().toLowerCase().contains(lower));
                        if (!matches && p.getTags() != null) {
                            for (String tag : p.getTags()) {
                                if (tag != null && tag.toLowerCase().contains(lower)) {
                                    matches = true;
                                    break;
                                }
                            }
                        }
                        if (matches) filtered.add(p);
                    }
                    callback.onSuccess(filtered);
                })
                .addOnFailureListener(callback::onError);
    }

    /** Fetch one product by its document ID */
    public void getProductById(String productId, ProductDetailCallback callback) {
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        TableTennisProduct product = doc.toObject(TableTennisProduct.class);
                        if (product != null) {
                            product.setId(doc.getId());
                            callback.onSuccess(product);
                        } else {
                            callback.onError(new NullPointerException("Product deserialized to null"));
                        }
                    } else {
                        callback.onError(new IllegalArgumentException("No such product: " + productId));
                    }
                })
                .addOnFailureListener(callback::onError);
    }


    public void createUserProfile(String userId, Map<String, Object> userProfileData, UserProfileCallback callback) {
        db.collection("users")
                .document(userId)
                .set(userProfileData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Adds a product to a user's wishlist.
     * The product is stored as a document in the 'wishlist' subcollection under the user's document.
     * We're storing the product's ID as the document ID in the wishlist for easy lookup.
     * The whole product object is stored in the subcollection.
     */
    public void addProductToWishlist(String userId, TableTennisProduct product, WishlistOperationCallback callback) {
        if (product.getId() == null) {
            callback.onError(new IllegalArgumentException("Product ID cannot be null when adding to wishlist."));
            return;
        }

        db.collection("users").document(userId)
                .collection("wishlist")
                .document(product.getId()) // Use product ID as document ID for wishlist item
                .set(product) // Store the entire product object
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Removes a product from a user's wishlist.
     */
    public void removeProductFromWishlist(String userId, String productId, WishlistOperationCallback callback) {
        db.collection("users").document(userId)
                .collection("wishlist")
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetches all products from a user's wishlist.
     */
    public void getWishlistProducts(String userId, WishlistProductsCallback callback) {
        db.collection("users").document(userId)
                .collection("wishlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TableTennisProduct> wishlist = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        TableTennisProduct product = doc.toObject(TableTennisProduct.class);
                        if (product != null) {
                            product.setId(doc.getId()); // Ensure the ID is set from the document ID
                            wishlist.add(product);
                        }
                    }
                    callback.onSuccess(wishlist);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Checks if a specific product is in a user's wishlist.
     * This is useful for updating the UI of product detail pages.
     */
    public void checkIfProductInWishlist(String userId, String productId, WishlistOperationCallback callback) {
        db.collection("users").document(userId)
                .collection("wishlist")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(); // Product is in wishlist
                    } else {
                        callback.onError(new Exception("Product not in wishlist")); // Product is not in wishlist
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void getTopViewedProducts(int limit, ProductsCallback callback) {
        db.collection("products")
                .orderBy("views", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TableTennisProduct> results = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        TableTennisProduct p = doc.toObject(TableTennisProduct.class);
                        p.setId(doc.getId());
                        results.add(p);
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetches a single random product from the "products" collection.
     * Useful for dynamically showcasing a featured item on the home screen.
     *
     * @param callback A callback interface to handle success or failure.
     *                 On success, returns a randomly selected TableTennisProduct.
     */
    public void getRandomProduct(ProductDetailCallback callback) {
        db.collection("products")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<DocumentSnapshot> docs = snapshot.getDocuments();
                    if (docs.isEmpty()) {
                        callback.onError(new Exception("No products found"));
                        return;
                    }

                    DocumentSnapshot randomDoc = docs.get(new Random().nextInt(docs.size()));
                    TableTennisProduct product = randomDoc.toObject(TableTennisProduct.class);
                    if (product != null) {
                        product.setId(randomDoc.getId());
                        callback.onSuccess(product);
                    } else {
                        callback.onError(new Exception("Failed to deserialize product"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

}

