package ru.mpei.grasshopper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.mpei.grasshopper.ui.theme.GrasshopperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {

            val viewModel by viewModel<MainViewModel>()

            val time1 by viewModel.time1.collectAsState()
            val time2 by viewModel.time2.collectAsState()
            val time4 by viewModel.time4.collectAsState()
            val time8 by viewModel.time8.collectAsState()
            val time16 by viewModel.time16.collectAsState()
            val timeAES by viewModel.timeAES.collectAsState()
            val timeGOST by viewModel.timeGOST.collectAsState()

            GrasshopperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {

                            Text(text = "Время: $time1 мс")
                            Button(onClick = { viewModel.cipher(count = 1) }) {
                                Text(text = "Шифрование, 1 поток")
                            }

                            Spacer(modifier = Modifier.height(height = 32.dp))

                            Text(text = "Время: $time2 мс")
                            Button(onClick = { viewModel.cipher(count = 2) }) {
                                Text(text = "Шифрование, 2 потока")
                            }

                            Spacer(modifier = Modifier.height(height = 32.dp))

                            Text(text = "Время: $time4 мс")
                            Button(onClick = { viewModel.cipher(count = 4) }) {
                                Text(text = "Шифрование, 4 потока")
                            }

                            Spacer(modifier = Modifier.height(height = 32.dp))

                            Text(text = "Время: $time8 мс")
                            Button(onClick = { viewModel.cipher(count = 8) }) {
                                Text(text = "Шифрование, 8 потоков")
                            }

                            Spacer(modifier = Modifier.height(height = 32.dp))

                            Text(text = "Время: $time16 мс")
                            Button(onClick = { viewModel.cipher(count = 16) }) {
                                Text(text = "Шифрование, 16 потоков")
                            }

                            Spacer(modifier = Modifier.height(height = 32.dp))

                            Text(text = "Время: $timeAES мс")
                            Button(onClick = { viewModel.cipherAES() }) {
                                Text(text = "Шифрование, AES")
                            }

                            Spacer(modifier = Modifier.height(height = 32.dp))

                            Text(text = "Время: $timeGOST мс")
                            Button(onClick = { viewModel.cipherGOST3412_2015() }) {
                                Text(text = "Шифрование, GOST")
                            }

                            Spacer(modifier = Modifier.height(height = 32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GrasshopperTheme {
        Greeting("Android")
    }
}