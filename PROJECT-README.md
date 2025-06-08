## Why did we change the wishlist schema?
- Did not need all the relationships, could insert an instance of a product object into users carts or wishlists.  
- Did not make sense to have an instance of a product for a singular quantity and then determine where than 1 item  
was in a cart or a wishlist. Made more sense to generalize.  

## Misc.
The class diagram from our design docs has been fully implemented in code, with the changes noted and justified below.

- **Added classes:**  
  - Adapter hierarchy to support RecyclerViews:  
    - `BaseProductAdapter` (extended by `ProductAdapter`, `RecommendationsAdapter`, `WishListAdapter`)  
    - `CartAdapter`, `ImageSliderAdapter`, `RecentSearchAdapter`  
  - Utility classes for helping with general app functionality:  
    - `AnimationUtils`, `ErrorHandler`, `ImageLoader`, `NavigationUtils`, `ToastUtils`, `UIStateManager`  
  - `AuthManager` class for firebase auth.  

- **Diagram classes not implemented:**  
  - `TableTennisCategory` class, helper classes for searchactivity (`SearchHistoryManager`, `FilterEngine`), `RecommendationEngine` class for `DetailsActivity`, `User` class.  
  > _Rationale:_ These were not implemented due to revised scope and prioritisation as implementing them would have required additional back end logic that we determined was unnecessary.  

- **Diagram classes implemented as stated in design docs:**  
  - `BaseActivity`, `CartActivity`, `DetailsActivity`, `ListActivity`, `MainActivity`, `ProfileActivity`, `SearchActivity`, `WishListActivity`, `TableTennisProduct`, `FirestoreRepository`.  

- **Associations:**  
  - Inheritance and composition remain consistent with the original diagram  
  - All class relationships have been maintained or updated with rationale explained during our demo.  

## AI Declaration
We utilised AI tools to enhance code comments for better readability, fill knowledge gaps when adding minor features, and streamline bug fixes. AI was also employed to generate the images used throughout the app.
