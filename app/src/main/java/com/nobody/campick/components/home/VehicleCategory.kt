package com.nobody.campick.components.home


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobody.campick.R
import kotlin.collections.listOf

data class CategoryItem(val imageRes: Int, val title: String)

@Composable
fun VehicleCategory() {
    val categories = listOf(
        CategoryItem(R.drawable.motorhome, "모터홈"),
        CategoryItem(R.drawable.trailer, "트레일러"),
        CategoryItem(R.drawable.category, "픽업캠퍼"),
        CategoryItem(R.drawable.camping_van, "캠핑밴")
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.DirectionsCar,
                contentDescription = "차량 종류",
                tint = Color(0xFFFF6F00) // AppColors.brandOrange 대체
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "차량 종류",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(120.dp) // Grid 높이 제한 (필요 시 조정)
        ) {
            items(categories) { category ->
                VehicleCategoryItem(category.imageRes, category.title)
            }
        }
    }
}

@Composable
fun VehicleCategoryItem(imageRes: Int, title: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}