# Continuity SDK

Enables Android apps to send app context to the Link to Windows (LTW) app, allowing users to resume activities on their Windows PCs.

---

## Overview

The Continuity SDK enables Android applications to provide seamless cross-device resume capabilities between Android devices and Windows PCs. By integrating this SDK, partner apps can programmatically share recent tasks (such as website URLs, document links, or music tracks) from Android devices equipped with the "Link to Windows" package, allowing users to continue these tasks directly on their Windows PC.
Learn more about seamless cross-device experiences: [Phone Link - Seamless task continuity](https://review.learn.microsoft.com/en-us/windows/cross-device/phonelink/?branch=pr-en-us-5521).
For complete API documentation and detailed guidance, see the [Continuity SDK Kotlin Docs](https://microsoft.github.io/Windows-Cross-Device/continuity/index.html).

---

The following sections will guide you through integrating our SDK.  
If you need to build a local AAR package, please refer to [Build local AAR](#build-local-aar).

## Supported Features

The Continuity SDK currently supports the following features for cross-device task continuity between Android and Windows devices:

- **Cross Device Resume (XDR) — Recommended:**  
  Share and resume recent activities (such as website URLs, document links, or music tracks) directly from your Android app to Windows PCs. These tasks can be resumed from the Windows Taskbar via deep integration with Link to Windows (LTW).

- **Browser Continuity:**  
  Send browser history from your Android app to be displayed in the Phone Link app on your PC, allowing users to conveniently open their recent web pages in their preferred browser.

> **Note:** iOS applications are currently not supported.

## Prerequisites

Before integrating the SDK, contact `wincrossdeviceapi@microsoft.com` with the information listed below:

- Description of your user experience
- Screenshot of your application where a user natively accesses web or documents
- PackageId of your application
- Google Play store link for your application

Once we are officially partnered up and aligned with the content being sent across devices, the following steps need to be completed:

- Share your app's debug/release signatures to be added to the approved list

You will receive:

- A private release build of the LTW app for testing and validation

### Android Platform Requirements

- **Minimum SDK Version:** 24
- **Minimum Kotlin Version:** 1.6.0

---

## Configure Your Android Development Environment

1. Download the `.aar` file from [Windows Cross-Device SDK releases](https://github.com/microsoft/Windows-Cross-Device/releases) add them to the project.
2. Add the SDK to your project dependencies.

---

## Integration Steps

### Manifest Setup

Declare meta-data entries in your `AndroidManifest.xml`:
To participate in the app context contract, meta-data must be declared for the supported type of app context.

#### Declare app context type

- Cross Device Resume:

  ```xml
  <meta-data
  android:name="com.microsoft.crossdevice.resumeActivityProvider
  android:value="true" />
  ```

- Browser Continuity:

  ```xml
  <meta-data
  android:name="com.microsoft.crossdevice.browserContextProvider"
  android:value="true" />
  ```

If the app supports more than one type of app context, each type of meta-data needs to be added.

#### Declare trigger type

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

```
BROWSER_HISTORY:     2  
RESUME_ACTIVITY:     4  
```

#### Example (Cross Device Resume):

```xml
<meta-data android:name="com.microsoft.crossdevice.applicationContextProvider" android:value="true" />
<meta-data android:name="com.microsoft.crossdevice.trigger.PartnerApp" android:value="4" />
```

---

### Code Examples

Once the app manifest declarations have been added, "Link to Windows" partner apps will need to:

1. Determine the appropriate timing to call the **Initialize and DeInitialize functions** for the Continuity SDK. After calling the Initialize function, a callback that implements `IAppContextEventHandler` should be triggered.

2. After initializing the Continuity SDK, if `onContextRequestReceived()` is called, it indicates the connection is established. The app can then **send `AppContext`** (including create and update) to LTW or **delete `AppContext`** from LTW.

### Basic App Context Example (Cross Device Resume)

```kotlin
val appContext = AppContext().apply {
    contextId = generateContextId()
    appId = packageName
    createTime = System.currentTimeMillis()
    lastUpdatedTime = System.currentTimeMillis()
    type = ProtocolConstants.TYPE_RESUME_ACTIVITY
    // set optional fields here
}
AppContextManager.sendAppContext(context, appContext, appContextResponse)
```

### Full SDK Usage Example

#### Cross Device Resume (Also see the full example in [Sample App](./partnerapptriggertestapp/))

```kotlin
class MainActivity : AppCompatActivity() {

    private val appContextResponse = object : IAppContextResponse {
        override fun onContextResponseSuccess(response: AppContext) {
            Log.d("MainActivity", "onContextResponseSuccess")
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Context response success: ${response.contextId}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onContextResponseError(response: AppContext, throwable: Throwable) {
            Log.d("MainActivity", "onContextResponseError: ${throwable.message}")
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Context response error: ${throwable.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private lateinit var appContextEventHandler: IAppContextEventHandler

    private val _currentAppContext = MutableLiveData<AppContext?>()
    private val currentAppContext: LiveData<AppContext?> get() = _currentAppContext


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        LogUtils.setDebugMode(true)
        var ready = false
        val buttonSend: Button = findViewById(R.id.buttonSend)
        val buttonDelete: Button = findViewById(R.id.buttonDelete)
        val buttonUpdate: Button = findViewById(R.id.buttonUpdate)
        setButtonDisabled(buttonSend)
        setButtonDisabled(buttonDelete)
        setButtonDisabled(buttonUpdate)
        buttonSend.setOnClickListener {
            if (ready) {
                sendAppContext()
            }
        }
        buttonDelete.setOnClickListener {
            if (ready) {
                deleteAppContext()
            }
        }
        buttonUpdate.setOnClickListener {
            if (ready) {
                updateAppContext()
            }
        }
        appContextEventHandler = object : IAppContextEventHandler {
            override fun onContextRequestReceived(contextRequestInfo: ContextRequestInfo) {
                LogUtils.d("MainActivity", "onContextRequestReceived")
                ready = true
                setButtonEnabled(buttonSend)
                setButtonEnabled(buttonDelete)
                setButtonEnabled(buttonUpdate)
            }

            override fun onInvalidContextRequestReceived(throwable: Throwable) {
                Log.d("MainActivity", "onInvalidContextRequestReceived")
            }

            override fun onSyncServiceDisconnected() {
                Log.d("MainActivity", "onSyncServiceDisconnected")
                ready = false
                setButtonDisabled(buttonSend)
                setButtonDisabled(buttonDelete)
            }
        }
        // Initialize the AppContextManager
        AppContextManager.initialize(this.applicationContext, appContextEventHandler)


        // Update currentAppContext text view.
        val textView = findViewById<TextView>(R.id.appContext)
        currentAppContext.observe(this, Observer { appContext ->
            appContext?.let {
                textView.text =
                    "Current app context: ${it.contextId}\n App ID: ${it.appId}\n Created: ${it.createTime}\n Updated: ${it.lastUpdatedTime}\n Type: ${it.type}"
                Log.d("MainActivity", "Current app context: ${it.contextId}")
            } ?: run {
                textView.text = "No current app context available"
                Log.d("MainActivity", "No current app context available")
            }
        })

    }

    // Send app context to LTW
    private fun sendAppContext() {
        val appContext = AppContext().apply {
            this.contextId = generateContextId()
            this.appId = applicationContext.packageName
            this.createTime = System.currentTimeMillis()
            this.lastUpdatedTime = System.currentTimeMillis()
            // Set the type of app context, for example, resume activity.
            this.type = ProtocolConstants.TYPE_RESUME_ACTIVITY
            // Set the rest fields in appContext
            //……
        }
        _currentAppContext.value = appContext
        AppContextManager.sendAppContext(this.applicationContext, appContext, appContextResponse)
    }

    // Delete app context from LTW
    private fun deleteAppContext() {
        currentAppContext.value?.let {
            AppContextManager.deleteAppContext(
                this.applicationContext,
                it.contextId,
                appContextResponse
            )
            _currentAppContext.value = null
        } ?: run {
            Toast.makeText(this, "No resume activity to delete", Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", "No resume activity to delete")
        }
    }

    // Update app context from LTW
    private fun updateAppContext() {
        currentAppContext.value?.let {
            it.lastUpdatedTime = System.currentTimeMillis()
            AppContextManager.sendAppContext(this.applicationContext, it, appContextResponse)
            _currentAppContext.postValue(it)
        } ?: run {
            Toast.makeText(this, "No resume activity to update", Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", "No resume activity to update")
        }
    }

    private fun setButtonDisabled(button: Button) {
        button.isEnabled = false
        button.alpha = 0.5f
    }

    private fun setButtonEnabled(button: Button) {
        button.isEnabled = true
        button.alpha = 1.0f
    }

    override fun onDestroy() {
        super.onDestroy()
        // Deinitialize the AppContextManager
        AppContextManager.deInitialize(this.applicationContext)
    }

    private fun generateContextId(): String {
        return "${packageName}.${UUID.randomUUID()}"
    }
}
```

For all the **required** and **optional** fields, see [AppContext](#appcontext)

#### Browser Continuity Example

```kotlin
class MainActivity : AppCompatActivity() {

    private val appContextResponse = object : IAppContextResponse {
        override fun onContextResponseSuccess(response: AppContext) {
            Log.d("MainActivity", "onContextResponseSuccess")
        }

        override fun onContextResponseError(response: AppContext, throwable: Throwable) {
            Log.d("MainActivity", "onContextResponseError: ${throwable.message}")
        }
    }

    private lateinit var appContextEventHandler: IAppContextEventHandler

    private val browserHistoryContext: BrowserHistoryContext = BrowserHistoryContext()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //……
        LogUtils.setDebugMode(true)
        var ready = false
        val buttonSend: Button = findViewById(R.id.buttonSend)
        val buttonDelete: Button = findViewById(R.id.buttonDelete)
        setButtonDisabled(buttonSend)
        setButtonDisabled(buttonDelete)
        buttonSend.setOnClickListener {
            if (ready) {
                sendBrowserHistory ()
            }
        }
        buttonDelete.setOnClickListener {
            if (ready) {
                clearBrowserHistory ()
            }
        }
        appContextEventHandler = object : IAppContextEventHandler {
            override fun onContextRequestReceived(contextRequestInfo: ContextRequestInfo) {
                LogUtils.d("MainActivity", "onContextRequestReceived")
                ready = true
                setButtonEnabled(buttonSend)
                setButtonEnabled(buttonDelete)
            }

            override fun onInvalidContextRequestReceived(throwable: Throwable) {
                Log.d("MainActivity", "onInvalidContextRequestReceived")
            }

            override fun onSyncServiceDisconnected() {
                Log.d("MainActivity", "onSyncServiceDisconnected")
                ready = false
                setButtonDisabled(buttonSend)
                setButtonDisabled(buttonDelete)
            }
        }
        // Initialize the AppContextManager
        AppContextManager.initialize(this.applicationContext, appContextEventHandler)
    }

    // Send browser history to LTW
    private fun sendBrowserHistory () {
        browserHistoryContext.setAppId(this.packageName)
        browserHistoryContext.addBrowserContext(System.currentTimeMillis(),
             Uri.parse("https://www.bing.com/"), "Bing Search", null
        )
        AppContextManager.sendAppContext(this.applicationContext, browserHistoryContext, appContextResponse)

    }

    // Clear browser history from LTW
         private fun clearBrowserHistory() {
        browserHistoryContext.setAppId(this.packageName)
        browserHistoryContext.setBrowserContextEmptyFlag(true)
        AppContextManager.sendAppContext(this.applicationContext, browserHistoryContext, appContextResponse)
    }

    private fun setButtonDisabled(button: Button) {
        button.isEnabled = false
        button.alpha = 0.5f
    }

    private fun setButtonEnabled(button: Button) {
        button.isEnabled = true
        button.alpha = 1.0f
    }

    override fun onDestroy() {
        super.onDestroy()
        // Deinitialize the AppContextManager
        AppContextManager.deInitialize(this.applicationContext)
    }

    //……
}
```

For all the **required** and **optional** fields, see [BrowserContext](#browsercontext)

---

## Integration Validation

### Preparation

1. Ensure private LTW is installed.

2. Ensure LTW is connected to PC and Phone Link:
   a.    Connect LTW to PC:  refer to [How to manage your mobile device on your PC](https://support.microsoft.com/en-us/topic/phone-link-requirements-and-setup-cd2a1ee7-75a7-66a6-9d4e-bf22e735f9e3#bkmk_cdeh_learn_more) for instructions.
   b.    Connect LTW to Phone Link: refer to ‘How to link your devices starting from Phone Link’ section in [Phone Link requirements and setup - Microsoft Support](https://support.microsoft.com/en-us/topic/phone-link-requirements-and-setup-cd2a1ee7-75a7-66a6-9d4e-bf22e735f9e3).
   Note: (If after scanning the QR code you cannot jump into LTW, please open LTW first and scan the QR code within the app.)

3. Verify that the partner app has integrated the Continuity SDK.

### Validation Steps

1. Launch the app and initialize the SDK. Confirm that `onContextRequestReceived()` is called.
2. Once `onContextRequestReceived()` is called, the app can send the app context to LTW. If `onContextResponseSuccess()` is called after sending app context, the SDK integration is successful.

---

## Context Field Reference

Notes

- Avoid sending sensitive data (e.g., tokens) in the context
- The required/optional keys shown in the table below are for general reference only. The actual required fields may vary depending on the feature (e.g., Cross Device Resume).  
  **Please contact us first. The final required keys will be determined based on our communication.**

### AppContext

| Key                               | Value                                                                                                                         | Extra Information                                                                                                                |
| --------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| `contextId`<br/> [required]       | Used to distinguish it from other app contexts.                                                                               | Unique for each app context. Format: `${packageName}.${UUID.randomUUID()}`                                                       |
| `type`<br/>[required]             | A binary flag that indicates what app context type is sent to LTW.                                                            | The value should be consistent with requestedContextType above                                                                   |
| `createTime`<br/> [required]      | Timestamp representing the create time of the app context.                                                                    |                                                                                                                                  |
| `lastUpdatedTime`<br/> [required] | Timestamp representing the last updated time of the app context.                                                              | Any time when any fields of app context is updated, the updated time needs to be recorded.                                       |
| `teamId`<br/> [optional]          | Used to identify the organization or group the app belongs to.                                                                |                                                                                                                                  |
| `intentUri`<br/> [optional]       | Used to indicate which app can continue the app context handed over from the originating device.                              | The maximum length is 2083 characters.                                                                                           |
| `appId`<br/>[optional]            | The package of the application the context is for.                                                                            |                                                                                                                                  |
| `title`<br/>[optional]            | The title of this app context, such as a document name or web page title.                                                     |                                                                                                                                  |
| `weblink`<br/>[optional]          | The URL of the webpage to load in a browser to continue the app context.                                                      | The maximum length is 2083 characters.                                                                                           |
| `preview`<br/>[optional]          | Bytes of the preview image that can represent the app context                                                                 |                                                                                                                                  |
| `extras`<br/>[optional]           | A key-value pair object containing app-specific state information needed to continue an app context on the continuing device. | Need to provide when the app context has its unique data.                                                                        |
| `LifeTime`<br/> [optional]        | The lifetime of the app context in milliseconds.                                                                              | Only used for ongoing scenario, if not set, the default value is 30 days.<br/>For XDR: the maximum supported value is 5 minutes. |

### BrowserContext

| Key                             | Value                                                               |
| ------------------------------- | ------------------------------------------------------------------- |
| `browserWebUri`<br/> [required] | A web URI that will open in browser on PC (http: or https:).        |
| `title`<br/>[required]          | The title of the web page.                                          |
| `timestamp`<br/>[required]      | The timestamp that the web page was first opened or last refreshed. |
| `favIcon`<br/>[optional]        | The favicon of the web page in bytes, should be small in general.   |

---

## Build local AAR

Clone the repo and Configure `local.properties` file in the project root with following properties.

```java
ado_reader=local
ado_reader_ms_pass=null
ado_reader_mmxsdk_pass=null
```

Then you can run `assemble` to generate the local aar or run `partnerapptriggertestapp` to install the test app.
