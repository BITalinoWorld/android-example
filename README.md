bitalino-android-example
========================

BITalino Android example application that uses my own BITalino Java SDK.

## Prerequisites ##
- JDK 6 or 7
- Android Studio (tested on 0.2.9)
- Gradle 1.7
- My own BITalino Java SDK compiled and installed (see https://github.com/pires/bitalino-java-sdk)

## Install my own BITalino SDK ##
```
git clone git@github.com:pires/bitalino-java-sdk.git
cd bitalino-java-sdk
git checkout -b 0.2 bitalino-java-sdk-0.2
mvn clean install
git checkout master
```

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
