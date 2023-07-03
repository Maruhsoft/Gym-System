package com.enjoyingfoss.feeel.data

import android.os.Parcel
import android.os.Parcelable

/**
@author Miroslav Mazel
 */
class ExerciseMeta(val exercise: Exercise, val duration: Int, val isFlipped: Boolean = false) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(Exercise::class.java.classLoader),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(exercise, flags)
        parcel.writeInt(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExerciseMeta> {
        override fun createFromParcel(parcel: Parcel): ExerciseMeta {
            return ExerciseMeta(parcel)
        }

        override fun newArray(size: Int): Array<ExerciseMeta?> {
            return arrayOfNulls(size)
        }
    }
}