package com.enjoyingfoss.feeel

import android.os.Parcelable
import com.enjoyingfoss.feeel.data.ExerciseMeta
import com.enjoyingfoss.feeel.data.Workout

/**
@author Miroslav Mazel
 */
interface WorkoutContract {
    interface Presenter {
        fun setView(callback: View, savedState: Parcelable?)
        fun getSavedState(): Parcelable
        fun disconnect(view: WorkoutContract.View) //todo test that this is always called; OR use weakReference instead

        fun skipToPreviousExercise()
        fun skipToNextExercise()

        fun togglePlayPause()
    }

    interface View {
        fun setExercise(exerciseMeta: ExerciseMeta)
        fun setBreak(nextExerciseMeta: ExerciseMeta)
        fun finishWorkout()

        fun setSeconds(seconds: Int) // sets seconds for countdown, both before and during exercise

        fun setPaused()
        fun setPlaying()
    }

    interface Model {
        fun retrieveWorkout(): Workout
    }
}