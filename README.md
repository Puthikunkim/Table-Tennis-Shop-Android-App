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

## Demo
### Home Page
<img src="https://github.com/user-attachments/assets/19a60516-64f6-4d3d-a3e7-dafdf74b251a" width="200">
<img src="https://github.com/user-attachments/assets/2f319d4d-4a6c-4131-9b9b-53bf5f6b4cf4" width="200">

### Search Page
<img src="https://github.com/user-attachments/assets/baac315f-4299-463d-81d2-8ad30a55323d" width="200">
<img src="https://github.com/user-attachments/assets/bf306df5-d76a-45ea-9b7b-c39cf8f3b60b" width="200">
<img src="https://github.com/user-attachments/assets/bf805ca0-9f8e-4d82-bbac-66a1e5f26aeb" width="200">

### Category Page
<img src="https://github.com/user-attachments/assets/08b78d71-cb79-4a85-ac80-e967149df17d" width="200">

### Item Page
<img src="https://github.com/user-attachments/assets/f9e89d94-8bb8-4060-b910-8ea4c1819d59" width="200">
<img src="https://github.com/user-attachments/assets/1e3eadfd-ab1c-4769-a295-e7d3f0e2c31b" width="200">

### Wishlist Page
<img src="https://github.com/user-attachments/assets/14a41828-0488-4998-9a3c-02d9e24bc48b" width="200">
<img src="https://github.com/user-attachments/assets/10c3ad4b-8a14-4d71-9d92-46c259347371" width="200">
<img src="https://github.com/user-attachments/assets/4282db85-6be9-4668-9893-7416fdf1be58" width="200">

### Cart Page
<img src="https://github.com/user-attachments/assets/e86869fe-3c9f-438f-9ff6-ddb467fa47bf" width="200">
<img src="https://github.com/user-attachments/assets/7e5dd87b-98b6-49d2-a1cf-eb48b19951ac" width="200">
<img src="https://github.com/user-attachments/assets/09f39c72-46f6-4c64-823f-478db165b3f3" width="200">

### Sign In Page
<img src="https://github.com/user-attachments/assets/cefce983-8335-4540-8f57-74df2814d6e2" width="200">
<img src="https://github.com/user-attachments/assets/c6e57ab3-4e60-440a-b281-eb8c35c0c47b" width="200">
<img src="https://github.com/user-attachments/assets/a15286c3-4c54-4ef7-934c-9f1b563c753a" width="200">
<img src="https://github.com/user-attachments/assets/6e067e19-d48d-425e-a11e-c46e201086da" width="200">

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

## Authors
- Rhett Murdoch
- Josh Shiu
- Jerry Kim

