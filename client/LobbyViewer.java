package client;

import java.awt.BorderLayout;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class LobbyViewer implements LobbyListener {
	private Lobby lobby;
	private Object usersLock = new Object();
	private String[][] users;
	private String[] columnNames = new String[] {"Username", "Address"};
	private JTable table;
	private AbstractTableModel tableModel;
	
	@SuppressWarnings("serial")
	public JPanel createTable() {
		JPanel panel = new JPanel();
		
		tableModel = new AbstractTableModel() {
			@Override
			public Object getValueAt(int row, int column) {
				return users[row][column];
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
	
	public LobbyViewer(Lobby lobby) {
		this.lobby = lobby;
		lobby.addListener(this);
		users = new String[0][2];
		
		JFrame frame = new JFrame("Tic-Tac-Toe lobby");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(300, 300);
		
		JPanel panel = createTable();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.NORTH);
		frame.add(Box.createVerticalGlue());
		frame.setVisible(true);
	}
	
	private void updateUsers() {
		synchronized (usersLock) {
			HashMap<String, InetSocketAddress> hmUsers = lobby.getUsers();
			ArrayList<Entry<String, InetSocketAddress>> entries
				= new ArrayList<Entry<String, InetSocketAddress>>(hmUsers.entrySet());
			
			entries.sort(new Comparator<Entry<String, InetSocketAddress>>() {
				@Override
				public int compare(Entry<String, InetSocketAddress> o1, Entry<String, InetSocketAddress> o2) {
					// Negative if the first is less than the second.
					// Sort by usernames
					return o1.getKey().compareToIgnoreCase(o2.getKey());
				}
			});
			
			int size = entries.size();
			String[][] oldUsers = users;
			users = new String[size][2];
			for (int i = 0; i < size; i++) {
				users[i][0] = entries.get(i).getKey();
				users[i][1] = entries.get(i).getValue().toString();
			}
			
			int newSelection = -1;
			if (table != null) {
				int[] selectedRows = table.getSelectedRows();
				if (selectedRows.length == 1) {
					int row = selectedRows[0];
					String oldUser = oldUsers[row][0];
					
					for (int i = 0; i < users.length; i++) {
						if (users[i][0].equalsIgnoreCase(oldUser)) {
							newSelection = i;
							break;
						}
					}
				}
			}
			
			if (tableModel != null && !areEqual(users, oldUsers)) {
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
	
	private boolean areEqual(String[][] o1, String[][] o2) {
		if (o1.length != o2.length)
			return false;
		
		for (int y = 0; y < o1.length; y++) {
			if (o1[y].length != o2[y].length) {
				return false;
			}
			for (int x = 0; x < o1[y].length; x++) {
				if (!o1[y][x].equals(o2[y][x])) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void usersChanged(HashMap<String, InetSocketAddress> newUsers) {
		updateUsers();
	}
}
