package com.example.ui.auth

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.TertiaryContainer
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
  authRepository: AuthRepository,
  onAuthSuccess: () -> Unit
) {
  var isRegistering by remember { mutableStateOf(false) }
  var isForgotPassword by remember { mutableStateOf(false) }

  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }

  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  val scope = rememberCoroutineScope()

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // App Hero Logo
      Box(
        modifier = Modifier
          .size(72.dp)
          .background(
            Brush.linearGradient(listOf(Primary, Secondary, TertiaryContainer)),
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Chat,
          contentDescription = "Smart Chat",
          tint = Color.White,
          modifier = Modifier.size(38.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Smart Chat Messenger",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = if (isForgotPassword) {
          "Reset your password"
        } else if (isRegistering) {
          "Create your account to start messaging"
        } else {
          "Welcome back! Sign in to continue"
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
      )

      // Segmented Tabs: Sign In / Sign Up
      if (!isForgotPassword) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceContainer,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (!isRegistering) Primary else Color.Transparent,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  isRegistering = false
                  errorMessage = null
                }
            ) {
              Text(
                text = "Sign In",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (!isRegistering) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 10.dp)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isRegistering) Primary else Color.Transparent,
              modifier = Modifier
                .weight(1f)
                .clickable {
                  isRegistering = true
                  errorMessage = null
                }
            ) {
              Text(
                text = "Sign Up",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (isRegistering) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 10.dp)
              )
            }
          }
        }
      }

      // Error / Success message banners
      if (errorMessage != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = ErrorRed.copy(alpha = 0.12f),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = errorMessage ?: "",
              color = ErrorRed,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium
            )
            if (!isRegistering && !isForgotPassword) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "👉 Don't have an account yet? Tap here to Sign Up",
                color = Primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                  isRegistering = true
                  errorMessage = null
                }
              )
            }
            if (isRegistering && errorMessage?.contains("already exists", ignoreCase = true) == true) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "👉 Account already exists. Tap here to Sign In",
                color = Primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                  isRegistering = false
                  errorMessage = null
                }
              )
            }
          }
        }
      }

      if (successMessage != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Primary.copy(alpha = 0.12f),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Text(
            text = successMessage ?: "",
            color = Primary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp)
          )
        }
      }

      // Input Form Fields
      if (isRegistering && !isForgotPassword) {
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text("Full Name") },
          leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .testTag("auth_fullname_input"),
          singleLine = true
        )
      }

      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email Address") },
        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
          .testTag("auth_email_input"),
        singleLine = true
      )

      if (!isForgotPassword) {
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password") },
          leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
          visualTransformation = PasswordVisualTransformation(),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .testTag("auth_password_input"),
          singleLine = true
        )

        if (isRegistering) {
          OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp)
              .testTag("auth_confirm_password_input"),
            singleLine = true
          )
        }
      }

      if (!isRegistering && !isForgotPassword) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
          horizontalArrangement = Arrangement.End
        ) {
          Text(
            text = "Forgot password?",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
              isForgotPassword = true
              errorMessage = null
            }
          )
        }
      }

      // Action Button
      Button(
        onClick = {
          errorMessage = null
          successMessage = null
          if (isForgotPassword) {
            scope.launch {
              isLoading = true
              val res = authRepository.resetPassword(email)
              isLoading = false
              if (res.isSuccess) {
                successMessage = "Password reset instructions sent to $email."
              } else {
                val err = res.exceptionOrNull() ?: Exception("Failed to reset password.")
                errorMessage = authRepository.parseAuthError(err, isRegistering = false)
              }
            }
          } else if (isRegistering) {
            if (password != confirmPassword) {
              errorMessage = "Passwords do not match."
              return@Button
            }
            scope.launch {
              isLoading = true
              val res = authRepository.register(fullName, email, password)
              isLoading = false
              if (res.isSuccess) {
                onAuthSuccess()
              } else {
                val err = res.exceptionOrNull() ?: Exception("Registration failed.")
                errorMessage = authRepository.parseAuthError(err, isRegistering = true)
              }
            }
          } else {
            scope.launch {
              isLoading = true
              val res = authRepository.login(email, password)
              isLoading = false
              if (res.isSuccess) {
                onAuthSuccess()
              } else {
                val err = res.exceptionOrNull() ?: Exception("Login failed.")
                errorMessage = authRepository.parseAuthError(err, isRegistering = false)
              }
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("auth_primary_submit_btn"),
        enabled = !isLoading
      ) {
        if (isLoading) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
          Text(
            text = if (isForgotPassword) "Send Reset Link" else if (isRegistering) "Create Account" else "Sign In",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      }


      Spacer(modifier = Modifier.height(16.dp))

      // Switch mode options
      if (isForgotPassword) {
        TextButton(onClick = { isForgotPassword = false }) {
          Text("Back to Sign In", color = Primary, fontWeight = FontWeight.Bold)
        }
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 8.dp)
        ) {
          Text(
            text = if (isRegistering) "Already have an account?" else "Don't have an account?",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isRegistering) "Sign In" else "Sign Up",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Primary,
            modifier = Modifier.clickable {
              isRegistering = !isRegistering
              errorMessage = null
            }
          )
        }
      }
    }
  }
}
