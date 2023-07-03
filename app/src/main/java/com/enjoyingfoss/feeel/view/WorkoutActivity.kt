package com.enjoyingfoss.feeel.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.TypedValue
import android.view.View
import com.enjoyingfoss.feeel.R
import com.enjoyingfoss.feeel.WorkoutContract
import com.enjoyingfoss.feeel.data.ExerciseMeta
import com.enjoyingfoss.feeel.presenter.WorkoutService
import kotlinx.android.synthetic.main.activity_workout.*
import java.lang.ref.WeakReference


//todo license info
//todo separate activity without workout
//todo force media playback audio controls at all times on this activity
//todo use a pager, preload next exercises
//todo make sure that an empty description still covers the whole width


//TODO add view stuff from contract

class WorkoutActivity : AppCompatActivity(), ServiceConnection, WorkoutContract.View {
    //todo implement transition based on https://www.thedroidsonroids.com/blog/android/meaningful-motion-with-shared-element-transition-and-circular-reveal-animation/ or https://guides.codepath.com/android/Circular-Reveal-Animation

    //todo do a pager view, preload
    private var presenterService: WeakReference<WorkoutService>? = null
    private val STATE_KEY = "STATE"
    private val CONTROLS_KEY = "controls"
    private var controlsShown = false //todo not happy with this, seems hackish, only there to fix a bug with disappearing controls
    private var restoredState: Parcelable? = null
    private val grayscaleMatrix = ColorMatrix()
    private val grayscaleFilter: ColorMatrixColorFilter
    private var isImageFlipped = false

    init {
        grayscaleMatrix.setSaturation(0f)
        grayscaleFilter = ColorMatrixColorFilter(grayscaleMatrix)
    }

    //
    // Lifecycle
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        volumeControlStream = AudioManager.STREAM_MUSIC
        setContentView(R.layout.activity_workout)

        if (savedInstanceState != null) {
            restoredState = savedInstanceState.get(STATE_KEY) as Parcelable?

            controlsShown = savedInstanceState.get(CONTROLS_KEY) as Boolean
            if (controlsShown)
                showControls()
        }
    }

    override fun onStart() {
        super.onStart()

        bindService(
                Intent(this, WorkoutService::class.java),
                this, Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        presenterService?.get()?.disconnect(this)
        unbindService(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_KEY, presenterService?.get()?.getSavedState())
        outState.putBoolean(CONTROLS_KEY, controlsShown)
    }

    //
    // Service connection
    //

    override fun onServiceDisconnected(p0: ComponentName?) {
        presenterService = null
        //todo unset onclicklisteners needed?
    }

    override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
        presenterService = (binder as WorkoutService.WorkoutBinder).service

        binder.service.get()!!.setView(callback = this, savedState = restoredState)
        //todo binder.service.get()?.enableAudio() //todo present actual UI option for this
        setOnClickListeners(binder.service.get())
    }

    private fun setOnClickListeners(service: WorkoutService?) {
        runOnUiThread {
            headerBox.setOnClickListener {
                //todo see if this covers the overlaid text as well —— I don't think it does, but it should
                service?.togglePlayPause()
            }

            imageView.setOnClickListener {
                service?.togglePlayPause()
            }

            playPauseButton.setOnClickListener {
                service?.togglePlayPause()
            }

            previousButton.setOnClickListener {
                service?.skipToPreviousExercise()
            }

            nextButton.setOnClickListener {
                //todo enable swiping through pages to get to next and prev. exercise, too, though only in this mode
                service?.skipToNextExercise()
            }
        }
    }

    //
    // WorkoutContract.View
    //
    override fun setExercise(exerciseMeta: ExerciseMeta) { //todo rejig architecture, I don't like checking for stage all the timeZ
        runOnUiThread {
            titleButton.text = getString(exerciseMeta.exercise.titleResource)
            descriptionText.text =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        Html.fromHtml(getString(exerciseMeta.exercise.descResource), Html.FROM_HTML_MODE_LEGACY)
                    else
                        Html.fromHtml(getString(exerciseMeta.exercise.descResource))
            imageView.setImageResource(exerciseMeta.exercise.imageResource)

            if (exerciseMeta.isFlipped && !isImageFlipped) {
                imageView.scaleX = -1f
            } else if (!exerciseMeta.isFlipped) { //todo check if this is performant and covers all options
                imageView.scaleX = 1f
            }

            imageView.colorFilter = null
        }
    }

    override fun setBreak(nextExerciseMeta: ExerciseMeta) {
        print("setting break")

        val nextExercise = nextExerciseMeta.exercise
        runOnUiThread {

            titleButton.text = String.format(
                    getString(R.string.next_label),
                    getString(nextExercise.titleResource)
            )
            descriptionText.text =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        Html.fromHtml(getString(nextExercise.descResource), Html.FROM_HTML_MODE_LEGACY)
                    else
                        Html.fromHtml(getString(nextExercise.descResource))
            imageView.setImageResource(nextExercise.imageResource)

            imageView.colorFilter = grayscaleFilter
        }
    }

    override fun finishWorkout() {
        finish()
    }

    override fun setSeconds(seconds: Int) = runOnUiThread {
        timeText.text = seconds.toString()
    }

    override fun setPlaying() {
        controlsShown = false
        runOnUiThread {
            playPauseButton.visibility = View.GONE
            previousButton.visibility = View.GONE
            nextButton.visibility = View.GONE
            tapIndicator.visibility = View.VISIBLE
            timeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.time_headline))

            if (Build.VERSION.SDK_INT >= 16)
                imageView.imageAlpha = 255
            else
                imageView.setAlpha(255)
        }
    }

    override fun setPaused() { //todo visibility failing on rotate sometime! I guess setPaused isn't called sometime!!!
        controlsShown = true

        showControls()
        runOnUiThread {
            tapIndicator.visibility = View.GONE
            timeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.time_headline_small))

            if (Build.VERSION.SDK_INT >= 16)
                imageView.imageAlpha = 128
            else
                imageView.setAlpha(128)
        }
    }

    fun showControls() {
        runOnUiThread {
            playPauseButton.visibility = View.VISIBLE
            previousButton.visibility = View.VISIBLE
            nextButton.visibility = View.VISIBLE
        }
    }

    //
    // Custom display
    //

    fun toggleDescription(view: View) {
        runOnUiThread {
            if (descriptionFrame.visibility == View.GONE) {
                descriptionFrame.visibility = View.VISIBLE
                titleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_collapse_down, 0)
            } else {
                descriptionFrame.visibility = View.GONE
                titleButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_up, 0)
            }
        }
    }
}