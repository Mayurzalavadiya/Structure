# Android Architecture

This is an Android starter project with MVVM architecture which you can use to bootstrap your application.

## Config

| SDK               | Version |
| ----------------- |---------|
| Min Sdk           | 23      |
| Compile Sdk       | 35      |
| Target Sdk        | 35      |

#### Kotlin & Gradle Versions

**1.** Gradle distributionUrl inside **gradle-wrapper.properties**

```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
```

**2.** Kotlin version inside project level **build.gradle** plugins

```
alias libs.plugins.kotlinAndroid apply false
```

## SDKs

**1. Dagger Hilt**

```
implementation "com.google.dagger:hilt-android:2.48"
kapt "com.google.dagger:hilt-compiler:2.48"
```

**2. Retrofit & OkHttp**

```
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'
```

## Other Updates
**1.** Preventing edge-to-edge by default is a new behavior in Android 15.

```
private fun handleEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= 35) {
            view?.let {
                ViewCompat.setOnApplyWindowInsetsListener(it) { view, windowInsets ->
                    val insetsSystemGestures =
                        windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
                    val insetsNavigationBars =
                        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

                    if (insetsSystemGestures.bottom == insetsNavigationBars.bottom) {
                        view.updatePadding(
                            insetsSystemGestures.left,
                            insetsSystemGestures.top,
                            insetsSystemGestures.right,
                            insetsSystemGestures.bottom
                        )
                    } else {
                        view.updatePadding(
                            0,
                            insetsSystemGestures.top,
                            0,
                            insetsSystemGestures.bottom
                        )
                    }
                    WindowInsetsCompat.CONSUMED
                }
            }
        }
    }
```

**2.** Deprecated the ```fun forResult(requestCode: Int): ActivityBuilder```, you can call the below fun with instance of
ActivityResultLauncher like below.

```
private var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
ActivityResultCallback { activityResult ->
    if (activityResult.resultCode == Activity.RESULT_OK) {
        //handle result
        activityResult.data?.let {
            showMessage(it.getStringExtra("my_data")?:"")
        }
    } else if (activityResult.resultCode == Activity.RESULT_CANCELED) {
        //cancelled
    }
})

navigator.loadActivity(IsolatedActivity::class.java, OrderFragment::class.java)
        .forResult(startForResult)
        .start()
```

**3.** Updated changes as per the deprecation of the RequestBody.create & ResponseBody.create methods of okhttp3 in AESCryptoInterceptor.

## Credits
Special thanks to [@rajeshk],[@kishan],[@jaydeepr],[@parthm],[@yaminm] for this update