import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ButtonParticleView(
    startX: Float,
    startY: Float,
    particleCount: Int = 50,
    modifier: Modifier = Modifier
) {
    val particles = remember { mutableStateListOf<Particle>() }

    // Funcție pentru generarea particulelor
    fun generateParticles() {
        repeat(particleCount) {
            particles.add(Particle.create(startX, startY))
        }
    }

    // Animăm particulele
    LaunchedEffect(startX, startY) {
        generateParticles()
        while (true) {
            particles.forEach { particle ->
                particle.animate()
            }
            particles.removeAll { it.isComplete }
            delay(16L) // ~60 FPS
        }
    }

    // Desenarea particulelor
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha()),
                radius = particle.radius,
                center = Offset(particle.x.value, particle.y.value)
            )
        }
    }
}

// Date clasă pentru particule
data class Particle(
    val x: Animatable<Float, AnimationVector1D>,
    val y: Animatable<Float, AnimationVector1D>,
    val radius: Float,
    val color: Color,
    private val vx: Float, // Viteză pe X
    private val vy: Float, // Viteză pe Y
    private val lifetime: Int
) {
    var age: Int = 0
        private set

    val isComplete: Boolean
        get() = age > lifetime

    suspend fun animate() {
        if (isComplete) return
        x.animateTo(x.value + vx, animationSpec = tween(16))
        y.animateTo(y.value + vy, animationSpec = tween(16))
        age++
    }

    fun alpha(): Float = 1f - (age.toFloat() / lifetime) // Dispariție treptată

    companion object {
        fun create(startX: Float, startY: Float): Particle {
            val random = Random.Default
            val angle = random.nextFloat() * 360f
            val speed = random.nextFloat() * 100f + 50f // Viteză mai mare
            val velocityX = speed * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
            val velocityY = speed * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()

            return Particle(
                x = Animatable(startX),
                y = Animatable(startY),
                radius = random.nextFloat() * 8f + 4f, // Dimensiuni variate
                color = Color(
                    red = random.nextFloat(),
                    green = random.nextFloat(),
                    blue = random.nextFloat(),
                    alpha = 1f
                ),
                vx = velocityX,
                vy = velocityY,
                lifetime = random.nextInt(30, 50)
            )
        }
    }
}