/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License
 */
package com.microsoft.contoso.partnerapptriggertestapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.microsoft.crossdevicesdk.continuity.AppContext
import com.microsoft.crossdevicesdk.continuity.AppContextManager
import com.microsoft.crossdevicesdk.continuity.ContextRequestInfo
import com.microsoft.crossdevicesdk.continuity.IAppContextEventHandler
import com.microsoft.crossdevicesdk.continuity.IAppContextResponse
import com.microsoft.crossdevicesdk.continuity.LogUtils
import com.microsoft.crossdevicesdk.continuity.ProtocolConstants
import java.util.UUID

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
                sendResumeActivity()
            }
        }
        buttonDelete.setOnClickListener {
            if (ready) {
                deleteResumeActivity()
            }
        }
        buttonUpdate.setOnClickListener {
            if (ready) {
                updateResumeActivity()
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
        currentAppContext.observe(
            this,
            Observer { appContext ->
                appContext?.let {
                    textView.text =
                        "Current app context: ${it.contextId}\n App ID: ${it.appId}\n Created: ${it.createTime}\n Updated: ${it.lastUpdatedTime}\n Type: ${it.type}"
                    Log.d("MainActivity", "Current app context: ${it.contextId}")
                } ?: run {
                    textView.text = "No current app context available"
                    Log.d("MainActivity", "No current app context available")
                }
            }
        )
    }

    // Send resume activity to LTW
    private fun sendResumeActivity() {
        val appContext = AppContext().apply {
            this.contextId = generateContextId()
            this.appId = applicationContext.packageName
            this.createTime = System.currentTimeMillis()
            this.lastUpdatedTime = System.currentTimeMillis()
            this.type = ProtocolConstants.TYPE_RESUME_ACTIVITY
        }
        _currentAppContext.value = appContext
        AppContextManager.sendAppContext(this.applicationContext, appContext, appContextResponse)
    }

    // Delete resume activity from LTW
    private fun deleteResumeActivity() {
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

    private fun updateResumeActivity() {
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
        return "$packageName.${UUID.randomUUID()}"
    }
}
