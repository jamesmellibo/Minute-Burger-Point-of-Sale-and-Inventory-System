package minute_burger_pos_and_inventory_system;

import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class inventory_system extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(inventory_system.class.getName());
    private int lastItemCount = -1;
    private boolean isErrorShown = false;
    
    // Temporary archive habang ga-run ang app (ug dili na mobalik pag-re-run kay gi-remove nato sa database)
    private final java.util.Map<String, Integer> archivedItems = new java.util.LinkedHashMap<>();
    // Persistent archive kept outside the project folder, surviving application restarts.
    private final java.nio.file.Path archiveFile = java.nio.file.Paths.get(
            System.getProperty("user.home"), ".minute_burger_inventory_archive.properties");
    private String lastTableSignature = "";
    private javax.swing.Timer inventoryRefreshTimer;
    
    // Gidugang diri ang txtNotes aron mawala ang pula nga error
    private javax.swing.JTextField txtNotes;


    public inventory_system() {
        setUndecorated(true); // Fullscreen / Kiosk Mode
        initComponents();
       
        // Strict Fullscreen Setup
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        this.setBounds(0, 0, screenSize.width, screenSize.height);

        // 1. SAFE POPULATE RIGHT PANEL COMBOBOXES
        try {
            cmbCategory.removeAllItems();
            cmbCategory.addItem("Burgers / Food");
            cmbCategory.addItem("Condiments / Sauces");
            cmbCategory.addItem("Beverages / Supplies");
        } catch (Exception e) {}

        try {
            cmbUnit.removeAllItems();
            cmbUnit.addItem("Pieces");
            cmbUnit.addItem("Packs/Bottles");
            cmbUnit.addItem("Kgs / Grams");
        } catch (Exception e) {}

        try {
            cmbStatus.removeAllItems();
            cmbStatus.addItem("IN STOCK");
            cmbStatus.addItem("LOW STOCK");
            cmbStatus.addItem("OUT OF STOCK");
        } catch (Exception e) {}

        // 2. SAFE POPULATE MIDDLE FILTER COMBOBOXES
        try {
            cmbCategoryFilter.removeAllItems();
            cmbCategoryFilter.addItem("All Categories");
            cmbCategoryFilter.addItem("Burgers / Food");
            cmbCategoryFilter.addItem("Condiments / Sauces");
            cmbCategoryFilter.addItem("Beverages / Supplies");
            cmbCategoryFilter.setSelectedIndex(0);
        } catch (Exception e) {}

        try {
            cmbStatusFilter.removeAllItems();
            cmbStatusFilter.addItem("All Status");
            cmbStatusFilter.addItem("IN STOCK");
            cmbStatusFilter.addItem("LOW STOCK");
            cmbStatusFilter.addItem("OUT OF STOCK");
            cmbStatusFilter.setSelectedIndex(0);
        } catch (Exception e) {}

        // 3. PROFESSIONAL DASHBOARD CARDS COLOR STYLING
        setupCardStyles();

        // 4. SETUP READ-ONLY TABLE MODEL
        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{"#", "Ingredient Name", "Category", "Quantity", "Unit", "Status"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable2.setModel(model);

        // Search immediately as the user types; preserve the NetBeans Design form.
        txtSearch.setText("");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { executeFilterAndSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { executeFilterAndSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { executeFilterAndSearch(); }
        });
        
        cmbCategoryFilter.addActionListener(e -> executeFilterAndSearch());
        cmbStatusFilter.addActionListener(e -> executeFilterAndSearch());

        loadArchivedItems();

        // Initial Data Load
        refreshInventoryData();
        updateLiveDate();

        // Safe Refresh Timer (3 seconds)
        inventoryRefreshTimer = new javax.swing.Timer(3000, e -> {
            refreshInventoryData();
            updateLiveDate();
        });
        inventoryRefreshTimer.start();
    }

    private void setupCardStyles() {
        try {
            if (lblInStock != null) {
                lblInStock.setOpaque(true);
                lblInStock.setBackground(new Color(255, 255, 255));
                lblInStock.setForeground(new Color(0, 153, 76));
                lblInStock.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
                lblInStock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                lblInStock.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 76), 2),
                    " ✔️ IN STOCK ",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                    new java.awt.Color(100, 100, 100)
                ));
            }

            if (lblLowStock != null) {
                lblLowStock.setOpaque(true);
                lblLowStock.setBackground(new Color(255, 255, 255));
                lblLowStock.setForeground(new Color(255, 128, 0));
                lblLowStock.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
                lblLowStock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                lblLowStock.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 128, 0), 2),
                    " ⚠️ LOW STOCK ",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                    new java.awt.Color(100, 100, 100)
                ));
            }

            if (lblOutOfStock != null) {
                lblOutOfStock.setOpaque(true);
                lblOutOfStock.setBackground(new Color(255, 255, 255));
                lblOutOfStock.setForeground(new Color(204, 0, 0));
                lblOutOfStock.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
                lblOutOfStock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                lblOutOfStock.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 0, 0), 2),
                    " ❌ OUT OF STOCK ",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                    new java.awt.Color(100, 100, 100)
                ));
            }
           
            if (lblTotalIngredients != null) {
                lblTotalIngredients.setOpaque(true);
                lblTotalIngredients.setBackground(new Color(255, 255, 255));
                lblTotalIngredients.setForeground(new Color(0, 102, 204));
                lblTotalIngredients.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
                lblTotalIngredients.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                lblTotalIngredients.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 204), 2),
                    " 📦 TOTAL INGREDIENTS ",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                    new java.awt.Color(100, 100, 100)
                ));
            }
        } catch (Exception e) {}
    }

    private void updateLiveDate() {
        if (lblDateTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd, yyyy - hh:mm:ss a");
            lblDateTime.setText(" " + sdf.format(new Date()) + " ");
            lblDateTime.setOpaque(true);
            lblDateTime.setBackground(new java.awt.Color(245, 245, 245));
            lblDateTime.setForeground(new java.awt.Color(80, 80, 80));
            lblDateTime.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(210, 210, 210), 1, true));
        }
    }

    public void refreshInventoryData() {
        executeFilterAndSearch();
    }

    private String getItemCategory(String itemName) {
        itemName = itemName.toUpperCase();
        if (itemName.contains("BUN") || itemName.contains("PATTY") || itemName.contains("BACON") || itemName.contains("CHEESE")) {
            return "Burgers / Food";
        } else if (itemName.contains("SAUCE") || itemName.contains("MAYO") || itemName.contains("DRESSING")) {
            return "Condiments / Sauces";
        } else {
            return "Beverages / Supplies";
        }
    }

    private String getItemUnit(String itemName) {
        itemName = itemName.toUpperCase();
        if (itemName.contains("SAUCE") || itemName.contains("MAYO")) {
            return "Packs/Bottles";
        } else {
            return "Pieces";
        }
    }

    public void executeClearForm() {
        if (txtIngredientName != null) txtIngredientName.setText("");
        try { if (cmbCategory != null) cmbCategory.setSelectedIndex(0); } catch (Exception e) {}
        if (txtQuantity != null) txtQuantity.setText("");
        try { if (cmbUnit != null) cmbUnit.setSelectedIndex(0); } catch (Exception e) {}
        if (txtMinStock != null) txtMinStock.setText("10");
        try { if (cmbStatus != null) cmbStatus.setSelectedIndex(0); } catch (Exception e) {}
        if (txtNotes != null) txtNotes.setText("");
    }

    public void executeSaveItem() {
        String name = txtIngredientName.getText().trim().toUpperCase();
        String qtyStr = txtQuantity.getText().trim();

        if (name.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Ingredient Name and Quantity!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty < 0) {
                JOptionPane.showMessageDialog(this, "Quantity cannot be negative!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SharedData.updateStockInDatabase(name, qty);
            lastItemCount = -1;
            refreshInventoryData();
            executeClearForm();
           
            JOptionPane.showMessageDialog(this, "Ingredient '" + name + "' successfully saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric value for Quantity!", "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void populateFormFromTable() {
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow != -1) {
            String name = jTable2.getValueAt(selectedRow, 1).toString();
            String category = jTable2.getValueAt(selectedRow, 2).toString();
            String qty = jTable2.getValueAt(selectedRow, 3).toString();
            String unit = jTable2.getValueAt(selectedRow, 4).toString();
            String status = jTable2.getValueAt(selectedRow, 5).toString();

            txtIngredientName.setText(name);
            txtQuantity.setText(qty);
           
            try { cmbCategory.setSelectedItem(category); } catch (Exception e) {}
            try { cmbUnit.setSelectedItem(unit); } catch (Exception e) {}
            try { cmbStatus.setSelectedItem(status); } catch (Exception e) {}
            if (txtMinStock != null) txtMinStock.setText("10");
        }
    }

    private void loadArchivedItems() {
        archivedItems.clear();
        if (!java.nio.file.Files.exists(archiveFile)) return;
        java.util.Properties properties = new java.util.Properties();
        try (java.io.InputStream in = java.nio.file.Files.newInputStream(archiveFile)) {
            properties.load(in);
            for (String name : properties.stringPropertyNames()) {
                archivedItems.put(name, Integer.parseInt(properties.getProperty(name)));
            }
        } catch (Exception ex) {
            archivedItems.clear();
            logger.log(java.util.logging.Level.SEVERE, "Cannot read archived inventory", ex);
            JOptionPane.showMessageDialog(this,
                    "Cannot load archived items. Please check the archive file before making changes.\n" + ex.getMessage(),
                    "Archive File Error", JOptionPane.ERROR_MESSAGE);
            archiveStorageReady = false;
        }
    }

    private boolean archiveStorageReady = true;

    private void saveArchivedItems() throws java.io.IOException {
        java.util.Properties properties = new java.util.Properties();
        for (Map.Entry<String, Integer> entry : archivedItems.entrySet()) {
            properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }
        java.nio.file.Path temp = java.nio.file.Files.createTempFile(
                archiveFile.getParent(), "minute_burger_archive_", ".tmp");
        try {
            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(temp)) {
                properties.store(out, "Minute Burger archived inventory - do not delete");
            }
            try {
                java.nio.file.Files.move(temp, archiveFile,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                java.nio.file.Files.move(temp, archiveFile,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            java.nio.file.Files.deleteIfExists(temp);
        }
    }

    private void archiveSelectedIngredient() {
        if (!archiveStorageReady) {
            JOptionPane.showMessageDialog(this, "Archive storage unavailable. No changes made.",
                    "Archive Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int row = jTable2.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an ingredient first.");
            return;
        }
        String name = String.valueOf(jTable2.getValueAt(row, 1));
        if (JOptionPane.showConfirmDialog(this,
                "Archive " + name + "? It will remain in Restore after restarting.",
                "Confirm Archive", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Integer qty = SharedData.ingredientStocks.get(name);
        if (qty == null) {
            JOptionPane.showMessageDialog(this, "Ingredient not found in active stock.");
            return;
        }
        archivedItems.put(name, qty);
        try {
            saveArchivedItems(); // Save the recoverable quantity before removing active stock.
        } catch (Exception ex) {
            archivedItems.remove(name);
            logger.log(java.util.logging.Level.SEVERE, "Cannot save archive", ex);
            JOptionPane.showMessageDialog(this, "Archive not saved: " + ex.getMessage(),
                    "Archive Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            SharedData.deleteStockFromDatabase(name);
            SharedData.ingredientStocks.remove(name);
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, "Unable to remove active stock", ex);
            JOptionPane.showMessageDialog(this,
                    "Archived record saved, but removing active stock failed. Check database.\n" + ex.getMessage(),
                    "Database Warning", JOptionPane.WARNING_MESSAGE);
        }
        refreshInventoryData();
        executeClearForm();
        JOptionPane.showMessageDialog(this, name + " is in Archive until you restore it.");
    }

    private void restoreArchivedIngredient() {
        if (!archiveStorageReady) {
            JOptionPane.showMessageDialog(this, "Archive storage unavailable.",
                    "Restore Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (archivedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No archived ingredients to restore.");
            return;
        }
        Object selected = JOptionPane.showInputDialog(this, "Choose ingredient to restore:",
                "Restore Ingredient", JOptionPane.QUESTION_MESSAGE, null,
                archivedItems.keySet().toArray(), archivedItems.keySet().iterator().next());
        if (selected == null) return;
        String name = selected.toString();
        int qty = archivedItems.get(name);
        try {
            SharedData.updateStockInDatabase(name, qty);
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, "Restore failed", ex);
            JOptionPane.showMessageDialog(this, "Database restore failed: " + ex.getMessage(),
                    "Restore Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        archivedItems.remove(name);
        try {
            saveArchivedItems();
        } catch (Exception ex) {
            archivedItems.put(name, qty);
            logger.log(java.util.logging.Level.SEVERE, "Cannot update archive file", ex);
            JOptionPane.showMessageDialog(this,
                    "Stock updated, but archive file could not be saved. Retry after checking file permissions.\n" + ex.getMessage(),
                    "Archive Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        refreshInventoryData();
        executeClearForm();
        JOptionPane.showMessageDialog(this, name + " restored with " + qty + " stock(s).");
    }

    public void executeFilterAndSearch() {
        try {
            SharedData.initStocksIfNeeded();
            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
            String selectedName = null;
            int selectedRow = jTable2.getSelectedRow();
            if (selectedRow >= 0) selectedName = String.valueOf(jTable2.getValueAt(selectedRow, 1));
            model.setRowCount(0);

            String searchQuery = txtSearch != null ? txtSearch.getText().trim().toUpperCase() : "";
           
            String selectedCategory = "All Categories";
            try {
                if (cmbCategoryFilter != null && cmbCategoryFilter.getSelectedItem() != null) {
                    selectedCategory = cmbCategoryFilter.getSelectedItem().toString();
                }
            } catch (Exception e) {}

            String selectedStatus = "All Status";
            try {
                if (cmbStatusFilter != null && cmbStatusFilter.getSelectedItem() != null) {
                    selectedStatus = cmbStatusFilter.getSelectedItem().toString();
                }
            } catch (Exception e) {}

            int totalItems = 0;
            int inStockCount = 0;
            int lowStockCount = 0;
            int outOfStockCount = 0;

            int idCounter = 1;
            for (Map.Entry<String, Integer> entry : SharedData.ingredientStocks.entrySet()) {
                String itemName = entry.getKey();
                if (archivedItems.containsKey(itemName)) continue;
                int qty = entry.getValue() == null ? 0 : entry.getValue();

                String status;
                if (qty <= 0) {
                    status = "OUT OF STOCK";
                    outOfStockCount++;
                } else if (qty < 10) {
                    status = "LOW STOCK";
                    lowStockCount++;
                } else {
                    status = "IN STOCK";
                    inStockCount++;
                }
                totalItems++;

                String category = getItemCategory(itemName);
                String unit = getItemUnit(itemName);

                boolean matchesSearch = itemName.toUpperCase().contains(searchQuery);
                boolean matchesCategory = selectedCategory.equals("All Categories") || category.equalsIgnoreCase(selectedCategory);
                boolean matchesStatus = selectedStatus.equals("All Status") || status.equalsIgnoreCase(selectedStatus);

                if (matchesSearch && matchesCategory && matchesStatus) {
                    model.addRow(new Object[]{
                        "ING-" + String.format("%03d", idCounter++),
                        itemName,
                        category,
                        qty,
                        unit,
                        status
                    });
                }
            }

            if (selectedName != null) {
                for (int r = 0; r < model.getRowCount(); r++) {
                    if (selectedName.equals(String.valueOf(model.getValueAt(r, 1)))) {
                        jTable2.setRowSelectionInterval(r, r);
                        break;
                    }
                }
            }
            
            lblInStock.setText("<html><div style='text-align: center;'><span style='font-size: 10px; color: #444;'>IN STOCK</span><br/><span style='font-size: 18px; font-weight: bold; color: #00994C;'>" + inStockCount + "</span></div></html>");
            lblLowStock.setText("<html><div style='text-align: center;'><span style='font-size: 10px; color: #444;'>LOW STOCK</span><br/><span style='font-size: 18px; font-weight: bold; color: #FF8000;'>" + lowStockCount + "</span></div></html>");
            lblOutOfStock.setText("<html><div style='text-align: center;'><span style='font-size: 10px; color: #444;'>OUT OF STOCK</span><br/><span style='font-size: 18px; font-weight: bold; color: #CC0000;'>" + outOfStockCount + "</span></div></html>");
           
            try {
                lblTotalIngredients.setText("<html><div style='text-align: center;'><span style='font-size: 10px; color: #444;'>TOTAL INGREDIENTS</span><br/><span style='font-size: 16px; font-weight: bold; color: #0066CC;'>" + totalItems + "</span></div></html>");
            } catch (Exception ex) {}

        } catch (Exception e) {
            logger.log(java.util.logging.Level.WARNING, "Unable to refresh inventory", e);
        }
    }

    public void executeSearch(String query) {
        executeFilterAndSearch();
    }






    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        homepage1 = new javax.swing.JButton();
        lblDateTime = new javax.swing.JLabel();
        btnExport = new javax.swing.JButton();
        btnViewRecipes = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        lblInStock = new javax.swing.JLabel();
        lblLowStock = new javax.swing.JLabel();
        lblOutOfStock = new javax.swing.JLabel();
        btnAddStock = new javax.swing.JButton();
        btnAddNewItem1 = new javax.swing.JButton();
        btnUpdateItems = new javax.swing.JButton();
        btnArchive = new javax.swing.JButton();
        btnRestore = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        cmbCategoryFilter = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtIngredientName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        cmbUnit = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
        txtMinStock = new javax.swing.JTextField();
        btnSaveItem = new javax.swing.JButton();
        btnClearForm = new javax.swing.JButton();
        cmbStatusFilter = new javax.swing.JComboBox<>();
        lblTotalIngredients = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel4.setBackground(new java.awt.Color(255, 153, 0));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("INVENTORY MANAGEMENT");

        homepage1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        homepage1.setIcon(new javax.swing.ImageIcon("C:\\Users\\win10\\Downloads\\340 (1).jpg")); // NOI18N
        homepage1.setText("BACK");
        homepage1.addActionListener(this::homepage1homepageActionPerformed);

        lblDateTime.setFont(new java.awt.Font("Segoe UI", 3, 17)); // NOI18N
        lblDateTime.setForeground(new java.awt.Color(255, 255, 255));
        lblDateTime.setText("Date:");

        btnExport.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnExport.setText("EXPORT CSV");
        btnExport.addActionListener(this::btnExportActionPerformed);

        btnViewRecipes.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnViewRecipes.setText("VIEW RECIPES");
        btnViewRecipes.addActionListener(this::btnViewRecipesActionPerformed);

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Manage your ingredients and stocks levels");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(homepage1)
                .addGap(39, 39, 39)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel13)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnViewRecipes)
                        .addGap(18, 18, 18)
                        .addComponent(btnExport)
                        .addGap(18, 18, 18)
                        .addComponent(lblDateTime)
                        .addGap(184, 184, 184))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(homepage1)
                    .addComponent(jLabel12)
                    .addComponent(btnViewRecipes)
                    .addComponent(btnExport)
                    .addComponent(lblDateTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addContainerGap())
        );

        jTable2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "#", "Ingredient", "Category", "Quantity", "Unit", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true, true, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable2);

        lblInStock.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblInStock.setForeground(new java.awt.Color(0, 0, 255));
        lblInStock.setText("  IN STOCK ");
        lblInStock.setMaximumSize(new java.awt.Dimension(176, 38));
        lblInStock.setMinimumSize(new java.awt.Dimension(176, 38));
        lblInStock.setPreferredSize(new java.awt.Dimension(176, 38));

        lblLowStock.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblLowStock.setForeground(new java.awt.Color(255, 102, 0));
        lblLowStock.setText("LOW STOCK ");
        lblLowStock.setMaximumSize(new java.awt.Dimension(176, 38));
        lblLowStock.setMinimumSize(new java.awt.Dimension(176, 38));

        lblOutOfStock.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblOutOfStock.setForeground(new java.awt.Color(255, 0, 0));
        lblOutOfStock.setText("OUT OF STOCK");

        btnAddStock.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddStock.setIcon(new javax.swing.ImageIcon("C:\\Users\\win10\\Downloads\\print (3).jpg")); // NOI18N
        btnAddStock.setText("ADD STOCK");
        btnAddStock.addActionListener(this::btnAddStockActionPerformed);

        btnAddNewItem1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddNewItem1.setIcon(new javax.swing.ImageIcon("C:\\Users\\win10\\Downloads\\resized-image (2).png")); // NOI18N
        btnAddNewItem1.setText("ADD NEW ITEM");
        btnAddNewItem1.addActionListener(this::btnAddNewItem1ActionPerformed);

        btnUpdateItems.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnUpdateItems.setIcon(new javax.swing.ImageIcon("C:\\Users\\win10\\Downloads\\resized_ASA (3).png")); // NOI18N
        btnUpdateItems.setText("UPDATE ITEMS");
        btnUpdateItems.addActionListener(this::btnUpdateItemsActionPerformed);

        btnArchive.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnArchive.setIcon(new javax.swing.ImageIcon("C:\\Users\\win10\\Downloads\\resized-image (3).png")); // NOI18N
        btnArchive.setText("ARCHIVE ");
        btnArchive.addActionListener(this::btnArchiveActionPerformed);

        btnRestore.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRestore.setIcon(new javax.swing.ImageIcon("C:\\Users\\win10\\Downloads\\resized-image (4).png")); // NOI18N
        btnRestore.setText("RESTORE");
        btnRestore.addActionListener(this::btnRestoreActionPerformed);

        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtSearch.setText("    Search Ingredient...");
        txtSearch.addActionListener(this::txtSearchActionPerformed);

        cmbCategoryFilter.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbCategoryFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Categories", "Breads", "Meat", "Dairy", "Vegetables", "Sauces", "Spices", "Others", " " }));
        cmbCategoryFilter.addActionListener(this::cmbCategoryFilterActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Ingredient Details");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Add or edit ingredient information");

        jLabel4.setIcon(new javax.swing.ImageIcon("C:\\Users\\win10\\Downloads\\resized-image (5).png")); // NOI18N

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel5.setText("Ingredients Name");

        txtIngredientName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel6.setText("Category");

        cmbCategory.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel7.setText("Quantity");

        txtQuantity.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel8.setText("Unit");

        cmbUnit.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbUnit.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel9.setText("Minimum Stock Level");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel10.setText("Status");

        cmbStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbStatus.addActionListener(this::cmbStatusActionPerformed);

        txtMinStock.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        btnSaveItem.setBackground(new java.awt.Color(255, 102, 0));
        btnSaveItem.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnSaveItem.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveItem.setText("Save Item");
        btnSaveItem.addActionListener(this::btnSaveItemActionPerformed);

        btnClearForm.setBackground(new java.awt.Color(204, 204, 204));
        btnClearForm.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnClearForm.setText("Clear Form");
        btnClearForm.addActionListener(this::btnClearFormActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel9)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cmbStatus, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtMinStock, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cmbCategory, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtIngredientName, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                            .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(26, 26, 26))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                            .addComponent(jLabel7)
                                            .addGap(172, 172, 172)))
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel8)
                                        .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(24, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnClearForm, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveItem, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel4)))
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtIngredientName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addComponent(txtMinStock, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSaveItem, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClearForm, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(217, Short.MAX_VALUE))
        );

        cmbStatusFilter.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cmbStatusFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Status", "In Stock", "Low Stock", "Out of Stock", " " }));
        cmbStatusFilter.addActionListener(this::cmbStatusFilterActionPerformed);

        lblTotalIngredients.setBackground(new java.awt.Color(255, 255, 255));
        lblTotalIngredients.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblTotalIngredients.setForeground(new java.awt.Color(0, 255, 0));
        lblTotalIngredients.setText("       TOTAL");
        lblTotalIngredients.setOpaque(true);

        jLabel2.setIcon(new javax.swing.ImageIcon("C:\\Users\\win10\\Downloads\\resized-image (6).png")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cmbCategoryFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cmbStatusFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddNewItem1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAddStock)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnUpdateItems)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnArchive, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRestore)
                        .addGap(6, 6, 6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblTotalIngredients, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblInStock, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblLowStock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblOutOfStock, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(125, 125, 125))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblTotalIngredients, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                            .addComponent(lblLowStock, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblOutOfStock, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblInStock, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbCategoryFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbStatusFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnRestore))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnAddNewItem1)
                                .addComponent(btnAddStock, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnUpdateItems, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void homepage1homepageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homepage1homepageActionPerformed
        homepage hp = new homepage();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        hp.setBounds(0, 0, screenSize.width, screenSize.height);
        hp.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_homepage1homepageActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Save Inventory Report as CSV");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String defaultFileName = "InventoryReport_" + sdf.format(new java.util.Date()) + ".csv";
        fileChooser.setSelectedFile(new java.io.File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(fileToSave))) {
                DefaultTableModel model = (DefaultTableModel) jTable2.getModel();

                for (int i = 0; i < model.getColumnCount(); i++) {
                    pw.print("\"" + model.getColumnName(i) + "\"");
                    if (i < model.getColumnCount() - 1) pw.print(",");
                }
                pw.println();

                for (int row = 0; row < model.getRowCount(); row++) {
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        Object val = model.getValueAt(row, col);
                        String cellValue = (val != null) ? val.toString().replace("\"", "\"\"") : "";
                        pw.print("\"" + cellValue + "\"");
                        if (col < model.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }

                JOptionPane.showMessageDialog(this, "Inventory report successfully exported to:\n" + fileToSave.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting inventory report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnViewRecipesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewRecipesActionPerformed
        StringBuilder guide = new StringBuilder();
        guide.append("=========================================================\n");
        guide.append("       🍔 MINUTE BURGER - COMPLETE MENU & RECIPE LIST 🥤\n");
        guide.append("=========================================================\n\n");

        // --- BURGERS ---
        guide.append("【 🍔 BURGERS CATEGORY 】\n");
        guide.append("• MINUTE BURGER\n");
        guide.append("   - Ingredients: 2x Burger Bun, 1x Beef Patty, 1x Dressing / Mayo\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("MINUTE BURGER")).append("\n\n");

        guide.append("• DOUBLE MINUTE BURGER\n");
        guide.append("   - Ingredients: 2x Burger Bun, 2x Beef Patty, 1x Dressing / Mayo\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("DOUBLE MINUTE BURGER")).append("\n\n");

        guide.append("• BACON PIZZA BURGER / BACON CHEESE BURGER\n");
        guide.append("   - Ingredients: 2x Burger Bun, 2x Beef Patty, 1x Pizza Sauce, 1x Cheese Slice, 1x Bacon Strips\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("BACON CHEESE BURGER")).append("\n\n");

        guide.append("• ROASTED SESAME CHICKEN BURGER\n");
        guide.append("   - Ingredients: 2x Burger Bun, 2x Chicken Patty, 1x Sesame Onion Sauce\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("ROASTED SESAME CHICKEN BURGER")).append("\n\n");

        guide.append("• CHICKEN SISIG BURGER\n");
        guide.append("   - Ingredients: 2x Burger Bun, 2x Chicken Patty, 1x Chimichurri & Mushroom\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("CHICKEN SISIG BURGER")).append("\n\n");

        // --- SULIT DEALS ---
        guide.append("【 🏷️ SULIT DEALS CATEGORY 】\n");
        guide.append("• CHICKEN TIME BURGER\n");
        guide.append("   - Ingredients: 2x Burger Bun, 2x Chicken Patty, 1x Creamy Dressing\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("CHICKEN TIME BURGER")).append("\n\n");

        guide.append("• DOUBLE CHICKEN TIME + DRINK\n");
        guide.append("   - Ingredients: 4x Burger Bun, 4x Chicken Patty, 2x Creamy Dressing, 1x Juice Mix, 1x Cup, 1x Straw\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("DOUBLE CHICKEN TIME + DRINK")).append("\n\n");

        guide.append("• CHICKEN TIME + DRINK\n");
        guide.append("   - Ingredients: 2x Burger Bun, 2x Chicken Patty, 1x Creamy Dressing, 1x Juice Mix, 1x Cup, 1x Straw\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("CHICKEN TIME + DRINK")).append("\n\n");

        // --- HOTDOGS ---
        guide.append("【 🌭 HOTDOGS CATEGORY 】\n");
        guide.append("• FRENCH ONION FRANKS\n");
        guide.append("   - Ingredients: 2x Hotdog Bun, 2x Hotdog Frank, 1x Sesame Onion Sauce\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("FRENCH ONION FRANKS")).append("\n\n");

        guide.append("• CHILI CON CARNE CHEESE FRANKS\n");
        guide.append("   - Ingredients: 2x Hotdog Bun, 2x Hotdog Frank, 1x Chili Con Carne Sauce, 1x Cheese Sauce\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("CHILI CON CARNE CHEESE FRANKS")).append("\n\n");

        guide.append("• CHEESY DOG\n");
        guide.append("   - Ingredients: 2x Hotdog Bun, 2x Hotdog Frank, 1x Cheese Sauce\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("CHEESY DOG")).append("\n\n");

        // --- DRINKS ---
        guide.append("【 🥤 DRINKS CATEGORY 】\n");
        guide.append("• FRUTWIST 3.0\n");
        guide.append("   - Ingredients: 1x Juice / Beverage Mix, 1x Drink Cup, 1x Straw\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("FRUTWIST 3.0")).append("\n\n");

        guide.append("• LEMON BLACK TEA\n");
        guide.append("   - Ingredients: 1x Lemon Black Tea Mix, 1x Drink Cup, 1x Straw\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("LEMON BLACK TEA")).append("\n\n");

        guide.append("• HOT CHOCO MALLOW\n");
        guide.append("   - Ingredients: 1x Choco Mix, 1x Marshmallows, 1x Drink Cup\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("HOT CHOCO MALLOW")).append("\n\n");

        guide.append("• ICED CHOCO\n");
        guide.append("   - Ingredients: 1x Choco Mix, 1x Drink Cup, 1x Straw\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("ICED CHOCO")).append("\n\n");

        guide.append("• HOT COFFEE\n");
        guide.append("   - Ingredients: 1x Coffee Mix, 1x Drink Cup\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("HOT COFFEE")).append("\n\n");

        guide.append("• MINERAL WATER\n");
        guide.append("   - Ingredients: 1x Bottled Water\n");
        guide.append("   - Available Set(s): ").append(SharedData.getMaxBuildableQuantity("MINERAL WATER")).append("\n");

        guide.append("=========================================================");

        // I-display sulod sa scrollable text area
        javax.swing.JTextArea textArea = new javax.swing.JTextArea(guide.toString());
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        textArea.setEditable(false);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(580, 450));

        JOptionPane.showMessageDialog(this, scrollPane, "Complete Menu List & Ingredients Mapping", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnViewRecipesActionPerformed

    private void btnAddStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddStockActionPerformed
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an ingredient from the table to add stock!", "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemName = jTable2.getValueAt(selectedRow, 1).toString();
        String inputQty = JOptionPane.showInputDialog(this, "Enter quantity to add for: " + itemName, "Add Stock Quantity", JOptionPane.QUESTION_MESSAGE);

        if (inputQty != null && !inputQty.trim().isEmpty()) {
            try {
                int addQty = Integer.parseInt(inputQty.trim());
                if (addQty <= 0) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid positive number!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int currentStock = SharedData.ingredientStocks.getOrDefault(itemName, 0);
                int newStock = currentStock + addQty;

                SharedData.updateStockInDatabase(itemName, newStock);
                lastItemCount = -1;
                refreshInventoryData();
                JOptionPane.showMessageDialog(this, "Successfully added +" + addQty + " stock to " + itemName + "!", "Stock Updated", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric value!", "Format Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnAddStockActionPerformed

    private void btnAddNewItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddNewItem1ActionPerformed
        executeClearForm();
        txtIngredientName.requestFocus();
        JOptionPane.showMessageDialog(this, "Please fill in the Ingredient Details on the right panel and click 'Save Item'.", "Add New Item", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnAddNewItem1ActionPerformed

    private void btnUpdateItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateItemsActionPerformed
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to update!", "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        populateFormFromTable();
        JOptionPane.showMessageDialog(this, "Item details loaded into the right panel. Modify the values and click 'Save Item'.", "Update Item Mode", JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_btnUpdateItemsActionPerformed

    private void btnArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArchiveActionPerformed
        archiveSelectedIngredient();
    }//GEN-LAST:event_btnArchiveActionPerformed

    private void btnRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestoreActionPerformed
        restoreArchivedIngredient();
    }//GEN-LAST:event_btnRestoreActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        executeFilterAndSearch();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void cmbCategoryFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCategoryFilterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCategoryFilterActionPerformed

    private void cmbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusActionPerformed
        executeFilterAndSearch();
    }//GEN-LAST:event_cmbStatusActionPerformed

    private void btnSaveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveItemActionPerformed
        executeSaveItem();

    }//GEN-LAST:event_btnSaveItemActionPerformed

    private void btnClearFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearFormActionPerformed
      if (txtIngredientName != null) txtIngredientName.setText("");
    try { if (cmbCategory != null) cmbCategory.setSelectedIndex(0); } catch (Exception e) {}
    if (txtQuantity != null) txtQuantity.setText("");
    try { if (cmbUnit != null) cmbUnit.setSelectedIndex(0); } catch (Exception e) {}
    if (txtMinStock != null) txtMinStock.setText("10");
    try { if (cmbStatus != null) cmbStatus.setSelectedIndex(0); } catch (Exception e) {}
    if (txtNotes != null) txtNotes.setText("");

    }//GEN-LAST:event_btnClearFormActionPerformed

    private void cmbStatusFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusFilterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbStatusFilterActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new inventory_system().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddNewItem1;
    private javax.swing.JButton btnAddStock;
    private javax.swing.JButton btnArchive;
    private javax.swing.JButton btnClearForm;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnRestore;
    private javax.swing.JButton btnSaveItem;
    private javax.swing.JButton btnUpdateItems;
    private javax.swing.JButton btnViewRecipes;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbCategoryFilter;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JComboBox<String> cmbStatusFilter;
    private javax.swing.JComboBox<String> cmbUnit;
    private javax.swing.JButton homepage1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable2;
    private javax.swing.JLabel lblDateTime;
    private javax.swing.JLabel lblInStock;
    private javax.swing.JLabel lblLowStock;
    private javax.swing.JLabel lblOutOfStock;
    private javax.swing.JLabel lblTotalIngredients;
    private javax.swing.JTextField txtIngredientName;
    private javax.swing.JTextField txtMinStock;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables

    private void loadInventoryData() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
