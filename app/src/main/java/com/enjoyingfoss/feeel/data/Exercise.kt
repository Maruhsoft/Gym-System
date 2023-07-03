package com.enjoyingfoss.feeel.data

import android.os.Parcel
import android.os.Parcelable

/**
@author Miroslav Mazel
 */
data class Exercise(val titleResource: Int, val imageResource: Int, val descResource: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(titleResource)
        parcel.writeInt(imageResource)
        parcel.writeInt(descResource)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Exercise> {
        override fun createFromParcel(parcel: Parcel): Exercise {
            return Exercise(parcel)
        }

        override fun newArray(size: Int): Array<Exercise?> {
            return arrayOfNulls(size)
        }
    }
}