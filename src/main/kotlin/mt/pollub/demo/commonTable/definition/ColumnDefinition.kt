package mt.pollub.demo.commonTable.definition

class ColumnDefinition<T> private constructor(
    val key: String,
    val displayName: String,
    val path: String,
    val sortable: Boolean,
    val filterable: Boolean,
    val valueExtractor: (T) -> Any?
) {
    class Builder<T>(private val key: String) {
        private var displayName: String = key
        private var path: String = key
        private var sortable: Boolean = false
        private var filterable: Boolean = false
        private var valueExtractor: (T) -> Any? = { null }

        fun displayName(value: String): Builder<T> {
            displayName = value
            return this
        }

        fun path(value: String): Builder<T> {
            path = value
            return this
        }

        fun sortable(value: Boolean): Builder<T> {
            sortable = value
            return this
        }

        fun filterable(value: Boolean): Builder<T> {
            filterable = value
            return this
        }

        fun value(extractor: (T) -> Any?): Builder<T> {
            valueExtractor = extractor
            return this
        }

        fun build(): ColumnDefinition<T> = ColumnDefinition(
            key = key,
            displayName = displayName,
            path = path,
            sortable = sortable,
            filterable = filterable,
            valueExtractor = valueExtractor
        )
    }

    companion object {
        fun <T> builder(key: String): Builder<T> = Builder(key)
    }
}

