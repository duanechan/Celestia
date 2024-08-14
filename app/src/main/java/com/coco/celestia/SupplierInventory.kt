package com.coco.celestia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.LightGreen
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.ui.theme.PurpleGrey40

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SupplierInventory(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 75.dp)
            .verticalScroll(rememberScrollState())
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(LightGreen)
            .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp)){
                Text(text = "Inventory", fontSize = 31.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        var text by remember {mutableStateOf("")}
        var active by remember{ mutableStateOf(false) }
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(PurpleGrey40)
            .padding(top = 10.dp, bottom = 15.dp, start = 25.dp, end = 16.dp)){
            SearchBar(
                query = text,
                onQueryChange = {},
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text(text = "Search...", color = Color.Black, fontSize = 15.sp)},
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")},
                modifier = Modifier
                    .width(225.dp)
                    .height(35.dp)){
                //TO DO
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(modifier = Modifier
            .width(500.dp)
            .height(200.dp)
            .offset(x = (-16).dp, y = 0.dp)
            .padding(top = 0.dp, bottom = 5.dp, start = 30.dp, end = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightGreen
            )){
            Text(text = "Item 1", fontSize = 29.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        Card(modifier = Modifier
            .width(500.dp)
            .height(200.dp)
            .offset(x = (-16).dp, y = 0.dp)
            .padding(top = 0.dp, bottom = 5.dp, start = 30.dp, end = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightGreen
            )){
            Text(text = "Item 2", fontSize = 29.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        Card(modifier = Modifier
            .width(500.dp)
            .height(200.dp)
            .offset(x = (-16).dp, y = 0.dp)
            .padding(top = 0.dp, bottom = 5.dp, start = 30.dp, end = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightGreen
            )){
            Text(text = "Item 3", fontSize = 29.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        Card(modifier = Modifier
            .width(500.dp)
            .height(200.dp)
            .offset(x = (-16).dp, y = 0.dp)
            .padding(top = 0.dp, bottom = 5.dp, start = 30.dp, end = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightGreen
            )){
            Text(text = "Item 4", fontSize = 29.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp))
        }

    }

}