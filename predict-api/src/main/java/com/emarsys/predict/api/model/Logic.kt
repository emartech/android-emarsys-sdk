package com.emarsys.predict.api.model

interface Logic {
    val logicName: String
    val data: Map<String, String>
    val variants: List<String>
}