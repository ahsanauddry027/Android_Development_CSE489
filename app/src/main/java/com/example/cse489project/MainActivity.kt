package com.example.cse489project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cse489project.ui.theme.CorrectGreen
import com.example.cse489project.ui.theme.Cse489ProjectTheme
import com.example.cse489project.ui.theme.DumbEnd
import com.example.cse489project.ui.theme.DumbStart
import com.example.cse489project.ui.theme.GeniusText
import com.example.cse489project.ui.theme.GoldEnd
import com.example.cse489project.ui.theme.GoldStart
import com.example.cse489project.ui.theme.LeftCardEnd
import com.example.cse489project.ui.theme.LeftCardStart
import com.example.cse489project.ui.theme.NightBottom
import com.example.cse489project.ui.theme.NightMiddle
import com.example.cse489project.ui.theme.NightTop
import com.example.cse489project.ui.theme.RightCardEnd
import com.example.cse489project.ui.theme.RightCardStart
import com.example.cse489project.ui.theme.WrongRed
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light status/navigation bar icons, since the game background is dark.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            Cse489ProjectTheme {
                BiggerNumberGame()
            }
        }
    }
}

/** Score needed to see the "Genius" pop-up. */
private const val GENIUS_SCORE = 10

/** Which pop-up (if any) is showing. */
private enum class GameDialog { Genius, Dumb }

/** Returns a random number from 0 to 9 that is different from [other], so one is always bigger. */
private fun randomNumberOtherThan(other: Int): Int {
    var number = Random.nextInt(10)
    while (number == other) {
        number = Random.nextInt(10)
    }
    return number
}

@Composable
fun BiggerNumberGame(modifier: Modifier = Modifier) {
    // rememberSaveable keeps the game state when the screen rotates.
    var leftNumber by rememberSaveable { mutableIntStateOf(Random.nextInt(10)) }
    var rightNumber by rememberSaveable { mutableIntStateOf(randomNumberOtherThan(leftNumber)) }
    var points by rememberSaveable { mutableIntStateOf(0) }
    var round by rememberSaveable { mutableIntStateOf(0) }
    var lastGuessCorrect by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var dialog by rememberSaveable { mutableStateOf<GameDialog?>(null) }

    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val shake = remember { Animatable(0f) }

    fun newRound() {
        leftNumber = Random.nextInt(10)
        rightNumber = randomNumberOtherThan(leftNumber)
        round++
    }

    // Called when the user clicks one of the two number cards.
    fun onGuess(pickedLeft: Boolean) {
        val correct = if (pickedLeft) leftNumber > rightNumber else rightNumber > leftNumber
        lastGuessCorrect = correct

        if (correct) {
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            points++
            if (points == GENIUS_SCORE) dialog = GameDialog.Genius
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.Reject)
            scope.launch {
                for (offset in listOf(-24f, 24f, -16f, 16f, -8f, 0f)) {
                    shake.animateTo(offset, tween(durationMillis = 50))
                }
            }
            // Points never go below 0. Guessing wrong at 0 shows the "learn math" pop-up instead.
            if (points == 0) dialog = GameDialog.Dumb else points--
        }
        newRound()
    }

    fun playAgain() {
        points = 0
        lastGuessCorrect = null
        dialog = null
        newRound()
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(NightTop, NightMiddle, NightBottom)))
                .drawBehind {
                    // Two soft colored glows behind the content.
                    val pinkCenter = Offset(0f, size.height * 0.2f)
                    val blueCenter = Offset(size.width, size.height * 0.75f)
                    val radius = size.width * 0.8f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(LeftCardStart.copy(alpha = 0.3f), Color.Transparent),
                            center = pinkCenter,
                            radius = radius
                        ),
                        radius = radius,
                        center = pinkCenter
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(RightCardStart.copy(alpha = 0.25f), Color.Transparent),
                            center = blueCenter,
                            radius = radius
                        ),
                        radius = radius,
                        center = blueCenter
                    )
                }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
            ) {
                // Fills the screen in portrait, and scrolls if it doesn't fit (e.g. landscape).
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    GameHeader()

                    Column(
                        modifier = Modifier.padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.which_is_bigger),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { translationX = shake.value.dp.toPx() },
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NumberCard(
                                number = leftNumber,
                                round = round,
                                colors = listOf(LeftCardStart, LeftCardEnd),
                                onClick = { onGuess(pickedLeft = true) },
                                modifier = Modifier.weight(1f)
                            )
                            VsBadge()
                            NumberCard(
                                number = rightNumber,
                                round = round,
                                colors = listOf(RightCardStart, RightCardEnd),
                                onClick = { onGuess(pickedLeft = false) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        FeedbackMessage(round = round, lastGuessCorrect = lastGuessCorrect)
                    }

                    ScoreCard(points = points)
                }
            }

            when (dialog) {
                GameDialog.Genius -> FancyDialog(
                    emoji = "🏆",
                    title = stringResource(R.string.genius_title),
                    message = stringResource(R.string.genius_message, GENIUS_SCORE),
                    gradient = listOf(GoldStart, GoldEnd),
                    textColor = GeniusText,
                    // Back button or tapping outside also starts a new game, same as Play Again.
                    onDismissRequest = { playAgain() }
                ) {
                    Button(
                        onClick = { playAgain() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeniusText,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(stringResource(R.string.play_again), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                GameDialog.Dumb -> FancyDialog(
                    emoji = "📚",
                    title = stringResource(R.string.dumb_title),
                    message = stringResource(R.string.dumb_message),
                    gradient = listOf(DumbStart, DumbEnd),
                    textColor = Color.White,
                    onDismissRequest = { dialog = null }
                ) {
                    Button(
                        onClick = { dialog = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DumbStart
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(stringResource(R.string.try_again), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                null -> Unit
            }
        }
    }
}

@Composable
private fun GameHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.game_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                brush = Brush.horizontalGradient(listOf(GoldEnd, LeftCardStart))
            ),
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.game_instructions),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
    }
}

/** A big gradient card showing one number. It shrinks a little while pressed. */
@Composable
private fun NumberCard(
    number: Int,
    round: Int,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "pressScale")
    val shape = RoundedCornerShape(28.dp)

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(170.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = 16.dp, shape = shape, spotColor = colors.last()),
        shape = shape,
        color = Color.Transparent,
        contentColor = Color.White,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center
        ) {
            // Keyed on the round too, so the number pops in every round, even if it didn't change.
            AnimatedContent(
                targetState = round to number,
                transitionSpec = {
                    (scaleIn(initialScale = 0.5f) + fadeIn()) togetherWith
                        (scaleOut(targetScale = 1.5f) + fadeOut())
                },
                label = "number"
            ) { (_, value) ->
                Text(text = value.toString(), fontSize = 72.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun VsBadge() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .glass(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.vs), fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

/** Shows "Correct!" or "Wrong!" after each guess, popping in again every round. */
@Composable
private fun FeedbackMessage(round: Int, lastGuessCorrect: Boolean?) {
    AnimatedContent(
        targetState = round to lastGuessCorrect,
        transitionSpec = { (scaleIn(initialScale = 0.7f) + fadeIn()) togetherWith fadeOut() },
        label = "feedback"
    ) { (_, correct) ->
        Box(modifier = Modifier.height(48.dp), contentAlignment = Alignment.Center) {
            if (correct == null) {
                Text(
                    text = stringResource(R.string.tap_to_start),
                    color = Color.White.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = stringResource(if (correct) R.string.correct else R.string.incorrect),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (correct) CorrectGreen else WrongRed)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

/** The points display, with a progress bar towards the Genius score. */
@Composable
private fun ScoreCard(points: Int) {
    val progress by animateFloatAsState(
        targetValue = points.coerceIn(0, GENIUS_SCORE) / GENIUS_SCORE.toFloat(),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.points_label),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        // The number slides up when points go up, and down when they go down.
        AnimatedContent(
            targetState = points,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                } else {
                    (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                }.using(SizeTransform(clip = false))
            },
            label = "points"
        ) { value ->
            Text(text = value.toString(), fontSize = 48.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(GoldStart, GoldEnd)))
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(
                R.string.genius_progress,
                points.coerceAtMost(GENIUS_SCORE),
                GENIUS_SCORE
            ),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/** A colorful pop-up that bounces in, with a pulsing emoji on top. */
@Composable
private fun FancyDialog(
    emoji: String,
    title: String,
    message: String,
    gradient: List<Color>,
    textColor: Color,
    onDismissRequest: () -> Unit,
    actions: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        val popIn = remember { Animatable(0.6f) }
        LaunchedEffect(Unit) {
            popIn.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 700), RepeatMode.Reverse),
            label = "emojiScale"
        )
        val shape = RoundedCornerShape(28.dp)

        CompositionLocalProvider(LocalContentColor provides textColor) {
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = popIn.value
                        scaleY = popIn.value
                        alpha = ((popIn.value - 0.6f) / 0.4f).coerceIn(0f, 1f)
                    }
                    .shadow(elevation = 24.dp, shape = shape)
                    .clip(shape)
                    .background(Brush.linearGradient(gradient))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = emoji,
                    fontSize = 72.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                actions()
            }
        }
    }
}

/** Frosted-glass look: a faint white fill with a thin white border. */
private fun Modifier.glass(shape: Shape): Modifier = this
    .clip(shape)
    .background(Color.White.copy(alpha = 0.08f))
    .border(1.dp, Color.White.copy(alpha = 0.18f), shape)

@Preview(showBackground = true)
@Composable
fun BiggerNumberGamePreview() {
    Cse489ProjectTheme {
        BiggerNumberGame()
    }
}
