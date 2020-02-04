package com.emarsys.core.util.log.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InAppLoadingTime(val startTime: Long, val endTime: Long) : Parcelable