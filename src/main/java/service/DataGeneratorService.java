package service;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DataGeneratorService {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    // 生成状態管理
    private volatile boolean isGenerating = false;
    private volatile int progress = 0;
    private volatile String currentTask = "";

    // ダミーデータ用リスト
    private static final String[] FIRST_NAMES = {
            "John", "Jane", "Michael", "Emily", "David", "Sarah", "James", "Emma",
            "Robert", "Olivia", "William", "Sophia", "Joseph", "Isabella", "Thomas",
            "Mia", "Charles", "Charlotte", "Daniel", "Amelia", "Taro", "Hanako",
            "Yuki", "Kenji", "Sakura", "Takeshi", "Yui", "Hiroshi", "Aoi", "Ren"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
            "Wilson", "Anderson", "Tanaka", "Yamada", "Sato", "Suzuki", "Takahashi",
            "Watanabe", "Ito", "Nakamura", "Kobayashi", "Kato"
    };

    private static final String[] CITIES = {
            "Tokyo", "Osaka", "New York", "London", "Paris", "Berlin", "Sydney",
            "Singapore", "Hong Kong", "Seoul", "Bangkok", "Mumbai", "Dubai",
            "Los Angeles", "Chicago", "Toronto", "Vancouver", "Amsterdam"
    };

    private static final String[] COUNTRIES = {
            "Japan", "USA", "UK", "Germany", "France", "Australia", "Canada",
            "Singapore", "China", "South Korea", "Thailand", "India", "UAE"
    };

    private static final String[] CATEGORIES = {
            "Electronics", "Clothing", "Home & Garden", "Sports", "Books",
            "Toys", "Food", "Beauty", "Automotive", "Health"
    };

    private static final String[] SUBCATEGORIES = {
            "Smartphones", "Laptops", "T-Shirts", "Dresses", "Furniture",
            "Kitchen", "Running", "Swimming", "Fiction", "Non-Fiction",
            "Board Games", "Snacks", "Skincare", "Car Parts", "Vitamins"
    };

    private static final String[] BRANDS = {
            "TechCorp", "FashionBrand", "HomeStyle", "SportMax", "BookWorld",
            "ToyLand", "FoodCo", "BeautyPlus", "AutoParts", "HealthFirst"
    };

    private static final String[] PAYMENT_METHODS = {
            "CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER", "CASH"
    };

    public DataGeneratorService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public GenerationStatus getStatus() {
        return new GenerationStatus(isGenerating, progress, currentTask);
    }

    public synchronized void generateAllData(int customerCount, int productCount,
                                             int orderCount, int itemsPerOrder) {
        if (isGenerating) {
            throw new IllegalStateException("Data generation is already in progress");
        }

        isGenerating = true;
        progress = 0;

        try {
            // 既存データをクリア
            currentTask = "Clearing existing data...";
            clearExistingData();
            progress = 5;

            // Customers生成
            currentTask = "Generating customers...";
            generateCustomers(customerCount);
            progress = 25;

            // Products生成
            currentTask = "Generating products...";
            generateProducts(productCount);
            progress = 40;

            // Orders生成
            currentTask = "Generating orders...";
            generateOrders(orderCount, customerCount);
            progress = 70;

            // OrderItems生成
            currentTask = "Generating order items...";
            generateOrderItems(orderCount, productCount, itemsPerOrder);
            progress = 100;

            currentTask = "Completed!";

        } finally {
            isGenerating = false;
        }
    }

    private void clearExistingData() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE sample_order_items");
        jdbcTemplate.execute("TRUNCATE TABLE sample_orders");
        jdbcTemplate.execute("TRUNCATE TABLE sample_products");
        jdbcTemplate.execute("TRUNCATE TABLE sample_customers");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private void generateCustomers(int count) {
        int batchSize = 5000;
        String sql = "INSERT INTO sample_customers (first_name, last_name, email, phone, " +
                "address, city, country, postal_code, registration_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (int i = 0; i < count; i += batchSize) {
            int currentBatchSize = Math.min(batchSize, count - i);
            int batchStart = i;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int j) throws SQLException {
                    int index = batchStart + j;
                    String firstName = randomElement(FIRST_NAMES);
                    String lastName = randomElement(LAST_NAMES);

                    ps.setString(1, firstName);
                    ps.setString(2, lastName);
                    ps.setString(3, firstName.toLowerCase() + "." + lastName.toLowerCase() + index + "@example.com");
                    ps.setString(4, generatePhone());
                    ps.setString(5, randomInt(1, 9999) + " " + randomElement(LAST_NAMES) + " Street");
                    ps.setString(6, randomElement(CITIES));
                    ps.setString(7, randomElement(COUNTRIES));
                    ps.setString(8, String.format("%05d", randomInt(10000, 99999)));
                    ps.setDate(9, Date.valueOf(randomDate(2018, 2024)));
                    ps.setString(10, randomElement(new String[]{"ACTIVE", "ACTIVE", "ACTIVE", "INACTIVE", "SUSPENDED"}));
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });

            // 進捗更新
            progress = 5 + (int) ((i + currentBatchSize) / (double) count * 20);
        }
    }

    private void generateProducts(int count) {
        int batchSize = 5000;
        String sql = "INSERT INTO sample_products (name, description, category, subcategory, " +
                "price, cost, stock_quantity, sku, brand, weight, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (int i = 0; i < count; i += batchSize) {
            int currentBatchSize = Math.min(batchSize, count - i);
            int batchStart = i;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int j) throws SQLException {
                    int index = batchStart + j;
                    String category = randomElement(CATEGORIES);
                    String brand = randomElement(BRANDS);
                    BigDecimal price = BigDecimal.valueOf(randomInt(100, 100000) / 100.0)
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal cost = price.multiply(BigDecimal.valueOf(0.6))
                            .setScale(2, RoundingMode.HALF_UP);

                    ps.setString(1, brand + " " + category + " Product " + index);
                    ps.setString(2, "High quality " + category.toLowerCase() + " product from " + brand);
                    ps.setString(3, category);
                    ps.setString(4, randomElement(SUBCATEGORIES));
                    ps.setBigDecimal(5, price);
                    ps.setBigDecimal(6, cost);
                    ps.setInt(7, randomInt(0, 10000));
                    ps.setString(8, "SKU-" + String.format("%08d", index));
                    ps.setString(9, brand);
                    ps.setBigDecimal(10, BigDecimal.valueOf(randomInt(10, 10000) / 100.0));
                    ps.setBoolean(11, random.nextDouble() > 0.1);
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });

            progress = 25 + (int) ((i + currentBatchSize) / (double) count * 15);
        }
    }

    private void generateOrders(int count, int customerCount) {
        int batchSize = 5000;
        String sql = "INSERT INTO sample_orders (customer_id, order_date, order_status, " +
                "total_amount, tax_amount, shipping_amount, discount_amount, payment_method, " +
                "shipping_address, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String[] statuses = {"PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "DELIVERED",
                "DELIVERED", "CANCELLED", "REFUNDED"};

        for (int i = 0; i < count; i += batchSize) {
            int currentBatchSize = Math.min(batchSize, count - i);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int j) throws SQLException {
                    BigDecimal total = BigDecimal.valueOf(randomInt(1000, 500000) / 100.0)
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal tax = total.multiply(BigDecimal.valueOf(0.1))
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal shipping = BigDecimal.valueOf(randomInt(0, 2000) / 100.0)
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal discount = total.multiply(BigDecimal.valueOf(random.nextDouble() * 0.2))
                            .setScale(2, RoundingMode.HALF_UP);

                    ps.setLong(1, randomInt(1, customerCount));
                    ps.setDate(2, Date.valueOf(randomDate(2020, 2024)));
                    ps.setString(3, randomElement(statuses));
                    ps.setBigDecimal(4, total);
                    ps.setBigDecimal(5, tax);
                    ps.setBigDecimal(6, shipping);
                    ps.setBigDecimal(7, discount);
                    ps.setString(8, randomElement(PAYMENT_METHODS));
                    ps.setString(9, randomInt(1, 9999) + " " + randomElement(LAST_NAMES) + " St, " + randomElement(CITIES));
                    ps.setString(10, random.nextDouble() > 0.8 ? "Please handle with care" : null);
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });

            progress = 40 + (int) ((i + currentBatchSize) / (double) count * 30);
        }
    }

    private void generateOrderItems(int orderCount, int productCount, int avgItemsPerOrder) {
        int batchSize = 10000;
        String sql = "INSERT INTO sample_order_items (order_id, product_id, quantity, " +
                "unit_price, discount_percent, subtotal) VALUES (?, ?, ?, ?, ?, ?)";

        // 各注文に対してランダムな数のアイテムを生成
        List<Object[]> items = new ArrayList<>();

        for (int orderId = 1; orderId <= orderCount; orderId++) {
            int itemCount = randomInt(1, avgItemsPerOrder * 2);
            for (int i = 0; i < itemCount; i++) {
                int quantity = randomInt(1, 5);
                BigDecimal unitPrice = BigDecimal.valueOf(randomInt(100, 50000) / 100.0)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal discountPercent = BigDecimal.valueOf(randomInt(0, 20))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity))
                        .multiply(BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)))
                        .setScale(2, RoundingMode.HALF_UP);

                items.add(new Object[]{
                        orderId,
                        randomInt(1, productCount),
                        quantity,
                        unitPrice,
                        discountPercent,
                        subtotal
                });
            }

            // バッチ実行
            if (items.size() >= batchSize || orderId == orderCount) {
                List<Object[]> batch = new ArrayList<>(items);
                items.clear();

                jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Object[] item = batch.get(i);
                        ps.setInt(1, (Integer) item[0]);
                        ps.setInt(2, (Integer) item[1]);
                        ps.setInt(3, (Integer) item[2]);
                        ps.setBigDecimal(4, (BigDecimal) item[3]);
                        ps.setBigDecimal(5, (BigDecimal) item[4]);
                        ps.setBigDecimal(6, (BigDecimal) item[5]);
                    }

                    @Override
                    public int getBatchSize() {
                        return batch.size();
                    }
                });
            }

            // 進捗更新（100注文ごと）
            if (orderId % 1000 == 0) {
                progress = 70 + (int) (orderId / (double) orderCount * 30);
            }
        }
    }

    private String randomElement(String[] array) {
        return array[random.nextInt(array.length)];
    }

    private int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private LocalDate randomDate(int startYear, int endYear) {
        long startEpoch = LocalDate.of(startYear, 1, 1).toEpochDay();
        long endEpoch = LocalDate.of(endYear, 12, 31).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDate.ofEpochDay(randomDay);
    }

    private String generatePhone() {
        return String.format("+1-%03d-%03d-%04d",
                randomInt(100, 999), randomInt(100, 999), randomInt(1000, 9999));
    }

    public static class GenerationStatus {
        private final boolean isGenerating;
        private final int progress;
        private final String currentTask;

        public GenerationStatus(boolean isGenerating, int progress, String currentTask) {
            this.isGenerating = isGenerating;
            this.progress = progress;
            this.currentTask = currentTask;
        }

        public boolean isGenerating() { return isGenerating; }
        public int getProgress() { return progress; }
        public String getCurrentTask() { return currentTask; }
    }
}
