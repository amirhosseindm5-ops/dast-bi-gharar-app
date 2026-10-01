package com.coldturkey.focus

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.coldturkey.focus.data.AppModel
import com.coldturkey.focus.data.InstalledAppsManager
import com.coldturkey.focus.data.SessionManager
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var installedAppsManager: InstalledAppsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)
        installedAppsManager = InstalledAppsManager(applicationContext)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0C0A09)
                ) {
                    ColdTurkeyMainScreen(
                        sessionManager = sessionManager,
                        installedAppsManager = installedAppsManager
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColdTurkeyMainScreen(
    sessionManager: SessionManager,
    installedAppsManager: InstalledAppsManager
) {
    val context = LocalContext.current
    var isSessionActive by remember { mutableStateOf(sessionManager.isSessionActive()) }
    var remainingSeconds by remember { mutableLongStateOf(sessionManager.getRemainingSeconds()) }
    var isStrictMode by remember { mutableStateOf(false) }
    var selectedMinutes by remember { mutableIntStateOf(30) }

    var allInstalledApps by remember { mutableStateOf<List<AppModel>>(emptyList()) }
    var selectedPackages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showAppPickerSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        allInstalledApps = installedAppsManager.getInstalledApps()
    }

    LaunchedEffect(isSessionActive) {
        while (isSessionActive) {
            delay(1000)
            remainingSeconds = sessionManager.getRemainingSeconds()
            if (remainingSeconds <= 0) {
                isSessionActive = false
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0C0A09),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "کولدتورکی | قفل تمرکز",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "مجوز دسترسی‌پذیری",
                            tint = Color(0xFFF59E0B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1C1917))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSessionActive) {
                ActiveSessionView(
                    remainingSeconds = remainingSeconds,
                    isStrictMode = sessionManager.isStrictMode(),
                    blockedCount = sessionManager.getBlockedApps().size,
                    onEmergencyCancel = {
                        if (!sessionManager.isStrictMode()) {
                            sessionManager.stopSession()
                            isSessionActive = false
                        }
                    }
                )
            } else {
                SetupSessionView(
                    allApps = allInstalledApps,
                    selectedPackages = selectedPackages,
                    selectedMinutes = selectedMinutes,
                    isStrictMode = isStrictMode,
                    onOpenPicker = { showAppPickerSheet = true },
                    onRemovePackage = { pkg -> selectedPackages = selectedPackages - pkg },
                    onSelectMinutes = { selectedMinutes = it },
                    onToggleStrict = { isStrictMode = it },
                    onStartLock = {
                        if (selectedPackages.isNotEmpty()) {
                            sessionManager.startSession(selectedMinutes, selectedPackages, isStrictMode)
                            isSessionActive = true
                            remainingSeconds = selectedMinutes * 60L
                        }
                    }
                )
            }
        }
    }

    if (showAppPickerSheet) {
        AppPickerBottomSheet(
            apps = allInstalledApps,
            selected = selectedPackages,
            onToggle = { pkg ->
                selectedPackages = if (selectedPackages.contains(pkg)) {
                    selectedPackages - pkg
                } else {
                    selectedPackages + pkg
                }
            },
            onDismiss = { showAppPickerSheet = false }
        )
    }
}

@Composable
fun SetupSessionView(
    allApps: List<AppModel>,
    selectedPackages: Set<String>,
    selectedMinutes: Int,
    isStrictMode: Boolean,
    onOpenPicker: () -> Unit,
    onRemovePackage: (String) -> Unit,
    onSelectMinutes: (Int) -> Unit,
    onToggleStrict: (Boolean) -> Unit,
    onStartLock: () -> Unit
) {
    val selectedAppModels = allApps.filter { selectedPackages.contains(it.packageName) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "برنامه‌های هدف برای بلاک شدن (${selectedPackages.size})",
            color = Color(0xFFA8A29E),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = Color(0xFF1C1917),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (selectedAppModels.isEmpty()) {
                    Text(
                        text = "هیچ برنامه‌ای انتخاب نشده است. برای انتخاب با آیکون روی دکمه زیر بزنید.",
                        color = Color(0xFF78716C),
                        fontSize = 13.sp
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        selectedAppModels.take(5).forEach { app ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(56.dp)
                            ) {
                                Image(
                                    bitmap = app.icon.toBitmap().asImageBitmap(),
                                    contentDescription = app.appName,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.appName,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenPicker,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF292524)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("انتخاب از میان برنامه‌های نصب‌شده", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "مدت زمان قفل (دقیقه)",
            color = Color(0xFFA8A29E),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        val presets = listOf(15, 25, 45, 60, 120)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { min ->
                val isSelected = selectedMinutes == min
                Surface(
                    color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF1C1917),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectMinutes(min) }
                ) {
                    Text(
                        text = "$min",
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            color = Color(0xFF1C1917),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "حالت قفل سفت‌وسخت (Cold Turkey)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "تا پایان تایمر هیچ راهی برای لغو وجود ندارد و بخش تنظیمات نیز قفل می‌شود.",
                        color = Color(0xFF78716C),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = isStrictMode,
                    onCheckedChange = onToggleStrict
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartLock,
            enabled = selectedPackages.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF59E0B),
                disabledContainerColor = Color(0xFF292524)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = if (selectedPackages.isEmpty()) "ابتدا برنامه‌ها را انتخاب کنید" else "فعال‌سازی قفل تمرکز",
                color = if (selectedPackages.isEmpty()) Color(0xFF78716C) else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
fun ActiveSessionView(
    remainingSeconds: Long,
    isStrictMode: Boolean,
    blockedCount: Int,
    onEmergencyCancel: () -> Unit
) {
    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔒 قفل تمرکز فعال است",
            color = Color(0xFFF59E0B),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = timeFormatted,
            color = Color.White,
            fontSize = 52.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$blockedCount برنامه در حالت قرنطینه کامل",
            color = Color(0xFFA8A29E),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (!isStrictMode) {
            OutlinedButton(
                onClick = onEmergencyCancel,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Text("لغو زودهنگام جلسه")
            }
        } else {
            Text(
                text = "حالت سفت‌وسخت فعال است: امکان لغو وجود ندارد.",
                color = Color(0xFF78716C),
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerBottomSheet(
    apps: List<AppModel>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = apps.filter {
        it.appName.contains(searchQuery, ignoreCase = true) ||
        it.packageName.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1917)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            Text(
                text = "انتخاب از برنامه‌های نصب‌شده",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجوی نام برنامه...", color = Color(0xFF78716C)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFF59E0B),
                    unfocusedBorderColor = Color(0xFF292524)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps) { app ->
                    val isChecked = selected.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isChecked) Color(0xFF292524) else Color.Transparent)
                            .clickable { onToggle(app.packageName) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon.toBitmap().asImageBitmap(),
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = app.packageName,
                                color = Color(0xFF78716C),
                                fontSize = 11.sp
                            )
                        }
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggle(app.packageName) },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF59E0B))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("تأیید (${selected.size} برنامه)", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
