package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.ui.theme.Indigo600
import com.zenith26.fest.ui.theme.Slate200
import com.zenith26.fest.ui.theme.Slate700

data class CategoryItem(
    val name: String,
    val displayLabel: String
)

val FestCategories = listOf(
    CategoryItem("All", "All"),
    CategoryItem("Technical", "💻 Technical"),
    CategoryItem("Cultural", "🎭 Cultural"),
    CategoryItem("Sports", "⚽ Sports"),
    CategoryItem("Gaming", "🎮 Gaming"),
    CategoryItem("Workshop", "🖌️ Workshop")
)

@Composable
fun CategoryFilterChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FestCategories.forEach { cat ->
            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) Indigo600 else Color(0xFFF1F5F9)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Indigo600 else Slate200,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onCategorySelected(cat.name) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = cat.displayLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Slate700
                )
            }
        }
    }
}
