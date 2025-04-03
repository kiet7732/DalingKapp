package com.example.dalingk.components.detailUser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import com.example.dalingk.R
import data.viewmodel.UserInputViewModel

class InterestUI : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DalingKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )
                {
//                    InterestUi()
                }
            }
        }
    }
}



@Composable
fun InterestUi(viewModel: UserInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
//        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "Select Up To 3 Interest",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tell us what piques your curiosity and passions",
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        Frame47(viewModel)
    }
}

@Composable
fun Frame47(viewModel: UserInputViewModel) {
    val buttonList = listOf(
        Pair(R.drawable.icon1, "Pets"),
        Pair(R.drawable.icon2, "Reading"),
        Pair(R.drawable.icon3, "Photography"),
        Pair(R.drawable.icon4, "Gaming"),
        Pair(R.drawable.icon5, "Music"),
        Pair(R.drawable.icon6, "Travel"),
        Pair(R.drawable.icon7, "Painting"),
        Pair(R.drawable.icon8, "Politics"),
        Pair(R.drawable.icon9, "Charity"),
        Pair(R.drawable.icon10, "Sports"),
    )

    val rows = listOf(
        buttonList.take(3),
        buttonList.drop(3).take(2),
        buttonList.drop(5).take(3),
        buttonList.drop(8).take(2)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { (_, text) ->
                    IconBtnCustom(
                        icon = buttonList.find { it.second == text }!!.first,
                        text = text,
                        isSelected = viewModel.interests.value.contains(text),
                        onClick = {
                            if (viewModel.interests.value.contains(text)) {
                                viewModel.interests.value = viewModel.interests.value - text
                            } else if (viewModel.interests.value.size < 3) {
                                viewModel.interests.value = viewModel.interests.value + text
                            }
                        }
                    )
                }
            }
        }
    }
}



//        Pair(R.drawable.icon11, "Cooking"),
@Composable
fun IconBtnCustom(
    modifier: Modifier = Modifier,
    icon: Int,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .wrapContentWidth() // Tự động mở rộng theo nội dung
            .requiredHeight(40.dp)
            .clip(shape = RoundedCornerShape(50.dp))
            .background(if (isSelected) Color(0xffff5069) else Color(0xFFFFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 12.dp), // Thêm khoảng cách ngang
        contentAlignment = Alignment.Center
    ) {
        // Dynamic text color based on isSelected
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp) // Thêm padding để cân đối
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(if (isSelected) Color(0xFFFFFFFF) else Color(0xffff5069)),
                modifier = Modifier
                    .requiredSize(size = 20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp)) // Khoảng cách giữa icon và text
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Black,
                style = TextStyle(
                    fontSize = 14.sp
                )
            )
        }
    }
}




@Preview(showBackground = true)
@Composable
fun F1() {
    DalingKTheme {
//        InterestUi()
    }
}