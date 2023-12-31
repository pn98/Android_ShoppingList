@Database(entities = {Product.class}, version = 1, exportSchema = false)
public abstract class ProductDB extends RoomDatabase {
    public abstract ProductDao productDao();

    private static volatile ProductDB INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static ProductDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ProductDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ProductDB.class, "product_db")
                            .addCallback(pRoomDatabaseCallback)  // Add this line
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback pRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                ProductDao dao = INSTANCE.productDao();
                dao.deleteAll();
            });
        }
    };
}
