package com.coldturkey.focus.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coldturkey.focus.data.SessionManager
import kotlinx.coroutines.delay

class BlockOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_CUSTOM_MESSAGE = "extra_custom_message"
    }

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

        // نمایش روی صفحه قفل و بالا بردن اولویت پنجره
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val targetPkg = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val customMsg = intent.getStringExtra(EXTRA_CUSTOM_MESSAGE)

        setContent {
            BlockOverlayContent(
                packageName = targetPkg,
                customMessage = customMsg,
                sessionManager = sessionManager,
                onDismiss = {
                    // فرستادن کاربر به صفحه اصلی گوشی
                    val home = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(home)
                    finish()
                }
            )
        }
    }

    // جلوگیری از بستن صفحه با دکمه بازگشت (Back)
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(home)
        finish()
    }
}

@Composable
fun BlockOverlayContent(
    packageName: String,
    customMessage: String?,
    sessionManager: SessionManager,
    onDismiss: () -> Unit
) {
    var remainingSeconds by remember { mutableLongStateOf(sessionManager.getRemainingSeconds()) }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds = sessionManager.getRemainingSeconds()
            if (remainingSeconds <= 0) {
                onDismiss()
                break
            }
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0A09)), // استایل زغالی کولدتورکی
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⛔",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "این برنامه در قرنطینه است!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = customMessage ?: "تمرکزت رو حفظ کن. تا اتمام زمان قفل به این برنامه دسترسی نداری.",
                color = Color(0xFFA8A29E),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // تایمر بزرگ معکوس
            Surface(
                color = Color(0xFF1C1917),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "زمان باقیمانده تا آزادی:",
                        color = Color(0xFF78716C),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeFormatted,
                        color = Color(0xFFF59E0B),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF292524)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "برگرد سر کارت (صفحه اصلی)",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
