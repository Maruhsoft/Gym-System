package com.enjoyingfoss.feeel

import com.enjoyingfoss.feeel.data.Exercise
import com.enjoyingfoss.feeel.data.ExerciseMeta
import com.enjoyingfoss.feeel.data.Workout

/**
@author Miroslav Mazel
 */
class WorkoutRepository : WorkoutContract.Model {
    override fun retrieveWorkout(): Workout {
        return Workout(
                titleResource = R.string.workout_title_7minute,
                exerciseMetas = arrayOf(
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_jumpingjacks,
                                        descResource = R.string.exercise_desc_jumpingjacks,
                                        imageResource = R.drawable.exercise_jumpingjacks
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_wallsit,
                                        descResource = R.string.exercise_desc_wallsit,
                                        imageResource = R.drawable.exercise_wallsit
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_pushups,
                                        descResource = R.string.exercise_desc_pushups,
                                        imageResource = R.drawable.exercise_pushup
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_abcrunches,
                                        descResource = R.string.exercise_desc_abcrunches,
                                        imageResource = R.drawable.exercise_abcrunch
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_stepups,
                                        descResource = R.string.exercise_desc_stepups,
                                        imageResource = R.drawable.exercise_stepup
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_squats,
                                        descResource = R.string.exercise_desc_squats,
                                        imageResource = R.drawable.exercise_squat
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_tricepsdips,
                                        descResource = R.string.exercise_desc_tricepsdips,
                                        imageResource = R.drawable.exercise_tricepsdip
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_plank,
                                        descResource = R.string.exercise_desc_plank,
                                        imageResource = R.drawable.exercise_plank
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_highknees,
                                        descResource = R.string.exercise_desc_highknees,
                                        imageResource = R.drawable.exercise_highknees
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_lunges,
                                        descResource = R.string.exercise_desc_lunges,
                                        imageResource = R.drawable.exercise_lunge
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_pushuprotations,
                                        descResource = R.string.exercise_desc_pushuprotations,
                                        imageResource = R.drawable.exercise_pushuprotation
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_sideplank_l,
                                        descResource = R.string.exercise_desc_sideplank_l,
                                        imageResource = R.drawable.exercise_sideplank
                                ), 30),
                        ExerciseMeta(
                                Exercise(
                                        titleResource = R.string.exercise_title_sideplank_r,
                                        descResource = R.string.exercise_desc_sideplank_r,
                                        imageResource = R.drawable.exercise_sideplank
                                ), 30, isFlipped = true)
                ),
                breakLength = 10
        )
    }
}