# فعال‌سازی AndroidX و Jetifier
android.useAndroidX=true
android.enableJetifier=true

# تنظیمات بهینه‌سازی Gradle
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.configureondemand=true

# غیرفعال‌سازی build cache برای جلوگیری از باگ‌های ناشناخته در CI
org.gradle.caching=false

# اختیاری: کاهش زمان build در GitHub Actions
kotlin.incremental=true
