package com.zoffcc.applications.trifa

class ToxVars {
    enum class TOX_LOG_LEVEL(var value: Int) {
        /**
         * Very detailed traces including all network activity.
         */
        TOX_LOG_LEVEL_TRACE(0),

        /**
         * Debug messages such as which port we bind to.
         */
        TOX_LOG_LEVEL_DEBUG(1),

        /**
         * Informational log messages such as video call status changes.
         */
        TOX_LOG_LEVEL_INFO(2),

        /**
         * Warnings about internal inconsistency or logic errors.
         */
        TOX_LOG_LEVEL_WARNING(3),

        /**
         * Severe unexpected errors caused by external or internal inconsistency.
         */
        TOX_LOG_LEVEL_ERROR(4);

        companion object {
            fun value_str(value: Int): String {
                if (value == TOX_LOG_LEVEL_TRACE.value) {
                    return "TOX_LOG_LEVEL_TRACE"
                } else if (value == TOX_LOG_LEVEL_DEBUG.value) {
                    return "TOX_LOG_LEVEL_DEBUG"
                } else if (value == TOX_LOG_LEVEL_INFO.value) {
                    return "TOX_LOG_LEVEL_INFO"
                } else if (value == TOX_LOG_LEVEL_WARNING.value) {
                    return "TOX_LOG_LEVEL_WARNING"
                } else if (value == TOX_LOG_LEVEL_ERROR.value) {
                    return "TOX_LOG_LEVEL_ERROR"
                }
                return "UNKNOWN"
            }
        }
    }
}
