package el.arn.opencheckers

class OpenCheckersApplication : android.app.Application() {

    companion object {
        var counter = 1;
    }

    override fun onCreate() {
        super.onCreate()

        counter += 4

    }

}