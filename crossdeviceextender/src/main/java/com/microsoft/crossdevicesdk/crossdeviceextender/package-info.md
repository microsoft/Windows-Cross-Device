# Module crossdeviceextender
# Package com.microsoft.crossdevicesdk.crossdeviceextender

# Welcome to the Cross Device Extender SDK documentation

The classes in this API should be used to add support for cross device app notifications (Extender).

## Requirements
- MinSdkVersion >= 24
- Kotlin >= 1.6.0


## Adding Publisher Metadata to App Notifications
In order to provide app publisher URL metadata in Notifications, *CrossDeviceExtender* can be used to add the following information using the *CrossDeviceNotification* object. Following fields can be set in Notification extras. The publisher URL will be used to redirect cross platform installations to requested sites.
- publisherUrl
- version

```
val notificationMetadata = CrossDeviceNotification("<publisher-url>","<version>")
val notificationExtender = CrossDeviceExtender().setCrossDeviceNotification(notificationMetadata);
Notification.Builder(context,"<notification-channel>")
            .extend(notificationExtender)
```



Note: Cross Platform Installations are handled by Link to Windows which will leverage this CrossDeviceExtender to verify publisher website to redirect installations on cross platform devices
package com.microsoft.crossdevicesdk.crossdeviceextender