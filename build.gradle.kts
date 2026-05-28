plugins {
    alias(libs.plugins.android.application) apply false
    // Ye line add karni hai
    id("com.google.gms.google-services") version "4.4.1" apply false
}