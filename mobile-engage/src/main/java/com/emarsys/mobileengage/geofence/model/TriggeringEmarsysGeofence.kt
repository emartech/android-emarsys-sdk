package com.emarsys.mobileengage.geofence.model

import com.emarsys.mobileengage.api.geofence.TriggerType

data class TriggeringEmarsysGeofence(val geofenceId: String, val triggerType: TriggerType)