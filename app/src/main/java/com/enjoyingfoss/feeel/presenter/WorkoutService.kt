package com.enjoyingfoss.feeel.presenter

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import com.enjoyingfoss.feeel.WorkoutContract
import com.enjoyingfoss.feeel.WorkoutRepository
import com.enjoyingfoss.feeel.data.ExerciseMeta
import com.enjoyingfoss.feeel.data.Workout
import com.enjoyingfoss.feeel.view.WorkoutAudio
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
@author Miroslav Mazel
 */

//todo

//todo render state along with countdown immediately (esp. when going through exercises)
class WorkoutService : Service(), WorkoutContract.Presenter {

    enum class ExerciseStage { PREPARING, EXERCISE, BREAK } //todo check that proguard handles enums effectively

    private var views = ArrayList<WorkoutContract.View>(2)
    private val exerciseRetriever = WorkoutRepository()
    private val exerciseExecutor = Executors.newScheduledThreadPool(1)
    private var audioView: WorkoutAudio? = null

    private var state = InternalState(workout = exerciseRetriever.retrieveWorkout())
    private var future: Future<*>? = null

    //
    // InternalState class
    //

    private class InternalState(val workout: Workout,
                                var exercisePos: Int = -1,
                                var timeRemaining: Int = 0,
                                var stage: ExerciseStage = ExerciseStage.PREPARING,
                                var isTimerRunning: Boolean = false,
                                var isAudioOn: Boolean = true) : Parcelable {
        val curExerciseMeta: ExerciseMeta?
            get() = workout.exerciseMetas.getOrNull(exercisePos)

        val curExerciseLength: Int
            get() = workout.exerciseMetas.getOrNull(exercisePos)?.duration ?: 0

        val isFirstExercise: Boolean
            get() = exercisePos == 0

        val isLastExercise: Boolean
            get() = exercisePos == workout.size - 1

        val hasNoExercise: Boolean
            get() = workout.size == 0

        constructor(parcel: Parcel) : this(
                parcel.readParcelable(Workout::class.java.classLoader),
                parcel.readInt(),
                parcel.readInt(),
                ExerciseStage.values()[parcel.readInt()],
                parcel.readByte() != 0.toByte(),
                parcel.readByte() != 0.toByte())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(workout, flags)
            parcel.writeInt(exercisePos)
            parcel.writeInt(timeRemaining)
            parcel.writeInt(stage.ordinal)
            parcel.writeByte(if (isTimerRunning) 1 else 0)
            parcel.writeByte(if (isAudioOn) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<InternalState> {
            override fun createFromParcel(parcel: Parcel): InternalState {
                return InternalState(parcel)
            }

            override fun newArray(size: Int): Array<InternalState?> {
                return arrayOfNulls(size)
            }
        }
    }

    //
    // Lifecycle
    //

    inner class WorkoutBinder : Binder() {
        val service: WeakReference<WorkoutService>
            get() = WeakReference(this@WorkoutService)
    }

    override fun onBind(intent: Intent): IBinder = WorkoutBinder()

    override fun onDestroy() {
        super.onDestroy()
        audioView?.shutdown()
        exerciseExecutor.shutdown() //todo see if necessary
        views.clear()
    }

    private fun startService() {
        enableAudio()
        setupNext()
    }

    private fun restoreService(restoredState: InternalState) {
        println("restored!")
        state = restoredState

        if (state.isAudioOn) enableAudio() //todo does this belong here?
        else disableAudio()

        if (state.isTimerRunning) startTimer()

        rerender()
    }

    //
    // Stage switching
    //

    private fun setupNext() { //todo make a test for switching states correctly
        when (state.stage) {
            ExerciseStage.PREPARING -> {
                if (state.hasNoExercise) finish() //todo should show a dialog, at least, or something
                else start()
            }

            ExerciseStage.BREAK -> startExerciseStage()

            ExerciseStage.EXERCISE -> {
                if (state.isLastExercise) finish()
                else startBreakStage()
            }
        }

        renderStage()
        renderPausePlay()
    }

    private fun start() {
        startTimer()
        startBreakStage()
    }

    private fun startExerciseStage() {
        state.stage = ExerciseStage.EXERCISE
        state.timeRemaining = state.curExerciseLength
    }

    private fun startBreakStage() { //todo say what to prepare (e.g. chair, mat, sthing else)!
        state.exercisePos++
        state.stage = ExerciseStage.BREAK
        state.timeRemaining = state.workout.breakLength
    }

    private fun finish() { //todo close service
        for (view in views) {
            view.finishWorkout()
        }
        stopTimer()
        disableAudio()
        stopSelf()
    }

    //
    // View render
    //

    private fun rerender() {
        renderStage()
        renderPausePlay()
        for (view in views) view.setSeconds(state.timeRemaining)
    }

    private fun renderStage() {
        when (state.stage) {
            ExerciseStage.EXERCISE -> for (view in views) view.setExercise(state.curExerciseMeta!!) //todo double-check !! assertion
            ExerciseStage.BREAK -> for (view in views) view.setBreak(state.curExerciseMeta!!)
            ExerciseStage.PREPARING -> {}
        }
    }

    private fun renderPausePlay() {
        print("rendering pause play")
        if (state.isTimerRunning) for (view in views) view.setPlaying()
        else for (view in views) view.setPaused()
    }

    //
    // Timer
    //
    private val secondCounter = Runnable {
        //todo is a try catch needed here?
        countSecond()
    }

    private fun countSecond() {
        if (state.timeRemaining <= 0) {
            setupNext()
        }

        for (view in views) view.setSeconds(state.timeRemaining)

        state.timeRemaining -= 1
    }

    private fun startTimer() {
        if (future == null) { //todo double-check if using futures right
            state.isTimerRunning = true
            future = exerciseExecutor.scheduleAtFixedRate(secondCounter, 0, 1, TimeUnit.SECONDS)
        }
    }

    private fun stopTimer() {
        future?.cancel(true) //todo double-check if using futures right
        future = null
        state.isTimerRunning = false
    }

    //
    // WorkoutContract.Presenter
    //
    /* override fun changeWorkout(workout: Workout) {
        this.state = InternalState(workout = workout)
    } */

    override fun setView(callback: WorkoutContract.View, savedState: Parcelable?) {
        views.add(callback)

        println("setting view")

        when {
            state.stage != ExerciseStage.PREPARING -> rerender()
            savedState != null -> restoreService(savedState as InternalState)
            else -> startService()
        }
    }

    override fun disconnect(view: WorkoutContract.View) {
        //todo disconnect audio IF no view or notification?
        views.remove(view)
    }

    override fun getSavedState(): Parcelable = state

    override fun togglePlayPause() {
        if (state.isTimerRunning) {
            stopTimer()
        } else {
            startTimer()
        }
        renderPausePlay()
    }

    override fun skipToPreviousExercise() {
        state.timeRemaining = state.curExerciseLength //todo show a reset icon for this special case?

        if (!state.isFirstExercise) {
            state.exercisePos--

            if (state.stage != ExerciseStage.EXERCISE) startExerciseStage()

            renderStage()
        }
    }

    override fun skipToNextExercise() {
        if (!state.isLastExercise) {
            state.timeRemaining = state.curExerciseLength
            state.exercisePos++

            if (state.stage != ExerciseStage.EXERCISE) startExerciseStage()

            renderStage()
        }
    }

    private fun isAudioEnabled() = audioView != null

    private fun enableAudio() {
        if (!isAudioEnabled()) {
            val workoutAudio = WorkoutAudio(WeakReference(this))
            views.add(workoutAudio)
            audioView = workoutAudio
            state.isAudioOn = true
        }
    }

    private fun disableAudio() {
        if (isAudioEnabled()) {
            views.remove(audioView!!)
            audioView?.shutdown()
            audioView = null
            state.isAudioOn = false
        }
    }
}