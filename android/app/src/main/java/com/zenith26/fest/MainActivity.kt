package com.zenith26.fest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenith26.fest.data.remote.ApiClient
import com.zenith26.fest.ui.components.AppScreen
import com.zenith26.fest.ui.components.ServerConfigDialog
import com.zenith26.fest.ui.components.ToastBanner
import com.zenith26.fest.ui.components.ZenithTopBar
import com.zenith26.fest.ui.screens.admin.AdminDashboardScreen
import com.zenith26.fest.ui.screens.student.StudentPortalScreen
import com.zenith26.fest.ui.theme.Slate50
import com.zenith26.fest.ui.theme.ZenithTheme
import com.zenith26.fest.ui.viewmodel.AdminViewModel
import com.zenith26.fest.ui.viewmodel.StudentViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val studentViewModel: StudentViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithTheme {
                MainAppContent(
                    studentViewModel = studentViewModel,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    studentViewModel: StudentViewModel,
    adminViewModel: AdminViewModel
) {
    var currentScreen by remember { mutableStateOf(AppScreen.STUDENT_PORTAL) }
    var showServerConfigDialog by remember { mutableStateOf(false) }

    val studentToast by studentViewModel.toast.collectAsState()
    val adminToast by adminViewModel.toast.collectAsState()

    var activeToast by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(studentToast) {
        studentToast?.let {
            activeToast = it
            delay(3500)
            activeToast = null
            studentViewModel.clearToast()
        }
    }

    LaunchedEffect(adminToast) {
        adminToast?.let {
            activeToast = it
            delay(3500)
            activeToast = null
            adminViewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            ZenithTopBar(
                currentScreen = currentScreen,
                onScreenSelected = { screen ->
                    currentScreen = screen
                    if (screen == AppScreen.ADMIN_DASHBOARD) {
                        adminViewModel.loadAllData()
                    } else {
                        studentViewModel.loadEvents()
                    }
                },
                onOpenServerConfig = { showServerConfigDialog = true }
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate50)
        ) {
            when (currentScreen) {
                AppScreen.STUDENT_PORTAL -> {
                    StudentPortalScreen(viewModel = studentViewModel)
                }
                AppScreen.ADMIN_DASHBOARD -> {
                    AdminDashboardScreen(viewModel = adminViewModel)
                }
            }

            // Floating Toast Banner
            AnimatedVisibility(
                visible = activeToast != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                activeToast?.let { (icon, msg) ->
                    ToastBanner(icon = icon, message = msg)
                }
            }
        }
    }

    // Dynamic Server Config Dialog
    if (showServerConfigDialog) {
        ServerConfigDialog(
            currentUrl = ApiClient.getBaseUrl(),
            onDismiss = { showServerConfigDialog = false },
            onSaveUrl = { newUrl ->
                ApiClient.setBaseUrl(newUrl)
                showServerConfigDialog = false
                studentViewModel.loadEvents()
                adminViewModel.loadAllData()
            }
        )
    }
}
