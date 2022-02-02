/*
 * Creator: Noam Shevach
 * Date: 8.7.2021
 * 
 * This class display a windows which contains details about the password score.
 * */
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PasswordStrengthForm extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private DefaultTableModel model;
	private PasswordStrength p;

	public PasswordStrengthForm(PasswordStrength p) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		this.p = p;
		JTable table = new JTable();
		Object[] columns = {"Status", "Description", "Rate", "Count", "Bonus"};
		this.model = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		this.getContentPane().setBackground(new Color(0,0,0));
		this.getContentPane().setForeground(Color.WHITE);
		this.setBounds(100,100,675,555);
		this.setLocationRelativeTo(null);
		
		model.setColumnIdentifiers(columns);
		table.setModel(model);
		
		table.setBackground(Color.white);
		table.setForeground(Color.black);
		table.setSelectionBackground(new Color(0,0,192));
		table.setGridColor(Color.GRAY);
		table.setSelectionForeground(Color.WHITE);
		table.setFont(new Font("Tahoma", Font.PLAIN, 17));
		table.setRowHeight(30);
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		table.getColumnModel().getColumn(1).setPreferredWidth(300);
		table.getColumnModel().getColumn(2).setMinWidth(120);
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		for(int i = 0; i < 5; i++) 
			table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBackground(Color.WHITE);
		pane.setBounds(10,10,650,505);
		
		this.add(pane);
		
		addRows();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle("Password Strength Details");
		this.setResizable(false);
		this.setLayout(null);
	}
	
	
	/*
	 * This function adds rows to the table.
	 * */
	private void addRows() {
		String[] descriptions= {"Number of Characters", "Uppercase Letters", "Lowercase Letters"
				, "Numbers", "Symbols", "Middle Numbers or Symbols", "Requirements",
				"Letters Only", "Numbers Only", "Repeat Characters(Case Insensitive)",
				"Consecutive Uppercase Letters", "Consecutive Lowercase Letters",
				"Consecutive Numbers", "Sequential Letters(3+)", "Sequential Numbers(3+)",
				"Sequential Symbols(3+)"};
		String[] rate = {"+(n*4)", "+((len-n)*2)", "+((len-n)*2)", "+(n*4)",
				"+(n*6)", "+(n*2)", "+(n*2)", "-n", "-n", "", "-(n*2)", "-(n*2)",
				"-(n*2)", "-(n*3)", "-(n*3)", "-(n*3)"};
		String[] count = p.getCountColumn();
		String[] status = p.getStatusColumn();
		String[] bonus = p.getBonusColumn();
		
		Object[] row = new Object[5];
		
		for(int i = 0; i < 16; i++) {
			row[0] = status[i];
			row[1] = descriptions[i];
			row[2] = rate[i];
			row[3] = count[i];
			row[4] = bonus[i];
			model.addRow(row);
		}
	}
	
	/*
	 * This fucntion updates the rows with new values.
	 * */
	public void updateRows() {
		String[] count = p.getCountColumn();
		String[] status = p.getStatusColumn();
		String[] bonus = p.getBonusColumn();
		for(int i = 0; i < 16; i++) {
			model.setValueAt(status[i], i, 0);
			model.setValueAt(count[i], i, 3);
			model.setValueAt(bonus[i], i, 4);
		}
	}

}
