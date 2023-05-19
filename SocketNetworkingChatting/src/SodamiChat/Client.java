package chatting;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import chatting.Server.RoomInformation;

public class Client extends JFrame implements ActionListener, KeyListener {

	// 메인 패널
	private JPanel mainPanel;

	// 로그인 탭
	private JPanel loginPanel;
	private JLabel iconLabel;
	private JLabel hostIPLabel;
	private JLabel serverPortLabel;
	private JLabel userIDLabel;
	private JTextField hostIPTextField;
	private JTextField serverPortTextField;
	private JTextField userIDTextField;
	private JButton connectButton;

	private JPanel waitingRoomPanel;
	private JScrollPane roomListScroll;
	private JPanel userListPanel;
	private JPanel bottomPanel;
	private JLabel currentUserLabel;
	private JList totalUserList; // 전체접속자 리스트
	private JScrollPane userListScroll;
	private JLabel totalRoomLabel;
	private JList totalRoomList; // 방 리스트
	private JButton sendNoteButton;
	private JButton joinRoomButton;
	private JButton createRoomButton;

	private JPanel chattingPanel;
	private JScrollPane chattingScroll;
	private JTextArea viewChatTextArea;
	private JTextField chattingTextField;
	private JButton sendMessageButton;
	private JButton leaveRoomButton;
	private JButton backButton;
	private ImageIcon backbuttonImageIcon;
	private ImageIcon coloredIcon;

	// 네트워크
	private Socket socket;
	private String ip;
	private int port;
	private String userId;
	private InputStream inputStream;
	private OutputStream outputStream;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;

	private Vector<String> userVectorList = new Vector<String>();
	private Vector<String> roomVectorList = new Vector<String>();
	private List<String> myRoomNameList = new ArrayList<String>();
	private String myCurrentRoomName = "";
	private boolean isUserIdvalidationOK = false;

	private Stack<JPanel> panelStack = new Stack<JPanel>();

	public Client() {
		init();
		serverPortTextField.setText("1");
		userIDTextField.setText("user1");
		hostIPTextField.setText("127.0.0.1");
		addListener();
	}

	private void init() {

		// 메인 패널
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 400, 520);
		// setUndecorated(true);
		// 아이콘 설정 및 크기 조정
		ImageIcon icon = new ImageIcon("images/icon_bee.png");
		Image img = icon.getImage();
		Image scaledImg = img.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
		icon = new ImageIcon(scaledImg);
		setIconImage(icon.getImage());

		mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(null);
		mainPanel.setBackground(Color.white);
		setContentPane(mainPanel);
		setResizable(false);

		loginPanel = new JPanel();
		loginPanel.setLayout(null);
		loginPanel.setBackground(Color.white);
		loginPanel.setBounds(0, 0, 400, 482);
		switchToTopPanel(loginPanel);
		// switchPanel(loginPanel);

		iconLabel = new JLabel();
		iconLabel.setIcon(new ImageIcon("images/Icon_YouchaeTalk.png"));
		iconLabel.setLayout(null);
		iconLabel.setBounds(84, 40, 232, 232);
		loginPanel.add(iconLabel);

		hostIPLabel = new JLabel("SERVER_IP ");
		hostIPLabel.setForeground(new Color(15, 64, 41));
		hostIPLabel.setBounds(70, 305, 110, 15);
		loginPanel.add(hostIPLabel);

		hostIPTextField = new JTextField();
		hostIPTextField.setBounds(180, 305, 150, 20);
		hostIPTextField.setColumns(10);
		loginPanel.add(hostIPTextField);

		serverPortLabel = new JLabel("SERVER_PORT");
		serverPortLabel.setBounds(70, 340, 110, 15);
		serverPortLabel.setForeground(new Color(15, 64, 41));
		loginPanel.add(serverPortLabel);

		serverPortTextField = new JTextField();
		serverPortTextField.setBounds(180, 340, 150, 20);
		serverPortTextField.setColumns(10);
		loginPanel.add(serverPortTextField);

		userIDLabel = new JLabel("USER_ID");
		userIDLabel.setBounds(70, 375, 110, 15);
		userIDLabel.setForeground(new Color(15, 64, 41));
		loginPanel.add(userIDLabel);

		userIDTextField = new JTextField();
		userIDTextField.setBounds(180, 375, 150, 20);
		userIDTextField.setColumns(10);
		loginPanel.add(userIDTextField);

		connectButton = new JButton("CONNECT");
		connectButton.setBackground(new Color(251, 225, 61));
		connectButton.setForeground(new Color(15, 64, 41));
		connectButton.setBounds(140, 420, 120, 25);
		loginPanel.add(connectButton);

//			JLabel img_lbl = new JLabel("input the image");
//			img_lbl.setIcon(new ImageIcon());
//			img_lbl.setBounds(12, 213, 299, 155);
//			panel_1.add(img_lbl);

////////////////////////////////////////////////////////////////			

		waitingRoomPanel = new JPanel();
		waitingRoomPanel.setLayout(null);
		waitingRoomPanel.setBackground(new Color(255, 255, 255));
		waitingRoomPanel.setBounds(0, 0, 400, 482);

		// bottomPanel
		bottomPanel = new JPanel();
		bottomPanel.setBackground(new Color(249, 249, 249));
		bottomPanel.setBounds(127, 384, 273, 98);
		bottomPanel.setLayout(null);
		waitingRoomPanel.add(bottomPanel);
		joinRoomButton = new JButton("Enter Room");
		joinRoomButton.setBounds(135, 24, 102, 23);
		joinRoomButton.setBorder(null);
		joinRoomButton.setBackground(new Color(212, 212, 212));
		joinRoomButton.setForeground(new Color(90, 90, 90));
		joinRoomButton.setEnabled(false);
		bottomPanel.add(joinRoomButton);
		// userListPanel
		userListPanel = new JPanel();
		userListPanel.setBackground(new Color(236, 236, 236));
		userListPanel.setBounds(0, 0, 127, 480);
		userListPanel.setLayout(null);
		waitingRoomPanel.add(userListPanel);

		currentUserLabel = new JLabel();
		currentUserLabel.setBounds(37, 100, 60, 20);
		setFont(currentUserLabel, Font.PLAIN, 18f);
		userListPanel.add(currentUserLabel);

		totalUserList = new JList();
		totalUserList.setBounds(0, 0, 90, 257);
		totalUserList.setBackground(new Color(236, 236, 236));
		totalUserList.setSelectionBackground(null);
		totalUserList.setSelectionForeground(new Color(150, 150, 150));
		setFont(totalUserList, Font.PLAIN, 14f);
		DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				label.setHorizontalAlignment(SwingConstants.LEFT);
				label.setBorder(new EmptyBorder(10, 0, 10, 10));
				return label;
			}
		};

		totalUserList.setCellRenderer(renderer);
		userListPanel.add(totalUserList);

		userListScroll = new JScrollPane();
		userListScroll.setBounds(7, 130, 90, 250);
		userListScroll.setBorder(null);
		userListScroll.setBackground(Color.white);
		JPanel contentPane = new JPanel();
		JViewport jViewport = new JViewport();
		jViewport.setView(contentPane);
		userListScroll.setViewport(jViewport);
		contentPane.add(totalUserList);
		userListPanel.add(userListScroll);
		userListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// userListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		userListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		// userListScroll.getVerticalScrollBar().setEnabled(false);
		// 컨텐트 패널에 대한 MouseWheelListener를 추가합니다.
		userListScroll.getViewport().getView().addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				JViewport viewport = userListScroll.getViewport();
				int scrollAmount = e.getWheelRotation();

				// 스크롤이 더 이상 올라가지 않도록 조절합니다.
				if (scrollAmount < 0 && viewport.getViewPosition().y == 0) {
					return; // 컨텐트 패널이 가장 위에 있는 경우, 더 이상 스크롤을 올리지 않습니다.
				}

				// 스크롤 이벤트에 따라 컨텐트 패널을 스크롤하는 코드를 작성합니다.
				Point viewPosition = viewport.getViewPosition();
				viewPosition.translate(0, scrollAmount * 10); // 스크롤 속도 조정을 위해 값을 조정합니다.
				viewport.setViewPosition(viewPosition);
			}
		});

		sendNoteButton = new JButton("Send Message");
		sendNoteButton.setBounds(7, 407, 112, 23);
		sendNoteButton.setBorder(null);
		sendNoteButton.setBackground(new Color(212, 212, 212));
		sendNoteButton.setForeground(new Color(90, 90, 90));
		setFont(sendNoteButton, Font.PLAIN, 14f);
		userListPanel.add(sendNoteButton);

		totalRoomLabel = new JLabel("채팅");
		totalRoomLabel.setHorizontalAlignment(SwingConstants.CENTER);
		totalRoomLabel.setBounds(148, 63, 40, 17);
		waitingRoomPanel.add(totalRoomLabel);
		setFont(totalRoomLabel, Font.BOLD, 16f);

		totalRoomList = new JList();
		totalRoomList.setBackground(Color.white);
		totalRoomList.setBounds(0, 0, 200, 257);
		totalRoomList.setBorder(null);
		totalRoomList.setFocusable(false);

		totalRoomList.setSelectionBackground(null);
		totalRoomList.setSelectionForeground(new Color(150, 150, 150));
		setFont(totalRoomList, Font.PLAIN, 16f);

		roomListScroll = new JScrollPane();
		roomListScroll.setBounds(150, 100, 230, 285);
		roomListScroll.setBackground(Color.white);
		roomListScroll.setBorder(null);
		waitingRoomPanel.add(roomListScroll);

		JViewport roomListViewport = roomListScroll.getViewport();
		roomListViewport.setBackground(Color.white);
		roomListViewport.add(totalRoomList);

		roomListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// roomListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		roomListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		// roomListScroll.getVerticalScrollBar().setEnabled(false);
		// 컨텐트 패널에 대한 MouseWheelListener를 추가합니다.
		roomListScroll.getViewport().getView().addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				JViewport viewport = roomListScroll.getViewport();
				int scrollAmount = e.getWheelRotation();

				// 스크롤이 더 이상 올라가지 않도록 조절합니다.
				if (scrollAmount < 0 && viewport.getViewPosition().y == 0) {
					return; // 컨텐트 패널이 가장 위에 있는 경우, 더 이상 스크롤을 올리지 않습니다.
				}

				// 스크롤 이벤트에 따라 컨텐트 패널을 스크롤하는 코드를 작성합니다.
				Point viewPosition = viewport.getViewPosition();
				viewPosition.translate(0, scrollAmount * 10); // 스크롤 속도 조정을 위해 값을 조정합니다.
				viewport.setViewPosition(viewPosition);
			}
		});

		createRoomButton = new JButton("+");
		createRoomButton.setBounds(330, 20, 35, 35);
		createRoomButton.setFont(new Font("Dongle", Font.BOLD, 25));
		createRoomButton.setBackground(new Color(255, 255, 255));
		createRoomButton.setForeground(new Color(69, 69, 69));
		createRoomButton.setBorder(null);
		createRoomButton.setFocusPainted(false);
		createRoomButton.setRolloverEnabled(false);
		createRoomButton.setContentAreaFilled(false);
		waitingRoomPanel.add(createRoomButton);

//////////////////////////////////////////////////////////////////////			

		// 채팅 탭
		chattingPanel = new JPanel();
		chattingPanel.setLayout(null);
		chattingPanel.setBackground(new Color(74, 43, 0));
		chattingPanel.setBounds(0, 0, 400, 482);

		viewChatTextArea = new JTextArea();
		viewChatTextArea.setEnabled(false);
		viewChatTextArea.setEditable(false);
		viewChatTextArea.setFont(new Font("Dongle", Font.BOLD, 12));
		viewChatTextArea.setBounds(10, 60, 362, 380);
		chattingPanel.add(viewChatTextArea);

		chattingTextField = new JTextField();
		chattingTextField.setFont(new Font("Dongle", Font.BOLD, 11));
		chattingTextField.setBounds(10, 450, 280, 21);
		chattingTextField.setColumns(10);
		chattingPanel.add(chattingTextField);

		sendMessageButton = new JButton("전 송");
		sendMessageButton.setFont(new Font("Dongle", Font.BOLD, 12));
		sendMessageButton.setBounds(300, 450, 72, 23);
		sendMessageButton.setBackground(new Color(255, 223, 136));
		sendMessageButton.setForeground(new Color(15, 64, 41));
		chattingPanel.add(sendMessageButton);

		chattingScroll = new JScrollPane();
		chattingPanel.add(chattingScroll);

		leaveRoomButton = new JButton("방 나가기");
		leaveRoomButton.setFont(new Font("Dongle", Font.BOLD, 11));
		leaveRoomButton.setBounds(275, 22, 97, 27);
		leaveRoomButton.setEnabled(false);
		leaveRoomButton.setBackground(new Color(255, 223, 136));
		leaveRoomButton.setForeground(new Color(15, 64, 41));
		chattingPanel.add(leaveRoomButton);

		backbuttonImageIcon = new ImageIcon("images/arrow.png");
		coloredIcon = new ImageIcon("images/coloredArrow.png");

		backButton = new JButton(backbuttonImageIcon);
		backButton.setBounds(10, 21, 30, 30);
		backButton.setBorderPainted(false);
		backButton.setContentAreaFilled(false);
		chattingPanel.add(backButton);

		setVisible(true);

	}

	private void addListener() {
		connectButton.addActionListener(this);
		sendMessageButton.addActionListener(this);
		sendNoteButton.addActionListener(this);
		joinRoomButton.addActionListener(this);
		chattingTextField.addActionListener(this);
		chattingTextField.addKeyListener(this);
		createRoomButton.addActionListener(this);
		leaveRoomButton.addActionListener(this);
		backButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				backButton.setIcon(coloredIcon);
				removeTopPanel();
				backButton.setIcon(backbuttonImageIcon);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				backButton.setIcon(coloredIcon);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				backButton.setIcon(backbuttonImageIcon);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				backButton.setIcon(coloredIcon);

			}

			@Override
			public void mouseExited(MouseEvent e) {
				backButton.setIcon(backbuttonImageIcon);
			}

		});
		totalRoomList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (totalRoomList.getSelectedIndex() != -1) {
						joinRoomButton.setEnabled(true); // 선택된 요소가 있으면 버튼 활성화
					} else {
						joinRoomButton.setEnabled(false); // 선택된 요소가 없으면 버튼 비활성화
					}
				}
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == connectButton) {
			System.out.println("connectButton Click");
			if (hostIPTextField.getText().length() == 0) {
				hostIPTextField.setText("IP를 입력하세요");
				hostIPTextField.requestFocus();
			} else if (serverPortTextField.getText().length() == 0) {
				serverPortTextField.setText("포트번호를 입력하세요");
				serverPortTextField.requestFocus();
			} else if (userIDTextField.getText().length() == 0) {
				setTitle("ID를 입력하세요.");
				userIDTextField.requestFocus();
			} else {
				ip = hostIPTextField.getText();
				try {
					port = Integer.parseInt(serverPortTextField.getText().trim());
					userId = userIDTextField.getText().trim();
					currentUserLabel.setText(userId);

					try {
						connectServer();

					} catch (Exception e2) {
						setTitle("서버와의 연결이 필요합니다.");
					}
				} catch (Exception e2) {
					serverPortTextField.setText("잘못 입력하였습니다.");
				}

			}

		} else if (e.getSource() == sendNoteButton) {
			System.out.println("쪽지보내기버튼 Click");
			String user = (String) totalUserList.getSelectedValue();
			if (user == null) {
				JOptionPane.showMessageDialog(null, "대상을 선택하세요", "알림", JOptionPane.ERROR_MESSAGE);
			}
			String note = JOptionPane.showInputDialog("보낼메시지");
			if (note != null) {
				System.out.println("note : " + note);
				sendMessage("Note/" + user + "@" + note);
			}

		} else if (e.getSource() == sendMessageButton) {
			System.out.println("sendMessageButton Click");
			if (chattingTextField.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "메세지를 입력하세요", "알림", JOptionPane.INFORMATION_MESSAGE);
			} else {
				sendMessage("Chatting/" + myCurrentRoomName + "/" + chattingTextField.getText());
				chattingTextField.setText("");
			}
		} else if (e.getSource() == joinRoomButton) {
			String joinRoom = (String) totalRoomList.getSelectedValue();
			Boolean isAlreadyIn = false;
			switchToTopPanel(chattingPanel);
			leaveRoomButton.setEnabled(true);
			createRoomButton.setEnabled(false);
			for (String myRoom : myRoomNameList) {
				if (myRoom.equals(joinRoom)) {
					isAlreadyIn = true;
					break;
				}
			}
			if (isAlreadyIn) // 이미 들어가있는 room 이라면 EnterRoom
			{
				sendMessage("EnterRoom/" + joinRoom);
			} else { // 들어와있지 않던 room이라면 JoinRoom
				sendMessage("JoinRoom/" + joinRoom);
			}

		}

		else if (e.getSource() == createRoomButton) {
			String roomName = JOptionPane.showInputDialog("방 이름을 입력하세요");
			if (roomName != null) {
				sendMessage("CreateRoom/" + roomName);
			}
			System.out.println("makeRoomButton Click");

		} else if (e.getSource() == leaveRoomButton) {
			checkLeaveRoom();
			// sendMessage("LeaveRoom/" + myCurrentRoomName);
			if (roomVectorList.size() != 0) {
				joinRoomButton.setEnabled(true);
			} else {
				joinRoomButton.setEnabled(false);
			}

		}

	}

	private void checkLeaveRoom() {
		int result = JOptionPane.showOptionDialog(null, "정말 나가시겠습니까?", "Warning", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, new String[] { "확인", "취소" }, "확인");

		if (result == JOptionPane.OK_OPTION) {
			System.out.println("okoption");
			sendMessage("LeaveRoomOK/" + myCurrentRoomName);
			myRoomNameList.remove(myCurrentRoomName);
			removeTopPanel();
			viewChatTextArea.setText("");
			createRoomButton.setEnabled(true);
			leaveRoomButton.setEnabled(false);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER && chattingTextField.hasFocus()) {
			if (chattingTextField.getText().length() == 0) {
			} else {
				sendMessage("Chatting/" + myCurrentRoomName + "/" + chattingTextField.getText());
				chattingTextField.setText("");
			}
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	private void connectServer() {
		try {
			socket = new Socket(ip, port);
			network();
		} catch (Exception e) {
		}
	}

	private void network() {
		try {
			inputStream = socket.getInputStream();
			dataInputStream = new DataInputStream(inputStream);
			outputStream = socket.getOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);

			sendMessage(userId);

			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {

					while (true) {
						try {
							String message = dataInputStream.readUTF();
							inMessage(message);
						} catch (Exception e) {
						}
					}

				}
			});
			thread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// connectButton.setEnabled(false);
	}

	private void inMessage(String str) {
		totalUserList.setListData(userVectorList);
		totalRoomList.setListData(roomVectorList);
		repaint();
		StringTokenizer stringTokenizer = new StringTokenizer(str, "/");
		String protocol = stringTokenizer.nextToken();
		String message = stringTokenizer.nextToken();
		if (protocol.equals("UserIdValidationFailed")) {
			userIDTextField.setText("");
			isUserIdvalidationOK = false;
			connectButton.setEnabled(true);
			setTitle("이미 사용중인 USER_ID 입니다.");
		} else if (protocol.equals("NewUser")) {
			userVectorList.add(message);
			totalUserList.setListData(userVectorList);
			totalRoomList.setListData(roomVectorList);
			repaint();

		} else if (protocol.equals("OldUser")) {
			userVectorList.add(message);
			totalUserList.setListData(userVectorList);
		} else if (protocol.equals("NetworkConnected")) {
			isUserIdvalidationOK = true;
			if (isUserIdvalidationOK) {
				userVectorList.add(userId);
				switchToTopPanel(waitingRoomPanel);
				setTitle(" Welcome! [ " + userId + " ] in YouChaeTalk!!");
				totalUserList.setListData(userVectorList);
				totalRoomList.setListData(roomVectorList);
				repaint();
				connectButton.setEnabled(false);
			}
			createRoomButton.setEnabled(socket.isConnected());

		} else if (protocol.equals("Note")) {
			stringTokenizer = new StringTokenizer(message, "@");
			String user = stringTokenizer.nextToken();
			String note = stringTokenizer.nextToken();
			JOptionPane.showMessageDialog(null, note, user + "로 부터 온 메시지", JOptionPane.CLOSED_OPTION);
		} else if (protocol.equals("CreateRoomFail")) {
			JOptionPane.showMessageDialog(null, "같은 방 이름이 존재합니다.", "알림", JOptionPane.ERROR_MESSAGE);
		} else if (protocol.equals("CreateRoom")) {
			sendMessage("JoinRoom/" + message);
			switchToTopPanel(chattingPanel);
			viewChatTextArea.setText("");
			myRoomNameList.add(message);
			myCurrentRoomName = message;
			leaveRoomButton.setEnabled(true);
			createRoomButton.setEnabled(false);
		} else if (protocol.equals("NewRoom")) {
			roomVectorList.add(message);
			totalRoomList.setListData(roomVectorList);
		} else if (protocol.equals("OldRoom")) {
			roomVectorList.add(message);
			totalRoomList.setListData(roomVectorList);

		} else if (protocol.equals("JoinRoom")) {
			myCurrentRoomName = message;
			myRoomNameList.add(message);
			JOptionPane.showMessageDialog(null, "채팅방 (  " + myCurrentRoomName + " ) 에 입장완료", "알림",
					JOptionPane.INFORMATION_MESSAGE);
			// joinRoomButton.setEnabled(false);
			// viewChatTextArea.setText("");
		} else if (protocol.equals("EnterRoom")) {
			// TODO
			myCurrentRoomName = message;

		} else if (protocol.equals("Chatting")) {
			String msg = stringTokenizer.nextToken();

			viewChatTextArea.append(message + " : " + msg + "\n");
		} else if (protocol.equals("LeaveRoom")) {

		} else if (protocol.equals("EmptyRoom")) {
			roomVectorList.remove(message);
			totalRoomList.setListData(roomVectorList);
		} else if (protocol.equals("UserOut")) {
			userVectorList.remove(message); // message는 나간 userID	
		}
		else if (protocol.equals("UserData_Update")) {
			totalUserList.setListData(userVectorList);
			totalRoomList.setListData(roomVectorList);
		}
	}

	private void sendMessage(String message) {
		try {
			dataOutputStream.writeUTF(message);
			dataOutputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void switchToTopPanel(JPanel currentPanel) {
		panelStack.push(currentPanel);
		currentPanel = panelStack.peek();
		currentPanel.setVisible(true);
		getContentPane().removeAll();
		getContentPane().add(currentPanel);
		getContentPane().validate();
		getContentPane().repaint();
	}

	private void removeTopPanel() {
		if (!panelStack.isEmpty()) {
			panelStack.pop();
		}

		getContentPane().removeAll();
		panelStack.peek().setVisible(true);
		getContentPane().add(panelStack.peek());
		getContentPane().validate();
		getContentPane().repaint();
	}

	private void setFont(Component component, int style, float size) {
		try {
			InputStream inputStream = new BufferedInputStream(new FileInputStream("fonts/배스킨라빈스 R.ttf"));
			Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
			component.setFont(font.deriveFont(style, size));
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Client();
	}

}
