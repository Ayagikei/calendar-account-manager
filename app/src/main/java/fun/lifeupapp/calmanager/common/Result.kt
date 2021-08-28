package `fun`.lifeupapp.calmanager.common


sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Error(val throwable: Throwable) : Resource<Nothing>()
    data class Success<T>(val item: T) : Resource<T>()

    companion object{
        fun loading() = Loading

        fun error(throwable: Throwable) = Error(throwable)

        fun <T> success(item: T) = Success(item)

        fun Resource<Any>.isError(): Boolean{
           return this is Resource.Error
        }
    }
}