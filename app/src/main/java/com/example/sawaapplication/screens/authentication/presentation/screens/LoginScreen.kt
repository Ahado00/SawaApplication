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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.sawaapplication.screens.authentication.presentation.vmModels.LoginViewModel
import com.example.sawaapplication.screens.authentication.presentation.vmModels.ValidationInputViewModel
import com.example.sawaapplication.screens.authentication.presentation.vmModels.handleAuthStateLogin
import com.example.sawaapplication.ui.screenComponent.CustomCard
import com.example.sawaapplication.ui.screenComponent.CustomTextField
import com.example.sawaapplication.ui.screenComponent.buttons.GradientButton
import kotlinx.coroutines.delay

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun LoginScreen(
    navController: NavController
) {

    // Entry animations
    var bannerVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    val loginViewModel: LoginViewModel = hiltViewModel()
    var showPassword by remember { mutableStateOf(false) }
    val validationInputViewModel: ValidationInputViewModel = hiltViewModel()
    val context = LocalContext.current

    val emailInput = loginViewModel.email.trim()
    val passwordInput = loginViewModel.password.trim()

    val authState by loginViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        handleAuthStateLogin(authState, context, navController)

    }
    LaunchedEffect(Unit) {
        bannerVisible = true
        delay(300)
        cardVisible = true
    }

    val bannerHeight = 250.dp
    val logoSize = 90.dp
    val cardOffsetY = bannerHeight * 0.7f + 24.dp

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

        //Login card

        AnimatedVisibility(
            visible = cardVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = cardOffsetY)
                .width(integerResource(id = R.integer.cardWidth).dp)
                .height(integerResource(id = R.integer.cardHeightLogin).dp),
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.Login),
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
                        value = loginViewModel.email,
                        onValueChange = { loginViewModel.email = it },
                        label = stringResource(id = R.string.email)
                    )
                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.mediumSpace).dp))

                    CustomTextField(
                        value = loginViewModel.password,
                        onValueChange = { loginViewModel.password = it },
                        label = stringResource(id = R.string.password),
                        isPassword = true,
                        showPassword = showPassword,
                        onTogglePasswordVisibility = { showPassword = !showPassword }
                    )

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = integerResource(id = R.integer.smallSpace).dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            stringResource(id = R.string.forgot_password),
                            fontSize = integerResource(id = R.integer.smallText).sp,
                            color = Color(0xFFC02525),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.ForgotPass.route)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.extraLargeSpace).dp))

                    GradientButton(
                        onClick = {
                            validationInputViewModel.email = emailInput
                            validationInputViewModel.password = passwordInput
                            validationInputViewModel.validateEmailAndPassword()

                            validationInputViewModel.emailAndPasswordError?.let {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            } ?: loginViewModel.login(emailInput, passwordInput)
                        },
                        text = stringResource(id = R.string.Login),
                        modifier = Modifier
                            .width(integerResource(id = R.integer.buttonWidth).dp)
                            .height(integerResource(id = R.integer.buttonHeight).dp)
                    )

                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.extraLargeSpace).dp))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = integerResource(id = R.integer.extraSmallSpace).dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(id = R.string.dont_have_account),
                            fontSize = integerResource(id = R.integer.smallText).sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(id = R.string.Signup),
                            fontSize = integerResource(id = R.integer.smallText).sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.SignUp.route)
                            }
                        )
                    }
                }
            }
        }
    }
}
