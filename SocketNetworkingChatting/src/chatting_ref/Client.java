package chatting_ref;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;



public class Client extends JFrame implements ActionListener {

	// GUI자원
	private JPanel main_pnl;
	private JTextField hostIP_tf;
	private JTextField port_tf;
	private JTextField userID_tf;
	private JTextField chatting_tf;
	private JTextArea viewChat_ta;
	private JButton connect_btn;
	private JButton confirm_btn;
	private JButton sendNote_btn;
	private JButton joinRomm_btn;
	private JList totalList_lst; // 전체접속자 리스트
	private JList roomList_lst; // 방 리스트
	private JButton btn_makeRoom;
	private JButton btn_outRoom;
	private JButton btn_end;
	private JPanel panel_1;

	// network 자원
	private Socket socket;
	private String ip;
	private int port;
	private String user_id;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	// 그외 변수들
	private Vector<String> user_Vclist = new Vector<String>();
	private Vector<String> roomList_vc = new Vector<String>();
	private StringTokenizer st;
	private String my_roomName;

	public Client() {
		init();
		addListener();
	}

	private void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 474, 483);
		main_pnl = new JPanel();
		main_pnl.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(main_pnl);
		main_pnl.setLayout(null);

		JTabbedPane Jtab = new JTabbedPane(JTabbedPane.TOP);
		Jtab.setBounds(12, 27, 328, 407);
		main_pnl.add(Jtab);

		panel_1 = new JPanel();
		Jtab.addTab("로그인", null, panel_1, null);
		panel_1.setLayout(null);

		JLabel hostIP_lbl = new JLabel("Host_IP ");
		hostIP_lbl.setFont(new Font("휴먼모음T", Font.BOLD, 13));
		hostIP_lbl.setBounds(12, 25, 91, 15);
		panel_1.add(hostIP_lbl);

		hostIP_tf = new JTextField();
		hostIP_tf.setFont(new Font("휴먼모음T", Font.BOLD, 13));
		hostIP_tf.setBounds(112, 21, 199, 21);
		panel_1.add(hostIP_tf);
		hostIP_tf.setColumns(10);

		JLabel port_lbl = new JLabel("Server_Port");
		port_lbl.setFont(new Font("휴먼모음T", Font.BOLD, 13));
		port_lbl.setBounds(12, 72, 91, 15);
		panel_1.add(port_lbl);

		port_tf = new JTextField();
		port_tf.setFont(new Font("휴먼모음T", Font.BOLD, 13));
		port_tf.setBounds(112, 69, 199, 21);
		panel_1.add(port_tf);
		port_tf.setColumns(10);

		JLabel userID_lbl = new JLabel("User_ID");
		userID_lbl.setFont(new Font("휴먼모음T", Font.BOLD, 13));
		userID_lbl.setBounds(12, 122, 91, 15);
		panel_1.add(userID_lbl);

		userID_tf = new JTextField();
		userID_tf.setBounds(112, 119, 199, 21);
		panel_1.add(userID_tf);
		userID_tf.setColumns(10);

		JLabel img_lbl = new JLabel("input the image");
		img_lbl.setIcon(new ImageIcon());
		img_lbl.setBounds(12, 213, 299, 155);
		panel_1.add(img_lbl);

		connect_btn = new JButton("connect");
		connect_btn.setFont(new Font("휴먼모음T", Font.BOLD, 12));
		connect_btn.setBounds(214, 162, 97, 23);
		panel_1.add(connect_btn);

		JPanel panel = new JPanel();
		Jtab.addTab("대기실", null, panel, null);
		panel.setLayout(null);

		JLabel totalList_lbl = new JLabel("전체접속자");
		totalList_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		totalList_lbl.setFont(new Font("휴먼모음T", Font.BOLD, 13));
		totalList_lbl.setBounds(12, 28, 102, 15);
		panel.add(totalList_lbl);

		JLabel roomList_lbl = new JLabel("방 리스트");
		roomList_lbl.setHorizontalAlignment(SwingConstants.CENTER);
		roomList_lbl.setFont(new Font("휴먼모음T", Font.BOLD, 13));
		roomList_lbl.setBounds(209, 27, 102, 15);
		panel.add(roomList_lbl);

		totalList_lst = new JList();
		totalList_lst.setBounds(12, 69, 102, 257);
		panel.add(totalList_lst);

		roomList_lst = new JList();
		roomList_lst.setBounds(209, 69, 102, 257);
		panel.add(roomList_lst);

		sendNote_btn = new JButton("쪽지보내기");
		sendNote_btn.setFont(new Font("휴먼모음T", Font.BOLD, 12));
		sendNote_btn.setBounds(12, 345, 102, 23);
		panel.add(sendNote_btn);

		joinRomm_btn = new JButton("채팅방참여");
		joinRomm_btn.setFont(new Font("휴먼모음T", Font.BOLD, 12));
		joinRomm_btn.setBounds(209, 345, 102, 23);
		panel.add(joinRomm_btn);
		hostIP_tf.setText("127.0.0.1");

		JPanel panel_2 = new JPanel();
		Jtab.addTab("채팅", null, panel_2, null);
		panel_2.setLayout(null);

		viewChat_ta = new JTextArea();
		viewChat_ta.setEnabled(false);
		viewChat_ta.setEditable(false);
		viewChat_ta.setFont(new Font("휴먼모음T", Font.BOLD, 12));
		viewChat_ta.setBounds(0, 0, 323, 337);
		panel_2.add(viewChat_ta);

		chatting_tf = new JTextField();
		chatting_tf.setFont(new Font("휴먼모음T", Font.BOLD, 11));
		chatting_tf.setBounds(0, 347, 214, 21);
		panel_2.add(chatting_tf);
		chatting_tf.setColumns(10);

		confirm_btn = new JButton("전 송");
		confirm_btn.setFont(new Font("휴먼모음T", Font.BOLD, 12));
		confirm_btn.setBounds(226, 346, 97, 23);
		panel_2.add(confirm_btn);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setEnabled(false);
		scrollPane.setBounds(0, 0, 323, 337);
		panel_2.add(scrollPane);

		btn_makeRoom = new JButton("방 만들기");
		btn_makeRoom.setFont(new Font("휴먼모음T", Font.BOLD, 11));
		btn_makeRoom.setBounds(352, 93, 97, 23);
		main_pnl.add(btn_makeRoom);

		btn_outRoom = new JButton("방 나가기");
		btn_outRoom.setFont(new Font("휴먼모음T", Font.BOLD, 12));
		btn_outRoom.setBounds(352, 150, 97, 23);
		main_pnl.add(btn_outRoom);
		btn_outRoom.setEnabled(false);
		btn_end = new JButton("종료");
		btn_end.setFont(new Font("휴먼모음T", Font.BOLD, 12));
		btn_end.setBounds(352, 398, 97, 23);
		main_pnl.add(btn_end);
		setVisible(true);

	}

	private void connectServer() {
		try {
			// 서버에 접속합니다.
			socket = new Socket(ip, port);
			network();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "연결실패!", "알림",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "연결실패!", "알림",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void network() {
		
		try {
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);

			user_id = userID_tf.getText().trim();
			sendmessage(user_id);

			// 벡터에 유저의 id 를 저장하고 리스트 화면에 추가시켜준다.
			user_Vclist.add(user_id);
			totalList_lst.setListData(user_Vclist);

			Thread cth = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							// 서버로부터 수신된 메세지.
							String msg = dis.readUTF();
							inmessage(msg);
						} catch (IOException e) {
							try {
								user_Vclist.removeAll(user_Vclist);
								roomList_vc.removeAll(roomList_vc);
								totalList_lst.setListData(user_Vclist);
								roomList_lst.setListData(roomList_vc);
								viewChat_ta.setText("\n");
								is.close();
								os.close();
								dis.close();
								dos.close();
								socket.close();
								JOptionPane.showMessageDialog(null, "서버가 종료됨!", "알림",
										JOptionPane.ERROR_MESSAGE);
								break;
							} catch (Exception e2) {
								return ;
							}
						}
					}
				}
			});
			cth.start();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "연결실패!", "알림",
					JOptionPane.ERROR_MESSAGE);
		}// Stream 준비완료
		connect_btn.setEnabled(false);
	}

	private void inmessage(String str) {

		st = new StringTokenizer(str, "/");

		String protocol = st.nextToken();
		String message = st.nextToken();

		System.out.println("프로토콜" + protocol);
		System.out.println("메세지" + message);

		if (protocol.equals("NewUser")) {
			user_Vclist.add(message);
			totalList_lst.setListData(user_Vclist);
		} else if (protocol.equals("OldUser")) {
			user_Vclist.add(message);
			totalList_lst.setListData(user_Vclist);
		} else if (protocol.equals("Note")) {
			st = new StringTokenizer(message, "@");
			String user = st.nextToken();
			String note = st.nextToken();
			JOptionPane.showMessageDialog(null, note, user + "로 부터 온 메세지",
					JOptionPane.CLOSED_OPTION);
		} else if (protocol.equals("CreateRoom")) {
			// 방만들기가 성공했을 경우
			my_roomName = message;
			joinRomm_btn.setEnabled(false);
			btn_outRoom.setEnabled(true);
			btn_makeRoom.setEnabled(false);
		} else if (protocol.equals("CreateRoomFail")) {
			JOptionPane.showMessageDialog(null, "같은 방 이름이 존재합니다.!", "알림",
					JOptionPane.ERROR_MESSAGE);
		} else if (protocol.equals("new_Room")) {
			roomList_vc.add(message);
			roomList_lst.setListData(roomList_vc);
		} else if (protocol.equals("Chatting")) {
			String msg = st.nextToken();
			viewChat_ta.append(message + " : " + msg + "\n");
		} else if (protocol.equals("OldRoom")) {
			roomList_vc.add(message);
			roomList_lst.setListData(roomList_vc);
		} else if (protocol.equals("JoinRoom")) {
			my_roomName = message;
			JOptionPane.showMessageDialog(null, "채팅방 (  " + my_roomName
					+ " ) 에 입장완료", "알림", JOptionPane.INFORMATION_MESSAGE);
			viewChat_ta.setText("");
		} else if(protocol.equals("UserOut")) {
			user_Vclist.remove(message);
			sendmessage("OutRoom/"+my_roomName);
		} else if(protocol.equals("UserData_Updata")) {
			totalList_lst.setListData(user_Vclist);
			roomList_lst.setListData(roomList_vc);
		} else if(protocol.equals("OutRoom")) {
			viewChat_ta.append("*** (( "+my_roomName+"에서 퇴장 ))***\n");
			my_roomName = null;
			btn_makeRoom.setEnabled(true);
			btn_outRoom.setEnabled(false);
		} else if(protocol.equals("EmptyRoom")) {
			roomList_vc.remove(message);
		//클라이언트가 강제 종료 되었고 방이 비었을때 방 목록에서 그 방을 없애준다.	
		} else if(protocol.equals("ErrorOutRoom") ) {
			roomList_vc.remove(message);
		}
	}

	private void sendmessage(String msg) {
		try {
			dos.writeUTF(msg);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// 이벤트리스너
	private void addListener() {
		connect_btn.addActionListener(this);
		confirm_btn.addActionListener(this);
		sendNote_btn.addActionListener(this);
		joinRomm_btn.addActionListener(this);
		chatting_tf.addActionListener(this);
		btn_end.addActionListener(this);
		btn_makeRoom.addActionListener(this);
		btn_outRoom.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == connect_btn) {
			if(hostIP_tf.getText().length() ==0) {
				hostIP_tf.setText("IP를 입력하세요");
				hostIP_tf.requestFocus();
			} else if(port_tf.getText().length() ==0) {
				port_tf.setText("포트번호를 입력하세요");
				port_tf.requestFocus();
			} else if(userID_tf.getText().length() == 0) {
				userID_tf.setText("id 를 입력하세요");
				userID_tf.requestFocus();
			} else {
				ip = hostIP_tf.getText();
				try{
				port = Integer.parseInt(port_tf.getText().trim());
				}catch (Exception e2) {
					port_tf.setText("잘못 입력하였습니다.");
				}
				user_id = userID_tf.getText().trim();
				// 서버연결하기
				connectServer();
				setTitle("[" + user_id + " ] 님 깨알톡에 오신걸 환경합니다.");
			}
		} else if (e.getSource() == confirm_btn) {
			System.out.println("전송버튼클릭");
			sendmessage("Chatting/" + my_roomName + "/"
					+ chatting_tf.getText().trim());
		} else if (e.getSource() == sendNote_btn) {
			System.out.println("쪽지보내기버튼 클릭");
			String user = (String) totalList_lst.getSelectedValue();
			if (user == null) {
				JOptionPane.showMessageDialog(null, "대상을 선택하세요", "알림",
						JOptionPane.ERROR_MESSAGE);
			}
			String note = JOptionPane.showInputDialog("보낼메세지");
			if (note != null) {
				sendmessage("Note/" + user + "@" + note);
			}
		} else if (e.getSource() == joinRomm_btn) {
			System.out.println("방입장버튼 클릭");
			String joinRoom = (String) roomList_lst.getSelectedValue();
			btn_outRoom.setEnabled(true);
			btn_makeRoom.setEnabled(false);
			sendmessage("JoinRoom/" + joinRoom);
		} else if (e.getSource() == chatting_tf) {
			if(chatting_tf.getText().length() == 0 ){
				System.out.println("이게 0값으로 들어가나?");
				sendmessage("Chatting/" + my_roomName + "/"
						+ chatting_tf.getText()+"   ");
			}else {
				sendmessage("Chatting/" + my_roomName + "/"
						+ chatting_tf.getText());
			}
		} else if (e.getSource() == btn_makeRoom) {
			System.out.println("방생성버튼클릭");
			String roomName = JOptionPane.showInputDialog("방 이름을 입력하세요");
			if (roomName != null) {
				sendmessage("CreateRoom/" + roomName);
			}
		} else if(e.getSource() == btn_outRoom) {
			System.out.println("방나가기버튼클릭.");
			sendmessage("OutRoom/"+my_roomName);
		} else if(e.getSource() == btn_end) {
			System.exit(0);
		}
		chatting_tf.setText("");
	}
	public static void main(String[] args) {
		new Client();
	}
}


