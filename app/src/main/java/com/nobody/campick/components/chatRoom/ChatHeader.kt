package com.nobody.campick.components.chatRoom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nobody.campick.R
import com.nobody.campick.models.chat.ChatSeller
import com.nobody.campick.models.chat.ChatVehicle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatHeader(
    seller: ChatSeller,
    vehicle: ChatVehicle,
    onBack: () -> Unit,
    onCall: () -> Unit
) {
    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B211A))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))


            Image(
                painter = painterResource(id = R.drawable.test_image1),
                contentDescription = seller.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))


            Column {
                Text(
                    text = seller.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (seller.isOnline) Color.Green else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (seller.isOnline) "온라인" else formatTime(seller.lastSeen),
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))


            IconButton(
                onClick = onCall,
                enabled = !seller.phoneNumber.isNullOrEmpty(),
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "전화",
                    tint = Color.White
                )
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B211A))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.test_image1),
                contentDescription = vehicle.title,
                modifier = Modifier
                    .size(width = 60.dp, height = 45.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.status,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(Color.Green, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    text = vehicle.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = "${vehicle.price}만원",
                    color = Color(0xFFFF9800),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "자세히",
                tint = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatTime(date: Date?): String {
    if (date == null) return "오프라인"
    val formatter = SimpleDateFormat("HH:mm", Locale.KOREA)
    return formatter.format(date)
}