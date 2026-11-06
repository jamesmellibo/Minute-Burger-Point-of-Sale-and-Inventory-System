package minute_burger_pos_and_inventory_system;

import minute_burger_inventory_system.DatabaseConnection;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SharedData {
    // Session User Details
    public static String currentUserName = "";
    public static String currentUserFullName = "";
    public static String currentUserRole = "";
    public static String currentUserPhone = "";

    // Order Details for Sales / Reports
    public static String orderId = "";
    public static String items = "";
    public static String total = "";
    public static String server = "";
    public static String time = "";
    public static boolean hasNewOrder = false;

    // Transaction History List
    public static ArrayList<String[]> allOrders = new ArrayList<>();

    // Map for Inventory Stocks from Database
    public static HashMap<String, Integer> ingredientStocks = new HashMap<>();

    // 0. INITIALIZE OR FETCH STOCKS FROM DATABASE
    public static void initStocksIfNeeded() {
        ingredientStocks.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "SELECT ingredient_name, quantity FROM inventory";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String name = rs.getString("ingredient_name").trim().toUpperCase();
                int qty = rs.getInt("quantity");
                ingredientStocks.put(name, qty);
            }
            rs.close();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. GET STOCK FROM MYSQL DATABASE
    public static int getDatabaseStock(String itemName) {
        if (itemName == null) return 0;
        String key = itemName.trim().toUpperCase();
        if (ingredientStocks.containsKey(key)) {
            return ingredientStocks.get(key);
        }
       
        int stock = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "SELECT quantity FROM inventory WHERE UPPER(TRIM(ingredient_name)) = UPPER(?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, key);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                stock = rs.getInt("quantity");
            }
            rs.close();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stock;
    }

    // 2. CHECK AND RETURN SPECIFIC MISSING INGREDIENTS FOR A MENU ITEM
    public static String getMissingIngredients(String menuItemName, int qty) {
        if (menuItemName == null) return "Unknown Item";
        String name = menuItemName.trim().toUpperCase();
        List<String> missingList = new ArrayList<>();

        switch (name) {
            case "DOUBLE MINUTE BURGER":
                if (getDatabaseStock("Burger Bun") < qty * 2) missingList.add("Burger Bun");
                if (getDatabaseStock("Beef Patty") < qty * 2) missingList.add("Beef Patty");
                if (getDatabaseStock("Dressing / Mayo") < qty) missingList.add("Dressing / Mayo");
                break;

            case "MINUTE BURGER":
                if (getDatabaseStock("Burger Bun") < qty * 2) missingList.add("Burger Bun");
                if (getDatabaseStock("Beef Patty") < qty) missingList.add("Beef Patty");
                if (getDatabaseStock("Dressing / Mayo") < qty) missingList.add("Dressing / Mayo");
                break;

            case "BACON CHEESE BURGER":
            case "BACON CHEASE BURGER":
            case "BACON PIZZA BURGER":
                if (getDatabaseStock("Burger Bun") < qty * 2) missingList.add("Burger Bun");
                if (getDatabaseStock("Beef Patty") < qty * 2) missingList.add("Beef Patty");
                if (getDatabaseStock("Pizza Sauce") < qty) missingList.add("Pizza Sauce");
                if (getDatabaseStock("Cheese Slice") < qty) missingList.add("Cheese Slice");
                if (getDatabaseStock("Bacon Strips") < qty) missingList.add("Bacon Strips");
                break;

            case "ROASTED SESAME CHICKEN BURGER":
                if (getDatabaseStock("Burger Bun") < qty * 2) missingList.add("Burger Bun");
                if (getDatabaseStock("Chicken Patty") < qty * 2) missingList.add("Chicken Patty");
                if (getDatabaseStock("Sesame Onion Sauce") < qty) missingList.add("Sesame Onion Sauce");
                break;

            case "CHICKEN SISIG BURGER":
                if (getDatabaseStock("Burger Bun") < qty * 2) missingList.add("Burger Bun");
                if (getDatabaseStock("Chicken Patty") < qty * 2) missingList.add("Chicken Patty");
                if (getDatabaseStock("Chimichurri & Mushroom") < qty) missingList.add("Chimichurri & Mushroom");
                break;

            case "CHICKEN TIME BURGER":
                if (getDatabaseStock("Burger Bun") < qty * 2) missingList.add("Burger Bun");
                if (getDatabaseStock("Chicken Patty") < qty * 2) missingList.add("Chicken Patty");
                if (getDatabaseStock("Creamy Dressing") < qty) missingList.add("Creamy Dressing");
                break;

            case "DOUBLE CHICKEN TIME + DRINK":
                if (getDatabaseStock("Burger Bun") < qty * 4) missingList.add("Burger Bun");
                if (getDatabaseStock("Chicken Patty") < qty * 4) missingList.add("Chicken Patty");
                if (getDatabaseStock("Creamy Dressing") < qty * 2) missingList.add("Creamy Dressing");
                if (getDatabaseStock("Juice / Beverage Mix") < qty) missingList.add("Juice / Beverage Mix");
                if (getDatabaseStock("Drink Cup") < qty) missingList.add("Drink Cup");
                if (getDatabaseStock("Straw") < qty) missingList.add("Straw");
                break;

            case "CHICKEN TIME + DRINK":
                if (getDatabaseStock("Burger Bun") < qty * 2) missingList.add("Burger Bun");
                if (getDatabaseStock("Chicken Patty") < qty * 2) missingList.add("Chicken Patty");
                if (getDatabaseStock("Creamy Dressing") < qty) missingList.add("Creamy Dressing");
                if (getDatabaseStock("Juice / Beverage Mix") < qty) missingList.add("Juice / Beverage Mix");
                if (getDatabaseStock("Drink Cup") < qty) missingList.add("Drink Cup");
                if (getDatabaseStock("Straw") < qty) missingList.add("Straw");
                break;

            case "FRENCH ONION FRANKS":
                if (getDatabaseStock("Hotdog Bun") < qty * 2) missingList.add("Hotdog Bun");
                if (getDatabaseStock("Hotdog Frank") < qty * 2) missingList.add("Hotdog Frank");
                if (getDatabaseStock("Sesame Onion Sauce") < qty) missingList.add("Sesame Onion Sauce");
                break;

            case "CHILI CON CARNE CHEESE FRANKS":
                if (getDatabaseStock("Hotdog Bun") < qty * 2) missingList.add("Hotdog Bun");
                if (getDatabaseStock("Hotdog Frank") < qty * 2) missingList.add("Hotdog Frank");
                if (getDatabaseStock("Chili Con Carne Sauce") < qty) missingList.add("Chili Con Carne Sauce");
                if (getDatabaseStock("Cheese Sauce") < qty) missingList.add("Cheese Sauce");
                break;

            case "CHEESY DOG":
                if (getDatabaseStock("Hotdog Bun") < qty * 2) missingList.add("Hotdog Bun");
                if (getDatabaseStock("Hotdog Frank") < qty * 2) missingList.add("Hotdog Frank");
                if (getDatabaseStock("Cheese Sauce") < qty) missingList.add("Cheese Sauce");
                break;

            case "FRUTWIST 3.0":
            case "FRUITWIST 3.0":
                if (getDatabaseStock("Juice / Beverage Mix") < qty) missingList.add("Juice / Beverage Mix");
                if (getDatabaseStock("Drink Cup") < qty) missingList.add("Drink Cup");
                if (getDatabaseStock("Straw") < qty) missingList.add("Straw");
                break;

            case "LEMON BLACK TEA":
            case "LEMON BLACK":
                if (getDatabaseStock("Lemon Black Tea Mix") < qty) missingList.add("Lemon Black Tea Mix");
                if (getDatabaseStock("Drink Cup") < qty) missingList.add("Drink Cup");
                if (getDatabaseStock("Straw") < qty) missingList.add("Straw");
                break;

            case "HOT CHOCO MALLOW":
                if (getDatabaseStock("Choco Mix") < qty) missingList.add("Choco Mix");
                if (getDatabaseStock("Marshmallows") < qty) missingList.add("Marshmallows");
                if (getDatabaseStock("Drink Cup") < qty) missingList.add("Drink Cup");
                break;

            case "ICED CHOCO":
                if (getDatabaseStock("Choco Mix") < qty) missingList.add("Choco Mix");
                if (getDatabaseStock("Drink Cup") < qty) missingList.add("Drink Cup");
                if (getDatabaseStock("Straw") < qty) missingList.add("Straw");
                break;

            case "HOT COFFEE":
                if (getDatabaseStock("Coffee Mix") < qty) missingList.add("Coffee Mix");
                if (getDatabaseStock("Drink Cup") < qty) missingList.add("Drink Cup");
                break;

            case "MINERAL WATER":
                if (getDatabaseStock("Bottled Water") < qty) missingList.add("Bottled Water");
                break;

            default:
                missingList.add("General Ingredient");
                break;
        }

        if (missingList.isEmpty()) {
            return "None";
        }
        return String.join(", ", missingList);
    }

    // 3. CALCULATE MAX BUILDABLE QUANTITY BASED ON DATABASE
    public static int getMaxBuildableQuantity(String menuItemName) {
        if (menuItemName == null) return 0;
        String name = menuItemName.trim().toUpperCase();

        switch (name) {
            case "DOUBLE MINUTE BURGER":
                return Math.min(
                    getDatabaseStock("Burger Bun") / 2,
                    Math.min(
                        getDatabaseStock("Beef Patty") / 2,
                        getDatabaseStock("Dressing / Mayo")
                    )
                );

            case "MINUTE BURGER":
                return Math.min(
                    getDatabaseStock("Burger Bun") / 2,
                    Math.min(
                        getDatabaseStock("Beef Patty"),
                        getDatabaseStock("Dressing / Mayo")
                    )
                );

            case "BACON CHEESE BURGER":
            case "BACON CHEASE BURGER":
            case "BACON PIZZA BURGER":
                return Math.min(
                    getDatabaseStock("Burger Bun") / 2,
                    Math.min(
                        getDatabaseStock("Beef Patty") / 2,
                        Math.min(
                            getDatabaseStock("Pizza Sauce"),
                            Math.min(
                                getDatabaseStock("Cheese Slice"),
                                getDatabaseStock("Bacon Strips")
                            )
                        )
                    )
                );

            case "ROASTED SESAME CHICKEN BURGER":
                return Math.min(
                    getDatabaseStock("Burger Bun") / 2,
                    Math.min(
                        getDatabaseStock("Chicken Patty") / 2,
                        getDatabaseStock("Sesame Onion Sauce")
                    )
                );

            case "CHICKEN SISIG BURGER":
                return Math.min(
                    getDatabaseStock("Burger Bun") / 2,
                    Math.min(
                        getDatabaseStock("Chicken Patty") / 2,
                        getDatabaseStock("Chimichurri & Mushroom")
                    )
                );

            case "CHICKEN TIME BURGER":
                return Math.min(
                    getDatabaseStock("Burger Bun") / 2,
                    Math.min(
                        getDatabaseStock("Chicken Patty") / 2,
                        getDatabaseStock("Creamy Dressing")
                    )
                );

            case "DOUBLE CHICKEN TIME + DRINK":
                return Math.min(
                    getDatabaseStock("Burger Bun") / 4,
                    Math.min(
                        getDatabaseStock("Chicken Patty") / 4,
                        Math.min(
                            getDatabaseStock("Creamy Dressing") / 2,
                            Math.min(
                                getDatabaseStock("Juice / Beverage Mix"),
                                Math.min(
                                    getDatabaseStock("Drink Cup"),
                                    getDatabaseStock("Straw")
                                )
                            )
                        )
                    )
                );

            case "CHICKEN TIME + DRINK":
                return Math.min(
                    getDatabaseStock("Burger Bun") / 2,
                    Math.min(
                        getDatabaseStock("Chicken Patty") / 2,
                        Math.min(
                            getDatabaseStock("Creamy Dressing"),
                            Math.min(
                                getDatabaseStock("Juice / Beverage Mix"),
                                Math.min(
                                    getDatabaseStock("Drink Cup"),
                                    getDatabaseStock("Straw")
                                )
                            )
                        )
                    )
                );

            case "FRENCH ONION FRANKS":
                return Math.min(
                    getDatabaseStock("Hotdog Bun") / 2,
                    Math.min(
                        getDatabaseStock("Hotdog Frank") / 2,
                        getDatabaseStock("Sesame Onion Sauce")
                    )
                );

            case "CHILI CON CARNE CHEESE FRANKS":
                return Math.min(
                    getDatabaseStock("Hotdog Bun") / 2,
                    Math.min(
                        getDatabaseStock("Hotdog Frank") / 2,
                        Math.min(
                            getDatabaseStock("Chili Con Carne Sauce"),
                            getDatabaseStock("Cheese Sauce")
                        )
                    )
                );

            case "CHEESY DOG":
                return Math.min(
                    getDatabaseStock("Hotdog Bun") / 2,
                    Math.min(
                        getDatabaseStock("Hotdog Frank") / 2,
                        getDatabaseStock("Cheese Sauce")
                    )
                );

            case "FRUTWIST 3.0":
            case "FRUITWIST 3.0":
                return Math.min(
                    getDatabaseStock("Juice / Beverage Mix"),
                    Math.min(
                        getDatabaseStock("Drink Cup"),
                        getDatabaseStock("Straw")
                    )
                );

            case "LEMON BLACK TEA":
            case "LEMON BLACK":
                return Math.min(
                    getDatabaseStock("Lemon Black Tea Mix"),
                    Math.min(
                        getDatabaseStock("Drink Cup"),
                        getDatabaseStock("Straw")
                    )
                );

            case "HOT CHOCO MALLOW":
                return Math.min(
                    getDatabaseStock("Choco Mix"),
                    Math.min(
                        getDatabaseStock("Marshmallows"),
                        getDatabaseStock("Drink Cup")
                    )
                );

            case "ICED CHOCO":
                return Math.min(
                    getDatabaseStock("Choco Mix"),
                    Math.min(
                        getDatabaseStock("Drink Cup"),
                        getDatabaseStock("Straw")
                    )
                );

            case "HOT COFFEE":
                return Math.min(
                    getDatabaseStock("Coffee Mix"),
                    getDatabaseStock("Drink Cup")
                );

            case "MINERAL WATER":
                return getDatabaseStock("Bottled Water");

            default:
                return 0;
        }
    }

    // 4. CHECK AND DEDUCT INVENTORY IN DATABASE UPON ORDERING
    public static boolean checkAndDeductIngredients(String menuItemName, int qty) {
        int max = getMaxBuildableQuantity(menuItemName);
        if (max < qty) {
            return false;
        }

        String name = menuItemName.trim().toUpperCase();
        switch (name) {
            case "DOUBLE MINUTE BURGER":
                deductStockInDB("Burger Bun", qty * 2);
                deductStockInDB("Beef Patty", qty * 2);
                deductStockInDB("Dressing / Mayo", qty);
                break;

            case "MINUTE BURGER":
                deductStockInDB("Burger Bun", qty * 2);
                deductStockInDB("Beef Patty", qty);
                deductStockInDB("Dressing / Mayo", qty);
                break;

            case "BACON CHEESE BURGER":
            case "BACON CHEASE BURGER":
            case "BACON PIZZA BURGER":
                deductStockInDB("Burger Bun", qty * 2);
                deductStockInDB("Beef Patty", qty * 2);
                deductStockInDB("Pizza Sauce", qty);
                deductStockInDB("Cheese Slice", qty);
                deductStockInDB("Bacon Strips", qty);
                break;

            case "ROASTED SESAME CHICKEN BURGER":
                deductStockInDB("Burger Bun", qty * 2);
                deductStockInDB("Chicken Patty", qty * 2);
                deductStockInDB("Sesame Onion Sauce", qty);
                break;

            case "CHICKEN SISIG BURGER":
                deductStockInDB("Burger Bun", qty * 2);
                deductStockInDB("Chicken Patty", qty * 2);
                deductStockInDB("Chimichurri & Mushroom", qty);
                break;

            case "CHICKEN TIME BURGER":
                deductStockInDB("Burger Bun", qty * 2);
                deductStockInDB("Chicken Patty", qty * 2);
                deductStockInDB("Creamy Dressing", qty);
                break;

            case "DOUBLE CHICKEN TIME + DRINK":
                deductStockInDB("Burger Bun", qty * 4);
                deductStockInDB("Chicken Patty", qty * 4);
                deductStockInDB("Creamy Dressing", qty * 2);
                deductStockInDB("Juice / Beverage Mix", qty);
                deductStockInDB("Drink Cup", qty);
                deductStockInDB("Straw", qty);
                break;

            case "CHICKEN TIME + DRINK":
                deductStockInDB("Burger Bun", qty * 2);
                deductStockInDB("Chicken Patty", qty * 2);
                deductStockInDB("Creamy Dressing", qty);
                deductStockInDB("Juice / Beverage Mix", qty);
                deductStockInDB("Drink Cup", qty);
                deductStockInDB("Straw", qty);
                break;

            case "FRENCH ONION FRANKS":
                deductStockInDB("Hotdog Bun", qty * 2);
                deductStockInDB("Hotdog Frank", qty * 2);
                deductStockInDB("Sesame Onion Sauce", qty);
                break;

            case "CHILI CON CARNE CHEESE FRANKS":
                deductStockInDB("Hotdog Bun", qty * 2);
                deductStockInDB("Hotdog Frank", qty * 2);
                deductStockInDB("Chili Con Carne Sauce", qty);
                deductStockInDB("Cheese Sauce", qty);
                break;

            case "CHEESY DOG":
                deductStockInDB("Hotdog Bun", qty * 2);
                deductStockInDB("Hotdog Frank", qty * 2);
                deductStockInDB("Cheese Sauce", qty);
                break;

            case "FRUTWIST 3.0":
            case "FRUITWIST 3.0":
                deductStockInDB("Juice / Beverage Mix", qty);
                deductStockInDB("Drink Cup", qty);
                deductStockInDB("Straw", qty);
                break;

            case "LEMON BLACK TEA":
            case "LEMON BLACK":
                deductStockInDB("Lemon Black Tea Mix", qty);
                deductStockInDB("Drink Cup", qty);
                deductStockInDB("Straw", qty);
                break;

            case "HOT CHOCO MALLOW":
                deductStockInDB("Choco Mix", qty);
                deductStockInDB("Marshmallows", qty);
                deductStockInDB("Drink Cup", qty);
                break;

            case "ICED CHOCO":
                deductStockInDB("Choco Mix", qty);
                deductStockInDB("Drink Cup", qty);
                deductStockInDB("Straw", qty);
                break;

            case "HOT COFFEE":
                deductStockInDB("Coffee Mix", qty);
                deductStockInDB("Drink Cup", qty);
                break;

            case "MINERAL WATER":
                deductStockInDB("Bottled Water", qty);
                break;
        }
        initStocksIfNeeded();
        return true;
    }

    private static void deductStockInDB(String itemName, int amount) {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "UPDATE inventory SET quantity = quantity - ? WHERE UPPER(TRIM(ingredient_name)) = UPPER(?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, amount);
            pst.setString(2, itemName.trim());
            pst.executeUpdate();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 5. RESTORE INGREDIENTS IN DATABASE
    public static void restoreIngredients(String menuItemName, int restoreQty) {
        if (menuItemName == null) return;
        String name = menuItemName.trim().toUpperCase();

        switch (name) {
            case "DOUBLE MINUTE BURGER":
                addStockInDB("Burger Bun", restoreQty * 2);
                addStockInDB("Beef Patty", restoreQty * 2);
                addStockInDB("Dressing / Mayo", restoreQty);
                break;

            case "MINUTE BURGER":
                addStockInDB("Burger Bun", restoreQty * 2);
                addStockInDB("Beef Patty", restoreQty);
                addStockInDB("Dressing / Mayo", restoreQty);
                break;

            case "BACON CHEESE BURGER":
            case "BACON CHEASE BURGER":
            case "BACON PIZZA BURGER":
                addStockInDB("Burger Bun", restoreQty * 2);
                addStockInDB("Beef Patty", restoreQty * 2);
                addStockInDB("Pizza Sauce", restoreQty);
                addStockInDB("Cheese Slice", restoreQty);
                addStockInDB("Bacon Strips", restoreQty);
                break;

            case "ROASTED SESAME CHICKEN BURGER":
                addStockInDB("Burger Bun", restoreQty * 2);
                addStockInDB("Chicken Patty", restoreQty * 2);
                addStockInDB("Sesame Onion Sauce", restoreQty);
                break;

            case "CHICKEN SISIG BURGER":
                addStockInDB("Burger Bun", restoreQty * 2);
                addStockInDB("Chicken Patty", restoreQty * 2);
                addStockInDB("Chimichurri & Mushroom", restoreQty);
                break;

            case "CHICKEN TIME BURGER":
                addStockInDB("Burger Bun", restoreQty * 2);
                addStockInDB("Chicken Patty", restoreQty * 2);
                addStockInDB("Creamy Dressing", restoreQty);
                break;

            case "DOUBLE CHICKEN TIME + DRINK":
                addStockInDB("Burger Bun", restoreQty * 4);
                addStockInDB("Chicken Patty", restoreQty * 4);
                addStockInDB("Creamy Dressing", restoreQty * 2);
                addStockInDB("Juice / Beverage Mix", restoreQty);
                addStockInDB("Drink Cup", restoreQty);
                addStockInDB("Straw", restoreQty);
                break;

            case "CHICKEN TIME + DRINK":
                addStockInDB("Burger Bun", restoreQty * 2);
                addStockInDB("Chicken Patty", restoreQty * 2);
                addStockInDB("Creamy Dressing", restoreQty);
                addStockInDB("Juice / Beverage Mix", restoreQty);
                addStockInDB("Drink Cup", restoreQty);
                addStockInDB("Straw", restoreQty);
                break;

            case "FRENCH ONION FRANKS":
                addStockInDB("Hotdog Bun", restoreQty * 2);
                addStockInDB("Hotdog Frank", restoreQty * 2);
                addStockInDB("Sesame Onion Sauce", restoreQty);
                break;

            case "CHILI CON CARNE CHEESE FRANKS":
                addStockInDB("Hotdog Bun", restoreQty * 2);
                addStockInDB("Hotdog Frank", restoreQty * 2);
                addStockInDB("Chili Con Carne Sauce", restoreQty);
                addStockInDB("Cheese Sauce", restoreQty);
                break;

            case "CHEESY DOG":
                addStockInDB("Hotdog Bun", restoreQty * 2);
                addStockInDB("Hotdog Frank", restoreQty * 2);
                addStockInDB("Cheese Sauce", restoreQty);
                break;

            case "FRUTWIST 3.0":
            case "FRUITWIST 3.0":
                addStockInDB("Juice / Beverage Mix", restoreQty);
                addStockInDB("Drink Cup", restoreQty);
                addStockInDB("Straw", restoreQty);
                break;

            case "LEMON BLACK TEA":
            case "LEMON BLACK":
                addStockInDB("Lemon Black Tea Mix", restoreQty);
                addStockInDB("Drink Cup", restoreQty);
                addStockInDB("Straw", restoreQty);
                break;

            case "HOT CHOCO MALLOW":
                addStockInDB("Choco Mix", restoreQty);
                addStockInDB("Marshmallows", restoreQty);
                addStockInDB("Drink Cup", restoreQty);
                break;

            case "ICED CHOCO":
                addStockInDB("Choco Mix", restoreQty);
                addStockInDB("Drink Cup", restoreQty);
                addStockInDB("Straw", restoreQty);
                break;

            case "HOT COFFEE":
                addStockInDB("Coffee Mix", restoreQty);
                addStockInDB("Drink Cup", restoreQty);
                break;

            case "MINERAL WATER":
                addStockInDB("Bottled Water", restoreQty);
                break;
        }
        initStocksIfNeeded();
    }

    private static void addStockInDB(String itemName, int amount) {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "UPDATE inventory SET quantity = quantity + ? WHERE UPPER(TRIM(ingredient_name)) = UPPER(?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, amount);
            pst.setString(2, itemName.trim());
            pst.executeUpdate();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 6. UPDATE OR INSERT STOCK IN INVENTORY DATABASE
    public static void updateStockInDatabase(String itemName, int newQuantity) {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "UPDATE inventory SET quantity = ? WHERE UPPER(TRIM(ingredient_name)) = UPPER(?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, newQuantity);
            pst.setString(2, itemName.trim());
            int rows = pst.executeUpdate();
           
            if (rows == 0) {
                String insertSql = "INSERT INTO inventory (ingredient_name, quantity, unit) VALUES (?, ?, 'Pcs')";
                PreparedStatement insertPst = con.prepareStatement(insertSql);
                insertPst.setString(1, itemName.trim().toUpperCase());
                insertPst.setInt(2, newQuantity);
                insertPst.executeUpdate();
                insertPst.close();
            }
           
            pst.close();
            con.close();
            initStocksIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 7. DELETE STOCK FROM INVENTORY DATABASE
    public static void deleteStockFromDatabase(String itemName) {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "DELETE FROM inventory WHERE UPPER(TRIM(ingredient_name)) = UPPER(?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, itemName.trim());
            pst.executeUpdate();
            pst.close();
            con.close();
            initStocksIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 8. SAVE ORDER DIRECTLY TO DATABASE
    public static void saveOrderToDB(String orderId, String items, String total, String cashier, String dateTime) {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "INSERT INTO orders (order_id, items, total_price, cashier, date_time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, orderId);
            pst.setString(2, items);
            pst.setString(3, total);
            pst.setString(4, cashier);
            pst.setString(5, dateTime);
            pst.executeUpdate();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Payment details are stored separately to preserve the existing orders table schema.
    public static void ensureOrderPaymentsTable(Connection con) throws SQLException {
        try (java.sql.Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS order_payments (" +
                    "order_id VARCHAR(100) PRIMARY KEY, " +
                    "payment_method VARCHAR(30) NOT NULL, " +
                    "cash_received DECIMAL(12,2) NOT NULL, " +
                    "change_amount DECIMAL(12,2) NOT NULL)");
        }
    }

    /** Returns the recorded payment details, or null for older transactions. */
    public static String[] getOrderPaymentDetails(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) return null;
        try (Connection con = DatabaseConnection.getConnection()) {
            ensureOrderPaymentsTable(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT payment_method, cash_received, change_amount FROM order_payments WHERE order_id = ?")) {
                ps.setString(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return new String[]{rs.getString(1), rs.getString(2), rs.getString(3)};
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // 9. GET ALL ORDERS FROM DATABASE
    public static ArrayList<String[]> getAllOrdersFromDB() {
        ArrayList<String[]> ordersList = new ArrayList<>();
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "SELECT order_id, items, total_price, cashier, date_time FROM orders";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String oId = rs.getString("order_id");
                String itms = rs.getString("items");
                String tot = rs.getString("total_price");
                String srv = rs.getString("cashier");
                String dt = rs.getString("date_time");
                ordersList.add(new String[]{oId, itms, tot, srv, dt});
            }
            rs.close();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ordersList;
    }

    // 10. DELETE ORDER FROM DATABASE
    public static void deleteOrderFromDB(String orderId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            String sql = "DELETE FROM orders WHERE order_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, orderId);
            pst.executeUpdate();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}