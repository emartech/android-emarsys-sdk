package com.emarsys.predict.api.model

import java.util.Collections.emptyList


data class LogicData @JvmOverloads constructor(val logicData: Map<String, String>, val extensions: List<String> = emptyList())