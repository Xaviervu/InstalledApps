package com.example.installedapps.ui.compose

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.installedapps.MainActivity
import com.example.installedapps.R
import com.example.installedapps.ui.model.AppInfo
import com.example.installedapps.ui.navigation.FirstScreen
import com.example.installedapps.ui.navigation.Screen
import com.example.installedapps.ui.theme.InstalledAppsTheme
import com.example.installedapps.viewModel.ViewModel
import androidx.compose.ui.res.painterResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAppContent(viewModel: ViewModel, packageManager: PackageManager, activity: MainActivity) {
    InstalledAppsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            var isBackButtonVisible by rememberSaveable {
                mutableStateOf(false)
            }
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            val context: Context = LocalContext.current
                            Text(context.getText((R.string.app_name)).toString())
                        },
                        navigationIcon = {
                            if (isBackButtonVisible) {
                                IconButton(onClick = {
                                    navController.navigateUp()
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.arrow_back),
                                        contentDescription = "Navigate back"
                                    )
                                }
                            }
                        }
                    )
                }
            )
            { innerPadding ->

                val startScreen =
                    StartScreen(viewModel, innerPadding) { appInfo ->
                        viewModel.setLatestAppInfo(appInfo.packageName)
                        navController.navigate(route = Screen(appInfo.packageName))

                    }
                NavHost(
                    navController,
                    startDestination = FirstScreen
                ) {
                    composable<FirstScreen> {
                        isBackButtonVisible = false
                        startScreen
                    }
                    composable<Screen> { appInfo ->
                        isBackButtonVisible = true
                        AppDetails(
                            viewModel.appInfoChannel,
                            innerPadding,
                            getChecksum = { packageName ->
                                viewModel.getChecksum(packageName)
                            }
                        ) {
                            viewModel.appInfoChannel.value?.packageName?.let { packageInfo ->
                                packageManager.getLaunchIntentForPackage(packageInfo)
                                    ?.let { intent ->
                                        activity.startActivity(intent)
                                    }
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StartScreen(
    viewModel: ViewModel,
    innerPadding: PaddingValues,
    onClick: (AppInfo) -> Unit
) {
    val showProgress = viewModel.progressChannel.collectAsStateWithLifecycle(true)
    if (showProgress.value) {
        IndeterminateProgressScreen(innerPadding, stringResource(R.string.gathering_app_list))
    } else {
        InstalledAppsApp(innerPadding, viewModel.appListChannel) { appInfo ->
            onClick(appInfo)
        }
    }
}