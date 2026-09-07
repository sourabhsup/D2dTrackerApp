# PantryPal

An Android app for a household to track grocery inventory, plan meals, and
keep a shopping list that's generated from what's actually running low —
shared live between two (or more) phones.

- **Inventory** — what you have at home, by category, with quantities and
  low-stock alerts.
- **Meal Plan** — a 7-day planner (breakfast/lunch/dinner) built from your
  own recipes, each with an ingredient list.
- **Shopping List** — add items by hand, or tap "Refresh from inventory &
  plan" to pull in anything low on stock plus whatever this week's meal
  plan needs that you don't already have. Check items off as you shop, then
  "Add to inventory" folds the purchased quantities straight back in.
- **Two phones, one household** — the app has no server of its own; it
  syncs through a free Firebase project. Create a household on one phone,
  get a 6-character code, enter that code on the second phone, and both
  phones read/write the same data in real time (works even offline —
  changes sync once you're back online).

No accounts or passwords: the app signs in anonymously and the household
code is what ties two phones together.

## Tech stack

Kotlin, Jetpack Compose (Material 3), Firebase Firestore + Firebase
Anonymous Auth, AndroidX DataStore (to remember which household this phone
is in), Navigation Compose. Minimum Android version: **Android 8.0 (API
26)**.

## Important — read this first

This project was built in an environment with no Android SDK and no
network access to Google's Maven repository, so **the code has not been
compiled here**. It's written using standard, well-established
Compose/Firebase patterns, but budget time to fix any small build issues
(a typo, a version mismatch) the first time you build it in Android Studio,
which will have much better tooling for that than this environment did.

## 1. Create a free Firebase project

You need your own Firebase project — it's what lets your two phones talk to
each other, and it's free for an app this size (Firestore's free tier is
far more than a household inventory app will ever use).

1. Go to the [Firebase console](https://console.firebase.google.com/) and
   click **Add project**. Name it anything (e.g. "PantryPal").
   You can leave Google Analytics off.
2. In your new project, click **Build → Firestore Database → Create
   database**. Start in **production mode** (the app's own security rules
   below handle access control). Pick any region.
3. Once the database is created, open the **Rules** tab and replace the
   default rules with the contents of [`firestore.rules`](firestore.rules)
   from this repo, then click **Publish**.
4. Click **Build → Authentication → Get started**. Under **Sign-in
   method**, enable **Anonymous**.
5. Register an Android app: from the project overview page, click the
   Android icon (or **Project settings → Add app → Android**).
   - **Android package name:** `com.pantrypal.app` (must match exactly —
     it's set in `app/build.gradle.kts` as the `applicationId`).
   - Nickname/SHA-1 are optional for this app.
   - Download the **`google-services.json`** file it offers you.
6. Copy that `google-services.json` file into the `app/` folder of this
   project (next to `app/build.gradle.kts`). It's gitignored on purpose —
   each person who builds the app supplies their own.

## 2. Open and build the project

1. Install [Android Studio](https://developer.android.com/studio) (the
   free official IDE — this is the easiest path, especially for installing
   onto phones).
2. Open this repository's root folder (`D2dTrackerApp`) in Android Studio
   as an existing project. Let it sync Gradle (first sync downloads
   dependencies and can take a few minutes).
3. If Android Studio flags anything about the Android Gradle Plugin or
   Kotlin version, let it apply the suggested upgrade — versions drift over
   time and newer Studio releases sometimes want newer plugin versions.

You can also build from the command line once you have the Android SDK
installed (e.g. via Android Studio's SDK Manager) and `ANDROID_HOME` set:

```bash
./gradlew assembleDebug
# APK lands at app/build/outputs/apk/debug/app-debug.apk
```

## 3. Install on both phones

**Easiest: run from Android Studio.**

1. On each phone: enable Developer Options (Settings → About phone → tap
   "Build number" 7 times), then enable **USB debugging** inside the new
   Developer Options menu.
2. Connect the phone by USB, accept the "Allow USB debugging" prompt on the
   phone.
3. In Android Studio, pick the phone from the device dropdown and click
   **Run** (▶). Repeat for the second phone (one at a time, or both
   connected together if your machine has enough USB ports — Android
   Studio will let you pick which device to run on, or run on both).

**Alternative: share the APK directly.**

After `./gradlew assembleDebug`, copy `app-debug.apk` to each phone (email,
cloud drive, USB transfer) and open it there. You'll need to allow
"Install unknown apps" for whichever app you used to open the file (Android
will prompt you the first time).

## 4. Link the two phones

1. Open the app on **phone A**. Choose **Create household**, give it a
   name (e.g. "The Smiths"), and you'll get a 6-character code.
2. Open the app on **phone B**. Choose **Join household** and enter that
   code.
3. Both phones now show the same inventory, meal plan, and shopping list —
   add an item on one phone and it appears on the other within a second or
   two (as long as both have internet access; Firestore also caches data
   offline and syncs once you're back online).

To add a third phone (or reinstall the app later), just repeat step 2 with
the same code — it's shown any time under the **Settings** tab, along with
buttons to copy or share it.

## Project layout

```
app/src/main/java/com/pantrypal/app/
  data/model/        Plain data classes stored in Firestore
  data/repository/    Firestore reads/writes, exposed as Kotlin Flows
  data/session/        Which household this phone belongs to (DataStore)
  ui/onboarding/       Create/join household screen
  ui/inventory/         Inventory list, add/edit item
  ui/mealplan/           Weekly meal plan + recipe management
  ui/shoppinglist/        Shopping list, auto-generation, purchase → inventory
  ui/settings/            Household code, share, leave
  ui/navigation/          Bottom nav + NavHost
  ui/theme/               Material 3 theme
firestore.rules       Firestore security rules (paste into Firebase console)
```
