bitalino-android-example
========================

BITalino Android example application that uses my own BITalino Java SDK.

## Prerequisites ##
- JDK 7
- Android Studio (tested on 0.6.1)
- Gradle 1.12
- Android SDK (build-tools 19.1 and SDK 19)

## Test with device ##
* Change _src/main/java/com/bitalino/bitalinodroid/MainActivity.java_ and replace current _remoteDevice_ assignment
with your BITalino device MAC address.
* Connect your Android device through USB to your workstation.
* Compile and install *BITalinoDroid* app onto your Android device:

```
cd whatever_directory_you_cloned_this_repository
gradle clean build installDebug
```

* Now, *be sure* to pair your Android device with BITalino device, before running _BITalinoDroid_ on your phone.
* Done! :-)

## Tested on ##
Galaxy Nexus (Android 4.3)
LG Nexus 5 (Android 4.4.3)
Asus Nexus 7 (Android 4.4.2)
