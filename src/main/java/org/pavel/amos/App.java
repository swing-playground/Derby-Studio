package org.pavel.amos;

import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.pavel.amos.data.Data;
import org.pavel.amos.db.DataBase;
import org.pavel.amos.enums.ConnectionStatus;

public class App {

	private JFrame frame;
	
	private JTextArea query;
	private DataBase dt;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				App window = new App();
				window.frame.setVisible(true);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private JLabel connectStatus;
	private JButton connectBtn;
	private JSeparator separator_2;
	private DefaultListModel<String> tablesModel;
	private JMenuBar menuBar;
	private JMenu mnAddTable;
	private JMenuItem addTableMenuItem;

	private Data data;
	private JMenuItem removeTableMenuItem;
	private JButton btnRun;
	private JTable table;

	/**
	 * Create the application.
	 * @wbp.parser.entryPoint
	 */
	public App() {
		dt = new DataBase();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		data = new Data();
		tablesModel = new DefaultListModel<>();
		frame = new JFrame();
		frame.setBounds(200, 200, 499, 357);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout
						.createSequentialGroup().addContainerGap().addComponent(panel,
								GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
						.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout
						.createSequentialGroup().addContainerGap().addComponent(panel,
								GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
						.addContainerGap()));

		connectBtn = new JButton("Connect");

		connectStatus = new JLabel("");
		connectStatus.setOpaque(true);
		connectStatus.addPropertyChangeListener(l -> {
			String text = ((JLabel) l.getSource()).getText();
			if (text.length() > 0) {
				connectStatus.setBackground(ConnectionStatus
						.valueOf(((JLabel) l.getSource()).getText()).getStatusColor());
			}
		});

		connectBtn.addActionListener(a -> {
			if (connectBtn.getText().equals("Connect")) {
				if (dt.connect()) {
					connectStatus.setText(ConnectionStatus.OK.toString());
					toggleConnectBtn();
					refreshTables();
					addTableMenuItem.setEnabled(true);
				}
				else {
					connectStatus.setText(ConnectionStatus.FAILED.toString());
				}
			}
			else {
				try {
					dt.closeDatabaseConnection();
					connectStatus.setText("");
					toggleConnectBtn();
					tablesModel.clear();
					addTableMenuItem.setEnabled(false);
					table.setModel(new DefaultTableModel());
					query.setText("");
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		separator_2 = new JSeparator();

		JScrollPane scrollPane = new JScrollPane();

		query = new JTextArea();

		btnRun = new JButton("Run");
		btnRun.addActionListener(a -> {
			String sqlQuery = query.getText();
			if (StringUtils.isNotBlank(sqlQuery)) {
				sqlQuery = sqlQuery.trim().toLowerCase();
				if (sqlQuery.startsWith("select")) {
					Map<String, List<Object>> selectFromDb = dt.selectFromDb(sqlQuery);
					DefaultTableModel tModel = new DefaultTableModel();
					table.setModel(tModel);
					selectFromDb.keySet().stream().forEach(d -> tModel.addColumn(d, selectFromDb.get(d).toArray()));
				}
				else {
					dt.modifyScript(sqlQuery);
					DefaultTableModel tModel = new DefaultTableModel();
					table.setModel(tModel);
				}
			}
		});
		
		JScrollPane scrollPane_1 = new JScrollPane();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
										.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
										.addComponent(query, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)))
								.addComponent(separator_2, GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE))
							.addContainerGap())
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(1)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addGap(108)
									.addComponent(connectStatus))
								.addGroup(gl_panel.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(connectBtn)))
							.addPreferredGap(ComponentPlacement.RELATED, 283, Short.MAX_VALUE)
							.addComponent(btnRun)
							.addGap(14))))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(12)
							.addComponent(connectStatus))
						.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
								.addComponent(connectBtn)
								.addComponent(btnRun))))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(query, GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
							.addGap(6)))
					.addContainerGap())
		);
		
		table = new JTable();
		scrollPane_1.setViewportView(table);

		JList<String> tablesList = new JList<>();
		tablesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablesList.setModel(tablesModel);
		scrollPane.setViewportView(tablesList);
		panel.setLayout(gl_panel);
		frame.getContentPane().setLayout(groupLayout);

		tablesList.addListSelectionListener(
				i -> setSelectedTable(tablesList.getSelectedValue()));

		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		mnAddTable = new JMenu("Tables");
		menuBar.add(mnAddTable);

		addTableMenuItem = new JMenuItem("Add");
		mnAddTable.add(addTableMenuItem);
		addTableMenuItem.setEnabled(false);

		removeTableMenuItem = new JMenuItem("Remove");
		removeTableMenuItem.addActionListener(e -> {
			dt.deleteTable(data.getSelectedTable());
			refreshTables();
		});
		mnAddTable.add(removeTableMenuItem);
		addTableMenuItem.addActionListener(e -> {
			String title = JOptionPane.showInputDialog(frame, "Table title", null);
			dt.createDatabaseTable(title);
			refreshTables();
		});

		mnAddTable.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				removeTableMenuItem.setEnabled(dt.isConnected()
						&& StringUtils.isNotBlank(data.getSelectedTable()));
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
	}

	private void toggleConnectBtn() {
		connectBtn.setText(
				connectBtn.getText().equals("Connect") ? "Disconnect" : "Connect");
	}

	private void refreshTables() {
		tablesModel.clear();
		dt.getTables().stream().forEach(tablesModel::addElement);
	}

	private void setSelectedTable(String item) {
		data.setSelectedTable(item);
	}
}
