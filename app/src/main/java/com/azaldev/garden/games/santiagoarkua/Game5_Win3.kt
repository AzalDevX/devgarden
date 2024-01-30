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
import com.azaldev.garden.LandingActivity
import com.azaldev.garden.R
import com.azaldev.garden.classes.dao.GameDao
import com.azaldev.garden.classes.database.AppDatabase
import com.azaldev.garden.globals.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random

class Piece @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var targetX: Float = 0f
    var targetY: Float = 0f

    var pieceWidth: Float = 0f
    var pieceHeight: Float = 0f

    init {
        // Customize the piece appearance if needed
    }
}

class Game5_Win3 : AppCompatActivity() {
    private lateinit var piecesContainer: LinearLayout
    private val SNAP_THRESHOLD = 100
    private val pieces = mutableListOf<Piece>()
    val game_id = 5;
    private lateinit var database : AppDatabase;
    private lateinit var gameDao : GameDao;
    private var screenWidth : Float = 0f// resources.displayMetrics.widthPixels.toFloat() / 2
    private var screenHeight : Float = 0f // resources.displayMetrics.heightPixels.toFloat() / 6

    private fun calculateDistance(piece: Piece, x: Float, y: Float): Float {
        val centerX = piece.targetX + piece.pieceWidth / 2
        val centerY = piece.targetY + piece.pieceHeight / 2
        return Math.sqrt((centerX - x).toDouble().pow(2) + (centerY - y).toDouble().pow(2)).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game5_win3)
        database = AppDatabase.getInstance(this)
        gameDao = database.GameDao();

        piecesContainer = findViewById(R.id.piecesContainer)

        screenWidth = resources.displayMetrics.widthPixels.toFloat() / 2
        screenHeight = resources.displayMetrics.heightPixels.toFloat() / 6

        createPiece(R.drawable.birdleft, 40f, 40f)
        createPiece(R.drawable.birdright, 80f, 300f)

        pieces.shuffle()

        for (i in pieces.indices) {
            pieces[i].x = screenWidth * Random.nextFloat()
            pieces[i].y = screenHeight
            piecesContainer.addView(pieces[i])
        }

        val gameTable = findViewById<FrameLayout>(R.id.gameTable)
        gameTable.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val piece = event.localState as Piece
                    piece.visibility = View.VISIBLE

                    val distance = calculateDistance(piece, event.x, event.y)

                    if (distance < SNAP_THRESHOLD) {
                        piece.x = piece.targetX
                        piece.y = piece.targetY
                        checkGameFinished()
                    } else {
                        piece.x = screenWidth * Random.nextFloat()
                        piece.y = screenHeight
                    }
                }
            }
            true
        }

    }

    private fun createPiece(drawableRes: Int, targetX: Float, targetY: Float) {
        val piece = Piece(this)
        piece.setImageResource(drawableRes)
        piece.targetX = targetX
        piece.targetY = targetY

        piece.x = screenWidth * Random.nextFloat()
        piece.y = screenHeight

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resources, drawableRes, options)
        piece.pieceWidth = options.outWidth.toFloat()
        piece.pieceHeight = options.outHeight.toFloat()

        piece.setOnLongClickListener { view ->
            val dragData = ClipData.newPlainText("", "")
            val shadowBuilder = View.DragShadowBuilder(view)
            view.startDragAndDrop(dragData, shadowBuilder, view, 0)
            view.visibility = View.INVISIBLE
            true
        }

        pieces.add(piece)
    }

    private fun checkGameFinished() {
        var allPiecesInPlace = true

        for (piece in pieces) {
            val distance = calculateDistance(piece, piece.targetX, piece.targetY)
            if (distance > SNAP_THRESHOLD) {
                allPiecesInPlace = false
                break
            }
        }

        if (allPiecesInPlace) {
            lifecycleScope.launch(Dispatchers.IO) {
                val current_game = gameDao.getGame(game_id);

                val next_game = current_game.getActivityProgress(1);
                gameDao.adv_progress(game_id, 1);

                lifecycleScope.launch(Dispatchers.Main) {
                    Log.i("devl|game53", "Moving to the next game")

                    if (next_game != null)
                        Utilities.startActivity(this@Game5_Win3, next_game)
                    else
                        Utilities.startActivity(this@Game5_Win3, LandingActivity::class.java)

                    finish()
                }
            }
        }
    }
}