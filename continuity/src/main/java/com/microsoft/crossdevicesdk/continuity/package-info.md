# Module continuity

# Package com.microsoft.crossdevicesdk.continuity

# Welcome to the Cross Device Continuity SDK documentation

The classes in this API should be used to add support for cross device app context (continuity).

## Requirements

- Phone Link (PL)/Mobile devices, Link to Windows (LTW) must support continuity.
- App to provide context for continuity scenarios need to add meta-data in AndroidManifest
- AndroidX Annotation
- MinSdkVersion >= 24
- Kotlin >= 1.6.0

## Manifest declarations

### Declare app context type

In order to participate in the app context contract an app or context provider service must declare the meta-data for supported type of app context.
For example, add resume activity provider meta-data for cross device resume feature:

```xml
<meta-data
android:name="com.microsoft.crossdevice.resumeActivityProvider"
android:value="true" />
```

Below are all the types of meta-data we support for now.

```xml
<meta-data
android:name="com.microsoft.crossdevice.browserContextProvider"
android:value="true" />
```

```xml
<meta-data
android:name="com.microsoft.crossdevice.resumeActivityProvider"
android:value="true" />
```

If the app supports more than one type of app context, each type of meta-data needs to be added.

### Declare trigger type

Apps need to declare the trigger type in the manifest:

- If the feature depends on itself to notify LTW, which means it will be enabled on all the devices, the trigger type should be declared as below:
  
  ```xml
  <application ...
  <meta-data
  android:name="com.microsoft.crossdevice.trigger.PartnerApp"
  android:value="the sum value of all features' binary codes" />
  </application>
  ```

- If the feature depends on system API trigger, which means it will only be enabled on specific OEM devices, the trigger type should be declared as below:
  
  ```xml
  <application ...
  <meta-data
  android:name="com.microsoft.crossdevice.trigger.SystemApi"
  android:value="the sum value of all features' binary codes" />
  </application>
  ```

Now all the features' binary codes are listed below:

```xml
BROWSER_HISTORY: 2
RESUME_ACTIVITY: 4
```



## Using with an app context service provider (Option for OEMs)

Apps that want their context to be provided by a context service provider DO NOT REQUIRE any code libraries to be added.
The capability has to be registered through the application manifest however for Link to Windows to detect and request context.
Below are all the features support app context service provider:

Browser Continuity:

```xml
<application ...
<meta-data
android:name="com.microsoft.crossdevice.browserContextService"
android:value="<package of context service provider that LTW should query>" />
</application>
```

Note: If an app supports more than one type of context, each meta-data section is needed.

## SDK key concepts

The SDK provides a core class for sending app context.
In order to properly send app context to the host app(Link to Windows), a concrete class that implements the [com.microsoft.crossdevicesdk.continuity.IAppContextEventHandler] class has to be provided with an implementation for the following method:

- [com.microsoft.crossdevicesdk.continuity.IAppContextEventHandler.onContextRequestReceived]
- [com.microsoft.crossdevicesdk.continuity.IAppContextEventHandler.onInvalidContextRequestReceived]
- [com.microsoft.crossdevicesdk.continuity.IAppContextEventHandler.onSyncServiceDisconnected]

When initializing, the app should call [com.microsoft.crossdevicesdk.continuity.AppContextManager.initialize] to register this callback.
When a new broadcast arrives, if it's a message notifying LTW and PL is disconnected, [com.microsoft.crossdevicesdk.continuity.IAppContextEventHandler.onSyncServiceDisconnected] gets called.
Otherwise, the SDK will validate the request content through the intent's extras bundle. More specifically it will verify the following:

- Contains a content provider URI
- The authority for the content provider is valid (package name and signature digest check)
- Requested content types are valid

If the validations fail, [com.microsoft.crossdevicesdk.continuity.IAppContextEventHandler.onInvalidContextRequestReceived] gets called.
When all the validations pass, [com.microsoft.crossdevicesdk.continuity.IAppContextEventHandler.onContextRequestReceived] gets called.
Then an app context can be sent successfully by calling [com.microsoft.crossdevicesdk.continuity.AppContextManager.sendAppContext].
If the sent app context needs to be deleted, [com.microsoft.crossdevicesdk.continuity.AppContextManager.deleteAppContext] should be called.
To receive the result of sending/deleting app context, a concrete class that implements the [com.microsoft.crossdevicesdk.continuity.IAppContextResponse] class has to be provided with an implementation for the following method:

- [com.microsoft.crossdevicesdk.continuity.IAppContextResponse.onContextResponseSuccess]
- [com.microsoft.crossdevicesdk.continuity.IAppContextResponse.onContextResponseError]

When deInitializing, the app should call [com.microsoft.crossdevicesdk.continuity.AppContextManager.deInitialize] to unregister the callback. After this, any app context should not be sent to the host app.



package com.microsoft.crossdevicesdk.continuity
