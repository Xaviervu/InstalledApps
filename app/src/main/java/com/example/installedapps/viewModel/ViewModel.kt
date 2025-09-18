package com.example.installedapps.viewModel

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.installedapps.ui.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest


class ViewModel(private val app: Application) : AndroidViewModel(app) {

    private val progressMutableChannel = MutableSharedFlow<Boolean>()

    val progressChannel: SharedFlow<Boolean> = progressMutableChannel
    private val appListMutableChannel = MutableStateFlow<List<AppInfo>>(mutableListOf())

    val appListChannel: StateFlow<List<AppInfo>> = appListMutableChannel
    private var getAppsListJob: Job? = null

    private val appInfoMutableChannel = MutableStateFlow<AppInfo?>(null)
    val appInfoChannel: StateFlow<AppInfo?> = appInfoMutableChannel

    private var appMap = mutableMapOf<String, AppInfo>()

    fun getAppList() {
        if (getAppsListJob?.isActive == true) return
        getAppsListJob = viewModelScope.launch {
            if (appMap.isEmpty()) {
                getInstalledAppsWithIcons()
            }
            progressMutableChannel.emit(false)
            appListMutableChannel.value = appMap.values.toList()
        }
    }

    fun setLatestAppInfo(packageName: String) {
        appMap[packageName]?.let {
            viewModelScope.launch {
                appInfoMutableChannel.value = it
            }
        }
    }

    private suspend fun getInstalledAppsWithIcons() =
        withContext(Dispatchers.Default) {
            val packageManager: PackageManager = app.packageManager
            val installedApps =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            installedApps.forEach { applicationInfo ->
                if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                    val appName: String =
                        packageManager.getApplicationLabel(applicationInfo).toString()
                    val version: String =
                        packageManager.getPackageInfo(applicationInfo.packageName, 0).versionName
                            ?: ""
                    val appIcon: Drawable = packageManager.getApplicationIcon(applicationInfo)
                    appMap.put(
                        applicationInfo.packageName,
                        AppInfo(
                            icon = appIcon,
                            name = appName,
                            version = version,
                            packageName = applicationInfo.packageName,
                            checkSum = ""
                        )
                    )
                }
            }
        }

    fun getChecksum(packageName: String) {
        viewModelScope.launch {
            val appInfo = appMap[packageName]
            appInfo?.let { appInfo ->
                val checksum = appInfo.checkSum
                if (checksum.isEmpty()) {
                    val checksum = getAppSHA1Fingerprint(app.packageManager, packageName)
                    val appInfoNew = AppInfo(
                        appInfo.icon, appInfo.name,
                        appInfo.version, appInfo.packageName,
                        checksum
                    )
                    appMap[packageName] = appInfoNew
                    appInfoMutableChannel.value = appInfoNew
                    appListMutableChannel.value = appMap.values.toList()
                }
            }
        }
    }
}


fun getAppSHA1Fingerprint(packageManager: PackageManager, packageName: String): String {
    try {

        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        }

        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures
        }

        signatures?.get(0)?.let { signature ->
            val md = MessageDigest.getInstance("SHA1")
            md.update(signature.toByteArray())
            return md.digest().toHexString()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "-"
}

