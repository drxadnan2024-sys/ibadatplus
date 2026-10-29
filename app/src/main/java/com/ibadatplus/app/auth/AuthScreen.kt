package com.ibadatplus.app.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibadatplus.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (AuthUser) -> Unit,
    onSkipGuest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()

    // Automatically trigger onAuthSuccess when state transitions to Success
    LaunchedEffect(uiState.authState) {
        val current = uiState.authState
        if (current is AuthState.Success) {
            onAuthSuccess(current.user)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Islamic Gradient Header with App Logo
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0B132B),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0B132B), Color(0xFF1C2541), Color(0xFF0F172A))
                            )
                        )
                        .padding(top = 28.dp, bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Ibadat Plus",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(22.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Ibadat Plus",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "Sign in to isolate & sync your Tasbeeh & Settings",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Authentication Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Method Selector Tabs (Phone SMS OTP / Email)
                    if (uiState.authState !is AuthState.OtpSent) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (uiState.authMethod == AuthMethod.PHONE) Color(0xFF10B981) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.onAuthMethodChanged(AuthMethod.PHONE) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = if (uiState.authMethod == AuthMethod.PHONE) Color.White else Color(0xFF4B5563),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Phone SMS OTP",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.authMethod == AuthMethod.PHONE) Color.White else Color(0xFF4B5563)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (uiState.authMethod == AuthMethod.EMAIL) Color(0xFF10B981) else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.onAuthMethodChanged(AuthMethod.EMAIL) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = if (uiState.authMethod == AuthMethod.EMAIL) Color.White else Color(0xFF4B5563),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Email Sign In",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.authMethod == AuthMethod.EMAIL) Color.White else Color(0xFF4B5563)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Display Error Banner if any
                    if (uiState.errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.errorMessage!!,
                                    fontSize = 12.sp,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // --- STEP 1: Phone Number Input ---
                    if (uiState.authMethod == AuthMethod.PHONE && uiState.authState !is AuthState.OtpSent) {
                        Text(
                            text = "Enter your Mobile Number",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Country Code Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF3F4F6),
                                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                ) {
                                    Text(
                                        text = uiState.countryCode,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Phone Number Field
                            OutlinedTextField(
                                value = uiState.phoneNumber,
                                onValueChange = { viewModel.onPhoneNumberChanged(it) },
                                placeholder = { Text("10-digit mobile number", fontSize = 13.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        val isSending = uiState.authState is AuthState.SendingOtp

                        Button(
                            onClick = {
                                if (activity != null) {
                                    viewModel.sendPhoneOtp(activity)
                                }
                            },
                            enabled = !isSending && uiState.phoneNumber.length >= 10,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Dispatching SMS OTP...", fontSize = 14.sp)
                            } else {
                                Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Real SMS OTP", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- STEP 2: 6-Digit OTP Verification Screen ---
                    if (uiState.authState is AuthState.OtpSent) {
                        val otpSentState = uiState.authState as AuthState.OtpSent

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { viewModel.changeNumber() }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F5132))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verify Mobile OTP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F5132)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "A 6-digit verification code has been sent via SMS to ${otpSentState.destination}.",
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563),
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 6 Individual Digit Display Boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in 0 until 6) {
                                val digitChar = uiState.otpCode.getOrNull(i)?.toString() ?: ""
                                val isFocusedCell = uiState.otpCode.length == i

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (digitChar.isNotEmpty()) Color(0xFFF0FDF4) else Color(0xFFF9FAFB),
                                    border = BorderStroke(
                                        if (isFocusedCell) 2.dp else 1.dp,
                                        if (isFocusedCell) Color(0xFF10B981) else if (digitChar.isNotEmpty()) Color(0xFF86EFAC) else Color(0xFFD1D5DB)
                                    ),
                                    modifier = Modifier
                                        .size(width = 46.dp, height = 52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = digitChar,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F5132)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Real Hidden Input to drive the 6 boxes seamlessly
                        OutlinedTextField(
                            value = uiState.otpCode,
                            onValueChange = { viewModel.onOtpCodeChanged(it) },
                            placeholder = { Text("Type 6-digit OTP here", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 30-Sec Resend Timer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!uiState.canResend) {
                                Text(
                                    text = "Resend OTP in 00:${String.format("%02d", uiState.resendTimerSeconds)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6B7280)
                                )
                            } else {
                                TextButton(
                                    onClick = {
                                        if (activity != null) {
                                            viewModel.resendPhoneOtp(activity)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF10B981))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resend OTP SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                            }

                            TextButton(onClick = { viewModel.changeNumber() }) {
                                Text("Edit Number", fontSize = 12.sp, color = Color(0xFF2563EB))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val isVerifying = uiState.authState is AuthState.Verifying

                        Button(
                            onClick = { viewModel.verifyOtp() },
                            enabled = !isVerifying && uiState.otpCode.length == 6,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Verifying Code...", fontSize = 14.sp)
                            } else {
                                Text("Verify & Login", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- STEP 1B: Email Alternative Input ---
                    if (uiState.authMethod == AuthMethod.EMAIL && uiState.authState !is AuthState.OtpSent) {
                        Text(
                            text = "Email Address",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            placeholder = { Text("name@example.com", fontSize = 13.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Password",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            placeholder = { Text("Minimum 6 characters", fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        val isVerifying = uiState.authState is AuthState.Verifying

                        Button(
                            onClick = { viewModel.signInWithEmail() },
                            enabled = !isVerifying && uiState.email.isNotBlank() && uiState.password.length >= 6,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Signing In...", fontSize = 14.sp)
                            } else {
                                Text("Sign In with Email", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Skip as Guest Button
            TextButton(
                onClick = onSkipGuest
            ) {
                Text(
                    text = "Skip & Continue as Guest",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
