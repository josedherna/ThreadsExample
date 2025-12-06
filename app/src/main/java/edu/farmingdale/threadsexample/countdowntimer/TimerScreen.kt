package edu.farmingdale.threadsexample.countdowntimer

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import android.widget.NumberPicker
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.DecimalFormat
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
fun playAlarmSound(context: Context) {
    try {
        val notificationUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (notificationUri != null) {
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone.play()
        }
    } catch (e: Exception) {
        Log.e("TimerAudio", "Error playing notification sound", e)
    }
}

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    timerViewModel: TimerViewModel = viewModel()
) {
    val progress: Float = if (timerViewModel.totalMillis > 0 && timerViewModel.isRunning) {
        timerViewModel.remainingMillis.toFloat() / timerViewModel.totalMillis.toFloat()
    } else {
        1.0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "TimerProgressAnimation"
    )

//    val timerTextColor = if  {
//        Color.Red
//    } else {
//        MaterialTheme.colorScheme.onSurface
//    }

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        timerViewModel.timerFinishedEvent.collect {
            playAlarmSound(context)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .padding(20.dp)
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (timerViewModel.isRunning) {

            }
            Text(
                text = timerText(timerViewModel.remainingMillis),
                fontSize = 60.sp,
                color = if (timerViewModel.remainingMillis <= 10000L && timerViewModel.isRunning) {
                    Color.Red
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (timerViewModel.remainingMillis <= 10000L && timerViewModel.isRunning) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
        }
        if (timerViewModel.isRunning) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(240.dp),
                strokeWidth = 10.dp,
                color = if (timerViewModel.remainingMillis <= 10000L && timerViewModel.isRunning) {
                    Color.Red
                }
                else {
                    MaterialTheme.colorScheme.primary
                },
            )
            Spacer(modifier = Modifier.height(50.dp))
        }

        TimePicker(
            hour = timerViewModel.selectedHour,
            min = timerViewModel.selectedMinute,
            sec = timerViewModel.selectedSecond,
            onTimePick = timerViewModel::selectTime,
        )
        if (timerViewModel.isRunning) {
            Button(
                onClick = timerViewModel::pauseTimer,
                modifier = modifier.padding(top = 50.dp)
            ) {
                Text("Pause")
            }

            Button(
                onClick = timerViewModel::cancelTimer,
                modifier = modifier.padding(50.dp)
            ) {
                Text("Cancel")
            }
        } else if (timerViewModel.isPaused) {
            Button(
                onClick = timerViewModel::startTimer,
                modifier = modifier.padding(top = 50.dp)
            ) {
                Text("Resume")
            }
            Button(
                onClick = timerViewModel::cancelTimer,
                modifier = modifier.padding(top = 50.dp)
            ) {
                Text("Cancel")
            }
        }
        else {
            Button(
                enabled = timerViewModel.selectedHour +
                        timerViewModel.selectedMinute +
                        timerViewModel.selectedSecond > 0,
                onClick = timerViewModel::startTimer,
                modifier = modifier.padding(top = 50.dp)
            ) {
                Text("Start")
            }
        }
    }
}



fun timerText(timeInMillis: Long): String {
    val duration: Duration = timeInMillis.milliseconds
    return String.format(
        Locale.getDefault(),"%02d:%02d:%02d",
        duration.inWholeHours, duration.inWholeMinutes % 60, duration.inWholeSeconds % 60)
}

@Composable
fun TimePicker(
    hour: Int = 0,
    min: Int = 0,
    sec: Int = 0,
    onTimePick: (Int, Int, Int) -> Unit = { _: Int, _: Int, _: Int -> }
) {
    // Values must be remembered for calls to onPick()
    var hourVal by remember { mutableIntStateOf(hour) }
    var minVal by remember { mutableIntStateOf(min) }
    var secVal by remember { mutableIntStateOf(sec) }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hours")
            NumberPickerWrapper(
                initVal = hourVal,
                maxVal = 99,
                onNumPick = {
                    hourVal = it
                    onTimePick(hourVal, minVal, secVal)
                }
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            Text("Minutes")
            NumberPickerWrapper(
                initVal = minVal,
                onNumPick = {
                    minVal = it
                    onTimePick(hourVal, minVal, secVal)
                }
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Seconds")
            NumberPickerWrapper(
                initVal = secVal,
                onNumPick = {
                    secVal = it
                    onTimePick(hourVal, minVal, secVal)
                }
            )
        }
    }
}

@Composable
fun NumberPickerWrapper(
    initVal: Int = 0,
    minVal: Int = 0,
    maxVal: Int = 59,
    onNumPick: (Int) -> Unit = {}
) {
    val numFormat = NumberPicker.Formatter { i: Int ->
        DecimalFormat("00").format(i)
    }

    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                setOnValueChangedListener { numberPicker, oldVal, newVal -> onNumPick(newVal) }
                minValue = minVal
                maxValue = maxVal
                value = initVal
                setFormatter(numFormat)
            }
        }
    )
}