package com.example.sawaapplication.screens.authentication.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
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
import com.example.sawaapplication.ui.screenComponent.GradientButton
import com.example.sawaapplication.ui.theme.black

@Composable
fun SignUpScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val signUpViewModel: SignUpViewModel = hiltViewModel()
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val authState = signUpViewModel.authState.collectAsState().value

    LaunchedEffect(authState) {
        handleAuthStateSignUp(authState, context,navController)// Add navController argument when navigation is finished

    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.Signup),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = integerResource(id = R.integer.cardHeaderSize).sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                top = integerResource(id = R.integer.cardHeaderTopPadding).dp,
                bottom = integerResource(id = R.integer.cardHeaderBottomPadding).dp
            )
        )
        Spacer(modifier = Modifier.padding(bottom= integerResource(id = R.integer.extraSmallSpace).dp))
        CustomCard(
            modifier = Modifier
                .size(
                    width = integerResource(id = R.integer.cardWidth).dp,
                    height = integerResource(id = R.integer.cardHeightSignUp).dp
                )
                .align(CenterHorizontally),
        ) {
            Spacer(modifier = Modifier.padding(bottom=integerResource(id = R.integer.smallSpace).dp))

            Column(
                modifier = Modifier
                    .padding((integerResource(id = R.integer.smallSpace).dp)),
                horizontalAlignment = CenterHorizontally
            ) {
                CustomTextField(
                    value = signUpViewModel.name,
                    onValueChange =  {signUpViewModel.name = it},
                    label = stringResource(id = R.string.username)
                )
                Spacer(modifier = Modifier.padding(bottom = integerResource(id = R.integer.smallerSpace).dp))

                CustomTextField(
                    value =signUpViewModel.email,
                    onValueChange = {signUpViewModel.email= it},
                    label = stringResource(id = R.string.email),
                )
                Spacer(modifier = Modifier.padding(bottom = integerResource(id = R.integer.smallerSpace).dp))

                CustomTextField(
                    value = signUpViewModel.password,
                    onValueChange = {signUpViewModel.password= it},
                    label = stringResource(id = R.string.password),
                    isPassword = true,
                    showPassword = showPassword,
                    onTogglePasswordVisibility = { showPassword = !showPassword },
                )
                Spacer(modifier = Modifier.padding(bottom = integerResource(id = R.integer.smallerSpace).dp))

                CustomTextField(
                    value = signUpViewModel.confirmPassword ,
                    onValueChange ={signUpViewModel.confirmPassword = it} ,
                    label = stringResource(id = R.string.confirmPassword),
                    isPassword = true,
                    showPassword = showConfirmPassword,
                    onTogglePasswordVisibility = { showConfirmPassword = !showConfirmPassword },
                )
                Spacer(modifier = Modifier.padding(integerResource(id = R.integer.smallSpace).dp))
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
                    modifier = Modifier.padding(top= integerResource(id = R.integer.largeSpace).dp,bottom = integerResource(id = R.integer.mediumSpace).dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = integerResource(id = R.integer.mediumSpace).dp),

                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.already_have_account),
                    fontSize = integerResource(id = R.integer.smallText).sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(id = R.string.Login),
                    fontSize = integerResource(id = R.integer.smallText).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { navController.navigate(Screen.Login.route) }
                )
            }
        }
    }
}
