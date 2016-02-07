package client;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import shared.Username;

public class LobbyViewer implements LobbyListener {
	private Lobby lobby;
	private Object usersLock = new Object();
	private Username[] users;
	private String[] columnNames = new String[] {"Username", "Address"};
	private JTable table;
	private AbstractTableModel tableModel;
	
	@SuppressWarnings("serial")
	private JPanel createTable() {
		JPanel panel = new JPanel();
		
		tableModel = new AbstractTableModel() {
			@Override
			public Object getValueAt(int row, int column) {
				if (column == 0)
					return users[row];
				else
					return "";
			}
			
			@Override
			public int getRowCount() {
				return users.length;
			}
			
			@Override
			public int getColumnCount() {
				return 2;
			}
			
			@Override
			public String getColumnName(int i) {
				return columnNames[i];
			}
		};
		
		table = new JTable(tableModel);
		panel.setLayout(new BorderLayout());
		panel.add(table.getTableHeader(), BorderLayout.NORTH);
		panel.add(table, BorderLayout.CENTER);
		table.setEnabled(true);
		
		return panel;
	}
	
	private JPanel createButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JButton refresh = new JButton("Refresh");
		panel.add(refresh, BorderLayout.WEST);
		JButton challenge = new JButton("Challenge");
		panel.add(challenge, BorderLayout.CENTER);
		JButton exit = new JButton("Exit");
		panel.add(exit, BorderLayout.EAST);
		
		return panel;
	}
	
	public LobbyViewer(Lobby lobby) {
		this.lobby = lobby;
		lobby.addListener(this);
		users = new Username[0];
		
		JFrame frame = new JFrame("Tic-Tac-Toe lobby");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(300, 300);
		
		JPanel mainPanel = new JPanel();
		JPanel tablePanel = createTable();
		JPanel buttonPanel = createButtons();
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(tablePanel, BorderLayout.NORTH);
		mainPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		frame.add(mainPanel);
		frame.setVisible(true);
	}
	
	private void updateUsers() {
		synchronized (usersLock) {
			Username[] oldUsers = users;
			users = lobby.getUsers();
			Arrays.sort(users);
			
			int newSelection = -1;
			if (table != null) {
				int[] selectedRows = table.getSelectedRows();
				if (selectedRows.length == 1) {
					int row = selectedRows[0];
					Username oldUser = oldUsers[row];
					
					for (int i = 0; i < users.length; i++) {
						if (users[i].equals(oldUser)) {
							newSelection = i;
							break;
						}
					}
				}
			}
			
			if (tableModel != null && !users.equals(oldUsers)) {
				System.out.println("User list changed.");
				TableModelListener[] listeners = tableModel.getTableModelListeners();
				TableModelEvent e = new TableModelEvent(tableModel);
				for (int i = 0; i < listeners.length; i++) {
					listeners[i].tableChanged(e);
				}
				if (newSelection != -1) {
					table.setRowSelectionInterval(newSelection, newSelection);
				}
			}
		}
	}
	
	@Override
	public void usersChanged(Username[] newUsers) {
		updateUsers();
	}
}
