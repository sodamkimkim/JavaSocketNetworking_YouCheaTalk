package chatting;

import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener {

	// GUI 자원
	private JPanel mainPanel;
	private JTextArea textArea;
	private ScrollPane serverInfoScroll;
	private JLabel portLabel;
	private JTextField portTextField;
	private JButton serverStartButton;
	private JButton serverStopButton;

	// 네트워크 자원
	private ServerSocket serverSocket;
	private Socket socket;
	private int port;
	private boolean isUserIdvalidationOK = true;

	private Vector<UserInformation> userVectorList = new Vector<UserInformation>();
	private Vector<RoomInformation> roomVectorList = new Vector<RoomInformation>();

	public Server() {
		init();
		addListener();
	}

	private void init() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 350, 410);
		mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(null);
		setContentPane(mainPanel);
		serverInfoScroll = new ScrollPane();
		serverInfoScroll.setBounds(10, 10, 309, 229);
		mainPanel.add(serverInfoScroll);
		textArea = new JTextArea();
		textArea.setBounds(12, 11, 310, 230);
		textArea.setBackground(Color.WHITE);
		textArea.setEditable(false);
		serverInfoScroll.add(textArea);

		portLabel = new JLabel("포트번호 :");
		portLabel.setBounds(12, 273, 82, 15);
		mainPanel.add(portLabel);

		portTextField = new JTextField("1");
		portTextField.setBounds(98, 270, 224, 21);
		portTextField.setColumns(10);
		mainPanel.add(portTextField);

		serverStartButton = new JButton("서버실행");
		serverStartButton.setBounds(12, 315, 154, 23);
		mainPanel.add(serverStartButton);

		serverStopButton = new JButton("서버중지");
		serverStopButton.setBounds(168, 315, 154, 23);
		serverStopButton.setEnabled(false);
		mainPanel.add(serverStopButton);

		setVisible(true);

	}

	private void addListener() {
		portTextField.requestFocus();
		serverStartButton.addActionListener(this);
		serverStopButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == serverStartButton) {
			startNetwork();
			portTextField.setEnabled(false);
			serverStartButton.setEnabled(false);
			serverStopButton.setEnabled(true);
		} else if (e.getSource() == serverStopButton) {
			try {
				serverSocket.close();
				userVectorList.removeAllElements();
				roomVectorList.removeAllElements();
				portTextField.setEnabled(true);
				serverStartButton.setEnabled(true);
				serverStopButton.setEnabled(false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void startNetwork() {
		if (portTextField.getText().length() == 0) {
			System.out.println("값을 입력 하세요");
		} else if (portTextField.getText().length() != 0) {
			port = Integer.parseInt(portTextField.getText());
		}

		try {
			serverSocket = new ServerSocket(port);
			textArea.append("서버를 시작합니다.\n");
			connect();
			portTextField.setEditable(false);
			serverStartButton.setEnabled(false);
			serverStopButton.setEnabled(true);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void connect() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {

						socket = serverSocket.accept();
						UserInformation userInfo = new UserInformation(socket);

						userInfo.start();

					} catch (IOException e) {
						textArea.append("서버가 중지됨! 다시 스타트 버튼을 눌러주세요\n");
						break;
					}
				}

			}
		});
		thread.start();
	}

	class UserInformation extends Thread {
		private InputStream inputStream;
		private OutputStream outputStream;
		private DataInputStream dataInputStream;
		private DataOutputStream dataOutputStream;
		private String userId;
		private String currentRoomName;
		private Socket userSocket;
		private Vector<String> myRoomVectorList = new Vector<String>();
		private boolean roomCheck = true;

		public UserInformation(Socket socket) {
			this.userSocket = socket;
			network();
		}

		private void network() {
			try {
				inputStream = userSocket.getInputStream();
				dataInputStream = new DataInputStream(inputStream);
				outputStream = userSocket.getOutputStream();
				dataOutputStream = new DataOutputStream(outputStream);

				userId = dataInputStream.readUTF();

				for (int j = 0; j < userVectorList.size(); j++) {
					if (userId.equals(userVectorList.elementAt(j).userId)) {

						sendMessage("UserIdValidationFailed/ok");
						System.out.println("server for문 안의 if");
						isUserIdvalidationOK = false;
						break;
					} else {
						isUserIdvalidationOK = true;
					}
				}

				if (isUserIdvalidationOK) {
					textArea.append("[ " + userId + " ] 로그인 성공!\n");
					sendMessage("NetworkConnected/ok");
					textArea.append("[ " + userId + " ] 입장\n");
					broadcast("NewUser/" + userId);

					for (int i = 0; i < userVectorList.size(); i++) {
						UserInformation userInfo = userVectorList.elementAt(i);
						sendMessage("OldUser/" + userInfo.userId);
					}

					for (int i = 0; i < roomVectorList.size(); i++) {
						RoomInformation room = roomVectorList.elementAt(i);
						sendMessage("OldRoom/" + room.roomName);
					}

					userVectorList.add(this);

				} else {
					textArea.append("[ " + userId + " ] userId 중복!\n");
				}

				broadcast("UserData_Updata/ok");

			} catch (IOException e) {
				System.out.println(e);
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					String message = dataInputStream.readUTF();
					textArea.append("[[" + userId + "]]" + message + "\n");
					inMessage(message);
				} catch (Exception e) {
					try {
						textArea.append(this.userId + " : 사용자접속끊어짐\n");
						dataOutputStream.close();
						dataInputStream.close();
						userSocket.close();
						userVectorList.remove(this);
						for (RoomInformation roomInfo : roomVectorList) {
							for (String  myRoom : myRoomVectorList) {
								if(roomInfo.roomName.equals(myRoom))
								{
									roomInfo.roomUserVectorList.remove(this);
									//roomInfo.removeRoom(this);
								}
							}
						}
						// 접속 끊긴 유저가 들어가있던 방 있으면 다 나가기 해주기
						broadcast("UserOut/" + this.userId);
						broadcast("UserData_Update/ok");	
						RemoveEmptyRoom();
						

						break;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}

			}
		}

		private void inMessage(String str) {

			StringTokenizer stringTokenizer = new StringTokenizer(str, "/");

			String protocol = stringTokenizer.nextToken();
			String message = stringTokenizer.nextToken();
			if (protocol.equals("Note")) {
				stringTokenizer = new StringTokenizer(message, "@");
				String user = stringTokenizer.nextToken();
				String note = stringTokenizer.nextToken();

				for (int i = 0; i < userVectorList.size(); i++) {
					UserInformation userInfo = userVectorList.elementAt(i);
					if (userInfo.userId.equals(user)) {
						userInfo.sendMessage("Note/" + userId + "@" + note);
					}
				}
			} else if (protocol.equals("CreateRoom")) {
				for (int i = 0; i < roomVectorList.size(); i++) {
					RoomInformation room = roomVectorList.elementAt(i);
					if (room.roomName.equals(message)) {
						sendMessage("CreateRoomFail/ok");
						roomCheck = false;
						break;
					} else {
						roomCheck = true;
					}
				}
				if (roomCheck == true) {
					RoomInformation newRoom = new RoomInformation(message, this);
					roomVectorList.add(newRoom);

					sendMessage("CreateRoom/" + message);
					broadcast("NewRoom/" + message);
				}

			} else if (protocol.equals("JoinRoom")) {
				myRoomVectorList.add(message);
				for (int i = 0; i < roomVectorList.size(); i++) {
					RoomInformation roomInfo = roomVectorList.elementAt(i);
					if (roomInfo.roomName.equals(message)) {
						roomInfo.addUser(this);
						roomInfo.roomBroadcast("Chatting/[[알림]]/(((" + userId + " 입장))) ");
						sendMessage("JoinRoom/" + message);

					}
				}

			} else if (protocol.equals("EnterRoom")) {
				for (int i = 0; i < roomVectorList.size(); i++) {
					RoomInformation roomInfo = roomVectorList.elementAt(i);
					if (roomInfo.roomName.equals(message)) {
						// TODO
						sendMessage("EnterRoom/" + message);
					}

				}
			} else if (protocol.equals("LeaveRoomOK")) {
				myRoomVectorList.remove(message);
				for (int i = 0; i < roomVectorList.size(); i++) {
					RoomInformation roomInfo = roomVectorList.elementAt(i);
					if (roomInfo.roomName.equals(message)) {
						roomInfo.excludeBroadcast("Chatting/[[알림]]/(((" + userId + " 퇴장))) ", userId);
						roomInfo.removeRoom(this);
						break;
					}
				}

			} else if (protocol.equals("Chatting")) {
				String msg = stringTokenizer.nextToken();
				for (int i = 0; i < roomVectorList.size(); i++) {
					RoomInformation roomInfo = roomVectorList.elementAt(i);
					if (roomInfo.roomName.equals(message)) {
						roomInfo.roomBroadcast("Chatting/" + userId + "/" + msg);
					}
				}
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

	}

	class RoomInformation {

		private String roomName;
		private Vector<UserInformation> roomUserVectorList = new Vector<UserInformation>();

		public RoomInformation(String roomName, UserInformation userInfo) {
			this.roomName = roomName;
			// this.roomUserVectorList.add(userInfo);
			userInfo.currentRoomName = roomName;
		}

		public void roomBroadcast(String string) {
			for (int i = 0; i < roomUserVectorList.size(); i++) {
				UserInformation userInfo = roomUserVectorList.elementAt(i);
				userInfo.sendMessage(string);
			}
		}

		public void excludeBroadcast(String string, String userId) {
			for (int i = 0; i < roomUserVectorList.size(); i++) {
				UserInformation userInfo = roomUserVectorList.elementAt(i);
				if (!userInfo.userId.equals(userId)) {
					userInfo.sendMessage(string);
				}
			}
		}

		private void addUser(UserInformation userInfo) {
			roomUserVectorList.add(userInfo);
		}

		private void removeRoom(UserInformation userInfo) {
			System.out.println("remove room");
			roomUserVectorList.remove(userInfo);
			boolean empty = roomUserVectorList.isEmpty();
			if (empty) {
				System.out.println("룸 유저 비어떠염");
				for (int i = 0; i < roomVectorList.size(); i++) {
					RoomInformation roomInfo = roomVectorList.elementAt(i);
					if (roomInfo.roomName.equals(roomName)) {
						System.out.println("if 탐");
						roomVectorList.remove(this);
						broadcast("EmptyRoom/" + roomName);
						break;
					}
				}
			}
		}

	}

	public void broadcast(String string) {
		for (int i = 0; i < userVectorList.size(); i++) {
			UserInformation userInfo = userVectorList.elementAt(i);
			userInfo.sendMessage(string);
		}
	}
	public void RemoveEmptyRoom()
	{
		if(roomVectorList.size() !=0)
		{
			for (RoomInformation roomInfo : roomVectorList) {
				if(roomInfo.roomUserVectorList.size()==0)
				{
					roomVectorList.remove(roomInfo);
					broadcast("EmptyRoom/"+roomInfo.roomName);
				}
			}
			
		}
	}

	public static void main(String[] args) {
		new Server();
	}

}
