package minute_burger_pos_and_inventory_system;

import java.awt.*;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class sales_report extends javax.swing.JFrame {
    private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(sales_report.class.getName());
    private final java.util.List<String[]> displayedOrders = new ArrayList<>();
    private javax.swing.Timer refreshTimer;
    private String lastSignature = "";

    public sales_report() {
        setUndecorated(true);
        initComponents();
        setupReport();
        refreshDashboardData();
        refreshTimer = new javax.swing.Timer(3000, e -> refreshDashboardData());
        refreshTimer.start();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { refreshTimer.stop(); }
        });
    }

    private void setupReport() {
        setTitle("Minute Burger - Sales Report");
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        setBounds(bounds);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        panelMain.setBackground(Color.WHITE);
        panelCenter.setBackground(Color.WHITE);
        panelHeader.setBackground(new Color(224, 66, 39));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        panelHeader.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 8));
        panelFilters.setBackground(new Color(255, 248, 242));
        panelFilters.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        panelFilters.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 8));
        panelFooter.setBackground(new Color(246, 247, 249));
        panelFooter.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));
        panelFooter.setLayout(new FlowLayout(FlowLayout.RIGHT, 18, 8));
        panelRight.setBackground(Color.WHITE);
        panelStats.setBackground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitle.setForeground(Color.WHITE);
        for (JLabel l : new JLabel[]{lblSales,lblOrders,lblItems,lblPeriod}) {
            l.setFont(new Font("Segoe UI", Font.BOLD, 20));
            l.setOpaque(true);
            l.setBackground(new Color(255, 249, 244));
            l.setForeground(new Color(39, 47, 59));
            l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(228, 228, 228)), BorderFactory.createEmptyBorder(15, 16, 15, 16)));
        }
        panelRight.setPreferredSize(new Dimension(470, 300));
        panelRight.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        for (JLabel label : new JLabel[]{lblFrom, lblTo}) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 19));
            label.setForeground(new Color(37, 48, 60));
        }
        for (JSpinner spinner : new JSpinner[]{dateFrom, dateTo}) {
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 19));
            spinner.setPreferredSize(new Dimension(190, 43));
        }
        for (JButton button : new JButton[]{btnBack, btnExport, btnGenerate, btnView, btnPrint, btnDelete, btnNewOrder}) {
            button.setFont(new Font("Segoe UI", Font.BOLD, 17));
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(button == btnGenerate ? 220 : 185, 49));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setBackground(new Color(242, 115, 34));
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
        }
        btnDelete.setBackground(new Color(198, 45, 45));
        btnPrint.setBackground(new Color(35, 101, 180));
        btnNewOrder.setBackground(new Color(30, 130, 86));
        btnBack.setBackground(new Color(45, 55, 70));
        panelCenter.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        scrollSales.setBorder(BorderFactory.createTitledBorder("Sales Transactions"));
        scrollReceipt.setBorder(BorderFactory.createTitledBorder("Receipt Preview"));
        txtReceipt.setEditable(false);
        txtReceipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        txtReceipt.setBackground(new Color(255, 253, 249));
        txtReceipt.setMargin(new Insets(10, 12, 10, 12));
        tblSales.setRowHeight(45);
        tblSales.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        tblSales.setGridColor(new Color(225, 229, 233));
        tblSales.setSelectionBackground(new Color(255, 225, 196));
        tblSales.setSelectionForeground(new Color(36, 43, 54));
        tblSales.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 17));
        tblSales.getTableHeader().setPreferredSize(new Dimension(100, 48));
        tblSales.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSales.setAutoCreateRowSorter(true);
        tblSales.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Order ID", "Items", "Total Price", "Staff Name", "Payment Method", "Date & Time"}) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        dateFrom.setModel(new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH));
        dateTo.setModel(new SpinnerDateModel(today, null, null, Calendar.DAY_OF_MONTH));
        dateFrom.setEditor(new JSpinner.DateEditor(dateFrom, "yyyy-MM-dd"));
        dateTo.setEditor(new JSpinner.DateEditor(dateTo, "yyyy-MM-dd"));
        btnGenerate.addActionListener(e -> { lastSignature = ""; refreshDashboardData(); });
        btnBack.addActionListener(e -> { new homepage().setVisible(true); dispose(); });
        btnExport.addActionListener(e -> exportCSV());
        btnView.addActionListener(e -> showReceipt());
        btnPrint.addActionListener(e -> printReceipt());
        btnDelete.addActionListener(e -> deleteSelectedOrder());
        btnNewOrder.addActionListener(e -> { new pos_ordering().setVisible(true); dispose(); });
        tblSales.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateReceiptPreview(); });
        txtReceipt.setText("Select a transaction to preview its receipt.");
    }

    private LocalDate parseOrderDate(String value) {
        if (value == null) return null;
        for (String pattern : new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd", "MMM dd, yyyy hh:mm:ss a", "MMMM dd, yyyy hh:mm:ss a", "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy"}) {
            SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.ENGLISH);
            parser.setLenient(false);
            java.text.ParsePosition pos = new java.text.ParsePosition(0);
            Date parsed = parser.parse(value.trim(), pos);
            if (parsed != null && pos.getIndex() == value.trim().length()) return parsed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private void refreshDashboardData() {
        LocalDate from = ((Date)dateFrom.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate to = ((Date)dateTo.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (from.isAfter(to)) { if (!lastSignature.equals("INVALID")) JOptionPane.showMessageDialog(this, "From Date must not be after To Date."); lastSignature="INVALID"; return; }
        try {
            ArrayList<String[]> orders = SharedData.getAllOrdersFromDB();
            StringBuilder sig = new StringBuilder(from.toString()).append('/').append(to);
            if (orders != null) for (String[] order : orders) sig.append(Arrays.toString(order));
            if (sig.toString().equals(lastSignature) && !SharedData.hasNewOrder) return;
            lastSignature = sig.toString();
            String previousId = null;
            int selected = tblSales.getSelectedRow();
            if (selected >= 0) previousId = String.valueOf(tblSales.getValueAt(selected, 0));
            DefaultTableModel model = (DefaultTableModel)tblSales.getModel();
            model.setRowCount(0);
            displayedOrders.clear();
            double total = 0;
            int count = 0;
            if (orders != null) for (String[] order : orders) {
                if (order == null || order.length < 5) continue;
                LocalDate day = parseOrderDate(order[4]);
                if (day == null || day.isBefore(from) || day.isAfter(to)) continue;
                String payment = order.length > 5 ? order[5] : "Not recorded";
                model.addRow(new Object[]{order[0], order[1], order[2], order[3], payment, order[4]});
                displayedOrders.add(order);
                try { total += Double.parseDouble(order[2].replace("₱", "").replace(",", "").trim()); } catch (NumberFormatException ignored) { }
                count++;
            }
            lblSales.setText(String.format(Locale.US,"TOTAL SALES: ₱ %,.2f", total));
            lblOrders.setText("TOTAL ORDERS: " + count);
            lblItems.setText("ITEMS SOLD: See transactions");
            lblPeriod.setText("PERIOD: " + from + " to " + to);
            SharedData.hasNewOrder = false;
            if (previousId != null) for (int i=0;i<model.getRowCount();i++) if (previousId.equals(String.valueOf(model.getValueAt(i,0)))) { int view=tblSales.convertRowIndexToView(i); if (view>=0) tblSales.setRowSelectionInterval(view,view); break; }
            updateReceiptPreview();
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE,"Sales report refresh failed",ex);
            lblPeriod.setText("Unable to load sales - check database");
        }
    }

    // Receipt uses recorded transaction fields only. Never invent cash/change.
    private String selectedReceipt() {
        int row = tblSales.getSelectedRow();
        if (row < 0) return null;
        int modelRow = tblSales.convertRowIndexToModel(row);
        String[] record = modelRow < displayedOrders.size() ? displayedOrders.get(modelRow) : null;
        String id = safeReceiptValue(tblSales.getValueAt(row, 0));
        String items = safeReceiptValue(tblSales.getValueAt(row, 1));
        String total = safeReceiptValue(tblSales.getValueAt(row, 2));
        String cashier = safeReceiptValue(tblSales.getValueAt(row, 3));
        String payment = safeReceiptValue(tblSales.getValueAt(row, 4));
        String date = safeReceiptValue(tblSales.getValueAt(row, 5));

        // Legacy records: 0=id, 1=items, 2=total, 3=cashier,
        // 4=date, 5=payment. Extra fields are NOT assumed to be cash/change.
        // Those require a verified database column mapping from SharedData.
        String cash = "Not recorded";
        String change = "Not recorded";
        StringBuilder receipt = new StringBuilder();
        receipt.append("==========================================\n")
               .append("              MINUTE BURGER\n")
               .append("              OFFICIAL RECEIPT\n")
               .append("==========================================\n")
               .append("Receipt No. : ").append(id).append('\n')
               .append("Date / Time : ").append(date).append('\n')
               .append("Cashier     : ").append(cashier).append('\n')
               .append("Payment     : ").append(payment).append('\n')
               .append("------------------------------------------\n")
               .append("ITEMS PURCHASED\n")
               .append(items.replace("; ", "\n").replace(" | ", "\n"))
               .append("\n------------------------------------------\n")
               .append("TOTAL AMOUNT  : ").append(total).append('\n')
               .append("CASH RECEIVED : ").append(cash).append('\n')
               .append("CHANGE        : ").append(change).append('\n')
               .append("------------------------------------------\n")
               .append("TRANSACTION STATUS: COMPLETED\n")
               .append("==========================================\n")
               .append("       THANK YOU FOR YOUR PURCHASE!\n")
               .append("             PLEASE COME AGAIN\n")
               .append("==========================================\n");
        return receipt.toString();
    }

    private String safeReceiptValue(Object value) {
        return value == null || value.toString().trim().isEmpty()
                ? "Not recorded" : value.toString().trim();
    }
    private void updateReceiptPreview() { String r=selectedReceipt(); txtReceipt.setText(r==null?"Select a transaction to preview its receipt.":r); txtReceipt.setCaretPosition(0); }
    private void showReceipt() { String r=selectedReceipt(); if (r==null) { JOptionPane.showMessageDialog(this,"Select a transaction first."); return; } JTextArea area=new JTextArea(r);area.setEditable(false);area.setFont(new Font(Font.MONOSPACED,Font.PLAIN,16));JOptionPane.showMessageDialog(this,new JScrollPane(area),"Minute Burger Receipt",JOptionPane.PLAIN_MESSAGE); }
    private void printReceipt() { if (selectedReceipt()==null) { JOptionPane.showMessageDialog(this,"Select a transaction first."); return; } try { txtReceipt.print(); } catch (PrinterException ex) { JOptionPane.showMessageDialog(this,"Printing failed: "+ex.getMessage()); } }
    private void deleteSelectedOrder() { int row=tblSales.getSelectedRow(); if (row<0) { JOptionPane.showMessageDialog(this,"Select a transaction first.");return; } String id=String.valueOf(tblSales.getValueAt(row,0)); if (JOptionPane.showConfirmDialog(this,"Permanently delete order "+id+"?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return; SharedData.deleteOrderFromDB(id); lastSignature=""; refreshDashboardData(); }
    private void exportCSV() {
        JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File("SalesReport_"+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())+".csv"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        java.io.File file=chooser.getSelectedFile();if(!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv"))file=new java.io.File(file.getAbsolutePath()+".csv");
        try(java.io.PrintWriter out=new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(file),java.nio.charset.StandardCharsets.UTF_8))){
            DefaultTableModel m=(DefaultTableModel)tblSales.getModel();
            for(int c=0;c<m.getColumnCount();c++){if(c>0)out.print(',');out.print(csv(m.getColumnName(c)));}out.println();
            for(int r=0;r<tblSales.getRowCount();r++){for(int c=0;c<tblSales.getColumnCount();c++){if(c>0)out.print(',');out.print(csv(String.valueOf(tblSales.getValueAt(r,c))));}out.println();}
            JOptionPane.showMessageDialog(this,"Exported: "+file.getAbsolutePath());
        }catch(Exception ex){JOptionPane.showMessageDialog(this,"Export failed: "+ex.getMessage());}
    }
    private String csv(String value){return "\""+value.replace("\"","\"\"")+"\"";}

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        panelMain = new javax.swing.JPanel();
        panelHeader = new javax.swing.JPanel();
        btnBack = new javax.swing.JButton();
        lblTitle = new javax.swing.JLabel();
        btnExport = new javax.swing.JButton();
        panelCenter = new javax.swing.JPanel();
        panelFilters = new javax.swing.JPanel();
        lblFrom = new javax.swing.JLabel();
        dateFrom = new javax.swing.JSpinner();
        lblTo = new javax.swing.JLabel();
        dateTo = new javax.swing.JSpinner();
        btnGenerate = new javax.swing.JButton();
        scrollSales = new javax.swing.JScrollPane();
        tblSales = new javax.swing.JTable();
        panelRight = new javax.swing.JPanel();
        panelStats = new javax.swing.JPanel();
        lblSales = new javax.swing.JLabel();
        lblOrders = new javax.swing.JLabel();
        lblItems = new javax.swing.JLabel();
        lblPeriod = new javax.swing.JLabel();
        scrollReceipt = new javax.swing.JScrollPane();
        txtReceipt = new javax.swing.JTextArea();
        panelFooter = new javax.swing.JPanel();
        btnView = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnNewOrder = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.BorderLayout());
        panelMain.setLayout(new java.awt.BorderLayout());
        panelHeader.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 24, 16));
        btnBack.setText("BACK"); panelHeader.add(btnBack);
        lblTitle.setText("SALES REPORT"); panelHeader.add(lblTitle);
        btnExport.setText("EXPORT CSV"); panelHeader.add(btnExport);
        panelMain.add(panelHeader, java.awt.BorderLayout.NORTH);
        panelCenter.setLayout(new java.awt.BorderLayout(12,12));
        panelFilters.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 12));
        lblFrom.setText("From Date:"); panelFilters.add(lblFrom);
        panelFilters.add(dateFrom);
        lblTo.setText("To Date:"); panelFilters.add(lblTo);
        panelFilters.add(dateTo);
        btnGenerate.setText("GENERATE REPORT"); panelFilters.add(btnGenerate);
        panelCenter.add(panelFilters,java.awt.BorderLayout.NORTH);
        scrollSales.setViewportView(tblSales);
        panelCenter.add(scrollSales,java.awt.BorderLayout.CENTER);
        panelRight.setLayout(new java.awt.BorderLayout(0,12));
        panelStats.setLayout(new java.awt.GridLayout(4,1));
        lblSales.setText("TOTAL SALES: ₱ 0.00"); panelStats.add(lblSales);
        lblOrders.setText("TOTAL ORDERS: 0"); panelStats.add(lblOrders);
        lblItems.setText("ITEMS SOLD: —"); panelStats.add(lblItems);
        lblPeriod.setText("SELECTED PERIOD: —"); panelStats.add(lblPeriod);
        panelRight.add(panelStats,java.awt.BorderLayout.NORTH);
        scrollReceipt.setViewportView(txtReceipt);
        panelRight.add(scrollReceipt,java.awt.BorderLayout.CENTER);
        panelCenter.add(panelRight,java.awt.BorderLayout.EAST);
        panelMain.add(panelCenter,java.awt.BorderLayout.CENTER);
        panelFooter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER,20,12));
        btnView.setText("VIEW RECEIPT"); panelFooter.add(btnView);
        btnPrint.setText("PRINT RECEIPT"); panelFooter.add(btnPrint);
        btnDelete.setText("DELETE ORDER"); panelFooter.add(btnDelete);
        btnNewOrder.setText("NEW ORDER"); panelFooter.add(btnNewOrder);
        panelMain.add(panelFooter,java.awt.BorderLayout.SOUTH);
        getContentPane().add(panelMain,java.awt.BorderLayout.CENTER);
        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String[] args) { java.awt.EventQueue.invokeLater(() -> new sales_report().setVisible(true)); }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnGenerate;
    private javax.swing.JButton btnNewOrder;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnView;
    private javax.swing.JSpinner dateFrom;
    private javax.swing.JSpinner dateTo;
    private javax.swing.JLabel lblFrom;
    private javax.swing.JLabel lblItems;
    private javax.swing.JLabel lblOrders;
    private javax.swing.JLabel lblPeriod;
    private javax.swing.JLabel lblSales;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTo;
    private javax.swing.JPanel panelCenter;
    private javax.swing.JPanel panelFilters;
    private javax.swing.JPanel panelFooter;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelRight;
    private javax.swing.JPanel panelStats;
    private javax.swing.JScrollPane scrollReceipt;
    private javax.swing.JScrollPane scrollSales;
    private javax.swing.JTable tblSales;
    private javax.swing.JTextArea txtReceipt;
    // End of variables declaration//GEN-END:variables
}
