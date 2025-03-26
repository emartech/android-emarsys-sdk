package com.emarsys.predict.api.model

import com.emarsys.core.Mockable

@Mockable
class RecommendationFilter {
    companion object {
        private const val IS = "IS"
        private const val IN = "IN"
        private const val HAS = "HAS"
        private const val OVERLAPS = "OVERLAPS"

        @JvmStatic
        fun include(field: String): Include {
            return Include.include(field)
        }

        @JvmStatic
        fun exclude(field: String): Exclude {
            return Exclude.exclude(field)
        }
    }

    lateinit var type: String
    lateinit var field: String
    lateinit var comparison: String
    lateinit var expectations: List<String?>

    internal constructor(
        type: String,
        field: String,
        comparison: String,
        expectations: List<String?>
    ) {
        this.type = type
        this.field = field
        this.comparison = comparison
        this.expectations = expectations
    }

    internal constructor(type: String, field: String, comparison: String, expectation: String) {
        this.type = type
        this.field = field
        this.comparison = comparison
        expectations = listOf(expectation)
    }

    class Exclude private constructor(val field: String) {

        companion object {
            private const val TYPE = "EXCLUDE"

            @JvmStatic
            fun exclude(field: String): Exclude {
                return Exclude(field)
            }
        }

        fun isValue(value: String): RecommendationFilter {
            return RecommendationFilter(TYPE, field, IS, value)
        }

        fun inValues(values: List<String>): RecommendationFilter {
            return RecommendationFilter(TYPE, field, IN, values)
        }

        fun hasValue(value: String): RecommendationFilter {
            return RecommendationFilter(TYPE, field, HAS, value)
        }

        fun overlapsValues(values: List<String>): RecommendationFilter {
            return RecommendationFilter(TYPE, field, OVERLAPS, values)
        }


    }

    class Include private constructor(val field: String) {
        companion object {
            private const val TYPE = "INCLUDE"

            @JvmStatic
            fun include(field: String): Include {
                return Include(field)
            }
        }

        fun isValue(value: String): RecommendationFilter {
            return RecommendationFilter(TYPE, field, IS, value)
        }

        fun inValues(values: List<String>): RecommendationFilter {
            return RecommendationFilter(TYPE, field, IN, values)
        }

        fun hasValue(value: String): RecommendationFilter {
            return RecommendationFilter(TYPE, field, HAS, value)
        }

        fun overlapsValues(values: List<String>): RecommendationFilter {
            return RecommendationFilter(TYPE, field, OVERLAPS, values)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecommendationFilter

        if (type != other.type) return false
        if (field != other.field) return false
        if (comparison != other.comparison) return false
        if (expectations != other.expectations) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + field.hashCode()
        result = 31 * result + comparison.hashCode()
        result = 31 * result + expectations.hashCode()
        return result
    }

}