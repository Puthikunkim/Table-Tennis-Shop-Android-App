# Table Tennis E-Commerce App

A modern Android application for browsing, searching, and purchasing table tennis products. Built with Firebase backend, user authentication, and a clean, user-friendly interface.

## Features

- **Product Catalog**: Browse bats, balls, tables, and more with detailed product pages.
- **Search & Filter**: Powerful search with recent history, filtering, and sorting.
- **Wishlist**: Add/remove products to your wishlist (requires sign-in).
- **Cart**: Add products to your cart, view totals, and checkout.
- **User Authentication**: Sign up, sign in, and manage your profile securely with Firebase Auth.
- **Recommendations**: See top picks and featured products on the homepage.
- **Firestore Integration**: All product, cart, wishlist, and user data is managed via Firebase Firestore.
- **Responsive UI**: Built with Material Design and ViewBinding for a smooth experience.

## Screenshots
*Add screenshots here if available*

## Project Structure

```
app/
├── src/main/java/com/example/app/
│   ├── UI/         # Activities for Main, Search, Details, Cart, Profile, Wishlist, List
│   ├── Model/      # Data models (e.g., TableTennisProduct)
│   ├── Data/       # FirestoreRepository for all DB operations
│   ├── Auth/       # AuthManager for Firebase Authentication
│   ├── Adapters/   # RecyclerView adapters for products, wishlist, cart, etc.
│   └── Util/       # Utility classes (navigation, error handling, etc.)
├── res/            # Layouts, drawables, values
├── AndroidManifest.xml
├── build.gradle.kts
└── google-services.json
```

## Getting Started

### Prerequisites
- Android Studio (latest recommended)
- Java 11+
- Firebase account (for your own deployment)

### Setup
1. **Clone the repository**
2. **Open in Android Studio**
3. **Firebase Setup**:
   - The project is pre-configured for Firebase (Firestore, Auth, Analytics).
   - If you fork or redeploy, replace `google-services.json` with your own from the Firebase Console.
4. **Build & Run**
   - Connect an Android device or use an emulator.
   - Click Run ▶️ in Android Studio.

### Dependencies
- Firebase Firestore, Auth, Analytics
- Google Material Components
- Glide (image loading)
- Gson (JSON parsing)

All dependencies are managed in `build.gradle.kts`.

## Core Classes & Architecture

- **MainActivity**: Homepage with categories, featured product, and top picks.
- **SearchActivity**: Search bar, recent searches, filtering, and sorting.
- **DetailsActivity**: Product details, add to cart, wishlist toggle, recommendations.
- **CartActivity**: View, update, and checkout cart items.
- **ProfileActivity**: Sign in/up, profile management, cart/wishlist summary.
- **WishListActivity**: Manage wishlist items.
- **FirestoreRepository**: Singleton for all Firestore DB operations (products, cart, wishlist, user profiles).
- **AuthManager**: Handles Firebase Auth (sign in, sign up, sign out).
- **Adapters**: For RecyclerViews (products, wishlist, cart, recommendations).
- **Model/TableTennisProduct**: Main data model for products.

## Customization
- To add new product categories, update Firestore and UI category cards.
- To change branding, update resources in `res/` and app icons.

## ProGuard
- Default ProGuard rules are provided. No special configuration is required unless you add custom serialization or reflection-heavy libraries.

## License
*Specify your license here (e.g., MIT, Apache 2.0, etc.)*

## Authors
- Team 11, COMPSYS 302, 2025

---
*For any issues or contributions, please open an issue or submit a pull request.* 