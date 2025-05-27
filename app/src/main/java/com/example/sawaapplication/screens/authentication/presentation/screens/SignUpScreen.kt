package com.example.sawaapplication.screens.authentication.presentation.screens

import android.annotation.SuppressLint
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
import com.example.sawaapplication.screens.authentication.presentation.vmModels.SignUpViewModel
import com.example.sawaapplication.screens.authentication.presentation.vmModels.handleAuthStateSignUp
import com.example.sawaapplication.ui.screenComponent.CustomCard
import com.example.sawaapplication.ui.screenComponent.CustomTextField
import com.example.sawaapplication.ui.screenComponent.buttons.GradientButton
import kotlinx.coroutines.delay

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun SignUpScreen(
    navController: NavController,
) {

    val signUpViewModel: SignUpViewModel = hiltViewModel()
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // Entry animations
    var bannerVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val authState = signUpViewModel.authState.collectAsState().value

    LaunchedEffect(authState) {
        handleAuthStateSignUp(
            authState,
            context,
            navController
        )

    }
    LaunchedEffect(Unit) {
        bannerVisible = true
        delay(300)
        cardVisible = true
    }

    val bannerHeight = 250.dp
    val logoSize = 90.dp
    val cardOffsetY = bannerHeight * 0.6f + 16.dp

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

        AnimatedVisibility(
            visible = cardVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = cardOffsetY)
                .width(integerResource(id = R.integer.cardWidth).dp)
                .height(integerResource(id = R.integer.cardHeightSignUp).dp),
            enter = fadeIn(tween(300)) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(300)
            ),
            exit = fadeOut()
        ) {
            Spacer(modifier = Modifier.height(integerResource(id = R.integer.mediumSpace).dp))

            CustomCard(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(integerResource(id = R.integer.smallSpace).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.Signup),
                        fontSize = integerResource(id = R.integer.cardHeaderSize).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(
                            top = integerResource(id = R.integer.cardHeaderTopPadding).dp,
                            bottom = integerResource(id = R.integer.cardHeaderBottomPadding).dp
                        )
                    )
                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.extraSmallSpace).dp))

                    CustomTextField(
                        value = signUpViewModel.name,
                        onValueChange = { signUpViewModel.name = it },
                        label = stringResource(id = R.string.username)
                    )
                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.smallerSpace).dp))

                    CustomTextField(
                        value = signUpViewModel.email,
                        onValueChange = { signUpViewModel.email = it },
                        label = stringResource(id = R.string.email)
                    )
                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.smallerSpace).dp))

                    CustomTextField(
                        value = signUpViewModel.password,
                        onValueChange = { signUpViewModel.password = it },
                        label = stringResource(id = R.string.password),
                        isPassword = true,
                        showPassword = showPassword,
                        onTogglePasswordVisibility = { showPassword = !showPassword }
                    )
                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.smallerSpace).dp))

                    CustomTextField(
                        value = signUpViewModel.confirmPassword,
                        onValueChange = { signUpViewModel.confirmPassword = it },
                        label = stringResource(id = R.string.confirmPassword),
                        isPassword = true,
                        showPassword = showConfirmPassword,
                        onTogglePasswordVisibility = { showConfirmPassword = !showConfirmPassword }
                    )
                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.mediumSpace).dp))

                    GradientButton(
                        onClick = {
                            signUpViewModel.signUp(
                                name = signUpViewModel.name,
                                email = signUpViewModel.email,
                                password = signUpViewModel.password,
                                confirmPassword = signUpViewModel.confirmPassword
                            )
                        },
                        text = stringResource(id = R.string.Signup),
                        modifier = Modifier
                            .width(integerResource(id = R.integer.buttonWidth).dp)
                            .height(integerResource(id = R.integer.buttonHeight).dp)
                    )

                    Spacer(modifier = Modifier.height(integerResource(id = R.integer.largeSpace).dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = integerResource(id = R.integer.extraSmallSpace).dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = R.string.already_have_account),
                            fontSize = integerResource(id = R.integer.smallText).sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(id = R.string.Login),
                            fontSize = integerResource(id = R.integer.smallText).sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Login.route)
                            }
                        )
                    }
                }
            }
        }
    }
}
