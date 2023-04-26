package chatting_ref;

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
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener {

	// GUI 자원
	private JPanel contentPane;
	private JTextField tfPort;
	private JTextArea textArea;
	private JLabel lblPortNum;
	private JButton btnServerStart;
	private JButton btnServerStop;

	// Network 자원
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	

	// 그외 자원들
	private Vector<UserInfomation> vc = new Vector<UserInfomation>();
	private Vector<RoomInfomation> vc_room = new Vector<RoomInfomation>();

	public Server() {
		init();
		addListener();
		tfPort.requestFocus();
	}

	// GUI 초기화
	private void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 350, 410);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setBounds(10, 10, 309, 229);
		textArea = new JTextArea();
		textArea.setBounds(12, 11, 310, 230);
		textArea.setBackground(Color.cyan);
		scrollPane.add(textArea);
		contentPane.add(scrollPane);
		textArea.setEditable(false);

		lblPortNum = new JLabel("포트번호 :");
		lblPortNum.setBounds(12, 273, 82, 15);
		contentPane.add(lblPortNum);

		tfPort = new JTextField();
		tfPort.setBounds(98, 270, 224, 21);
		contentPane.add(tfPort);
		tfPort.setColumns(10);

		btnServerStart = new JButton("서버실행");
		btnServerStart.setBounds(12, 315, 154, 23);
		contentPane.add(btnServerStart);

		btnServerStop = new JButton("서버중지");
		btnServerStop.setBounds(168, 315, 154, 23);
		contentPane.add(btnServerStop);
		btnServerStop.setEnabled(false);

		setVisible(true);
	}

	// 이벤트 리스너
	private void addListener() {

		tfPort.addActionListener(this);
		btnServerStart.addActionListener(this);
		btnServerStop.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == btnServerStart) {
			if (tfPort.getText().length() == 0) {
				System.out.println("  값을 입력 하세요 ");
			} else if (tfPort.getText().length() != 0) {

				// 값을 가져와서 port변수에 저장시키기
				port = Integer.parseInt(tfPort.getText());
				startNetwork();
				tfPort.setEditable(false);
				btnServerStart.setEnabled(false);
				btnServerStop.setEnabled(true);
			}

		} else if (e.getSource() == btnServerStop) {
			try {
				server_socket.close();
				vc.removeAllElements();
				vc_room.removeAllElements();
				tfPort.setEditable(true);
				btnServerStart.setEnabled(true);
				btnServerStop.setEnabled(false);
			} catch (IOException e1) {

			}
		}
	}

	private void startNetwork() {
		try {
			server_socket = new ServerSocket(port);
			textArea.append("서버를 시작 하겠습니다.\n");
			connect();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "이미 사용중인 포트입니다.", "알림", JOptionPane.ERROR_MESSAGE);
			btnServerStart.setEnabled(true);
			btnServerStop.setEnabled(false);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "잘못입력하였습니다.", "알림", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void connect() {
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						textArea.append("사용자의 접속을 기다립니다.\n");
						socket = server_socket.accept();

						UserInfomation useInfo = new UserInfomation(socket);
						// 각각의 스레드를 등록시켜준다.
						useInfo.start();
						// 여기서 벡터에 등록하지 않음

					} catch (IOException e) {
						textArea.append("서버가 중지됨! 다시 스타트 버튼을 눌러주세요\n");
						break;
					}
				}
			}
		});
		th.start();
	}

	// 전체 사용자에게 메세지를 보내는 부분
	public void broadCast(String str) {
		for (int i = 0; i < vc.size(); i++) {
			UserInfomation uinf = vc.elementAt(i);
			// 여기서 프로토콜의 개념을 사용
			uinf.sendmessage(str);
		}
	}

	// 내부클래스
	class UserInfomation extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		String nickName;
		String myCurrentRoomName;
		private Socket user_socket;

		private boolean roomCheck = true;

		public UserInfomation(Socket soc) {
			this.user_socket = soc; // 의존성컴포지션관계
			network();
		}

		private void network() {
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);

				// 처음 접속시 유저의 id를 입력받는다.
				nickName = dis.readUTF();
				textArea.append("[[" + nickName + "]] 입장\n");

				// 기존사용자들에게 신규 유저의 접속을 알린다.
				broadCast("NewUser/" + nickName); // c1한테만 메시지가 전달된다.

				// c1,c2 (실행시점)

				// 자신에게 기존 사용자들을 알린다.
				for (int i = 0; i < vc.size(); i++) {
					UserInfomation uinf = vc.elementAt(i);
					// 브로드 캐스트가 아니다(나와 연결되어있는 스트림을 통해서 보낸다)
					// 포문 돌아서,,
					// 닉네임을 가져오기 위해서.
					// 개인한테 메시지를 보내는거.
					sendmessage("OldUser/" + uinf.nickName);
				}
				for (int i = 0; i < vc_room.size(); i++) {
					RoomInfomation room = vc_room.elementAt(i);
					sendmessage("OldRoom/" + room.roomName);
				}

				// 사용자에게 자신을 알린후 벡터에 자신을 추가한다.
				vc.add(this);

			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Stream설정에러!", "알림", JOptionPane.ERROR_MESSAGE);
			}
		}

		// 브로드캐스트
		@Override
		public void run() {
			while (true) {
				try {
					String msg = dis.readUTF();
					textArea.append("[[" + nickName + "]]" + msg + "\n");
					inmessage(msg);
				} catch (IOException e) {
					try {
						textArea.append(nickName + " : 사용자접속끊어짐\n");
						dos.close();
						dis.close();
						user_socket.close();
						vc.remove(this);
						vc_room.remove(this);
						broadCast("UserOut/" + nickName);
						broadCast("ErrorOutRoom/" + myCurrentRoomName);
						broadCast("UserData_Updata/ok");
						break;
					} catch (IOException e1) {
						break;
					}
				}
			}
		}

		private void inmessage(String str) {
			StringTokenizer st = new StringTokenizer(str, "/");

			String protocol = st.nextToken();
			String message = st.nextToken();

			System.out.println("protocol : " + protocol);
			System.out.println("message : " + message);

			if (protocol.equals("Note")) {
				System.out.println(message);
				st = new StringTokenizer(message, "@");
				String user = st.nextToken();
				String note = st.nextToken();
				// 백터에서 해당 사용자를 찾아서 쪽지를 전송한다.
				for (int i = 0; i < vc.size(); i++) {
					UserInfomation u = vc.elementAt(i);
					// 쪽지는 반드시 찾은 사용자에게 메세지를 보내줘어야 한다.
					if (u.nickName.equals(user)) {
						u.sendmessage("Note/" + nickName + "@" + note);
					}
				}
			} else if (protocol.equals("CreateRoom")) {
				// 1.현재같은방이 존재하는지 확인한다.
				for (int i = 0; i < vc_room.size(); i++) {
					RoomInfomation room = vc_room.elementAt(i);
					if (room.roomName.equals(message)) { // 만들고자하는방이름을 확인한다
						sendmessage("CreateRoomFail/ok");
						roomCheck = false;
						break;
					} else {
						roomCheck = true;
					}
				} // end for
				if (roomCheck == true) {
					// 1.방을 생성한다.
					RoomInfomation new_room = new RoomInfomation(message, this);
					// 2. 전체 방 벡터에 생성된 방을 저장한다.
					vc_room.add(new_room);
					// 3.사용자들에게 방과 방이름을 생성되었다고 알려준다.
					sendmessage("CreateRoom/" + message); // 자신에게 방 성공 메세지를 보낸다.
					broadCast("new_Room/" + message);
				}
			} else if (protocol.equals("Chatting")) {
				String msg = st.nextToken();
				for (int i = 0; i < vc_room.size(); i++) {
					RoomInfomation r = vc_room.elementAt(i);
					if (r.roomName.equals(message)) {
						r.roomBroadcast("Chatting/" + nickName + "/" + msg);
					}
				}
			} else if (protocol.equals("JoinRoom")) {
				for (int i = 0; i < vc_room.size(); i++) {
					RoomInfomation r = vc_room.elementAt(i);
					if (r.roomName.equals(message)) {
						// 신규접속자를 알린다.
						r.roomBroadcast("Chatting/[[알림]]/(((" + nickName + " 입장))) ");
						r.addUser(this); // 해당 룸 객체에 자신을 추가시킨다.
						sendmessage("JoinRoom/" + message);
					}
				}
			} else if (protocol.equals("OutRoom")) {
				for (int i = 0; i < vc_room.size(); i++) {
					RoomInfomation r = vc_room.elementAt(i);
					if (r.roomName.equals(message)) {
						r.removeRoom(this);
						sendmessage("OutRoom/ok");
						break;
					}
				}
			}
		}

		private void sendmessage(String msg) {
			try {
				dos.writeUTF(msg);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// 내부클래스
	class RoomInfomation {

		String roomName;
		Vector<UserInfomation> room_user_vc = new Vector<UserInfomation>();

		public RoomInfomation(String roomName, UserInfomation u) {
			this.roomName = roomName;
			this.room_user_vc.add(u);
			// 와우 대박. ㅋㅋ
			u.myCurrentRoomName = roomName;
		}

		private void roomBroadcast(String str) { // 현재방의 모든 사람들에게 알린다.
			for (int i = 0; i < room_user_vc.size(); i++) {
				UserInfomation u = room_user_vc.elementAt(i);
				u.sendmessage(str);
			}
		}

		private void addUser(UserInfomation u) {
			room_user_vc.add(u);
		}

		@Override
		public String toString() {
			return roomName;
		}

		private void removeRoom(UserInfomation u) {
			room_user_vc.remove(u);
			boolean empty = room_user_vc.isEmpty();
			if (empty) {
				for (int i = 0; i < vc_room.size(); i++) {
					RoomInfomation r = vc_room.elementAt(i);
					if (r.roomName.equals(roomName)) {
						vc_room.remove(this);
						broadCast("EmptyRoom/" + roomName);
						broadCast("UserData_Updata/ok");
						break;
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		new Server();
	}
}
