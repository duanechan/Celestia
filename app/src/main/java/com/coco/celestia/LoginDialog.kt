package com.coco.celestia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel

@Composable
fun LoginDialog(userViewModel: UserViewModel, onDismiss: () -> Unit, onLogin: () -> Unit) {
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = when (userState) {
                    is UserState.ERROR -> "Login Failed"
                    is UserState.LOGIN_SUCCESS, UserState.SUCCESS -> "Login Successful"
                    else -> ""
                }
            )
        },
        text = {
            when (userState) {
                is UserState.ERROR -> Text((userState as UserState.ERROR).message)
                is UserState.LOGIN_SUCCESS, UserState.SUCCESS -> Text("Welcome back, ${userData?.firstname}!")
                else -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        },
        confirmButton = {
            if (userState is UserState.ERROR || userState is UserState.LOGIN_SUCCESS || userState is UserState.SUCCESS) {
                Button(
                    onClick = {
                        when (userState) {
                            is UserState.ERROR -> { onDismiss() }
                            is UserState.LOGIN_SUCCESS -> { onLogin() }
                            else -> { onDismiss() }
                        }
                    }
                ) {
                    Text(
                        text = when (userState) {
                            is UserState.ERROR -> "Retry"
                            is UserState.LOGIN_SUCCESS, UserState.SUCCESS -> "Let's Go!"
                            else -> ""
                        }
                    )
                }
            }
        }
    )
}
