package com.example.bookbuffet.view

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bookbuffet.R
import com.example.bookbuffet.ui.theme.Purple700
import com.example.bookbuffet.ui.theme.Teal200
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(navController: NavController, context: ComponentActivity) {
    var startAnimation by remember { mutableStateOf(false)}
    val alphaAnimation = animateFloatAsState(
        targetValue = if(startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 3000
        )
    )
    
    LaunchedEffect(key1 = true){
        startAnimation = true
        delay(4000)
        navController.popBackStack()
        navController.navigate(route = "signin")
    }
    Splash(alpha = alphaAnimation.value)
}


@Composable
fun Splash(alpha: Float){
    Box(modifier = Modifier
        .background(if (isSystemInDarkTheme()) Color.Black else Purple700)
        .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
//            verticalArrangement = Arrangement.Center
        ){
            Icon(
                modifier = Modifier
                    .size(195.dp)
                    .alpha(alpha),
                painter = painterResource(id = R.drawable.ic_baseline_menu_book_24),
                contentDescription = "Logo",
                tint = Color.Black)

            Spacer(modifier = Modifier.height(20.dp))

            Text(modifier = Modifier.alpha(alpha), text = "Book Buffet", fontSize = 32.sp, color = Teal200, style = MaterialTheme.typography.h1)
        }

    }
}

@Preview
@Composable
fun SplashPreview() {
    Splash(alpha = 1f)
}