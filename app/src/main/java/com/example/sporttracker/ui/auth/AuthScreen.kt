package com.example.sporttracker.ui.auth

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sporttracker.ui.theme.MainBlue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Ваш WEB_CLIENT_ID з Google Cloud Console (OAuth Client ID типу "Web application")
// Використовуємо Client ID з google-services.json (client_type: 3 = Web application)
const val WEB_CLIENT_ID = "599835124344-rmfqsu02co1teecugm9f0i7v71scejmc.apps.googleusercontent.com"

sealed class SignInResult {
    object Idle : SignInResult()
    object Loading : SignInResult()
    data class Success(val user: String) : SignInResult()
    data class Error(val message: String) : SignInResult()
    object Canceled : SignInResult()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val _signInResult = MutableStateFlow<SignInResult>(SignInResult.Idle)
    val signInResult: StateFlow<SignInResult> = _signInResult

    private val _isSignedIn = MutableStateFlow(auth.currentUser != null)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn

    init {
        checkSignInStatus()
    }

    fun checkSignInStatus() {
        _isSignedIn.value = auth.currentUser != null
    }

    fun signIn(googleIdToken: String) {
        android.util.Log.d("AuthViewModel", "signIn called with token")
        _signInResult.value = SignInResult.Loading
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Creating credential...")
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                android.util.Log.d("AuthViewModel", "Signing in to Firebase...")
                val result = auth.signInWithCredential(credential).await()
                val userEmail = result.user?.email ?: "Unknown User"
                android.util.Log.d("AuthViewModel", "Sign in successful: $userEmail")
                _signInResult.value = SignInResult.Success(userEmail)
                _isSignedIn.value = true
                checkSignInStatus() // Оновлюємо статус
                android.util.Log.d("AuthViewModel", "isSignedIn set to true")
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Sign in error", e)
                _signInResult.value = SignInResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "signOut called")
                // Виходимо з Firebase
                auth.signOut()
                android.util.Log.d("AuthViewModel", "Firebase signOut completed")
                
                // Виходимо з Google Sign-In з правильними параметрами
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(WEB_CLIENT_ID)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(auth.app.applicationContext, gso)
                googleSignInClient.signOut().await()
                android.util.Log.d("AuthViewModel", "Google Sign-In signOut completed")
                
                // Оновлюємо стан
                _isSignedIn.value = false
                _signInResult.value = SignInResult.Idle
                checkSignInStatus() // Перевіряємо статус після виходу
                android.util.Log.d("AuthViewModel", "isSignedIn set to false")
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error during signOut", e)
                // Навіть якщо є помилка, оновлюємо стан
                _isSignedIn.value = false
                _signInResult.value = SignInResult.Idle
                checkSignInStatus()
            }
        }
    }

    fun setSignInResult(result: SignInResult) {
        _signInResult.value = result
    }

}

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onSignInSuccess: () -> Unit
) {
    val context = LocalContext.current
    val signInResult by viewModel.signInResult.collectAsState()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(WEB_CLIENT_ID)
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        android.util.Log.d("AuthScreen", "Activity result received: resultCode=${result.resultCode}, data=${result.data}")
        
        // Спробуємо обробити результат навіть якщо resultCode != RESULT_OK
        // Бо іноді Google Sign-In може повертати помилки через resultCode, але дані все одно є
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        
        if (result.resultCode == Activity.RESULT_OK) {
            android.util.Log.d("AuthScreen", "Processing Google Sign-In result...")
            task.addOnCompleteListener { completedTask ->
                try {
                    if (completedTask.isSuccessful) {
                        val account = completedTask.getResult(ApiException::class.java)
                        android.util.Log.d("AuthScreen", "Google account retrieved: ${account.email}")
                        
                        account.idToken?.let { idToken ->
                            android.util.Log.d("AuthScreen", "ID Token received, signing in to Firebase...")
                            viewModel.signIn(idToken)
                        } ?: run {
                            android.util.Log.e("AuthScreen", "Google ID Token is null")
                            Toast.makeText(context, "Google ID Token is null. Перевірте налаштування в Firebase Console.", Toast.LENGTH_LONG).show()
                            viewModel.setSignInResult(SignInResult.Error("Google ID Token is null"))
                        }
                    } else {
                        val exception = completedTask.exception
                        android.util.Log.e("AuthScreen", "Task failed", exception)
                        if (exception is ApiException) {
                            val errorMessage = when (exception.statusCode) {
                                com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> "Помилка мережі. Перевірте інтернет-з'єднання."
                                com.google.android.gms.common.api.CommonStatusCodes.INTERNAL_ERROR -> "Внутрішня помилка. Спробуйте пізніше."
                                10 -> "Помилка розробника. Перевірте SHA-1 fingerprint та OAuth Client ID у Firebase Console."
                                12500 -> "Вхід скасовано користувачем."
                                else -> "Помилка входу: ${exception.statusCode}. ${exception.message}"
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            viewModel.setSignInResult(SignInResult.Error(errorMessage))
                        } else {
                            Toast.makeText(context, "Помилка входу: ${exception?.message}", Toast.LENGTH_LONG).show()
                            viewModel.setSignInResult(SignInResult.Error(exception?.message ?: "Unknown error"))
                        }
                    }
                } catch (e: ApiException) {
                    android.util.Log.e("AuthScreen", "Google Sign In failed: ${e.statusCode}, message: ${e.message}", e)
                    val errorMessage = when (e.statusCode) {
                        com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> "Помилка мережі. Перевірте інтернет-з'єднання."
                        com.google.android.gms.common.api.CommonStatusCodes.INTERNAL_ERROR -> "Внутрішня помилка. Спробуйте пізніше."
                        10 -> "Помилка розробника (код 10). Перевірте:\n1. SHA-1 fingerprint у Firebase Console\n2. OAuth Client ID у Google Cloud Console\n3. Чи WEB_CLIENT_ID відповідає OAuth Client ID"
                        12500 -> "Вхід скасовано користувачем."
                        else -> "Помилка входу: ${e.statusCode}. ${e.message}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    viewModel.setSignInResult(SignInResult.Error(errorMessage))
                }
            }
        } else {
            // Навіть якщо resultCode != RESULT_OK, спробуємо обробити задачу
            task.addOnCompleteListener { completedTask ->
                if (completedTask.isSuccessful) {
                    try {
                        val account = completedTask.getResult(ApiException::class.java)
                        android.util.Log.d("AuthScreen", "Google account retrieved despite non-OK result: ${account.email}")
                        account.idToken?.let { idToken ->
                            android.util.Log.d("AuthScreen", "ID Token received, signing in to Firebase...")
                            viewModel.signIn(idToken)
                        } ?: run {
                            android.util.Log.e("AuthScreen", "Google ID Token is null")
                            Toast.makeText(context, "Google ID Token is null. Перевірте налаштування в Firebase Console.", Toast.LENGTH_LONG).show()
                            viewModel.setSignInResult(SignInResult.Error("Google ID Token is null"))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AuthScreen", "Error processing account", e)
                        Toast.makeText(context, "Помилка обробки облікового запису: ${e.message}", Toast.LENGTH_LONG).show()
                        viewModel.setSignInResult(SignInResult.Error(e.message ?: "Unknown error"))
                    }
                } else {
                    val exception = completedTask.exception
                    android.util.Log.e("AuthScreen", "Google Sign In failed with resultCode=${result.resultCode}, exception=${exception?.message}", exception)
                    
                    if (exception is ApiException) {
                        val errorMessage = when (exception.statusCode) {
                            10 -> "Помилка розробника (код 10). Це означає проблему з налаштуваннями:\n1. Перевірте SHA-1 fingerprint у Firebase Console\n2. Перевірте OAuth Client ID у Google Cloud Console\n3. Переконайтеся, що WEB_CLIENT_ID відповідає OAuth Client ID з типом 'Web application'"
                            12500 -> "Вхід скасовано користувачем."
                            else -> "Помилка входу: ${exception.statusCode}. ${exception.message}"
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        viewModel.setSignInResult(SignInResult.Error(errorMessage))
                    } else {
                        Toast.makeText(context, "Не вдалося увійти. Перевірте налаштування в Firebase Console (SHA-1, OAuth Client ID).", Toast.LENGTH_LONG).show()
                        viewModel.setSignInResult(SignInResult.Error("Failed to sign in: ${exception?.message ?: "Unknown error"}"))
                    }
                }
            }
        }
    }

    LaunchedEffect(signInResult) {
        android.util.Log.d("AuthScreen", "LaunchedEffect triggered, signInResult: $signInResult")
        when (signInResult) {
            is SignInResult.Success -> {
                android.util.Log.d("AuthScreen", "Sign in successful, calling onSignInSuccess")
                Toast.makeText(context, "Увійшли як ${(signInResult as SignInResult.Success).user}", Toast.LENGTH_SHORT).show()
                // Оновлюємо статус входу перед викликом onSignInSuccess
                viewModel.checkSignInStatus()
                onSignInSuccess()
            }
            is SignInResult.Error -> {
                android.util.Log.e("AuthScreen", "Sign in error: ${(signInResult as SignInResult.Error).message}")
                Toast.makeText(context, "Помилка входу: ${(signInResult as SignInResult.Error).message}", Toast.LENGTH_LONG).show()
            }
            is SignInResult.Canceled -> {
                android.util.Log.d("AuthScreen", "Sign in canceled")
            }
            is SignInResult.Loading -> {
                android.util.Log.d("AuthScreen", "Sign in in progress...")
            }
            else -> {
                android.util.Log.d("AuthScreen", "Sign in idle")
            }
        }
    }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color.Black

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.4f))
            
            // Логотип додатку - використовуємо іконку додатку
            val packageManager = context.packageManager
            val appIconBitmap = remember {
                try {
                    val appInfo = packageManager.getApplicationInfo(context.packageName, 0)
                    val drawable = packageManager.getApplicationIcon(appInfo)
                    val bitmap = if (drawable is BitmapDrawable) {
                        drawable.bitmap
                    } else {
                        // Якщо це не BitmapDrawable, конвертуємо через Bitmap
                        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 512
                        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 512
                        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bmp)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bmp
                    }
                    bitmap.asImageBitmap()
                } catch (e: Exception) {
                    android.util.Log.e("AuthScreen", "Помилка завантаження іконки додатку", e)
                    null
                }
            }
            
            if (appIconBitmap != null) {
                Image(
                    bitmap = appIconBitmap,
                    contentDescription = "Логотип Sport Tracker",
                    modifier = Modifier.size(120.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Вітаємо у\nSport Tracker!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = textColor
            )
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedButton(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth(0.75f).height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MainBlue
                ),
                border = BorderStroke(1.dp, MainBlue)
            ) {
                Text("Увійти через Google")
            }
            
            Spacer(modifier = Modifier.weight(0.6f))
        }
    }
}