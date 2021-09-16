package com.emarsys.mobileengage.geofence

import com.emarsys.core.response.ResponseModel
import com.emarsys.mobileengage.api.geofence.Geofence
import com.emarsys.mobileengage.api.geofence.Trigger
import com.emarsys.mobileengage.api.geofence.TriggerType
import com.emarsys.mobileengage.geofence.model.GeofenceGroup
import com.emarsys.mobileengage.geofence.model.GeofenceResponse
import com.emarsys.testUtil.TimeoutUtils
import io.kotlintest.shouldBe
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GeofenceResponseMapperTest {

    private lateinit var mockResponseModel: ResponseModel
    private lateinit var mapper: GeofenceResponseMapper

    @Rule
    @JvmField
    val timeout: TestRule = TimeoutUtils.timeoutRule

    @Before
    fun setUp() {
        mapper = GeofenceResponseMapper()
        mockResponseModel = mock()
    }

    @Test
    fun testMap_whenResponseBodyIsEmpty() {
        val json = ""
        val expected = GeofenceResponse(emptyList())

        whenever(mockResponseModel.body).thenReturn(json)

        val result = mapper.map(mockResponseModel)

        result shouldBe expected
    }

    @Test
    fun testMap_whenResponseBodyContainsGeofence() {
        val responseBody = """
            {
               "refreshRadiusRatio":0.3,
               "groups":[
                  {
                     "id":"geoGroupId1",
                     "waitInterval":20.0,
                     "geofences":[
                        {
                           "id":"geofenceId1",
                           "lat":34.5,
                           "lon":12.789,
                           "r":5,
                           "waitInterval":30.0,
                           "triggers":[
                              {
                                "id": "triggerId1",
                                 "type":"ENTER",
                                 "action":{
                                    "id":"testActionId1",
                                    "title":"Custom event",
                                    "type":"MECustomEvent",
                                    "name":"nameValue",
                                    "payload":{
                                       "someKey":"someValue"
                                    }
                                 }
                              },
                              {
                                  "id": "triggerId2",
                                 "type":"ENTER",
                                 "loiteringDelay":13,
                                 "action":{
                                    "id":"testActionId2",
                                    "title":"Custom event",
                                    "type":"MECustomEvent",
                                    "name":"nameValue",
                                    "payload":{
                                       "someKey":"someValue"
                                    }
                                 }
                              }
                           ]
                        }
                     ]
                  }
               ]
            }"""

        whenever(mockResponseModel.body).thenReturn(responseBody)

        val trigger1 = Trigger("triggerId1", TriggerType.ENTER, action = JSONObject("""
                {
                    "id":"testActionId1",
                    "title":"Custom event",
                    "type":"MECustomEvent",
                    "name":"nameValue",
                    "payload":{
                        "someKey":"someValue"
                    }
                }"""))

        val trigger2 = Trigger("triggerId2", TriggerType.ENTER, 13, JSONObject("""
                 {
                        "id":"testActionId2",
                        "title":"Custom event",
                        "type":"MECustomEvent",
                        "name":"nameValue",
                        "payload":{
                           "someKey":"someValue"
                        }
                 }"""))

        val geoFence = Geofence("geofenceId1", 34.5, 12.789, 5.0, 30.0, listOf(trigger1, trigger2))

        val expected = GeofenceResponse(listOf(GeofenceGroup("geoGroupId1", 20.0, listOf(geoFence))), 0.3)

        val result = mapper.map(mockResponseModel)

        result.toString() shouldBe expected.toString()
    }

    @Test
    fun testMap_whenTriggerTypeIsInvalid() {
        val triggerJsonArray = JSONArray(
                """
                    [
                        {
                        "id": "triggerId1",
                         "type":"WRONG_TYPE",
                         "loiteringDelay":7,
                             "action":{
                                "id":"testActionId1",
                                "title":"Custom event",
                                "type":"MECustomEvent",
                                "name":"nameValue",
                                "payload":{
                                   "someKey":"someValue"
                                    }
                             }
                        },
                        {
                        "id": "triggerId2",
                         "type":"ENTER",
                         "loiteringDelay":13,
                             "action":{
                                "id":"testActionId2",
                                "title":"Custom event",
                                "type":"MECustomEvent",
                                "name":"nameValue",
                                "payload":{
                                   "someKey":"someValue"
                                }
                             }
                        }
                    ]""")

        val expected = listOf(
            Trigger("triggerId2", TriggerType.ENTER, 13, JSONObject("""
                 {
                    "id":"testActionId2",
                    "title":"Custom event",
                    "type":"MECustomEvent",
                    "name":"nameValue",
                    "payload":{
                           "someKey":"someValue"
                        }
                 }"""))
        )

        val result = mapper.extractTriggersFromJson(triggerJsonArray)

        result.joinToString() shouldBe expected.joinToString()
    }

    @Test
    fun testMap_whenTriggerTypeIsDwelling_and_loiteringDelayIsMissing() {
        val triggerJsonArray = JSONArray(
                """
                    [
                        {
                        "id": "triggerId1",
                         "type":"DWELLING",
                             "action":{
                                "id":"testActionId1",
                                "title":"Custom event",
                                "type":"MECustomEvent",
                                "name":"nameValue",
                                "payload":{
                                   "someKey":"someValue"
                                    }
                             }
                        }
                    ]""")

        val expected = emptyList<Trigger>()
        val result = mapper.extractTriggersFromJson(triggerJsonArray)

        result shouldBe expected
    }

    @Test
    fun testMap_groupIsNotCreated_ifItHasNoGeofences() {
        val responseBody = """
            {
                "refreshRadiusRatio":0.3,
                "groups":[
                {
                    "id":"geoGroupId1",
                    "waitInterval":20.0,
                    "geofences":[
                    ]
                }
                    ]
            }"""

        whenever(mockResponseModel.body).thenReturn(responseBody)

        val expected = GeofenceResponse(listOf(), 0.3)

        val result = mapper.map(mockResponseModel)

        result shouldBe expected
    }
}

