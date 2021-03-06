#!/usr/bin/env bash

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";

export ROLE=$1;

if [[ $ROLE = 'all' ]]; then

  $SCRIPT_DIR/_for-all-roles.sh build

else

  export KEY_PSWD=`cat $SCRIPT_DIR/_private/rfcx-guardian-keystore-pswd.txt;`;

  cd $SCRIPT_DIR/../;

  if [ ! -d tmp ]; then
    echo "creating tmp directory";
    mkdir tmp;
  fi

  cd rfcx-guardian-role-$ROLE;

  export APK_VERSION=`cat $SCRIPT_DIR/../rfcx-guardian-role-$ROLE/AndroidManifest.xml | grep 'android:versionName=' | cut -d'"' -f 2`;
  echo ""; echo "RFCx $ROLE ($APK_VERSION)";
  
  export SDK_VERSION=`cat $SCRIPT_DIR/../rfcx-guardian-role-$ROLE/AndroidManifest.xml | grep 'android:targetSdkVersion=' | cut -d'"' -f 2`;

  echo "generating build configurations... (android-$SDK_VERSION)";
  export BUILD_CONFIG_LIB_UTIL=`$ANDROID_HOME/tools/android update project --path ../rfcx-guardian-lib-utility --name rfcx-guardian-lib-utility --target android-$SDK_VERSION --subprojects`;
  export BUILD_CONFIG_LIB_AUDIO=`$ANDROID_HOME/tools/android update project --path ../rfcx-guardian-lib-audio --name rfcx-guardian-lib-audio --target android-$SDK_VERSION --subprojects`;
  export BUILD_CONFIG_LIB_I2C=`$ANDROID_HOME/tools/android update project --path ../rfcx-guardian-lib-i2c --name rfcx-guardian-lib-i2c --target android-$SDK_VERSION --subprojects`;

  export BUILD_CONFIG=`$ANDROID_HOME/tools/android update project --path . --name rfcx-guardian-role-$ROLE --target android-$SDK_VERSION --subprojects`;

  echo "key.store=$SCRIPT_DIR/_private/rfcx-guardian-keystore.jks" >> local.properties;
  echo "key.alias=rfcx-guardian-android" >> local.properties;
  echo "key.store.password=$KEY_PSWD" >> local.properties;
  echo "key.alias.password=$KEY_PSWD" >> local.properties;

  # include rfcx shared 'utility' library
  echo "android.library.reference.1=../rfcx-guardian-lib-utility" >> local.properties;

  # for the 'guardian' role, include rfcx shared audio encoding library
  if [[ $ROLE = 'guardian' ]]; then
    
    echo "android.library.reference.2=../rfcx-guardian-lib-audio" >> local.properties;
    cd $SCRIPT_DIR/../rfcx-guardian-lib-audio/jni && $ANDROID_HOME/ndk-bundle/ndk-build;

  fi

  # for the 'admin' role, include rfcx shared i2c access library
  if [[ $ROLE = 'admin' ]]; then
    
 #   echo "android.library.reference.2=../rfcx-guardian-lib-i2c" >> local.properties;
    cd $SCRIPT_DIR/../rfcx-guardian-lib-i2c/jni && $ANDROID_HOME/ndk-bundle/ndk-build;

    cp $SCRIPT_DIR/../rfcx-guardian-lib-i2c/libs/armeabi/i2cget $SCRIPT_DIR/../rfcx-guardian-role-$ROLE/assets/i2cget;
    cp $SCRIPT_DIR/../rfcx-guardian-lib-i2c/libs/armeabi/i2cset $SCRIPT_DIR/../rfcx-guardian-role-$ROLE/assets/i2cset;
     
  fi

  cd $SCRIPT_DIR/../rfcx-guardian-role-$ROLE;

  echo "setting up build process...";
  export ANT_CLEAN=`ant clean`;

  echo "building apk...";
  export APK_PATH=`ant release | grep 'Release Package:' | cut -d':' -f 2 | cut -d' ' -f 2`;

  echo "copying apk into tmp directory..."
  cp $APK_PATH ../tmp/$ROLE-$APK_VERSION.apk;

  export POST_CLEANUP_ROLES=`cd $SCRIPT_DIR/../; rm rfcx-guardian-role-*/local.properties rfcx-guardian-role-*/build.xml rfcx-guardian-role-*/proguard-project.txt;`;
  export POST_CLEANUP_LIBS=`cd $SCRIPT_DIR/../; rm rfcx-guardian-lib-*/local.properties rfcx-guardian-lib-*/build.xml rfcx-guardian-lib-*/proguard-project.txt;`;

  cd $SCRIPT_DIR/../;

fi
