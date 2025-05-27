package com.example.sawaapplication.screens.onboarding.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.profile.presentation.screens.changeAppLocale
import com.example.sawaapplication.screens.profile.presentation.vm.ThemeViewModel
import com.example.sawaapplication.ui.screenComponent.buttons.GradientButton
import com.example.sawaapplication.ui.theme.OrangeText

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun OnBoardingScreen(
    navController: NavController,
    changeAppTheme: () -> Unit,
) {
    val pagerState = rememberPagerState { 3 }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(integerResource(id = R.integer.mediumSpace).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageV2(
                    page = page,
                    navController = navController,
                    changeAppTheme = changeAppTheme
                )
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun OnboardingPageV2(
    page: Int,
    navController: NavController,
    changeAppTheme: () -> Unit,
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val isArabic by themeViewModel.isArabic.collectAsState()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current

    val titles = listOf(
        stringResource(R.string.onboarding_title_1),
        stringResource(R.string.onboarding_title_2),
        stringResource(R.string.onboarding_title_3)
    )
    val descriptions = listOf(
        stringResource(R.string.onboarding_text_1),
        stringResource(R.string.onboarding_text_2),
        stringResource(R.string.onboarding_text_3)
    )
    val images = listOf(
        R.drawable.first, R.drawable.second, R.drawable.third
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(page) { visible = true }

    val infiniteTransition = rememberInfiniteTransition()

    val imageSize by infiniteTransition.animateFloat(
        initialValue = 230.0f,
        targetValue = 280.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, delayMillis = 200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = integerResource(id = R.integer.mediumSpace).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon-only switch for theme
            IconToggleButton(
                checked = isDarkTheme,
                onCheckedChange = {
                    changeAppTheme()
                    themeViewModel.toggleTheme()
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Theme Icon"
                )
            }

            // Clickable language character
            Box(
                modifier = Modifier
                    .padding(integerResource(id = R.integer.smallerSpace).dp)
                    .size(integerResource(id = R.integer.imageSize).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
                    .clickable {
                        themeViewModel.toggleLanguage()
                        val newLang = if (isArabic) "en" else "ar"
                        changeAppLocale(context, newLang)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "EN" else "ع",
                    fontSize = integerResource(id = R.integer.textFontSize).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.height(integerResource(id = R.integer.mediumSpace).dp))

        val fixedImageHeight = 280.dp
        Box(
            modifier = Modifier
                .height(fixedImageHeight)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            this@Column.AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Image(
                    painter = painterResource(id = images[page]),
                    contentDescription = "Onboarding Image",
                    modifier = Modifier.size(imageSize.dp)
                )
            }
        }

        Text(
            text = titles[page],
            fontSize = integerResource(id = R.integer.TitleFontSize).sp,
            fontWeight = FontWeight.Bold,
            color = OrangeText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(integerResource(id = R.integer.mediumSpace).dp))

        Text(
            text = descriptions[page],
            fontSize = integerResource(id = R.integer.textFontSize).sp,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(integerResource(id = R.integer.smallerSpace).dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(integerResource(id = R.integer.circleSize).dp)
                        .padding(integerResource(id = R.integer.extraSmallSpace).dp)
                        .clip(CircleShape)
                        .background(
                            if (page == index)
                                OrangeText
                            else
                                Color.Gray
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(integerResource(id = R.integer.Space).dp))

        if (page == 2) {
            Spacer(modifier = Modifier.height(integerResource(id = R.integer.extraLargeSpace).dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.width(integerResource(id = R.integer.smallSpace).dp))

                GradientButton(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = false }
                        }
                    },

                    text = stringResource(R.string.get_started),
                    modifier = Modifier
                        .height(integerResource(id = R.integer.getStartedButtonHeight).dp)
                        .width(integerResource(id = R.integer.getStartedButtonWidth).dp)
                        .clip(RoundedCornerShape(integerResource(id = R.integer.buttonRoundCornerShape).dp))
                )
            }
        }
    }
}

