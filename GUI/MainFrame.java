package GUI;

import ChessBoard.ChessBoard;
import ChessBoard.Player;
import ChessBoard.FileOperation;
import ChessBoard.ChessException;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainFrame extends JFrame {
    private boolean started;
    public ChessBoard Game;

    public PieceComponent[][] GameBoard;
    private final JLabel ChessboardBackg;
    private JButton StartButton;
    private JButton StopButton;
    private JButton LoadButton;
    private JButton WdButton;
    private JLabel pro1;
    private JLabel pro2;
    private JLabel PlayerName1;
    private JLabel PlayerName2;
    private JScrollPane scrollPane1;
    private JTextPane MessagePane;
    private JScrollPane scrollPane2;
    private JTextPane RankPane;
    private JButton ReplayButton;
    private JButton CheatButton;
    private JLabel TurnLabel;
    private JLabel RoundLabel;
    private String Message = "";
    private JButton ReplayLast;
    private JButton ReplayNext;
    public Controller controller;
    public boolean pvp;
    public boolean cheat;
    public Player[] local;
    public ArrayList<Player> list;
    private boolean isLogin;
    private int theme;
    Clip bgm;

    public MainFrame(String title) {
        super(title);
        this.setLocation(100,100);
        this.setSize(785,700);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
        this.setLayout(null);
        this.ChessboardBackg = new JLabel();
        this.started = false;
        this.isLogin = false;
        local = new Player[2];

        String[] options = {"Genius", "Basic"};
        theme = JOptionPane.showOptionDialog(this, "Please choose theme", "Start",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null) + 1;
        String themestr = "theme" + theme + "/";
        AddChess();
        AddButton();
        AddLabel();
        AddInfoArea();
        AddPlayerInfo();
        //---- ChessboardBackg ----
        ChessboardBackg.setIcon(new ImageIcon("resources/" + themestr + "board.jpg"));
        this.add(ChessboardBackg);
        ChessboardBackg.setBounds(115, 0, 305, 610);

        JLabel Background = new JLabel(new ImageIcon("resources/bgpic1.jpg"));
        Background.setBounds(0,0,785,785);
        this.add(Background);

        try {
            list = FileOperation.ScanUser("User/");
        } catch (ChessException e) {
            JOptionPane.showMessageDialog(this,e.getMessage(),"Warning",JOptionPane.WARNING_MESSAGE);
            return;
        }
        Collections.sort(list);
        StringBuilder rankness = new StringBuilder();
        for(Player o: list)
            rankness.append(String.format("%-8s %5d %7d\n", o.id, o.rating, o.score));
        printRank(rankness.toString());

        try {
            bgm = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        PlayBGM(0);
    }

    private void AddChess() {
        this.GameBoard = new PieceComponent[4][8];
        this.controller = new Controller();
        int disx = theme == 1 ? 73 : 72;
        int disy = theme == 1 ? 74 : 72;
        int boardY = theme == 1 ? 10 : 15;
        int boardX = theme == 1 ? 120 : 115;
        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 4; x++) {
                GameBoard[x][y] = new PieceComponent(0,0);
                GameBoard[x][y].theme = theme;
                GameBoard[x][y].setLocation(boardX + disx * x, boardY + disy * y);
                GameBoard[x][y].setVisible(false);
                GameBoard[x][y].addActionListener(this.controller);
                this.add(GameBoard[x][y]);
            }
        }
    }

    public void generate(boolean isLoad) {
        this.controller.mainFrame = this;
        for(int y = 0; y < 8; y++) {
            for(int x = 0; x < 4; x++) {
                GameBoard[x][y].setVisible(true);
                int who = this.Game.map[y + 1][x + 1].player;
                int value = 9;
                if(who != -1) {
                    value = (int)Math.pow(-1,who) * this.Game.players[who].pieces.chess[this.Game.map[y+1][x+1].index].level;
                    GameBoard[x][y].isRevealed = this.Game.players[who].pieces.chess[this.Game.map[y+1][x+1].index].show;
                } else {
                    GameBoard[x][y].isRevealed = false;
                }
                if(value != 9) {
                    GameBoard[x][y].rank = value > 0 ? value : -value;
                    GameBoard[x][y].player = value > 0 ? 0 : 1;
                } else {
                    GameBoard[x][y].rank = 0;
                    GameBoard[x][y].player = -1;
                }
                if(Game.steps < 1) GameBoard[x][y].color = value > 0 ? 0 : 1;
                if(isLoad) GameBoard[x][y].color = value > 0 ? 0 : 1;
                GameBoard[x][y].x = x + 1; GameBoard[x][y].y = y + 1;
                GameBoard[x][y].update();
            }
        }
    }

    private void AddButton() {
        //---- Init ----
        this.StartButton = new JButton();
        this.StopButton = new JButton();
        this.LoadButton = new JButton();
        this.WdButton = new JButton();
        this.ReplayButton = new JButton();
        this.ReplayLast = new JButton();
        this.ReplayNext = new JButton();
        this.CheatButton = new JButton();

        //---- StartButton ----
        StartButton.setText("Start");
        this.add(StartButton);
        StartButton.setBounds(10, 615, 100, 45);
        StartButton.addActionListener((e)->{
            if(!started && !isLogin) {
                Player tmp = Login();
                if(tmp != null) local[0] = tmp;
                else return;
            }
            String[] options = {"Remote", "Local", "Medium", "Easy", "Beginner"};
            int select = JOptionPane.showOptionDialog(this, "Please choose the level of AI or connect to others", "Start",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
            this.started = true;
            Game = new ChessBoard();
            if(select != 0 && select != 1) {
                int level = 4;
                String name = "Beginner";
                if(select == 3) {
                    level = 1;
                    name = "Easy";
                } else if(select == 2) {
                    level = 2;
                    name = "Medium";
                }
                pro1.setIcon(new ImageIcon("resources/profile1.png"));
                Player AI = new Player(name, level);
                Game.Init(local[0], AI);
                pvp = false;
            } else {
                pvp = true;
                pro1.setIcon(new ImageIcon("resources/profile2.png"));
                if(select == 0) {
                    try {
                        Connect();
                    } catch (ChessException ex) {
                        JOptionPane.showMessageDialog(this,ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } else {
                    Player tmp = Login();
                    if(tmp != null) local[1] = tmp;
                    Game.Init(local[0], local[1]);
                }
            }
            Game.mainFrame = this;
            controller.first = null;
            Game.InitialMap();
            Game.Show();
            printTurnAndRound();
            pro1.setVisible(true);
            pro2.setVisible(true);
            PlayerName1.setText(Game.players[1].id);
            PlayerName2.setText(Game.players[0].id);
            CheatButton.setVisible(true);
            StartButton.setText("Restart");
            generate(false);
            PlayBGM(1);
            double p = 1.0/(1.0+Math.pow(10,1.0*(Game.players[1].rating-Game.players[0].rating)/400));
            printMess(String.format("Possibility of %s \nwinning %s: %.2f", Game.players[0].id,Game.players[1].id, p * 100.0) + "% \n");
        });

        //---- CheatButton ----
        CheatButton.setText("Cheat");
        this.add(CheatButton);
        CheatButton.setBounds(10,500,100,45);
        CheatButton.addActionListener((e)-> this.cheat = !this.cheat);
        CheatButton.setVisible(false);

        //---- StopButton ----
        StopButton.setText("Stop");
        this.add(StopButton);
        StopButton.setBounds(120, 615, 100, 45);
        StopButton.addActionListener((e)->{
            if( started ) {
                String dir;
                try {
                    dir = FileOperation.GamePause(this.Game);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(this, "Save at " + dir);
                Game = null;
                this.started = false;
                RoundLabel.setText("");
                TurnLabel.setText("");
                MessagePane.setText("");
                CheatButton.setVisible(false);
                pro1.setVisible(false);
                pro2.setVisible(false);
                PlayerName1.setText("");
                PlayerName2.setText("");
                this.Message = "";
                for(int y = 0; y < 8; y++) {
                    for(int x = 0; x < 4; x++) {
                        GameBoard[x][y].setVisible(false);
                        GameBoard[x][y].rank = -1;
                        GameBoard[x][y].player = -1;
                        GameBoard[x][y].isRevealed = false;
                        GameBoard[x][y].repaint();
                    }
                }
            }
            PlayBGM(0);
        });

        //---- LoadButton ----
        LoadButton.setText("Load");
        this.add(LoadButton);
        LoadButton.setBounds(230, 615, 100, 45);
        LoadButton.addActionListener((e)->{
            if(started) return;
            JFileChooser fileChooser = new JFileChooser();
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                File ret = fileChooser.getSelectedFile();
                String dir = ret.getPath(), data;
                try {
                    data=FileOperation.Load(dir);
                } catch (ChessException ex) {
                    JOptionPane.showMessageDialog(this,ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
                    return ;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                String name=dir.substring(dir.lastIndexOf("/")+1);
                try {
                    Game = new ChessBoard();
                    Game.mainFrame = this;
                    Game.GameContinue(data,name.substring(0,name.length()-5));
                } catch (ChessException ex) {
                    JOptionPane.showMessageDialog(this,ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
                }
                if(!isLogin) Login();
                generate(true);
                pro1.setVisible(true);
                pro2.setVisible(true);
                PlayerName1.setText(Game.players[1].id);
                PlayerName2.setText(Game.players[0].id);
                CheatButton.setVisible(true);
                started = true;
                Game.Show();

            }
        });

        //---- WdButton ----
        WdButton.setText("Withdraw");
        this.add(WdButton);
        WdButton.setBounds(340, 615, 100, 45);
        WdButton.addActionListener((e)->{
            if(started) {
                if(Game.steps != 0){
                    Game.LoadPoint(false);
                    generate(false);
                    printTurnAndRound();
                    Game.Show();
                } else {
                    JOptionPane.showMessageDialog(this,"You can't withdraw", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        //---- ReplayButton ----
        ReplayButton.setText("Replay");
        this.add(ReplayButton);
        ReplayButton.setBounds(450, 615, 100, 45);
        ReplayButton.addActionListener((e)->{
            if(started) return;
            JFileChooser fileChooser = new JFileChooser();
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File ret = fileChooser.getSelectedFile();
                String dir = ret.getPath();
                String data;
                try {
                    data = FileOperation.Load(dir);
                } catch (ChessException ex) {
                    System.out.println(ex.getMessage());
                    JOptionPane.showMessageDialog(this,ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                ChessBoard Replay = new ChessBoard();
                String name = dir.substring(dir.lastIndexOf("/") + 1);
                this.Game = Replay;
                Game.mainFrame = this;
                try {
                    Game.LoadReplay(data, name.substring(0, name.length() - 6));
                } catch (ChessException ex) {
                    JOptionPane.showMessageDialog(this,ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                pro1.setVisible(true);
                pro2.setVisible(true);
                local[0] = Game.players[0];
                local[1] = Game.players[1];
                PlayerName1.setText(Game.players[1].id);
                PlayerName2.setText(Game.players[0].id);
                printTurnAndRound();
                generate(true);
                ReplayLast.setVisible(true);
                ReplayNext.setVisible(true);
                CheatButton.setVisible(false);
            }
        });

        ReplayLast.setText("<");
        ReplayLast.setFont(new Font("Rockwell", Font.BOLD, 15));
        ReplayLast.setBounds(35, 300, 50,50);
        this.add(ReplayLast);
        ReplayLast.setVisible(false);
        ReplayLast.addActionListener((e)->{
            if(Game.steps != 0){
                Game.LoadPoint(true);
                generate(false);
                Game.Show();
                printTurnAndRound();
            }
        });

        ReplayNext.setText(">");
        ReplayNext.setFont(new Font("Rockwell", Font.BOLD, 15));
        ReplayNext.setBounds(450, 300, 50,50);
        this.add(ReplayNext);
        ReplayNext.setVisible(false);
        ReplayNext.addActionListener((e)->{
            try {
                Game.nextStep(Game.opt_stack.get(Game.steps),3);
            } catch (ChessException ex) {
                JOptionPane.showMessageDialog(this,ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
                Game = null;
                this.started = false;
                RoundLabel.setText("");
                TurnLabel.setText("");
                MessagePane.setText("");
                this.Message = "";
                for(int y = 0; y < 8; y++) {
                    for(int x = 0; x < 4; x++) {
                        GameBoard[x][y].setVisible(false);
                        GameBoard[x][y].rank = -1;
                        GameBoard[x][y].player = -1;
                        GameBoard[x][y].isRevealed = false;
                        GameBoard[x][y].repaint();
                    }
                }
                return;
            }
            printTurnAndRound();
        });
    }

    private void AddLabel() {
        //---- Init ----
        this.TurnLabel = new JLabel();
        this.RoundLabel = new JLabel();

        //---- TurnLabel ----
        TurnLabel.setFont(new Font("Rockwell", Font.PLAIN, 14));
        TurnLabel.setHorizontalAlignment(JLabel.CENTER);
        TurnLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.add(TurnLabel);
        TurnLabel.setBounds(430, 60, 115, 40);

        //---- RoundLabel ----
        RoundLabel.setFont(new Font("Rockwell", Font.PLAIN, 18));
        RoundLabel.setHorizontalAlignment(JLabel.CENTER);
        RoundLabel.setHorizontalTextPosition(JLabel.CENTER);
        this.add(RoundLabel);
        RoundLabel.setBounds(430, 25, 110, 40);
    }

    private void AddInfoArea() {
        //---- Init ----
        this.scrollPane1 = new JScrollPane();
        this.MessagePane = new JTextPane();
        this.scrollPane2 = new JScrollPane();
        this.RankPane = new JTextPane();

        //======== scrollPane1 ========
        //---- MessagePane ----
        MessagePane.setEditable(false);
        scrollPane1.setViewportView(MessagePane);
        this.add(scrollPane1);
        scrollPane1.setBounds(560, 5, 215, 425);

        //======== scrollPane2 ========
        //---- RankPane ----
        RankPane.setEditable(false);
        RankPane.setFont(new Font("Space Mono", Font.PLAIN, 14));
        scrollPane2.setViewportView(RankPane);
        this.add(scrollPane2);
        scrollPane2.setBounds(560, 435, 215, 230);
    }

    private void AddPlayerInfo() {
        //---- Init ----
        this.pro1 = new JLabel();
        this.pro2 = new JLabel();
        this.PlayerName1 = new JLabel();
        this.PlayerName2 = new JLabel();

        //---- pro1 ----
        pro1.setIcon(new ImageIcon("resources/profile1.png"));
        this.add(pro1);
        pro1.setBounds(40, 50, 33, 33);
        pro1.setVisible(false);

        //---- pro2 ----
        pro2.setIcon(new ImageIcon("resources/profile2.png"));
        this.add(pro2);
        pro2.setBounds(460, 520, 33, 33);
        pro2.setVisible(false);

        //---- PlayerName1 ----
        PlayerName1.setText("");
        PlayerName1.setFont(PlayerName1.getFont().deriveFont(PlayerName1.getFont().getSize() + 6f));
        PlayerName1.setBorder(null);
        PlayerName1.setOpaque(false);
        PlayerName1.setHorizontalAlignment(SwingConstants.CENTER);
        PlayerName1.setHorizontalTextPosition(SwingConstants.CENTER);
        this.add(PlayerName1);
        PlayerName1.setBounds(15, 25, 85, 25);

        //---- PlayerName2 ----
        PlayerName2.setText("");
        PlayerName2.setFont(PlayerName2.getFont().deriveFont(PlayerName2.getFont().getSize() + 6f));
        PlayerName2.setBorder(null);
        PlayerName2.setOpaque(false);
        PlayerName2.setHorizontalAlignment(SwingConstants.CENTER);
        PlayerName2.setHorizontalTextPosition(SwingConstants.CENTER);
        this.add(PlayerName2);
        PlayerName2.setBounds(435, 550, 85, 25);
    }

    public void printMess(String mess) {
        this.Message += mess;
        MessagePane.setText(this.Message);
    }

    public void printRank(String mess) {
        String info = "+------+------+-------+\n|  ID  |Rating| Score |\n+------+------+-------+\n";
        RankPane.setText(info + mess);
    }

    public void printTurnAndRound() {
        int turn = Game.turn;
        String player = turn == 0 ? Game.players[0].id : Game.players[1].id;
        int round = Game.steps;
        RoundLabel.setText(String.format("ROUND %2d", round));
        TurnLabel.setText(player + "'s Turn");
    }

    public void showGameOver(String dir, int status) {
        started = false;
        CheatButton.setVisible(false);
        if(status == 1) {
            JOptionPane.showMessageDialog(this,"You Won!\nSave at: " + dir);
        } else if(status == 0) {
            JOptionPane.showMessageDialog(this,"Draw!\nSave at: " + dir);
        } else {
            JOptionPane.showMessageDialog(this,"You Lost!\nSave at: " + dir);
        }
    }

    private void Connect() throws ChessException {
        String[] option = {"Guest", "Host"};
        int select = JOptionPane.showOptionDialog(this, "Decide if you're the host", "Connect",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, option, null);
        String ip = "";
        if(select == 0) {
            do {
                ip = JOptionPane.showInputDialog(this, "Target IP: ", "Connect", JOptionPane.PLAIN_MESSAGE);
                if(!ip.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))"))
                    JOptionPane.showMessageDialog(this,"Invalid IP");
            } while(!ip.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))"));
        }
        int port;
        do {
            port = Integer.parseInt(JOptionPane.showInputDialog(this, "Target port: ", "Connect", JOptionPane.PLAIN_MESSAGE));
            if(port < 1 || port > 65535)
                JOptionPane.showMessageDialog(this,"Invalid port");
        } while (port < 1 || port > 65535);
        JOptionPane.showMessageDialog(this,"Waiting Connection...","Success",JOptionPane.PLAIN_MESSAGE);
        Game.NetworkInit(ip, port, select, local[0]);
    }

    public void PlayBGM(int mode) {
        if(bgm.isActive()) {
            bgm.stop();
            bgm.close();
        }

        AudioInputStream inputStream = null;
        if(mode == 0) {
            try {
                inputStream = AudioSystem.getAudioInputStream(new File("resources/hall.wav"));
            } catch (UnsupportedAudioFileException | IOException e) {
                throw new RuntimeException(e);
            }
        } else if(mode == 1) {
            try {
                inputStream = AudioSystem.getAudioInputStream(new File("resources/combat.wav"));
            } catch (UnsupportedAudioFileException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            bgm.open(inputStream);
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }

        bgm.loop(Clip.LOOP_CONTINUOUSLY);
    }

    private Player Login() {
        String[] logoption = {"Sign in", "Sign up"};
        int login = JOptionPane.showOptionDialog(this, "Sign up or Sign in", "Login",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, logoption, null);
        if(login == 0) {
            if(list.size()==0) {
                JOptionPane.showMessageDialog(this,"Empty User List","Warning",JOptionPane.WARNING_MESSAGE);
                return null;
            }
            Player tmp = null;
            while (true) {
                String id = JOptionPane.showInputDialog(this, "Account: ", "Login",JOptionPane.PLAIN_MESSAGE);
                if(id == null || id.equals("")) return null;
                for(Player o: list) {
                    if(o.id.equals(id)) {
                        tmp = o;
                    }
                }
                if (tmp == null) {
                    JOptionPane.showMessageDialog(this,"Invalid User!","Warning",JOptionPane.WARNING_MESSAGE);
                } else {
                    String passwd;
                    do {
                        passwd = JOptionPane.showInputDialog(this, "Password: ", "Login", JOptionPane.PLAIN_MESSAGE);
                        if(passwd == null || passwd.equals("")) return null;
                    } while(!tmp.login(passwd));
                    StringBuilder hist = new StringBuilder();
                    for(String str : tmp.history) {
                        hist.append(str).append("\n");
                    }
                    if(!hist.isEmpty())
                        JOptionPane.showMessageDialog(this,hist.toString(),"History",JOptionPane.PLAIN_MESSAGE);
                    return tmp;
                }
            }
        } else {
            String id;
            do {
                id = JOptionPane.showInputDialog(this, "Create User: ", "Sign up",JOptionPane.PLAIN_MESSAGE);
                if(id == null || id.equals("")) return null;
                if(!id.matches("^[a-zA-Z0-9_]{0,15}$")) {
                    JOptionPane.showMessageDialog(this,"Invalid Name","Warning",JOptionPane.PLAIN_MESSAGE);
                }
            } while (!id.matches("^[a-zA-Z0-9_]{0,15}$"));
            String passwd = JOptionPane.showInputDialog(this, "Password: ", "Login", JOptionPane.PLAIN_MESSAGE);
            Player tmp = new Player(id, 3, passwd);
            try {
                FileOperation.SaveUser(tmp);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            JOptionPane.showMessageDialog(this,"User created","Success",JOptionPane.PLAIN_MESSAGE);
            try {
                list = FileOperation.ScanUser("User/");
            } catch (ChessException ex) {
                JOptionPane.showMessageDialog(this,ex.getMessage(),"Warning",JOptionPane.WARNING_MESSAGE);
                return null;
            }
            Collections.sort(list);
            StringBuilder rankness = new StringBuilder();
            for(Player o: list)
                rankness.append(String.format("%-8s %5d %7d\n", o.id, o.rating, o.score));
            printRank(rankness.toString());
            return tmp;
        }
    }
}
