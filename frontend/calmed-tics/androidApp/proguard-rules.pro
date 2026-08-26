# ProGuard / R8 rules for androidApp (CalmEd Android Application)

# ==============================================================================
# Stack trace readability
# ==============================================================================
# Retain line numbers and file names for readable stack traces in crash logs
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retain annotations, generic signatures, inner classes, and enclosing methods
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions
