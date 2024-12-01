@file:Suppress("DEPRECATION")

package com.example.pineappleexpense.ui.screens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pineappleexpense.ui.components.BottomBar
import com.example.pineappleexpense.ui.components.TopBar
import androidx.navigation.compose.rememberNavController
import com.example.pineappleexpense.ui.viewmodel.AccessViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ViewPreviousExpense(navHost: NavHostController, viewModel: AccessViewModel, modifier: Modifier = Modifier) {
    val userRole = viewModel.userState.collectAsState().value
    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9EEFF),
        bottomBar = {
            BottomBar(navHost, viewModel)
        },
        topBar = {
            TopBar(navHost, viewModel)
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding).padding(bottom = 8.dp)
            .background(Color(0xFFF9EEFF))
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExpenseCard("Best Buy", "Supplies", 50.00)
            ExpenseCard("Olive Garden", "Food", 50.00)
            Spacer(modifier = Modifier.weight(1f))
            TotalCard(50.00 + 50.00)

        }
    }
}

@Composable
fun ExpenseCard(storeName:String, category:String, amount: Double, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
            .padding(top = 8.dp)
            .padding(horizontal = 4.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.onPrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,

                ) {

                    Text(
                        text = category.first().toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = storeName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$category: \$${"%.2f".format(amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Receipt here",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Category Icon",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

        }
    }
}

@Composable
fun TotalCard(total: Double) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(8.dp))
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            Text(
                text = "Period Total:",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "\$${"%.2f".format(total)}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

    }
}

@Preview
@Composable
fun PreviewPrevious() {
    val navController = rememberNavController()
    val viewModel = AccessViewModel()
    ViewPreviousExpense(navController, viewModel)
}