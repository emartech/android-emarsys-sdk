package com.emarsys.mobileengage.geofence

import android.app.Activity
import android.location.Location
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import java.util.concurrent.Executor

class FakeLocationTask(private val location: Location) : Task<Location>() {
    override fun isComplete(): Boolean {
        return true
    }

    override fun getException(): Exception? {
        return null
    }

    override fun addOnFailureListener(onFailureListener: OnFailureListener): Task<Location> {
        return this
    }

    override fun addOnFailureListener(executor: Executor, onFailureListener: OnFailureListener): Task<Location> {
        return this
    }

    override fun addOnFailureListener(activity: Activity, onFailureListener: OnFailureListener): Task<Location> {
        return this
    }

    override fun getResult(): Location? {
        return location
    }

    override fun <T : Throwable?> getResult(clazz: Class<T>): Location? {
        return location
    }

    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<in Location>): Task<Location> {
        onSuccessListener.onSuccess(location)
        return this
    }

    override fun addOnSuccessListener(executor: Executor, onSuccessListener: OnSuccessListener<in Location>): Task<Location> {
        return this
    }

    override fun addOnSuccessListener(activity: Activity, onSuccessListener: OnSuccessListener<in Location>): Task<Location> {
        return this
    }

    override fun isSuccessful(): Boolean {
        return true
    }

    override fun isCanceled(): Boolean {
        return false
    }
}