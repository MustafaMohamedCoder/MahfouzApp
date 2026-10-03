# تطبيق مَحْفُوظ (Mahfouz) 📱
### أرشيف فواتير ومشتريات المحل التجارية

تطبيق أندرويد أصلي (Native Android) مبني بأحدث التقنيات الرسمية من Google (**Kotlin** + **Jetpack Compose** + **Room Database**). صُمم خصيصاً لمساعدة أصحاب المحال والمتاجر في تنظيم وأرشفة فواتير المشتريات الورقية والإلكترونية وتصنيفها في مجموعات حسب المورد أو القسم.

---

## ✨ المميزات الرئيسية

1. **إدارة المجموعات (Groups & Categories):**
   - إنشاء مجموعات مخصصة (مثل: "موردو الألبان"، "بضاعة مجمدة"، "أدوات كهربائية").
   - تخصيص ألوان لكل مجموعة لسهولة التمييز البصري.
   - بطاقات إحصائية تعرض عدد الفواتير وإجمالي المبالغ المصروفة في كل مجموعة.

2. **أرشفة الفواتير وحفظ الصور (Invoices & Receipts):**
   - إمكانية التقاط صورة حية للفاتورة الورقية عبر كاميرا الهاتف أو اختيار صورة من المعرض.
   - حفظ بيانات المورد، رقم الفاتورة، التاريخ، والملاحظات.
   - تتبع حالة السداد: **مدفوع** أو **آجل / ديون**.

3. **تفاصيل الأصناف والبنود (Invoice Items):**
   - جدول ديناميكي داخل كل فاتورة لإضافة المنتجات المشتراة، الكمية، وسعر الوحدة.
   - حساب إجمالي الفاتورة تلقائياً عند تغيير أسعار وكميات الأصناف.

4. **البحث والفلترة (Search & Filters):**
   - بحث فوري وسريع برقم الفاتورة أو اسم المورد أو الشركة.

5. **تصميم عصري يدعم اللغة العربية بالكامل (RTL Support):**
   - واجهات Jetpack Compose بأسلوب Material 3 الحديث.
   - دعم الوضع الليلي والنهاري (Dark/Light Mode).

---

## 🛠 البنية التقنية (Tech Stack)

- **اللغة:** Kotlin
- **واجهة المستخدم:** Jetpack Compose (Material 3)
- **قاعدة البيانات المحلية:** Android Room ORM (SQLite) مع Coroutines & StateFlow
- **معالجة الصور:** Coil Compose مع FileProvider لتخزين الصور بأمان
- **التنقل:** Jetpack Compose Navigation
- **المعمارية:** MVVM + Repository Pattern

---

## 📂 هيكل المشروع

```text
MahfouzApp/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/mahfouz/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MahfouzApplication.kt
│   │   │   ├── data/
│   │   │   │   ├── local/ (Room Database, Entities, DAOs)
│   │   │   │   └── repository/ (InvoiceRepository)
│   │   │   └── ui/
│   │   │       ├── navigation/ (Screen, NavGraph)
│   │   │       ├── screens/ (Groups, Invoices, AddEdit, Detail)
│   │   │       └── theme/ (Colors, Type, Theme)
│   │   └── res/ (Strings, Colors, FileProvider XML)
│   └── build.gradle.kts
├── gradle/libs.versions.toml
├── settings.gradle.kts
└── build.gradle.kts
```

---

## 🚀 كيفية تشغيل المشروع

1. **عبر Android Studio (الأسهل والأفضل):**
   - انقل مجلد `MahfouzApp` إلى جهاز الكمبيوتر الخاص بك أو افتحه مباشرة.
   - افتح **Android Studio** واختر **Open** ثم حدد مجلد `MahfouzApp`.
   - انتظر مزامنة Gradle، ثم اضغط على زر **Run ▶** لتشغيله على المحاكي أو هاتفك.

2. **عبر سطر الأوامر (Gradle):**
   ```bash
   cd MahfouzApp
   ./gradlew assembleDebug
   ```
   سيتم توليد ملف الـ APK في مسار: `app/build/outputs/apk/debug/app-debug.apk`.
