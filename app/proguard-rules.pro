# Add project specific ProGuard rules here.
# Firestore model classes are (de)serialized via reflection; keep their fields.
-keepclassmembers class com.pantrypal.app.data.model.** {
  *;
}
