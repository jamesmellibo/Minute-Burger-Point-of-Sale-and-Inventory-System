package minute_burger_pos_and_inventory_system;
import minute_burger_inventory_system.DatabaseConnection;

import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Shared checkout for the four POS categories. No inventory deduction occurs on Add to Cart. */
public class MyCart extends JFrame {
    private static final class Item {
        final String name; final double price; int qty;
        Item(String n, double p, int q) { name=n; price=p; qty=q; }
    }
    private static final java.util.List<Item> CART = new ArrayList<>();
    private final JFrame origin;
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Product", "Quantity", "Unit Price", "Subtotal"},0) {
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable table = new JTable(model);
    private final JLabel totalLabel = new JLabel("TOTAL: ₱0.00");
    private final JTextField cash = new JTextField("0",12);
    private final JLabel changeLabel = new JLabel("CHANGE: ₱0.00");
    private final JButton checkout = new JButton("CHECKOUT");
    private boolean busy=false;

    public static synchronized void addProduct(Component parent, String product, double price) {
        String name=normalize(product);
        int existing=0;
        for(Item it:CART) if(it.name.equals(name)) existing+=it.qty;
        int available=SharedData.getMaxBuildableQuantity(name);
        if(available<=existing) {
            JOptionPane.showMessageDialog(parent,"Insufficient ingredients for " + name + ".\n" + SharedData.getMissingIngredients(name,existing+1),"Inventory Guard",JOptionPane.WARNING_MESSAGE);
            return;
        }
        String answer=JOptionPane.showInputDialog(parent,"Product: "+name+"\nAvailable to add: "+(available-existing)+"\nQuantity:","1");
        if(answer==null)return;
        try {
            int qty=Integer.parseInt(answer.trim());
            if(qty<=0||qty>available-existing)throw new NumberFormatException();
            for(Item it:CART) if(it.name.equals(name)&&Double.compare(it.price,price)==0){it.qty+=qty;JOptionPane.showMessageDialog(parent,"Added to My Cart (stock not yet deducted).");return;}
            CART.add(new Item(name,price,qty));
            JOptionPane.showMessageDialog(parent,"Added to My Cart (stock not yet deducted).");
        } catch(NumberFormatException ex){JOptionPane.showMessageDialog(parent,"Enter a positive quantity within available stock.");}
    }
    private static String normalize(String s) {
        String v=s.trim().toUpperCase(java.util.Locale.ROOT);
        if(v.equals("CHESSY DOG"))return "CHEESY DOG";
        if(v.equals("BACON CHEASE BURGER"))return "BACON CHEESE BURGER";
        if(v.equals("CHILI CON CARNE CHEEKS FRANKS"))return "CHILI CON CARNE CHEESE FRANKS";
        if(v.equals("LEMON BLACK"))return "LEMON BLACK TEA";
        if(v.equals("FRUTWIST 3.0"))return "FRUITWIST 3.0";
        return v;
    }
    public MyCart(JFrame origin) {
        this.origin=origin;
        PresentationUI.fullscreen(this);
        setTitle("MINUTE BURGER | MY CART"); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(850,550));
        Color orange=new Color(239,103,27),dark=new Color(38,45,52);
        JPanel root=new JPanel(new BorderLayout(16,16));root.setBackground(Color.WHITE);root.setBorder(BorderFactory.createEmptyBorder(20,25,20,25));setContentPane(root);
        JLabel title=new JLabel("MY CART  |  ORDER CHECKOUT");title.setFont(new Font("Segoe UI",Font.BOLD,28));title.setForeground(dark);root.add(title,BorderLayout.NORTH);
        table.setRowHeight(38);table.setFont(new Font("Segoe UI",Font.PLAIN,17));table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,17));
        root.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(8,12));bottom.setBackground(Color.WHITE);
        JPanel amounts=new JPanel(new GridLayout(2,3,15,8));amounts.setBackground(Color.WHITE);
        totalLabel.setFont(new Font("Segoe UI",Font.BOLD,25));totalLabel.setForeground(orange);
        changeLabel.setFont(new Font("Segoe UI",Font.BOLD,22));
        JLabel cashLabel=new JLabel("CASH RECEIVED (₱)");cashLabel.setFont(new Font("Segoe UI",Font.BOLD,17));
        cash.setFont(new Font("Segoe UI",Font.PLAIN,21));
        amounts.add(totalLabel);amounts.add(cashLabel);amounts.add(cash);amounts.add(changeLabel);amounts.add(new JLabel(""));amounts.add(new JLabel(""));
        bottom.add(amounts,BorderLayout.NORTH);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,8));buttons.setBackground(Color.WHITE);
        JButton update=new JButton("UPDATE QTY"),remove=new JButton("REMOVE ITEM"),clear=new JButton("CLEAR CART"),back=new JButton("BACK TO POS");
        for(JButton b:new JButton[]{update,remove,clear,back,checkout}){b.setFont(new Font("Segoe UI",Font.BOLD,15));b.setPreferredSize(new Dimension(165,48));buttons.add(b);}
        checkout.setBackground(orange);checkout.setForeground(Color.WHITE);
        bottom.add(buttons,BorderLayout.SOUTH);root.add(bottom,BorderLayout.SOUTH);
        cash.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){calculateChange();}public void removeUpdate(javax.swing.event.DocumentEvent e){calculateChange();}public void changedUpdate(javax.swing.event.DocumentEvent e){calculateChange();}});
        update.addActionListener(e->updateQty());remove.addActionListener(e->removeItem());clear.addActionListener(e->{if(JOptionPane.showConfirmDialog(this,"Clear all items?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){CART.clear();refresh();}});
        back.addActionListener(e->dispose());checkout.addActionListener(e->checkout());refresh();
    }
    private double total(){double t=0;for(Item i:CART)t+=i.price*i.qty;return t;}
    private void refresh(){model.setRowCount(0);for(Item i:CART)model.addRow(new Object[]{i.name,i.qty,String.format("₱%.2f",i.price),String.format("₱%.2f",i.price*i.qty)});totalLabel.setText(String.format("TOTAL: ₱%.2f",total()));calculateChange();}
    private void calculateChange(){try{double c=Double.parseDouble(cash.getText().trim());changeLabel.setText(String.format("CHANGE: ₱%.2f",Math.max(0,c-total())));}catch(Exception ex){changeLabel.setText("CHANGE: —");}}
    private void updateQty(){int row=table.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Select a product first.");return;}Item it=CART.get(table.convertRowIndexToModel(row));String s=JOptionPane.showInputDialog(this,"New quantity for "+it.name,it.qty);if(s==null)return;try{int q=Integer.parseInt(s.trim());if(q<1||q>SharedData.getMaxBuildableQuantity(it.name))throw new NumberFormatException();it.qty=q;refresh();}catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"Invalid quantity or insufficient stock.");}}
    private void removeItem(){int row=table.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Select a product first.");return;}CART.remove(table.convertRowIndexToModel(row));refresh();}
    private static void ingredient(Map<String,Integer> m,String n,int q){m.put(n,m.getOrDefault(n,0)+q);}
    private static Map<String,Integer> recipe(String name,int qty){
        Map<String,Integer> m=new LinkedHashMap<>();
        switch(name){
            case "MINUTE BURGER":ingredient(m,"Burger Bun",2*qty);ingredient(m,"Beef Patty",qty);ingredient(m,"Dressing / Mayo",qty);break;
            case "DOUBLE MINUTE BURGER":ingredient(m,"Burger Bun",2*qty);ingredient(m,"Beef Patty",2*qty);ingredient(m,"Dressing / Mayo",qty);break;
            case "BACON CHEESE BURGER":case "BACON PIZZA BURGER":ingredient(m,"Burger Bun",2*qty);ingredient(m,"Beef Patty",2*qty);ingredient(m,"Pizza Sauce",qty);ingredient(m,"Cheese Slice",qty);ingredient(m,"Bacon Strips",qty);break;
            case "ROASTED SESAME CHICKEN BURGER":ingredient(m,"Burger Bun",2*qty);ingredient(m,"Chicken Patty",2*qty);ingredient(m,"Sesame Onion Sauce",qty);break;
            case "CHICKEN SISIG BURGER":ingredient(m,"Burger Bun",2*qty);ingredient(m,"Chicken Patty",2*qty);ingredient(m,"Chimichurri & Mushroom",qty);break;
            case "CHICKEN TIME BURGER":ingredient(m,"Burger Bun",2*qty);ingredient(m,"Chicken Patty",2*qty);ingredient(m,"Creamy Dressing",qty);break;
            case "CHICKEN TIME + DRINK":case "DOUBLE CHICKEN TIME + DRINK":int mul=name.startsWith("DOUBLE")?2:1;ingredient(m,"Burger Bun",2*qty*mul);ingredient(m,"Chicken Patty",2*qty*mul);ingredient(m,"Creamy Dressing",qty*mul);ingredient(m,"Juice / Beverage Mix",qty);ingredient(m,"Drink Cup",qty);ingredient(m,"Straw",qty);break;
            case "FRENCH ONION FRANKS":ingredient(m,"Hotdog Bun",2*qty);ingredient(m,"Hotdog Frank",2*qty);ingredient(m,"Sesame Onion Sauce",qty);break;
            case "CHILI CON CARNE CHEESE FRANKS":ingredient(m,"Hotdog Bun",2*qty);ingredient(m,"Hotdog Frank",2*qty);ingredient(m,"Chili Con Carne Sauce",qty);ingredient(m,"Cheese Sauce",qty);break;
            case "CHEESY DOG":ingredient(m,"Hotdog Bun",2*qty);ingredient(m,"Hotdog Frank",2*qty);ingredient(m,"Cheese Sauce",qty);break;
            case "FRUITWIST 3.0":ingredient(m,"Juice / Beverage Mix",qty);ingredient(m,"Drink Cup",qty);ingredient(m,"Straw",qty);break;
            case "LEMON BLACK TEA":ingredient(m,"Lemon Black Tea Mix",qty);ingredient(m,"Drink Cup",qty);ingredient(m,"Straw",qty);break;
            case "HOT CHOCO MALLOW":ingredient(m,"Choco Mix",qty);ingredient(m,"Marshmallows",qty);ingredient(m,"Drink Cup",qty);break;
            case "ICED CHOCO":ingredient(m,"Choco Mix",qty);ingredient(m,"Drink Cup",qty);ingredient(m,"Straw",qty);break;
            case "HOT COFFEE":ingredient(m,"Coffee Mix",qty);ingredient(m,"Drink Cup",qty);break;
            case "MINERAL WATER":ingredient(m,"Bottled Water",qty);break;
            default:throw new IllegalArgumentException("No inventory recipe defined for: "+name);
        }
        return m;
    }
    private void checkout(){
        if(busy)return;
        if(CART.isEmpty()){JOptionPane.showMessageDialog(this,"Cart is empty.");return;}
        double received;
        try{received=Double.parseDouble(cash.getText().trim());if(!Double.isFinite(received)||received<total())throw new NumberFormatException();}
        catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"Cash received must be at least the total amount.");return;}
        if(JOptionPane.showConfirmDialog(this,String.format("Confirm payment of ₱%.2f?",total()),"Checkout",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        busy=true;checkout.setEnabled(false);
        String orderId="MB-"+System.currentTimeMillis()+"-"+UUID.randomUUID().toString().substring(0,6).toUpperCase();
        String date=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String cashier=SharedData.currentUserName==null||SharedData.currentUserName.trim().isEmpty()?"Staff":SharedData.currentUserName;
        StringBuilder items=new StringBuilder();Map<String,Integer> needs=new LinkedHashMap<>();
        try {
            for(Item it:CART){if(items.length()>0)items.append(", ");items.append(it.name).append(" (x").append(it.qty).append(") - ₱").append(String.format(java.util.Locale.US,"%.2f",it.qty*it.price));for(Map.Entry<String,Integer> e:recipe(it.name,it.qty).entrySet())ingredient(needs,e.getKey(),e.getValue());}
            try(Connection con=DatabaseConnection.getConnection()){
                SharedData.ensureOrderPaymentsTable(con);
                con.setAutoCommit(false);
                try {
                    // Conditional updates protect against concurrent sales and shared ingredients.
                    for(Map.Entry<String,Integer> e:needs.entrySet()){
                        try(PreparedStatement ps=con.prepareStatement("UPDATE inventory SET quantity=quantity-? WHERE UPPER(TRIM(ingredient_name))=UPPER(?) AND quantity>=?")){
                            ps.setInt(1,e.getValue());ps.setString(2,e.getKey());ps.setInt(3,e.getValue());
                            if(ps.executeUpdate()!=1)throw new SQLException("Insufficient/missing stock: "+e.getKey());
                        }
                    }
                    try(PreparedStatement ps=con.prepareStatement("INSERT INTO orders (order_id,items,total_price,cashier,date_time) VALUES (?,?,?,?,?)")){
                        ps.setString(1,orderId);ps.setString(2,items.toString());ps.setString(3,String.format(java.util.Locale.US,"%.2f",total()));ps.setString(4,cashier);ps.setString(5,date);if(ps.executeUpdate()!=1)throw new SQLException("Sales record was not saved.");
                    }
                    try (PreparedStatement payment = con.prepareStatement(
                            "INSERT INTO order_payments (order_id, payment_method, cash_received, change_amount) VALUES (?, ?, ?, ?)")) {
                        payment.setString(1, orderId);
                        payment.setString(2, "CASH");
                        payment.setBigDecimal(3, java.math.BigDecimal.valueOf(received).setScale(2, java.math.RoundingMode.HALF_UP));
                        payment.setBigDecimal(4, java.math.BigDecimal.valueOf(received - total()).setScale(2, java.math.RoundingMode.HALF_UP));
                        if (payment.executeUpdate() != 1) throw new SQLException("Payment details were not saved.");
                    }
                    con.commit();
                }catch(Exception ex){con.rollback();throw ex;}finally{con.setAutoCommit(true);}
            }
            StringBuilder receiptBuilder = new StringBuilder();
            receiptBuilder.append("             MINUTE BURGER\n")
                    .append("              SALES RECEIPT\n")
                    .append("========================================\n")
                    .append("Receipt No : ").append(orderId).append('\n')
                    .append("Date       : ").append(date).append('\n')
                    .append("Cashier    : ").append(cashier).append('\n')
                    .append("Payment    : CASH\n")
                    .append("----------------------------------------\n")
                    .append(String.format(java.util.Locale.US, "%-19s %3s %9s\n", "ITEM", "QTY", "AMOUNT"))
                    .append("----------------------------------------\n");
            for (Item it : CART) {
                receiptBuilder.append(it.name).append('\n')
                        .append(String.format(java.util.Locale.US, "  %d x PHP %.2f %19s\n", it.qty, it.price,
                                String.format(java.util.Locale.US, "%.2f", it.qty * it.price)));
            }
            receiptBuilder.append("----------------------------------------\n")
                    .append(String.format(java.util.Locale.US, "TOTAL          PHP %12.2f\n", total()))
                    .append(String.format(java.util.Locale.US, "CASH RECEIVED  PHP %12.2f\n", received))
                    .append(String.format(java.util.Locale.US, "CHANGE         PHP %12.2f\n", received - total()))
                    .append("========================================\n")
                    .append("       THANK YOU FOR YOUR PURCHASE!\n")
                    .append("            PLEASE COME AGAIN\n");
            String receipt = receiptBuilder.toString();
            CART.clear();refresh();JTextArea ta=new JTextArea(receipt,18,42);ta.setEditable(false);ta.setFont(new Font("Monospaced",Font.PLAIN,15));JOptionPane.showMessageDialog(this,new JScrollPane(ta),"Checkout Successful",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception ex){JOptionPane.showMessageDialog(this,"Checkout NOT completed. No stock/order changes committed.\n"+ex.getMessage(),"Checkout Error",JOptionPane.ERROR_MESSAGE);}
        finally{busy=false;checkout.setEnabled(true);}
    }
}
