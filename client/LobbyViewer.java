package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import shared.Username;

public class LobbyViewer implements LobbyListener, WindowListener {
	private Lobby lobby;
	private Object usersLock = new Object();
	private ArrayList<Username> users;
	private String[] columnNames = new String[] {"Username", "Address"};
	private JTable table;
	private AbstractTableModel tableModel;
	private Client client;
	
	@SuppressWarnings("serial")
	private JPanel createTable() {
		JPanel panel = new JPanel();
		
		tableModel = new AbstractTableModel() {
			@Override
			public Object getValueAt(int row, int column) {
				if (column == 0)
					return users.get(row);
				else
					return "";
			}
			
			@Override
			public int getRowCount() {
				return users.size();
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
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lobby.interrupt();
			}
		});
		JButton challenge = new JButton("Challenge");
		challenge.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (client.isChallenging()) {
					JOptionPane.showMessageDialog(null,
							"Cannot challenge multiple people at once.", "Challenge Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int row = table.getSelectedRow();
				if (row == -1) {
					JOptionPane.showMessageDialog(null,
							"No row selected.", "Challenge Error", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						client.challenge(users.get(row));
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(null,
								"Connection to server lost: " + ex.getMessage(), "Challenge Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		JButton exit = new JButton("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		panel.add(refresh, BorderLayout.WEST);
		panel.add(challenge, BorderLayout.CENTER);
		panel.add(exit, BorderLayout.EAST);
		
		return panel;
	}
	
	public LobbyViewer(Client client, Lobby lobby) {
		this.client = client;
		this.lobby = lobby;
		lobby.addListener(this);
		users = new ArrayList<Username>();
		
		JFrame frame = new JFrame("Tic-Tac-Toe lobby");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(300, 300);
		frame.addWindowListener(this);
		
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
			ArrayList<Username> oldUsers = users;
			users = lobby.getUsers();
			users.sort(null);
			
			int newSelection = -1;
			if (table != null) {
				int[] selectedRows = table.getSelectedRows();
				if (selectedRows.length == 1) {
					int row = selectedRows[0];
					Username oldUser = oldUsers.get(row);
					
					for (int i = 0; i < users.size(); i++) {
						if (users.get(i).equals(oldUser)) {
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
	public void usersChanged(ArrayList<Username> newUsers) {
		updateUsers();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		System.exit(0);
	}

	@Override
	public void windowClosing(WindowEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowOpened(WindowEvent e) {}
}
