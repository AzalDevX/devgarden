package com.azaldev.garden.games.santiagoarkua

import android.content.ClipData
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.LifecycleOwner
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random
import android.graphics.*
import android.os.Handler
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import java.util.concurrent.TimeUnit

/**
 * Credits https://github.com/monsterbrain
 * and his repository https://github.com/monsterbrain/android-drag-picture-puzzle-game
 */

class Piece @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var targetX: Float = 0f
    var targetY: Float = 0f

    var pieceWidth: Float = 0f
    var pieceHeight: Float = 0f
}

class Game5_Win3 : AppCompatActivity() {
    private lateinit var piecesContainer: LinearLayout
    private val pieces = mutableListOf<Piece>()
    private var screenWidth : Float = 0f// resources.displayMetrics.widthPixels.toFloat() - 200
    private var screenHeight : Float = 0f // resources.displayMetrics.heightPixels.toFloat() - 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game5_win3)

        // make an array of drawables
        val drawables = arrayOf(
            R.drawable.karramarro,
            R.drawable.portua2,
            R.drawable.cool_lanscape
        )

        val bitmap = BitmapFactory.decodeResource(resources, drawables.random(random = Random(System.currentTimeMillis())))
        val dragGameView = findViewById<DragAndPlaceGameView>(R.id.dragGameView)
        dragGameView.setBitmap(bitmap)

        screenWidth = resources.displayMetrics.widthPixels.toFloat() - 200
        screenHeight = resources.displayMetrics.heightPixels.toFloat() - 200
    }
}

class DragAndPlaceGameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private lateinit var imgSrcRect: Rect
    private lateinit var dstRect: Rect
    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var debugPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val game_id = 5;
    private var database : AppDatabase;
    private var gameDao : GameDao;

    private var isScrambled = false
    private var isFinished = false
    private var scrambleStartTime: Long = 0
    private val scrambleDuration = 2000L // 2 seconds
    private val SNAP_THRESHOLD = 200

    private val numCols = 3
    private val numRows = 3

    private var imgBitmap: Bitmap? = null
    private val pieceRects = mutableListOf<Rect>()

    private val pieces = mutableListOf<PuzzlePiece>()
    private var movingPiece: PuzzlePiece? = null

    private var bufferBitmap: Bitmap? = null
    private var bufferCanvas: Canvas? = null

    private var handler: Handler

    inner class PuzzlePiece(
        val srcRect: Rect,
        var destRect: Rect
    ) {
        val initialRect = Rect(destRect)

        fun moveToTouchPoint(x: Float, y: Float) {
            destRect.offsetTo((x - destRect.width()/2).toInt(), (y -destRect.height()/2).toInt())
        }
    }

    init {
        debugPaint.style = Paint.Style.STROKE
        debugPaint.color = Color.RED
        debugPaint.strokeWidth = 2f

        handler = Handler()

        database = AppDatabase.getInstance(context)
        gameDao = database.GameDao();
    }

    fun setBitmap(bmp: Bitmap) {
        imgBitmap = bmp
        imgSrcRect = Rect(0, 0, bmp.width, bmp.height)

        invalidate()
    }

    private val imgWidthInPercent = 0.70f
    private val imgHeightInPercent = 0.90f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        bufferBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bufferCanvas = Canvas(bufferBitmap!!)

        var imgWidth = imgWidthInPercent * w
        val imgHeight = if (imgWidth > h) h*imgHeightInPercent else imgWidth
        imgWidth = imgHeight // h == w

        val leftMargin = ((w - imgWidth) / 2f).toInt()
        val topMargin = ((h - imgHeight) / 2f).toInt()

        dstRect = Rect(leftMargin, topMargin, leftMargin + imgWidth.toInt(), topMargin + imgHeight.toInt())

        val numPieces = numCols * numRows
        val pieceWidth = (imgWidth / numCols).toInt()

        val srcWidth = imgBitmap?.width ?: 0
        val srcPieceWidth = srcWidth / numCols

        for (i in 0 until numRows) {
            for (j in 0 until numCols) {
                val rect = Rect(
                    leftMargin + (j*pieceWidth),
                    topMargin + (i*pieceWidth),
                    leftMargin + (j+1)*pieceWidth, topMargin + (i+1)*pieceWidth
                )
                pieceRects.add(rect)

                val srcRect = Rect(
                    (j*srcPieceWidth), (i*srcPieceWidth),
                    (j+1)*srcPieceWidth, (i+1)*srcPieceWidth
                )
                val piece = PuzzlePiece(srcRect, rect)
                pieces.add(piece)
            }
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        imgBitmap?.let {
            bufferCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            if (!isScrambled) {
                canvas.drawBitmap(it, imgSrcRect, dstRect, mPaint)
                if (System.currentTimeMillis() - scrambleStartTime < scrambleDuration) {
                    handler.postDelayed({ invalidate() }, 16) // Update every 16 milliseconds
                } else {
                    startScramble()
                }
            } else {
                for (piece in pieces) {
                    bufferCanvas?.drawBitmap(it, piece.srcRect, piece.destRect, mPaint)

                    /*
                      canvas.drawLine(
                            piece.destRect.centerX().toFloat(),
                            piece.destRect.centerY().toFloat(),
                            piece.initialRect.centerX().toFloat(),
                            piece.initialRect.centerY().toFloat(),
                            debugPaint
                        )
                    */
                }
                bufferBitmap?.let { canvas.drawBitmap(it, 0f, 0f, mPaint) }
                // drawDebugRects(pieceRects, canvas)

                // Check if the puzzle is completed
                if (isPuzzleCompleted() && !isFinished) {
                    isFinished = true
                    Toast.makeText(context, "Puzzle Completed!", Toast.LENGTH_SHORT).show()

                    // launch thread
                    GlobalScope.launch(Dispatchers.IO) {
                        TimeUnit.SECONDS.sleep(2);
                        val current_game = gameDao.getGame(game_id);

                        val next_game = current_game.getActivityProgress(1);
                        gameDao.adv_progress(game_id, 1);

                        GlobalScope.launch(Dispatchers.Main) {
                            Log.i("devl|game52", "Moving to the next game")

                            if (next_game != null)
                                Utilities.startActivity(context, next_game)
                            else
                                Utilities.startActivity(context, LandingActivity::class.java)

                            (context as AppCompatActivity).finish()
                        }
                    }
                } else { }
            }
        }
    }

    private fun isPuzzleCompleted(): Boolean {
        // Log.i("devl|game5", "Checking if puzzle is completed!")

        for ((i, piece) in pieces.withIndex()) {
            val distance = calculateDistance(piece.destRect, piece.initialRect)
            // Log.i("devl|game5", "($i) distance: $distance")
            if (distance > SNAP_THRESHOLD) {
                return false
            }
        }
        return true
    }

    private fun snapToTarget(piece: PuzzlePiece) {
        val distance = calculateDistance(piece.destRect, piece.initialRect)
        Log.i("devl|game5", "Snap distance: $distance < $SNAP_THRESHOLD")
        if (distance < SNAP_THRESHOLD) {
            Log.i("devl|game5", "Snapping to target, distance: $distance")
//            piece.initialRect.set(piece.destRect)
            piece.moveToTouchPoint(piece.initialRect.centerX().toFloat(), piece.initialRect.centerY().toFloat())
            invalidate()
        }
    }

    private fun calculateDistance(rect1: Rect, rect2: Rect): Float {
        val centerX1 = rect1.centerX()
        val centerY1 = rect1.centerY()
        val centerX2 = rect2.centerX()
        val centerY2 = rect2.centerY()

        return Math.sqrt((centerX1 - centerX2).toDouble().pow(2) + (centerY1 - centerY2).toDouble().pow(2)).toFloat()
    }

    private fun drawDebugRects(rectList: MutableList<Rect>, canvas: Canvas?) {
        canvas?.let {
            for (rect in rectList) {
                it.drawRect(rect, debugPaint)
            }
        }
    }

    // currently touch is handled in the activity
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isScrambled) {
                    for (piece in pieces) {
                        if (piece.destRect.contains(event.x.toInt(), event.y.toInt())) {
                            movingPiece = piece
                            Log.i("xxy", "touched: $piece")
                            break
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                movingPiece?.let {
                    it.moveToTouchPoint(event.x, event.y)
                    invalidate()
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                movingPiece?.let { snapToTarget(it) }
                invalidate()
            }
        }
        return true
    }

    private fun scramblePieces() {
        pieces.shuffle()
        for ((index, piece) in pieces.withIndex()) {
            val row = index / numCols
            val col = index % numCols
            val left = dstRect.left + col * piece.destRect.width() + Random.nextInt(-100, 100)
            val top = dstRect.top + row * piece.destRect.height() + Random.nextInt(-100, 100)
            piece.destRect.set(left, top, left + piece.destRect.width(), top + piece.destRect.height())
        }
    }

    private fun startScramble() {
        handler.postDelayed({
            isScrambled = true
            scrambleStartTime = System.currentTimeMillis()
            invalidate()

            scramblePieces() // Final scramble after 2 seconds
        }, scrambleDuration)
    }
}