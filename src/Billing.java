import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Billing{
	private static Connection getDatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory", "root", "Vishnu@tj");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
	
	public static ResultSet getProductdetails(Connection con,String itemid) throws SQLException{
		String sql="SELECT * FROM productdetails WHERE item_id = ?";
		PreparedStatement stmt=con.prepareStatement(sql);
		stmt.setString(1,itemid);
		return stmt.executeQuery();
	}
	
	public static int getGstRate(Connection con,String type) throws SQLException {
		String gstQuery= "SELECT GST FROM GST WHERE type = ?";
		PreparedStatement psGst=con.prepareStatement(gstQuery);
		psGst.setString(1, type);
		ResultSet rsGST=psGst.executeQuery();
		 if (rsGST.next()) {
	            return rsGST.getInt("GST");
	        } else {
	            return 0; 
	        }
		
	}
	
	public static String getOffer(Connection con,String itemid) throws SQLException {
		String offerQuery= "SELECT offer FROM Offer WHERE item_id = ?";
		PreparedStatement psOffer=con.prepareStatement(offerQuery);
		psOffer.setString(1, itemid);
		ResultSet rsOffer=psOffer.executeQuery();
		 if (rsOffer.next()) {
	            return rsOffer.getString("offer");
	        } else {
	            return "no offer available"; 
	        }
		
	}
	
	public static double[] calculateFinalPrice(int itemPrice,String offer,int gstRate) {
		double discount=0;
		if (offer.equals("10% off")) {
            discount = 0.10;
        } else if (offer.equals("5% off")) {
            discount = 0.05;
        } else if (offer.equals("3% off")) {
            discount = 0.03;
        }

        double discountedPrice = itemPrice - (itemPrice * discount);
        double gstAmount = discountedPrice * gstRate / 100;
        double finalAmount = discountedPrice + gstAmount;
        
        return new double[] { discountedPrice, gstAmount, finalAmount };
   }
	
	public static void getBillingDetails(String itemid,int quantitySold) throws SQLException {
		Connection con =getDatabaseConnection();
		ResultSet rsProduct=getProductdetails(con, itemid);
		if (rsProduct.next()) {
			String itemName = rsProduct.getString("item_name");
            String type = rsProduct.getString("type");
            int itemPrice = rsProduct.getInt("item_price");
            int availableQuantity = rsProduct.getInt("item_quantity");
            System.out.println("Product: " + itemName);
            System.out.println("Price: ₹" + itemPrice);
            int gstRate = getGstRate(con, type);
            System.out.println("GST: " + gstRate + "%");
            String offer = getOffer(con, itemid);
            System.out.println("Offer: " + offer);
            
            double[] finalprices=calculateFinalPrice(itemPrice,offer,gstRate);
            double discountedPrice=finalprices[0];
            double gstAmount=finalprices[1];
            double finalAmount=finalprices[2];
            System.out.println("Discounted Price: ₹" + discountedPrice);
            System.out.println("GST AMOUNT: ₹" + gstAmount);
            System.out.println("FinalAmount: ₹" + finalAmount);
            
            if(availableQuantity >= quantitySold) {
            	double totalAmount=finalAmount*quantitySold;
            	System.out.println("Total Amount:" +totalAmount);
            	
            }else {
            	System.out.println("Inefficient Stock");
            }
            updateStock(con,itemid,quantitySold);
            
            
            
		}else {
			System.out.println("product not found with itemid"+itemid);
		}
		con.close();
	
		
	}
	
	
	public static void updateStock(Connection con,String itemid,int quantitySold) throws SQLException {
		String updateQuery="UPDATE productdetails SET item_quantity=item_quantity - ? WHERE item_id= ?";
		PreparedStatement psUpdate=con.prepareStatement(updateQuery);
		psUpdate.setInt(1,quantitySold);
		psUpdate.setString(2,itemid);
		psUpdate.executeUpdate();
	}
	public static void main(String[] args) {
		Scanner scanner=new Scanner(System.in);
		 System.out.print("Enter item ID: ");
		 String itemid = scanner.nextLine();
		 System.out.print("Enter quantity sold: ");
		 int quantitySold = scanner.nextInt();
		 try {
			getBillingDetails(itemid, quantitySold);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		 
		 
		 
		
		
	}
}


