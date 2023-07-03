package com.enjoyingfoss.feeel.data

import android.os.Parcel
import android.os.Parcelable

/**
@author Miroslav Mazel
 */
class Workout(val titleResource: Int,
              val exerciseMetas: Array<ExerciseMeta>,
              val breakLength: Int) : Parcelable {
    val size: Int
        get() = exerciseMetas.size

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.createTypedArray(ExerciseMeta),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(titleResource)
        parcel.writeTypedArray(exerciseMetas, flags)
        parcel.writeInt(breakLength)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Workout> {
        override fun createFromParcel(parcel: Parcel): Workout {
            return Workout(parcel)
        }

        override fun newArray(size: Int): Array<Workout?> {
            return arrayOfNulls(size)
        }
    }
}