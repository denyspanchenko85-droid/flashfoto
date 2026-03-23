class MainActivity : Activity() {
    private lateinit var settings: SettingsManager
    private lateinit var history: HistoryManager
    private val imageProcessor = ImageOverlayProcessor // твій існуючий об'єкт

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        settings = SettingsManager(this)
        history = HistoryManager(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)

        // Шестерня в кутку
        findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            // Відкрити діалог або нову Activity з налаштуваннями
        }

        // Кнопки Назад/Вперед біля картинки
        findViewById<Button>(R.id.btnPrev).setOnClickListener { showImage(history.getPrev()) }
        findViewById<Button>(R.id.btnNext).setOnClickListener { showImage(history.getNext()) }

        try {
            if (intent.action == Intent.ACTION_MAIN) {
                // Твій автозапуск камери
                checkPermissionAndCapture()
            }
        } catch (e: Exception) {
            Logger.log(this, "Crash in onCreate", e)
        }
    }

    private fun showImage(file: File?) {
        file?.let {
            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
            findViewById<ImageView>(R.id.resultImage).setImageBitmap(bitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            super.onActivityResult(requestCode, resultCode, data)
            // Твоя логіка обробки...
            
            history.updateHistory() // Оновили список після нового фото
            
            // ПЕРЕВІРКА ПРАПОРЦЯ АВТОДРУКУ
            if (settings.isAutoPrintEnabled) {
                doPrint()
            }
        } catch (e: Exception) {
            Logger.log(this, "Error processing photo", e)
        }
    }
}
