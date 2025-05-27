package com.example.sawaapplication.screens.authentication.presentation.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.authentication.presentation.vmModels.ForgotPasswordViewModel
import com.example.sawaapplication.ui.screenComponent.CustomCard
import com.example.sawaapplication.ui.screenComponent.CustomTextField
import com.example.sawaapplication.ui.screenComponent.buttons.GradientButton
import kotlinx.coroutines.delay

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ResetPasswordScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
    val emailState by forgotPasswordViewModel.email.collectAsState()

    // Entry animations
    var bannerVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bannerVisible = true
        delay(300)
        cardVisible = true
    }

    val bannerHeight = 250.dp
    val logoSize = 90.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(top = integerResource(id = R.integer.mediumSpace).dp)

    ) {
        Spacer(modifier = Modifier.height(integerResource(id = R.integer.mediumSpace).dp))

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(bannerHeight)
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )

            // Logo centered on the banner
            Image(
                painter = painterResource(id = R.drawable.applogo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(logoSize)
                    .align(Alignment.Center)
                    .padding(bottom = integerResource(id = R.integer.mediumSpace).dp),

            )
        }

        AnimatedVisibility(
            visible = cardVisible,
            modifier = Modifier
                .align(Alignment.Center)
                .width(integerResource(id = R.integer.cardWidth).dp)
                .height(integerResource(id = R.integer.cardHeightResetPass).dp),
            enter = fadeIn(tween(300)) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(300)
            ),
            exit = fadeOut()
        ) {
            Spacer(modifier = Modifier.height(integerResource(id = R.integer.mediumSpace).dp))

            CustomCard(modifier = Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(integerResource(id = R.integer.smallSpace).dp),
                    horizontalAlignment = CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.resetPassword),
                        fontSize = integerResource(id = R.integer.cardHeaderSize).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(
                            top = integerResource(id = R.integer.cardHeaderTopPadding).dp,
                            bottom = integerResource(id = R.integer.cardHeaderBottomPadding).dp
                        )
                    )
                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.smallSpace).dp))

                    CustomTextField(
                        value = emailState,
                        onValueChange = { forgotPasswordViewModel.email.value = it },
                        label = stringResource(id = R.string.email),
                    )

                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.largerSpace).dp))

                    GradientButton(
                        onClick = {
                            val email = emailState.trim()
                            if (email.isNotEmpty()) {
                                forgotPasswordViewModel.forgotPassword()  // Handles Firebase email sending
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.SentPassword),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(Screen.Login.route)
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.failedSend),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        text = stringResource(id = R.string.resetPassword)
                    )
                }
            }
        }
    }
}
