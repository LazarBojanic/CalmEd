# Consumer ProGuard / R8 rules for the CalmEd Shared Module (KMP / Android)
# These rules are consumed by any Android application target that depends on this shared library.

# ==============================================================================
# Cast Framework
# ==============================================================================
# CastOptionsProvider is loaded reflectively by the Cast Framework from the
# AndroidManifest meta-data entry OPTIONS_PROVIDER_CLASS_NAME.
-keep class com.calmed.calmedtics.cast.CastOptionsProvider {
    public <init>();
    *;
}
