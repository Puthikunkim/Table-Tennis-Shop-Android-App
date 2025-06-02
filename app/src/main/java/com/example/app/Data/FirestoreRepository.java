package com.example.app.Data;

import com.example.app.Model.TableTennisProduct;
import com.google.firebase.firestore.FirebaseFirestore;
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

    public interface WishlistOperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface WishlistProductsCallback {
        void onSuccess(List<TableTennisProduct> products);
        void onError(Exception e);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

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

    public void searchProducts(String query, ProductsCallback callback) {
        db.collection("products")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String lower = query.toLowerCase();
                    List<TableTennisProduct> all = snapshot.toObjects(TableTennisProduct.class);
                    List<TableTennisProduct> filtered = new ArrayList<>();
                    for (int i = 0; i < all.size(); i++) {
                        TableTennisProduct p = all.get(i);
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

    public void addProductToWishlist(String userId, TableTennisProduct product, WishlistOperationCallback callback) {
        if (product.getId() == null) {
            callback.onError(new IllegalArgumentException("Product ID cannot be null when adding to wishlist."));
            return;
        }
        db.collection("users").document(userId)
                .collection("wishlist")
                .document(product.getId())
                .set(product)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void removeProductFromWishlist(String userId, String productId, WishlistOperationCallback callback) {
        db.collection("users").document(userId)
                .collection("wishlist")
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getWishlistProducts(String userId, WishlistProductsCallback callback) {
        db.collection("users").document(userId)
                .collection("wishlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TableTennisProduct> wishlist = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        TableTennisProduct product = doc.toObject(TableTennisProduct.class);
                        if (product != null) {
                            product.setId(doc.getId());
                            wishlist.add(product);
                        }
                    }
                    callback.onSuccess(wishlist);
                })
                .addOnFailureListener(callback::onError);
    }

    public void checkIfProductInWishlist(String userId, String productId, WishlistOperationCallback callback) {
        db.collection("users").document(userId)
                .collection("wishlist")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(new Exception("Product not in wishlist"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void addToCart(String userId, TableTennisProduct product, int quantity, OperationCallback callback) {
        if (product.getId() == null) {
            callback.onError(new IllegalArgumentException("Product ID cannot be null when adding to cart."));
            return;
        }

        db.collection("users").document(userId)
                .collection("cart")
                .document(product.getId())
                .get()
                .addOnSuccessListener(docSnapshot -> {
                    if (docSnapshot.exists()) {
                        // Already in cart: increment quantity
                        Long currentQty = docSnapshot.getLong("quantity");
                        int newQty = (currentQty != null ? currentQty.intValue() : 0) + quantity;

                        Map<String, Object> update = new HashMap<>();
                        update.put("quantity", newQty);

                        db.collection("users").document(userId)
                                .collection("cart")
                                .document(product.getId())
                                .update(update)
                                .addOnSuccessListener(aVoid -> {
                                    if (callback != null) callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onError(e);
                                });

                    } else {
                        // Not in cart: add as new item
                        Map<String, Object> cartItem = new HashMap<>();
                        cartItem.put("product", product);
                        cartItem.put("quantity", quantity);

                        db.collection("users").document(userId)
                                .collection("cart")
                                .document(product.getId())
                                .set(cartItem)
                                .addOnSuccessListener(aVoid -> {
                                    if (callback != null) callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onError(e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }


    public void getCartItems(String userId, ProductsCallback callback) {
        db.collection("users").document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TableTennisProduct> cartItems = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        TableTennisProduct product = doc.get("product", TableTennisProduct.class);
                        Long qty = doc.getLong("quantity");

                        if (product != null) {
                            product.setId(doc.getId()); // Optional: use Firestore doc ID
                            product.setCartQuantity(qty != null ? qty.intValue() : 1);
                            cartItems.add(product);
                        }
                    }
                    callback.onSuccess(cartItems);
                })
                .addOnFailureListener(callback::onError);
    }

    public void removeFromCart(String userId, String productId, OperationCallback callback) {
        db.collection("users").document(userId)
                .collection("cart")
                .document(productId)
                .delete()
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
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

    public void clearCart(String userId, OperationCallback callback) {
        db.collection("users").document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }

    public void clearWishlist(String userId, OperationCallback callback) {
        db.collection("users").document(userId)
                .collection("wishlist")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }


}
