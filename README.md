# 🚗 RentWheels – AI-Powered Vehicle Rental App

RentWheels is a feature-rich Android application designed for intelligent vehicle rentals. It offers dynamic pricing based on demand, time, and location using AI. This project supports multiple vehicle types including cars, bikes, trucks, and more, and provides a smooth booking experience integrated with secure payments.

---

## 📱 Features

* 🔍 **Vehicle Filtering** – Filter by model, type, and availability.
* 🧠 **AI-Based Dynamic Pricing** – Pricing adapts to demand, time, and location.
* 💳 **Integrated Payments** – Razorpay integration for secure transactions.
* 🗓️ **Booking Management** – Track, book, and cancel rentals with ease.
* 👤 **User Profile** – Manage your profile and view past rentals.
* 📦 **Vehicle Categories Supported:**

  * Self-drive and chauffeur-driven cars
  * Bikes and scooters (including electric)
  * Trucks and commercial vehicles
  * Limousines and luxury vehicles

---

## 🧠 AI Functionality

The app leverages AI/ML components for:

* **Dynamic Pricing Engine**: Adapts pricing based on:

  * Peak vs. off-peak hours
  * Seasonal trends
  * Real-time demand
  * Location factors

> All dynamic pricing logic is centralized in classes like `Booking`, `BookingDao`, and `InvoiceGenerator`.

---

## 🏗️ Project Structure

```
📦 RentWheels/
├── 📁 java/
│   └── 📁 com/example/enhanceddynamicpricing/
│       ├── AppDatabase.java                  # Room Database configuration
│       ├── Booking.java                      # Booking model class
│       ├── BookingAdapter.java               # RecyclerView Adapter for bookings
│       ├── BookingDao.java                   # Data Access Object (DAO) for bookings
│       ├── BookingsFragment.java             # UI Fragment to display bookings
│       ├── DateConverter.java                # Type converter for Room
│       ├── InvoiceAdapter.java               # Adapter for invoice RecyclerView
│       ├── InvoiceGenerator.java             # Logic for dynamic pricing and invoice
│       ├── MainActivity.java                 # App launcher activity
│       ├── PaymentActivity.java              # Razorpay payment integration
│       ├── ProfileFragment.java              # User profile section
│       ├── SeasonalFragment.java             # Displays seasonal offers/prices
│       ├── VehicleModel.java                 # Vehicle model representation
│       ├── VehicleModelAdapter.java          # Adapter for vehicle models
│       ├── VehicleType.java                  # Enum or class for vehicle types
│       └── VehicleTypeAdapter.java           # Adapter for vehicle types

```

```
📁 res/
├── 📁 layout/
│   ├── activity_main.xml                     # Main activity layout
│   ├── activity_payment.xml                  # Payment screen layout
│   ├── bottom_sheet_vehicle_details.xml      # Bottom sheet for vehicle info
│   ├── dialog_about_us.xml                   # About Us dialog
│   ├── dialog_booking_details.xml            # Booking detail popup
│   ├── dialog_face_verification.xml          # Face verification dialog
│   ├── dialog_filter.xml                     # Vehicle filter dialog
│   ├── dialog_terms_conditions.xml           # Terms & Conditions dialog
│   ├── fragment_bookings.xml                 # Bookings screen
│   ├── fragment_profile.xml                  # User profile screen
│   ├── fragment_seasonal.xml                 # Seasonal recommendations screen
│   ├── item_booking.xml                      # Booking list item UI
│   ├── item_invoice.xml                      # Invoice list item UI
│   ├── item_vehicle_model.xml                # Vehicle model list item UI
│   ├── item_vehicle_type.xml                 # Vehicle type list item UI
├── 📁 menu/
│   └── bottom_nav_menu.xml                   # Bottom navigation configuration
├── 📁 mipmap/                                # App icons
├── 📁 values/
│   └── strings.xml, colors.xml, styles.xml   # Resources
├── 📁 xml/
│   ├── backup_rules.xml                      # Backup config
│   └── data_extraction_rules.xml             # Data extraction settings

```

---

## 💳 Razorpay Payment Integration

**Status:** ✅ Fully Integrated

* Implemented in `PaymentActivity.java`
* Automatically fetches amount based on vehicle & pricing logic
* Accepts all UPI/Cards/Wallets

---

## 🧪 Testing# 🚗 RentWheels – AI-Powered Vehicle Rental App

RentWheels is a feature-rich Android application designed for intelligent vehicle rentals. It offers dynamic pricing based on demand, time, and location using AI. This project supports multiple vehicle types including cars, bikes, trucks, and more, and provides a smooth booking experience integrated with secure payments.

---

## 📱 Features

* 🔍 **Vehicle Filtering** – Filter by model, type, and availability.
* 🧠 **AI-Based Dynamic Pricing** – Pricing adapts to demand, time, and location.
* 💳 **Integrated Payments** – Razorpay integration for secure transactions.
* 🗓️ **Booking Management** – Track, book, and cancel rentals with ease.
* 👤 **User Profile** – Manage your profile and view past rentals.
* 📦 **Vehicle Categories Supported:**

  * Self-drive and chauffeur-driven cars
  * Bikes and scooters (including electric)
  * Trucks and commercial vehicles
  * Limousines and luxury vehicles

---

## 🧠 AI Functionality

The app leverages AI/ML components for:

* **Dynamic Pricing Engine**: Adapts pricing based on:

  * Peak vs. off-peak hours
  * Seasonal trends
  * Real-time demand
  * Location factors

> All dynamic pricing logic is centralized in classes like `Booking`, `BookingDao`, and `InvoiceGenerator`.

---

## 🏗️ Project Structure

```
📦 RentWheels/
├── 📁 java/
│   └── 📁 com/example/enhanceddynamicpricing/
│       ├── AppDatabase.java                  # Room Database configuration
│       ├── Booking.java                      # Booking model class
│       ├── BookingAdapter.java               # RecyclerView Adapter for bookings
│       ├── BookingDao.java                   # Data Access Object (DAO) for bookings
│       ├── BookingsFragment.java             # UI Fragment to display bookings
│       ├── DateConverter.java                # Type converter for Room
│       ├── InvoiceAdapter.java               # Adapter for invoice RecyclerView
│       ├── InvoiceGenerator.java             # Logic for dynamic pricing and invoice
│       ├── MainActivity.java                 # App launcher activity
│       ├── PaymentActivity.java              # Razorpay payment integration
│       ├── ProfileFragment.java              # User profile section
│       ├── SeasonalFragment.java             # Displays seasonal offers/prices
│       ├── VehicleModel.java                 # Vehicle model representation
│       ├── VehicleModelAdapter.java          # Adapter for vehicle models
│       ├── VehicleType.java                  # Enum or class for vehicle types
│       └── VehicleTypeAdapter.java           # Adapter for vehicle types
```

```
📁 res/
├── 📁 layout/
│   ├── activity_main.xml                     # Main activity layout
│   ├── activity_payment.xml                  # Payment screen layout
│   ├── bottom_sheet_vehicle_details.xml      # Bottom sheet for vehicle info
│   ├── dialog_about_us.xml                   # About Us dialog
│   ├── dialog_booking_details.xml            # Booking detail popup
│   ├── dialog_face_verification.xml          # Face verification dialog
│   ├── dialog_filter.xml                     # Vehicle filter dialog
│   ├── dialog_terms_conditions.xml           # Terms & Conditions dialog
│   ├── fragment_bookings.xml                 # Bookings screen
│   ├── fragment_profile.xml                  # User profile screen
│   ├── fragment_seasonal.xml                 # Seasonal recommendations screen
│   ├── item_booking.xml                      # Booking list item UI
│   ├── item_invoice.xml                      # Invoice list item UI
│   ├── item_vehicle_model.xml                # Vehicle model list item UI
│   ├── item_vehicle_type.xml                 # Vehicle type list item UI
├── 📁 menu/
│   └── bottom_nav_menu.xml                   # Bottom navigation configuration
├── 📁 mipmap/                                # App icons
├── 📁 values/
│   └── strings.xml, colors.xml, styles.xml   # Resources
├── 📁 xml/
│   ├── backup_rules.xml                      # Backup config
│   └── data_extraction_rules.xml             # Data extraction settings
```

---

## 💳 Razorpay Payment Integration

**Status:** ✅ Fully Integrated

* Implemented in `PaymentActivity.java`
* Automatically fetches amount based on vehicle & pricing logic
* Accepts all UPI/Cards/Wallets

---

## 🧪 Testing

* ✔️ Room database tested via `BookingDao`
* ✔️ Fragments & Adapters tested with dummy data
* ✔️ Razorpay sandbox tested
* ✔️ Pricing logic tested with seasonal and location-based simulations

---

## 📦 Dependencies

```gradle
// Room Database
implementation "androidx.room:room-runtime:2.4.0"
annotationProcessor "androidx.room:room-compiler:2.4.0"

// Razorpay
implementation 'com.razorpay:checkout:1.6.20'

// RecyclerView, Lifecycle, etc.
implementation 'androidx.recyclerview:recyclerview:1.2.1'
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.4.0'
```

---

## 🛠️ How to Run

1. Clone the repo:

   ```bash
   git clone https://github.com/NORTEX0030/RentWheels-Using-AI
   ```

2. Open in **Android Studio Arctic Fox+**

3. Connect a device or emulator

4. Run the app

5. For Razorpay, add your API key in `PaymentActivity.java`

---

## 📃 License

This project is licensed under the [MIT License](LICENSE).


* ✔️ Room database tested via `BookingDao`
* ✔️ Fragments & Adapters tested with dummy data
* ✔️ Razorpay sandbox tested
* ✔️ Pricing logic tested with seasonal and location-based simulations

---




---

## 📦 Dependencies

```gradle
// Room Database
implementation "androidx.room:room-runtime:2.4.0"
annotationProcessor "androidx.room:room-compiler:2.4.0"

// Razorpay
implementation 'com.razorpay:checkout:1.6.20'

// RecyclerView, Lifecycle, etc.
implementation 'androidx.recyclerview:recyclerview:1.2.1'
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.4.0'
```

---

## 🛠️ How to Run

1. Clone the repo:

   ```bash
   git clone https://github.com/NORTEX0030/RentWheels-Using-AI
   ```

2. Open in **Android Studio Arctic Fox+**

3. Connect a device or emulator

4. Run the app

5. For Razorpay, add your API key in `PaymentActivity.java`

---

## 📃 License

This project is licensed under the [MIT License](LICENSE).
